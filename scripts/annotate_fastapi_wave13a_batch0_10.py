#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-13a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w13a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W13B_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w13b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/header_params/tutorial001_py310.py": '''\
"""教程 001：从请求 Header 读取可选 User-Agent。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(user_agent: str | None = Header(default=None)):
    """Header() 将 User-Agent 头解析为 user_agent；未携带时返回 None。"""
    return {"User-Agent": user_agent}  # 以 JSON 回显解析到的 UA
''',
    "docs_src/header_params/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：Header(convert_underscores=False) 保留参数名中的下划线。"""

from typing import Annotated

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(
    strange_header: Annotated[str | None, Header(convert_underscores=False)] = None,
):
    """默认会把 strange_header 映射为 Strange-Header；False 则按原名匹配请求头。"""
    return {"strange_header": strange_header}
''',
    "docs_src/header_params/tutorial002_py310.py": '''\
"""教程 002：非 Annotated 写法——Header(convert_underscores=False) 禁用下划线转连字符。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(
    strange_header: str | None = Header(default=None, convert_underscores=False),
):
    """与 Annotated 版等价：请求头名需与参数名一致（含下划线）。"""
    return {"strange_header": strange_header}
''',
    "docs_src/header_params/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：list[str] Header 接收同名头的多个值（如 X-Token）。"""

from typing import Annotated

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(x_token: Annotated[list[str] | None, Header()] = None):
    """重复 X-Token 头会聚合为字符串列表；无该头时返回 None。"""
    return {"X-Token values": x_token}
''',
    "docs_src/header_params/tutorial003_py310.py": '''\
"""教程 003：list[str] Header 接收重复头名的全部值。"""

from fastapi import FastAPI, Header

app = FastAPI()


@app.get("/items/")
async def read_items(x_token: list[str] | None = Header(default=None)):
    """x_token 映射 X-Token；多个同名头合并为 list[str]。"""
    return {"X-Token values": x_token}
''',
    "docs_src/json_base64_bytes/__init__.py": '''\
"""FastAPI 文档示例：JSON 中 bytes 字段以 Base64 编解码（val_json_bytes / ser_json_bytes）。"""
''',
    "docs_src/json_base64_bytes/tutorial001_py310.py": '''\
"""教程 001：Pydantic model_config 控制 JSON 请求/响应中 bytes 的 Base64 序列化与反序列化。"""

from fastapi import FastAPI
from pydantic import BaseModel


class DataInput(BaseModel):
    """请求体：data 字段从 JSON Base64 解码为 bytes。"""
    description: str
    data: bytes

    model_config = {"val_json_bytes": "base64"}  # 反序列化：Base64 -> bytes


class DataOutput(BaseModel):
    """响应体：data 字段序列化为 JSON Base64 字符串。"""
    description: str
    data: bytes

    model_config = {"ser_json_bytes": "base64"}  # 序列化：bytes -> Base64


class DataInputOutput(BaseModel):
    """请求与响应均使用 Base64 传输 bytes。"""
    description: str
    data: bytes

    model_config = {
        "val_json_bytes": "base64",
        "ser_json_bytes": "base64",
    }


app = FastAPI()


@app.post("/data")
def post_data(body: DataInput):
    """接收 Base64 编码的 data，解码后以 UTF-8 文本回显 content。"""
    content = body.data.decode("utf-8")
    return {"description": body.description, "content": content}


@app.get("/data")
def get_data() -> DataOutput:
    """返回 DataOutput，data 在 OpenAPI/JSON 中以 Base64 呈现。"""
    data = "hello".encode("utf-8")
    return DataOutput(description="A plumbus", data=data)


@app.post("/data-in-out")
def post_data_in_out(body: DataInputOutput) -> DataInputOutput:
    """原样回传请求体，bytes 字段全程 Base64 往返。"""
    return body
''',
    "docs_src/metadata/__init__.py": '''\
"""FastAPI 文档示例：应用元数据（title、description、contact、license 等 OpenAPI 字段）。"""
''',
    "docs_src/metadata/tutorial001_1_py310.py": '''\
"""教程 001.1：license_info 使用 identifier（SPDX 标识符）而非 url。"""

from fastapi import FastAPI

description = """
ChimichangApp API 帮你做很酷的事。🚀

## Items

你可以 **读取 items**。

## Users

你将能够：

* **创建 users**（_未实现_）。
* **读取 users**（_未实现_）。
"""

app = FastAPI(
    title="ChimichangApp",
    description=description,  # 出现在 /docs 顶部的 Markdown 描述
    summary="Deadpool 最爱的应用，无需多言。",
    version="0.0.1",
    terms_of_service="http://example.com/terms/",
    contact={
        "name": "Deadpoolio the Amazing",
        "url": "http://x-force.example.com/contact/",
        "email": "dp@x-force.example.com",
    },
    license_info={
        "name": "Apache 2.0",
        "identifier": "Apache-2.0",  # SPDX 许可证标识符
    },
)


@app.get("/items/")
async def read_items():
    """示例路由；元数据不影响业务逻辑，仅丰富 OpenAPI 文档。"""
    return [{"name": "Katana"}]
''',
    "docs_src/metadata/tutorial001_py310.py": '''\
"""教程 001：FastAPI 构造函数元数据——title、description、summary、contact、license_info（含 URL）。"""

from fastapi import FastAPI

description = """
ChimichangApp API 帮你做很酷的事。🚀

## Items

你可以 **读取 items**。

## Users

你将能够：

* **创建 users**（_未实现_）。
* **读取 users**（_未实现_）。
"""

app = FastAPI(
    title="ChimichangApp",
    description=description,  # 出现在 /docs 顶部的 Markdown 描述
    summary="Deadpool 最爱的应用，无需多言。",
    version="0.0.1",
    terms_of_service="http://example.com/terms/",
    contact={
        "name": "Deadpoolio the Amazing",
        "url": "http://x-force.example.com/contact/",
        "email": "dp@x-force.example.com",
    },
    license_info={
        "name": "Apache 2.0",
        "url": "https://www.apache.org/licenses/LICENSE-2.0.html",  # 许可证文档链接
    },
)


@app.get("/items/")
async def read_items():
    """示例路由；元数据不影响业务逻辑，仅丰富 OpenAPI 文档。"""
    return [{"name": "Katana"}]
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists() and rel.endswith("__init__.py"):
        dst.parent.mkdir(parents=True, exist_ok=True)
    elif not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    else:
        dst.parent.mkdir(parents=True, exist_ok=True)
        if not dst.exists():
            shutil.copy2(src, dst)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def update_batch_json() -> None:
    """Mark w13a done; keep w13b files in batch.json."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["files"] = W13B_FILES
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
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
            "wave13a header_params/json_base64/metadata [0:10]",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
