package com.code.finlit.presentation.scam

import androidx.lifecycle.ViewModel
import com.code.finlit.domain.repository.ScamRepository
import com.code.finlit.presentation.users.toUserMessage
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class ScamScannerViewModel(
    private val repository: ScamRepository,
) : ViewModel(), ContainerHost<ScamScannerState, ScamScannerSideEffect> {

    override val container = container<ScamScannerState, ScamScannerSideEffect>(ScamScannerState())

    fun onIntent(intent: ScamScannerIntent) = when (intent) {
        is ScamScannerIntent.MessageChanged -> updateMessage(intent.text)
        ScamScannerIntent.ClearMessage -> clearMessage()
        ScamScannerIntent.PasteFromClipboard -> requestPaste()
        ScamScannerIntent.AnalyzeMessage -> analyzeMessage()
        ScamScannerIntent.DismissResult -> dismissResult()
    }

    private fun updateMessage(text: String) = intent {
        reduce { state.copy(messageText = text, error = null) }
    }

    private fun clearMessage() = intent {
        reduce { state.copy(messageText = "", analysis = null, error = null) }
    }

    private fun requestPaste() = intent {
        postSideEffect(ScamScannerSideEffect.RequestClipboardPaste)
    }

    private fun analyzeMessage() = intent {
        if (state.messageText.isBlank()) {
            postSideEffect(ScamScannerSideEffect.ShowMessage("Please enter a message to analyze"))
            return@intent
        }

        reduce { state.copy(isAnalyzing = true, error = null, analysis = null) }
        
        repository.analyzeScam(state.messageText)
            .onSuccess { analysis ->
                android.util.Log.d("ScamScanner", "Analysis successful: $analysis")
                reduce { state.copy(isAnalyzing = false, analysis = analysis) }
            }
            .onFailure { error ->
                android.util.Log.e("ScamScanner", "Analysis failed", error)
                android.util.Log.e("ScamScanner", "Error type: ${error::class.simpleName}")
                android.util.Log.e("ScamScanner", "Error message: ${error.message}")
                
                val errorMessage = when {
                    error.message?.contains("Unable to resolve host") == true -> 
                        "Network error: Cannot reach server. Check your internet connection."
                    error.message?.contains("timeout") == true -> 
                        "Request timeout. Please try again."
                    error.message?.contains("JSON") == true -> 
                        "Server response format error: ${error.message}"
                    else -> "Error: ${error.message ?: error.toUserMessage()}"
                }
                
                reduce { state.copy(isAnalyzing = false, error = errorMessage) }
                postSideEffect(ScamScannerSideEffect.ShowMessage(errorMessage))
            }
    }

    private fun dismissResult() = intent {
        reduce { state.copy(analysis = null, error = null) }
    }
}
