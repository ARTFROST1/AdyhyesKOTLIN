# Bug Tracking Document

## Overview
This document tracks all bugs, errors, and their resolutions during the development of Adygyes application.

## Bug Format Template
```markdown
### BUG-[NUMBER]: [Brief Description]
**Date:** [YYYY-MM-DD]
**Stage:** [Implementation Stage]
**Severity:** [Critical/High/Medium/Low]
**Status:** [Open/In Progress/Resolved/Closed]

**Description:**
[Detailed description of the bug]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Error Message/Stack Trace:**
```
[Error details]
```

**Root Cause:**
[Analysis of why the bug occurred]

**Solution:**
[How the bug was fixed]

**Prevention:**
[Steps to prevent similar issues]

**Related Files:**
- [File path 1]
- [File path 2]
```

---

## Active Bugs

### BUG-003: Gradle Version Compatibility Issue
**Date:** 2024-12-22
**Stage:** Stage 1 - Foundation & Setup
**Severity:** High
**Status:** Resolved

**Description:**
Gradle build fails due to version compatibility issue. Android Gradle Plugin (AGP) version 8.11.1 requires minimum Gradle version 8.13, but current configuration may have mismatched versions.

**Steps to Reproduce:**
1. Run Gradle sync or build
2. Error appears about minimum supported Gradle version

**Expected Behavior:**
Gradle and AGP versions should be compatible and build should succeed.

**Actual Behavior:**
Build fails with message: "Minimum supported Gradle version is 8.13. Current version is 8.11.1."

**Error Message/Stack Trace:**
```
Minimum supported Gradle version is 8.13. Current version is 8.11.1.
Try updating the 'distributionUrl' property in gradle-wrapper.properties to 'gradle-8.13-bin.zip'.
```

**Root Cause:**
AGP version 8.11.1 in libs.versions.toml requires Gradle 8.13+, but there may be a version mismatch or caching issue.

**Solution:**
1. Verified Gradle wrapper is set to 8.13 (already correct)
2. Updated AGP version from 8.11.1 to 8.7.3 (8.11.1 was invalid version)
3. AGP 8.7.3 is compatible with Gradle 8.13

**Resolution Details:**
- Changed `agp = "8.11.1"` to `agp = "8.7.3"` in gradle/libs.versions.toml
- AGP 8.11.1 was not a valid release version
- AGP 8.7.3 is tested and compatible with Gradle 8.13

**Prevention:**
- Always check AGP-Gradle compatibility matrix before updating versions
- Use compatible version combinations from official documentation

**Related Files:**
- gradle/wrapper/gradle-wrapper.properties
- gradle/libs.versions.toml (agp version)

---

### BUG-002: Gradle Repository Configuration Conflict
**Date:** 2024-12-22
**Stage:** Stage 1 - Foundation & Setup
**Severity:** Critical
**Status:** Resolved

**Description:**
Build fails with InvalidUserCodeException due to repository configuration conflict between settings.gradle.kts and build.gradle.kts.

**Steps to Reproduce:**
1. Run `./gradlew build` or sync project in IDE
2. Build fails at line 16 in build.gradle.kts

**Expected Behavior:**
Gradle build should complete successfully without repository conflicts.

**Actual Behavior:**
Build fails with error: "Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'build.gradle.kts'"

**Error Message/Stack Trace:**
```
org.gradle.api.InvalidUserCodeException: Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'build.gradle.kts'
```

**Root Cause:**
settings.gradle.kts has `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` which prevents repositories from being declared in project-level build files, but build.gradle.kts contains an `allprojects` block with repository declarations.

**Solution:**
Move all repository declarations from build.gradle.kts to settings.gradle.kts in the dependencyResolutionManagement block.

**Prevention:**
- Always check settings.gradle.kts repository mode before adding repositories to project files
- Use centralized repository management in settings.gradle.kts for modern Gradle projects

**Related Files:**
- build.gradle.kts (line 14-22)
- settings.gradle.kts (line 14-19)

---

### BUG-001: Example Bug Entry
**Date:** 2024-12-22
**Stage:** Stage 1 - Foundation & Setup
**Severity:** Low
**Status:** Open

**Description:**
This is an example bug entry to demonstrate the format.

**Steps to Reproduce:**
1. Open Bug_tracking.md
2. Read the example entry
3. Use as template for real bugs

**Expected Behavior:**
Developers understand bug tracking format

**Actual Behavior:**
Template is available for use

**Error Message/Stack Trace:**
```
N/A - Example entry
```

**Root Cause:**
Need for standardized bug tracking

**Solution:**
Created template and example

**Prevention:**
Use this template for all bug reports

**Related Files:**
- Docs/Bug_tracking.md

---

## Resolved Bugs

[Resolved bugs will be moved here with resolution details]

---

## Known Issues

### Known Issue 001: Gradle Sync
**Description:** Initial Gradle sync may fail if Maven repositories are not configured properly for Yandex MapKit.

**Workaround:**
1. Ensure `maven { url = uri("https://maven.google.com") }` is in repositories
2. Ensure `maven { url = uri("https://repo1.maven.org/maven2/") }` is in repositories
3. Add Yandex MapKit repository when implementing maps

**Permanent Fix:** Will be addressed in Stage 2 when implementing maps.

---

## Common Issues and Solutions

### Gradle Build Issues
- **Issue:** Dependency resolution failures
- **Solution:** Check internet connection, clear Gradle cache: `./gradlew clean --refresh-dependencies`

### Compose Preview Issues
- **Issue:** Preview not showing in Android Studio
- **Solution:** Invalidate caches and restart: File → Invalidate Caches → Invalidate and Restart

### Room Database Issues
- **Issue:** Schema export directory not set
- **Solution:** Add to build.gradle: `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`

### Hilt Issues
- **Issue:** Missing @HiltAndroidApp annotation
- **Solution:** Add annotation to Application class

### Navigation Issues
- **Issue:** NavController not found in Composable
- **Solution:** Pass NavController as parameter or use CompositionLocal

---

## Bug Statistics

### By Severity
- Critical: 0
- High: 0
- Medium: 0
- Low: 1 (example)

### By Stage
- Stage 1: 1 (example)
- Stage 2: 0
- Stage 3: 0
- Stage 4: 0
- Stage 5: 0
- Stage 6: 0
- Stage 7: 0
- Stage 8: 0

### Resolution Time
- Average: N/A
- Fastest: N/A
- Slowest: N/A

---

## Notes
- Update this document immediately when encountering bugs
- Move resolved bugs to the Resolved section with complete solution details
- Review known issues before starting new development tasks
- Use bug numbers for reference in commit messages

---

## BUG-009: Dependency Version Compatibility Issues
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** High
**Component:** Build System

### Issue:
Build failed with AAR metadata check errors - newer AndroidX libraries (core-ktx 1.17.0, activity-compose 1.11.0) required Android SDK 36 and AGP 8.9.1, but project used SDK 35 and AGP 8.7.3.

### Error Messages:
```
Dependency 'androidx.core:core:1.17.0' requires libraries and applications that depend on it to compile against version 36 or later
Dependency 'androidx.activity:activity-compose:1.11.0' requires Android Gradle plugin 8.9.1 or higher
```

### Root Cause:
Version mismatch between AndroidX libraries and Android build tools.

### Solution:
Downgraded AndroidX libraries to compatible versions:
- androidx.core:core-ktx from 1.17.0 to 1.13.1
- androidx.activity:activity-compose from 1.11.0 to 1.9.3

Modified files:
- `/gradle/libs.versions.toml`

---

## BUG-010: MapScreenWithYandex Compilation Errors
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** High
**Component:** Map Feature

### Issue:
Multiple compilation errors in MapScreenWithYandex.kt:
1. Try-catch not supported around composable function invocations
2. MapObjectVisitor type mismatch
3. PointF unresolved references
4. userData property access issues

### Error Messages:
```
Try catch is not supported around composable function invocations
Unresolved reference 'PointF'
Argument type mismatch: actual type is 'kotlin.Function1', but 'MapObjectVisitor' was expected
```

### Root Cause:
1. Incorrect error handling in Compose LaunchedEffect
2. Missing imports and wrong API usage for Yandex MapKit

### Solution:
1. Removed try-catch from composable context
2. Added correct imports:
   - `import com.yandex.mapkit.map.MapObjectVisitor`
   - `import com.yandex.mapkit.geometry.PointF`
3. Implemented proper MapObjectVisitor interface for traversing map objects
4. Fixed all PointF references to use correct import

Modified files:
- `/app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapScreenWithYandex.kt`
- `/app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapStyleProvider.kt`

---

## BUG-011: MainActivity NavHost Parameters Missing
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** Medium
**Component:** Navigation

### Issue:
AdygyesNavHost called without required parameters (navController, paddingValues).

### Error Messages:
```
No value passed for parameter 'navController'
No value passed for parameter 'paddingValues'
```

### Root Cause:
MainActivity was not providing required navigation parameters to AdygyesNavHost composable.

### Solution:
Created AdygyesApp composable with Scaffold that provides:
- NavController via rememberNavController()
- PaddingValues from Scaffold's innerPadding

Modified files:
- `/app/src/main/java/com/adygyes/app/MainActivity.kt`

---

## BUG-012: Additional MapScreenWithYandex Issues
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** High
**Component:** Map Feature

### Issue:
Additional compilation errors after initial fixes:
1. PointF class still unresolved in multiple locations
2. @Composable function called from non-composable context (MapStyleProvider.applyMapStyle)

### Error Messages:
```
Unresolved reference 'PointF'
@Composable invocations can only happen from the context of a @Composable function
```

### Root Cause:
1. PointF class doesn't exist in com.yandex.mapkit.geometry package
2. MapStyleProvider.applyMapStyle was marked as @Composable but called from LaunchedEffect

### Solution:
1. Removed PointF usage entirely - IconStyle anchor property is optional
2. Removed @Composable annotation from MapStyleProvider.applyMapStyle function
3. Simplified IconStyle configuration to only use scale property

Modified files:
- `/app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapScreenWithYandex.kt`
- `/app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapStyleProvider.kt`

## BUG-013: DataSyncManager SyncState Type Mismatch
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** High
**Component:** Data Sync

### Issue:
Build failed with Kotlin compilation errors in DataSyncManager.kt due to type mismatch when assigning SyncState enum values to MutableStateFlow.

### Error Messages:
```
Assignment type mismatch: actual type is 'com.adygyes.app.data.sync.DataSyncManager.SyncState.SYNCING', but 'com.adygyes.app.data.sync.DataSyncManager.SyncState.IDLE' was expected.
Assignment type mismatch: actual type is 'com.adygyes.app.data.sync.DataSyncManager.SyncState.SUCCESS', but 'com.adygyes.app.data.sync.DataSyncState.IDLE' was expected.
Assignment type mismatch: actual type is 'com.adygyes.app.data.sync.DataSyncManager.SyncState.ERROR', but 'com.adygyes.app.data.sync.DataSyncManager.SyncState.IDLE' was expected.
```

### Root Cause:
The SyncState sealed class was defined with `object` declarations instead of `data object`, which caused type inference issues with StateFlow in modern Kotlin versions.

### Solution:
1. Changed SyncState sealed class objects to use `data object` instead of `object`:
```kotlin
sealed class SyncState {
    data object IDLE : SyncState()
    data object SYNCING : SyncState()
    data object SUCCESS : SyncState()
    data class ERROR(val message: String) : SyncState()
}
```

2. Added explicit type parameter to MutableStateFlow to resolve type inference:
```kotlin
private val _syncState = MutableStateFlow<SyncState>(SyncState.IDLE)
```

### Prevention:
- Use `data object` for sealed class objects in modern Kotlin
- Test compilation after implementing sealed classes with StateFlow

### Modified Files:
- `/app/src/main/java/com/adygyes/app/data/sync/DataSyncManager.kt`

## BUG-014: AttractionMapper JVM Signature Conflicts
**Date:** 2025-09-22
**Status:** ✅ Fixed
**Severity:** High
**Component:** Data Mapping

### Issue:
Build failed with JVM signature conflicts in AttractionMapper.kt due to duplicate extension function names that have the same signature after type erasure.

### Error Messages:
```
Platform declaration clash: The following declarations have the same JVM signature (toDomainModels(Ljava/util/List;)Ljava/util/List;):
    fun List<AttractionEntity>.toDomainModels(): List<Attraction>
    fun List<AttractionDto>.toDomainModels(): List<Attraction>

Platform declaration clash: The following declarations have the same JVM signature (toEntities(Ljava/util/List;)Ljava/util/List;):
    fun List<AttractionDto>.toEntities(): List<AttractionEntity>
    fun List<Attraction>.toEntities(): List<AttractionEntity>
```

### Root Cause:
Extension functions with the same name on different generic types (`List<AttractionEntity>` vs `List<AttractionDto>`) result in identical JVM signatures after type erasure, causing compilation conflicts.

### Solution:
Renamed conflicting extension functions to have unique names:
1. `List<AttractionDto>.toDomainModels()` → `List<AttractionDto>.toDomainModelsFromDto()`
2. `List<AttractionDto>.toEntities()` → `List<AttractionDto>.toEntitiesFromDto()`

Updated usage in AttractionRepositoryImpl.kt to use the new function names.

### Prevention:
- Avoid extension functions with identical names on different generic types
- Use descriptive names that indicate the source type when dealing with similar conversions
- Test compilation after adding new mapper functions

### Modified Files:
- `/app/src/main/java/com/adygyes/app/data/mapper/AttractionMapper.kt`
- `/app/src/main/java/com/adygyes/app/data/repository/AttractionRepositoryImpl.kt`

---

*Last Updated: 2025-09-22*
*Version: 1.0.0*
