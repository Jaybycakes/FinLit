package com.code.finlit.domain.repository

import com.code.finlit.domain.model.ScamAnalysis

interface ScamRepository {
    suspend fun analyzeScam(scenario: String): Result<ScamAnalysis>
}
