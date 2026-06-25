package com.example.exchangenotifier.ui.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exchangenotifier.R
import com.example.exchangenotifier.domain.model.Currency
import com.example.exchangenotifier.domain.model.CurrencyPair
import com.example.exchangenotifier.ui.theme.NegativeDark
import com.example.exchangenotifier.ui.theme.NegativeLight
import com.example.exchangenotifier.ui.theme.PositiveDark
import com.example.exchangenotifier.ui.theme.PositiveLight
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateToSettings: () -> Unit) {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedPair by viewModel.selectedPair.collectAsState()

    var showPairPicker by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { showPairPicker = true },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedPair.label,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.select_pair),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is MainUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is MainUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.no_connection), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.lastRate?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.last_value, "%.4f".format(it)),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            is MainUiState.Success -> {
                SuccessContent(
                    state = state,
                    isRefreshing = isRefreshing,
                    onPeriodSelected = viewModel::onPeriodSelected,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                )
            }
        }
    }

    if (showPairPicker) {
        PairPickerDialog(
            currentPair = selectedPair,
            onDismiss = { showPairPicker = false },
            onPairSelected = { pair ->
                viewModel.selectPair(pair)
                showPairPicker = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PairPickerDialog(
    currentPair: CurrencyPair,
    onDismiss: () -> Unit,
    onPairSelected: (CurrencyPair) -> Unit,
) {
    var selectedBase  by remember { mutableStateOf(currentPair.base) }
    var selectedQuote by remember { mutableStateOf(currentPair.quote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_pair)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.base_currency),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Currency.entries.forEach { currency ->
                        FilterChip(
                            selected = currency == selectedBase,
                            onClick = { selectedBase = currency },
                            label = { Text("${currency.flag} ${currency.code}") },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.quote_currency),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Currency.entries.forEach { currency ->
                        FilterChip(
                            selected = currency == selectedQuote,
                            onClick = { selectedQuote = currency },
                            label = { Text("${currency.flag} ${currency.code}") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPairSelected(CurrencyPair(selectedBase, selectedQuote)) },
                enabled = selectedBase != selectedQuote,
            ) {
                Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    state: MainUiState.Success,
    isRefreshing: Boolean,
    onPeriodSelected: (ChartPeriod) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val positiveColor = if (isDark) PositiveDark else PositiveLight
    val negativeColor = if (isDark) NegativeDark else NegativeLight

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "%.4f".format(state.rate),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
        )

        state.change?.let { change ->
            val isPositive = change >= 0
            val changeColor = if (isPositive) positiveColor else negativeColor
            val arrow = if (isPositive) "▲" else "▼"
            val pctText = state.changePercent?.let { " (${"%.2f".format(it)}%)" } ?: ""
            Text(
                text = "$arrow ${"%.4f".format(change)}$pctText",
                style = MaterialTheme.typography.bodyLarge,
                color = changeColor,
            )
        }

        val updatedStr = remember(state.lastUpdated) {
            DateTimeFormatter.ofPattern("HH:mm:ss")
                .format(state.lastUpdated.atZone(ZoneId.systemDefault()))
        }
        Text(
            text = stringResource(R.string.updated_at, updatedStr),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ChartPeriod.entries.forEach { period ->
                FilterChip(
                    selected = period == state.selectedPeriod,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(stringResource(period.labelRes)) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
        ) {
            RateChart(
                points = state.chartPoints,
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
    }
    }
}

@Composable
private fun RateChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.size(28.dp))
        }
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val labelColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val density = LocalDensity.current
    val textSizePx = with(density) { 10.sp.toPx() }

    val minVal = remember(points) { points.minOf { it.value } }
    val maxVal = remember(points) { points.maxOf { it.value } }

    Canvas(modifier) {
        val labelW   = 52.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val topPad    = 12.dp.toPx()

        val chartLeft   = labelW
        val chartTop    = topPad
        val chartBottom = size.height - bottomPad
        val chartW      = size.width - labelW
        val chartH      = chartBottom - chartTop
        val range       = (maxVal - minVal).coerceAtLeast(0.001f)

        fun xAt(i: Int) = chartLeft + if (points.size <= 1) chartW / 2f else chartW * i / (points.size - 1)
        fun yAt(v: Float) = chartTop + chartH * (1f - (v - minVal) / range)

        val linePath = Path()
        points.forEachIndexed { i, pt ->
            val x = xAt(i); val y = yAt(pt.value)
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(xAt(points.lastIndex), chartBottom)
            lineTo(xAt(0), chartBottom)
            close()
        }
        drawPath(
            fillPath,
            Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.28f), Color.Transparent),
                startY = chartTop, endY = chartBottom,
            )
        )

        drawPath(linePath, lineColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lx = xAt(points.lastIndex); val ly = yAt(points.last().value)
        drawCircle(lineColor.copy(alpha = 0.2f), 7.dp.toPx(), center = Offset(lx, ly))
        drawCircle(lineColor, 3.5.dp.toPx(), center = Offset(lx, ly))

        val yPaint = android.graphics.Paint().apply {
            color = labelColorArgb; textSize = textSizePx; isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        drawIntoCanvas { c ->
            c.nativeCanvas.drawText("%.2f".format(maxVal), labelW - 4.dp.toPx(), chartTop + textSizePx, yPaint)
            c.nativeCanvas.drawText("%.2f".format(minVal), labelW - 4.dp.toPx(), chartBottom, yPaint)
        }

        if (points.size >= 2) {
            val xPaint = android.graphics.Paint().apply {
                color = labelColorArgb; textSize = textSizePx; isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val indices = if (points.size <= 3) points.indices.toList()
            else listOf(0, points.size / 2, points.lastIndex)
            drawIntoCanvas { c ->
                indices.forEach { i ->
                    c.nativeCanvas.drawText(points[i].label, xAt(i), size.height - 2.dp.toPx(), xPaint)
                }
            }
        }
    }
}
