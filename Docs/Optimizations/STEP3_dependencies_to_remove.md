# Этап 3: Удаление неиспользуемых зависимостей

## ❌ УДАЛИТЬ из app/build.gradle.kts:

### 1. Сетевые библиотеки (строки 167-171) - НЕ ИСПОЛЬЗУЮТСЯ
```kotlin
// УДАЛИТЬ:
implementation(libs.retrofit.core)                    // ~450 KB
implementation(libs.retrofit.kotlinx.serialization)   // ~100 KB
implementation(libs.okhttp)                           // ~800 KB
implementation(libs.okhttp.logging)                   // ~50 KB
```
**Экономия: ~1.4 MB** ⭐ Крупнейшая находка!

### 2. Google Fonts (строка 152) - НЕ ИСПОЛЬЗУЮТСЯ
```kotlin
// УДАЛИТЬ:
implementation("androidx.compose.ui:ui-text-google-fonts")  // ~50 KB
```

### 3. Accompanist Pager (строка 157) - НЕ ИСПОЛЬЗУЕТСЯ
```kotlin
// УДАЛИТЬ:
implementation(libs.accompanist.pager)                // ~100 KB
```

### 4. Coil SVG (строка 182) - SVG не используются
```kotlin
// УДАЛИТЬ:
implementation(libs.coil.svg)                         // ~150 KB
```

---

## ✅ ОСТАВИТЬ (используются):

```kotlin
// ✅ Accompanist (используются)
implementation(libs.accompanist.permissions)          // MapScreen
implementation(libs.accompanist.systemuicontroller)   // System bars

// ✅ Serialization (используется для JSON)
implementation(libs.kotlinx.serialization.json)       // attractions.json

// ✅ Coil (загрузка изображений)
implementation(libs.coil.compose)                     // Основной Coil
implementation(libs.compose.zoomable)                 // Зум фото
```

---

## 📝 Инструкция по удалению:

1. Открыть `app/build.gradle.kts`
2. Найти раздел `dependencies {` (строка 135)
3. Удалить строки:
   - 152 (Google Fonts)
   - 157 (Accompanist Pager)
   - 167-171 (Retrofit + OkHttp, 5 строк)
   - 182 (Coil SVG)
4. Сохранить файл
5. Sync Gradle

---

## 💰 Общая экономия: ~1.7 MB

### Детализация:
- Retrofit core: ~450 KB
- Retrofit serialization: ~100 KB
- OkHttp: ~800 KB
- OkHttp logging: ~50 KB
- Accompanist Pager: ~100 KB
- Coil SVG: ~150 KB
- Google Fonts: ~50 KB

**ИТОГО: ~1.7 MB**
