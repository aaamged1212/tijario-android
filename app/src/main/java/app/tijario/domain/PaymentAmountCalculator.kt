package app.tijario.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object PaymentAmountCalculator {
    data class Amounts(
        val paid: BigDecimal,
        val remaining: BigDecimal,
    )

    fun calculate(
        paymentStatus: String?,
        total: BigDecimal,
        amountPaid: BigDecimal?,
    ): Amounts {
        val normalizedTotal = total.nonNegativeMoney()
        val paid = when (paymentStatus?.lowercase(Locale.US)) {
            "paid" -> normalizedTotal
            "partial", "partially_paid" -> amountPaid.orZero().coerceMoneyIn(BigDecimal.ZERO, normalizedTotal)
            else -> BigDecimal.ZERO
        }.nonNegativeMoney()
        return Amounts(
            paid = paid,
            remaining = normalizedTotal.subtract(paid).nonNegativeMoney(),
        )
    }

    fun remainingDouble(
        paymentStatus: String?,
        total: Double,
        amountPaid: Double?,
    ): Double =
        calculate(
            paymentStatus = paymentStatus,
            total = BigDecimal.valueOf(total),
            amountPaid = amountPaid?.let(BigDecimal::valueOf),
        ).remaining.toDouble()

    private fun BigDecimal?.orZero(): BigDecimal = this ?: BigDecimal.ZERO

    private fun BigDecimal.nonNegativeMoney(): BigDecimal =
        maxOf(this, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

    private fun BigDecimal.coerceMoneyIn(minimum: BigDecimal, maximum: BigDecimal): BigDecimal =
        minOf(maxOf(this, minimum), maximum)
}
