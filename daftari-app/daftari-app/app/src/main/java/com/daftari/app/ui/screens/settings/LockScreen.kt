package com.daftari.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import androidx.compose.ui.res.stringResource

@Composable
fun LockScreen(correctPin: String, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it.filter { c -> c.isDigit() }.take(6)
                error = false
                if (pin == correctPin) onUnlocked()
            },
            label = { Text("PIN") },
            isError = error,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (pin == correctPin) onUnlocked() else error = true
        }) { Text(stringResource(R.string.confirm)) }
    }
}
