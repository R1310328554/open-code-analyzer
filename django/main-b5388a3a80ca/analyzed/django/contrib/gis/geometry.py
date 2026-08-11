import re

from django.utils.regex_helper import _lazy_re_compile

# 几何输入预检正则：在到达 C 库前过滤 HEX EWKB、WKT 与 JSON 栅格
# Regular expression for recognizing HEXEWKB and WKT. A prophylactic measure
# to prevent potentially malicious input from reaching the underlying C
# library. Not a substitute for good web security programming practices.
# 十六进制 EWKB 字符串
hex_regex = _lazy_re_compile(r"^[0-9A-F]+$", re.I)
# OGC WKT 格式（可选 SRID 前缀）
wkt_regex = _lazy_re_compile(
    r"^(SRID=(?P<srid>\-?[0-9]+);)?"
    r"(?P<wkt>"
    r"(?P<type>POINT|LINESTRING|LINEARRING|POLYGON|MULTIPOINT|"
    r"MULTILINESTRING|MULTIPOLYGON|GEOMETRYCOLLECTION|CIRCULARSTRING|COMPOUNDCURVE|"
    r"CURVEPOLYGON|MULTICURVE|MULTISURFACE|CURVE|SURFACE|POLYHEDRALSURFACE|TIN|"
    r"TRIANGLE)"
    r"[ACEGIMLONPSRUTYZ0-9,.+() -]+)$",
    re.I,
)
# JSON 对象字面量（用于 GDALRaster 字典输入）
json_regex = _lazy_re_compile(r"^(\s+)?\{.*}(\s+)?$", re.DOTALL)
