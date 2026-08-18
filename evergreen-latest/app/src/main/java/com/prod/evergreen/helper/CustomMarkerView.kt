package com.prod.evergreen.helper



import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.prod.evergreen.R

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.customTooltip)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            tvContent.text = "${e.y} KgCo2e"
        }
        super.refreshContent(e, highlight)
    }

    fun getXOffset(xPosition: Float): Int {
        // Center the marker horizontally
        return -(width / 2)
    }

    fun getYOffset(yPosition: Float): Int {
        // Show the marker above the data point with an offset
        return -height - 20 // Adjust the offset as needed
    }


}
