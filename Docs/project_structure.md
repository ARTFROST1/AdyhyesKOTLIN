# Project Structure Guide

## Project Directory Layout

```
AdyhyesKOTLIN/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/adygyes/app/
│   │   │   │   ├── core/           # Core utilities and extensions
│   │   │   │   │   ├── extensions/
│   │   │   │   │   ├── utils/
│   │   │   │   │   └── constants/
│   │   │   │   ├── data/           # Data layer
│   │   │   │   │   ├── local/      # Room DB, DAOs, local data sources
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── entities/
│   │   │   │   │   ├── remote/     # API services, DTOs
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   └── dto/
│   │   │   │   │   ├── repository/ # Repository implementations
│   │   │   │   │   └── mapper/     # Data mappers
│   │   │   │   ├── domain/         # Business logic
│   │   │   │   │   ├── model/      # Domain models
│   │   │   │   │   ├── repository/ # Repository interfaces
│   │   │   │   │   └── usecase/    # Use cases
│   │   │   │   ├── di/             # Dependency injection
│   │   │   │   │   ├── module/
│   │   │   │   │   └── qualifier/
│   │   │   │   ├── presentation/   # UI layer
│   │   │   │   │   ├── navigation/ # Navigation setup
│   │   │   │   │   ├── theme/      # Theme, colors, typography
│   │   │   │   │   ├── ui/         # Screens and components
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── map/
│   │   │   │   │   │   │   ├── detail/
│   │   │   │   │   │   │   ├── favorites/
│   │   │   │   │   │   │   ├── search/
│   │   │   │   │   │   │   └── settings/
│   │   │   │   │   │   └── components/
│   │   │   │   │   └── viewmodel/  # ViewModels
│   │   │   │   └── AdygyesApplication.kt  # Application class
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       ├── values-en/      # English translations
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

## Changelog
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
