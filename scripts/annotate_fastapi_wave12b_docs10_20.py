#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-12b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w12b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave12b handling_errors/header_param_models/header_params [10:20]"

PREPEND: dict[str, str] = {
    "docs_src/header_param_models/__init__.py": (
        '"""FastAPI 文档示例：用 Pydantic 模型声明 Header 参数。"""\n'
    ),
    "docs_src/header_params/__init__.py": (
        '"""FastAPI 文档示例：Header 参数（Header parameters）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/handling_errors/tutorial006_py310.py": [
        (
            "from fastapi import FastAPI, HTTPException",
            '"""教程 006：注册自定义异常处理器，记录日志后委托默认 handler 返回标准响应。"""\n\nfrom fastapi import FastAPI, HTTPException',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.exception_handler(StarletteHTTPException)\nasync def custom_http_exception_handler(request, exc):",
            '@app.exception_handler(StarletteHTTPException)\nasync def custom_http_exception_handler(request, exc):\n    """捕获 HTTP 异常：打印调试信息后调用内置 http_exception_handler。"""',
        ),
        (
            '    print(f"OMG! An HTTP error!: {repr(exc)}")',
            '    print(f"OMG! An HTTP error!: {repr(exc)}")  # 自定义日志（保留英文示例输出）',
        ),
        (
            "    return await http_exception_handler(request, exc)",
            "    return await http_exception_handler(request, exc)  # 委托默认处理，保持标准 JSON 响应",
        ),
        (
            "@app.exception_handler(RequestValidationError)\nasync def validation_exception_handler(request, exc):",
            '@app.exception_handler(RequestValidationError)\nasync def validation_exception_handler(request, exc):\n    """捕获请求校验错误：打印无效数据后调用默认 validation handler。"""',
        ),
        (
            '    print(f"OMG! The client sent invalid data!: {exc}")',
            '    print(f"OMG! The client sent invalid data!: {exc}")  # 自定义日志',
        ),
        (
            "    return await request_validation_exception_handler(request, exc)",
            "    return await request_validation_exception_handler(request, exc)  # 返回标准 422 响应",
        ),
        (
            "@app.get(\"/items/{item_id}\")\nasync def read_item(item_id: int):",
            '@app.get("/items/{item_id}")\nasync def read_item(item_id: int):\n    """读取 item；item_id 为 3 时主动抛出 HTTPException 演示自定义 handler。"""',
        ),
        (
            '        raise HTTPException(status_code=418, detail="Nope! I don\'t like 3.")',
            '        raise HTTPException(status_code=418, detail="Nope! I don\'t like 3.")  # 418 触发自定义 HTTP handler',
        ),
    ],
    "docs_src/header_param_models/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 001（Annotated）：用 Pydantic 模型一次性声明多个 Header 字段。"""\n\nfrom typing import Annotated',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """从请求 Header 解析出的字段集合；连字符名自动映射为下划线字段。"""',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # Save-Data 头（save-data -> save_data）",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent（分布式追踪）",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # X-Tag，可重复出现，解析为列表",
        ),
        (
            "async def read_items(headers: Annotated[CommonHeaders, Header()]):",
            'async def read_items(headers: Annotated[CommonHeaders, Header()]):\n    """Header() 将模型各字段映射为 HTTP 头并注入。"""',
        ),
    ],
    "docs_src/header_param_models/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI, Header",
            '"""教程 001：Pydantic Header 模型（非 Annotated 默认参数写法）。"""\n\nfrom fastapi import FastAPI, Header',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """Header 字段模型；字段名对应 HTTP 头（连字符转下划线）。"""',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # Save-Data",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # 重复 X-Tag 头合并为列表",
        ),
        (
            "async def read_items(headers: CommonHeaders = Header()):",
            'async def read_items(headers: CommonHeaders = Header()):\n    """`= Header()` 声明整个模型来自请求 Header。"""',
        ),
    ],
    "docs_src/header_param_models/tutorial002_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 002（Annotated）：Header 模型禁止额外字段（extra=forbid）。"""\n\nfrom typing import Annotated',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """仅允许声明过的 Header；未知头将导致校验失败。"""',
        ),
        (
            '    model_config = {"extra": "forbid"}',
            '    model_config = {"extra": "forbid"}  # 拒绝模型未定义的 Header',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # Save-Data",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # X-Tag 列表",
        ),
        (
            "async def read_items(headers: Annotated[CommonHeaders, Header()]):",
            'async def read_items(headers: Annotated[CommonHeaders, Header()]):\n    """返回校验通过的 Header 模型；多余请求头会触发 422。"""',
        ),
    ],
    "docs_src/header_param_models/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI, Header",
            '"""教程 002：Header 模型 extra=forbid（非 Annotated 写法）。"""\n\nfrom fastapi import FastAPI, Header',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """声明允许的 Header 集合；额外请求头不被接受。"""',
        ),
        (
            '    model_config = {"extra": "forbid"}',
            '    model_config = {"extra": "forbid"}  # Pydantic 拒绝未声明字段',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # Save-Data",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # X-Tag 列表",
        ),
        (
            "async def read_items(headers: CommonHeaders = Header()):",
            'async def read_items(headers: CommonHeaders = Header()):\n    """解析 Header 为模型；未知头键会校验失败。"""',
        ),
    ],
    "docs_src/header_param_models/tutorial003_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 003（Annotated）：Header(convert_underscores=False) 禁用下划线与连字符自动转换。"""\n\nfrom typing import Annotated',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """Header 字段模型；convert_underscores=False 时字段名须与头名一致。"""',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # 须与 Save-Data 等标准头名按规则匹配",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # X-Tag 列表",
        ),
        (
            "    headers: Annotated[CommonHeaders, Header(convert_underscores=False)],\n):",
            '    headers: Annotated[CommonHeaders, Header(convert_underscores=False)],  # 不将字段名下划线转为连字符\n):\n    """按原始头名解析；适用于含下划线的自定义 Header 名。"""',
        ),
    ],
    "docs_src/header_param_models/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI, Header",
            '"""教程 003：Header(convert_underscores=False) 禁用下划线转换（非 Annotated 写法）。"""\n\nfrom fastapi import FastAPI, Header',
        ),
        (
            "class CommonHeaders(BaseModel):",
            'class CommonHeaders(BaseModel):\n    """Header 模型；convert_underscores=False 保持字段名与头名对应关系。"""',
        ),
        (
            "    host: str",
            "    host: str  # Host 头",
        ),
        (
            "    save_data: bool",
            "    save_data: bool  # Save-Data",
        ),
        (
            "    if_modified_since: str | None = None",
            "    if_modified_since: str | None = None  # If-Modified-Since",
        ),
        (
            "    traceparent: str | None = None",
            "    traceparent: str | None = None  # traceparent",
        ),
        (
            "    x_tag: list[str] = []",
            "    x_tag: list[str] = []  # X-Tag 列表",
        ),
        (
            "async def read_items(headers: CommonHeaders = Header(convert_underscores=False)):",
            'async def read_items(headers: CommonHeaders = Header(convert_underscores=False)):\n    """`= Header(convert_underscores=False)` 按原始头名解析整个模型。"""',
        ),
    ],
    "docs_src/header_params/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 001（Annotated）：从请求 Header 读取可选 User-Agent。"""\n\nfrom typing import Annotated',
        ),
        (
            "async def read_items(user_agent: Annotated[str | None, Header()] = None):",
            'async def read_items(user_agent: Annotated[str | None, Header()] = None):\n    """Header() 将 User-Agent 头解析为 user_agent；未携带时返回 None。"""',
        ),
        (
            '    return {"User-Agent": user_agent}',
            '    return {"User-Agent": user_agent}  # 以 JSON 回显解析到的 UA',
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, rel: str) -> str:
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old not in text:
            if has_chinese(text):
                continue
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if rel in PREPEND:
        if text.strip():
            if not text.startswith('"""'):
                text = PREPEND[rel] + text
        else:
            text = PREPEND[rel]
    text = apply_replacements(text, rel)
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


def update_batch_queue() -> None:
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done_set = {ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()}
    batch["files"] = [f for f in batch.get("files", []) if f not in done_set]
    batch["done"] = len(done_set)
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "fastapi",
            "--version",
            "0.141.1",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_queue()
    print(f"Marked {len(BATCH_FILES)} files done in queue (note={MARK_NOTE})")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
