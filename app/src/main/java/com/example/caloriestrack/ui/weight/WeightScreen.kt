package com.example.caloriestrack.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.caloriestrack.data.CaloriesRepository
import com.example.caloriestrack.data.WeightLogEntity
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun WeightScreen(
    repository: CaloriesRepository,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    var date by remember { mutableStateOf(today) }
    var weight by remember { mutableStateOf("") }
    var weightLogs by remember { mutableStateOf(emptyList<WeightLogEntity>()) }
    var isViewingAnalysis by remember { mutableStateOf(false) }
    var selectedAnalysisRange by remember { mutableStateOf(WeightAnalysisRange.Week) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(repository) {
        repository.observeWeightLogs().collect { logs ->
            weightLogs = logs
        }
    }

    val latestWeight = remember(weightLogs) {
        weightLogs.latestInput()
    }
    val weekAnalysis = remember(weightLogs) {
        WeightAnalysisStats.from(
            logs = weightLogs,
            range = WeightAnalysisRange.Week,
            today = LocalDate.now()
        )
    }

    if (isViewingAnalysis) {
        WeightAnalysisScreen(
            weightLogs = weightLogs,
            selectedRange = selectedAnalysisRange,
            onRangeSelected = { selectedAnalysisRange = it },
            onBack = { isViewingAnalysis = false },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            WeightAnalysisSummaryCard(
                stats = weekAnalysis,
                onOpenAnalysis = { isViewingAnalysis = true }
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current weight",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = latestWeight?.let { "${it.weightKg.toCleanText()} kg" } ?: "No weight saved yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (latestWeight != null) {
                        Text(
                            text = latestWeight.date,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        weight = it
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight for this day (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = {
                        val parsedDate = runCatching { LocalDate.parse(date.trim()) }.getOrNull()
                        val parsedWeight = weight.toDoubleOrNull()

                        if (parsedDate == null) {
                            errorMessage = "Enter a valid date in yyyy-mm-dd format."
                            successMessage = null
                            return@Button
                        }
                        if (parsedWeight == null || parsedWeight <= 0) {
                            errorMessage = "Enter a valid weight greater than 0."
                            successMessage = null
                            return@Button
                        }

                        val weightLog = WeightLogEntity(
                            0,
                            parsedDate.toString(),
                            parsedWeight,
                            System.currentTimeMillis()
                        )

                        coroutineScope.launch {
                            repository.saveWeightLog(weightLog)
                            weight = ""
                            successMessage = "Weight saved."
                            errorMessage = null
                        }
                    }
                ) {
                    Text("Add weight")
                }
            }
        }
        item {
            Text(
                text = "Saved weights",
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (weightLogs.isEmpty()) {
            item {
                Text(
                    text = "No weights saved yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(weightLogs, key = { it.id }) { weightLog ->
                WeightLogItem(
                    weightLog = weightLog,
                    onDelete = {
                        coroutineScope.launch {
                            repository.deleteWeightLog(weightLog)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WeightAnalysisSummaryCard(
    stats: WeightAnalysisStats,
    onOpenAnalysis: () -> Unit
) {
    val changeColor = stats.changeColor()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weight analysis",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onOpenAnalysis) {
                    Text(">")
                }
            }
            if (stats.logs.isEmpty()) {
                Text(
                    text = "Add weight entries to see weekly progress.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Week change",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${stats.weightChange.toSignedCleanText()} kg (${stats.percentChange.toSignedCleanText()}%)",
                    color = changeColor,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun WeightAnalysisScreen(
    weightLogs: List<WeightLogEntity>,
    selectedRange: WeightAnalysisRange,
    onRangeSelected: (WeightAnalysisRange) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = remember(weightLogs, selectedRange) {
        WeightAnalysisStats.from(
            logs = weightLogs,
            range = selectedRange,
            today = LocalDate.now()
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }
        item {
            Text(
                text = "Weight analysis",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            WeightAnalysisRangeSelector(
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (stats.logs.isEmpty()) {
                        Text(
                            text = "No weight entries in this range.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        WeightPercentChart(stats = stats)
                        MetricRow("Start weight", "${stats.startWeight.toCleanText()} kg")
                        MetricRow("Current weight", "${stats.currentWeight.toCleanText()} kg")
                        MetricRow(
                            "Weight change",
                            "${stats.weightChange.toSignedCleanText()} kg"
                        )
                        MetricRow(
                            "Percent change",
                            "${stats.percentChange.toSignedCleanText()}%"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightAnalysisRangeSelector(
    selectedRange: WeightAnalysisRange,
    onRangeSelected: (WeightAnalysisRange) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(WeightAnalysisRange.entries) { _, range ->
            if (range == selectedRange) {
                Button(onClick = { onRangeSelected(range) }) {
                    Text(range.label)
                }
            } else {
                OutlinedButton(onClick = { onRangeSelected(range) }) {
                    Text(range.label)
                }
            }
        }
    }
}

@Composable
private fun WeightPercentChart(stats: WeightAnalysisStats) {
    val lineColor = stats.changeColor()
    val pointColor = lineColor
    val axisColor = MaterialTheme.colorScheme.outline
    val points = stats.percentPoints
    val minPercent = points.minOf { it.percentChange }
    val maxPercent = points.maxOf { it.percentChange }
    val percentRange = (maxPercent - minPercent).takeIf { it > 0 } ?: 1.0

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val horizontalPadding = 12.dp.toPx()
        val verticalPadding = 18.dp.toPx()
        val chartWidth = size.width - horizontalPadding * 2
        val chartHeight = size.height - verticalPadding * 2
        val zeroY = if (minPercent <= 0 && maxPercent >= 0) {
            val normalizedZero = ((0.0 - minPercent) / percentRange).toFloat()
            verticalPadding + chartHeight * (1f - normalizedZero)
        } else {
            size.height - verticalPadding
        }

        drawLine(
            color = axisColor,
            start = Offset(horizontalPadding, zeroY),
            end = Offset(size.width - horizontalPadding, zeroY),
            strokeWidth = 1.dp.toPx()
        )

        val chartPoints = points.mapIndexed { index, point ->
            val x = if (points.size == 1) {
                size.width / 2f
            } else {
                horizontalPadding + chartWidth * (index.toFloat() / points.lastIndex.toFloat())
            }
            val normalizedPercent = ((point.percentChange - minPercent) / percentRange).toFloat()
            val y = verticalPadding + chartHeight * (1f - normalizedPercent)
            Offset(x, y)
        }

        if (chartPoints.size > 1) {
            val path = Path().apply {
                moveTo(chartPoints.first().x, chartPoints.first().y)
                chartPoints.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        chartPoints.forEach { point ->
            drawCircle(
                color = pointColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun WeightAnalysisStats.changeColor(): Color {
    return if (weightChange <= 0) {
        WeightLossGreen
    } else {
        WeightGainRed
    }
}

@Composable
private fun WeightLogItem(
    weightLog: WeightLogEntity,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = weightLog.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${weightLog.weightKg.toCleanText()} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

private data class WeightAnalysisStats(
    val logs: List<WeightLogEntity>,
    val startWeight: Double,
    val currentWeight: Double,
    val weightChange: Double,
    val percentChange: Double,
    val percentPoints: List<WeightPercentPoint>
) {
    companion object {
        fun from(
            logs: List<WeightLogEntity>,
            range: WeightAnalysisRange,
            today: LocalDate
        ): WeightAnalysisStats {
            val startDate = range.startDate(today)
            val filteredLogs = logs.filter { log ->
                val logDate = runCatching { LocalDate.parse(log.date) }.getOrNull()
                logDate != null && (startDate == null || !logDate.isBefore(startDate)) && !logDate.isAfter(today)
            }.sortedByTimeline()

            val startWeight = filteredLogs.firstOrNull()?.weightKg ?: 0.0
            val currentWeight = filteredLogs.latestInput()?.weightKg ?: 0.0
            val weightChange = currentWeight - startWeight
            val percentChange = if (startWeight > 0) {
                (weightChange / startWeight) * 100.0
            } else {
                0.0
            }
            val percentPoints = filteredLogs.map { log ->
                WeightPercentPoint(
                    date = log.date,
                    percentChange = if (startWeight > 0) {
                        ((log.weightKg - startWeight) / startWeight) * 100.0
                    } else {
                        0.0
                    }
                )
            }

            return WeightAnalysisStats(
                logs = filteredLogs,
                startWeight = startWeight,
                currentWeight = currentWeight,
                weightChange = weightChange,
                percentChange = percentChange,
                percentPoints = percentPoints
            )
        }
    }
}

private data class WeightPercentPoint(
    val date: String,
    val percentChange: Double
)

private enum class WeightAnalysisRange(val label: String) {
    Week("Week"),
    Month("Month"),
    ThreeMonths("3 months"),
    SixMonths("6 months"),
    Year("Year"),
    AllTime("All time");

    fun startDate(today: LocalDate): LocalDate? {
        return when (this) {
            Week -> today.minusWeeks(1)
            Month -> today.minusMonths(1)
            ThreeMonths -> today.minusMonths(3)
            SixMonths -> today.minusMonths(6)
            Year -> today.minusYears(1)
            AllTime -> null
        }
    }
}

private fun Double.toCleanText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

private fun Double.toSignedCleanText(): String {
    val prefix = if (this > 0) "+" else ""
    return prefix + toCleanText()
}

private fun List<WeightLogEntity>.latestInput(): WeightLogEntity? {
    return maxWithOrNull(
        compareBy<WeightLogEntity> { it.createdAtMillis }
            .thenBy { it.id }
    )
}

private fun List<WeightLogEntity>.sortedByTimeline(): List<WeightLogEntity> {
    return sortedWith(
        compareBy<WeightLogEntity> { it.date }
            .thenBy { it.createdAtMillis }
            .thenBy { it.id }
    )
}

private val WeightLossGreen = Color(0xFF2E7D32)
private val WeightGainRed = Color(0xFFC62828)
