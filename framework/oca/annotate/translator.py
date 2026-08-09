from __future__ import annotations

import hashlib
import json
import re
import time
from pathlib import Path

from ..util.fs import ensure_dir


def _mostly_chinese(text: str) -> bool:
    cn = sum(1 for ch in text if "\u4e00" <= ch <= "\u9fff")
    en = sum(1 for ch in text if ("a" <= ch.lower() <= "z"))
    return cn >= 2 and cn >= en


class ZhTranslator:
    """带磁盘缓存的英→中翻译器。"""

    def __init__(self, cache_path: Path, *, sleep_s: float = 0.02):
        self.cache_path = cache_path
        ensure_dir(cache_path.parent)
        self.sleep_s = sleep_s
        self.cache: dict[str, str] = {}
        if cache_path.exists():
            try:
                self.cache = json.loads(cache_path.read_text(encoding="utf-8"))
            except Exception:
                self.cache = {}
        self._backend = None
        self._dirty = 0

    def _get_backend(self):
        if self._backend is None:
            from deep_translator import GoogleTranslator

            self._backend = GoogleTranslator(source="en", target="zh-CN")
        return self._backend

    @staticmethod
    def _key(text: str) -> str:
        return hashlib.sha1(text.encode("utf-8")).hexdigest()

    def translate(self, text: str) -> str:
        text = text.strip()
        if not text:
            return text
        if _mostly_chinese(text):
            return text
        if re.fullmatch(r"[\W\d_]+", text):
            return text
        # 已经是占位保护串则直接返回
        if text.startswith("OCAJAVA") and text.endswith("DOC") and " " not in text:
            return text

        k = self._key(text)
        if k in self.cache:
            return self.cache[k]

        try:
            out = self._get_backend().translate(text)
            time.sleep(self.sleep_s)
        except Exception as ex:
            msg = str(ex)
            print(f"[oca-translate] warn: {msg[:160]}")
            simplified = re.sub(r"OCAJAVA\d+DOC", " X ", text)
            simplified = re.sub(r"\s+", " ", simplified).strip()
            try:
                if simplified and simplified != text:
                    out = self._get_backend().translate(simplified)
                    time.sleep(self.sleep_s)
                else:
                    out = text
            except Exception:
                out = text

        # 翻译失败（仍是原文）不写入缓存，以便下次重试
        if out.strip() != text.strip() or _mostly_chinese(out):
            self.cache[k] = out
            self._dirty += 1
            if self._dirty >= 15:
                self.flush()
        return out

    def flush(self) -> None:
        self.cache_path.write_text(
            json.dumps(self.cache, ensure_ascii=False, indent=0),
            encoding="utf-8",
        )
        self._dirty = 0
