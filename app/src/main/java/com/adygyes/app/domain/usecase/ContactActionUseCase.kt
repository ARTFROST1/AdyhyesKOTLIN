package com.adygyes.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case для обработки действий с контактной информацией
 */
@Singleton
class ContactActionUseCase @Inject constructor() {

    /**
     * Совершить звонок по номеру телефона
     */
    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.replace(" ", "").replace("-", "")}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showError(context, "Не удалось открыть приложение для звонков")
        }
    }

    /**
     * Отправить email
     */
    fun sendEmail(context: Context, email: String, subject: String? = null, body: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                body?.let { putExtra(Intent.EXTRA_TEXT, it) }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showError(context, "Не удалось открыть приложение для email")
        }
    }

    /**
     * Открыть веб-сайт
     */
    fun openWebsite(context: Context, url: String) {
        try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            showError(context, "Не удалось открыть веб-сайт")
        }
    }

    /**
     * Открыть социальную сеть
     */
    fun openSocialMedia(context: Context, platform: String, url: String) {
        try {
            // Попытка открыть через нативное приложение
            val nativeIntent = createNativeSocialIntent(platform, url)
            if (nativeIntent != null && canResolveIntent(context, nativeIntent)) {
                context.startActivity(nativeIntent)
                return
            }
            
            // Fallback на веб-версию
            openWebsite(context, url)
        } catch (e: Exception) {
            showError(context, "Не удалось открыть ${getSocialMediaName(platform)}")
        }
    }

    /**
     * Скопировать контактную информацию в буфер обмена
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Контакт") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            showError(context, "Не удалось скопировать")
        }
    }

    /**
     * Поделиться контактной информацией
     */
    fun shareContact(context: Context, contactText: String, attractionName: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Контакты $attractionName:\n$contactText")
                putExtra(Intent.EXTRA_SUBJECT, "Контакты $attractionName")
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться контактами"))
        } catch (e: Exception) {
            showError(context, "Не удалось поделиться контактами")
        }
    }

    private fun createNativeSocialIntent(platform: String, url: String): Intent? {
        return when (platform.lowercase()) {
            "instagram" -> {
                val username = extractUsernameFromUrl(url)
                Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/$username"))
            }
            "telegram" -> {
                val username = extractUsernameFromUrl(url)
                Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$username"))
            }
            "vk", "vkontakte" -> {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.vkontakte.android")
                }
            }
            "youtube" -> {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.google.android.youtube")
                }
            }
            else -> null
        }
    }

    private fun canResolveIntent(context: Context, intent: Intent): Boolean {
        return context.packageManager.resolveActivity(intent, 0) != null
    }

    private fun extractUsernameFromUrl(url: String): String {
        return url.split("/").lastOrNull()?.takeIf { it.isNotEmpty() } ?: url
    }

    private fun getSocialMediaName(platform: String): String {
        return when (platform.lowercase()) {
            "instagram" -> "Instagram"
            "telegram" -> "Telegram"
            "vk", "vkontakte" -> "VKontakte"
            "facebook" -> "Facebook"
            "youtube" -> "YouTube"
            else -> platform.replaceFirstChar { it.uppercase() }
        }
    }

    private fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Результат действия с контактом
 */
sealed class ContactActionResult {
    object Success : ContactActionResult()
    data class Error(val message: String) : ContactActionResult()
}

/**
 * Типы контактных действий
 */
enum class ContactActionType {
    PHONE_CALL,
    EMAIL,
    WEBSITE,
    SOCIAL_MEDIA,
    COPY,
    SHARE
}
