# 🔐 Быстрая настройка Keystore для GitHub Actions

## ✅ Base64 код уже создан!

Файл `keystore-base64.txt` содержит правильный base64 код вашего keystore.

## 🚀 Следующие шаги:

### 1. Скопируйте base64 код
```powershell
# Откройте файл и скопируйте ВСЁ содержимое:
notepad keystore-base64.txt
```

### 2. Добавьте секреты в GitHub

Перейдите: **GitHub репозиторий** → **Settings** → **Secrets and variables** → **Actions**

Создайте эти секреты:

| Имя секрета | Значение |
|-------------|----------|
| `KEYSTORE_BASE64` | Содержимое файла `keystore-base64.txt` |
| `KEYSTORE_PASSWORD` | `A12345` |
| `KEY_PASSWORD` | `A12345` |
| `KEY_ALIAS` | `adygyes-release` |
| `YANDEX_MAPKIT_API_KEY` | Ваш API ключ из `local.properties` |

### 3. Создайте тестовый релиз

```bash
git add .
git commit -m "🔧 Fix keystore setup"
git tag v1.0.1-test
git push origin main
git push origin v1.0.1-test
```

## 🎯 Проверка

После добавления секретов релиз должен пройти успешно и вы увидите:

```
✅ Keystore decoded successfully
📦 Build Release APK
📱 Build Release AAB  
🚀 Create GitHub Release
```

## ⚠️ Важно

- Никогда не публикуйте содержимое `keystore-base64.txt`
- Файл добавлен в `.gitignore`
- Секреты видны только вам в настройках GitHub

**🎉 Готово! Теперь запустите релиз.**
