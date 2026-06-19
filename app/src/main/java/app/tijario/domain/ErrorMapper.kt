package app.tijario.domain

import java.net.ConnectException
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException

object ErrorMapper {
    fun map(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException, is ConnectException -> {
                "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت."
            }
            is SocketTimeoutException -> {
                "انتهت مهلة الاتصال بالخادم. يرجى المحاولة مرة أخرى."
            }
            is IOException -> {
                "حدث خطأ في الاتصال بالشبكة."
            }
            else -> {
                val message = throwable.message.orEmpty()
                when {
                    message.contains("Invalid login credentials", ignoreCase = true) -> {
                        "البريد الإلكتروني أو كلمة المرور غير صحيحة."
                    }
                    message.contains("Email not confirmed", ignoreCase = true) -> {
                        "الرجاء تأكيد البريد الإلكتروني أولاً."
                    }
                    message.contains("already registered", ignoreCase = true) || message.contains("user_already_exists", ignoreCase = true) -> {
                        "البريد الإلكتروني مسجل بالفعل."
                    }
                    message.contains("rate limit", ignoreCase = true) -> {
                        "تم تجاوز حد إرسال الرموز. يرجى المحاولة بعد قليل."
                    }
                    message.contains("invalid flow state", ignoreCase = true) || message.contains("invalid grant", ignoreCase = true) -> {
                        "رمز التحقق غير صحيح أو انتهت صلاحيته."
                    }
                    message.contains("limit", ignoreCase = true) || message.contains("threshold", ignoreCase = true) || message.contains("exceeded", ignoreCase = true) -> {
                        "لقد تجاوزت الحد الشهري المسموح به للعمليات."
                    }
                    else -> {
                        "حدث خطأ غير متوقع. يرجى المحاولة لاحقاً."
                    }
                }
            }
        }
    }

    fun mapApiCode(code: String?, defaultMessage: String? = null): String {
        if (code == null) return defaultMessage ?: "حدث خطأ غير متوقع. حاول مرة أخرى."
        return when (code.uppercase()) {
            "LIMIT_EXCEEDED", "USAGE_LIMIT" -> "لقد تجاوزت الحد الشهري المسموح به للعمليات."
            "UNAUTHORIZED", "SESSION_EXPIRED" -> "انتهت صلاحية الجلسة. يرجى تسجيل الدخول مرة أخرى."
            "INVALID_CREDENTIALS" -> "البريد الإلكتروني أو كلمة المرور غير صحيحة."
            "DUPLICATE_CUSTOMER" -> "هذا العميل مضاف بالفعل."
            "DUPLICATE_PRODUCT" -> "هذا المنتج مضاف بالفعل."
            "EMAIL_NOT_CONFIRMED" -> "الرجاء تأكيد البريد الإلكتروني أولاً."
            "SERVER_ERROR" -> "حدث خطأ في الخادم الداخلي."
            else -> defaultMessage ?: "حدث خطأ في النظام ($code)."
        }
    }
}
