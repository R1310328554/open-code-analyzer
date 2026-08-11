# WKT 适配器：将 Geometry 转为 MySQL/Oracle 后端可接受的 WKT 与 SRID
class WKTAdapter:
    """
    An adaptor for Geometries sent to the MySQL and Oracle database backends.
    """

    # 从几何对象提取 wkt 字符串与空间参考标识 srid
    def __init__(self, geom):
        self.wkt = geom.wkt
        self.srid = geom.srid

    # 按 wkt 与 srid 判断两个适配器是否相等
    def __eq__(self, other):
        return (
            isinstance(other, WKTAdapter)
            and self.wkt == other.wkt
            and self.srid == other.srid
        )

    # 基于 (wkt, srid) 元组生成哈希值
    def __hash__(self):
        return hash((self.wkt, self.srid))

    # 字符串形式即 WKT 文本
    def __str__(self):
        return self.wkt

    # Oracle 后端可覆盖的多边形修正钩子
    @classmethod
    def _fix_polygon(cls, poly):
        # Hook for Oracle.
        return poly
