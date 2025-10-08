package com.adygyes.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adygyes.app.R
import kotlinx.coroutines.delay

/**
 * Стильный overlay для отображения процесса обновления данных
 */
@Composable
fun DataUpdateOverlay(
    isVisible: Boolean,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(600, easing = EaseInOut)
        ) + slideInVertically(
            animationSpec = tween(600, easing = EaseOutCubic),
            initialOffsetY = { it / 3 }
        ),
        exit = fadeOut(
            animationSpec = tween(400, easing = EaseInOut)
        ) + slideOutVertically(
            animationSpec = tween(400, easing = EaseInCubic),
            targetOffsetY = { -it / 3 }
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.7f)
                ),
            contentAlignment = Alignment.Center
        ) {
            DataUpdateCard(progress = progress)
        }
    }
}

@Composable
private fun DataUpdateCard(
    progress: Float,
    modifier: Modifier = Modifier
) {
    var currentMessageIndex by remember { mutableIntStateOf(0) }
    
    // Мотивирующие сообщения
    val messages = listOf(
        "Настраиваем всё для вас...",
        "Загружаем новые места...",
        "Подготавливаем карту...",
        "Осталось совсем чуть-чуть!",
        "Почти готово..."
    )
    
    // Автоматическая смена сообщений
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Меняем сообщение каждые 2 секунды
            currentMessageIndex = (currentMessageIndex + 1) % messages.size
        }
    }
    
    Card(
        modifier = modifier
            .padding(32.dp)
            .widthIn(max = 320.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Анимированная иконка
            AnimatedIcon()
            
            // Заголовок
            Text(
                text = "Обновление данных",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Анимированное сообщение
            AnimatedContent(
                targetState = messages[currentMessageIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                },
                label = "message_animation"
            ) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(24.dp)
                )
            }
            
            // Прогресс-бар
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                // Процент выполнения
                if (progress > 0f) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Дополнительная информация
            Text(
                text = "Мы подготавливаем для вас самую актуальную информацию о достопримечательностях Адыгеи",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimatedIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")
    
    // Вращение иконки
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Пульсация размера
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(40.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                },
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
