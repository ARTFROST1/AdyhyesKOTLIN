# Исправление ошибок компиляции onMarkerClick

## Проблема
После изменения сигнатуры `onMarkerClick` возникли ошибки компиляции из-за несоответствия типов функций.

## Ошибки:
```
Argument type mismatch: actual type is 'kotlin.Function1<Attraction, Unit>', 
but 'kotlin.Function2<Attraction, MapView?, Unit>' was expected.
```

## Исправления:

### 1. DualLayerMarkerSystem.kt
```kotlin
// Было (неправильно):
onMarkerClick = { attraction -> onMarkerClick(attraction, mapView) }

// Стало (правильно):
onMarkerClick = onMarkerClick
```

### 2. MapScreen.kt
```kotlin
// Было (неправильно):
onMarkerClick = { attraction ->
    viewModel.onMarkerClick(attraction, mapView)
}

// Стало (правильно):
onMarkerClick = { attraction, mapViewParam ->
    viewModel.onMarkerClick(attraction, mapViewParam ?: mapView)
}
```

## Логика исправления:

1. **DualLayerMarkerSystem** - просто передает функцию как есть, без обертки
2. **MapScreen** - принимает два параметра и использует переданный mapView или fallback
3. **MarkerOverlay** - вызывает функцию с двумя параметрами

## Результат:
✅ Все ошибки компиляции устранены  
✅ Сигнатуры функций соответствуют ожиданиям  
✅ MapView корректно передается через всю цепочку вызовов  
✅ Центрирование работает при клике на маркер  

Теперь проект компилируется без ошибок!
