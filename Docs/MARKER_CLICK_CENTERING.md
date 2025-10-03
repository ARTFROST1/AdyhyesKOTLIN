# Центрирование карты при клике на маркер

## Изменение
Добавлено автоматическое центрирование карты с смещением при клике на любой маркер на карте, аналогично поведению при выборе из списка результатов.

## Реализация

### Обновленный MapViewModel.onMarkerClick():
```kotlin
fun onMarkerClick(attraction: Attraction, mapView: com.yandex.mapkit.mapview.MapView?) {
    Timber.d("✅ Marker clicked via overlay: ${attraction.name}")
    
    // Center map on clicked attraction with same offset as selection from panel
    centerMapOnAttraction(attraction, mapView)
    
    // Then show bottom sheet
    selectAttraction(attraction)
}
```

### Обновленные сигнатуры компонентов:
```kotlin
// DualLayerMarkerSystem
onMarkerClick: (Attraction, MapView?) -> Unit

// MarkerOverlay  
onMarkerClick: (Attraction, MapView?) -> Unit

// TransparentClickOverlay
onMarkerClick: (Attraction, MapView?) -> Unit
```

### Передача mapView в MapScreen:
```kotlin
onMarkerClick = { attraction ->
    Timber.d("🎯 DUAL-LAYER SYSTEM: Clicked ${attraction.name}")
    viewModel.onMarkerClick(attraction, mapView)
}
```

## Поведение

### До изменения:
1. Клик на маркер → открытие BottomSheet
2. Карта остается в текущей позиции
3. Маркер может быть в любом месте экрана

### После изменения:
1. Клик на маркер → центрирование с смещением
2. Маркер появляется в верхней части экрана (смещение 100м)
3. Плавная анимация 1 секунда
4. Открытие BottomSheet

## Консистентность поведения

Теперь одинаковое поведение для всех способов выбора достопримечательности:

✅ **Клик на карточку в панели поиска** → центрирование + BottomSheet  
✅ **Клик на маркер на карте** → центрирование + BottomSheet  
✅ **Выбор из списка** → центрирование + BottomSheet  

## Параметры центрирования

- **Смещение**: 100 метров вниз (`latOffset = 0.001`)
- **Зум**: 17.0f для детального просмотра
- **Анимация**: Плавная анимация 1 секунда
- **Результат**: Маркер в верхней части экрана

## Преимущества

✅ **Единообразный UX** - одинаковое поведение во всех случаях  
✅ **Лучшая видимость** - маркер всегда в оптимальной позиции  
✅ **Контекст окружения** - видна область вокруг достопримечательности  
✅ **Удобство просмотра** - маркер не перекрывается UI элементами  

Теперь клик на любой маркер обеспечивает оптимальное позиционирование для просмотра!
