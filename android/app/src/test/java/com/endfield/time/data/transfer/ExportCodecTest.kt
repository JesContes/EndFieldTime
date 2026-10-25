package com.endfield.time.data.transfer

import com.endfield.time.data.local.MilestoneEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportCodecTest {

    private fun record(
        id: Long = 1L,
        content: String = "坚持早起",
        category: String = "生活",
        targetDate: Long = 20600L,
        colorHex: String = "#66CCFF",
        detail: String = "",
        createdAt: Long = 1_761_300_000_000L,
        sortOrder: Int = 0
    ) = MilestoneEntity(
        id = id,
        content = content,
        category = category,
        targetDate = targetDate,
        colorHex = colorHex,
        detail = detail,
        createdAt = createdAt,
        sortOrder = sortOrder
    )

    @Test
    fun `encode then decode keeps all fields`() {
        val source = listOf(
            record(id = 1, content = "坚持早起", category = "生活", detail = "第 45 天"),
            record(id = 2, content = "恋爱纪念日", category = "纪念日", colorHex = "#F06292", targetDate = 20800)
        )
        val json = ExportCodec.encode(source, exportedAtSeconds = 1_761_300_000L)
        val preview = ExportCodec.decode(json)

        assertEquals(2, preview.count)
        assertEquals(0, preview.skipped)
        assertEquals(listOf("生活", "纪念日"), preview.categories)

        val first = preview.records[0]
        assertEquals("坚持早起", first.content)
        assertEquals("生活", first.category)
        assertEquals(20600L, first.targetDate)
        assertEquals("#66CCFF", first.colorHex)
        assertEquals("第 45 天", first.detail)
        assertEquals(1_761_300_000_000L, first.createdAt)
        assertEquals(0L, first.id) // 导入时重新分配主键

        val second = preview.records[1]
        assertEquals("#F06292", second.colorHex)
        assertEquals("", second.detail)
    }

    @Test
    fun `empty detail and zero sortOrder are omitted from output`() {
        val json = ExportCodec.encode(listOf(record(detail = "", sortOrder = 0)))
        assertFalse("空详情不应出现 t 键", json.contains("\"t\""))
        assertFalse("默认排序不应出现 s 键", json.contains("\"s\""))
        assertTrue("紧凑键 c 应存在", json.contains("\"c\""))
    }

    @Test
    fun `color is stored as palette index when preset`() {
        val json = ExportCodec.encode(listOf(record(colorHex = "#4DB6AC")))
        assertTrue(json.contains("\"h\":1"))
    }

    @Test
    fun `rejects wrong format id`() {
        val error = runCatching {
            ExportCodec.decode("""{"format":"something-else","version":1,"milestones":[]}""")
        }.exceptionOrNull()
        assertTrue(error is ExportFormatException)
    }

    @Test
    fun `rejects future format version`() {
        val error = runCatching {
            ExportCodec.decode("""{"format":"endfield-time-export","version":99,"milestones":[]}""")
        }.exceptionOrNull()
        assertTrue(error is ExportFormatException)
    }

    @Test
    fun `skips invalid records and counts them`() {
        val json = """
            {"format":"endfield-time-export","version":1,
             "categories":["生活"],
             "milestones":[
               {"c":"正常记录","k":0,"d":20600,"h":0,"a":1761300000},
               {"c":"","k":0,"d":20600,"h":0,"a":1761300000},
               {"c":"缺日期","k":0,"h":0,"a":1761300000},
               {"c":"日期越界","k":0,"d":999999,"h":0,"a":1761300000}
             ]}
        """.trimIndent()
        val preview = ExportCodec.decode(json)
        assertEquals(1, preview.count)
        assertEquals(3, preview.skipped)
        assertEquals("正常记录", preview.records[0].content)
    }

    @Test
    fun `accepts category as string and color as hex fallback`() {
        val json = """
            {"format":"endfield-time-export","version":1,
             "milestones":[{"c":"自定义","k":"健身","d":20600,"h":"#123ABC","a":1761300000}]}
        """.trimIndent()
        val preview = ExportCodec.decode(json)
        assertEquals("健身", preview.records[0].category)
        assertEquals("#123ABC", preview.records[0].colorHex)
    }

    @Test
    fun `truncates oversized fields instead of failing`() {
        val longContent = "字".repeat(80)
        val longDetail = "详".repeat(1500)
        val json = """
            {"format":"endfield-time-export","version":1,"categories":["生活"],
             "milestones":[{"c":"$longContent","k":0,"d":20600,"h":0,"a":1761300000,"t":"$longDetail"}]}
        """.trimIndent()
        val preview = ExportCodec.decode(json)
        assertEquals(50, preview.records[0].content.length)
        assertEquals(1000, preview.records[0].detail.length)
    }
}
