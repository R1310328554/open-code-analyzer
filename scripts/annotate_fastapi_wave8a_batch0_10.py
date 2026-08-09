#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-8a docs_src slice [0:10]."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
WAVE8A_FILES = Path("/tmp/fastapi_w8a.txt").read_text().strip().splitlines()

ANNOTATED: dict[str, str] = {
    "docs_src/custom_response/tutorial006_py310.py": '''\
"""教程 006：在路由内显式返回 RedirectResponse 实例完成 HTTP 重定向。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/typer")
async def redirect_typer():
    """构造 RedirectResponse；默认 307 临时重定向到 Typer 官网。"""
    return RedirectResponse("https://typer.tiangolo.com")
''',
    "docs_src/custom_response/tutorial006b_py310.py": '''\
"""教程 006b：response_class=RedirectResponse，路由只需返回目标 URL 字符串。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/fastapi", response_class=RedirectResponse)
async def redirect_fastapi():
    """返回 URL 字符串；FastAPI 用 RedirectResponse 包装并写入 Location 头。"""
    return "https://fastapi.tiangolo.com"
''',
    "docs_src/custom_response/tutorial006c_py310.py": '''\
"""教程 006c：RedirectResponse 配合 status_code=302 指定重定向状态码。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/pydantic", response_class=RedirectResponse, status_code=302)
async def redirect_pydantic():
    """302 Found；response_class 与 status_code 可同时在装饰器上声明。"""
    return "https://docs.pydantic.dev/"
''',
    "docs_src/custom_response/tutorial007_py310.py": '''\
"""教程 007：StreamingResponse 配合异步生成器逐块推送响应体。"""

import anyio
from fastapi import FastAPI
from fastapi.responses import StreamingResponse

app = FastAPI()


async def fake_video_streamer():
    """模拟视频流：循环 yield 字节块，anyio.sleep 让出事件循环。"""
    for i in range(10):
        yield b"some fake video bytes"
        await anyio.sleep(0)


@app.get("/")
async def main():
    """StreamingResponse 接受 async generator，适合大文件或实时流。"""
    return StreamingResponse(fake_video_streamer())
''',
    "docs_src/custom_response/tutorial008_py310.py": '''\
"""教程 008：StreamingResponse 通过同步生成器分块读取本地大文件。"""

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/")
def main():
    def iterfile():  # (1) 同步生成器，逐块读取文件
        with open(some_file_path, mode="rb") as file_like:  # (2) 二进制模式打开
            yield from file_like  # (3) 委托给文件对象迭代，避免一次性读入内存

    return StreamingResponse(iterfile(), media_type="video/mp4")
''',
    "docs_src/custom_response/tutorial009_py310.py": '''\
"""教程 009：FileResponse 直接返回本地文件路径，由框架发送文件内容。"""

from fastapi import FastAPI
from fastapi.responses import FileResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/")
async def main():
    """FileResponse 自动设置 Content-Type 与 Content-Disposition 等头。"""
    return FileResponse(some_file_path)
''',
    "docs_src/custom_response/tutorial009b_py310.py": '''\
"""教程 009b：response_class=FileResponse，路由返回文件路径字符串。"""

from fastapi import FastAPI
from fastapi.responses import FileResponse

some_file_path = "large-video-file.mp4"
app = FastAPI()


@app.get("/", response_class=FileResponse)
async def main():
    """返回路径字符串；FastAPI 用 FileResponse 打开并流式发送文件。"""
    return some_file_path
''',
    "docs_src/custom_response/tutorial009c_py310.py": '''\
"""教程 009c：继承 Response 实现 CustomORJSONResponse 自定义 JSON 序列化。"""

from typing import Any

import orjson
from fastapi import FastAPI, Response

app = FastAPI()


class CustomORJSONResponse(Response):
    """自定义 JSON 响应：用 orjson 序列化，带缩进选项。"""

    media_type = "application/json"

    def render(self, content: Any) -> bytes:
        assert orjson is not None, "orjson must be installed"
        return orjson.dumps(content, option=orjson.OPT_INDENT_2)


@app.get("/", response_class=CustomORJSONResponse)
async def main():
    """仍返回 dict；FastAPI 调用 render() 生成响应体字节。"""
    return {"message": "Hello World"}
''',
    "docs_src/custom_response/tutorial010_py310.py": '''\
"""教程 010：default_response_class=HTMLResponse 为整个应用设置默认响应类型。"""

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

app = FastAPI(default_response_class=HTMLResponse)  # 全局默认 HTML，无需每路由声明


@app.get("/items/")
async def read_items():
    """返回 HTML 字符串；未指定 response_class 时使用应用级默认值。"""
    return "<h1>Items</h1><p>This is a list of items.</p>"
''',
    "docs_src/dataclasses_/__init__.py": '''\
"""FastAPI 文档示例：dataclass 与 Pydantic 模型配合（请求/响应体声明）。"""
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    for rel in WAVE8A_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
