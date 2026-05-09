package com.code.finlit.data.repository

import com.code.finlit.data.remote.ScamApi
import com.code.finlit.data.remote.dto.ScamAnalysisRequest
import com.code.finlit.data.remote.mapFailure
import com.code.finlit.data.remote.mapper.toDomain
import com.code.finlit.data.remote.toNetworkError
import com.code.finlit.domain.model.ScamAnalysis
import com.code.finlit.domain.repository.ScamRepository

class ScamRepositoryImpl(private val api: ScamApi) : ScamRepository {

    override suspend fun analyzeScam(scenario: String): Result<ScamAnalysis> = runCatching {
        api.analyzeScam(ScamAnalysisRequest(scenario)).toDomain()
    }.mapFailure { it.toNetworkError() }
}
