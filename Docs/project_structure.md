# Project Structure Guide

**Last Updated:** 2025-09-26  
**Current Version:** Stage 10 - Quality Assurance & Optimization (91% Complete)

> Branding: User-facing app name is "AdygGIS". Internal code/package retains "Adygyes" to avoid breaking changes.

## 🎯 Key Architecture Updates:
- **✅ Dual-Layer Marker System:** Native visual markers + Compose interactive overlay for 100% click reliability
- **✅ Map Clustering:** Dynamic marker clustering based on zoom level with visual cluster indicators
- **✅ Bottom Navigation:** Integrated bottom navigation with Map/List toggle
- **✅ Favorites Integration:** CategoryCarousel + List/Grid toggle + Sorting integrated into MapScreen
- **✅ ImageCacheManager:** Advanced image caching system with version-based cache invalidation
- **✅ Simplified Data Management:** JsonFileManager now only reads from assets/attractions.json
- **✅ Developer Mode Removed:** DeveloperScreen, DeveloperViewModel, AttractionEditorScreen replaced with stubs
- **✅ LocaleViewModel:** Runtime language switching support
- **✅ Hardware Bitmap Fix:** Resolved Canvas compatibility issues for map markers
- **🚧 In Progress:** Performance optimization, testing implementation, Firebase integration

## Project Directory Layout

```
AdyhyesKOTLIN/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/                    # Static data files
│   │   │   │   ├── attractions.json      # 10 real Adygea attractions
│   │   │   │   └── geo_objects.json      # Geographic objects data
│   │   │   ├── java/com/adygyes/app/
│   │   │   │   ├── data/                 # Data layer
│   │   │   │   │   ├── local/            # Local data sources
│   │   │   │   │   │   ├── cache/        # Cache management
│   │   │   │   │   │   │   ├── CacheManager.kt
│   │   │   │   │   │   │   └── ImageCacheManager.kt      # ⭐ NEW: Advanced image caching
│   │   │   │   │   │   ├── dao/          # Room DAOs
│   │   │   │   │   │   │   └── AttractionDao.kt
│   │   │   │   │   │   ├── database/     # Room database
│   │   │   │   │   │   │   └── AdygyesDatabase.kt
│   │   │   │   │   │   ├── entities/     # Room entities
│   │   │   │   │   │   │   └── AttractionEntity.kt
│   │   │   │   │   │   ├── locale/        # Locale management
│   │   │   │   │   │   │   └── LocaleManager.kt
│   │   │   │   │   │   ├── preferences/  # DataStore preferences
│   │   │   │   │   │   │   └── PreferencesManager.kt
│   │   │   │   │   │   └── JsonFileManager.kt  # Simplified JSON reader
│   │   │   │   │   ├── remote/           # Remote data sources
│   │   │   │   │   │   └── dto/          # Data transfer objects
│   │   │   │   │   │       └── AttractionDto.kt
│   │   │   │   │   ├── repository/       # Repository implementations
│   │   │   │   │   │   └── AttractionRepositoryImpl.kt
│   │   │   │   │   ├── mapper/           # Data mappers
│   │   │   │   │   │   └── AttractionMapper.kt
│   │   │   │   │   └── sync/             # Data synchronization
│   │   │   │   │       └── DataSyncManager.kt
│   │   │   │   ├── domain/               # Business logic
│   │   │   │   │   ├── model/            # Domain models
│   │   │   │   │   │   ├── Attraction.kt
│   │   │   │   │   │   └── GeoObject.kt
│   │   │   │   │   ├── repository/       # Repository interfaces
│   │   │   │   │   │   └── AttractionRepository.kt
│   │   │   │   │   └── usecase/          # Use cases
│   │   │   │   │       ├── AttractionDisplayUseCase.kt
│   │   │   │   │       ├── DataSyncUseCase.kt
│   │   │   │   │       ├── GetLocationUseCase.kt
│   │   │   │   │       ├── NavigationUseCase.kt
│   │   │   │   │       ├── NetworkUseCase.kt
│   │   │   │   │       └── ShareUseCase.kt
│   │   │   │   ├── di/                   # Dependency injection
│   │   │   │   │   └── module/
│   │   │   │   │       ├── AppModule.kt
│   │   │   │   │       └── DatabaseModule.kt
│   │   │   │   ├── presentation/         # UI layer
│   │   │   │   │   ├── navigation/       # Navigation setup
│   │   │   │   │   │   ├── AdygyesNavHost.kt
│   │   │   │   │   │   └── NavDestinations.kt
│   │   │   │   │   ├── theme/            # Material Design 3 theme
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Dimensions.kt
│   │   │   │   │   │   ├── Shapes.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Typography.kt
│   │   │   │   │   ├── ui/               # Screens and components
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── splash/   # Splash screen
│   │   │   │   │   │   │   │   └── SplashScreen.kt           # App launch screen with logo
│   │   │   │   │   │   │   ├── map/      # Map screen with dual-layer markers
│   │   │   │   │   │   │   │   ├── MapScreen.kt              # 🎬 Main map screen with cinema-quality search animations
│   │   │   │   │   │   │   │   ├── MapHost.kt                # Map container with persistent MapView
│   │   │   │   │   │   │   │   ├── MapScreenTablet.kt        # Tablet version
│   │   │   │   │   │   │   │   ├── ClusteringAlgorithm.kt    # Marker clustering logic
│   │   │   │   │   │   │   │   ├── ClusterMarker.kt          # Cluster visualization
│   │   │   │   │   │   │   │   ├── GeoObjectProvider.kt      # Geo objects support
│   │   │   │   │   │   │   │   ├── MapStyleProvider.kt       # Map styling
│   │   │   │   │   │   │   │   ├── WaypointMarkerProvider.kt # Waypoint markers
│   │   │   │   │   │   │   │   └── markers/                  # Dual-layer marker system
│   │   │   │   │   │   │   │       ├── DualLayerMarkerSystem.kt    # Main orchestrator
│   │   │   │   │   │   │   │       ├── VisualMarkerProvider.kt     # Native visual markers
│   │   │   │   │   │   │   │       ├── VisualMarkerRegistry.kt     # Marker persistence
│   │   │   │   │   │   │   │       ├── CircularImageMarker.kt      # Compose click overlay
│   │   │   │   │   │   │   │       ├── MarkerOverlay.kt            # Positioning system
│   │   │   │   │   │   │   │       ├── MapCoordinateConverter.kt   # Coordinate conversion
│   │   │   │   │   │   │   │       └── MarkerState.kt              # State management
│   │   │   │   │   │   │   ├── detail/   # Attraction details
│   │   │   │   │   │   │   │   └── AttractionDetailScreen.kt
│   │   │   │   │   │   │   ├── favorites/ # Favorites management
│   │   │   │   │   │   │   │   └── FavoritesScreen.kt
│   │   │   │   │   │   │   ├── search/   # Search functionality
│   │   │   │   │   │   │   │   └── SearchScreen.kt
│   │   │   │   │   │   │   ├── settings/ # App settings
│   │   │   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   │   │   └── SettingsComponents.kt    # Settings UI components
│   │   │   │   │   │   │   ├── onboarding/ # First launch
│   │   │   │   │   │   │   │   └── OnboardingScreen.kt
│   │   │   │   │   │   │   └── developer/ # Developer mode (stubs)
│   │   │   │   │   │   │       ├── DeveloperScreen.kt        # Stub file
│   │   │   │   │   │   │       ├── DeveloperViewModel.kt     # Stub file
│   │   │   │   │   │   │       └── AttractionEditorScreen.kt # Stub file
│   │   │   │   │   │   └── components/   # Reusable UI components
│   │   │   │   │   │       ├── AccessibilityHelper.kt
│   │   │   │   │   │       ├── AdygyesBottomNavigation.kt    # Bottom navigation bar
│   │   │   │   │   │       ├── AttractionBottomSheet.kt      # Attraction detail sheet
│   │   │   │   │   │       ├── AttractionCard.kt             # Attraction card component
│   │   │   │   │   │       ├── AttractionsList.kt            # List/Grid view with sorting
│   │   │   │   │   │       ├── CategoryCarousel.kt           # Horizontal category scroll
│   │   │   │   │   │       ├── CategoryChip.kt               # Category chip component
│   │   │   │   │   │       ├── CategoryFilterBottomSheet.kt  # Category filter UI
│   │   │   │   │   │       ├── EmptyState.kt
│   │   │   │   │   │       ├── HapticFeedback.kt
│   │   │   │   │   │       ├── LoadingShimmer.kt
│   │   │   │   │   │       ├── PhotoGallery.kt
│   │   │   │   │   │       ├── RatingBar.kt
│   │   │   │   │   │       └── SearchBar.kt
│   │   │   │   │   └── viewmodel/        # ViewModels
│   │   │   │   │       ├── AttractionDetailViewModel.kt
│   │   │   │   │       ├── DeveloperViewModel.kt  # Stub file
│   │   │   │   │       ├── FavoritesViewModel.kt
│   │   │   │   │       ├── ImageCacheViewModel.kt # ⭐ NEW: Image cache management
│   │   │   │   │       ├── LocaleViewModel.kt    # Language switching
│   │   │   │   │       ├── MapViewModel.kt                 # Map state and filtering
│   │   │   │   │       ├── MapPreloadViewModel.kt          # Map preloading support
│   │   │   │   │       ├── MapStateViewModel.kt            # Camera state persistence
│   │   │   │   │       ├── SearchViewModel.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       └── ThemeViewModel.kt              # Theme management
│   │   │   │   │   └── util/               # Utilities
│   │   │   │   │       └── MapPreloadManager.kt          # Map preloading utilities
│   │   │   │   ├── AdygyesApplication.kt  # Application class
│   │   │   │   └── MainActivity.kt        # Main activity (renders MapHost { AdygyesNavHost(...) })
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       ├── values-en/            # English translations
│   │   │       ├── raw/            # JSON data files
│   │   │       └── drawable/       # Icons and images
│   │   ├── androidTest/            # Instrumented tests
│   │   └── test/                   # Unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml          # Version catalog
├── Docs/                           # Documentation
│   ├── Implementation_Plan.md      # Development roadmap (Stage 10 in progress)
│   ├── AppMap_adygyes.md          # App flow and UI structure
│   ├── project_structure.md       # This file
│   ├── PRD_adygyes.md            # Product requirements
│   ├── Bug_tracking.md           # Known issues and fixes
│   ├── UI_UX_doc.md             # Design specifications
│   ├── Technical_Specs.md        # Technical specifications
│   ├── DATA_VERSIONING_GUIDE.md  # Data versioning system
│   ├── IMAGE_CACHING_SYSTEM.md   # Image caching documentation
│   └── markers_update/            # Marker system documentation
│       ├── DUAL_LAYER_FINAL_SUCCESS.md
│       ├── MARKER_COMPONENTS_SPEC.md
│       └── MAP_MARKER_REDESIGN_PLAN.md
├── build.gradle.kts               # Project build configuration
├── settings.gradle.kts            # Project settings
├── gradle.properties             # Gradle properties
├── API_SETUP.md                  # API configuration guide
└── README.md                     # Project overview
```

## Key Architecture Patterns

### 🏗️ **Clean Architecture Implementation**
- **Domain Layer**: Business logic and entities
- **Data Layer**: Repository pattern with local/remote data sources
- **Presentation Layer**: MVVM with Compose UI

### 🎯 **Key Features Implemented**

#### 🚧 **Stage 10 In Progress - Quality Assurance & Optimization (91% Complete):**
- **UI/UX Review**: Comprehensive review of all screens and interactions
- **Performance Optimization**: Map performance and memory usage improvements
- **Image Caching**: Advanced caching system with lazy loading
- **Testing**: Unit tests, UI tests, integration tests in development
- **Firebase Integration**: Crashlytics and analytics planned
- **Bug Fixes**: Addressing all identified issues

#### ✅ **Stage 9 Completed - Advanced Map Features:**
- **Revolutionary Architecture** - Native visual + Compose interactive layers
- **100% Click Reliability** - Perfect marker tap handling with transparent overlay
- **Zero Visual Lag** - Native MapKit rendering with hardware acceleration
- **Full Map Interactivity** - Preserved pan, zoom, rotate functionality
- **Production Ready** - Optimized performance with minimal overhead
- **Bottom navigation** - Map/List toggle, Favorites, Settings
- **Persistent MapHost** - Single `MapView` at app root, `NavHost` rendered inside `MapHost`
- **Camera state persistence** - `MapStateViewModel` + `PreferencesManager.cameraStateFlow`
- **Marker persistence** - `VisualMarkerRegistry` + incremental updates in `VisualMarkerProvider`
- **Real-time search** - Debounced search with instant filtering
- **Category filtering** - Bottom sheet with category selection

#### 🗺️ **Map Features:**
- **Yandex MapKit v4.8.0** - Full interactive map integration
- **Location Services** - GPS positioning with permission handling
- **Dual-Layer Markers** - Native visual markers with Compose overlay for clicks
- **Dynamic Clustering** - Automatic grouping with ClusteringAlgorithm
- **Circular Image Markers** - Attraction photos with fallback to transparent
- **Geo-objects Support** - Polygons and polylines for parks/trails
- **Map Styles** - Light/Dark theme support with MapStyleProvider

#### 📱 **UI Components:**
- **Material Design 3** - Complete theme system with Typography, Colors, Shapes
- **Responsive Design** - Phone and tablet layouts
- **Bottom Navigation** - Map, List, Favorites, Settings tabs
- **Search Bar** - Real-time search with suggestions
- **Category Carousel** - Horizontal scrolling category filter
- **Photo Gallery** - Swipeable gallery with zoom support
- **Loading Shimmers** - Skeleton loading animations
- **Empty States** - Contextual empty state messages
- **Haptic Feedback** - Touch feedback for interactions

#### 💾 **Data Management:**
- **Room Database** - Local persistence with migrations support
- **DataStore Preferences** - User settings and preferences
- **JSON Assets** - 10+ real Adygea attractions with full details
- **Image Caching** - Coil-based caching with version invalidation
- **Data Versioning** - Automatic updates when JSON version changes
- **Offline Support** - Full offline functionality
- **Repository Pattern** - Clean separation of data sources

## Development Guidelines

### 📋 **Code Organization**
- Each screen has its own package under `ui/screens/`
- Reusable components in `ui/components/`
- ViewModels follow MVVM pattern with StateFlow
- Use cases encapsulate business logic

### 🔧 **Key Dependencies**
- **Jetpack Compose** - UI toolkit (BOM 2024.12.01)
- **Hilt** - Dependency injection (2.52)
- **Room** - Local database (2.6.1)
- **Yandex MapKit** - Map functionality (4.8.0-full)
- **Coil** - Image loading and caching (2.7.0)
- **Accompanist** - Permissions and utilities
- **Kotlinx Serialization** - JSON parsing (1.7.3)
- **DataStore** - Preferences storage
- **Timber** - Logging

### 🎨 **UI Standards**
- Material Design 3 components
- Consistent spacing using Dimensions.kt
- Dark/Light theme support
- ✅ **Полная локализация на русский язык** - весь интерфейс переведен и адаптирован
- English localization (архитектура готова для будущего расширения)

## Recent Major Updates

### ✅ **Stage 9 - Advanced Map Features:**
- Implemented dual-layer marker system for 100% click reliability
- Added dynamic marker clustering with zoom-based grouping
- Integrated bottom navigation with Map/List toggle
- Added CategoryCarousel for quick filtering
- Implemented favorites integration in main map screen

### ✅ **Stage 8 - Navigation & UI Enhancement:**
- Unified multiple MapScreen implementations into single version
- Added bottom navigation bar with tabs
- Implemented category filtering with bottom sheet
- Enhanced search with real-time suggestions

### ✅ **Data Architecture Simplification:**
- Removed Developer Mode completely
- Simplified to single JSON data source (assets/attractions.json)
- Implemented automatic data versioning system
- Added comprehensive image caching with Coil

## Architecture Pattern: MVVM + Clean Architecture

### Layers:
1. **Presentation Layer** (UI)
   - Compose UI screens
   - ViewModels
   - Navigation
   - Theme

2. **Domain Layer** (Business Logic)
   - Use cases
   - Domain models
   - Repository interfaces

3. **Data Layer** (Data Management)
   - Repository implementations
   - Local data sources (Room)
   - Remote data sources (Retrofit)
   - Data mappers

4. **Core Layer** (Utilities)
   - Extensions
   - Constants
   - Utilities
   - Base classes

## Package Naming Convention
- Base package: `com.adygyes.app`
- Features grouped by layer, then by feature
- Use lowercase with underscores for resource files

## File Naming Conventions
- **Kotlin Files:** PascalCase (e.g., `MapViewModel.kt`)
- **Compose Screens:** PascalCase + "Screen" (e.g., `MapScreen.kt`)
- **Compose Components:** PascalCase (e.g., `AttractionCard.kt`)
- **XML Resources:** lowercase_with_underscores (e.g., `ic_marker.xml`)
- **Test Files:** ClassName + "Test" (e.g., `MapViewModelTest.kt`)

## Gradle Configuration Structure
- Use Gradle Kotlin DSL (`.kts` files)
- Version catalog for dependency management (`gradle/libs.versions.toml`)
- Centralized repositories in `settings.gradle.kts` using `dependencyResolutionManagement` (project build files must not declare repositories)
- Build variants for different app flavors
- Separate debug and release configurations

Example repository configuration in `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.google.com/") }
        // Yandex repository will be added at Stage 2 when MapKit is integrated
    }
}
```

## Git Structure
- Main branch: `main`
- Development branch: `develop`
- Feature branches: `feature/feature-name`
- Bugfix branches: `bugfix/issue-description`
- Release branches: `release/version-number`

## Build Variants
- **debug:** Development build with debugging enabled
- **release:** Production build with ProGuard/R8
- **full:** Full version with Yandex MapKit Full
- **lite:** Lite version with Yandex MapKit Lite

## Dependency Management Rules
- All versions defined in `libs.versions.toml`
- Group related dependencies
- Use BOM where available (Compose, Firebase)
- Keep dependencies up to date

## Resource Organization
- Strings: Centralized in `strings.xml` (app display name: `<string name="app_name">AdygGIS</string>`)
- Colors: Defined in `colors.xml`, referenced in theme
- Dimensions: Use Material Design spacing
- Drawables: Vector drawables preferred
- App icon: Adaptive icon configured via `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`.
  - Foreground: `res/drawable/ic_launcher_foreground.xml` (gold compass, VectorDrawable; uses only `<path>` elements for compatibility)
  - Background: `res/drawable/ic_launcher_background.xml` (green gradient)

## Testing Structure
- Unit tests mirror source structure
- Integration tests in `androidTest`
- Use MockK for mocking
- Compose UI tests for screens

## Code Style Guidelines
- Follow Kotlin coding conventions
- Maximum line length: 120 characters
- Use meaningful variable/function names
- Document public APIs
- Use data classes for models
- Prefer immutable data structures

## Important Files Locations
- API Keys: `local.properties` (never commit)
- ProGuard Rules: `app/proguard-rules.pro`
- Version Catalog: `gradle/libs.versions.toml`
- Application Class: `app/src/main/java/com/adygyes/app/AdygyesApplication.kt`
- Navigation Graph: `presentation/navigation/NavGraph.kt`

## 🖼️ Image Caching System

### Architecture Overview:
The app now features a sophisticated image caching system that optimizes performance and reduces network usage:

#### Components:
1. **ImageCacheManager** (`data/local/cache/ImageCacheManager.kt`)
   - Manages Coil ImageLoader with optimized cache settings
   - Memory cache: 25% of available app memory
   - Disk cache: Up to 250MB persistent storage
   - Version-based cache invalidation

2. **ImageCacheViewModel** (`presentation/viewmodel/ImageCacheViewModel.kt`)
   - Provides ImageLoader instance to UI components
   - Manages cache statistics and monitoring
   - Handles preloading of first attraction images

#### Key Features:
- **Smart Preloading**: First image of each attraction preloaded on app start
- **Lazy Loading**: Additional gallery images loaded on-demand
- **Version Sync**: Cache automatically cleared when attractions.json version changes
- **Hardware Bitmap Fix**: Resolved Canvas compatibility for map markers with `.allowHardware(false)`

#### Integration Points:
- **Map Markers**: VisualMarkerProvider uses cached images for circular markers
- **Photo Gallery**: PhotoGallery component with lazy loading and cache policies
- **Attraction Cards**: All attraction images benefit from caching
- **Repository**: AttractionRepositoryImpl integrates with cache versioning

## Changelog
- 2025-09-27: **Search Field Animation Enhancement** 🎬 — Implemented cinema-quality search field animations in MapScreen.kt:
  - Replaced `Crossfade` with `AnimatedContent` + `SizeTransform` for smooth expansion
  - Added spring-based animations (`DampingRatioLowBouncy`, `StiffnessVeryLow`) for organic movement
  - Implemented Cubic Bezier easing curves for professional Material Design feel
  - Fixed mode-specific logic: `EnhancedSearchTextField` (List mode) vs `UnifiedSearchTextField` (Map mode)
  - Sequential button animations with staggered delays (200ms/250ms) for elegant appearance
  - Enhanced scale effects (0.7f ↔ 1.0f) and optimized timing (450ms fade-in, 200ms fade-out)
- 2025-09-26: **Favorites Integration** — Integrated favorites functionality into MapScreen with CategoryCarousel, List/Grid toggle, and sorting. Enhanced AttractionsList with compact card mode matching FavoritesScreen design.
- 2025-09-26: **Branding Update** — App display name changed to "AdygGIS" (no internal package rename). Adaptive icon updated (green gradient background + gold compass foreground). `AndroidManifest.xml` `android:label` set to `AdygGIS`; `values/strings.xml` and `values-en/strings.xml` updated accordingly.
- 2025-09-26: **Marker Visuals Update** — Removed colored background and emoji fallback for markers without photos. Default fallback is now fully transparent with a white border and shadow until an image loads. Updated `AppMap_adygyes.md`, `Implementation_Plan.md`, and `IMAGE_CACHING_SYSTEM.md` accordingly.
- 2025-09-25: **MAJOR UPDATE** - Added ImageCacheManager system with version-based invalidation, fixed hardware bitmap issues in map markers, integrated lazy loading in PhotoGallery
- 2025-09-25: Documentation update - Simplified JsonFileManager, removed Developer Mode files (replaced with stubs), added LocaleViewModel for language switching
- 2025-09-24: Stage 9 Complete - Dual-Layer Marker System with DualLayerMarkerSystem, VisualMarkerProvider, and transparent overlay
- 2025-09-22: Centralized repositories in `settings.gradle.kts`; Gradle wrapper updated to 8.13; AGP aligned to 8.7.3.

## Commands Reference
```bash
# Build debug variant
./gradlew assembleDebug

# Build release variant
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Generate dependency updates report
./gradlew dependencyUpdates
```
