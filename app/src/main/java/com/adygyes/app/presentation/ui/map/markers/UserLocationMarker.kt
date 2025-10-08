package com.adygyes.app.presentation.ui.map.markers

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adygyes.app.R

/**
 * Маркер местоположения пользователя с иконкой приложения
 * Отображается как круглая иконка с анимацией пульсации
 */
@Composable
fun UserLocationMarker(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showPulseAnimation: Boolean = true
) {
    // Анимация пульсации
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (showPulseAnimation) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Анимация появления
    var isVisible by remember { mutableStateOf(false) }
    val appearanceScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "appearance_scale"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Box(
        modifier = modifier.size(size * 1.5f), // Увеличиваем область для пульсации
        contentAlignment = Alignment.Center
    ) {
        // Внешний пульсирующий круг (полупрозрачный)
        if (showPulseAnimation) {
            Surface(
                modifier = Modifier
                    .size(size * pulseScale)
                    .scale(appearanceScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ) {}
        }
        
        // Основной маркер
        Surface(
            modifier = Modifier
                .size(size)
                .scale(appearanceScale),
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                // Внутренний круг с иконкой приложения
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Мое местоположение",
                            modifier = Modifier.size(size * 0.6f),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Простой маркер местоположения пользователя без анимации
 * Для использования в нативных маркерах Yandex Maps
 */
@Composable
fun SimpleUserLocationMarker(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        shadowElevation = 6.dp,
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Мое местоположение",
                        modifier = Modifier.size(size * 0.6f),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
