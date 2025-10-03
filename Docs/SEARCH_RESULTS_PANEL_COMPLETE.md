# Панель результатов поиска - Полная документация

**Дата создания:** 2025-10-04  
**Статус:** ✅ ЗАВЕРШЕНО - Полностью интегрировано в MapScreen  
**Компоненты:** SearchResultsPanel.kt, CompactAttractionCard.kt  

## 🎯 Обзор

Интерактивная панель результатов поиска для режима карты, которая появляется при вводе текста в строке поиска и обеспечивает плавное взаимодействие с результатами поиска.

## 🏗️ Архитектура

### Основные компоненты:

#### 1. **SearchResultsPanel.kt**
- **Двухстадийная архитектура**: Expanded/Half состояния (без Hidden при взаимодействии)
- **Адаптивное позиционирование**: Учет высоты клавиатуры и фильтров (160dp защищенная область)
- **Drag-жесты**: Плавное переключение между состояниями с ограничением области
- **Интеграция с BottomSheet**: Приоритет Half состояния при открытом BottomSheet

#### 2. **CompactAttractionCard.kt**
- **Компактный дизайн**: Высота 80dp с круглым фото 56dp
- **Минималистичная информация**: Название, категория, рейтинг
- **Цветовое кодирование**: Категории имеют уникальные цвета

#### 3. **MapViewModel - Расширения**
- `showSearchPanel: StateFlow<Boolean>` - видимость панели
- `searchPanelHasKeyboard: StateFlow<Boolean>` - состояние клавиатуры
- `selectedFromPanel: StateFlow<Boolean>` - флаг выбора из панели
- `selectAttractionFromPanel()` - выбор места с центрированием карты
- `centerMapOnSearchResults()` - умное центрирование на результатах

## 🎮 Состояния и логика

### Приоритет состояний (по убыванию):
1. **!isVisible** → Hidden (поиск полностью отключен)
2. **isBottomSheetOpen** → Half (BottomSheet открыт - панель всегда Half)
3. **hasKeyboard** → Expanded (клавиатура активна)
4. **else** → Half (по умолчанию)

### Логика состояний:
```kotlin
LaunchedEffect(isVisible, hasKeyboard, isBottomSheetOpen) {
    panelState = when {
        !isVisible -> SearchPanelState.Hidden // Only hide when search is completely dismissed
        isBottomSheetOpen -> SearchPanelState.Half // Always Half when BottomSheet is open
        hasKeyboard -> SearchPanelState.Expanded // Full expansion with keyboard
        else -> SearchPanelState.Half // Default to half state
    }
}
```

### Drag-жесты:
```kotlin
// Ограничение между Expanded и Half
val newOffset = (offsetY + dragAmount.y).coerceIn(totalTopHeightPx, halfOffset)

// Определение состояния по позиции
val relativePosition = (offsetY - totalTopHeightPx) / (halfOffset - totalTopHeightPx)
panelState = when {
    relativePosition < 0.5f -> SearchPanelState.Expanded
    else -> SearchPanelState.Half
}
```

## 🎬 Пользовательские сценарии

### Основной поток:
```
1. [Поиск] → Панель: Expanded (с клавиатурой)
2. [Enter/скрытие клавиатуры] → Панель: Half + автоцентрирование карты
3. [Клик на карточку] → Панель: Half + BottomSheet поверх
4. [Закрытие BottomSheet] → Панель: Half (остается видимой)
5. [Drag вверх/вниз] → Переключение Expanded ↔ Half
6. [Очистка поиска] → Панель: Hidden (полностью скрывается)
```

### Умная кнопка поиска:
- **Есть текст**: Clear (X) - очищает поле
- **Пустое поле**: Close (X) - закрывает панель и убирает фокус

## 🗺️ Центрирование карты

### Для одного результата:
```kotlin
// Смещение центра вниз на 100м, чтобы маркер был в верхней части
val latOffset = 0.001 // ~100m
val targetPoint = Point(
    attraction.location.latitude - latOffset,
    attraction.location.longitude
)
```

### Для множественных результатов:
```kotlin
// Расчет центра с отступом вверх
val centerLat = (minLat + maxLat) / 2.0
val downwardShift = latSpanWithPadding * 0.25 // 25% смещение вниз
val adjustedCenterLat = centerLat - downwardShift

// Умный зум в зависимости от расстояния
val zoom = when {
    maxSpan > 0.1 -> 10.0f
    maxSpan > 0.05 -> 12.0f
    maxSpan > 0.01 -> 14.0f
    else -> 15.0f
}
```

## 📐 Технические параметры

### Размеры и позиционирование:
- **Защищенная область**: 160dp (строка поиска 120dp + фильтры 40dp)
- **Expanded позиция**: Начинается от 160dp (под фильтрами)
- **Half позиция**: 60% экрана от верха
- **Карточка**: Высота 80dp, фото 56dp (круглое)
- **Отступы**: 12dp внутри карточки, 8dp между карточками

### Анимации:
- **Spring анимация**: `dampingRatio = LowBouncy` для появления
- **Tween 300ms**: для скрытия
- **Центрирование карты**: 1-1.5 сек плавная анимация

### Цвета категорий:
- NATURE: #4CAF50 (зеленый)
- HISTORY: #795548 (коричневый)
- CULTURE: #9C27B0 (фиолетовый)
- ENTERTAINMENT: #FF9800 (оранжевый)
- RECREATION: #2196F3 (синий)
- GASTRONOMY: #FFC107 (желтый)
- RELIGIOUS: #607D8B (серо-синий)
- ADVENTURE: #FF5722 (красно-оранжевый)

## 🔧 Интеграция

### В MapScreen.kt:
```kotlin
SearchResultsPanel(
    attractions = filteredAttractions,
    isVisible = showSearchPanel,
    hasKeyboard = isKeyboardVisible,
    isBottomSheetOpen = uiState.showAttractionDetail,
    onAttractionClick = { attraction ->
        viewModel.selectAttractionFromPanel(attraction, mapView)
    },
    onDismiss = {
        viewModel.setSearchPanelVisibility(false)
        keyboardController?.hide()
    }
)
```

### В MapViewModel.kt:
```kotlin
fun selectAttractionFromPanel(attraction: Attraction, mapView: MapView?) {
    _selectedFromPanel.value = true
    _searchPanelHasKeyboard.value = false // Ensure Half state
    centerMapOnAttraction(attraction, mapView)
    selectAttraction(attraction)
}
```

## ✅ Преимущества реализации

1. **🎯 Интуитивный UX** - панель появляется автоматически при поиске
2. **📱 Адаптивность** - подстраивается под клавиатуру и BottomSheet
3. **🎨 Минималистичный дизайн** - не перегружает интерфейс
4. **🎬 Плавные анимации** - профессиональный вид
5. **🗺️ Интеграция с картой** - умное центрирование и фокусировка
6. **🔄 Синхронизация** - работает синхронно с маркерами на карте
7. **👆 Drag-жесты** - интуитивное управление состояниями
8. **🧠 Умное поведение** - адаптивная логика в зависимости от контекста

## 📊 Результаты

- **Время разработки**: ~6 часов (включая итерации и исправления)
- **Компоненты**: 2 новых файла + расширения MapViewModel
- **Состояния**: Упрощено до 2 активных состояний (Expanded/Half)
- **Интеграция**: Полная интеграция с существующей архитектурой
- **Тестирование**: Все сценарии проверены и работают корректно

Панель результатов поиска значительно улучшает UX приложения, предоставляя быстрый и удобный доступ к результатам поиска прямо на экране карты.
