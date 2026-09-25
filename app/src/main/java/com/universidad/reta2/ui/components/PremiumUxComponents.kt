package com.universidad.reta2.ui.components

import androidx.compose.ui.graphics.luminance
import com.universidad.reta2.ui.theme.SkeletonBaseDark
import com.universidad.reta2.ui.theme.SkeletonBaseLight
import com.universidad.reta2.ui.theme.SkeletonGlowDark
import com.universidad.reta2.ui.theme.SkeletonGlowLight
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── 1. EFECTO SHIMMER PARA SKELETON LOADERS ───────────────────
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat().coerceAtLeast(1f),
        targetValue = 2 * size.width.toFloat().coerceAtLeast(1f),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // El esqueleto se apoya en el tema en lugar de llevar los grises fijos: en
    // oscuro era un bloque casi blanco sobre la tarjeta (12,63:1). Se decide por
    // la luminancia de la superficie y no por el tema del sistema, porque el
    // usuario puede forzar claro u oscuro desde Perfil.
    val esquemaOscuro = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val baseEsqueleto = if (esquemaOscuro) SkeletonBaseDark else SkeletonBaseLight
    val brilloEsqueleto = if (esquemaOscuro) SkeletonGlowDark else SkeletonGlowLight

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                baseEsqueleto,
                brilloEsqueleto,
                baseEsqueleto,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@Composable
fun CompetenceSkeletonItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
        }
    }
}

// ── 2. ESTADOS VACÍOS (EMPTY STATES) ILUSTRADOS ────────────────
@Composable
fun IllustratedEmptyState(
    title: String,
    description: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Silueta ilustrativa usando figuras nativas en escala de grises
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onActionClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = actionLabel, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

// ── 3. CONTADOR ANIMADO (ANIMATED COUNTER) ─────────────────────
@Composable
fun AnimatedCounterText(
    targetValue: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified,
    suffix: String = ""
) {
    var startAnim by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (startAnim) targetValue else 0,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "counterAnimation"
    )

    LaunchedEffect(targetValue) {
        startAnim = true
    }

    Text(
        text = "$animatedValue$suffix",
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

// ── 4. INDICADOR DE PASOS VISUAL (PROGRESS STEP INDICATOR) ─────
@Composable
fun ProgressStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isActive = i == currentStep
            val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            val width = if (isActive) 24.dp else 8.dp

            val animatedWidth by animateDpAsState(
                targetValue = width,
                animationSpec = tween(durationMillis = 300),
                label = "stepWidth"
            )

            Box(
                modifier = Modifier
                    .size(height = 8.dp, width = animatedWidth)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ── 5. BARRA DE NIVEL CON XP ───────────────────────────────────
@Composable
fun LevelXpBar(totalQuestionsAnswered: Int) {
    // Definimos niveles: Nivel 1 (0-50), Nivel 2 (51-150), Nivel 3 (151+)
    val levelInfo = remember(totalQuestionsAnswered) {
        when {
            totalQuestionsAnswered <= 50 -> {
                Triple(1, totalQuestionsAnswered, 50)
            }
            totalQuestionsAnswered <= 150 -> {
                Triple(2, totalQuestionsAnswered - 50, 100)
            }
            else -> {
                Triple(3, (totalQuestionsAnswered - 150).coerceAtMost(200), 200)
            }
        }
    }

    val currentLevel = levelInfo.first
    val currentXp = levelInfo.second
    val maxXp = levelInfo.third
    val progress = currentXp.toFloat() / maxXp.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nivel de Experiencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LVL $currentLevel",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currentXp / $maxXp XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Siguiente nivel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── 6. INSIGNIAS (BADGES) DESBLOQUEABLES ───────────────────────
@Composable
fun UnlockableBadgesGrid(totalQuestionsAnswered: Int, currentStreak: Int) {
    val badges = listOf(
        BadgeItemData("Primer Intento", "Responde tu primera pregunta", totalQuestionsAnswered >= 1, Icons.Filled.GpsFixed),
        BadgeItemData("Constancia", "Alcanza una racha de 3 días", currentStreak >= 3, Icons.Filled.LocalFireDepartment),
        BadgeItemData("Maestro Básico", "Responde 50 preguntas", totalQuestionsAnswered >= 50, Icons.Filled.WorkspacePremium),
        BadgeItemData("Explorador", "Responde 100 preguntas", totalQuestionsAnswered >= 100, Icons.Filled.Rocket)
    )

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Insignias Desbloqueadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            badges.forEach { badge ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (badge.isUnlocked) borderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (badge.isUnlocked) badge.icon else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .padding(bottom = 4.dp)
                        )
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

data class BadgeItemData(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: ImageVector
)
