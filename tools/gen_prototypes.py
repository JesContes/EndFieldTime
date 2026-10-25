# -*- coding: utf-8 -*-
"""终末时记 · 界面原型图生成脚本(半平面风,主色 #66CCFF)"""
import datetime
from PIL import Image, ImageDraw, ImageFont, ImageFilter

FONT = "C:/Windows/Fonts/msyh.ttc"
FONT_B = "C:/Windows/Fonts/msyhbd.ttc"

def F(size, bold=False):
    return ImageFont.truetype(FONT_B if bold else FONT, size)

PRIMARY = "#66CCFF"
PRIMARY_DEEP = "#3FA9F5"
BG = "#F6FAFD"
INK = "#22313F"
INK2 = "#7C93A3"
FIELD_BG = "#EFF5FA"
DANGER = "#FF5252"

def mix(h1, h2, t):
    c1 = [int(h1[i:i+2], 16) for i in (1, 3, 5)]
    c2 = [int(h2[i:i+2], 16) for i in (1, 3, 5)]
    return "#%02X%02X%02X" % tuple(round(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

W, H = 780, 1688
OUT = r"D:\projects\EndFieldTime\docs\images"

def canvas():
    return Image.new("RGBA", (W, H), BG)

def soft_shadow(img, box, radius, blur=16, dy=10, alpha=36):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.rounded_rectangle([box[0], box[1] + dy, box[2], box[3] + dy],
                        radius=radius, fill=(40, 80, 120, alpha))
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(blur)))

def soft_shadow_circle(img, cx, cy, r, blur=14, dy=8, alpha=48):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.ellipse([cx - r, cy - r + dy, cx + r, cy + r + dy], fill=(40, 80, 120, alpha))
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(blur)))

def status_bar(d):
    d.text((44, 27), "9:41", font=F(26, True), fill=INK, anchor="lm")
    for i in range(4):  # 信号
        h = 8 + i * 6
        d.rounded_rectangle([608 + i * 14, 40 - h, 618 + i * 14, 40], radius=3, fill=INK)
    d.text((670, 27), "5G", font=F(22, True), fill=INK, anchor="lm")
    d.rounded_rectangle([708, 16, 748, 38], radius=7, outline=INK, width=3)   # 电池
    d.rectangle([750, 22, 755, 32], fill=INK)
    d.rounded_rectangle([711, 19, 736, 35], radius=4, fill=INK)

def clock_icon(d, cx, cy):
    d.ellipse([cx - 42, cy - 42, cx + 42, cy + 42], fill="#E1F3FE")
    d.ellipse([cx - 24, cy - 24, cx + 24, cy + 24], outline=PRIMARY_DEEP, width=5)
    d.line([cx, cy, cx, cy - 14], fill=PRIMARY_DEEP, width=5)
    d.line([cx, cy, cx + 10, cy + 4], fill=PRIMARY_DEEP, width=5)

# ---------------- 里程碑卡片 ----------------
TODAY = datetime.date.today()

def day_state(date):
    delta = (date - TODAY).days
    if delta > 0:
        return "还有", delta
    if delta < 0:
        return "已过", -delta
    return "今天", 0

def draw_card(img, d, x, y, w, h, cat, title, date, color, highlight=False):
    soft_shadow(img, (x, y, x + w, y + h), 30)
    d.rounded_rectangle([x, y, x + w, y + h], radius=30, fill=color)
    if highlight:
        d.rounded_rectangle([x, y, x + w, y + h], radius=30, outline="#FFFFFF", width=6)
    pad = 28
    # 分类 chip(白色 28% 混合底)
    f_chip = F(25, True)
    tw = d.textlength(cat, font=f_chip)
    d.rounded_rectangle([x + pad, y + 26, x + pad + tw + 40, y + 72], radius=23,
                        fill=mix(color, "#FFFFFF", 0.30))
    d.text((x + pad + 20, y + 49), cat, font=f_chip, fill="#FFFFFF", anchor="lm")
    # 状态标签(已过/还有)
    label, n = day_state(date)
    d.text((x + w - pad, y + 49), label, font=F(26, True),
           fill=mix(color, "#FFFFFF", 0.85), anchor="rm")
    # 大数字 + 天
    f_num = F(88, True)
    num = str(n)
    d.text((x + pad, y + 96), num, font=f_num, fill="#FFFFFF")
    nw = d.textlength(num, font=f_num)
    d.text((x + pad + nw + 10, y + 96 + 52), "天", font=F(34, True),
           fill=mix(color, "#FFFFFF", 0.90))
    # 标题 & 日期
    d.text((x + pad, y + 206), title, font=F(34, True), fill="#FFFFFF")
    d.text((x + pad, y + 250), date.strftime("%Y-%m-%d"), font=F(24),
           fill=mix(color, "#FFFFFF", 0.75))

def fab(img, d):
    cx, cy, r = 688, 1586, 62
    soft_shadow_circle(img, cx, cy, r)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=PRIMARY)
    d.line([cx - 26, cy, cx + 26, cy], fill="#FFFFFF", width=10)
    d.line([cx, cy - 26, cx, cy + 26], fill="#FFFFFF", width=10)

def funnel(d, cx, cy, color):
    d.polygon([(cx - 13, cy - 11), (cx + 13, cy - 11), (cx + 4, cy + 1),
               (cx + 4, cy + 13), (cx - 4, cy + 13), (cx - 4, cy + 1)], fill=color)

def filter_bar(d, current, total):
    """首页筛选栏:漏斗 + 当前生效标签(非全部时实心并带 ✕ 重置) + 右侧记录数"""
    cy, x = 250, 32
    f = F(28, True)
    tw = d.textlength(current, font=f)
    if current == "全部":
        w = 176
        d.rounded_rectangle([x, cy - 30, x + w, cy + 30], radius=30,
                            fill="#FFFFFF", outline="#D8E3EC", width=2)
        funnel(d, x + 30, cy, PRIMARY_DEEP)
        d.text((x + 52, cy - 2), current, font=f, fill="#5A6B78", anchor="lm")
        chevron_down(d, x + w - 30, cy)
    else:
        w = int(52 + tw + 66)
        d.rounded_rectangle([x, cy - 30, x + w, cy + 30], radius=30, fill=PRIMARY)
        funnel(d, x + 30, cy, "#FFFFFF")
        d.text((x + 52, cy - 2), current, font=f, fill="#FFFFFF", anchor="lm")
        d.line([(x + w - 38, cy - 9), (x + w - 20, cy + 9)], fill="#FFFFFF", width=5)
        d.line([(x + w - 38, cy + 9), (x + w - 20, cy - 9)], fill="#FFFFFF", width=5)
    d.text((748, cy), "共 %d 条记录" % total, font=F(24), fill=INK2, anchor="rm")

def home_header(d, subtitle="记录每一个重要时刻"):
    d.text((32, 96), "终末时记", font=F(54, True), fill=INK)
    d.text((34, 164), subtitle, font=F(25), fill=INK2)
    clock_icon(d, 692, 122)

CARDS = [
    dict(cat="生活",   title="坚持早起",     date=TODAY - datetime.timedelta(days=45),  color="#66CCFF"),
    dict(cat="纪念日", title="恋爱纪念日",   date=TODAY + datetime.timedelta(days=30),  color="#F06292"),
    dict(cat="工作",   title="入职新公司",   date=TODAY - datetime.timedelta(days=200), color="#FFB74D"),
    dict(cat="学习",   title="每天背单词",   date=TODAY - datetime.timedelta(days=365), color="#81C784"),
    dict(cat="生活",   title="戒掉熬夜",     date=TODAY - datetime.timedelta(days=12),  color="#9575CD"),
    dict(cat="健身",   title="第一次马拉松", date=TODAY - datetime.timedelta(days=90),  color="#4DB6AC"),
]
GRID_X = [32, 402]
GRID_Y = [320, 644, 968]
CW, CH = 346, 300

def draw_grid(img, d, cards, highlight_first=False):
    for i, c in enumerate(cards):
        x = GRID_X[i % 2]
        y = GRID_Y[i // 2]
        draw_card(img, d, x, y, CW, CH, c["cat"], c["title"], c["date"], c["color"],
                  highlight=(highlight_first and i == 0))

# ---------------- 屏幕 1:主界面 ----------------
def screen_home():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d); home_header(d)
    filter_bar(d, "全部", len(CARDS))
    draw_grid(img, d, CARDS)
    fab(img, d)
    img.convert("RGB").save(OUT + r"\proto-01-home.png")

# ---------------- 屏幕 2:分类过滤 ----------------
def screen_filter():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d); home_header(d)
    filter_bar(d, "生活", 2)
    draw_grid(img, d, [c for c in CARDS if c["cat"] == "生活"])
    fab(img, d)
    img.convert("RGB").save(OUT + r"\proto-02-filter.png")

# ---------------- 屏幕 10:首页筛选面板(搜索选择) ----------------
def screen_home_filter():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d); home_header(d)
    filter_bar(d, "全部", len(CARDS))
    draw_grid(img, d, CARDS)
    fab(img, d)
    # 筛选面板(覆盖在列表上方)
    soft_shadow(img, (32, 296, 748, 660), 24, blur=24, dy=12, alpha=70)
    d.rounded_rectangle([32, 296, 748, 660], radius=24, fill="#FFFFFF")
    # 搜索行
    magnifier(d, 84, 340)
    f = F(30)
    d.text((128, 340), "健", font=f, fill=INK, anchor="lm")
    tw = d.textlength("健", font=f)
    d.rectangle([128 + tw + 8, 322, 128 + tw + 11, 358], fill=PRIMARY)
    d.line([(72, 384), (708, 384)], fill="#E5EDF3", width=2)
    # 置顶「全部」重置行(当前生效,右侧对勾)
    cy = 428
    funnel(d, 96, cy, PRIMARY_DEEP)
    d.text((128, cy), "全部", font=F(30, True), fill=INK, anchor="lm")
    d.text((620, cy), "重置", font=F(22), fill=INK2, anchor="rm")
    d.line([(676, cy + 2), (686, cy + 12), (704, cy - 12)],
           fill=PRIMARY_DEEP, width=6, joint="curve")
    d.line([(72, 472), (708, 472)], fill="#E5EDF3", width=2)
    # 模糊匹配分类(命中高亮 + 记录数)
    for i, (name, cnt) in enumerate([("健身", "3 条"), ("健康", "1 条")]):
        cy = 516 + i * 88
        d.text((96, cy), name[0], font=F(30, True), fill=PRIMARY_DEEP, anchor="lm")
        w1 = d.textlength(name[0], font=F(30, True))
        d.text((96 + w1, cy), name[1:], font=F(30), fill=INK, anchor="lm")
        d.text((708, cy), cnt, font=F(22), fill=INK2, anchor="rm")
        if i == 0:
            d.line([(72, 560), (708, 560)], fill="#E5EDF3", width=2)
    img.convert("RGB").save(OUT + r"\proto-10-home-filter.png")

# ---------------- 表单公共元素 ----------------
DETAIL_TEXT = ("每天 5:30 起床，温水一杯，阅读 40 分钟，拉伸 20 分钟。"
               "已经连续坚持 45 天，精力明显变好，上午效率翻倍。目标:连续打卡 365 天!")
CONTENT_TEXT = (
    "决定坚持早起，是为了把每天最清醒的两个小时留给自己。\n"
    "\n"
    "计划每天 5:30 起床，先喝一杯温水，然后阅读 40 分钟，再做 20 分钟拉伸;"
    "周末不晚于 7:00 起床，保持生物钟稳定。\n"
    "\n"
    "第 1 周最难，闹钟响了三次才爬起来;第 3 周开始自然醒;到现在基本不需要闹钟。"
    "精力明显变好，上午的工作效率提升了一倍多，读完的书也从一年 3 本变成一个月 2 本。\n"
    "\n"
    "下一个目标:把早起省下的时间用于备考，争取连续打卡 365 天!")
COLORS = ["#66CCFF", "#4DB6AC", "#81C784", "#FFD54F",
          "#FFB74D", "#F06292", "#9575CD", "#FF8A65"]

def section_label(d, y, text):
    d.rounded_rectangle([32, y + 8, 40, y + 40], radius=4, fill=PRIMARY)
    d.text((54, y + 24), text, font=F(30, True), fill=INK, anchor="lm")

def calendar_icon(d, cx, cy):
    d.rounded_rectangle([cx - 26, cy - 22, cx + 26, cy + 26], radius=8, outline=INK2, width=4)
    d.line([cx - 26, cy - 8, cx + 26, cy - 8], fill=INK2, width=4)
    d.line([cx - 12, cy - 30, cx - 12, cy - 16], fill=INK2, width=5)
    d.line([cx + 12, cy - 30, cx + 12, cy - 16], fill=INK2, width=5)
    for dx in (-13, 0, 13):
        for dy in (6, 18):
            d.ellipse([cx + dx - 3, cy + dy - 3, cx + dx + 3, cy + dy + 3], fill=INK2)

def chevron_down(d, cx, cy, color=INK2):
    d.line([(cx - 13, cy - 7), (cx, cy + 7), (cx + 13, cy - 7)],
           fill=color, width=6, joint="curve")

def magnifier(d, cx, cy, color=INK2):
    d.ellipse([cx - 15, cy - 15, cx + 15, cy + 15], outline=color, width=5)
    d.line([(cx + 11, cy + 11), (cx + 22, cy + 22)], fill=color, width=6)

def pencil_icon(d, cx, cy):
    d.line([(cx - 12, cy + 12), (cx + 12, cy - 12)], fill=PRIMARY_DEEP, width=9)
    d.line([(cx + 8, cy - 18), (cx + 18, cy - 8)], fill=PRIMARY_DEEP, width=7)
    d.polygon([(cx - 16, cy + 16), (cx - 6, cy + 10), (cx - 10, cy + 6)],
              fill=PRIMARY_DEEP)

def close_icon(d, cx, cy, color=INK2):
    d.line([(cx - 13, cy - 13), (cx + 13, cy + 13)], fill=color, width=6)
    d.line([(cx - 13, cy + 13), (cx + 13, cy - 13)], fill=color, width=6)

def wrap_text(d, text, font, max_w):
    lines = []
    for para in text.split("\n"):
        cur = ""
        for ch in para:
            if d.textlength(cur + ch, font=font) <= max_w:
                cur += ch
            else:
                lines.append(cur); cur = ch
        lines.append(cur)
    return lines

def dropdown_field(d, box, text, placeholder=False):
    d.rounded_rectangle(box, radius=24, fill=FIELD_BG)
    d.text((box[0] + 40, (box[1] + box[3]) // 2), text, font=F(30),
           fill="#9AB0C0" if placeholder else INK, anchor="lm")
    chevron_down(d, box[2] - 44, (box[1] + box[3]) // 2)

def multiline_field(d, box, text, hint):
    d.rounded_rectangle(box, radius=24, fill=FIELD_BG)
    if text:
        lines = wrap_text(d, text, F(26), box[2] - box[0] - 80)[:4]
        for i, ln in enumerate(lines):
            d.text((box[0] + 32, box[1] + 26 + i * 42), ln, font=F(26), fill=INK)
        count = "%d/1000" % len(text)
    else:
        d.text((box[0] + 32, box[1] + 26), hint, font=F(26), fill="#9AB0C0")
        count = "0/1000"
    d.text((box[2] - 32, box[3] - 20), count, font=F(22), fill=INK2, anchor="rd")

def color_row(d, cy, sel):
    r, gap = 34, 24.57
    for i, c in enumerate(COLORS):
        cx = 66 + i * (2 * r + gap)
        if i == sel:
            d.ellipse([cx - r - 10, cy - r - 10, cx + r + 10, cy + r + 10],
                      outline=c, width=5)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=c)
        if i == sel:
            d.line([(cx - 16, cy + 2), (cx - 4, cy + 14), (cx + 18, cy - 12)],
                   fill="#FFFFFF", width=8, joint="curve")

def form_page(title, content, category, detail, date_str, color_idx, preview):
    """新增/编辑共用表单页(分类为下拉选择,含内容详情字段)"""
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d)
    d.line([(64, 96), (40, 120), (64, 144)], fill=INK, width=7, joint="curve")
    d.text((104, 118), title, font=F(40, True), fill=INK, anchor="lm")
    # 内容
    section_label(d, 176, "里程碑内容")
    d.rounded_rectangle([32, 220, 748, 308], radius=24, fill=FIELD_BG)
    d.text((72, 264), content if content else "例如:坚持早起 100 天", font=F(30),
           fill=INK if content else "#9AB0C0", anchor="lm")
    # 分类(下拉 + 模糊搜索)
    section_label(d, 332, "分类")
    dropdown_field(d, (32, 376, 748, 464),
                   category if category else "请选择分类(支持搜索)",
                   placeholder=(not category))
    # 内容详情
    section_label(d, 488, "内容详情(选填)")
    multiline_field(d, (32, 532, 748, 736), detail,
                    "补充背景、计划或心得,1000 字以内…")
    # 日期
    section_label(d, 760, "目标日期")
    d.rounded_rectangle([32, 804, 748, 892], radius=24, fill=FIELD_BG)
    d.text((72, 848), date_str, font=F(30), fill=INK, anchor="lm")
    calendar_icon(d, 690, 848)
    # 颜色
    section_label(d, 916, "卡片颜色")
    color_row(d, 988, color_idx)
    # 预览
    section_label(d, 1052, "效果预览")
    cat, ttl, dt, col = preview
    draw_card(img, d, 32, 1096, 716, 300, cat, ttl, dt, col)
    d.text((390, 1436), "卡片天数将根据目标日期自动计算", font=F(24), fill=INK2, anchor="mm")
    # 保存
    soft_shadow(img, (32, 1540, 748, 1652), 32, blur=18, dy=8, alpha=56)
    d.rounded_rectangle([32, 1540, 748, 1652], radius=32, fill=PRIMARY)
    d.text((390, 1594), "保  存", font=F(38, True), fill="#FFFFFF", anchor="mm")
    return img, d

# ---------------- 屏幕 3:新增里程碑 ----------------
def screen_add():
    date = TODAY + datetime.timedelta(days=30)
    img, d = form_page("新增里程碑", "和TA的周年旅行", "纪念日", "",
                       date.strftime("%Y-%m-%d"), 5,
                       ("纪念日", "和TA的周年旅行", date, "#F06292"))
    img.convert("RGB").save(OUT + r"\proto-03-add.png")

# ---------------- 屏幕 8:分类下拉选择(模糊搜索) ----------------
def screen_category():
    date = TODAY + datetime.timedelta(days=30)
    img, d = form_page("新增里程碑", "和TA的周年旅行", "", "",
                       date.strftime("%Y-%m-%d"), 5,
                       ("纪念日", "和TA的周年旅行", date, "#F06292"))
    # 下拉面板(覆盖在表单上方)
    soft_shadow(img, (32, 476, 748, 840), 24, blur=24, dy=12, alpha=70)
    d.rounded_rectangle([32, 476, 748, 840], radius=24, fill="#FFFFFF")
    # 搜索行
    magnifier(d, 84, 520)
    f = F(30)
    d.text((128, 520), "健", font=f, fill=INK, anchor="lm")
    tw = d.textlength("健", font=f)
    d.rectangle([128 + tw + 8, 502, 128 + tw + 11, 538], fill=PRIMARY)
    d.line([(72, 564), (708, 564)], fill="#E5EDF3", width=2)
    # 模糊匹配结果(命中字符高亮)
    for i, name in enumerate(["健身", "健康"]):
        cy = 608 + i * 88
        d.text((96, cy), name[0], font=F(30, True), fill=PRIMARY_DEEP, anchor="lm")
        w1 = d.textlength(name[0], font=F(30, True))
        d.text((96 + w1, cy), name[1:], font=F(30), fill=INK, anchor="lm")
        d.text((708, cy), "已有分类", font=F(22), fill=INK2, anchor="rm")
        if i == 0:
            d.line([(72, 652), (708, 652)], fill="#E5EDF3", width=2)
    d.line([(72, 740), (708, 740)], fill="#E5EDF3", width=2)
    # 新建分类入口
    d.text((96, 784), "＋ 新建分类「健」", font=F(28, True), fill=PRIMARY_DEEP, anchor="lm")
    img.convert("RGB").save(OUT + r"\proto-08-category.png")

# ---------------- 屏幕 9:编辑里程碑 ----------------
def screen_edit():
    date = TODAY - datetime.timedelta(days=45)
    img, d = form_page("编辑里程碑", "坚持早起", "生活", DETAIL_TEXT,
                       date.strftime("%Y-%m-%d"), 0,
                       ("生活", "坚持早起", date, "#66CCFF"))
    img.convert("RGB").save(OUT + r"\proto-09-edit.png")

# ---------------- 屏幕 6:卡片详情页 ----------------
def card_detail_base():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d)
    d.line([(64, 96), (40, 120), (64, 144)], fill=INK, width=7, joint="curve")
    d.text((104, 118), "里程碑详情", font=F(40, True), fill=INK, anchor="lm")
    # 右上角编辑快捷入口
    d.ellipse([650, 74, 734, 158], fill="#E3F4FE")
    pencil_icon(d, 692, 116)
    # 大卡片
    date = TODAY - datetime.timedelta(days=45)
    draw_card(img, d, 32, 190, 716, 300, "生活", "坚持早起", date, "#66CCFF")
    # 信息面板
    soft_shadow(img, (32, 530, 748, 1020), 24)
    d.rounded_rectangle([32, 530, 748, 1020], radius=24, fill="#FFFFFF")
    rows = [
        ("分类", "生活"),
        ("目标日期", date.strftime("%Y-%m-%d")),
        ("天数状态", "已过 45 天"),
        ("内容详情", "已填写 · %d 字" % len(DETAIL_TEXT)),
        ("创建时间", date.strftime("%Y-%m-%d") + " 08:00"),
    ]
    for i, (k, v) in enumerate(rows):
        cy = 579 + i * 98
        d.text((72, cy), k, font=F(26), fill=INK2, anchor="lm")
        emph = (k == "天数状态")
        d.text((708, cy), v, font=F(28, emph),
               fill=PRIMARY_DEEP if emph else INK, anchor="rm")
        if i < 4:
            d.line([(72, cy + 49), (708, cy + 49)], fill="#EDF3F8", width=2)
    d.text((390, 1084), "提示:长按首页卡片可快速删除", font=F(24), fill=INK2, anchor="mm")
    # 底部按钮
    d.rounded_rectangle([32, 1500, 370, 1612], radius=28,
                        outline=PRIMARY_DEEP, width=3)
    d.text((201, 1554), "查看内容详情", font=F(30, True), fill=PRIMARY_DEEP, anchor="mm")
    soft_shadow(img, (410, 1500, 748, 1612), 28, blur=16, dy=8, alpha=50)
    d.rounded_rectangle([410, 1500, 748, 1612], radius=28, fill=PRIMARY)
    d.text((579, 1554), "编  辑", font=F(30, True), fill="#FFFFFF", anchor="mm")
    return img, d

def screen_card_detail():
    img, d = card_detail_base()
    img.convert("RGB").save(OUT + r"\proto-06-card-detail.png")

# ---------------- 屏幕 7:内容详情页(底部弹层) ----------------
def screen_content_detail():
    img, d = card_detail_base()
    # 遮罩
    img.alpha_composite(Image.new("RGBA", (W, H), (15, 30, 45, 118)))
    # 底部弹层
    soft_shadow(img, (0, 560, 780, 1688), 40, blur=30, dy=-8, alpha=70)
    d.rounded_rectangle([0, 560, 780, 1708], radius=40, fill="#FFFFFF",
                        corners=(True, True, False, False))
    d.rounded_rectangle([365, 586, 415, 594], radius=4, fill="#D8E3EC")
    d.text((48, 660), "内容详情", font=F(40, True), fill=INK, anchor="lm")
    close_icon(d, 710, 662)
    d.text((48, 722), "来自里程碑「坚持早起」", font=F(24), fill=INK2, anchor="lm")
    d.line([(48, 770), (732, 770)], fill="#E5EDF3", width=2)
    y = 806
    for ln in wrap_text(d, CONTENT_TEXT, F(28), 780 - 96)[:14]:
        d.text((48, y), ln, font=F(28), fill="#37485A")
        y += 52
    # 关闭按钮
    d.rounded_rectangle([32, 1560, 748, 1656], radius=28, fill=PRIMARY)
    d.text((390, 1606), "关  闭", font=F(32, True), fill="#FFFFFF", anchor="mm")
    img.convert("RGB").save(OUT + r"\proto-07-content-detail.png")

# ---------------- 屏幕 4:长按删除菜单 ----------------
def trash_icon(d, cx, cy, color):
    d.rounded_rectangle([cx - 14, cy - 6, cx + 14, cy + 20], radius=4, outline=color, width=4)
    d.line([cx - 18, cy - 10, cx + 18, cy - 10], fill=color, width=5)
    d.line([cx - 6, cy - 16, cx + 6, cy - 16], fill=color, width=5)
    d.line([cx - 5, cy, cx - 5, cy + 14], fill=color, width=3)
    d.line([cx + 5, cy, cx + 5, cy + 14], fill=color, width=3)

def screen_longpress():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d); home_header(d)
    filter_bar(d, "全部", len(CARDS))
    draw_grid(img, d, CARDS, highlight_first=True)
    # 弹出菜单(覆盖在被长按卡片上方)
    soft_shadow(img, (120, 336, 408, 444), 20, blur=20, dy=10, alpha=60)
    d.rounded_rectangle([120, 336, 408, 444], radius=20, fill="#FFFFFF")
    trash_icon(d, 176, 390, DANGER)
    d.text((212, 388), "删除", font=F(32, True), fill=DANGER, anchor="lm")
    fab(img, d)
    img.convert("RGB").save(OUT + r"\proto-04-longpress.png")

# ---------------- 屏幕 5:删除确认对话框 ----------------
def screen_delete():
    img = canvas(); d = ImageDraw.Draw(img)
    status_bar(d); home_header(d)
    filter_bar(d, "全部", len(CARDS))
    draw_grid(img, d, CARDS)
    fab(img, d)
    # 遮罩
    overlay = Image.new("RGBA", (W, H), (15, 30, 45, 118))
    img.alpha_composite(overlay)
    # 对话框
    soft_shadow(img, (60, 640, 720, 1062), 40, blur=30, dy=16, alpha=80)
    d.rounded_rectangle([60, 640, 720, 1062], radius=40, fill="#FFFFFF")
    # 顶部警示图标(压在对话框上边缘)
    d.ellipse([390 - 46, 640 - 46, 390 + 46, 640 + 46], fill=DANGER)
    d.text((390, 642), "!", font=F(48, True), fill="#FFFFFF", anchor="mm")
    d.text((390, 752), "删除里程碑", font=F(40, True), fill=INK, anchor="mm")
    d.text((390, 822), "确定要删除「坚持早起」吗?", font=F(29), fill=INK2, anchor="mm")
    d.text((390, 868), "删除后将无法恢复。", font=F(29), fill=INK2, anchor="mm")
    # 按钮
    d.rounded_rectangle([120, 940, 370, 1016], radius=38, outline="#C9D6E0", width=3)
    d.text((245, 976), "取消", font=F(32, True), fill=INK2, anchor="mm")
    d.rounded_rectangle([410, 940, 660, 1016], radius=38, fill=DANGER)
    d.text((535, 976), "删除", font=F(32, True), fill="#FFFFFF", anchor="mm")
    img.convert("RGB").save(OUT + r"\proto-05-delete.png")

if __name__ == "__main__":
    screen_home()
    screen_filter()
    screen_home_filter()
    screen_add()
    screen_category()
    screen_edit()
    screen_card_detail()
    screen_content_detail()
    screen_longpress()
    screen_delete()
    print("done:", TODAY)
