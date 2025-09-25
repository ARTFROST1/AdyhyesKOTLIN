Анализ текущего состояния
Я изучил ключевые документы и код, следуя вашему рабочему процессу (Docs/Implementation_Plan.md, Docs/project_structure.md, Docs/AppMap_adygyes.md, Docs/Bug_tracking.md):

Главный экран карты реализован в 
presentation/ui/screens/map/MapScreen.kt
. Здесь создается MapView внутри AndroidView(factory = { MapView(ctx) ... }), а MapKitFactory.onStart()/onStop() и mapView.onStop() вызываются в DisposableEffect. При навигации на другой экран этот Composable выходит из композиции — MapView останавливается и уничтожается, при возврате он создаётся заново. Это и вызывает «перезагрузку» экрана и сброс состояния камеры.
Навигация организована в 
presentation/navigation/AdygyesNavHost.kt
 — стартовая точка NavDestination.Map.route показывает 
MapScreen
. При переходе на Favorites/Settings/Detail MapScreen снимается с экрана.
Двухслойная система маркеров (визуальные нативные + прозрачный Compose-оверлей) реализована в:
presentation/ui/map/markers/DualLayerMarkerSystem.kt
.../VisualMarkerProvider.kt
Прозрачный клик-оверлей (MarkerOverlay) рассчитывает позиции по MapView.
Состояние карты (камера: lat/lon/zoom/azimuth/tilt) сейчас не сохраняется в 
PreferencesManager
 (
data/local/preferences/PreferencesManager.kt
) — там нет соответствующих ключей.
Логика состояния экрана карты управляется 
presentation/viewmodel/MapViewModel.kt
. Камера явно нигде не хранится.
Вывод: Причина пересоздания — MapView живет внутри 
MapScreen
 и уничтожается при смене маршрута. Даже если сохранить состояние камеры, «пересоздание» будет происходить. Чтобы карта оставалась активной, MapView нужно вынести на уровень выше навигации и держать постоянно в композиции приложения.

Цель
Карта загружается один раз при запуске приложения и остается активной даже при переходе на другие экраны.
Положение карты (центр, зум, азимут, наклон) сохраняется и восстанавливается:
При возврате на экран карты.
При пересоздании процесса (через DataStore как fallback).
Предлагаемая архитектура
Постоянный «хост» карты: MapHost — отдельный Composable, размещаемый на уровне 
AdygyesApp()
 (выше NavHost), который:
Создаёт единственный экземпляр MapView через remember.
Управляет жизненным циклом MapKit/MapView через LifecycleEventObserver активности (onResume → onStart; onPause → onStop).
Подключает визуальный слой маркеров (через 
VisualMarkerProvider
).
Предоставляет ссылку на MapView и контроллер камеры вниз через CompositionLocal (например, LocalMapHost).
Экран карты (оверлей): MapOverlayScreen (или адаптируем текущий 
MapScreen
) — НЕ создаёт MapView, а использует mapView из MapHost:
Отрисовывает прозрачный клик-оверлей MarkerOverlay поверх живой карты.
Рисует UI-оверлеи: поиск, переключатель режимов, нижнюю навигацию, BottomSheet.
Навигация: 
AdygyesApp()
 рендерит MapHost на заднем плане и поверх — NavHost для экранов:
Роут Map показывает только «оверлей» (UI+прозрачный клик-слой), карта позади уже живет.
Роуты Favorites/Settings/Detail рисуют поверх полноэкранные экраны (с фоном), «закрывая» видимость карты, но не уничтожая её.
Сохранение состояния камеры:
Вводим MapController/MapStateHolder (singleton/Hilt @Singleton) с StateFlow<CameraState> (lat/lon/zoom/azimuth/tilt).
При изменении камеры CameraListener обновляет MapStateHolder.
На app resume/restoration — восстанавливаем камеру из MapStateHolder или из 
PreferencesManager
.
Добавляем ключи камеры в 
PreferencesManager
 и методы чтения/записи. Периодически или при onPause — сохраняем состояние.
Детальный план внедрения
Архитектура постоянного MapHost
Создать presentation/ui/screens/map/MapHost.kt:
@Composable fun MapHost(...) c Box(Modifier.fillMaxSize()).
val context = LocalContext.current
val mapView = remember { MapView(context) }
Подключить LifecycleEventObserver (через LocalLifecycleOwner) для вызовов:
onStart/onResume → MapKitFactory.getInstance().onStart() и mapView.onStart()
onPause/onStop → mapView.onStop() и MapKitFactory.getInstance().onStop()
Применить стили карты при изменении темы (как в 
MapScreen
: MapStyleProvider.applyMapStyle(mapView, isDarkTheme)).
Начальная инициализация камеры:
Прочитать CameraState из MapStateHolder или из 
PreferencesManager
 (DataStore) и применить map.move(CameraPosition(...)).
Интегрировать визуальный слой маркеров:
Создать и держать 
VisualMarkerProvider
 через remember.
Экспонировать методы или колбэки для обновления маркеров (через MapController).
Предоставить LocalMapHost/LocalMapController:
CompositionLocal с mapView, методами moveCamera(...), addMarkers(...), 
clearMarkers()
, getCameraState().
Контейнер поверх карты и навигация
Обновить 
app/src/main/java/com/adygyes/app/MainActivity.kt
 → 
AdygyesApp()
:
Внутри Scaffold обернуть контент в Box:
Сначала MapHost() (всегда отрисовывается и живет).
Затем 
AdygyesNavHost(...)
 поверх (экраны рисуют фон и перекрывают карту).
Обновить 
presentation/navigation/AdygyesNavHost.kt
:
Для маршрута NavDestination.Map.route рендерить не 
MapScreen
 в текущем виде, а «оверлей» (см. следующий пункт).
Остальные экраны оставить как есть. Они будут просто перекрывать карту, которая остаётся активной.
Оверлей экрана карты
Вариант A (меньше правок): Переименовать текущий 
MapScreen.kt
 в MapOverlayScreen.kt и:
Удалить создание AndroidView { MapView(...) } — вместо этого получить mapView из LocalMapHost.
Вызов 
DualLayerMarkerSystem(...)
 оставить, но передавать mapView из LocalMapHost.
Оставить поиск, нижнюю навигацию, BottomSheet — это элементы оверлея.
Вариант B (минимальные изменения): Оставить имя 
MapScreen.kt
, но переработать его как «оверлей», чтобы он:
Не создавал новый MapView.
Работал поверх MapHost.
Состояние камеры и сохранение
Добавить в 
PreferencesManager.kt
 ключи:
KEY_CAMERA_LAT, KEY_CAMERA_LON, KEY_CAMERA_ZOOM, KEY_CAMERA_AZIMUTH, KEY_CAMERA_TILT.
Методы: updateCameraState(...), cameraStateFlow (или отдельные поля).
Создать MapStateHolder (например, presentation/ui/map/MapStateHolder.kt или data/local/map/MapStateRepository.kt под Hilt @Singleton):
data class CameraState(lat: Double, lon: Double, zoom: Float, azimuth: Float, tilt: Float)
val cameraState: MutableStateFlow<CameraState>
Методы updateFromMap(mapView.map.cameraPosition), applyToMap(mapView).
При изменении cameraState — дебаунсить и писать в 
PreferencesManager
 (для восстановления после убийства процесса).
В MapHost повесить CameraListener на mapView.map:
В onCameraPositionChanged обновлять MapStateHolder.cameraState.
При старте MapHost:
Прочитать cameraState из MapStateHolder (если пусто — из 
PreferencesManager
), применить на карту.
Интеграция маркеров (двухслойная система)
Визуальные маркеры: MapHost (нижний слой) управляет 
VisualMarkerProvider
:
Подписаться на список аттракций (например, через 
MapViewModel
 или общий репозиторий) и обновлять нативные маркеры.
Выбор маркера обновлять через вызовы 
updateSelectedMarker
.
Прозрачный клик-оверлей: оставляем в «оверлее» (бывший 
MapScreen
) — он рендерится, только когда открыт маршрут карты. На других экранах этот слой не нужен.
Совместимость и минимизация правок
Добавить LocalMapHost и переключатель режимов:
Если LocalMapHost существует, 
MapScreen
 работает как оверлей и пропускает создание MapView.
Это позволяет внедрять изменения поэтапно, с откатом при необходимости.
Тесты и верификация
Сценарии:
Запустить приложение → карта один раз инициализируется.
Перейти на Favorites/Settings → вернуться на Map — карта не пересоздалась, визуальные маркеры не мигали, кэш изображений не заново грузился (проверка логов из 
VisualMarkerProvider
).
Позиция камеры сохраняется при закрытии приложения и восстановлении (проверка чтения/записи в 
PreferencesManager
).
Проверить память/жизненный цикл: в фоне MapView должен останавливаться (onPause/onStop), чтобы не тратить батарею.
Какие файлы придётся изменить/добавить
Добавить:
presentation/ui/screens/map/MapHost.kt — постоянный хост карты.
presentation/ui/map/MapStateHolder.kt (или data/.../map/MapStateRepository.kt) — контроллер состояния камеры (Hilt @Singleton).
presentation/ui/screens/map/MapOverlayScreen.kt (или переработать 
MapScreen.kt
).
Изменить:
app/src/main/java/com/adygyes/app/MainActivity.kt
 → 
AdygyesApp()
:
В Scaffold → Box: сначала MapHost(), затем 
AdygyesNavHost(...)
.
presentation/navigation/AdygyesNavHost.kt
:
На маршруте карты — отображать оверлей, не создавать MapView.
presentation/ui/screens/map/MapScreen.kt
:
Превратить в оверлей (не создавать MapView).
data/local/preferences/PreferencesManager.kt
:
Добавить ключи и методы для CameraState.
Риски и как их нивелировать
Постоянный MapView потребляет ресурсы:
Решение: корректно управлять лайфциклом через LifecycleEventObserver (вызывать onStop() в фоне).
Дублирование маркеров:
Визуальные маркеры теперь создаются в MapHost, а клик-оверлей — в оверлее. Убедиться, что не создаём визуальные маркеры повторно в оверлее.
Сложность передачи mapView:
CompositionLocal (LocalMapHost) и/или MapController с явными методами.
Обратная совместимость:
Переходить поэтапно: сначала добавить MapHost + LocalMapHost и переключатель в 
MapScreen
, затем вырезать старый код создания MapView.
План работ (TODO)
Выполнено:
Консультация документации и анализ кода.
Определение причины пересоздания MapView.
Далее:
Добавить архитектурный слой MapHost (Persistent Map).
Спроектировать и внедрить MapStateHolder (камера).
Обновить 
PreferencesManager
 для хранения камеры.
Переработать 
MapScreen
 в оверлей, использующий LocalMapHost.
Обновить 
AdygyesApp
 и 
AdygyesNavHost
 для наложения поверх MapHost.
Тестирование и логирование.
Текущий список задач я завел и веду в ходе работы.

Что получите в итоге
Карта инициализируется один раз при старте приложения и остаётся активной.
Переход на любой экран не уничтожает карту (визуальные маркеры не пересоздаются, изображения не перезагружаются).
Положение карты/зум/азимут/наклон сохраняются и восстанавливаются.
Архитектура соответствует проектной структуре и не ломает текущую двухслойную систему маркеров.