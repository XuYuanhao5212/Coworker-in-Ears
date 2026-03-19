package com.coworkerinears.app

import android.content.Context
import com.coworkerinears.app.validation.ValidationDevicePreset
import com.coworkerinears.app.validation.ValidationTriggerPreset

enum class AppLanguage(val code: String) {
    ZH("zh"),
    EN("en");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: EN
        }
    }
}

private const val SETTINGS_PREFS = "coworker_settings"
private const val APP_LANGUAGE_KEY = "app_language"

fun Context.loadAppLanguage(): AppLanguage {
    val storedCode = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getString(APP_LANGUAGE_KEY, null)
    if (storedCode != null) {
        return AppLanguage.fromCode(storedCode)
    }

    val systemLanguage = resources.configuration.locales[0]?.language.orEmpty()
    return if (systemLanguage.startsWith("zh")) {
        AppLanguage.ZH
    } else {
        AppLanguage.EN
    }
}

fun Context.persistAppLanguage(language: AppLanguage) {
    getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(APP_LANGUAGE_KEY, language.code)
        .apply()
}

fun AppLanguage.settingsButtonLabel(): String {
    return if (this == AppLanguage.ZH) "设置" else "Settings"
}

fun AppLanguage.languageMenuItemLabel(target: AppLanguage): String {
    return when (target) {
        AppLanguage.ZH -> if (this == AppLanguage.ZH) "中文" else "Chinese"
        AppLanguage.EN -> if (this == AppLanguage.ZH) "英文" else "English"
    }
}

fun ValidationTriggerPreset.localizedDisplayName(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ZH -> when (this) {
            ValidationTriggerPreset.MEDIA_KEY -> "耳机媒体键"
            ValidationTriggerPreset.PUSH_TO_TALK -> "按住说话"
            ValidationTriggerPreset.SESSION_HOTWORD -> "会话触发词"
        }

        AppLanguage.EN -> displayName
    }
}

fun ValidationTriggerPreset.localizedPathSummary(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ZH -> when (this) {
            ValidationTriggerPreset.MEDIA_KEY -> "模拟已支持耳机发出媒体按钮触发事件。"
            ValidationTriggerPreset.PUSH_TO_TALK -> "模拟用户主动开始一次采集，不依赖耳机设备事件。"
            ValidationTriggerPreset.SESSION_HOTWORD -> "模拟仅在当前 App 会话内生效的自定义触发词。"
        }

        AppLanguage.EN -> pathSummary
    }
}

fun ValidationDevicePreset.localizedDisplayName(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ZH -> when (this) {
            ValidationDevicePreset.HUAWEI -> "华为 FreeBuds"
            ValidationDevicePreset.LENOVO -> "联想 ThinkPlus"
            ValidationDevicePreset.OPPO -> "OPPO Enco"
            ValidationDevicePreset.HONOR -> "荣耀 Earbuds"
            ValidationDevicePreset.UNKNOWN -> "未知耳机"
        }

        AppLanguage.EN -> displayName
    }
}
