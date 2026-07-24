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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atigger.status.data.ServerUiModel
import com.atigger.status.i18n.AppStrings

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

/**
 * Circular ring gauge showing a percentage value.
 * Used for CPU usage visualization.
 */
@Composable
fun UsageRing(
    percent: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp
) {
    val color = usageColor(percent)
    val bgTrack = chartTrackColor()
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            // Background track
            drawArc(
                color = bgTrack,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Usage arc
            val sweep = (percent.coerceIn(0f, 100f) / 100f) * 270f
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percent.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Compact horizontal progress bar showing usage percentage.
 * Used for memory, disk, and swap visualization.
 */
@Composable
fun UsageBar(
    percent: Float,
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    barColor: Color? = null
) {
    val color = barColor ?: usageColor(percent)
    val bgTrack = chartTrackColor()
    val clampedPercent = percent.coerceIn(0f, 100f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                val cornerRadius = height.toPx() / 2f
                // Background
                drawRoundRect(
                    color = bgTrack,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
                // Usage
                if (clampedPercent > 0f) {
                    val fillWidth = size.width * (clampedPercent / 100f)
                    drawRoundRect(
                        color = color,
                        size = Size(fillWidth, size.height),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${clampedPercent.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Compact network speed indicator with up/down arrows.
 */
@Composable
fun NetworkSpeedRow(
    downSpeed: String?,
    upSpeed: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Download
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2193",
                color = NetworkDownBlue,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = downSpeed ?: "-",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Upload
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\u2191",
                color = NetworkUpGreen,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = upSpeed ?: "-",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Compact load average display.
 */
@Composable
fun LoadIndicator(
    load1: Double?,
    load5: Double?,
    load15: Double?,
    modifier: Modifier = Modifier
) {
    if (load1 == null && load5 == null && load15 == null) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Load",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = listOfNotNull(
                load1?.let { String.format("%.1f", it) },
                load5?.let { String.format("%.1f", it) },
                load15?.let { String.format("%.1f", it) }
            ).joinToString("/"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Combined metrics dashboard for a server card.
 * Shows CPU ring, memory/disk bars, and network speed in a compact layout.
 */
@Composable
fun ServerMetricsDashboard(
    server: ServerUiModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: CPU ring + Network speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CPU gauge
            if (server.cpuPercent != null) {
                UsageRing(
                    percent = server.cpuPercent,
                    label = strings.cpu,
                    size = 52.dp,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Memory & Disk bars (stacked vertically next to CPU ring)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (server.memoryPercent != null) {
                    UsageBar(
                        percent = server.memoryPercent,
                        label = "RAM",
                        height = 7.dp,
                        barColor = MemoryBlue
                    )
                }
                if (server.diskPercent != null) {
                    UsageBar(
                        percent = server.diskPercent,
                        label = "DSK",
                        height = 7.dp,
                        barColor = DiskPurple
                    )
                }
            }
        }

        // Row 2: Network speed + load
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkSpeedRow(
                downSpeed = server.netInSpeedText,
                upSpeed = server.netOutSpeedText
            )
            LoadIndicator(
                load1 = server.load1,
                load5 = server.load5,
                load15 = server.load15
            )
        }
    }
}
