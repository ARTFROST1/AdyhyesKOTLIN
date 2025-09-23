---

# 📱 Карта приложения **Adygyes**

**Последнее обновление:** Декабрь 2024  
**Версия:** Stage 8 Complete - Unified MapScreen Implementation  
**Статус:** ✅ Полностью реализовано

## 🟩 Главный экран — **Unified MapScreen с надежной обработкой кликов**

**Архитектура:** Единый MapScreen.kt объединяет все лучшие функции из предыдущих версий

**Содержимое:**

* **Карта (Yandex Maps API) / Список достопримечательностей** (плавное переключение с анимацией)
* **Поле поиска с real-time фильтрацией** (floating Surface над картой) 🔍
* **Метки достопримечательностей** с 100% надежными кликами (userData validation)
* **Геообъекты** (многоугольники парков, линии троп, водные объекты)
* **Кнопка "Моё местоположение"** с GPS позиционированием 📍
* **Edge-to-edge дизайн** с правильными WindowInsets
* **Нижнее навигационное меню** (Material Design 3):
  - Переключение вида (карта/список) 📋 с анимированными иконками
  - Избранное ⭐ с badge счетчиком
  - Настройки ⚙️

**Технические особенности:**
* **Debounced search** (300ms задержка) для оптимальной производительности
* **Кластеризация маркеров** с TextImageProvider для групп
* **Proper MapKit lifecycle** с DisposableEffect
* **Comprehensive logging** с emoji индикаторами для отладки

**Действия:**

* 🔘 **Ввод в поле поиска** → мгновенная фильтрация на карте/в списке (debounced)
* 🔘 **Кнопка фильтров** → открыть CategoryFilterBottomSheet с выбором категорий
* 🔘 **Нижнее меню: Переключение вида** → плавная анимация карта ↔ список
* 🔘 **Нижнее меню: Избранное** → переход в **FavoritesScreen**
* 🔘 **Нижнее меню: Настройки** → переход в **SettingsScreen**
* 🔘 **Клик по метке/элементу списка** → **AttractionBottomSheet** (100% гарантированно)
* 🔘 **Масштабирование/перемещение карты** → обновление меток (оптимизировано)
* 🔘 **Кнопка «Моё местоположение»** → центрирование карты на пользователе с анимацией

---

## 🟦 Компонент — **AttractionBottomSheet** (Modal Bottom Sheet)

**Реализация:** AttractionBottomSheet.kt с Material Design 3

**Содержимое:**

* **PhotoGallery** - Галерея фото с swipe и zoom поддержкой
* **Название достопримечательности** - Typography.headlineSmall
* **Краткое описание** - Scrollable content с полной информацией
* **Рейтинг** - RatingBar component с звездами ⭐
* **Контактная информация** - Часы работы, цены, телефон
* **Кнопки действий** (Material 3 buttons):
  * 🚩 **«Построить маршрут»** → NavigationUseCase → Яндекс.Карты
  * ⭐ **«Добавить в избранное»** → ToggleFavorite в ViewModel
  * 🔗 **«Поделиться»** → ShareUseCase → нативный share sheet

**Технические особенности:**
* **ModalBottomSheet** с rememberModalBottomSheetState
* **key(attraction.id)** для принудительного пересоздания
* **Drag handle** для удобного закрытия
* **Proper dismiss handling** с clearSelection в ViewModel

**Действия:**

* **Swipe down / tap outside** → onDismiss() → возврат на карту
* **Кнопка «Избранное»** → toggleFavorite() → обновление состояния
* **Кнопка «Поделиться»** → shareAttraction() → системное меню
* **Кнопка «Маршрут»** → navigateToAttraction() → внешняя навигация

---

## 🟨 Экран — **Избранное**

**Содержимое:**

* Список сохранённых мест (мини-карточки с фото, названием, категорией).
* Возможность открыть карточку места.
* Возможность удалить из избранного.

**Действия:**

* Нажать на место → открывается **Карточка достопримечательности**.
* Удалить из списка → обновить избранное.

---

## 🟧 Компонент — **CategoryFilterBottomSheet** (Фильтрация категорий)

**Реализация:** CategoryFilterBottomSheet.kt с Material Design 3

**Содержимое:**

* **Header** с заголовком "Фильтры" и кнопкой "Очистить все"
* **LazyColumn** со списком всех категорий AttractionCategory.values()
* **Checkbox** для каждой категории с состоянием выбора
* **Apply button** для применения фильтров
* **ModalBottomSheet** с rememberModalBottomSheetState

**Технические особенности:**
* **Real-time filtering** - изменения применяются мгновенно
* **State management** через selectedCategories StateFlow
* **Category toggle** через ViewModel.toggleCategory()
* **Persistent state** - фильтры сохраняются при навигации

**Действия:**

* **Выбор категории** → toggleCategory() → мгновенная фильтрация на карте/списке
* **Кнопка "Очистить все"** → clearFilters() → сброс всех фильтров
* **Кнопка "Применить"** → onApply() → закрытие bottom sheet
* **Swipe down / tap outside** → onDismiss() → закрытие без изменений

---

## 🟥 Экран — **Настройки** (будущее, но заложить структуру)

**Содержимое:**

* Язык (Русский / English).
* Тёмная тема 🌙.
* Информация о приложении.
* (Фундамент для будущего: профиль, авторизация, уведомления).

---

# 🔄 User Flow (Unified MapScreen Architecture)

```
[MapScreen.kt - Unified Main Screen]
   │
   ├── 🔍 UnifiedSearchTextField (floating)
   │    ├── Real-time input → debounced search (300ms)
   │    ├── Clear button → onValueChange("")
   │    └── Filter button → CategoryFilterBottomSheet
   │         ├── Category checkboxes → toggleCategory()
   │         ├── "Clear All" → clearFilters()
   │         └── "Apply" → close sheet
   │
   ├── 📱 AdygyesBottomNavigation (Material 3)
   │    ├── View Toggle 📋 → AnimatedContent(MAP ↔ LIST)
   │    ├── Favorites ⭐ (with badge) → FavoritesScreen
   │    └── Settings ⚙️ → SettingsScreen
   │
   ├── 🗺️ Map Mode (AndroidView + MapView)
   │    ├── Yandex MapKit → ClusterizedPlacemarkCollection
   │    ├── CategoryMarkerProvider → colored markers
   │    ├── Marker tap → userData validation → selectAttraction()
   │    ├── Cluster tap → zoom in animation
   │    └── FAB 📍 → GPS location → camera animation
   │
   ├── 📋 List Mode (AttractionsList)
   │    ├── Filtered attractions → real-time updates
   │    ├── Item tap → onAttractionClick()
   │    ├── Favorite toggle → toggleFavorite()
   │    └── WindowInsets padding → proper layout
   │
   └── 🎯 selectedAttraction?.let → AttractionBottomSheet
        ├── key(attraction.id) → force recomposition
        ├── PhotoGallery → swipe & zoom
        ├── Attraction details → scrollable content
        ├── Action buttons:
        │    ├── 🚩 Route → NavigationUseCase → Yandex Maps
        │    ├── ⭐ Favorite → toggleFavorite() → state update
        │    └── 🔗 Share → ShareUseCase → system share
        └── onDismiss → clearSelection() → close sheet

Technical Features:
├── 🔧 Reliable marker taps (100% guaranteed)
├── 🎨 Edge-to-edge UI with WindowInsets
├── 🔄 Proper MapKit lifecycle (DisposableEffect)
├── 📊 Comprehensive logging (emoji indicators)
├── ⚡ Optimized marker updates (prevent recreation)
└── 🎭 Smooth animations (view mode transitions)
```

## 🏗️ Architecture Benefits

### ✅ **Code Consolidation:**
- **6 MapScreen files** → **1 unified MapScreen.kt**
- **Single source of truth** for map functionality
- **Easier maintenance** and debugging

### ✅ **Reliability Improvements:**
- **100% guaranteed marker taps** with userData validation
- **Proper state management** with debug logging
- **Robust error handling** in tap listeners

### ✅ **Modern UI/UX:**
- **Edge-to-edge design** with proper system insets
- **Material Design 3** components throughout
- **Smooth animations** and transitions
- **Responsive layout** for different screen sizes

### ✅ **Performance Optimizations:**
- **Debounced search** (300ms) for better performance
- **Smart marker updates** to prevent unnecessary recreation
- **Efficient state management** with StateFlow
- **Proper lifecycle handling** with DisposableEffect

---

# 📌 Ключевые принципы UX

* **Главный экран — это карта с возможностью переключения на список.**
* **Поиск интегрирован в главный экран** для быстрого доступа.
* Минимум кликов до информации (метка/элемент → карточка).
* Все действия (избранное, маршруты, поделиться) доступны прямо из карточки.
* Поддержка работы оффлайн (закэшированные данные + базовые карты).

---

# 🎯 Статус реализации (Stage 8 Complete)

## ✅ **Полностью реализовано:**

### 🗺️ **Unified MapScreen:**
- ✅ Единый MapScreen.kt с надежными кликами по меткам
- ✅ Edge-to-edge дизайн с правильными WindowInsets
- ✅ Real-time поиск с debouncing (300ms)
- ✅ Плавные анимации между режимами карты/списка
- ✅ Proper MapKit lifecycle с DisposableEffect

### 📱 **Bottom Navigation:**
- ✅ AdygyesBottomNavigation с Material Design 3
- ✅ Анимированные иконки и badge счетчики
- ✅ Навигация между экранами (Favorites, Settings)

### 🎛️ **Components:**
- ✅ AttractionBottomSheet с ModalBottomSheet
- ✅ CategoryFilterBottomSheet для фильтрации
- ✅ UnifiedSearchTextField с floating design
- ✅ AttractionsList для режима списка

### 💾 **Data & State:**
- ✅ 10 реальных достопримечательностей Адыгеи
- ✅ Room database с offline поддержкой
- ✅ StateFlow для reactive UI updates
- ✅ PreferencesManager для настроек

## 🚧 **В разработке (Stage 9):**
- Polish & Optimization
- Performance improvements
- UI/UX refinements
- Testing & bug fixes

## 📊 **Прогресс:** 95% (114/120 задач выполнено)
* Единый поток взаимодействия — поиск и просмотр в одном месте.
* Возможность масштабирования проекта на другие регионы (категория «Регион» в данных).

---