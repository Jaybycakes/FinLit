package com.code.finlit.presentation.scam

import com.code.finlit.domain.model.ScamAnalysis

data class ScamScannerState(
    val messageText: String = "",
    val isAnalyzing: Boolean = false,
    val analysis: ScamAnalysis? = null,
    val error: String? = null,
)

sealed interface ScamScannerIntent {
    data class MessageChanged(val text: String) : ScamScannerIntent
    data object ClearMessage : ScamScannerIntent
    data object PasteFromClipboard : ScamScannerIntent
    data object AnalyzeMessage : ScamScannerIntent
    data object DismissResult : ScamScannerIntent
}

sealed interface ScamScannerSideEffect {
    data class ShowMessage(val message: String) : ScamScannerSideEffect
    data object RequestClipboardPaste : ScamScannerSideEffect
}
