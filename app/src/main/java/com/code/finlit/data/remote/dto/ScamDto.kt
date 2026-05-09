package com.code.finlit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScamAnalysisRequest(
    @SerialName("Scenario") val scenario: String
)

@Serializable
data class ScamAnalysisResponse(
    val persona: String,
    @SerialName("risk_analysis") val riskAnalysis: String,
    @SerialName("recommended_action") val recommendedAction: String
)
