package com.rogers.eventapp.utils

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class StringResource(@StringRes val resId: Int) : UiText()
    data class DynamicString(val value: String) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId)
        }
    }
}