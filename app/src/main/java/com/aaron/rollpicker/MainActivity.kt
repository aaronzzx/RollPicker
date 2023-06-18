package com.aaron.rollpicker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.aaron.rollpicker.App.Companion.applicationContext
import com.aaron.rollpicker.ui.theme.RollPickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RollPickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Picker3()
                    }
                }
            }
        }
    }
}

@Composable
private fun Picker1() {
    VerticalRollPicker(
        modifier = Modifier
            .size(300.dp)
            .background(Color(0xFFF0F0F0)),
        count = 10,
        onPick = { index ->
            showToast(formatInt(index.value))
        }
    ) { index ->
        RollPickerText(text = formatInt(index.value))
    }
}

@Composable
private fun Picker2() {
    VerticalRollPicker(
        modifier = Modifier
            .size(300.dp)
            .background(Color(0xFFF0F0F0)),
        count = 10,
        loop = true,
        onPick = { index ->
            showToast(formatInt(index.value))
        }
    ) { index ->
        RollPickerText(text = formatInt(index.value))
    }
}

@Composable
private fun Picker3() {
    VerticalRollPicker(
        modifier = Modifier
            .size(300.dp)
            .background(Color(0xFFF0F0F0)),
        count = 10,
        loop = true,
        style = RollPickerStyle.Flat(),
        onPick = { index ->
            showToast(formatInt(index.value))
        }
    ) { index ->
        RollPickerText(
            modifier = Modifier.graphicsLayer {
                val offsetFraction = calculateOffsetFraction(index)
                rotationY = offsetFraction * (90f / ((visibleCount + 1) / 2))
            },
            text = formatInt(index.value)
        )
    }
}

private fun formatInt(index: Int): String {
    return if (index > 9) "$index" else "0$index"
}

private var toast: Toast? = null

private fun showToast(text: String) {
    toast?.cancel()
    toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
    toast?.show()
}