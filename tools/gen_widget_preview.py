# -*- coding: utf-8 -*-
"""桌面上「终末时记」小组件效果示意（不是真机截图，是设计稿）"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter

FONT = "C:/Windows/Fonts/msyh.ttc"
FONT_B = "C:/Windows/Fonts/msyhbd.ttc"
def F(s, b=False): return ImageFont.truetype(FONT_B if b else FONT, s)

PRIMARY = "#66CCFF"
INK = "#22313F"
W, H = 780, 1688

img = Image.new("RGB", (W, H))
d = ImageDraw.Draw(img)

# 壁纸：淡蓝渐变
for y in range(H):
    t = y / H
    r = int(226 + (146 - 226) * t)
    g = int(242 + (196 - 242) * t)
    b = int(250 + (240 - 250) * t)
    d.line([(0, y), (W, y)], fill=(r, g, b))

# 状态栏
d.text((44, 30), "9:41", font=F(26, True), fill=INK, anchor="lm")
d.rounded_rectangle([708, 18, 748, 40], radius=7, outline=INK, width=3)
d.rounded_rectangle([711, 21, 736, 37], radius=4, fill=INK)

# ---------- 小组件 1：4x2，单条里程碑大卡 ----------
wx, wy, ww, wh = 40, 96, 700, 300
sh = Image.new("RGBA", (W, H), (0, 0, 0, 0))
ImageDraw.Draw(sh).rounded_rectangle([wx, wy + 10, wx + ww, wy + wh + 10], 34, fill=(40, 80, 120, 40))
img.paste(Image.alpha_composite(img.convert("RGBA"), sh.filter(ImageFilter.GaussianBlur(16))).convert("RGB"), (0, 0))

d.rounded_rectangle([wx, wy, wx + ww, wy + wh], radius=34, fill=PRIMARY)
# 分类 chip
chip = Image.new("RGBA", (W, H), (0, 0, 0, 0))
ImageDraw.Draw(chip).rounded_rectangle([wx + 26, wy + 24, wx + 122, wy + 68], radius=22, fill=(255, 255, 255, 90))
img.paste(Image.alpha_composite(img.convert("RGBA"), chip).convert("RGB"), (0, 0))
d.text((wx + 74, wy + 46), "生活", font=F(22, True), fill="#FFFFFF", anchor="mm")
d.text((wx + ww - 26, wy + 46), "已过", font=F(22), fill="#E8F7FF", anchor="rm")
# 大数字
d.text((wx + 26, wy + 96), "45", font=F(96, True), fill="#FFFFFF")
d.text((wx + 160, wy + 150), "天", font=F(34, True), fill="#E8F7FF")
# 标题与日期
d.text((wx + 26, wy + 220), "坚持早起", font=F(32, True), fill="#FFFFFF")
d.text((wx + 26, wy + 262), "2026-09-10 起 · 点击查看详情", font=F(20), fill="#DCF2FF")

# ---------- 小组件 2：4x1，多条紧凑列表 ----------
wx2, wy2, ww2, wh2 = 40, 420, 700, 250
d.rounded_rectangle([wx2, wy2, wx2 + ww2, wy2 + wh2], radius=28, fill="#FFFFFF")
rows = [("恋爱纪念日", "还有", "30", "#F06292"),
        ("每天背单词", "已过", "365", "#81C784"),
        ("第一次马拉松", "已过", "90", "#4DB6AC")]
for i, (title, label, num, color) in enumerate(rows):
    cy = wy2 + 52 + i * 76
    d.rounded_rectangle([wx2 + 22, cy - 22, wx2 + 30, cy + 22], radius=4, fill=color)
    d.text((wx2 + 46, cy), title, font=F(26, True), fill=INK, anchor="lm")
    d.text((wx2 + ww2 - 130, cy), label, font=F(20), fill="#7C93A3", anchor="rm")
    d.text((wx2 + ww2 - 24, cy), num, font=F(34, True), fill=color, anchor="rm")

# 提示标注
d.text((40, 700), "↑ 上面是「大卡」样式（4×2），下面是「列表」样式（4×1）", font=F(22), fill="#5A6B78")

# ---------- 假 App 图标网格 ----------
icon_labels = [("日历", "#FFB74D"), ("相机", "#90A4AE"), ("音乐", "#9575CD"), ("设置", "#78909C"),
               ("邮箱", "#4DB6AC"), ("相册", "#F06292"), ("地图", "#81C784"), ("时钟", "#64B5F6")]
gx0, gy0, step = 68, 790, 178
for i, (name, color) in enumerate(icon_labels):
    cx = gx0 + (i % 4) * step
    cy = gy0 + (i // 4) * step
    d.rounded_rectangle([cx - 56, cy - 56, cx + 56, cy + 56], radius=30, fill=color)
    d.text((cx, cy + 86), name, font=F(20), fill="#33465A", anchor="mm")

# 底部 dock
d.rounded_rectangle([40, 1500, 740, 1640], radius=40, fill="#FFFFFFB0")
for i, color in enumerate(["#4DB6AC", "#F06292", "#FFB74D", "#66CCFF"]):
    cx = 140 + i * 168
    d.rounded_rectangle([cx - 48, 1522, cx + 48, 1618], radius=28, fill=color)

img.save(r"D:\projects\EndFieldTime\docs\images\widget-preview.png")
print("ok")
