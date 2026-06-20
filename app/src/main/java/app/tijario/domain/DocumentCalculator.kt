package app.tijario.domain

import java.math.BigDecimal
import java.math.RoundingMode

object DocumentCalculator {
    data class ItemInput(
        val quantity: String,
        val unitPrice: String
    )

    data class CalculationResult(
        val subtotal: BigDecimal,
        val discount: BigDecimal,
        val extraFees: BigDecimal,
        val total: BigDecimal
    )

    fun calculate(
        items: List<ItemInput>,
        discountStr: String,
        extraFeesStr: String
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
        
        var total = subtotal.subtract(discount).add(extraFees)
        if (total < BigDecimal.ZERO) {
            total = BigDecimal.ZERO
        }
        
        return CalculationResult(
            subtotal = subtotal.setScale(2, RoundingMode.HALF_UP),
            discount = discount.setScale(2, RoundingMode.HALF_UP),
            extraFees = extraFees.setScale(2, RoundingMode.HALF_UP),
            total = total.setScale(2, RoundingMode.HALF_UP)
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
