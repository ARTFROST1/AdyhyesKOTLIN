package com.adygyes.app.presentation.ui.map.markers

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.adygyes.app.R
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import timber.log.Timber

/**
 * Провайдер для нативного маркера местоположения пользователя
 * Создает статичный маркер, который не дергается при движении карты
 */
class UserLocationMarkerProvider(
    private val mapView: MapView,
    private val context: Context
) {
    private val mapObjectCollection = mapView.map.mapObjects
    private var userLocationMarker: PlacemarkMapObject? = null
    private var currentLocation: Pair<Double, Double>? = null
    
    /**
     * Показать маркер местоположения пользователя
     */
    fun showUserLocationMarker(location: Pair<Double, Double>) {
        try {
            // Проверяем, нужно ли обновлять маркер
            if (currentLocation == location && userLocationMarker != null) {
                return // Маркер уже на правильном месте
            }
            
            currentLocation = location
            
            // Удаляем старый маркер если есть
            userLocationMarker?.let { marker ->
                mapObjectCollection.remove(marker)
            }
            
            // Создаем новый маркер
            val point = Point(location.first, location.second)
            val imageProvider = createUserLocationIcon()
            
            userLocationMarker = mapObjectCollection.addPlacemark(point, imageProvider).apply {
                // Устанавливаем z-index выше обычных маркеров
                zIndex = 1000f
                // Отключаем возможность клика
                userData = "user_location"
            }
            
            Timber.d("✅ User location marker created at: ${location.first}, ${location.second}")
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to create user location marker")
        }
    }
    
    /**
     * Скрыть маркер местоположения пользователя
     */
    fun hideUserLocationMarker() {
        userLocationMarker?.let { marker ->
            mapObjectCollection.remove(marker)
            userLocationMarker = null
            currentLocation = null
            Timber.d("🚫 User location marker removed")
        }
    }
    
    /**
     * Создает иконку для маркера местоположения пользователя
     */
    private fun createUserLocationIcon(): ImageProvider {
        val size = 56 // Увеличенный размер маркера в dp
        val density = context.resources.displayMetrics.density
        val sizePixels = (size * density).toInt()
        
        // Создаем bitmap для маркера
        val bitmap = Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Цвета - зеленый маркер как в оригинале
        val primaryColor = Color.parseColor("#4CAF50") // Зеленый цвет как в оригинале
        val whiteColor = Color.WHITE
        
        // Рисуем зеленый круг с тенью
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = primaryColor
            setShadowLayer(8f * density, 0f, 2f * density, Color.argb(80, 0, 0, 0))
        }
        
        val centerX = sizePixels / 2f
        val centerY = sizePixels / 2f
        val radius = sizePixels / 2f - 4f * density
        
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Рисуем иконку приложения (белая)
        val iconPaint = Paint().apply {
            isAntiAlias = true
            color = whiteColor
            style = Paint.Style.FILL
        }
        
        // Пытаемся загрузить иконку приложения
        try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
            if (drawable != null) {
                // Размер иконки - используем основной радиус
                val iconSize = (radius * 1.2f).toInt()
                val iconLeft = (centerX - iconSize / 2).toInt()
                val iconTop = (centerY - iconSize / 2).toInt()
                
                drawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                drawable.setTint(whiteColor)
                drawable.draw(canvas)
            } else {
                // Fallback: рисуем простую точку
                val dotRadius = 8f * density
                canvas.drawCircle(centerX, centerY, dotRadius, iconPaint)
            }
        } catch (e: Exception) {
            // Fallback: рисуем простую точку
            val dotRadius = 6f * density
            canvas.drawCircle(centerX, centerY, dotRadius, iconPaint)
            Timber.w(e, "Failed to load app icon for user location marker")
        }
        
        return ImageProvider.fromBitmap(bitmap)
    }
    
    /**
     * Очистка ресурсов
     */
    fun cleanup() {
        hideUserLocationMarker()
    }
}
