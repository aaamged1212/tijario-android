package app.tijario

import app.tijario.domain.DocumentCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.math.BigDecimal

class DocumentCalculatorTests {

    @Test
    fun testCalculator_oneItem() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "2", unitPrice = "15.50")
        )
        val result = DocumentCalculator.calculate(items, discountStr = "0", extraFeesStr = "0")
        
        assertEquals(BigDecimal("31.00"), result.subtotal)
        assertEquals(BigDecimal("0.00"), result.discount)
        assertEquals(BigDecimal("0.00"), result.extraFees)
        assertEquals(BigDecimal("31.00"), result.total)
    }

    @Test
    fun testCalculator_multipleItems() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "2", unitPrice = "15.50"),
            DocumentCalculator.ItemInput(quantity = "1", unitPrice = "10.00")
        )
        val result = DocumentCalculator.calculate(items, discountStr = "5.00", extraFeesStr = "2.50")
        
        assertEquals(BigDecimal("41.00"), result.subtotal)
        assertEquals(BigDecimal("5.00"), result.discount)
        assertEquals(BigDecimal("2.50"), result.extraFees)
        assertEquals(BigDecimal("38.50"), result.total)
    }

    @Test
    fun testCalculator_emptyItem_ignoredInSubtotal() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "2", unitPrice = "15.50"),
            DocumentCalculator.ItemInput(quantity = "", unitPrice = "") // empty name or quantity should be ignored in calculations
        )
        val result = DocumentCalculator.calculate(items, discountStr = "0", extraFeesStr = "0")
        assertEquals(BigDecimal("31.00"), result.subtotal)
    }

    @Test
    fun testCalculator_invalidQuantityOrPrice_ignored() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "abc", unitPrice = "15.50"),
            DocumentCalculator.ItemInput(quantity = "2", unitPrice = "xyz")
        )
        val result = DocumentCalculator.calculate(items, discountStr = "0", extraFeesStr = "0")
        assertEquals(BigDecimal("0.00"), result.subtotal)
    }

    @Test
    fun testCalculator_decimalValues() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "1.5", unitPrice = "10.00")
        )
        val result = DocumentCalculator.calculate(items, discountStr = "0", extraFeesStr = "0")
        assertEquals(BigDecimal("15.00"), result.subtotal)
    }

    @Test
    fun testCalculator_totalNotFallingBelowZero() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "1", unitPrice = "10.00")
        )
        // Discount 15 exceeds subtotal of 10. The calculation is invalid and should not produce a taxable total.
        val result = DocumentCalculator.calculate(items, discountStr = "15.00", extraFeesStr = "2.00")
        
        assertEquals(BigDecimal("10.00"), result.subtotal)
        assertEquals(BigDecimal("15.00"), result.discount)
        assertEquals(BigDecimal("2.00"), result.extraFees)
        assertEquals(BigDecimal("0.00"), result.total)
        assertFalse(result.isValid)
    }

    @Test
    fun testCalculator_appliesDocumentTax() {
        val items = listOf(
            DocumentCalculator.ItemInput(quantity = "2", unitPrice = "15.50")
        )
        val result = DocumentCalculator.calculate(
            items = items,
            discountStr = "5.00",
            extraFeesStr = "2.50",
            taxRateStr = "10",
            amountPaidStr = "0",
        )

        assertEquals(BigDecimal("31.00"), result.subtotal)
        assertEquals(BigDecimal("5.00"), result.discount)
        assertEquals(BigDecimal("2.50"), result.extraFees)
        assertEquals(BigDecimal("28.50"), result.taxBase)
        assertEquals(BigDecimal("2.85"), result.taxAmount)
        assertEquals(BigDecimal("31.35"), result.total)
    }

    @Test
    fun testCalculator_keepsDecimalMathExactAcrossDiscountTaxAndPaidAmount() {
        val result = DocumentCalculator.calculate(
            items = listOf(
                DocumentCalculator.ItemInput(quantity = "1", unitPrice = "0.1"),
                DocumentCalculator.ItemInput(quantity = "1", unitPrice = "0.2"),
                DocumentCalculator.ItemInput(quantity = "3", unitPrice = "999999999.99"),
            ),
            discountStr = "10",
            extraFeesStr = "0.05",
            taxRateStr = "15",
            amountPaidStr = "1.00",
        )

        assertEquals(BigDecimal("3000000000.27"), result.subtotal)
        assertEquals(BigDecimal("2999999990.32"), result.taxBase)
        assertEquals(BigDecimal("449999998.55"), result.taxAmount)
        assertEquals(BigDecimal("3449999988.87"), result.total)
        assertEquals(BigDecimal("3449999987.87"), result.amountRemaining)
    }

    @Test
    fun testCalculator_acceptsArabicAndCommaDecimalInputs() {
        val result = DocumentCalculator.calculate(
            items = listOf(DocumentCalculator.ItemInput(quantity = "\u0662", unitPrice = "\u0661\u066c\u0662\u0663\u0664\u066b\u0665")),
            discountStr = "0,50",
            extraFeesStr = "\u0661\u066b\u0662\u0665",
            taxRateStr = "5",
            amountPaidStr = "0",
        )

        assertEquals(BigDecimal("2469.00"), result.subtotal)
        assertEquals(BigDecimal("0.50"), result.discount)
        assertEquals(BigDecimal("1.25"), result.extraFees)
        assertEquals(BigDecimal("2469.75"), result.taxBase)
        assertEquals(BigDecimal("123.49"), result.taxAmount)
        assertEquals(BigDecimal("2593.24"), result.total)
    }
}
