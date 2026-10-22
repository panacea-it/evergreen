package com.prod.evergreen.helper

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class EquipmentWorkbookRecord(
    var name: String = "",
    var make: String = "",
    var model: String = "",
    var specifications: String = "",
    var location: String = "",
    var description: String = "",
    var tmFrequency: String = "",
    var manufacturerDate: String = "",
    var egSerialNumber: String = ""
)

/** Reads and rewrites the existing equipment upload workbook format for review-before-upload. */
object EquipmentWorkbookReview {
    val headers = listOf(
        "name", "make", "model", "specifications", "location", "description",
        "tm_frequency", "manufacturer_date", "eg_serial_number"
    )

    fun read(context: Context, uri: Uri): List<EquipmentWorkbookRecord> {
        val entries = mutableMapOf<String, ByteArray>()
        context.contentResolver.openInputStream(uri)?.use { source ->
            ZipInputStream(source).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && (entry.name == "xl/sharedStrings.xml" || entry.name == "xl/worksheets/sheet1.xml")) {
                        entries[entry.name] = zip.readBytes()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("Could not open the selected Excel file")

        val worksheet = entries["xl/worksheets/sheet1.xml"] ?: error("Excel file has no first worksheet")
        val sharedStrings = entries["xl/sharedStrings.xml"]?.let(::parseSharedStrings).orEmpty()
        val rows = parseWorksheet(worksheet, sharedStrings)
        val headerRow = rows.firstOrNull() ?: error("The worksheet is empty")
        val columnByHeader = headerRow.mapNotNull { (column, value) ->
            value.trim().lowercase().replace(Regex("\\s+"), "_").takeIf { it.isNotBlank() }
                ?.let { it to column }
        }.toMap()
        val required = listOf("name", "make", "model")
        val missing = required.filterNot(columnByHeader::containsKey)
        if (missing.isNotEmpty()) error("Missing required columns: ${missing.joinToString()}")

        return rows.drop(1).mapNotNull { row ->
            fun value(header: String): String {
                val column = columnByHeader[header] ?: return ""
                return row[column].orEmpty().trim()
            }
            val record = EquipmentWorkbookRecord(
                name = value("name"),
                make = value("make"),
                model = value("model"),
                specifications = value("specifications"),
                location = value("location"),
                description = value("description"),
                tmFrequency = value("tm_frequency"),
                manufacturerDate = value("manufacturer_date").toExcelDateIfNumeric(),
                egSerialNumber = value("eg_serial_number")
            )
            record.takeUnless {
                listOf(it.name, it.make, it.model, it.specifications, it.location, it.description,
                    it.tmFrequency, it.manufacturerDate, it.egSerialNumber).all(String::isBlank)
            }
        }
    }

    fun write(context: Context, records: List<EquipmentWorkbookRecord>, destination: File) {
        val rows = records.map { record ->
            listOf(
                record.name, record.make, record.model, record.specifications, record.location,
                record.description, record.tmFrequency, record.manufacturerDate, record.egSerialNumber
            )
        }
        val strings = headers + rows.flatten()
        context.assets.open("evergreen_equipment_template.xlsx").use { source ->
            FileOutputStream(destination).use { output ->
                ZipInputStream(source).use { input ->
                    ZipOutputStream(output).use { zipOutput ->
                        var entry = input.nextEntry
                        while (entry != null) {
                            val bytes = when (entry.name) {
                                "xl/sharedStrings.xml" -> sharedStringsXml(strings).toByteArray(Charsets.UTF_8)
                                "xl/worksheets/sheet1.xml" -> worksheetXml(rows).toByteArray(Charsets.UTF_8)
                                else -> input.readBytes()
                            }
                            zipOutput.putNextEntry(ZipEntry(entry.name))
                            zipOutput.write(bytes)
                            zipOutput.closeEntry()
                            input.closeEntry()
                            entry = input.nextEntry
                        }
                    }
                }
            }
        }
    }

    private fun parseSharedStrings(xml: ByteArray): List<String> {
        val parser = parserFor(xml)
        val result = mutableListOf<String>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG || parser.tag() != "si") continue
            val text = StringBuilder()
            while (!(parser.eventType == XmlPullParser.END_TAG && parser.tag() == "si")) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.tag() == "t") {
                    text.append(parser.nextText())
                } else {
                    parser.next()
                }
            }
            result.add(text.toString())
        }
        return result
    }

    private fun parseWorksheet(xml: ByteArray, sharedStrings: List<String>): List<Map<Int, String>> {
        val parser = parserFor(xml)
        val rows = mutableListOf<Map<Int, String>>()
        var currentRow: MutableMap<Int, String>? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.tag() == "row") {
                currentRow = linkedMapOf()
            } else if (parser.eventType == XmlPullParser.START_TAG && parser.tag() == "c") {
                val reference = parser.getAttributeValue(null, "r").orEmpty()
                val type = parser.getAttributeValue(null, "t").orEmpty()
                val column = columnIndex(reference)
                var value = ""
                while (!(parser.eventType == XmlPullParser.END_TAG && parser.tag() == "c")) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.tag() in setOf("v", "t")) {
                        value = parser.nextText()
                    } else {
                        parser.next()
                    }
                }
                if (column >= 0) {
                    currentRow?.set(
                        column,
                        if (type == "s") sharedStrings.getOrNull(value.toIntOrNull() ?: -1).orEmpty() else value
                    )
                }
            } else if (parser.eventType == XmlPullParser.END_TAG && parser.tag() == "row") {
                currentRow?.let(rows::add)
                currentRow = null
            }
        }
        return rows
    }

    private fun parserFor(xml: ByteArray): XmlPullParser = Xml.newPullParser().apply {
        setInput(ByteArrayInputStream(xml), "UTF-8")
    }

    private fun XmlPullParser.tag(): String = name?.substringAfter(':').orEmpty()

    private fun columnIndex(reference: String): Int {
        var result = 0
        var sawLetter = false
        for (character in reference) {
            if (!character.isLetter()) break
            sawLetter = true
            result = result * 26 + (character.uppercaseChar() - 'A' + 1)
        }
        return if (sawLetter) result - 1 else -1
    }

    private fun String.toExcelDateIfNumeric(): String {
        val serial = toDoubleOrNull() ?: return this
        if (serial < 1 || serial > 100_000) return this
        val date = java.time.LocalDate.of(1899, 12, 30).plusDays(serial.toLong())
        return date.toString()
    }

    private fun sharedStringsXml(values: List<String>) = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"${values.size}\" uniqueCount=\"${values.size}\">")
        values.forEach { append("<si><t xml:space=\"preserve\">${it.escapeXml()}</t></si>") }
        append("</sst>")
    }

    private fun worksheetXml(rows: List<List<String>>) = buildString {
        val allRows = listOf(headers) + rows
        val columns = ('A'..'I').map(Char::toString)
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"A1:I${allRows.size}\"/><sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/><selection pane=\"bottomLeft\"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight=\"15\"/><cols><col min=\"1\" max=\"9\" width=\"22\" customWidth=\"1\"/></cols><sheetData>")
        var stringIndex = 0
        allRows.forEachIndexed { rowIndex, row ->
            append("<row r=\"${rowIndex + 1}\">")
            row.forEachIndexed { columnIndex, _ ->
                val style = if (rowIndex == 0) " s=\"1\"" else ""
                append("<c r=\"${columns[columnIndex]}${rowIndex + 1}\" t=\"s\"$style><v>${stringIndex++}</v></c>")
            }
            append("</row>")
        }
        append("</sheetData><autoFilter ref=\"A1:I${allRows.size}\"/></worksheet>")
    }

    private fun String.escapeXml(): String = replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")
}
