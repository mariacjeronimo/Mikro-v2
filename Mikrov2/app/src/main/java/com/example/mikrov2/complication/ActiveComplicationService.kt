package com.example.mikrov2.complication

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.mikrov2.StepsDataRepository
import androidx.wear.watchface.complications.data.MonochromaticImage


/**
 * Skeleton for complication data source that returns short text.
 * A complication is any feature that is displayed on a watch face in addition to the time.
 */
class ActiveComplicationService : SuspendingComplicationDataSourceService() {

    private val TAG = "ActiveStepsComp"

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) {
            Log.w(TAG, "Tipo de complicação não suportado: ${request.complicationType}")
            return null
        }

        // Obtém o repositório singleton
        val repository = StepsDataRepository.getInstance(applicationContext)

        // Lê o valor atual de minutos ativos
        val activeMinutes = repository.activeMinutes.value

        Log.d(TAG, "Minutos Ativos Lidos: $activeMinutes")


        // Cria a complicação
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(activeMinutes.toString()).build(),
            contentDescription = PlainComplicationText.Builder("Minutos Ativos").build()
        )
            .setTitle(PlainComplicationText.Builder("ACTIVE").build())
            .build()
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder("12").build(),
                contentDescription = PlainComplicationText.Builder("Minutos Ativos").build()
            )
                .setTitle(PlainComplicationText.Builder("ACTIVE").build())
                .build()
            else -> null
        }
    }
}