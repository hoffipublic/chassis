package com.hoffi.chassis.shared.test

//import org.junit.jupiter.api.Test // plain junit test without kotlin test support
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeJunit5Test {
    @Test
    fun testSum() {
        val expected = 42
        assertEquals(expected, 42)
    }
}
