from ctypes import byref, c_double, c_int, c_void_p

from django.contrib.gis.gdal.error import GDALException
from django.contrib.gis.gdal.prototypes import raster as capi
from django.contrib.gis.gdal.raster.base import GDALRasterBase
from django.contrib.gis.shortcuts import numpy
from django.utils.encoding import force_str

from .const import (
    GDAL_COLOR_TYPES,
    GDAL_INTEGER_TYPES,
    GDAL_PIXEL_TYPES,
    GDAL_TO_CTYPES,
)


# 封装单个 GDAL 栅格波段，须从 GDALRaster 获取
class GDALBand(GDALRasterBase):
    """
    Wrap a GDAL raster band, needs to be obtained from a GDALRaster object.
    """

    # 绑定父栅格与 1 基波段索引，获取波段指针
    def __init__(self, source, index):
        self.source = source
        self._ptr = capi.get_ds_raster_band(source._ptr, index)

    # 刷新父栅格缓存并标记统计量需重新计算
    def _flush(self):
        """
        Call the flush method on the Band's parent raster and force a refresh
        of the statistics attribute when requested the next time.
        """
        self.source._flush()
        self._stats_refresh = True

    # 波段描述字符串
    @property
    def description(self):
        """
        Return the description string of the band.
        """
        return force_str(capi.get_band_description(self._ptr))

    # 波段宽度（像素，X 轴）
    @property
    def width(self):
        """
        Width (X axis) in pixels of the band.
        """
        return capi.get_band_xsize(self._ptr)

    # 波段高度（像素，Y 轴）
    @property
    def height(self):
        """
        Height (Y axis) in pixels of the band.
        """
        return capi.get_band_ysize(self._ptr)

    # 波段总像素数
    @property
    def pixel_count(self):
        """
        Return the total number of pixels in this band.
        """
        return self.width * self.height

    _stats_refresh = False

    # 计算最小/最大/均值/标准差；支持 PAM 缓存与近似统计
    def statistics(self, refresh=False, approximate=False):
        """
        Compute statistics on the pixel values of this band.

        The return value is a tuple with the following structure:
        (minimum, maximum, mean, standard deviation).

        If approximate=True, the statistics may be computed based on overviews
        or a subset of image tiles.

        If refresh=True, the statistics will be computed from the data
        directly, and the cache will be updated where applicable.

        For empty bands (where all pixel values are nodata), all statistics
        values are returned as None.

        For raster formats using Persistent Auxiliary Metadata (PAM) services,
        the statistics might be cached in an auxiliary file.
        """
        # Prepare array with arguments for capi function
        smin, smax, smean, sstd = c_double(), c_double(), c_double(), c_double()
        stats_args = [
            self._ptr,
            c_int(approximate),
            byref(smin),
            byref(smax),
            byref(smean),
            byref(sstd),
            c_void_p(),
            c_void_p(),
        ]

        if refresh or self._stats_refresh:
            func = capi.compute_band_statistics
        else:
            # Add additional argument to force computation if there is no
            # existing PAM file to take the values from.
            force = True
            stats_args.insert(2, c_int(force))
            func = capi.get_band_statistics

        # Computation of statistics fails for empty bands.
        try:
            func(*stats_args)
            result = smin.value, smax.value, smean.value, sstd.value
        except GDALException:
            result = (None, None, None, None)

        self._stats_refresh = False

        return result

    # 最小像素值（委托 statistics）
    @property
    def min(self):
        """
        Return the minimum pixel value for this band.
        """
        return self.statistics()[0]

    # 最大像素值
    @property
    def max(self):
        """
        Return the maximum pixel value for this band.
        """
        return self.statistics()[1]

    # 像素均值
    @property
    def mean(self):
        """
        Return the mean of all pixel values of this band.
        """
        return self.statistics()[2]

    # 像素标准差
    @property
    def std(self):
        """
        Return the standard deviation of all pixel values of this band.
        """
        return self.statistics()[3]

    # 无数据值；未设置时返回 None
    @property
    def nodata_value(self):
        """
        Return the nodata value for this band, or None if it isn't set.
        """
        # Get value and nodata exists flag
        nodata_exists = c_int()
        value = capi.get_band_nodata_value(self._ptr, nodata_exists)
        if not nodata_exists:
            value = None
        # If the pixeltype is an integer, convert to int
        elif self.datatype() in GDAL_INTEGER_TYPES:
            value = int(value)
        return value

    # 设置或清除 NoData 值
    @nodata_value.setter
    def nodata_value(self, value):
        """
        Set the nodata value for this band.
        """
        if value is None:
            capi.delete_band_nodata_value(self._ptr)
        elif not isinstance(value, (int, float)):
            raise ValueError("Nodata value must be numeric or None.")
        else:
            capi.set_band_nodata_value(self._ptr, value)
        self._flush()

    # GDAL 像素数据类型（可选返回 GDT_* 名称）
    def datatype(self, as_string=False):
        """
        Return the GDAL Pixel Datatype for this band.
        """
        dtype = capi.get_band_datatype(self._ptr)
        if as_string:
            dtype = GDAL_PIXEL_TYPES[dtype]
        return dtype

    # 颜色解释（灰度/RGB/Alpha 等）
    def color_interp(self, as_string=False):
        """Return the GDAL color interpretation for this band."""
        color = capi.get_band_color_interp(self._ptr)
        if as_string:
            color = GDAL_COLOR_TYPES[color]
        return color

    # 按块读写像素；读时返回 numpy 数组、memoryview 或 list
    def data(self, data=None, offset=None, size=None, shape=None, as_memoryview=False):
        """
        Read or writes pixel values for this band. Blocks of data can
        be accessed by specifying the width, height and offset of the
        desired block. The same specification can be used to update
        parts of a raster by providing an array of values.

        Allowed input data types are bytes, memoryview, list, tuple, and array.
        """
        offset = offset or (0, 0)
        size = size or (self.width - offset[0], self.height - offset[1])
        shape = shape or size
        if any(x <= 0 for x in size):
            raise ValueError("Offset too big for this raster.")

        if size[0] > self.width or size[1] > self.height:
            raise ValueError("Size is larger than raster.")

        # Create ctypes type array generator
        ctypes_array = GDAL_TO_CTYPES[self.datatype()] * (shape[0] * shape[1])

        if data is None:
            # Set read mode
            access_flag = 0
            # Prepare empty ctypes array
            data_array = ctypes_array()
        else:
            # Set write mode
            access_flag = 1

            # Instantiate ctypes array holding the input data
            if isinstance(data, (bytes, memoryview)) or (
                numpy and isinstance(data, numpy.ndarray)
            ):
                data_array = ctypes_array.from_buffer_copy(data)
            else:
                data_array = ctypes_array(*data)

        # Access band
        capi.band_io(
            self._ptr,
            access_flag,
            offset[0],
            offset[1],
            size[0],
            size[1],
            byref(data_array),
            shape[0],
            shape[1],
            self.datatype(),
            0,
            0,
        )

        # Return data as numpy array if possible, otherwise as list
        if data is None:
            if as_memoryview:
                return memoryview(data_array)
            elif numpy:
                # reshape() needs a reshape parameter with the height first.
                return numpy.frombuffer(
                    data_array, dtype=numpy.dtype(data_array)
                ).reshape(tuple(reversed(size)))
            else:
                return list(data_array)
        else:
            self._flush()


# 栅格波段列表：按 1 基索引迭代 GDALBand
class BandList(list):
    # 保存父 GDALRaster 引用
    def __init__(self, source):
        self.source = source
        super().__init__()

    # 按波段序号 1..n 生成 GDALBand
    def __iter__(self):
        for idx in range(1, len(self) + 1):
            yield GDALBand(self.source, idx)

    # 波段数量
    def __len__(self):
        return capi.get_ds_raster_count(self.source._ptr)

    # 0 基索引访问波段（内部转为 1 基）
    def __getitem__(self, index):
        try:
            return GDALBand(self.source, index + 1)
        except GDALException:
            raise GDALException("Unable to get band index %d" % index)
