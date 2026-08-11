from .base import GEOSBase
from .prototypes import prepared as capi


# 预计算几何：加速 contains/covers/intersects 等空间谓词
class PreparedGeometry(GEOSBase):
    """
    A geometry that is prepared for performing certain operations.
    At the moment this includes the contains covers, and intersects
    operations.
    """

    ptr_type = capi.PREPGEOM_PTR
    destructor = capi.prepared_destroy

    # 从 GEOSGeometry 创建预计算几何，保留原几何引用防 GC
    def __init__(self, geom):
        # Keeping a reference to the original geometry object to prevent it
        # from being garbage collected which could then crash the prepared one
        # See #21662
        self._base_geom = geom
        from .geometry import GEOSGeometry

        if not isinstance(geom, GEOSGeometry):
            raise TypeError
        self.ptr = capi.geos_prepare(geom.ptr)

    # 判断 other 是否完全位于本几何内部
    def contains(self, other):
        return capi.prepared_contains(self.ptr, other.ptr)

    # 判断 other 是否严格位于本几何内部（不含边界）
    def contains_properly(self, other):
        return capi.prepared_contains_properly(self.ptr, other.ptr)

    # 判断本几何是否覆盖 other
    def covers(self, other):
        return capi.prepared_covers(self.ptr, other.ptr)

    # 判断两几何是否相交
    def intersects(self, other):
        return capi.prepared_intersects(self.ptr, other.ptr)

    # 判断两几何是否交叉
    def crosses(self, other):
        return capi.prepared_crosses(self.ptr, other.ptr)

    # 判断两几何是否不相交
    def disjoint(self, other):
        return capi.prepared_disjoint(self.ptr, other.ptr)

    # 判断两几何是否部分重叠
    def overlaps(self, other):
        return capi.prepared_overlaps(self.ptr, other.ptr)

    # 判断两几何是否仅边界接触
    def touches(self, other):
        return capi.prepared_touches(self.ptr, other.ptr)

    # 判断本几何是否位于 other 内部
    def within(self, other):
        return capi.prepared_within(self.ptr, other.ptr)
