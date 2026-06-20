package app.tijario

import app.tijario.domain.DocumentCalculator
import org.junit.Assert.assertEquals
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
        // Discount 15 exceeds subtotal of 10. Total must not fall below zero.
        val result = DocumentCalculator.calculate(items, discountStr = "15.00", extraFeesStr = "2.00")
        
        assertEquals(BigDecimal("10.00"), result.subtotal)
        assertEquals(BigDecimal("15.00"), result.discount)
        assertEquals(BigDecimal("2.00"), result.extraFees)
        assertEquals(BigDecimal("0.00"), result.total)
    }
}
