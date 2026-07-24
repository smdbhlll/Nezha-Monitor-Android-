package com.atigger.status.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atigger.status.data.ServerUiModel
import com.atigger.status.i18n.AppStrings
import kotlin.math.absoluteValue

private val CpuGreen = Color(0xFF1B8A5A)
private val CpuYellow = Color(0xFFE6A817)
private val CpuOrange = Color(0xFFE67E22)
private val CpuRed = Color(0xFFB3261E)
private val MemoryBlue = Color(0xFF3B82F6)
private val DiskPurple = Color(0xFF8B5CF6)
private val NetworkDownBlue = Color(0xFF3B82F6)
private val NetworkUpGreen = Color(0xFF10B981)
private val LightTrack = Color(0xFFE5E7EB)
private val DarkTrack = Color(0xFF374151)

@Composable
fun usageColor(percent: Float): Color {
    return when {
        percent < 50f -> CpuGreen
        percent < 75f -> CpuYellow
        percent < 90f -> CpuOrange
        else -> CpuRed
    }
}

@Composable
fun chartTrackColor(): Color {
    return if (isSystemInDarkTheme()) DarkTrack else LightTrack
}

private fun formatBytes(value: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var display = value.toDouble().absoluteValue
    var unitIndex = 0
    while (display >= 1024 && unitIndex < units.lastIndex) {
        display /= 1024.0
        unitIndex += 1
    }
    val sign = if (value < 0) "-" else ""
    val pattern = if (display >= 100 || unitIndex == 0) "%.0f" else "%.1f"
    return sign + pattern.format(display) + units[unitIndex]
}

/** Ultra-compact CPU ring gauge (42dp, only percent inside, label outside) */
@Composable
fun CpuRing(
    percent: Float,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    strokeWidth: Dp = 4.dp
) {
    val color = usageColor(percent)
    val bgTrack = chartTrackColor()
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = bgTrack, startAngle = 135f, sweepAngle = 270f,
                useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val sweep = (percent.coerceIn(0f, 100f) / 100f) * 270f
            drawArc(
                color = color, startAngle = 135f, sweepAngle = sweep,
                useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${percent.toInt()}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Compact progress bar: [label%] [====bar====] */
@Composable
fun CompactBar(
    percent: Float,
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    barColor: Color? = null
) {
    val color = barColor ?: usageColor(percent)
    val bgTrack = chartTrackColor()
    val clamped = percent.coerceIn(0f, 100f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ${clamped.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.width(58.dp)
        )
        Box(modifier = Modifier.weight(1f).padding(start = 2.dp, end = 2.dp)) {
            Canvas(
                modifier = Modifier.fillMaxWidth().height(height)
            ) {
                val r = height.toPx() / 2f
                drawRoundRect(color = bgTrack, cornerRadius = CornerRadius(r, r))
                if (clamped > 0f) {
                    drawRoundRect(
                        color = color,
                        size = Size(size.width * (clamped / 100f), size.height),
                        cornerRadius = CornerRadius(r, r)
                    )
                }
            }
        }
    }
}

/** Single-line network speed + load, compact */
@Composable
fun NetworkLoadLine(
    downSpeed: String?,
    upSpeed: String?,
    load1: Double?,
    load5: Double?,
    load15: Double?,
    modifier: Modifier = Modifier
) {
    val parts = buildList {
        downSpeed?.let { add("\u2193$it") }
        upSpeed?.let { add("\u2191$it") }
        val loads = listOfNotNull(load1, load5, load15)
        if (loads.isNotEmpty()) {
            add(loads.joinToString("/") { String.format("%.1f", it) })
        }
    }
    if (parts.isEmpty()) return

    Text(
        text = parts.joinToString("   "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth()
    )
}

/** Single-line total traffic display */
@Composable
fun TrafficTotalLine(
    totalIn: Long?,
    totalOut: Long?,
    modifier: Modifier = Modifier
) {
    val parts = buildList {
        totalIn?.let { add("\u21e3${formatBytes(it)}") }
        totalOut?.let { add("\u21e1${formatBytes(it)}") }
    }
    if (parts.isEmpty()) return

    Text(
        text = parts.joinToString("   "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth()
    )
}

/** Combined metrics: three compact rows */
@Composable
fun ServerMetricsDashboard(
    server: ServerUiModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Row 1: CPU ring + RAM bar + DSK bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (server.cpuPercent != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp)
                ) {
                    CpuRing(percent = server.cpuPercent, size = 42.dp, strokeWidth = 4.dp)
                    Text(
                        text = strings.cpu,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // RAM + DSK bars stacked
            Column(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (server.memoryPercent != null) {
                    CompactBar(
                        percent = server.memoryPercent,
                        label = "RAM",
                        height = 6.dp,
                        barColor = MemoryBlue
                    )
                }
                if (server.diskPercent != null) {
                    CompactBar(
                        percent = server.diskPercent,
                        label = "DSK",
                        height = 6.dp,
                        barColor = DiskPurple
                    )
                }
                if (server.memoryPercent == null && server.diskPercent == null && server.swapPercent != null) {
                    CompactBar(
                        percent = server.swapPercent,
                        label = "SWP",
                        height = 6.dp
                    )
                }
            }
        }

        // Row 2: Network speeds + load
        NetworkLoadLine(
            downSpeed = server.netInSpeedText,
            upSpeed = server.netOutSpeedText,
            load1 = server.load1,
            load5 = server.load5,
            load15 = server.load15
        )

        // Row 3: Total traffic (in/out cumulative)
        TrafficTotalLine(
            totalIn = server.netInTransfer,
            totalOut = server.netOutTransfer
        )
    }
}
