package app.tijario.domain

object OtpValidator {
    fun sanitize(input: String): String {
        return input.trim().replace(" ", "").filter { it.isDigit() }.take(6)
    }

    fun isValid(otp: String): Boolean {
        val normalized = otp.trim().replace(" ", "")
        return normalized.length == 6 && normalized.all { it.isDigit() }
    }
}
