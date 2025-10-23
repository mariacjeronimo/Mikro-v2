package com.example.mikrov2


import android.content.ComponentName

import android.content.Context

import android.hardware.Sensor

import android.hardware.SensorEvent

import android.hardware.SensorEventListener

import android.hardware.SensorManager

import android.util.Log // Importação necessária para usar Log

import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

import com.example.mikrov2.complication.ActiveComplicationService

import com.example.mikrov2.complication.SedentaryComplicationService

import kotlinx.coroutines.*

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import java.util.concurrent.atomic.AtomicInteger


class StepsDataRepository private constructor(private val context: Context) : SensorEventListener {


    private val TAG = "StepsDataRepository"

    private val activeComp = ComponentName(context, ActiveComplicationService::class.java)

    private val inactiveComp = ComponentName(context, SedentaryComplicationService::class.java)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)


    private val _activeMinutes = MutableStateFlow(0)

    val activeMinutes: StateFlow<Int> = _activeMinutes


    private val _inactiveMinutes = MutableStateFlow(0)

    val inactiveMinutes: StateFlow<Int> = _inactiveMinutes


// Variáveis de controlo

    private var lastTotalSensorSteps = 0

    private val stepsInCurrentMinute = AtomicInteger(0)


    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    init {

        Log.d(TAG, "StepsDataRepository inicializado.") // LOG: Inicialização do Repositório


// 1. Iniciar o Sensor para acumular passos (apenas registra, não processa a lógica de tempo)

        stepCounter?.let {

            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)

            Log.d(TAG, "SensorEventListener registado com sucesso.") // LOG: Confirmação do registo

        } ?: run {

            Log.e(TAG, "Sensor TYPE_STEP_COUNTER NÃO DISPONÍVEL neste dispositivo.")

        }


// 2. Iniciar o Timer para aplicar a lógica de minutos ativos/inativos

        scope.launch {

            startMinuteTimer()

        }

    }


// --- Lógica do Sensor (ACUMULADOR) ---

    override fun onSensorChanged(event: SensorEvent) {

        val totalSensorSteps = event.values[0].toInt()


        Log.v(TAG, "EVENTO SENSOR: Total de passos lidos: $totalSensorSteps") // LOG VITAL: Recebeu evento?


        if (lastTotalSensorSteps == 0) {

// Inicialização (Primeira leitura)

            lastTotalSensorSteps = totalSensorSteps

            Log.d(TAG, "Primeira leitura do sensor. Valor inicial: $totalSensorSteps")

            return // Sai da função após a inicialização

        }


// Passos tomados desde o último evento do sensor

        val stepsTaken = totalSensorSteps - lastTotalSensorSteps

        lastTotalSensorSteps = totalSensorSteps


// Acumula os passos para a lógica que será executada pelo timer

        if (stepsTaken > 0) {

            stepsInCurrentMinute.addAndGet(stepsTaken)

            Log.i(TAG, "Passos tomados desde o último evento: $stepsTaken. Acumulado no minuto: ${stepsInCurrentMinute.get()}") // LOG: Passos acumulados

        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        Log.v(TAG, "Acurácia do sensor alterada para: $accuracy")

    }


// --- Lógica do Timer (PROCESSAMENTO E ATUALIZAÇÃO) ---

    private suspend fun startMinuteTimer() {

        while (scope.isActive) {

            Log.d(TAG, "TIMER: A dormir por 60 segundos...") // LOG: Início do Timer Sleep

            delay(60_000) // Esperar 60 segundos (1 minuto)


            Log.d(TAG, "TIMER: Acordou. Processando minuto.") // LOG: Timer acordou


// 1. Obter e redefinir o contador para o próximo minuto

            val stepsLastMinute = stepsInCurrentMinute.getAndSet(0)

            Log.d(TAG, "TIMER: Passos acumulados no minuto anterior: $stepsLastMinute") // LOG: Passos no último minuto


// 2. Aplicar a regra (Minuto Ativo: > 10 passos)

            if (stepsLastMinute > 10) {

                _activeMinutes.value += 1

                Log.i(TAG, "RESULTADO: Minuto Ativo. Total: ${_activeMinutes.value}")

            } else {

                _inactiveMinutes.value += 1

                Log.i(TAG, "RESULTADO: Minuto Inativo. Total: ${_inactiveMinutes.value}")

            }


// 3. Forçar a Complicação a atualizar o ecrã

            updateComplications()

        }

    }


    private fun updateComplications() {

        ComplicationDataSourceUpdateRequester

            .create(context, activeComp)

            .requestUpdateAll()

        Log.d(TAG, "UPDATE: requestUpdateAll enviado para a complicação ActiveSteps.") // LOG: Notificação enviada

        ComplicationDataSourceUpdateRequester

            .create(context, inactiveComp)

            .requestUpdateAll()

        Log.d(TAG, "UPDATE: requestUpdateAll enviado para InactiveSteps.")


    }


    fun unregister() {

        sensorManager.unregisterListener(this)

        scope.cancel()

        Log.d(TAG, "Cleanup: SensorListener e Coroutine cancelados.")

    }


    companion object {

        @Volatile private var INSTANCE: StepsDataRepository? = null

        fun getInstance(context: Context) =

            INSTANCE ?: synchronized(this) {

                INSTANCE ?: StepsDataRepository(context.applicationContext).also { INSTANCE = it }

            }

    }

} 