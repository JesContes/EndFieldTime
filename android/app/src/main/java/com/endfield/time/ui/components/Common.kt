package com.endfield.time.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.ui.theme.DangerRed
import com.endfield.time.ui.theme.DividerGray
import com.endfield.time.ui.theme.FieldBackground
import com.endfield.time.ui.theme.InkPrimary
import com.endfield.time.ui.theme.InkSecondary
import com.endfield.time.ui.theme.PlaceholderGray
import com.endfield.time.ui.theme.SkyBlue
import com.endfield.time.ui.theme.SkyBlueDeep

fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (e: Exception) {
    SkyBlue
}

fun showToast(context: android.content.Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

/** 半平面风漏斗图标（不依赖 material-icons-extended） */
@Composable
fun FunnelIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        drawFunnel(color)
    }
}

private fun DrawScope.drawFunnel(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.06f, h * 0.16f)
        lineTo(w * 0.94f, h * 0.16f)
        lineTo(w * 0.62f, h * 0.52f)
        lineTo(w * 0.62f, h * 0.90f)
        lineTo(w * 0.38f, h * 0.90f)
        lineTo(w * 0.38f, h * 0.52f)
        close()
    }
    drawPath(path, color)
}

/** 输入框样式的可点击行（用于分类下拉 / 日期选择，避免只读 TextField 的焦点问题） */
@Composable
fun FieldLikeRow(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FieldBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.ifBlank { placeholder },
            color = if (value.isBlank()) PlaceholderGray else InkPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}

@Composable
fun ChevronDownIcon() {
    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        tint = InkSecondary,
        modifier = Modifier.size(22.dp)
    )
}

/**
 * 分类搜索面板内容：首页筛选与表单分类选择共用。
 * @param entries 分类 + 记录数
 * @param showAllRow 首页模式为 true（置顶「全部」+ 重置）
 * @param allowCreate 表单模式为 true（搜索词可一键新建分类）
 */
@Composable
fun CategoryMenuContent(
    entries: List<Pair<String, Int>>,
    selected: String,
    query: String,
    onQueryChange: (String) -> Unit,
    showAllRow: Boolean,
    allowCreate: Boolean,
    onSelect: (String) -> Unit,
    onCreate: (String) -> Unit
) {
    val keyword = query.trim()
    val filtered = if (keyword.isEmpty()) entries
    else entries.filter { it.first.contains(keyword, ignoreCase = true) }

    Column(modifier = Modifier.width(290.dp).padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text("搜索分类", fontSize = 14.sp, color = PlaceholderGray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = InkSecondary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清空",
                        tint = InkSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 4.dp)
        )

        if (showAllRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(MilestoneRepository.ALL_CATEGORY) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FunnelIcon(color = if (selected == MilestoneRepository.ALL_CATEGORY) SkyBlue else SkyBlueDeep)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = MilestoneRepository.ALL_CATEGORY,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text("重置", fontSize = 11.sp, color = InkSecondary)
                if (selected == MilestoneRepository.ALL_CATEGORY) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Check, contentDescription = null, tint = SkyBlueDeep, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = DividerGray)
        }

        if (filtered.isEmpty()) {
            Text(
                text = if (allowCreate) "无匹配分类，可在下方新建" else "无匹配分类",
                fontSize = 13.sp,
                color = InkSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        } else {
            // 注意：DropdownMenu 内部不能放 LazyColumn(SubcomposeLayout)，否则请求 intrinsic 尺寸会崩溃
            Column(
                modifier = Modifier
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                filtered.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(entry.first) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = highlightMatch(entry.first, keyword),
                            fontSize = 15.sp,
                            color = InkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (entry.second > 0) {
                            Text("${entry.second} 条", fontSize = 11.sp, color = InkSecondary)
                        }
                        if (selected == entry.first) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Check, contentDescription = null, tint = SkyBlueDeep, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        if (allowCreate && keyword.isNotEmpty() && filtered.none { it.first == keyword }) {
            HorizontalDivider(color = DividerGray)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreate(keyword) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "＋ 新建分类「$keyword」",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkyBlueDeep
                )
            }
        }
    }
}

/** 命中字符主色高亮 */
private fun highlightMatch(text: String, keyword: String): AnnotatedString {
    if (keyword.isEmpty()) return AnnotatedString(text)
    val index = text.indexOf(keyword, ignoreCase = true)
    if (index < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, index))
        withStyle(SpanStyle(color = SkyBlueDeep, fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + keyword.length))
        }
        append(text.substring(index + keyword.length))
    }
}

/** 8 色卡片色板 */
@Composable
fun ColorPickerRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MilestoneRepository.PRESET_COLORS.forEach { hex ->
            val color = hex.toComposeColor()
            val isSelected = hex.equals(selected, ignoreCase = true)
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 38.dp else 32.dp)
                        .background(if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteMenuRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("删除", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DangerRed)
    }
}
