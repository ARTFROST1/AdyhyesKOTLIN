package com.adygyes.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.adygyes.app.domain.usecase.ContactActionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel для управления действиями с контактной информацией
 */
@HiltViewModel
class ContactActionViewModel @Inject constructor(
    private val contactActionUseCase: ContactActionUseCase
) : ViewModel() {

    fun makePhoneCall(context: Context, phoneNumber: String) {
        contactActionUseCase.makePhoneCall(context, phoneNumber)
    }

    fun sendEmail(context: Context, email: String) {
        contactActionUseCase.sendEmail(context, email)
    }

    fun openWebsite(context: Context, url: String) {
        contactActionUseCase.openWebsite(context, url)
    }

    fun openSocialMedia(context: Context, platform: String, url: String) {
        contactActionUseCase.openSocialMedia(context, platform, url)
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Контакт") {
        contactActionUseCase.copyToClipboard(context, text, label)
    }

    fun shareContact(context: Context, contactText: String, attractionName: String) {
        contactActionUseCase.shareContact(context, contactText, attractionName)
    }
}
