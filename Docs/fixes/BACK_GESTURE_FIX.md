# Back Gesture Navigation Fix

## Проблема
Жест "назад" выкидывал пользователя из приложения вместо навигации по иерархии экранов. Это происходило потому, что в приложении полностью отсутствовала обработка системного жеста через `BackHandler`.

## Причина
Android Compose требует явной обработки жеста "назад" через компонент `BackHandler`. Без него система всегда выполняет действие по умолчанию - выход из Activity.

## Решение

### 1. MapScreenContainer.kt
Добавлена обработка навигации между Settings и подэкранами:

```kotlin
BackHandler(enabled = screenMode != ScreenMode.MAP) {
    when (screenMode) {
        ScreenMode.ABOUT, ScreenMode.PRIVACY, ScreenMode.TERMS -> {
            // Возврат в Settings
            screenMode = ScreenMode.SETTINGS
        }
        ScreenMode.SETTINGS -> {
            // Возврат на карту
            screenMode = ScreenMode.MAP
        }
        ScreenMode.MAP -> {
            // Система обработает (выход из приложения на корневом экране)
        }
    }
}
```

**Иерархия:**
- About/Privacy/Terms → Settings → Map → Выход

### 2. MapScreen.kt
Добавлена многоуровневая обработка с правильными приоритетами:

```kotlin
val shouldInterceptBack = selectedAttraction != null || 
                          showSearchPanel || 
                          isSearchFieldFocused || 
                          (showCategoryCarousel && viewMode == ViewMode.MAP) || 
                          viewMode == ViewMode.LIST

BackHandler(enabled = shouldInterceptBack) {
    when {
        // Priority 1: Закрыть bottom sheet
        selectedAttraction != null -> {
            viewModel.clearSelection()
        }
        // Priority 2: Скрыть панель поиска
        showSearchPanel -> {
            viewModel.setSearchPanelVisibility(false)
            keyboardController?.hide()
        }
        // Priority 3: Убрать фокус с поиска
        isSearchFieldFocused -> {
            focusManager.clearFocus()
            keyboardController?.hide()
            isSearchFieldFocused = false
        }
        // Priority 4: Скрыть карусель категорий
        showCategoryCarousel && viewMode == ViewMode.MAP -> {
            showCategoryCarousel = false
        }
        // Priority 5: Переключить с LIST на MAP
        viewMode == ViewMode.LIST -> {
            viewModel.toggleViewMode()
        }
    }
}
```

**Иерархия на MapScreen:**
1. Bottom Sheet (детали места)
2. Search Panel (панель результатов)
3. Search Focus (фокус на поле поиска)
4. Category Carousel (карусель фильтров)
5. LIST mode → MAP mode
6. MAP mode → Переход к предыдущему экрану/выход

### 3. FavoritesScreen.kt
Простая обработка для возврата:

```kotlin
BackHandler(enabled = onNavigateBack != null) {
    onNavigateBack?.invoke()
}
```

### 4. SearchScreen.kt
Обработка filter bottom sheet:

```kotlin
BackHandler(enabled = true) {
    if (showFilterSheet) {
        showFilterSheet = false
    } else {
        onBackClick()
    }
}
```

**Иерархия:**
- Filter Sheet → Search Screen → Map

### 5. AttractionDetailScreen.kt
Обработка photo viewer:

```kotlin
BackHandler(enabled = true) {
    if (showPhotoViewer) {
        showPhotoViewer = false
    } else {
        onBackClick()
    }
}
```

**Иерархия:**
- Photo Viewer → Detail Screen → Предыдущий экран

## Архитектура решения

### Принцип работы BackHandler:
```kotlin
BackHandler(enabled: Boolean) {
    // Действие при нажатии жеста назад
}
```

- `enabled = true` - перехватывает жест
- `enabled = false` - пропускает жест дальше (к другим BackHandler или системе)

### Множественные BackHandler:
Если несколько BackHandler активны одновременно, срабатывает **последний добавленный** (самый вложенный в иерархии Composable).

### Иерархия обработчиков:
```
Photo Viewer BackHandler (priority 1)
    ↓ (если не активен)
Bottom Sheet BackHandler (priority 2)
    ↓ (если не активен)
Search Panel BackHandler (priority 3)
    ↓ (если не активен)
Screen BackHandler (priority 4)
    ↓ (если не активен)
System default (выход из приложения)
```

## Результат

### ✅ Правильная навигация по всем экранам:
1. **MapScreenContainer**: Settings → About/Privacy/Terms работает
2. **MapScreen**: Закрытие bottom sheet, панелей, переключение режимов
3. **FavoritesScreen**: Возврат на карту
4. **SearchScreen**: Закрытие фильтров, возврат
5. **DetailScreen**: Закрытие photo viewer, возврат

### ✅ Логичная иерархия:
Жест "назад" работает интуитивно - закрывает самый верхний элемент UI или возвращает на предыдущий экран.

### ✅ Логирование:
Все действия жеста логируются через Timber для отладки:
```kotlin
Timber.d("🔙 Back pressed: closing bottom sheet")
```

## Тестирование

### Проверка MapScreen:
1. Открыть bottom sheet → Back → Закрывается
2. Открыть search panel → Back → Закрывается  
3. Фокус на поиске → Back → Убирается фокус
4. Показать фильтры → Back → Скрываются
5. Режим LIST → Back → Переход в MAP
6. Режим MAP → Back → Выход (если корневой экран)

### Проверка Settings:
1. Map → Settings → Back → Map
2. Settings → About → Back → Settings
3. Settings → Privacy → Back → Settings
4. Settings → Terms → Back → Settings

### Проверка других экранов:
1. Detail → Photo Viewer → Back → Detail
2. Detail → Back → Предыдущий экран
3. Search → Filter Sheet → Back → Search
4. Search → Back → Map
5. Favorites → Back → Map

## Файлы изменены:
- `MapScreenContainer.kt` - обработка Settings navigation
- `MapScreen.kt` - многоуровневая обработка UI элементов
- `FavoritesScreen.kt` - простой возврат
- `SearchScreen.kt` - обработка filter sheet
- `AttractionDetailScreen.kt` - обработка photo viewer

## Заметки
- BackHandler автоматически очищается при удалении Composable из композиции
- Важен порядок приоритетов в `when` блоке
- `enabled` параметр позволяет условно включать/выключать обработчик
- Система Android автоматически обрабатывает выход когда все BackHandler пропущены
