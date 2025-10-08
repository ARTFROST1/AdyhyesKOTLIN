# 🔧 Исправление ошибки "base64: invalid input"

## ❌ Проблема

```
base64: invalid input
Error: Process completed with exit code 1.
```

## 🔍 Причины ошибки

1. **Секрет KEYSTORE_BASE64 не установлен** в GitHub
2. **Некорректный base64 код** - содержит лишние символы
3. **Пустой секрет** - значение не было сохранено
4. **Неправильная кодировка** при создании base64

## ✅ Решение

### Шаг 1: Генерация правильного base64 кода

Запустите автоматический скрипт:

```powershell
# В корне проекта выполните:
.\scripts\generate-keystore-base64.ps1
```

**Что делает скрипт:**
- ✅ Проверяет существование keystore файла
- ✅ Читает файл как бинарные данные
- ✅ Корректно конвертирует в base64
- ✅ Сохраняет результат в `keystore-base64.txt`
- ✅ Показывает превью для проверки

### Шаг 2: Ручная генерация (если скрипт не работает)

```powershell
# Альтернативный способ в PowerShell:
$bytes = [System.IO.File]::ReadAllBytes("keystore\adygyes-release.keystore")
$base64 = [System.Convert]::ToBase64String($bytes)
$base64 | Out-File -FilePath "keystore-base64.txt" -Encoding UTF8
Write-Host "Base64 код сохранен в keystore-base64.txt"
```

### Шаг 3: Добавление секрета в GitHub

1. **Откройте ваш GitHub репозиторий**
2. **Перейдите:** Settings → Secrets and variables → Actions
3. **Нажмите:** "New repository secret"
4. **Введите имя:** `KEYSTORE_BASE64`
5. **Скопируйте содержимое** файла `keystore-base64.txt`
6. **Вставьте в поле Value** (весь текст, без пробелов)
7. **Нажмите:** "Add secret"

### Шаг 4: Проверка других секретов

Убедитесь, что все секреты установлены:

| Секрет | Значение | Источник |
|--------|----------|----------|
| `KEYSTORE_BASE64` | base64 код keystore | `keystore-base64.txt` |
| `KEYSTORE_PASSWORD` | `A12345` | `keystore.properties` |
| `KEY_PASSWORD` | `A12345` | `keystore.properties` |
| `KEY_ALIAS` | `adygyes-release` | `keystore.properties` |
| `YANDEX_MAPKIT_API_KEY` | ваш API ключ | `local.properties` |

### Шаг 5: Повторный запуск релиза

```bash
# Создайте новый тег для тестирования:
git tag v1.0.1-test
git push origin v1.0.1-test

# Или запустите вручную через GitHub Actions UI
```

## 🔍 Диагностика проблем

### Проверка base64 кода локально:

```powershell
# Проверьте, что base64 код валидный:
$base64 = Get-Content "keystore-base64.txt"
$bytes = [System.Convert]::FromBase64String($base64)
Write-Host "✅ Base64 код корректный, размер: $($bytes.Length) байт"
```

### Типичные ошибки:

#### ❌ Лишние символы:
```
# Неправильно (с переносами строк):
MIIKEAIBAzCCCc4GCSqGSIb3DQEHAaCCCb8EggmzMIIJrzCCBW8GCSqGSIb3DQEHAaCCBWAEggVc
MIIFWDCCBVQGCyqGSIb3DQEMCgECoIIE7jCCBOowHAYKKoZIhvcNAQwBAzAOBAhQcm9qZWN0...

# Правильно (одна строка):
MIIKEAIBAzCCCc4GCSqGSIb3DQEHAaCCCb8EggmzMIIJrzCCBW8GCSqGSIb3DQEHAaCCBWAEggVcMIIFWDCCBVQGCyqGSIb3DQEMCgECoIIE7jCCBOowHAYKKoZIhvcNAQwBAzAOBAhQcm9qZWN0...
```

#### ❌ Неправильная кодировка:
```powershell
# Неправильно:
Get-Content "keystore\adygyes-release.keystore" | Out-String | Out-File

# Правильно:
[System.IO.File]::ReadAllBytes() | [System.Convert]::ToBase64String()
```

## 🚀 Улучшенная диагностика в GitHub Actions

Обновленный workflow теперь показывает:
- ✅ Длину base64 строки
- ✅ Успешность декодирования
- ✅ Размер созданного keystore файла
- ❌ Детальные ошибки при проблемах

## 📋 Чек-лист исправления

- [ ] Запустил скрипт `generate-keystore-base64.ps1`
- [ ] Получил файл `keystore-base64.txt`
- [ ] Скопировал содержимое файла (одна строка)
- [ ] Добавил секрет `KEYSTORE_BASE64` в GitHub
- [ ] Проверил все остальные секреты
- [ ] Создал тестовый тег для проверки
- [ ] Убедился, что релиз прошел успешно

## 🎯 Ожидаемый результат

После исправления в логах GitHub Actions увидите:

```
🔍 Keystore length: 6789
✅ Keystore decoded successfully
-rw-r--r-- 1 runner runner 4992 Oct  8 14:30 adygyes-release.keystore
```

**🎉 Готово! Теперь релиз должен проходить без ошибок.**
