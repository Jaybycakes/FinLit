package com.code.finlit.data.remote

import com.code.finlit.data.remote.dto.ScamAnalysisRequest
import com.code.finlit.data.remote.dto.ScamAnalysisResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface ScamApi {
    @POST("sentinv2-evaluate")
    suspend fun analyzeScam(@Body request: ScamAnalysisRequest): ScamAnalysisResponse
}
