package com.endfield.time.ui.edit

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endfield.time.EndFieldApp
import com.endfield.time.domain.DayCounter
import com.endfield.time.domain.MilestoneUi
import com.endfield.time.ui.components.CategoryMenuContent
import com.endfield.time.ui.components.ChevronDownIcon
import com.endfield.time.ui.components.ColorPickerRow
import com.endfield.time.ui.components.FieldLikeRow
import com.endfield.time.ui.components.showToast
import com.endfield.time.ui.home.MilestoneCard
import com.endfield.time.ui.theme.FieldBackground
import com.endfield.time.ui.theme.InkPrimary
import com.endfield.time.ui.theme.InkSecondary
import com.endfield.time.ui.theme.PageBackground
import com.endfield.time.ui.theme.PlaceholderGray
import com.endfield.time.ui.theme.SkyBlue
import com.endfield.time.ui.viewModelFactoryOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    milestoneId: Long?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as EndFieldApp).container
    val viewModel: EditViewModel = viewModel(
        factory = viewModelFactoryOf { EditViewModel(milestoneId, container.repository) }
    )
    val state by viewModel.ui.collectAsState()

    var categoryPanelOpen by remember { mutableStateOf(false) }
    var categoryQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditEvent.Toast -> showToast(context, event.text)
            }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    val selectedDate: LocalDate? = state.dateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneOffset.UTC).toLocalDate()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = InkPrimary)
            }
            Text(
                text = if (state.isEdit) "编辑里程碑" else "新增里程碑",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SectionLabel("里程碑内容")
            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                singleLine = true,
                placeholder = { Text("例如：坚持早起 100 天", fontSize = 15.sp, color = PlaceholderGray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = SkyBlue,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("分类")
            Box {
                FieldLikeRow(
                    value = state.category,
                    placeholder = "请选择分类（支持搜索）",
                    onClick = { categoryPanelOpen = true },
                    trailing = { ChevronDownIcon() }
                )
                DropdownMenu(
                    expanded = categoryPanelOpen,
                    onDismissRequest = {
                        categoryPanelOpen = false
                        categoryQuery = ""
                    }
                ) {
                    CategoryMenuContent(
                        entries = state.categories.map { it to 0 },
                        selected = state.category,
                        query = categoryQuery,
                        onQueryChange = { categoryQuery = it },
                        showAllRow = false,
                        allowCreate = true,
                        onSelect = {
                            viewModel.onCategoryChange(it)
                            categoryPanelOpen = false
                            categoryQuery = ""
                        },
                        onCreate = {
                            viewModel.onCategoryChange(it)
                            categoryPanelOpen = false
                            categoryQuery = ""
                        }
                    )
                }
            }

            SectionLabel("内容详情（选填）")
            Box {
                OutlinedTextField(
                    value = state.detail,
                    onValueChange = viewModel::onDetailChange,
                    placeholder = {
                        Text("补充背景、计划或心得，1000 字以内…", fontSize = 15.sp, color = PlaceholderGray)
                    },
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FieldBackground,
                        unfocusedContainerColor = FieldBackground,
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${state.detail.length}/${EditViewModel.DETAIL_MAX}",
                    fontSize = 10.sp,
                    color = InkSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp)
                )
            }

            SectionLabel("目标日期")
            FieldLikeRow(
                value = selectedDate?.format(DATE_FORMATTER) ?: "",
                placeholder = "请选择目标日期",
                onClick = { showDatePicker = true },
                trailing = { Text("选择", fontSize = 13.sp, color = SkyBlue) }
            )

            SectionLabel("卡片颜色")
            ColorPickerRow(selected = state.colorHex, onSelect = viewModel::onColorChange)

            SectionLabel("效果预览")
            PreviewCard(state = state, selectedDate = selectedDate)
            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Button(
                onClick = viewModel::save,
                enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("保  存", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMillis ?: EditViewModel.todayMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateChange(pickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("确定", color = SkyBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = InkSecondary)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp)
                .background(SkyBlue, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
    }
}

@Composable
private fun PreviewCard(state: EditUiState, selectedDate: LocalDate?) {
    val date = selectedDate ?: LocalDate.now()
    val dayState = DayCounter.compute(date, LocalDate.now())
    val preview = MilestoneUi(
        id = 0L,
        content = state.content.ifBlank { "里程碑内容预览" },
        category = state.category.ifBlank { "分类" },
        colorHex = state.colorHex,
        targetDateText = date.format(DATE_FORMATTER),
        dayType = dayState.type,
        dayCount = dayState.count,
        dayLabel = dayState.label,
        detail = state.detail
    )
    MilestoneCard(item = preview, aspectRatio = 2.1f, onClick = { }, onLongClick = { })
    Text(
        text = "卡片天数将根据目标日期自动计算",
        fontSize = 10.sp,
        color = InkSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
