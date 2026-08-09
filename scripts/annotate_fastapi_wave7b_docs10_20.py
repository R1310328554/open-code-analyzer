#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-7b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w7b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

PREPEND: dict[str, str] = {
    "docs_src/custom_response/__init__.py": (
        '"""FastAPI 文档示例：自定义响应类型（response_class、HTMLResponse 等）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/custom_request_and_route/tutorial002_an_py310.py": [
        (
            "from collections.abc import Callable",
            '"""教程 002（Annotated）：自定义 APIRoute，在校验失败时附带原始请求体。"""\n\nfrom collections.abc import Callable',
        ),
        (
            "class ValidationErrorLoggingRoute(APIRoute):",
            'class ValidationErrorLoggingRoute(APIRoute):\n    """捕获 RequestValidationError 并将 body 写入 422 详情。"""',
        ),
        (
            "        original_route_handler = super().get_route_handler()",
            "        original_route_handler = super().get_route_handler()  # 保留 FastAPI 默认处理链",
        ),
        (
            "            except RequestValidationError as exc:",
            "            except RequestValidationError as exc:  # Pydantic/参数校验失败",
        ),
        (
            "                body = await request.body()",
            "                body = await request.body()  # 读取原始 body 便于调试",
        ),
        (
            "                detail = {\"errors\": exc.errors(), \"body\": body.decode()}",
            "                detail = {\"errors\": exc.errors(), \"body\": body.decode()}  # 合并错误与 body",
        ),
        (
            "                raise HTTPException(status_code=422, detail=detail)",
            "                raise HTTPException(status_code=422, detail=detail)  # 仍返回 422",
        ),
        (
            "app.router.route_class = ValidationErrorLoggingRoute",
            "# 全局替换路由类，使所有端点使用自定义校验错误处理\n"
            "app.router.route_class = ValidationErrorLoggingRoute",
        ),
        (
            "async def sum_numbers(numbers: Annotated[list[int], Body()]):",
            "async def sum_numbers(numbers: Annotated[list[int], Body()]):\n"
            '    """接收 JSON 数组并求和；校验失败时响应含原始 body。"""',
        ),
    ],
    "docs_src/custom_request_and_route/tutorial002_py310.py": [
        (
            "from collections.abc import Callable",
            '"""教程 002：自定义 APIRoute 记录校验失败时的请求体（非 Annotated 写法）。"""\n\nfrom collections.abc import Callable',
        ),
        (
            "class ValidationErrorLoggingRoute(APIRoute):",
            'class ValidationErrorLoggingRoute(APIRoute):\n    """包装默认 handler，422 时 detail 包含 errors 与 body。"""',
        ),
        (
            "        original_route_handler = super().get_route_handler()",
            "        original_route_handler = super().get_route_handler()  # 委托给父类生成 handler",
        ),
        (
            "            except RequestValidationError as exc:",
            "            except RequestValidationError as exc:  # 请求体验证失败",
        ),
        (
            "                body = await request.body()",
            "                body = await request.body()  # 再次读取 body 字符串",
        ),
        (
            "                detail = {\"errors\": exc.errors(), \"body\": body.decode()}",
            "                detail = {\"errors\": exc.errors(), \"body\": body.decode()}  # 便于排查错误输入",
        ),
        (
            "                raise HTTPException(status_code=422, detail=detail)",
            "                raise HTTPException(status_code=422, detail=detail)  # 统一 422 响应格式",
        ),
        (
            "app.router.route_class = ValidationErrorLoggingRoute",
            "# 在 app 级别设置 route_class\napp.router.route_class = ValidationErrorLoggingRoute",
        ),
        (
            "async def sum_numbers(numbers: list[int] = Body()):",
            "async def sum_numbers(numbers: list[int] = Body()):\n"
            '    """Body() 声明 JSON 数组；失败时由自定义 Route 附加 body。"""',
        ),
    ],
    "docs_src/custom_request_and_route/tutorial003_py310.py": [
        (
            "import time",
            '"""教程 003：TimedRoute 为指定 router 的路由添加 X-Response-Time 响应头。"""\n\nimport time',
        ),
        (
            "class TimedRoute(APIRoute):",
            'class TimedRoute(APIRoute):\n    """测量 handler 耗时并写入响应头 X-Response-Time。"""',
        ),
        (
            "            before = time.time()",
            "            before = time.time()  # 记录请求开始时间",
        ),
        (
            "            duration = time.time() - before",
            "            duration = time.time() - before  # 计算处理耗时（秒）",
        ),
        (
            '            response.headers["X-Response-Time"] = str(duration)',
            '            response.headers["X-Response-Time"] = str(duration)  # 暴露给客户端',
        ),
        (
            "router = APIRouter(route_class=TimedRoute)",
            "# 仅挂载在此 router 上的路由会计时\nrouter = APIRouter(route_class=TimedRoute)",
        ),
        (
            "async def not_timed():",
            'async def not_timed():\n    """直接注册在 app 上，不使用 TimedRoute。"""',
        ),
        (
            "async def timed():",
            'async def timed():\n    """通过 TimedRoute 注册，响应含 X-Response-Time。"""',
        ),
        (
            "app.include_router(router)",
            "app.include_router(router)  # 合并带计时的子路由",
        ),
    ],
    "docs_src/custom_response/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：用 response_class=UJSONResponse 返回更快的 JSON 序列化。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/items/\", response_class=UJSONResponse)",
            '@app.get("/items/", response_class=UJSONResponse)  # 声明端点使用 UJSON 编码',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """仍返回 Python 对象；FastAPI 用 UJSONResponse 序列化。"""',
        ),
    ],
    "docs_src/custom_response/tutorial001b_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001b：直接返回 ORJSONResponse 实例（绕过默认 JSONResponse）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/items/\", response_class=ORJSONResponse)",
            '@app.get("/items/", response_class=ORJSONResponse)  # OpenAPI 仍标注 ORJSON 类型',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """显式构造 ORJSONResponse；适合需精细控制响应头的场景。"""',
        ),
    ],
    "docs_src/custom_response/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：response_class=HTMLResponse，路由函数返回 HTML 字符串。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/items/\", response_class=HTMLResponse)",
            '@app.get("/items/", response_class=HTMLResponse)  # 文档与 Content-Type 为 text/html',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """返回 HTML 字符串；FastAPI 包装为 HTMLResponse。"""',
        ),
    ],
    "docs_src/custom_response/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：在路由内显式返回 HTMLResponse(content=..., status_code=...)。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """手动构造 HTMLResponse，可指定状态码与 headers。"""',
        ),
        (
            "    return HTMLResponse(content=html_content, status_code=200)",
            "    return HTMLResponse(content=html_content, status_code=200)  # 完全控制响应对象",
        ),
    ],
    "docs_src/custom_response/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 004：response_class 与辅助函数配合，分离 HTML 生成逻辑。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "def generate_html_response():",
            'def generate_html_response():\n    """生成 HTMLResponse；可在多处复用。"""',
        ),
        (
            "@app.get(\"/items/\", response_class=HTMLResponse)",
            '@app.get("/items/", response_class=HTMLResponse)  # 声明响应媒体类型',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """返回 HTMLResponse 实例；response_class 用于 OpenAPI 文档。"""',
        ),
    ],
    "docs_src/custom_response/tutorial005_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 005：PlainTextResponse 返回纯文本而非 JSON。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/\", response_class=PlainTextResponse)",
            '@app.get("/", response_class=PlainTextResponse)  # Content-Type: text/plain',
        ),
        (
            "async def main():",
            'async def main():\n    """返回字符串；客户端收到 text/plain 正文。"""',
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


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
