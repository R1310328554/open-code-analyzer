"""
django.contrib.gis.db.backends.base.models — 空间参考系统 Mixin。

SpatialRefSysMixin 为各数据库后端的 SpatialRefSys 模型提供共用属性与方法。
"""
from django.contrib.gis import gdal
from django.utils.functional import cached_property


# 空间参考系统 Mixin：通过 GDAL 解析 WKT/PROJ.4 并提供椭球体、单位等属性
class SpatialRefSysMixin:
    """
    The SpatialRefSysMixin is a class used by the database-dependent
    SpatialRefSys objects to reduce redundant code.
    """

    @cached_property
    # 返回 GDAL SpatialReference 对象，依次尝试 WKT 与 PROJ.4
    def srs(self):
        """
        Return a GDAL SpatialReference object.
        """
        try:
            return gdal.SpatialReference(self.wkt)
        except Exception as e:
            wkt_error = e

        try:
            return gdal.SpatialReference(self.proj4text)
        except Exception as e:
            proj4_error = e

        raise Exception(
            "Could not get OSR SpatialReference.\n"
            f"Error for WKT '{self.wkt}': {wkt_error}\n"
            f"Error for PROJ.4 '{self.proj4text}': {proj4_error}"
        )

    @property
    # 椭球体参数：(长半轴, 短半轴, 扁率倒数)
    def ellipsoid(self):
        """
        Return a tuple of the ellipsoid parameters:
        (semimajor axis, semiminor axis, and inverse flattening).
        """
        return self.srs.ellipsoid

    @property
    def name(self):
        "Return the projection name."
        return self.srs.name

    @property
    def spheroid(self):
        "Return the spheroid name for this spatial reference."
        return self.srs["spheroid"]

    @property
    def datum(self):
        "Return the datum for this spatial reference."
        return self.srs["datum"]

    @property
    def projected(self):
        "Is this Spatial Reference projected?"
        return self.srs.projected

    @property
    def local(self):
        "Is this Spatial Reference local?"
        return self.srs.local

    @property
    def geographic(self):
        "Is this Spatial Reference geographic?"
        return self.srs.geographic

    @property
    def linear_name(self):
        "Return the linear units name."
        return self.srs.linear_name

    @property
    def linear_units(self):
        "Return the linear units."
        return self.srs.linear_units

    @property
    def angular_name(self):
        "Return the name of the angular units."
        return self.srs.angular_name

    @property
    def angular_units(self):
        "Return the angular units."
        return self.srs.angular_units

    @property
    def units(self):
        "Return a tuple of the units and the name."
        if self.projected or self.local:
            return (self.linear_units, self.linear_name)
        elif self.geographic:
            return (self.angular_units, self.angular_name)
        else:
            return (None, None)

    @classmethod
    # 类方法：从 WKT 解析单位值与单位名，无需访问数据库
    def get_units(cls, wkt):
        """
        Return a tuple of (unit_value, unit_name) for the given WKT without
        using any of the database fields.
        """
        return gdal.SpatialReference(wkt).units

    @classmethod
    # 类方法：GeometryField 初始化时从 WKT 提取 SPHEROID 参数
    def get_spheroid(cls, wkt, string=True):
        """
        Class method used by GeometryField on initialization to
        retrieve the `SPHEROID[..]` parameters from the given WKT.
        """
        srs = gdal.SpatialReference(wkt)
        sphere_params = srs.ellipsoid
        sphere_name = srs["spheroid"]

        if not string:
            return sphere_name, sphere_params
        else:
            # `string` parameter used to place in format acceptable by PostGIS
            if len(sphere_params) == 3:
                radius, flattening = sphere_params[0], sphere_params[2]
            else:
                radius, flattening = sphere_params
            return 'SPHEROID["%s",%s,%s]' % (sphere_name, radius, flattening)

    # 返回 OGC WKT 格式的字符串表示
    def __str__(self):
        """
        Return the string representation, a 'pretty' OGC WKT.
        """
        return str(self.srs)
