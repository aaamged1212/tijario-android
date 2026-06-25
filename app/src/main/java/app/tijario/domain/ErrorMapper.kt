package app.tijario.domain

import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException
import app.tijario.config.AppLanguage
import app.tijario.config.Localization

object ErrorMapper {
    fun map(throwable: Throwable, lang: AppLanguage): String {
        return when (throwable) {
            is UnknownHostException, is ConnectException -> {
                Localization.getString("error_connect_server", lang)
            }
            is SocketTimeoutException -> {
                Localization.getString("error_timeout", lang)
            }
            is IOException -> {
                Localization.getString("error_network", lang)
            }
            else -> {
                val message = throwable.message.orEmpty()
                when {
                    message.contains("Invalid login credentials", ignoreCase = true) -> {
                        Localization.getString("error_invalid_credentials", lang)
                    }
                    message.contains("Email not confirmed", ignoreCase = true) -> {
                        Localization.getString("error_email_not_confirmed", lang)
                    }
                    message.contains("email not found", ignoreCase = true) || message.contains("user not found", ignoreCase = true) || message.contains("not registered", ignoreCase = true) -> {
                        Localization.getString("error_email_not_registered", lang)
                    }
                    message.contains("already registered", ignoreCase = true) || message.contains("user_already_exists", ignoreCase = true) -> {
                        Localization.getString("error_email_already_registered", lang)
                    }
                    message.contains("rate limit", ignoreCase = true) -> {
                        Localization.getString("error_rate_limit", lang)
                    }
                    message.contains("invalid flow state", ignoreCase = true) || message.contains("invalid grant", ignoreCase = true) || message.contains("otp expired", ignoreCase = true) || message.contains("code expired", ignoreCase = true) -> {
                        Localization.getString("error_invalid_flow_state", lang)
                    }
                    message.contains("limit", ignoreCase = true) || message.contains("threshold", ignoreCase = true) || message.contains("exceeded", ignoreCase = true) -> {
                        Localization.getString("error_limit_exceeded", lang)
                    }
                    else -> {
                        Localization.getString("error_generic", lang)
                    }
                }
            }
        }
    }

    fun mapApiCode(code: String?, lang: AppLanguage, defaultMessage: String? = null): String {
        if (code == null) return defaultMessage ?: Localization.getString("error_generic", lang)
        return when (code.uppercase()) {
            "LIMIT_EXCEEDED", "USAGE_LIMIT" -> Localization.getString("error_limit_exceeded", lang)
            "UNAUTHORIZED", "SESSION_EXPIRED" -> Localization.getString("error_session_expired", lang)
            "INVALID_CREDENTIALS" -> Localization.getString("error_invalid_credentials", lang)
            "EMAIL_NOT_REGISTERED", "USER_NOT_FOUND", "EMAIL_NOT_FOUND" -> Localization.getString("error_email_not_registered", lang)
            "DUPLICATE_CUSTOMER" -> Localization.getString("error_duplicate_customer", lang)
            "DUPLICATE_PRODUCT" -> Localization.getString("error_duplicate_product", lang)
            "EMAIL_NOT_CONFIRMED" -> Localization.getString("error_email_not_confirmed", lang)
            "SERVER_ERROR" -> Localization.getString("error_server", lang)
            else -> defaultMessage ?: String.format(Localization.getString("error_system_with_code", lang), code)
        }
    }
}
