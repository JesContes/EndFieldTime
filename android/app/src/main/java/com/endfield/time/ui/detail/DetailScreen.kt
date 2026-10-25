package com.endfield.time.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endfield.time.EndFieldApp
import com.endfield.time.ui.components.showToast
import com.endfield.time.ui.home.MilestoneCard
import com.endfield.time.ui.theme.DividerGray
import com.endfield.time.ui.theme.InkPrimary
import com.endfield.time.ui.theme.InkSecondary
import com.endfield.time.ui.theme.PageBackground
import com.endfield.time.ui.theme.SkyBlue
import com.endfield.time.ui.theme.SkyBlueDeep
import com.endfield.time.ui.viewModelFactoryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    milestoneId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as EndFieldApp).container
    val viewModel: DetailViewModel = viewModel(
        factory = viewModelFactoryOf {
            DetailViewModel(milestoneId, container.repository, container.todayFlow)
        }
    )
    val state by viewModel.ui.collectAsState()
    var showContentSheet by rememberSaveable { mutableStateOf(false) }

    val item = state.milestone

    // 记录被删除后返回列表
    LaunchedEffect(state.loading, item?.id) {
        if (!state.loading && item == null) {
            showToast(context, "记录已不存在")
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = InkPrimary)
            }
            Text(
                text = "里程碑详情",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { item?.let { onEdit(it.id) } }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = SkyBlueDeep)
            }
        }

        if (item != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                MilestoneCard(
                    item = item,
                    aspectRatio = 1.9f,
                    onClick = { },
                    onLongClick = { }
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    InfoRow("分类", item.category)
                    HorizontalDivider(color = DividerGray)
                    InfoRow("目标日期", item.targetDateText)
                    HorizontalDivider(color = DividerGray)
                    InfoRow(
                        label = "天数状态",
                        value = if (item.dayType == com.endfield.time.domain.DayType.TODAY) "就是今天"
                        else "${item.dayLabel} ${item.dayCount} 天",
                        valueColor = SkyBlueDeep,
                        emphasize = true
                    )
                    HorizontalDivider(color = DividerGray)
                    InfoRow(
                        "内容详情",
                        if (item.hasDetail) "已填写 · ${item.detail.length} 字" else "未填写"
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "提示：长按首页卡片可快速删除",
                    fontSize = 11.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showContentSheet = true },
                    enabled = item.hasDetail,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看内容详情", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEdit(item.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("编 辑", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("加载中…", color = InkSecondary, fontSize = 14.sp)
            }
        }
    }

    val sheetItem = item?.takeIf { it.hasDetail }
    if (showContentSheet && sheetItem != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showContentSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "内容详情",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = InkSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showContentSheet = false }
                    )
                }
                Text(
                    text = "来自里程碑「${sheetItem.content}」",
                    fontSize = 11.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                HorizontalDivider(color = DividerGray)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = sheetItem.detail,
                        fontSize = 15.sp,
                        color = InkPrimary,
                        lineHeight = 26.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showContentSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关 闭", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = InkPrimary,
    emphasize: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = InkSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
