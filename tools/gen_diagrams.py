# -*- coding: utf-8 -*-
"""终末时记 · 功能流程图 + 时序图生成脚本"""
import math
from PIL import Image, ImageDraw, ImageFont

FONT = "C:/Windows/Fonts/msyh.ttc"
FONT_B = "C:/Windows/Fonts/msyhbd.ttc"

def F(size, bold=False):
    return ImageFont.truetype(FONT_B if bold else FONT, size)

PRIMARY = "#66CCFF"
PRIMARY_DEEP = "#3FA9F5"
INK = "#22313F"
INK2 = "#5A6B78"
ARROW = "#8FA6B8"
DECIDE_BG = "#FFF6E8"
DECIDE_LINE = "#FFB74D"
OUT = r"D:\projects\EndFieldTime\docs\images"

# ---------------- 基础绘图 ----------------
def seg(d, p1, p2, color, width=5, dashed=False):
    if not dashed:
        d.line([p1, p2], fill=color, width=width)
        return
    x1, y1 = p1; x2, y2 = p2
    length = math.hypot(x2 - x1, y2 - y1)
    if length == 0:
        return
    dx, dy = (x2 - x1) / length, (y2 - y1) / length
    t = 0.0
    while t < length:
        t2 = min(t + 14, length)
        d.line([(x1 + dx * t, y1 + dy * t), (x1 + dx * t2, y1 + dy * t2)],
               fill=color, width=width)
        t = t2 + 10

def arrow(d, pts, color=ARROW, width=5, dashed=False, head=18):
    for i in range(len(pts) - 1):
        seg(d, pts[i], pts[i + 1], color, width, dashed)
    (x1, y1), (x2, y2) = pts[-2], pts[-1]
    ang = math.atan2(y2 - y1, x2 - x1)
    p1 = (x2 + head * math.cos(ang + math.pi - 0.42),
          y2 + head * math.sin(ang + math.pi - 0.42))
    p2 = (x2 + head * math.cos(ang + math.pi + 0.42),
          y2 + head * math.sin(ang + math.pi + 0.42))
    d.polygon([(x2, y2), p1, p2], fill=color)

def box_text(d, cx, cy, lines, font, fill):
    lh = font.size + 14
    y0 = cy - lh * (len(lines) - 1) / 2
    for i, ln in enumerate(lines):
        d.text((cx, y0 + i * lh), ln, font=font, fill=fill, anchor="mm")

def label(d, x, y, text, size=26):
    d.text((x, y), text, font=F(size), fill=INK2, anchor="mm")

# ---------------- 流程图元素 ----------------
def fc_start(d, box, text):
    x1, y1, x2, y2 = box
    d.rounded_rectangle(box, radius=(y2 - y1) // 2, fill=PRIMARY)
    box_text(d, (x1 + x2) // 2, (y1 + y2) // 2, [text], F(30, True), "#FFFFFF")

def fc_proc(d, box, lines):
    d.rounded_rectangle(box, radius=18, fill="#FFFFFF", outline=PRIMARY_DEEP, width=4)
    box_text(d, (box[0] + box[2]) // 2, (box[1] + box[3]) // 2, lines, F(30), INK)

def fc_decide(d, box, text):
    x1, y1, x2, y2 = box
    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
    pts = [(cx, y1), (x2, cy), (cx, y2), (x1, cy)]
    d.polygon(pts, fill=DECIDE_BG, outline=DECIDE_LINE)
    d.line([pts[0], pts[1]], fill=DECIDE_LINE, width=4)
    d.line([pts[1], pts[2]], fill=DECIDE_LINE, width=4)
    d.line([pts[2], pts[3]], fill=DECIDE_LINE, width=4)
    d.line([pts[3], pts[0]], fill=DECIDE_LINE, width=4)
    box_text(d, cx, cy, [text], F(30, True), INK)

def flowchart():
    W, H = 2500, 2360
    img = Image.new("RGBA", (W, H), "#FFFFFF")
    d = ImageDraw.Draw(img)
    d.text((80, 66), "终末时记 · 功能流程图", font=F(48, True), fill=INK)
    d.text((80, 136), "启动加载 → 渲染列表 → 用户操作(分类过滤 / 新增 / 查看详情 / 长按删除)→ 数据驱动自动刷新",
           font=F(26), fill=INK2)

    # 图例
    d.rounded_rectangle([1860, 56, 2460, 214], radius=16, fill="#FAFDFF",
                        outline="#D8E3EC", width=2)
    d.rounded_rectangle([1890, 76, 1980, 112], radius=18, fill=PRIMARY)
    d.text((2002, 94), "开始 / 结束", font=F(24), fill=INK, anchor="lm")
    d.rounded_rectangle([1890, 124, 1980, 160], radius=8, fill="#FFFFFF",
                        outline=PRIMARY_DEEP, width=3)
    d.text((2002, 142), "处理步骤", font=F(24), fill=INK, anchor="lm")
    d.polygon([(1935, 172), (1975, 188), (1935, 204), (1895, 188)],
              fill=DECIDE_BG, outline=DECIDE_LINE)
    d.text((2002, 188), "条件判断", font=F(24), fill=INK, anchor="lm")

    # 主流程(脊柱)
    fc_start(d, (90, 230, 430, 320), "开始 · 启动应用")
    arrow(d, [(260, 320), (260, 370)])
    fc_proc(d, (70, 370, 450, 490), ["初始化 Room", "本地数据库"])
    arrow(d, [(260, 490), (260, 540)])
    fc_proc(d, (70, 540, 450, 660), ["读取里程碑记录", "(Flow 监听数据变更)"])
    arrow(d, [(260, 660), (260, 710)])
    fc_proc(d, (70, 710, 450, 830), ["计算每条记录", "与当天的天数差"])
    arrow(d, [(260, 830), (260, 880)])
    fc_proc(d, (70, 880, 450, 1000), ["渲染里程碑卡片列表"])
    arrow(d, [(260, 1000), (260, 1070)])
    fc_decide(d, (70, 1070, 450, 1280), "用户操作?")

    # 分支总线
    arrow(d, [(260, 1280), (260, 1350)])
    seg(d, (260, 1350), (2040, 1350), ARROW, 5)
    label(d, 700, 1316, "点击筛选按钮")
    label(d, 1120, 1316, "点击 ＋ 新增")
    label(d, 1580, 1316, "单击卡片")
    label(d, 2040, 1316, "长按卡片")
    arrow(d, [(700, 1350), (700, 1430)])
    arrow(d, [(1120, 1350), (1120, 1430)])
    arrow(d, [(1580, 1350), (1580, 1430)])
    arrow(d, [(2040, 1350), (2040, 1430)])

    # 分支一:分类过滤
    fc_proc(d, (540, 1430, 860, 1550), ["更新过滤条件", "并重新查询"])

    # 分支二:新增
    fc_proc(d, (940, 1430, 1300, 1550), ["填写内容、分类、日期", "颜色与内容详情"])
    arrow(d, [(1120, 1550), (1120, 1610)])
    fc_decide(d, (940, 1610, 1300, 1790), "校验通过?")
    arrow(d, [(1120, 1790), (1120, 1870)])          # 是
    label(d, 1152, 1824, "是")
    fc_proc(d, (940, 1870, 1300, 1990), ["写入 Room(INSERT)"])
    arrow(d, [(940, 1700), (900, 1700)])            # 否 → 提示
    label(d, 918, 1666, "否")
    fc_proc(d, (560, 1640, 900, 1760), ["Toast 提示错误原因"])
    arrow(d, [(730, 1640), (730, 1580), (1120, 1580), (1120, 1550)])  # 回到填写

    # 分支三:单击卡片 → 卡片详情(子流程)
    fc_proc(d, (1400, 1430, 1760, 1590),
            ["打开卡片详情页", "查看内容详情 / 编辑", "(详见子流程图)"])
    seg(d, (1580, 1590), (1580, 2130), ARROW, 5)
    d.text((1612, 1830), "返回主列表", font=F(24), fill=INK2, anchor="lm")

    # 分支四:长按删除
    fc_decide(d, (1860, 1430, 2220, 1610), "确认删除?")
    arrow(d, [(2040, 1610), (2040, 1690)])          # 是
    label(d, 2072, 1644, "是")
    fc_proc(d, (1860, 1690, 2220, 1810), ["从 Room 删除记录"])
    arrow(d, [(2220, 1520), (2290, 1520), (2290, 2130)], dashed=True)  # 取消
    label(d, 2290, 1570, "取消")

    # 汇聚回流
    seg(d, (545, 1550), (545, 2130), ARROW, 5)
    seg(d, (1120, 1990), (1120, 2130), ARROW, 5)
    seg(d, (2040, 1810), (2040, 2130), ARROW, 5)
    seg(d, (2290, 2130), (40, 2130), ARROW, 5)
    seg(d, (40, 2130), (40, 940), ARROW, 5)
    arrow(d, [(40, 940), (70, 940)])
    label(d, 1165, 2172, "数据变更后由 Flow 自动发射最新列表,回到列表渲染,循环等待下一次操作")
    d.text((80, 2250), "注:应用以前台驻留方式循环等待用户操作;退出应用后流程结束,数据已持久化在本地 Room 数据库。",
           font=F(24), fill=INK2)
    d.text((80, 2300), "注:「单击卡片」打开的卡片详情页内的查看详情 / 编辑操作,见下方「卡片详情子流程图」。",
           font=F(24), fill=INK2)
    img.convert("RGB").save(OUT + r"\flowchart.png")
    print("flowchart ok")

def flowchart_detail():
    """卡片详情子流程:查看内容详情 / 编辑 / 返回"""
    W, H = 1860, 1660
    img = Image.new("RGBA", (W, H), "#FFFFFF")
    d = ImageDraw.Draw(img)
    d.text((70, 60), "卡片详情 · 子流程图", font=F(44, True), fill=INK)
    d.text((70, 122), "单击卡片进入详情页后的操作:查看内容详情 / 编辑 / 返回",
           font=F(24), fill=INK2)

    fc_start(d, (90, 180, 430, 270), "进入卡片详情页")
    arrow(d, [(260, 270), (260, 330)])
    fc_proc(d, (70, 330, 450, 450), ["展示完整卡片与信息", "(分类/日期/天数)"])
    arrow(d, [(260, 450), (260, 510)])
    fc_decide(d, (70, 510, 450, 720), "用户操作?")

    # 分支总线
    arrow(d, [(260, 720), (260, 780)])
    seg(d, (260, 780), (1580, 780), ARROW, 5)
    label(d, 700, 756, "查看内容详情")
    label(d, 1120, 756, "点击「编辑」")
    label(d, 1580, 756, "点击「返回」")
    arrow(d, [(700, 780), (700, 840)])
    arrow(d, [(1120, 780), (1120, 840)])
    arrow(d, [(1580, 780), (1580, 840)])

    # 查看内容详情
    fc_proc(d, (540, 840, 860, 960), ["弹出内容详情页", "(滚动展示全文)"])
    arrow(d, [(700, 960), (700, 1020)])
    fc_proc(d, (540, 1020, 860, 1140), ["点击「关闭」"])
    arrow(d, [(620, 1140), (620, 1240), (40, 1240), (40, 390), (70, 390)])

    # 编辑
    fc_proc(d, (940, 840, 1300, 960), ["回填现有数据到表单"])
    arrow(d, [(1120, 960), (1120, 1020)])
    fc_proc(d, (940, 1020, 1300, 1140), ["修改内容并点击保存"])
    arrow(d, [(1120, 1140), (1120, 1200)])
    fc_decide(d, (940, 1200, 1300, 1380), "校验通过?")
    arrow(d, [(1120, 1380), (1120, 1440)])          # 是
    label(d, 1148, 1404, "是")
    fc_proc(d, (940, 1440, 1300, 1560), ["写入 Room(UPDATE)"])
    arrow(d, [(1300, 1290), (1340, 1290)])          # 否 → 提示
    label(d, 1322, 1255, "否")
    fc_proc(d, (1340, 1230, 1700, 1350), ["Toast 提示错误原因"])
    arrow(d, [(1520, 1230), (1520, 1080), (1300, 1080)])  # 回到修改
    seg(d, (1120, 1560), (1120, 1590), ARROW, 5)    # 编辑完成回流
    seg(d, (1120, 1590), (40, 1590), ARROW, 5)
    seg(d, (40, 1590), (40, 390), ARROW, 5)

    # 返回
    fc_start(d, (1400, 840, 1760, 940), "返回主列表")

    d.text((70, 1600), "注:「查看内容详情」仅当详情非空时可用;编辑保存成功后返回详情页并自动刷新数据。",
           font=F(22), fill=INK2)
    img.convert("RGB").save(OUT + r"\flowchart-02-detail.png")
    print("flowchart-02 ok")

# ---------------- 时序图元素 ----------------
LIFES = [
    ("用户", "actor"),
    ("主界面\nCompose UI", "ui"),
    ("Milestone\nViewModel", "box"),
    ("Milestone\nRepository", "box"),
    ("MilestoneDao\n(Room)", "box"),
    ("SQLite\n数据库", "db"),
]
XS = [150, 450, 750, 1050, 1350, 1650]

def seq_header(d, W, title, sub):
    d.text((70, 60), title, font=F(44, True), fill=INK)
    d.text((70, 122), sub, font=F(24), fill=INK2)
    for (name, kind), x in zip(LIFES, XS):
        if kind == "ui":
            fill, outline, tc = PRIMARY, None, "#FFFFFF"
        elif kind == "actor":
            fill, outline, tc = PRIMARY_DEEP, None, "#FFFFFF"
        elif kind == "db":
            fill, outline, tc = "#E3F4FE", PRIMARY_DEEP, INK
        else:
            fill, outline, tc = "#FFFFFF", PRIMARY_DEEP, INK
        d.rounded_rectangle([x - 135, 150, x + 135, 245], radius=14, fill=fill,
                            outline=outline, width=3 if outline else 1)
        lines = name.split("\n")
        box_text(d, x, 197, lines, F(27, True), tc)

def lifelines(d, H):
    for x in XS:
        seg(d, (x, 255), (x, H - 60), "#B9C9D6", 3, dashed=True)

def section(d, W, y, text):
    d.text((70, y), text, font=F(28, True), fill=PRIMARY_DEEP)
    d.line([(60, y + 34), (W - 60, y + 34)], fill="#DDE9F2", width=3)

def msg(d, i, j, y, text, ret=False):
    x1, x2 = XS[i], XS[j]
    arrow(d, [(x1, y), (x2, y)], color=PRIMARY_DEEP if not ret else "#9AB0C0",
          width=4, dashed=ret, head=15)
    d.text(((x1 + x2) / 2, y - 10), text, font=F(24), fill=INK, anchor="md")

def self_msg(d, i, y, text):
    x = XS[i]
    arrow(d, [(x, y), (x + 72, y), (x + 72, y + 36), (x + 8, y + 36)],
          color=PRIMARY_DEEP, width=4, head=15)
    d.text((x + 86, y + 18), text, font=F(24), fill=INK, anchor="lm")

def caption(d, H):
    d.text((70, H - 46), "注:实线箭头为调用消息,虚线箭头为返回消息;前缀数字为消息顺序。",
           font=F(22), fill=INK2)

def seq_load_filter():
    W, H = 1900, 2420
    img = Image.new("RGBA", (W, H), "#FFFFFF")
    d = ImageDraw.Draw(img)
    seq_header(d, W, "时序图 ① 应用启动加载 与 分类过滤", "参与者:用户 / Compose 主界面 / ViewModel / Repository / Room DAO / SQLite")
    lifelines(d, H)
    section(d, W, 290, "阶段一:启动加载")
    y = 360
    msgs1 = [
        ("m", 0, 1, "1: 打开应用"),
        ("m", 1, 2, "2: 订阅里程碑列表(全部)"),
        ("m", 2, 3, "3: getMilestones(全部)"),
        ("m", 3, 4, "4: query(全部)"),
        ("m", 4, 5, "5: SELECT * FROM milestone"),
        ("r", 5, 4, "6: 结果集"),
        ("r", 4, 3, "7: Flow<List<Milestone>>"),
        ("r", 3, 2, "8: 持续发射最新列表"),
        ("s", 2, None, "9: 计算天数差,组装 UiState"),
        ("r", 2, 1, "10: 更新 UiState"),
        ("m", 1, 0, "11: 渲染彩色卡片列表"),
    ]
    for kind, a, b, t in msgs1:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    section(d, W, y + 16, "阶段二:分类过滤")
    y += 86
    msgs2 = [
        ("m", 0, 1, "12: 筛选面板中选中「生活」"),
        ("m", 1, 2, "13: setCategory(生活)"),
        ("m", 2, 3, "14: getMilestones(生活)"),
        ("m", 3, 4, "15: query(生活)"),
        ("m", 4, 5, "16: SELECT ... WHERE category=?"),
        ("r", 5, 4, "17: 结果集"),
        ("r", 4, 3, "18: Flow 发射过滤结果"),
        ("r", 3, 2, "19: 新列表"),
        ("s", 2, None, "20: 重新计算天数差"),
        ("r", 2, 1, "21: 更新 UiState"),
        ("m", 1, 0, "22: 展示过滤后列表"),
    ]
    for kind, a, b, t in msgs2:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    caption(d, H)
    img.convert("RGB").save(OUT + r"\seq-01-load-filter.png")
    print("seq-01 ok, last y =", y)

def seq_add_delete():
    W, H = 1900, 2680
    img = Image.new("RGBA", (W, H), "#FFFFFF")
    d = ImageDraw.Draw(img)
    seq_header(d, W, "时序图 ② 新增里程碑 与 长按删除", "参与者:用户 / Compose 主界面 / ViewModel / Repository / Room DAO / SQLite")
    lifelines(d, H)
    section(d, W, 290, "阶段一:新增里程碑")
    y = 360
    msgs1 = [
        ("m", 0, 1, "1: 点击右下角「＋」"),
        ("s", 1, None, "2: 打开新增里程碑页面"),
        ("m", 0, 1, "3: 填写内容/分类/日期/颜色,点击保存"),
        ("m", 1, 2, "4: onSave(input)"),
        ("s", 2, None, "5: 校验:内容非空/日期合法/颜色有效"),
        ("m", 2, 3, "6: insert(milestone)"),
        ("m", 3, 4, "7: insert(entity)"),
        ("m", 4, 5, "8: INSERT INTO milestone"),
        ("r", 5, 4, "9: 写入成功(rowId)"),
        ("r", 4, 3, "10: Flow 发射新列表"),
        ("r", 3, 2, "11: 新列表 + 保存成功事件"),
        ("r", 2, 1, "12: 关闭页面,刷新列表"),
        ("m", 1, 0, "13: Toast「保存成功」"),
    ]
    for kind, a, b, t in msgs1:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    section(d, W, y + 16, "阶段二:长按删除")
    y += 86
    msgs2 = [
        ("m", 0, 1, "14: 长按某个卡片"),
        ("m", 1, 0, "15: 弹出删除确认框"),
        ("m", 0, 1, "16: 点击「删除」"),
        ("m", 1, 2, "17: onDeleteConfirmed(id)"),
        ("m", 2, 3, "18: deleteById(id)"),
        ("m", 3, 4, "19: deleteById(id)"),
        ("m", 4, 5, "20: DELETE ... WHERE id=?"),
        ("r", 5, 4, "21: 删除成功"),
        ("r", 4, 3, "22: Flow 发射新列表"),
        ("r", 3, 2, "23: 新列表"),
        ("r", 2, 1, "24: 刷新列表"),
        ("m", 1, 0, "25: Toast「已删除」"),
    ]
    for kind, a, b, t in msgs2:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    caption(d, H)
    img.convert("RGB").save(OUT + r"\seq-02-add-delete.png")
    print("seq-02 ok, last y =", y)

def seq_detail_edit():
    W, H = 1900, 3030
    img = Image.new("RGBA", (W, H), "#FFFFFF")
    d = ImageDraw.Draw(img)
    seq_header(d, W, "时序图 ③ 卡片详情 · 查看内容详情 · 编辑", "参与者:用户 / Compose 主界面 / ViewModel / Repository / Room DAO / SQLite")
    lifelines(d, H)
    section(d, W, 290, "阶段一:打开卡片详情")
    y = 360
    msgs1 = [
        ("m", 0, 1, "1: 单击卡片"),
        ("m", 1, 2, "2: openDetail(id)"),
        ("m", 2, 3, "3: getById(id)"),
        ("m", 3, 4, "4: selectById(id)"),
        ("m", 4, 5, "5: SELECT ... WHERE id=?"),
        ("r", 5, 4, "6: 记录实体"),
        ("r", 4, 3, "7: 实体"),
        ("r", 3, 2, "8: 实体"),
        ("s", 2, None, "9: 组装详情 UiState"),
        ("r", 2, 1, "10: 更新详情 UiState"),
        ("m", 1, 0, "11: 渲染卡片详情页"),
    ]
    for kind, a, b, t in msgs1:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    section(d, W, y + 16, "阶段二:查看内容详情")
    y += 86
    msgs2 = [
        ("m", 0, 1, "12: 点击「查看内容详情」"),
        ("s", 1, None, "13: 弹出内容详情页(全文滚动)"),
        ("m", 0, 1, "14: 点击「关闭」"),
        ("s", 1, None, "15: 关闭弹层,返回详情页"),
    ]
    for kind, a, b, t in msgs2:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    section(d, W, y + 16, "阶段三:编辑并保存")
    y += 86
    msgs3 = [
        ("m", 0, 1, "16: 点击「编辑」"),
        ("s", 1, None, "17: 打开编辑页(回填数据)"),
        ("m", 0, 1, "18: 修改内容并点击「保存」"),
        ("m", 1, 2, "19: onSaveEdit(input)"),
        ("s", 2, None, "20: 校验(含详情≤1000字)"),
        ("m", 2, 3, "21: update(milestone)"),
        ("m", 3, 4, "22: update(entity)"),
        ("m", 4, 5, "23: UPDATE milestone ..."),
        ("r", 5, 4, "24: 更新成功"),
        ("r", 4, 3, "25: Flow 发射新列表"),
        ("r", 3, 2, "26: 新列表"),
        ("r", 2, 1, "27: 关闭编辑页并刷新"),
        ("m", 1, 0, "28: Toast「已保存」"),
    ]
    for kind, a, b, t in msgs3:
        if kind == "s":
            self_msg(d, a, y, t)
        else:
            msg(d, a, b, y, t, ret=(kind == "r"))
        y += 86
    caption(d, H)
    img.convert("RGB").save(OUT + r"\seq-03-detail-edit.png")
    print("seq-03 ok, last y =", y)

if __name__ == "__main__":
    flowchart()
    flowchart_detail()
    seq_load_filter()
    seq_add_delete()
    seq_detail_edit()
