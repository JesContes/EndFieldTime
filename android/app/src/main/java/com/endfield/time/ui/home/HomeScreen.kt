package com.endfield.time.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.endfield.time.EndFieldApp
import com.endfield.time.data.MilestoneRepository
import com.endfield.time.domain.MilestoneUi
import com.endfield.time.ui.UiEvent
import com.endfield.time.ui.components.CategoryMenuContent
import com.endfield.time.ui.components.ChevronDownIcon
import com.endfield.time.ui.components.FunnelIcon
import com.endfield.time.ui.components.showToast
import com.endfield.time.ui.theme.DangerRed
import com.endfield.time.ui.theme.DividerGray
import com.endfield.time.ui.theme.FieldBackground
import com.endfield.time.ui.theme.InkPrimary
import com.endfield.time.ui.theme.InkSecondary
import com.endfield.time.ui.theme.PageBackground
import com.endfield.time.ui.theme.SkyBlue
import com.endfield.time.ui.theme.SkyBlueDeep
import com.endfield.time.ui.transfer.TransferViewModel

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpenDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as EndFieldApp).container
    val viewModel: HomeViewModel = viewModel(
        factory = com.endfield.time.ui.viewModelFactoryOf {
            HomeViewModel(container.repository, container.settingsStore, container.todayFlow)
        }
    )
    val state by viewModel.ui.collectAsState()

    val transferViewModel: TransferViewModel = viewModel(
        factory = com.endfield.time.ui.viewModelFactoryOf {
            TransferViewModel(container.repository)
        }
    )
    val transferState by transferViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> showToast(context, event.text)
            }
        }
    }

    LaunchedEffect(Unit) {
        transferViewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> showToast(context, event.text)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) transferViewModel.export(context.contentResolver, uri)
    }
    val exportGzipLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri ->
        if (uri != null) transferViewModel.export(context.contentResolver, uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) transferViewModel.prepareImport(context.contentResolver, uri)
    }

    var panelOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menuForId by remember { mutableStateOf<Long?>(null) }
    var pendingDelete by remember { mutableStateOf<MilestoneUi?>(null) }
    var overflowOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PageBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = SkyBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增里程碑")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HomeHeader(
                trailing = {
                    Box {
                        IconButton(onClick = { overflowOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = InkSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = overflowOpen,
                            onDismissRequest = { overflowOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出数据（JSON）", fontSize = 15.sp) },
                                onClick = {
                                    overflowOpen = false
                                    exportLauncher.launch(exportFileName(gzip = false))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("导出数据（压缩 .gz）", fontSize = 15.sp) },
                                onClick = {
                                    overflowOpen = false
                                    exportGzipLauncher.launch(exportFileName(gzip = true))
                                }
                            )
                            HorizontalDivider(color = DividerGray)
                            DropdownMenuItem(
                                text = { Text("导入数据…", fontSize = 15.sp) },
                                onClick = {
                                    overflowOpen = false
                                    importLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                }
            )

            FilterRow(
                selected = state.selectedCategory,
                totalCount = state.totalCount,
                onOpenPanel = { panelOpen = true },
                onReset = {
                    viewModel.selectCategory(MilestoneRepository.ALL_CATEGORY)
                },
                panelOpen = panelOpen,
                onDismissPanel = {
                    panelOpen = false
                    query = ""
                },
                panelContent = {
                    CategoryMenuContent(
                        entries = state.categoryEntries().map { it.name to it.count },
                        selected = state.selectedCategory,
                        query = query,
                        onQueryChange = { query = it },
                        showAllRow = true,
                        allowCreate = false,
                        onSelect = { category ->
                            viewModel.selectCategory(category)
                            panelOpen = false
                            query = ""
                        },
                        onCreate = { }
                    )
                }
            )

            if (state.isEmpty) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    hasAnyRecord = state.totalCount > 0 || state.selectedCategory != MilestoneRepository.ALL_CATEGORY
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.milestones, key = { it.id }) { item ->
                        MilestoneCard(
                            item = item,
                            menuExpanded = menuForId == item.id,
                            onDismissMenu = { menuForId = null },
                            onDelete = {
                                menuForId = null
                                pendingDelete = item
                            },
                            onClick = { onOpenDetail(item.id) },
                            onLongClick = { menuForId = item.id }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除里程碑", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除「${target.content}」吗？删除后将无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
                    pendingDelete = null
                }) {
                    Text("删除", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消", color = InkSecondary)
                }
            }
        )
    }

    // 导入确认：解析后先让用户选择「合并」或「覆盖」
    transferState.pending?.let { preview ->
        val duplicated = transferState.existingInPending
        AlertDialog(
            onDismissRequest = { transferViewModel.dismissImport() },
            title = { Text("导入数据", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("解析到 ${preview.count} 条记录。")
                    if (duplicated > 0) {
                        Text(
                            "其中 $duplicated 条与本机已有记录重复（内容 + 分类 + 日期相同）。",
                            color = InkSecondary,
                            fontSize = 13.sp
                        )
                    }
                    if (preview.skipped > 0) {
                        Text(
                            "已忽略 ${preview.skipped} 条无效数据。",
                            color = InkSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        "「合并」只新增不重复的记录；「覆盖」会先清空本机全部记录。",
                        color = InkSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { transferViewModel.confirmImport(replace = false) }) {
                    Text("合并", color = SkyBlueDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { transferViewModel.dismissImport() }) {
                        Text("取消", color = InkSecondary)
                    }
                    TextButton(onClick = { transferViewModel.confirmImport(replace = true) }) {
                        Text("覆盖", color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

/** 导出文件名：时记-yyyyMMdd-HHmm.json[.gz] */
private fun exportFileName(gzip: Boolean): String {
    val stamp = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    return if (gzip) "时记-$stamp.json.gz" else "时记-$stamp.json"
}

@Composable
private fun HomeHeader(trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "时记",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = "记录每一个重要时刻",
                fontSize = 12.sp,
                color = InkSecondary
            )
        }
        trailing()
    }
}

@Composable
private fun FilterRow(
    selected: String,
    totalCount: Int,
    onOpenPanel: () -> Unit,
    onReset: () -> Unit,
    panelOpen: Boolean,
    onDismissPanel: () -> Unit,
    panelContent: @Composable () -> Unit
) {
    val isAll = selected == MilestoneRepository.ALL_CATEGORY

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isAll) Color.White else SkyBlue)
                    .clickable { onOpenPanel() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FunnelIcon(color = if (isAll) SkyBlueDeep else Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selected,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAll) InkPrimary else Color.White
                )
                Spacer(Modifier.width(6.dp))
                if (isAll) {
                    ChevronDownIcon()
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "重置为全部",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onReset)
                    )
                }
            }

            DropdownMenu(expanded = panelOpen, onDismissRequest = onDismissPanel) {
                panelContent()
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "共 $totalCount 条记录",
            fontSize = 11.sp,
            color = InkSecondary
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, hasAnyRecord: Boolean) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(FieldBackground, RoundedCornerShape(20.dp))
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (hasAnyRecord) "该分类下还没有里程碑" else "还没有里程碑",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = InkPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "点右下角 ＋ 记录一个吧",
            fontSize = 12.sp,
            color = InkSecondary
        )
    }
}
