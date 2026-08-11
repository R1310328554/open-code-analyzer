"""
几何集合模块 — GeometryCollection、MultiPoint、MultiLineString、MultiPolygon。

This module houses the Geometry Collection objects:"""
This module houses the Geometry Collection objects:
GeometryCollection, MultiPoint, MultiLineString, and MultiPolygon
"""

from django.contrib.gis.geos import prototypes as capi
from django.contrib.gis.geos.geometry import GEOSGeometry, LinearGeometryMixin
from django.contrib.gis.geos.libgeos import GEOM_PTR
from django.contrib.gis.geos.linestring import LinearRing, LineString
from django.contrib.gis.geos.point import Point
from django.contrib.gis.geos.polygon import Polygon


# 几何集合：可包含多种子几何的容器
class GeometryCollection(GEOSGeometry):
    _typeid = 7

    # 从几何序列初始化集合，校验允许的类型
    def __init__(self, *args, **kwargs):
        "Initialize a Geometry Collection from a sequence of Geometry objects."
        # Checking the arguments
        if len(args) == 1:
            # If only one geometry provided or a list of geometries is provided
            #  in the first argument.
            if isinstance(args[0], (tuple, list)):
                init_geoms = args[0]
            else:
                init_geoms = args
        else:
            init_geoms = args

        # Ensuring that only the permitted geometries are allowed in this
        # collection this is moved to list mixin super class
        self._check_allowed(init_geoms)

        # Creating the geometry pointer array.
        collection = self._create_collection(len(init_geoms), init_geoms)
        super().__init__(collection, **kwargs)

    # 迭代集合中的每个子几何
    def __iter__(self):
        "Iterate over each Geometry in the Collection."
        for i in range(len(self)):
            yield self[i]

    # 返回集合中几何数量
    def __len__(self):
        "Return the number of geometries in this Collection."
        return self.num_geom

    # ### Methods for compatibility with ListMixin ###
    # 克隆子几何指针并调用 GEOS 创建集合
    def _create_collection(self, length, items):
        # Creating the geometry pointer array.
        geoms = (GEOM_PTR * length)(
            *[
                # this is a little sloppy, but makes life easier
                # allow GEOSGeometry types (python wrappers) or pointer types
                capi.geom_clone(getattr(g, "ptr", g))
                for g in items
            ]
        )
        return capi.create_collection(self._typeid, geoms, length)

    def _get_single_internal(self, index):
        return capi.get_geomn(self.ptr, index)

    # 按索引返回子几何的克隆（供外部使用）
    def _get_single_external(self, index):
        """
        Return the Geometry from this Collection at the given index (0-based).
        """
        # Checking the index and returning the corresponding GEOS geometry.
        return GEOSGeometry(
            capi.geom_clone(self._get_single_internal(index)), srid=self.srid
        )

    # 重建集合并销毁旧指针
    def _set_list(self, length, items):
        """
        Create a new collection, and destroy the contents of the previous
        pointer.
        """
        prev_ptr = self.ptr
        srid = self.srid
        self.ptr = self._create_collection(length, items)
        if srid:
            self.srid = srid
        capi.destroy_geom(prev_ptr)

    _set_single = GEOSGeometry._set_single_rebuild
    _assign_extended_slice = GEOSGeometry._assign_extended_slice_rebuild

    @property
    def kml(self):
        "Return the KML for this Geometry Collection."
        return "<MultiGeometry>%s</MultiGeometry>" % "".join(g.kml for g in self)

    @property
    def tuple(self):
        "Return a tuple of all the coordinates in this Geometry Collection"
        return tuple(g.tuple for g in self)

    coords = tuple


# MultiPoint, MultiLineString, and MultiPolygon class definitions.
# 多点几何集合
class MultiPoint(GeometryCollection):
    _allowed = Point
    _typeid = 4


# 多线串几何集合
class MultiLineString(LinearGeometryMixin, GeometryCollection):
    _allowed = (LineString, LinearRing)
    _typeid = 5


# 多多边形几何集合
class MultiPolygon(GeometryCollection):
    _allowed = Polygon
    _typeid = 6


# Setting the allowed types here since GeometryCollection is defined before
# its subclasses.
GeometryCollection._allowed = (
    Point,
    LineString,
    LinearRing,
    Polygon,
    MultiPoint,
    MultiLineString,
    MultiPolygon,
)
