# 🚀 БЫСТРЫЙ СТАРТ ОПТИМИЗАЦИИ APK

## 📋 Краткая сводка

**Полный план**: `Docs/APK_OPTIMIZATION_PLAN.md`  
**Время выполнения**: ~2 часа  
**Ожидаемая экономия**: 30-50% размера APK

---

## ⚡ ТОП-5 БЫСТРЫХ ПОБЕД (30 минут, ~10-15 MB)

### 1️⃣ Удаление мусорных файлов (5 минут)
```bash
# В корне проекта
rm 1.txt compile_test.kt test_compilation.kt logo.png

# Удаление design_reference
rm -rf app/src/design_reference/
```

### 2️⃣ Удаление пустых Developer Mode файлов (3 минуты)
```bash
rm -rf app/src/main/java/com/adygyes/app/presentation/ui/screens/developer/
rm app/src/main/java/com/adygyes/app/presentation/viewmodel/DeveloperViewModel.kt
```

### 3️⃣ Проверка и удаление неиспользуемых Map файлов (5 минут)
```bash
# Проверить использование
grep -r "MapScreenReliable" app/src/main/java/
grep -r "MapScreenTablet" app/src/main/java/

# Если не используются - удалить
rm app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapScreenReliable.kt
rm app/src/main/java/com/adygyes/app/presentation/ui/screens/map/MapScreenTablet.kt
```

### 4️⃣ Удаление неиспользуемых зависимостей (10 минут)
**Проверить в коде, затем удалить из `app/build.gradle.kts`:**

```kotlin
// Проверить необходимость (если нет HTTP запросов):
// implementation(libs.retrofit.core)
// implementation(libs.retrofit.kotlinx.serialization)
// implementation(libs.okhttp)
// implementation(libs.okhttp.logging)

// Проверить использование:
// implementation(libs.coil.svg)
// implementation(libs.accompanist.pager)
```

### 5️⃣ Оптимизация ProGuard (7 минут)
**Добавить в `app/proguard-rules.pro`:**

```proguard
# Агрессивная оптимизация
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
```

**Удалить из `proguard-rules.pro`:**
```proguard
# Удалить строки 132-140 (Gson - не используется)
```

---

## 🎯 ПОРЯДОК ВЫПОЛНЕНИЯ

### Сейчас:
1. ✅ Прочитал план в `Docs/APK_OPTIMIZATION_PLAN.md`
2. ⬜ **Измерить текущий размер APK** (для сравнения)
3. ⬜ Выполнить **Этап 1** (10 минут)
4. ⬜ Commit изменений

### Далее (по одному этапу):
- ⬜ Этап 2: Анализ неиспользуемого кода (30 мин)
- ⬜ Этап 3: Оптимизация зависимостей (20 мин)
- ⬜ Этап 4: Оптимизация ресурсов (30 мин)
- ⬜ Этап 5: Build конфигурация (15 мин)
- ⬜ Этап 6: Очистка документации (5 мин)
- ⬜ Этап 7: Финальная проверка (20 мин)

---

## 🛡️ БЕЗОПАСНОСТЬ

### Перед началом:
```bash
# Создать ветку для оптимизации
git checkout -b optimization/apk-size

# Или сделать backup
git add .
git commit -m "Checkpoint before optimization"
```

### После каждого этапа:
```bash
# Тест компиляции
./gradlew clean assembleFullRelease

# Если всё ОК - commit
git add .
git commit -m "Optimization: Stage X completed"
```

---

## 📊 КАК ИЗМЕРИТЬ РАЗМЕР APK

### Вариант 1: Через командную строку
```bash
# После сборки
ls -lh app/build/outputs/apk/full/release/*.apk
```

### Вариант 2: Android Studio
1. Build > Build Bundle(s) / APK(s) > Build APK(s)
2. После сборки: Build > Analyze APK...
3. Выбрать `app-full-release.apk`
4. Посмотреть размеры компонентов

---

## ⚠️ ЧТО ТОЧНО НЕ ТРОГАТЬ

- ❌ `keystore.properties` и `keystore/`
- ❌ Yandex MapKit зависимости
- ❌ Room, Hilt, Coroutines
- ❌ Активные экраны и ViewModels
- ❌ Используемые drawable

---

## 📞 ЕСЛИ ЧТО-ТО СЛОМАЛОСЬ

1. Откатить последний commit:
```bash
git reset --hard HEAD~1
```

2. Проверить логи компиляции
3. Восстановить удалённый файл из git:
```bash
git checkout HEAD -- путь/к/файлу
```

---

## ✅ ЧЕКЛИСТ БЫСТРОЙ ПРОВЕРКИ

После оптимизации проверить:
- [ ] Приложение запускается
- [ ] Карта отображается
- [ ] Маркеры загружаются
- [ ] Поиск работает
- [ ] Избранное работает
- [ ] Настройки открываются

---

## 🎉 НАЧИНАЕМ!

**Следующий шаг**: Открой `Docs/APK_OPTIMIZATION_PLAN.md` и начни с Этапа 1!

```bash
# Измерь текущий размер
ls -lh app/build/outputs/apk/full/release/*.apk

# Запиши результат, затем начинай оптимизацию
```

Удачи! 🚀
