# Project Structure Guide

**Last Updated:** 2025-09-26  
**Current Version:** Stage 9 + Persistent MapHost & Camera/Marker Persistence

## 🎯 Key Architecture Updates:
- **Simplified Data Management:** JsonFileManager now only reads from assets/attractions.json
- **Developer Mode Removed:** DeveloperScreen, DeveloperViewModel, AttractionEditorScreen replaced with stubs
- **New LocaleViewModel:** Added for runtime language switching support
- **Preserved MapScreenReliable:** Kept as backup map implementation with reliable tap handling
- **✨ NEW: ImageCacheManager:** Advanced image caching system with version-based cache invalidation
- **✨ NEW: Hardware Bitmap Fix:** Resolved Canvas compatibility issues for map markers

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
│   │   │   │   │   │   │   ├── map/      # Map screen with dual-layer markers
│   │   │   │   │   │   │   │   ├── MapScreen.kt              # ⭐ MAIN UNIFIED OVERLAY SCREEN (over persistent MapHost)
│   │   │   │   │   │   │   │   ├── MapScreenTablet.kt        # Tablet version
│   │   │   │   │   │   │   │   ├── CategoryMarkerProvider.kt
│   │   │   │   │   │   │   │   ├── GeoObjectProvider.kt
│   │   │   │   │   │   │   │   ├── MapStyleProvider.kt
│   │   │   │   │   │   │   │   ├── TextImageProvider.kt
│   │   │   │   │   │   │   │   ├── WaypointMarkerProvider.kt
│   │   │   │   │   │   │   │   └── markers/                  # ⭐ DUAL-LAYER SYSTEM
│   │   │   │   │   │   │   │       ├── DualLayerMarkerSystem.kt    # Main orchestrator (uses registry, incremental sync)
│   │   │   │   │   │   │   │       ├── VisualMarkerProvider.kt     # Native markers (incremental updates)
│   │   │   │   │   │   │   │       ├── VisualMarkerRegistry.kt     # ⭐ NEW: Persist provider across navigation
│   │   │   │   │   │   │   │       ├── CircularImageMarker.kt      # Compose markers
│   │   │   │   │   │   │   │       ├── MarkerOverlay.kt            # Positioning system
│   │   │   │   │   │   │   │       ├── MapCoordinateConverter.kt   # Coordinate utils
│   │   │   │   │   │   │   │       └── MarkerState.kt              # State management
│   │   │   │   │   │   │   ├── detail/   # Attraction details
│   │   │   │   │   │   │   │   └── AttractionDetailScreen.kt
│   │   │   │   │   │   │   ├── favorites/ # Favorites management
│   │   │   │   │   │   │   │   └── FavoritesScreen.kt
│   │   │   │   │   │   │   ├── search/   # Search functionality
│   │   │   │   │   │   │   │   └── SearchScreen.kt
│   │   │   │   │   │   │   ├── settings/ # App settings
│   │   │   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   │   │   └── onboarding/ # First launch
│   │   │   │   │   │   │       └── OnboardingScreen.kt
│   │   │   │   │   │   └── components/   # Reusable UI components
│   │   │   │   │   │       ├── AccessibilityHelper.kt
│   │   │   │   │   │       ├── AdygyesBottomNavigation.kt  # ⭐ BOTTOM NAV
│   │   │   │   │   │       ├── AttractionBottomSheet.kt
│   │   │   │   │   │       ├── AttractionCard.kt
│   │   │   │   │   │       ├── AttractionsList.kt
│   │   │   │   │   │       ├── CategoryChip.kt
│   │   │   │   │   │       ├── CategoryFilterBottomSheet.kt # ⭐ NEW
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
│   │   │   │   │       ├── MapViewModel.kt
│   │   │   │   │       ├── MapStateViewModel.kt            # ⭐ NEW: Camera state persistence
│   │   │   │   │       ├── SearchViewModel.kt
│   │   │   │   │       └── SettingsViewModel.kt
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
│   ├── Implementation_Plan.md      # Development roadmap
│   ├── AppMap_adygyes.md          # App flow and UI structure
│   ├── project_structure.md       # This file
│   ├── PRD_adygyes.md            # Product requirements
│   ├── Bug_tracking.md           # Known issues and fixes
│   ├── UI_UX_doc.md             # Design specifications
│   └── Technical_Specs.md        # Technical specifications
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

#### ⭐ **Stage 9 Completed - Dual-Layer Marker System + Persistent Map:**
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
- **Yandex MapKit integration** - Interactive map with clustering
- **Location services** - GPS positioning and permission handling
- **Marker providers** - Category-based colored markers; visual markers use attraction photos, and if no photo is available or loading fails, the marker remains transparent (no colored background, no emoji) with a white border and shadow
- **Geo-objects support** - Polygons and polylines for parks/trails

#### 📱 **UI Components:**
- **Material Design 3** - Modern theming and components
- **Responsive design** - Tablet support with MapScreenTablet.kt
- **Accessibility** - Screen reader and haptic feedback support
- **Animations** - Smooth transitions and loading states

#### 💾 **Data Management:**
- **Room Database** - Local data persistence
- **DataStore** - User preferences storage
- **JSON Assets** - 10 real Adygea attractions data
- **Cache management** - Offline-first architecture

## Development Guidelines

### 📋 **Code Organization**
- Each screen has its own package under `ui/screens/`
- Reusable components in `ui/components/`
- ViewModels follow MVVM pattern with StateFlow
- Use cases encapsulate business logic

### 🔧 **Dependencies**
- **Jetpack Compose** - Modern UI toolkit
- **Hilt** - Dependency injection
- **Room** - Local database
- **Yandex MapKit** - Map functionality
- **Accompanist** - Compose utilities

### 🎨 **UI Standards**
- Material Design 3 components
- Consistent spacing using Dimensions.kt
- Dark/Light theme support
- ✅ **Полная локализация на русский язык** - весь интерфейс переведен и адаптирован
- English localization (архитектура готова для будущего расширения)

## Recent Changes (Stage 8)

### ✅ **MapScreen Unification:**
- Merged 6 different MapScreen files into single unified version
- Removed: MapScreenReliable, MapScreenWithBottomNav, MapScreenEnhanced, etc.
- Kept: MapScreen.kt (main), MapScreenTablet.kt (tablet support)

### ✅ **New Components Added:**
- `CategoryFilterBottomSheet.kt` - Category filtering UI
- Enhanced `AdygyesBottomNavigation.kt` - Bottom navigation bar
- Improved marker tap handling with userData validation

### ✅ **Architecture Improvements:**
- Edge-to-edge display support with WindowInsets
- Proper MapKit lifecycle management
- Reliable state management with debug logging
- Optimized marker updates to prevent unnecessary recreation
│   ├── Implementation_Plan.md
│   ├── TechStack_Complete_Guide.md
│   ├── project_structure.md
│   ├── UI_UX_doc.md
│   └── Bug_tracking.md
├── build.gradle.kts                # Project-level build file
├── settings.gradle.kts
├── gradle.properties
├── local.properties                # Local configuration (not in VCS)
└── README.md
```

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
- Strings: Centralized in `strings.xml`
- Colors: Defined in `colors.xml`, referenced in theme
- Dimensions: Use Material Design spacing
- Drawables: Vector drawables preferred

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
