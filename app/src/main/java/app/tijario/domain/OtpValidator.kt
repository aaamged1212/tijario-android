package app.tijario.domain

object OtpValidator {
    fun sanitize(input: String): String {
        return input.filter { it.isDigit() }.take(8)
    }

    fun isValid(otp: String): Boolean {
        return otp.length == 8 && otp.all { it.isDigit() }
    }
}
