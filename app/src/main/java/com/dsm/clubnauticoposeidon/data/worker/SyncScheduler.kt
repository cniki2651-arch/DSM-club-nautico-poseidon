package com.dsm.clubnauticoposeidon.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {

    // Se ejecuta solo cuando hay internet; si falla, reintenta con espera creciente
    fun sincronizarConsumos(context: Context) {
        val request = OneTimeWorkRequestBuilder<SincronizarConsumosWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_consumos",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}
