package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfile
import com.example.data.GameSessionRecord
import java.util.Calendar

enum class RechartsChartStyle {
    AREA_SPLINE,
    GROUPED_BAR
}

enum class RechartsSeriesFilter {
    ALL,
    MATH_ONLY,
    LOGIC_ONLY
}

data class WeeklyActivityDayPoint(
    val dayIndex: Int,
    val dayShortLabel: String,
    val dayFullLabel: String,
    val mathProblemsSolved: Int,
    val logicProblemsSolved: Int,
    val sessionsCount: Int
) {
    val totalProblemsSolved: Int get() = mathProblemsSolved + logicProblemsSolved
}

object WeeklyActivityAggregator {

    private val mathGameIds = setOf("math_safari", "number_bridge", "shape_galaxy", "cosmic_balance")

    fun buildWeeklySeries(
        sessions: List<GameSessionRecord>,
        profile: ChildProfile,
        languageCode: String,
        nowMillis: Long = System.currentTimeMillis()
    ): List<WeeklyActivityDayPoint> {
        val dayLabelsShort = localizedShortDays(languageCode)
        val dayLabelsFull = localizedFullDays(languageCode)
        val oneDayMs = 24L * 60L * 60L * 1000L

        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfToday = cal.timeInMillis

        val mathBySlot = IntArray(7)
        val logicBySlot = IntArray(7)
        val countBySlot = IntArray(7)

        sessions.forEachIndexed { idx, session ->
            val sessionTime = session.createdAt?.toDate()?.time
            val slot = if (sessionTime != null && sessionTime <= endOfToday) {
                val daysAgo = ((endOfToday - sessionTime) / oneDayMs).toInt()
                if (daysAgo in 0..6) 6 - daysAgo else 6 - (idx % 7)
            } else {
                6 - (idx % 7)
            }
            val solved = session.correctAnswers.toInt().coerceAtLeast(1)
            if (session.gameType in mathGameIds) {
                mathBySlot[slot] += solved
            } else {
                logicBySlot[slot] += solved
            }
            countBySlot[slot] += 1
        }

        // If profile has accumulated mathScore/logicScore beyond recorded session list, attribute remainder to today
        val recordedMath = mathBySlot.sum()
        val recordedLogic = logicBySlot.sum()
        val profileMathProblems = (profile.mathScore / 15L).toInt()
        val profileLogicProblems = (profile.logicScore / 15L).toInt()
        if (profileMathProblems > recordedMath) {
            mathBySlot[6] += (profileMathProblems - recordedMath)
        }
        if (profileLogicProblems > recordedLogic) {
            logicBySlot[6] += (profileLogicProblems - recordedLogic)
        }

        return (0..6).map { slot ->
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = endOfToday - (6 - slot) * oneDayMs
            }
            val dowIndex = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0 .. Sunday = 6
            WeeklyActivityDayPoint(
                dayIndex = slot,
                dayShortLabel = dayLabelsShort[dowIndex],
                dayFullLabel = dayLabelsFull[dowIndex],
                mathProblemsSolved = mathBySlot[slot],
                logicProblemsSolved = logicBySlot[slot],
                sessionsCount = countBySlot[slot]
            )
        }
    }

    private fun localizedShortDays(lang: String): List<String> = when (lang) {
        "en" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        "es" -> listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        "fr" -> listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
        "hi" -> listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि")
        else -> listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    }

    private fun localizedFullDays(lang: String): List<String> = when (lang) {
        "en" -> listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        "es" -> listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        "fr" -> listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
        "hi" -> listOf("सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार", "रविवार")
        else -> listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    }
}

@Composable
fun RechartsWeeklyActivityCard(
    sessions: List<GameSessionRecord>,
    profile: ChildProfile,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    val weeklyData = remember(sessions, profile.mathScore, profile.logicScore, languageCode) {
        WeeklyActivityAggregator.buildWeeklySeries(
            sessions = sessions,
            profile = profile,
            languageCode = languageCode
        )
    }

    var chartStyle by remember { mutableStateOf(RechartsChartStyle.AREA_SPLINE) }
    var seriesFilter by remember { mutableStateOf(RechartsSeriesFilter.ALL) }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to Today (last slot)

    val mathColor = Color(0xFFFF7043)
    val logicColor = Color(0xFF7E57C2)

    val totalWeeklyMath = weeklyData.sumOf { it.mathProblemsSolved }
    val totalWeeklyLogic = weeklyData.sumOf { it.logicProblemsSolved }
    val maxDayValue = remember(weeklyData) {
        weeklyData.maxOfOrNull { maxOf(it.mathProblemsSolved, it.logicProblemsSolved, 4) } ?: 6
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "recharts_entry_anim"
    )

    val chartTitle = when (languageCode) {
        "en" -> "Weekly Math & Logic Activity"
        "es" -> "Actividad Semanal de Matemáticas y Lógica"
        "fr" -> "Activité Hebdomadaire Maths & Logique"
        "hi" -> "साप्ताहिक गणित और तर्क गतिविधि"
        else -> "Grafik Aktivitas Mingguan Matematika & Logika"
    }
    val chartSubtitle = when (languageCode) {
        "en" -> "Problems solved over the last 7 days (tap any day to inspect)"
        "es" -> "Problemas resueltos en los últimos 7 días"
        "fr" -> "Problèmes résolus au cours des 7 derniers jours"
        "hi" -> "पिछले 7 दिनों में हल किए गए प्रश्न"
        else -> "Jumlah soal Matematika & Logika yang diselesaikan 7 hari terakhir"
    }
    val mathLegendLabel = when (languageCode) {
        "en" -> "Math Solved"
        "es" -> "Matemáticas"
        "fr" -> "Maths"
        "hi" -> "गणित"
        else -> "Soal Matematika"
    }
    val logicLegendLabel = when (languageCode) {
        "en" -> "Logic Solved"
        "es" -> "Lógica"
        "fr" -> "Logique"
        "hi" -> "तर्क"
        else -> "Soal Logika"
    }
    val allLegendLabel = when (languageCode) {
        "en" -> "Both"
        "es" -> "Ambos"
        "fr" -> "Tous"
        "hi" -> "सभी"
        else -> "Semua"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_activity_chart_card"),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row + Chart Style Switcher (Area Spline vs Grouped Bar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = chartTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = chartSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Recharts Chart Type Toggle (AreaChart vs BarChart)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (chartStyle == RechartsChartStyle.AREA_SPLINE)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { chartStyle = RechartsChartStyle.AREA_SPLINE }
                            .testTag("chart_style_toggle_area")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Area Chart",
                                tint = if (chartStyle == RechartsChartStyle.AREA_SPLINE)
                                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (chartStyle == RechartsChartStyle.GROUPED_BAR)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { chartStyle = RechartsChartStyle.GROUPED_BAR }
                            .testTag("chart_style_toggle_bar")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = if (chartStyle == RechartsChartStyle.GROUPED_BAR)
                                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Summary KPI Totals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = mathColor.copy(alpha = 0.14f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = mathColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "$totalWeeklyMath",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = mathColor,
                                modifier = Modifier.testTag("weekly_math_total_value")
                            )
                            Text(
                                text = mathLegendLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = logicColor.copy(alpha = 0.14f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = logicColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "$totalWeeklyLogic",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = logicColor,
                                modifier = Modifier.testTag("weekly_logic_total_value")
                            )
                            Text(
                                text = logicLegendLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Recharts Interactive Series Legend Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = seriesFilter == RechartsSeriesFilter.ALL,
                    onClick = { seriesFilter = RechartsSeriesFilter.ALL },
                    label = { Text(allLegendLabel, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chart_series_filter_all")
                )
                FilterChip(
                    selected = seriesFilter == RechartsSeriesFilter.MATH_ONLY,
                    onClick = { seriesFilter = RechartsSeriesFilter.MATH_ONLY },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(mathColor)
                        )
                    },
                    label = { Text(mathLegendLabel, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chart_series_filter_math")
                )
                FilterChip(
                    selected = seriesFilter == RechartsSeriesFilter.LOGIC_ONLY,
                    onClick = { seriesFilter = RechartsSeriesFilter.LOGIC_ONLY },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(logicColor)
                        )
                    },
                    label = { Text(logicLegendLabel, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chart_series_filter_logic")
                )
            }

            // Recharts Responsive Container Canvas (CartesianGrid + Area/Bar + Active Tooltip Line)
            RechartsResponsiveCanvas(
                weeklyData = weeklyData,
                maxVal = maxDayValue,
                chartStyle = chartStyle,
                seriesFilter = seriesFilter,
                selectedDayIndex = selectedDayIndex,
                onSelectDay = { selectedDayIndex = it },
                mathColor = mathColor,
                logicColor = logicColor,
                animProgress = animProgress
            )

            // Interactive X-Axis Day Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEachIndexed { idx, point ->
                    val isSelected = idx == selectedDayIndex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedDayIndex = idx }
                            .testTag("chart_day_selector_$idx")
                    ) {
                        Text(
                            text = point.dayShortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            // Recharts Interactive Tooltip Popover for Selected Day
            val activePoint = weeklyData.getOrElse(selectedDayIndex) { weeklyData.last() }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharts_tooltip_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activePoint.dayFullLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Total: ${activePoint.totalProblemsSolved}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(mathColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activePoint.mathProblemsSolved}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = mathColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(logicColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${activePoint.logicProblemsSolved}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = logicColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RechartsResponsiveCanvas(
    weeklyData: List<WeeklyActivityDayPoint>,
    maxVal: Int,
    chartStyle: RechartsChartStyle,
    seriesFilter: RechartsSeriesFilter,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    mathColor: Color,
    logicColor: Color,
    animProgress: Float
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val cursorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    val showMath = seriesFilter != RechartsSeriesFilter.LOGIC_ONLY
    val showLogic = seriesFilter != RechartsSeriesFilter.MATH_ONLY

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .pointerInput(weeklyData.size) {
                detectTapGestures { offset ->
                    val slotWidth = size.width / 7f
                    val tappedIdx = (offset.x / slotWidth).toInt().coerceIn(0, 6)
                    onSelectDay(tappedIdx)
                }
            }
            .testTag("recharts_responsive_canvas")
    ) {
        val w = size.width
        val h = size.height
        val topPad = 14f
        val botPad = 14f
        val chartH = (h - topPad - botPad).coerceAtLeast(10f)
        val safeMax = maxVal.coerceAtLeast(4).toFloat()

        // 1. Recharts CartesianGrid (4 horizontal dashed reference lines)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        for (i in 0..3) {
            val y = topPad + (chartH * i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 2f,
                pathEffect = if (i < 3) dashEffect else null
            )
        }

        val slotW = w / 7f
        val selectedX = slotW * selectedDayIndex + slotW / 2f

        // 2. Active Tooltip Cursor Line
        drawLine(
            color = cursorColor,
            start = Offset(selectedX, topPad),
            end = Offset(selectedX, h - botPad),
            strokeWidth = 3f,
            pathEffect = dashEffect
        )

        if (chartStyle == RechartsChartStyle.GROUPED_BAR) {
            // Recharts <BarChart> with rounded bars
            val barW = (slotW * 0.28f).coerceAtMost(28f)
            weeklyData.forEachIndexed { idx, pt ->
                val centerX = slotW * idx + slotW / 2f
                if (showMath) {
                    val ratio = (pt.mathProblemsSolved / safeMax).coerceIn(0f, 1f) * animProgress
                    val barH = (chartH * ratio).coerceAtLeast(6f)
                    val x = if (showLogic) centerX - barW - 3f else centerX - barW / 2f
                    drawRoundRect(
                        color = mathColor,
                        topLeft = Offset(x, topPad + chartH - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                if (showLogic) {
                    val ratio = (pt.logicProblemsSolved / safeMax).coerceIn(0f, 1f) * animProgress
                    val barH = (chartH * ratio).coerceAtLeast(6f)
                    val x = if (showMath) centerX + 3f else centerX - barW / 2f
                    drawRoundRect(
                        color = logicColor,
                        topLeft = Offset(x, topPad + chartH - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        } else {
            // Recharts <AreaChart> with smooth monotone cubic Bezier splines & gradient fills
            fun drawSmoothSeries(values: List<Int>, strokeColor: Color) {
                val pts = values.mapIndexed { idx, v ->
                    val x = slotW * idx + slotW / 2f
                    val ratio = (v / safeMax).coerceIn(0f, 1f) * animProgress
                    val y = topPad + chartH * (1f - ratio)
                    Offset(x, y)
                }
                if (pts.isEmpty()) return

                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 0 until pts.lastIndex) {
                        val p0 = pts[i]
                        val p1 = pts[i + 1]
                        val ctrlX = (p0.x + p1.x) / 2f
                        cubicTo(ctrlX, p0.y, ctrlX, p1.y, p1.x, p1.y)
                    }
                }
                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(pts.last().x, topPad + chartH)
                    lineTo(pts.first().x, topPad + chartH)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(strokeColor.copy(alpha = 0.36f), strokeColor.copy(alpha = 0.02f)),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )
                drawPath(
                    path = linePath,
                    color = strokeColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
                pts.forEachIndexed { idx, pt ->
                    val isSelected = idx == selectedDayIndex
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 11f else 7f,
                        center = pt
                    )
                    drawCircle(
                        color = strokeColor,
                        radius = if (isSelected) 7.5f else 4.5f,
                        center = pt
                    )
                }
            }

            if (showMath) {
                drawSmoothSeries(weeklyData.map { it.mathProblemsSolved }, mathColor)
            }
            if (showLogic) {
                drawSmoothSeries(weeklyData.map { it.logicProblemsSolved }, logicColor)
            }
        }
    }
}
