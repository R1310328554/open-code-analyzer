#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
EPUB 电子书解析：按 spine 阅读顺序提取 XHTML 章节，委托 HTML 解析器分块。
"""



import logging
import warnings
import zipfile
from io import BytesIO
from xml.etree import ElementTree

from .html_parser import RAGFlowHtmlParser

# OPF 包描述命名空间
# OPF XML namespaces
_OPF_NS = "http://www.idpf.org/2007/opf"
_CONTAINER_NS = "urn:oasis:names:tc:opendocument:xmlns:container"

# 含可读 XHTML 正文的 MIME 类型
# Media types that contain readable XHTML content
_XHTML_MEDIA_TYPES = {"application/xhtml+xml", "text/html", "text/xml"}

logger = logging.getLogger(__name__)


class RAGFlowEpubParser:
    """按 spine 阅读顺序解析 EPUB：提取 XHTML 并分块。

    Parse EPUB files by extracting XHTML content in spine (reading) order
    and delegating to RAGFlowHtmlParser for chunking."""

    def __call__(self, fnm, binary=None, chunk_token_num=512):
        # 打开 ZIP 包，逐 spine 项读取 HTML 并合并分块结果
        if binary is not None:
            if not binary:
                logger.warning(
                    "RAGFlowEpubParser received an empty EPUB binary payload for %r",
                    fnm,
                )
                raise ValueError("Empty EPUB binary payload")
            zf = zipfile.ZipFile(BytesIO(binary))
        else:
            zf = zipfile.ZipFile(fnm)

        try:
            content_items = self._get_spine_items(zf)
            all_sections = []
            html_parser = RAGFlowHtmlParser()

            for item_path in content_items:
                try:
                    html_bytes = zf.read(item_path)
                except KeyError:
                    continue
                if not html_bytes:
                    logger.debug("Skipping empty EPUB content item: %s", item_path)
                    continue
                with warnings.catch_warnings():
                    warnings.filterwarnings("ignore", category=UserWarning)
                    sections = html_parser(item_path, binary=html_bytes, chunk_token_num=chunk_token_num)
                all_sections.extend(sections)

            return all_sections
        finally:
            zf.close()

    @staticmethod
    def _get_spine_items(zf):
        """按 spine 顺序返回内容文件路径。"""
        # 1. Find the OPF file path from META-INF/container.xml
        try:
            container_xml = zf.read("META-INF/container.xml")
        except KeyError:
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        try:
            container_root = ElementTree.fromstring(container_xml)
        except ElementTree.ParseError:
            logger.warning("Failed to parse META-INF/container.xml; falling back to XHTML order.")
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        rootfile_el = container_root.find(f".//{{{_CONTAINER_NS}}}rootfile")
        if rootfile_el is None:
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        opf_path = rootfile_el.get("full-path", "")
        if not opf_path:
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        # Base directory of the OPF file (content paths are relative to it)
        opf_dir = opf_path.rsplit("/", 1)[0] + "/" if "/" in opf_path else ""

        # 2. Parse the OPF file
        try:
            opf_xml = zf.read(opf_path)
        except KeyError:
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        try:
            opf_root = ElementTree.fromstring(opf_xml)
        except ElementTree.ParseError:
            logger.warning("Failed to parse OPF file '%s'; falling back to XHTML order.", opf_path)
            return RAGFlowEpubParser._fallback_xhtml_order(zf)

        # 3. Build id->href+mediatype map from <manifest>
        manifest = {}
        for item in opf_root.findall(f".//{{{_OPF_NS}}}item"):
            item_id = item.get("id", "")
            href = item.get("href", "")
            media_type = item.get("media-type", "")
            if item_id and href:
                manifest[item_id] = (href, media_type)

        # 4. Walk <spine> to get reading order
        spine_items = []
        for itemref in opf_root.findall(f".//{{{_OPF_NS}}}itemref"):
            idref = itemref.get("idref", "")
            if idref not in manifest:
                continue
            href, media_type = manifest[idref]
            if media_type not in _XHTML_MEDIA_TYPES:
                continue
            spine_items.append(opf_dir + href)

        return spine_items if spine_items else RAGFlowEpubParser._fallback_xhtml_order(zf)

    @staticmethod
    def _fallback_xhtml_order(zf):
        """回退：按字母序返回全部 .xhtml/.html 文件。"""
        return sorted(n for n in zf.namelist() if n.lower().endswith((".xhtml", ".html", ".htm")) and not n.startswith("META-INF/"))
