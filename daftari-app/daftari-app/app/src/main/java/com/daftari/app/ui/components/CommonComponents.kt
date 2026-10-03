package com.daftari.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daftari.app.ui.theme.SubjectColorPalette
import java.util.Calendar

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cancelLabel) } }
    )
}

@Composable
fun ColorPickerRow(selectedHex: String, onColorSelected: (String) -> Unit) {
    var customColor by remember { mutableStateOf(selectedHex) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SubjectColorPalette.forEach { hex ->
                val color = runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(Color.Gray)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedHex.equals(hex, true)) 3.dp else 0.dp,
                            color = Color.Black,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = customColor,
            onValueChange = { value ->
                customColor = value
                if (runCatching { AndroidColor.parseColor(value) }.isSuccess) {
                    onColorSelected(value)
                }
            },
            label = { Text("#RRGGBB") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun rememberDatePickerLauncher(initialMillis: Long, onDatePicked: (Long) -> Unit): () -> Unit {
    val context = LocalContext.current
    return {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                onDatePicked(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

@Composable
fun rememberTimePickerLauncher(onTimePicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    return {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimePicked(String.format("%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
