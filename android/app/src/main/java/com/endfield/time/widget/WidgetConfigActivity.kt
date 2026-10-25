package com.endfield.time.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endfield.time.EndFieldApp
import com.endfield.time.MainActivity
import com.endfield.time.domain.DayCounter
import com.endfield.time.ui.components.toComposeColor
import com.endfield.time.ui.theme.DividerGray
import com.endfield.time.ui.theme.InkPrimary
import com.endfield.time.ui.theme.InkSecondary
import com.endfield.time.ui.theme.PageBackground
import java.time.LocalDate

/**
 * 添加小组件时的配置页：选择要显示哪一条里程碑。
 * 未选择直接返回 → RESULT_CANCELED（不创建小组件）。
 */
class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // 官方建议：默认返回取消，用户显式选择后再设为 OK
        setResult(RESULT_CANCELED)

        setContent {
            com.endfield.time.ui.theme.EndFieldTimeTheme {
                WidgetConfigScreen(
                    onPick = { milestoneId -> bind(widgetId, milestoneId) },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun bind(widgetId: Int, milestoneId: Long) {
        WidgetBindings.bind(this, widgetId, milestoneId)
        (application as EndFieldApp).container.widgetUpdater.refresh(intArrayOf(widgetId))
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@Composable
private fun WidgetConfigScreen(onPick: (Long) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as EndFieldApp).container
    val records by container.repository
        .observe(com.endfield.time.data.MilestoneRepository.ALL_CATEGORY)
        .collectAsState(initial = emptyList())
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp)) {
            Text(
                text = "选择要显示的里程碑",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = "小组件会展示它的天数，点按可打开应用",
                fontSize = 12.sp,
                color = InkSecondary
            )
        }

        Spacer(Modifier.height(12.dp))

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("还没有里程碑", fontSize = 15.sp, color = InkPrimary)
                Spacer(Modifier.height(6.dp))
                Text("先在应用里添加一个吧", fontSize = 12.sp, color = InkSecondary)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    onCancel()
                }) {
                    Text("打开时记", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(records, key = { it.id }) { entity ->
                    val ui = DayCounter.toUi(entity, today)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(entity.id) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(ui.colorHex.toComposeColor(), CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ui.content,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkPrimary
                            )
                            Text(
                                text = "${ui.category} · ${ui.targetDateText}",
                                fontSize = 11.sp,
                                color = InkSecondary
                            )
                        }
                        Text(
                            text = if (ui.dayType == com.endfield.time.domain.DayType.TODAY) "今天"
                            else "${ui.dayLabel} ${ui.dayCount} 天",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ui.colorHex.toComposeColor()
                        )
                    }
                    HorizontalDivider(color = DividerGray, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp)
        ) {
            Text("取消", color = InkSecondary)
        }
    }
}
