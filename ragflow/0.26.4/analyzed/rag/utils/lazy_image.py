"""
延迟加载图像：多 blob 纵向拼接为单 PIL Image，支持上下文管理与 numpy 桥接。
"""

import logging
from io import BytesIO

from PIL import Image

from rag.nlp import concat_img


class LazyImage:
    # 缓存 _blobs 列表，首次 to_pil() 时 decode 并 concat_img 拼接
    def __init__(self, blobs, source=None):
        self._blobs = [b for b in (blobs or []) if b]
        self.source = source
        self._pil = None

    def __bool__(self):
        return bool(self._blobs)

    def to_pil(self):
        # 逐 blob 打开 RGB 图并纵向合并，结果缓存在 _pil
        if self._pil is not None:
            try:
                self._pil.load()
                return self._pil
            except Exception:
                try:
                    self._pil.close()
                except Exception:
                    pass
                self._pil = None
        res_img = None
        for blob in self._blobs:
            try:
                image = Image.open(BytesIO(blob)).convert("RGB")
            except Exception as e:
                logging.info(f"LazyImage: skip bad image blob: {e}")
                continue

            if res_img is None:
                res_img = image
                continue

            new_img = concat_img(res_img, image)
            if new_img is not res_img:
                try:
                    res_img.close()
                except Exception:
                    pass
            try:
                image.close()
            except Exception:
                pass
            res_img = new_img

        self._pil = res_img
        return self._pil

    def to_pil_detached(self):
        # 返回 PIL 并清空内部缓存，调用方负责 close
        pil = self.to_pil()
        self._pil = None
        return pil

    def close(self):
        if self._pil is not None:
            try:
                self._pil.close()
            except Exception:
                pass
            self._pil = None
        return None

    def __getattr__(self, name):
        # 代理 PIL 属性（width/size 等）
        pil = self.to_pil()
        if pil is None:
            raise AttributeError(name)
        return getattr(pil, name)

    def __array__(self, dtype=None):
        import numpy as np

        pil = self.to_pil()
        if pil is None:
            return np.array([], dtype=dtype)
        return np.array(pil, dtype=dtype)

    def __enter__(self):
        return self.to_pil()

    def __exit__(self, exc_type, exc, tb):
        self.close()
        return False

    @staticmethod
    def merge(a, b):
        """
        合并两个 LazyImage 的 blob 列表。
        """
        a_blobs = a._blobs if isinstance(a, LazyImage) else []
        b_blobs = b._blobs if isinstance(b, LazyImage) else []
        combined = a_blobs + b_blobs
        if not combined:
            return None
        merged = LazyImage(combined)
        return merged


# Word 解析器使用的 LazyImage 别名
LazyDocxImage = LazyImage


def ensure_pil_image(img):
    # Image 或 LazyImage → PIL，否则 None
    if isinstance(img, Image.Image):
        return img
    if isinstance(img, LazyImage):
        return img.to_pil()
    return None


def is_image_like(img):
    return isinstance(img, Image.Image) or isinstance(img, LazyImage)


def open_image_for_processing(img, *, allow_bytes=False):
    # 统一入口：返回 (pil, close_after) 供 image2id 等使用
    if isinstance(img, Image.Image):
        return img, False
    if isinstance(img, LazyImage):
        return img.to_pil_detached(), True
    if allow_bytes and isinstance(img, (bytes, bytearray)):
        try:
            pil = Image.open(BytesIO(img)).convert("RGB")
            return pil, True
        except Exception as e:
            logging.info(f"open_image_for_processing: bad bytes: {e}")
            return None, False
    return img, False
