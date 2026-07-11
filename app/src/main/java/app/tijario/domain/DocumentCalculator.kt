package app.tijario.domain

import java.math.BigDecimal
import java.math.RoundingMode

object DocumentCalculator {
    data class ItemInput(
        val quantity: String,
        val unitPrice: String,
    )

    data class CalculationResult(
        val subtotal: BigDecimal,
        val discount: BigDecimal,
        val extraFees: BigDecimal,
        val taxBase: BigDecimal,
        val taxAmount: BigDecimal,
        val total: BigDecimal,
        val amountRemaining: BigDecimal,
        val isValid: Boolean,
    )

    fun calculate(
        items: List<ItemInput>,
        discountStr: String,
        extraFeesStr: String,
        taxRateStr: String = "0",
        amountPaidStr: String = "0",
    ): CalculationResult {
        var subtotal = BigDecimal.ZERO
        for (item in items) {
            val qtyVal = parseQty(item.quantity)
            val priceVal = parseMoney(item.unitPrice)
            if (qtyVal != null && priceVal != null) {
                subtotal = subtotal.add(qtyVal.multiply(priceVal))
            }
        }

        val discount = parseMoney(discountStr) ?: BigDecimal.ZERO
        val extraFees = parseMoney(extraFeesStr) ?: BigDecimal.ZERO
        val rawTaxBase = subtotal.subtract(discount).add(extraFees).setScale(2, RoundingMode.HALF_UP)
        val isValid = rawTaxBase >= BigDecimal.ZERO
        val zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        val taxBase = if (isValid) rawTaxBase else zero
        val normalizedTaxRate = parseMoney(taxRateStr) ?: BigDecimal.ZERO
        val taxAmount = if (isValid) {
            taxBase.multiply(normalizedTaxRate.divide(BigDecimal("100"), 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            zero
        }
        val total = if (isValid) taxBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP) else zero
        val amountPaid = parseMoney(amountPaidStr) ?: BigDecimal.ZERO
        val amountRemaining = if (isValid) {
            total.subtract(amountPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
        } else {
            zero
        }

        return CalculationResult(
            subtotal = subtotal.setScale(2, RoundingMode.HALF_UP),
            discount = discount.setScale(2, RoundingMode.HALF_UP),
            extraFees = extraFees.setScale(2, RoundingMode.HALF_UP),
            taxBase = taxBase,
            taxAmount = taxAmount,
            total = total,
            amountRemaining = amountRemaining,
            isValid = isValid,
        )
    }

    fun parseQty(value: String): BigDecimal? {
        val cleaned = normalize(value)
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned).takeIf { it > BigDecimal.ZERO }
        } catch (e: Exception) {
            null
        }
    }

    fun parseMoney(value: String): BigDecimal? {
        val cleaned = normalize(value)
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned).takeIf { it >= BigDecimal.ZERO }
        } catch (e: Exception) {
            null
        }
    }

    private fun normalize(value: String): String {
        val raw = buildString {
            value.trim().forEach { char ->
                append(
                    when (char) {
                        in '0'..'9' -> char
                        in '٠'..'٩' -> '0' + (char.code - '٠'.code)
                        in '۰'..'۹' -> '0' + (char.code - '۰'.code)
                        '٫' -> '.'
                        '٬' -> ','
                        else -> char
                    }
                )
            }
        }
            .replace("\\s".toRegex(), "")
            .replace("[^0-9,.-]".toRegex(), "")

        if (raw.count { it == ',' } == 1 && !raw.contains('.')) {
            val digitsAfterComma = raw.substringAfter(',').count { it.isDigit() }
            return if (digitsAfterComma in 1..2) raw.replace(',', '.') else raw.replace(",", "")
        }

        return raw.replace(",", "")
    }
}
