package com.endfield.time.data.transfer

import com.endfield.time.data.MilestoneRepository
import com.endfield.time.data.local.MilestoneEntity
import org.json.JSONArray
import org.json.JSONObject

/** 解析结果 */
data class ImportPreview(
    val records: List<MilestoneEntity>,
    val skipped: Int,
    val categories: List<String>
) {
    val count: Int get() = records.size
}

class ExportFormatException(message: String) : Exception(message)

/**
 * 「时记」导出格式 v1 编解码。
 *
 * 设计要点（详见 docs/07-数据导入导出设计.md）：
 * - 紧凑键（c/k/d/h/a/t/s）替代全字段名，省约 20% 体积；
 * - 目标日期用 epochDay 整数（5 位）而非 ISO 字符串；
 * - 分类放字典 `categories`，记录内只存下标；颜色存 8 色板下标；
 * - createdAt 用秒级时间戳；
 * - detail 为空则整个键省略，sortOrder 为 0 则省略。
 */
object ExportCodec {

    const val FORMAT_ID = "endfield-time-export"
    const val VERSION = 1

    private const val MAX_CONTENT = 50
    private const val MAX_CATEGORY = 10
    private const val MAX_DETAIL = 1000
    private const val FALLBACK_CATEGORY = "未分类"

    /** 合理日期范围：1900-01-01 ~ 2200-01-01，越界视为脏数据 */
    private val MIN_EPOCH_DAY = java.time.LocalDate.of(1900, 1, 1).toEpochDay()
    private val MAX_EPOCH_DAY = java.time.LocalDate.of(2200, 1, 1).toEpochDay()

    private val COLOR_REGEX = Regex("^#[0-9a-fA-F]{6}$")

    // ---------------- 导出 ----------------

    fun encode(
        records: List<MilestoneEntity>,
        exportedAtSeconds: Long = System.currentTimeMillis() / 1000L,
        appVersion: String = "1.0"
    ): String {
        val categories = records.map { it.category }.distinct()
        val milestones = JSONArray()
        records.forEach { record ->
            val item = JSONObject()
            item.put("c", record.content)
            item.put("k", categories.indexOf(record.category))
            item.put("d", record.targetDate)
            item.put("h", colorToken(record.colorHex))
            item.put("a", record.createdAt / 1000L)
            if (record.detail.isNotEmpty()) item.put("t", record.detail)
            if (record.sortOrder != 0) item.put("s", record.sortOrder)
            milestones.put(item)
        }
        val root = JSONObject()
        root.put("format", FORMAT_ID)
        root.put("version", VERSION)
        root.put("exportedAt", exportedAtSeconds)
        root.put("appVersion", appVersion)
        root.put("categories", JSONArray(categories))
        root.put("milestones", milestones)
        return root.toString()
    }

    private fun colorToken(hex: String): Any {
        val index = MilestoneRepository.PRESET_COLORS.indexOfFirst { it.equals(hex, ignoreCase = true) }
        return if (index >= 0) index else hex
    }

    // ---------------- 导入 ----------------

    fun decode(text: String): ImportPreview {
        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw ExportFormatException("文件不是有效的 JSON")
        }

        if (root.optString("format") != FORMAT_ID) {
            throw ExportFormatException("这不是「时记」的导出文件")
        }
        val version = root.optInt("version", 0)
        if (version > VERSION) {
            throw ExportFormatException("文件版本 v$version 高于当前应用支持的 v$VERSION，请先升级应用")
        }

        val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
        val categories = ArrayList<String>(categoriesArray.length())
        for (i in 0 until categoriesArray.length()) {
            categories.add(categoriesArray.optString(i))
        }

        val milestones = root.optJSONArray("milestones")
            ?: throw ExportFormatException("文件缺少 milestones 字段")

        val records = ArrayList<MilestoneEntity>(milestones.length())
        var skipped = 0

        for (i in 0 until milestones.length()) {
            val item = milestones.optJSONObject(i)
            if (item == null) {
                skipped++
                continue
            }

            val content = item.optString("c", "").trim().take(MAX_CONTENT)
            if (content.isEmpty()) {
                skipped++
                continue
            }

            val category = when (val token = item.opt("k")) {
                is Number -> categories.getOrNull(token.toInt()).orEmpty()
                is String -> token
                else -> ""
            }.trim().take(MAX_CATEGORY).ifEmpty { FALLBACK_CATEGORY }

            val epochDay = item.optLong("d", Long.MIN_VALUE)
            if (epochDay == Long.MIN_VALUE || epochDay < MIN_EPOCH_DAY || epochDay > MAX_EPOCH_DAY) {
                skipped++
                continue
            }

            val color = resolveColor(item.opt("h"))
            val createdAtSeconds = item.optLong("a", System.currentTimeMillis() / 1000L)
            val detail = item.optString("t", "").take(MAX_DETAIL)

            records.add(
                MilestoneEntity(
                    id = 0L, // 导入时重新分配主键，避免与本地冲突
                    content = content,
                    category = category,
                    targetDate = epochDay,
                    colorHex = color,
                    detail = detail,
                    createdAt = createdAtSeconds * 1000L,
                    sortOrder = item.optInt("s", 0)
                )
            )
        }

        return ImportPreview(records = records, skipped = skipped, categories = categories)
    }

    private fun resolveColor(token: Any?): String = when (token) {
        is Number -> MilestoneRepository.PRESET_COLORS.getOrNull(token.toInt())
            ?: MilestoneRepository.PRESET_COLORS.first()
        is String -> if (COLOR_REGEX.matches(token)) token.uppercase() else MilestoneRepository.PRESET_COLORS.first()
        else -> MilestoneRepository.PRESET_COLORS.first()
    }
}
