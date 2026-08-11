"""
django.contrib.gis.db.backends.oracle.adapter — Oracle 几何 WKT 适配器。

Oracle 要求多边形外环逆时针、内环顺时针，写入前自动修正环方向。
"""
import oracledb

from django.contrib.gis.db.backends.base.adapter import WKTAdapter
from django.contrib.gis.geos import GeometryCollection, Polygon


# Oracle 空间适配器：CLOB 输入，构造前修正 Polygon/GeometryCollection 方向
class OracleSpatialAdapter(WKTAdapter):
    input_size = oracledb.CLOB

    # 检测并修正多边形环方向后保存 wkt 与 srid
    def __init__(self, geom):
        """
        Oracle requires that polygon rings are in proper orientation. This
        affects spatial operations and an invalid orientation may cause
        failures. Correct orientations are:
         * Outer ring - counter clockwise
         * Inner ring(s) - clockwise
        """
        if isinstance(geom, Polygon):
            if self._polygon_must_be_fixed(geom):
                geom = self._fix_polygon(geom)
        elif isinstance(geom, GeometryCollection):
            if any(
                isinstance(g, Polygon) and self._polygon_must_be_fixed(g) for g in geom
            ):
                geom = self._fix_geometry_collection(geom)

        self.wkt = geom.wkt
        self.srid = geom.srid

    @staticmethod
    def _polygon_must_be_fixed(poly):
        return not poly.empty and (
            not poly.exterior_ring.is_counterclockwise
            or any(x.is_counterclockwise for x in poly)
        )

    @classmethod
    # 修正单个多边形：外环逆时针、内环顺时针
    def _fix_polygon(cls, poly, clone=True):
        """Fix single polygon orientation as described in __init__()."""
        if clone:
            poly = poly.clone()

        if not poly.exterior_ring.is_counterclockwise:
            poly.exterior_ring = list(reversed(poly.exterior_ring))

        for i in range(1, len(poly)):
            if poly[i].is_counterclockwise:
                poly[i] = list(reversed(poly[i]))

        return poly

    @classmethod
    # 遍历集合中所有 Polygon 并修正环方向
    def _fix_geometry_collection(cls, coll):
        """
        Fix polygon orientations in geometry collections as described in
        __init__().
        """
        coll = coll.clone()
        for i, geom in enumerate(coll):
            if isinstance(geom, Polygon):
                coll[i] = cls._fix_polygon(geom, clone=False)
        return coll
