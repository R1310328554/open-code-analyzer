#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
"""
文件 HTTP 响应工具：Content-Type/Content-Disposition 解析与安全预览/下载头设置。
"""

#

import re
from urllib.parse import urlencode

# 扩展名 → MIME 类型映射
CONTENT_TYPE_MAP = {
    "docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "doc": "application/msword",
    "pdf": "application/pdf",
    "csv": "text/csv",
    "xls": "application/vnd.ms-excel",
    "xlsx": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "txt": "text/plain",
    "py": "text/plain",
    "js": "text/plain",
    "java": "text/plain",
    "c": "text/plain",
    "cpp": "text/plain",
    "h": "text/plain",
    "php": "text/plain",
    "go": "text/plain",
    "ts": "text/plain",
    "sh": "text/plain",
    "cs": "text/plain",
    "kt": "text/plain",
    "sql": "text/plain",
    "md": "text/markdown",
    "markdown": "text/markdown",
    "mdx": "text/markdown",
    "htm": "text/html",
    "html": "text/html",
    "json": "application/json",
    "png": "image/png",
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg",
    "gif": "image/gif",
    "bmp": "image/bmp",
    "tiff": "image/tiff",
    "tif": "image/tiff",
    "webp": "image/webp",
    "svg": "image/svg+xml",
    "ico": "image/x-icon",
    "avif": "image/avif",
    "heic": "image/heic",
    "ppt": "application/vnd.ms-powerpoint",
    "pptx": "application/vnd.openxmlformats-officedocument.presentationml.presentation",
}

# 强制 attachment 的扩展名（防 XSS/嗅探）
FORCE_ATTACHMENT_EXTENSIONS = {
    "htm",
    "html",
    "shtml",
    "xht",
    "xhtml",
    "xml",
    "mhtml",
    "svg",
}

FORCE_ATTACHMENT_CONTENT_TYPES = {
    "text/html",
    "image/svg+xml",
    "application/xhtml+xml",
    "text/xml",
    "application/xml",
    "multipart/related",
}


def should_force_attachment(ext: str | None, content_type: str | None = None) -> bool:
    # 按扩展名或 MIME 判断是否必须 attachment 下载
    normalized_ext = (ext or "").lower().strip(".")
    if normalized_ext in FORCE_ATTACHMENT_EXTENSIONS:
        return True
    normalized_type = (content_type or "").lower().split(";")[0].strip()
    return normalized_type in FORCE_ATTACHMENT_CONTENT_TYPES


def sanitize_content_disposition_filename(filename: str | None) -> str | None:
    # 清洗文件名，仅保留安全字符
    if not filename:
        return None
    base = re.sub(r"[^\w.\-]", "_", str(filename).split("/")[-1].split("\\")[-1])
    return base or None


def resolve_attachment_content_type(ext: str | None = None, mime_type: str | None = None) -> tuple[str | None, str | None]:
    # 由 mime 或扩展名解析 (content_type, ext)
    if mime_type:
        normalized_type = mime_type.lower().split(";")[0].strip()
        for known_ext, known_type in CONTENT_TYPE_MAP.items():
            if known_type == normalized_type:
                return normalized_type, known_ext
        return normalized_type, (ext or "").lower().strip(".") or None
    if ext:
        normalized_ext = ext.lower().strip(".")
        return CONTENT_TYPE_MAP.get(normalized_ext, f"application/{normalized_ext}"), normalized_ext
    return None, None


# 设置 inline 预览响应头；HTML/SVG 等强制 attachment
def apply_preview_file_response_headers(
    response,
    content_type: str | None,
    ext: str | None = None,
    filename: str | None = None,
):
    if content_type:
        response.headers.set("Content-Type", content_type)
    if should_force_attachment(ext, content_type):
        response.headers.set("X-Content-Type-Options", "nosniff")
        response.headers.set("Content-Disposition", "attachment")
        return response
    safe_filename = sanitize_content_disposition_filename(filename)
    if safe_filename:
        response.headers.set("Content-Disposition", f'inline; filename="{safe_filename}"')
    else:
        response.headers.set("Content-Disposition", "inline")
    return response


# 设置 attachment 下载响应头
def apply_download_file_response_headers(
    response,
    content_type: str | None,
    ext: str | None = None,
    filename: str | None = None,
):
    if content_type:
        response.headers.set("Content-Type", content_type)
    if should_force_attachment(ext, content_type):
        response.headers.set("X-Content-Type-Options", "nosniff")
        response.headers.set("Content-Disposition", "attachment")
        return response
    safe_filename = sanitize_content_disposition_filename(filename)
    if safe_filename:
        response.headers.set("Content-Disposition", f'attachment; filename="{safe_filename}"')
    else:
        response.headers.set("Content-Disposition", "attachment")
    return response


def agent_attachment_preview_path(attachment_id: str, *, ext: str | None = None, mime_type: str | None = None) -> str:
    # 构造 Agent 附件预览 API 路径（可选 ext/mime 查询参数）
    query: dict[str, str] = {}
    if ext:
        query["ext"] = ext
    if mime_type:
        query["mime_type"] = mime_type
    suffix = f"?{urlencode(query)}" if query else ""
    return f"/api/v1/agents/attachments/{attachment_id}/preview{suffix}"
