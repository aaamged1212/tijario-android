package app.tijario.domain

object OtpValidator {
    const val OTP_LENGTH = 8

    fun sanitize(input: String): String {
        return input.trim().replace(" ", "").filter { it.isDigit() }.take(OTP_LENGTH)
    }

    fun isValid(otp: String): Boolean {
        val normalized = otp.trim().replace(" ", "")
        return normalized.length == OTP_LENGTH && normalized.all { it.isDigit() }
    }
}
