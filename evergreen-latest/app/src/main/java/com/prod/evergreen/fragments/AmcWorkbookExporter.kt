package com.prod.evergreen.fragments

import android.content.Context
import android.net.Uri
import com.prod.evergreen.models.AMCData
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Writes a company report in the AMC upload workbook layout without exporting credentials. */
internal object AmcWorkbookExporter {
    private val headers = listOf(
        "company_name", "branch_name", "company_email", "company_location", "start_date", "end_date",
        "admin_name", "admin_phone", "admin_email", "admin_password", "admin_location"
    )

    private val instructions = listOf(
        "How to use this sheet",
        "Use the AMCs sheet. Keep the first row headers exactly as they are.",
        "Each row creates one AMC/company and its client admin.",
        "Required: company_name, branch_name, admin_name, admin_phone, admin_email, admin_password.",
        "admin_phone must be a 10-digit Indian mobile number.",
        "Optional: company_email, company_location, start_date, end_date (YYYY-MM-DD), admin_location.",
        "If dates are empty, start_date becomes today and end_date one year later.",
        "Do not reuse a company+branch pair or an existing admin phone/email.",
        "Delete the sample rows before uploading real data, or change phones/emails so they are unique."
    )

    fun write(context: Context, companies: List<AMCData>, destination: Uri) {
        val dataRows = companies.map { company ->
            val admin = company.pocDetails?.user
            listOf(
                company.name.orEmpty(),
                company.branchName.orEmpty(),
                company.email.orEmpty(),
                company.location.orEmpty(),
                company.startDate.orEmpty(),
                company.endDate.orEmpty(),
                admin?.name.orEmpty(),
                admin?.phone.orEmpty(),
                admin?.email.orEmpty(),
                "",
                ""
            )
        }
        val strings = headers + dataRows.flatten() + instructions
        val instructionsStringOffset = headers.size + dataRows.flatten().size
        val input = context.assets.open("evergreen_amc_template.xlsx")
        val output = context.contentResolver.openOutputStream(destination)
            ?: error("Could not open the selected save location")
        input.use { template ->
            output.use { target ->
                ZipInputStream(template).use { zipInput ->
                    ZipOutputStream(target).use { zipOutput ->
                        var entry = zipInput.nextEntry
                        while (entry != null) {
                            val content = when (entry.name) {
                                "xl/sharedStrings.xml" -> sharedStringsXml(strings).toByteArray(Charsets.UTF_8)
                                "xl/worksheets/sheet1.xml" -> companiesSheetXml(dataRows).toByteArray(Charsets.UTF_8)
                                "xl/worksheets/sheet2.xml" -> instructionsSheetXml(instructionsStringOffset).toByteArray(Charsets.UTF_8)
                                else -> zipInput.readBytes()
                            }
                            zipOutput.putNextEntry(ZipEntry(entry.name))
                            zipOutput.write(content)
                            zipOutput.closeEntry()
                            zipInput.closeEntry()
                            entry = zipInput.nextEntry
                        }
                    }
                }
            }
        }
    }

    private fun sharedStringsXml(values: List<String>) = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"${values.size}\" uniqueCount=\"${values.size}\">")
        values.forEach { append("<si><t xml:space=\"preserve\">${it.escapeXml()}</t></si>") }
        append("</sst>")
    }

    private fun companiesSheetXml(dataRows: List<List<String>>) = buildString {
        val rows = listOf(headers) + dataRows
        val columns = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"A1:K${rows.size}\"/><sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/><selection pane=\"bottomLeft\"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight=\"15\"/><cols><col min=\"1\" max=\"11\" width=\"22\" customWidth=\"1\"/></cols><sheetData>")
        var index = 0
        rows.forEachIndexed { rowIndex, row ->
            append("<row r=\"${rowIndex + 1}\">")
            row.forEachIndexed { columnIndex, _ ->
                append("<c r=\"${columns[columnIndex]}${rowIndex + 1}\" t=\"s\"><v>${index++}</v></c>")
            }
            append("</row>")
        }
        append("</sheetData><autoFilter ref=\"A1:K${rows.size}\"/></worksheet>")
    }

    private fun instructionsSheetXml(stringOffset: Int) = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"A1:A${instructions.size}\"/><sheetFormatPr defaultRowHeight=\"15\"/><cols><col min=\"1\" max=\"1\" width=\"110\" customWidth=\"1\"/></cols><sheetData>")
        instructions.forEachIndexed { index, _ ->
            append("<row r=\"${index + 1}\"><c r=\"A${index + 1}\" t=\"s\"><v>${stringOffset + index}</v></c></row>")
        }
        append("</sheetData></worksheet>")
    }

    private fun String.escapeXml(): String = replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")
}
