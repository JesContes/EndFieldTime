package com.endfield.time.domain

import com.endfield.time.data.local.MilestoneEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DayCounterTest {

    private val today = LocalDate.of(2026, 10, 25)

    @Test
    fun `past date is PASSED`() {
        val state = DayCounter.compute(LocalDate.of(2026, 9, 10), today)
        assertEquals(DayType.PASSED, state.type)
        assertEquals(45, state.count)
        assertEquals("已过", state.label)
    }

    @Test
    fun `same date is TODAY`() {
        val state = DayCounter.compute(today, today)
        assertEquals(DayType.TODAY, state.type)
        assertEquals(0, state.count)
        assertEquals("就是今天", state.label)
    }

    @Test
    fun `future date is FUTURE`() {
        val state = DayCounter.compute(LocalDate.of(2026, 11, 24), today)
        assertEquals(DayType.FUTURE, state.type)
        assertEquals(30, state.count)
        assertEquals("还有", state.label)
    }

    @Test
    fun `entity maps to ui model`() {
        val entity = MilestoneEntity(
            id = 7L,
            content = "坚持早起",
            category = "生活",
            targetDate = LocalDate.of(2026, 9, 10).toEpochDay(),
            colorHex = "#66CCFF",
            detail = "",
            createdAt = 0L
        )
        val ui = DayCounter.toUi(entity, today)
        assertEquals(7L, ui.id)
        assertEquals("2026-09-10", ui.targetDateText)
        assertEquals(45, ui.dayCount)
        assertEquals(false, ui.hasDetail)
    }
}
