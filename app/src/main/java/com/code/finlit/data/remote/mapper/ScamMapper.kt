package com.code.finlit.data.remote.mapper

import com.code.finlit.data.remote.dto.ScamAnalysisResponse
import com.code.finlit.domain.model.ScamAnalysis

fun ScamAnalysisResponse.toDomain(): ScamAnalysis = ScamAnalysis(
    persona = persona,
    riskAnalysis = riskAnalysis,
    recommendedAction = recommendedAction
)
