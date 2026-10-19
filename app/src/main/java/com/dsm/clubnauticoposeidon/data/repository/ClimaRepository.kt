package com.dsm.clubnauticoposeidon.data.repository


import com.dsm.clubnauticoposeidon.data.remote.ClimaApi
import com.dsm.clubnauticoposeidon.data.remote.MarineApi
import com.dsm.clubnauticoposeidon.data.remote.RetrofitClient
import com.dsm.clubnauticoposeidon.model.ClimaActual
import com.dsm.clubnauticoposeidon.model.ClimaDetalle

class ClimaRepository(
    private val api: ClimaApi = RetrofitClient.climaApi,
    private val marineApi: MarineApi = RetrofitClient.marineApi
)