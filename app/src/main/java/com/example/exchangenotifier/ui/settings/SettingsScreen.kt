package com.example.exchangenotifier.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exchangenotifier.R
import com.example.exchangenotifier.data.provider.CompositeRateProvider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val prefs by viewModel.prefs.collectAsState()

    var upperText by rememberSaveable { mutableStateOf("") }
    var lowerText by rememberSaveable { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(prefs.upperThreshold) {
        if (upperText.isEmpty() && prefs.upperThreshold != null)
            upperText = prefs.upperThreshold.toString()
    }
    LaunchedEffect(prefs.lowerThreshold) {
        if (lowerText.isEmpty() && prefs.lowerThreshold != null)
            lowerText = prefs.lowerThreshold.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Alerts ─────────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.section_alerts))

            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = upperText,
                        onValueChange = { upperText = it; viewModel.setUpperThreshold(it) },
                        label = { Text(stringResource(R.string.upper_threshold)) },
                        placeholder = { Text(stringResource(R.string.upper_threshold_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = prefs.upperAlertEnabled,
                        onCheckedChange = { viewModel.setUpperAlertEnabled(it) },
                        enabled = upperText.toDoubleOrNull() != null,
                    )
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = lowerText,
                        onValueChange = { lowerText = it; viewModel.setLowerThreshold(it) },
                        label = { Text(stringResource(R.string.lower_threshold)) },
                        placeholder = { Text(stringResource(R.string.lower_threshold_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = prefs.lowerAlertEnabled,
                        onCheckedChange = { viewModel.setLowerAlertEnabled(it) },
                        enabled = lowerText.toDoubleOrNull() != null,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Data source ────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.section_data_source))

            // Chips wrap onto a new line when they don't all fit in one row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilterChip(
                    selected = prefs.preferredProvider == CompositeRateProvider.PROVIDER_AUTO,
                    onClick = { viewModel.setPreferredProvider(CompositeRateProvider.PROVIDER_AUTO) },
                    label = { Text(stringResource(R.string.provider_auto)) },
                )
                viewModel.providers.forEach { provider ->
                    val noSeries = !provider.supportsTimeSeries
                    FilterChip(
                        selected = prefs.preferredProvider == provider.id,
                        onClick = { viewModel.setPreferredProvider(provider.id) },
                        label = {
                            Column {
                                Text(provider.displayName)
                                if (noSeries) Text(
                                    stringResource(R.string.provider_no_series),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Monitoring ─────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.section_monitoring))
            Text(stringResource(R.string.check_interval), style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15 to "15m", 30 to "30m", 60 to "1h", 120 to "2h").forEach { (minutes, label) ->
                    FilterChip(
                        selected = prefs.pollIntervalMinutes == minutes,
                        onClick = { viewModel.setInterval(minutes) },
                        label = { Text(label) },
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(onClick = { viewModel.runCheck() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.check_now))
            }

            OutlinedButton(onClick = { viewModel.testNotification() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.test_notification))
            }

            Spacer(Modifier.height(8.dp))

            // ── History ────────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.section_history))
            Text(
                stringResource(R.string.retain_days, prefs.historyRetentionDays),
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = prefs.historyRetentionDays.toFloat(),
                onValueChange = { viewModel.setHistoryRetentionDays(it.toInt()) },
                valueRange = 1f..30f,
                steps = 28,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.clear_history))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.clear_history_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearDialog = false }) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}
