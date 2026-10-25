# -*- coding: utf-8 -*-
"""导出格式体积测算：对比不同格式方案的字节数"""
import json, gzip, io, random, datetime

random.seed(42)

CATS = ["生活", "工作", "学习", "纪念日", "健身"]
PALETTE = ["#66CCFF", "#4DB6AC", "#81C784", "#FFD54F",
           "#FFB74D", "#F06292", "#9575CD", "#FF8A65"]
TITLES = ["坚持早起", "恋爱纪念日", "入职新公司", "每天背单词", "第一次马拉松",
          "戒掉熬夜", "年度体检", "读完一本好书", "学会游泳", "存够第一笔钱"]
DETAIL_UNIT = "今天又坚持了一天，状态不错，继续保持节奏。计划下个月提高强度。"

# 高熵文本：从常用汉字池随机取字，模拟真实互不相同的日记内容（gzip 收益更保守）
POOL = "的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严龙飞"
def rand_text(n):
    return "".join(random.choice(POOL) for _ in range(n))

def make_records(n):
    recs = []
    for i in range(n):
        has_detail = random.random() < 0.6
        detail = rand_text(random.randint(40, 300)) if has_detail else ""
        recs.append({
            "id": i + 1,
            "content": TITLES[i % len(TITLES)],
            "category": CATS[i % len(CATS)],
            "targetDate": 20500 + i * 13,
            "colorHex": PALETTE[i % len(PALETTE)],
            "detail": detail,
            "createdAt": 1761300000000 + i * 3600_000,
            "sortOrder": 0,
        })
    return recs

def naive_json(recs):
    """方案A：全字段名 + ISO 日期 + 十六进制色"""
    out = []
    for r in recs:
        d = datetime.date(1970, 1, 1) + datetime.timedelta(days=r["targetDate"])
        out.append({
            "id": r["id"], "content": r["content"], "category": r["category"],
            "targetDate": d.isoformat(), "colorHex": r["colorHex"],
            "detail": r["detail"], "createdAt": r["createdAt"],
            "sortOrder": r["sortOrder"],
        })
    return json.dumps({"format": "endfield-time-export", "version": 1,
                       "milestones": out}, ensure_ascii=False, separators=(",", ":"))

def compact_json(recs):
    """方案B：紧凑键 + epochDay + 分类/颜色字典索引 + 秒级时间戳"""
    cats = sorted({r["category"] for r in recs})
    items = []
    for r in recs:
        item = {
            "c": r["content"],
            "k": cats.index(r["category"]),
            "d": r["targetDate"],
            "h": PALETTE.index(r["colorHex"]),
            "a": r["createdAt"] // 1000,
        }
        if r["detail"]:
            item["t"] = r["detail"]
        items.append(item)
    doc = {
        "format": "endfield-time-export", "version": 1,
        "exportedAt": 1761300000, "appVersion": "1.0",
        "categories": cats, "milestones": items,
    }
    return json.dumps(doc, ensure_ascii=False, separators=(",", ":"))

def gz_size(text):
    buf = io.BytesIO()
    with gzip.GzipFile(fileobj=buf, mode="wb", compresslevel=9, mtime=0) as f:
        f.write(text.encode("utf-8"))
    return len(buf.getvalue())

def csv_size(recs):
    """方案C：CSV（内容里含逗号/换行需转义）"""
    lines = ["content,category,targetDate,colorHex,detail,createdAt"]
    for r in recs:
        d = datetime.date(1970, 1, 1) + datetime.timedelta(days=r["targetDate"])
        detail = r["detail"].replace('"', '""')
        lines.append(f'"{r["content"]}",{r["category"]},{d.isoformat()},{r["colorHex"]},"{detail}",{r["createdAt"]}')
    return len("\n".join(lines).encode("utf-8"))

print(f"{'条数':>6} {'方案A 全字段':>14} {'方案B 紧凑':>12} {'B gzip':>10} {'CSV':>10} "
      f"{'B较A省':>9} {'gz较A省':>9} {'B/条':>8}")
for n in (10, 50, 200, 1000):
    recs = make_records(n)
    a = len(naive_json(recs).encode("utf-8"))
    b = len(compact_json(recs).encode("utf-8"))
    g = gz_size(compact_json(recs))
    c = csv_size(recs)
    print(f"{n:>6} {a:>14,} {b:>12,} {g:>10,} {c:>10,} "
          f"{(1-b/a)*100:>8.1f}% {(1-g/a)*100:>8.1f}% {b/n:>8.0f}")

# 关键结论：空详情占比的影响
recs = make_records(200)
print("\n200 条时，仅含最小字段(无详情)的记录平均字节：")
recs_nodet = [dict(r, detail="") for r in recs]
print(f"  紧凑 JSON: {len(compact_json(recs_nodet).encode('utf-8'))/200:.0f} B/条")
print(f"  含详情时  : {len(compact_json(recs).encode('utf-8'))/200:.0f} B/条")
