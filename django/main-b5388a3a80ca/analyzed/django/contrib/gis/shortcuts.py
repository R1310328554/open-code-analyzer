import zipfile
from io import BytesIO

from django.conf import settings
from django.http import HttpResponse
from django.template import loader

# NumPy supported?
try:
    import numpy
except ImportError:
    numpy = False


# 将 KML 字符串压缩为 KMZ 二进制内容
def compress_kml(kml):
    "Return compressed KMZ from the given KML string."
    kmz = BytesIO()
    with zipfile.ZipFile(kmz, "a", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("doc.kml", kml.encode(settings.DEFAULT_CHARSET))
    kmz.seek(0)
    return kmz.read()


# 渲染模板并返回 KML MIME 类型的 HttpResponse
def render_to_kml(*args, **kwargs):
    "Render the response as KML (using the correct MIME type)."
    return HttpResponse(
        loader.render_to_string(*args, **kwargs),
        content_type="application/vnd.google-earth.kml+xml",
    )


# 渲染模板、压缩为 KMZ 并返回对应 HttpResponse
def render_to_kmz(*args, **kwargs):
    """
    Compress the KML content and return as KMZ (using the correct
    MIME type).
    """
    return HttpResponse(
        compress_kml(loader.render_to_string(*args, **kwargs)),
        content_type="application/vnd.google-earth.kmz",
    )
