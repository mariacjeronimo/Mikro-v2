package com.example.mikrov2.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.mikrov2.presentation.MainActivity
import java.util.Calendar

/**
 * Skeleton for complication data source that returns short text.
 * A complication is any feature that is displayed on a watch face in addition to the time.
 */
class TestService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val prefs = this.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)

        val glassCount = prefs.getInt("glasses_drank", 0).toFloat()
        val maxGlasses = prefs.getInt("max_glasses", 8).toFloat()

        val text = "${glassCount.toInt()}/${maxGlasses.toInt()}"
        val contentDescription =
            "You've had ${glassCount.toInt()} out of ${maxGlasses.toInt()} glasses of water"

        return createRangedValueComplicationData(
            current = glassCount,
            max = maxGlasses,
            text = text,
            contentDescription = contentDescription
        )
    }


    private fun createRangedValueComplicationData(
            current: Float,
            max: Float,
            text: String,
            contentDescription: String
        ): ComplicationData {
/**val monochromaticImage = MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.drinking_water_icon)
            ).build()*/
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            return RangedValueComplicationData.Builder(
                value = current,
                min = 0f,
                max = max,
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
                .setText(PlainComplicationText.Builder(text).build())
                .setTapAction(pendingIntent)
                //.setMonochromaticImage(monochromaticImage)
                .build()
        }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.RANGED_VALUE) {
            return null
        }
        return createRangedValueComplicationData(
            current = 0f,
            max = 8f,
            text = "0 out of 8",
            contentDescription = "You've had 0 out of 8 glasses of water"
        )
    }


}

