package com.code.finlit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.code.finlit.presentation.scam.ScamScannerRoute
import com.code.finlit.ui.theme.FinLitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinLitTheme {
                ScamScannerRoute(
                    onBack = { finish() }
                )
            }
        }
    }
}
