#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-18b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w18b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave18b query_params_str_validations/request_files [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/query_params_str_validations/tutorial012_py310.py": '''\
"""教程 012：Query 声明 list[str] 查询参数，default 为字符串列表默认值。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list[str] = Query(default=["foo", "bar"])):
    """省略 q 时返回默认 ["foo","bar"]；传 ?q=a&q=b 可覆盖为多个值。"""
    query_items = {"q": q}
    return query_items
''',
    "docs_src/query_params_str_validations/tutorial012_an_py310.py": '''\
"""教程 012（Annotated）：Annotated[list[str], Query()] 声明多值查询参数与默认列表。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list[str], Query()] = ["foo", "bar"]):
    """默认值写在参数侧；Query() 承载列表型查询参数元数据。"""
    query_items = {"q": q}
    return query_items
''',
    "docs_src/query_params_str_validations/tutorial013_py310.py": '''\
"""教程 013：未参数化的 list + Query(default=[])——接收重复查询键组成列表。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list = Query(default=[])):
    """?q=foo&q=bar 解析为 ["foo","bar"]；未传 q 时为空列表。"""
    query_items = {"q": q}
    return query_items
''',
    "docs_src/query_params_str_validations/tutorial013_an_py310.py": '''\
"""教程 013（Annotated）：Annotated[list, Query()] 声明可重复的多值查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list, Query()] = []):
    """Annotated 写法；行为与 tutorial013 非 Annotated 版一致。"""
    query_items = {"q": q}
    return query_items
''',
    "docs_src/query_params_str_validations/tutorial014_py310.py": '''\
"""教程 014：Query(include_in_schema=False) 将参数从 OpenAPI/Swagger 文档中隐藏。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    hidden_query: str | None = Query(default=None, include_in_schema=False),
):
    """hidden_query 仍可正常接收 ?hidden_query=；仅不出现在自动生成的 API 文档里。"""
    if hidden_query:
        return {"hidden_query": hidden_query}
    else:
        return {"hidden_query": "Not found"}
''',
    "docs_src/query_params_str_validations/tutorial014_an_py310.py": '''\
"""教程 014（Annotated）：Annotated + Query(include_in_schema=False) 隐藏文档中的查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    hidden_query: Annotated[str | None, Query(include_in_schema=False)] = None,
):
    """Annotated 形式集中声明 include_in_schema；运行时校验与路由行为不变。"""
    if hidden_query:
        return {"hidden_query": hidden_query}
    else:
        return {"hidden_query": "Not found"}
''',
    "docs_src/query_params_str_validations/tutorial015_an_py310.py": '''\
"""教程 015（Annotated）：AfterValidator 为查询参数 id 添加自定义格式校验。"""

import random
from typing import Annotated

from fastapi import FastAPI
from pydantic import AfterValidator

app = FastAPI()  # 创建 FastAPI 应用实例

data = {
    "isbn-9781529046137": "The Hitchhiker's Guide to the Galaxy",
    "imdb-tt0371724": "The Hitchhiker's Guide to the Galaxy",
    "isbn-9781439512982": "Isaac Asimov: The Complete Stories, Vol. 2",
}


def check_valid_id(id: str):
    """id 须以 isbn- 或 imdb- 开头，否则抛出 ValueError 触发 422。"""
    if not id.startswith(("isbn-", "imdb-")):
        raise ValueError('Invalid ID format, it must start with "isbn-" or "imdb-"')
    return id


@app.get("/items/")
async def read_items(
    id: Annotated[str | None, AfterValidator(check_valid_id)] = None,
):
    """提供 id 时查表返回书名；省略 id 时随机返回一条示例记录。"""
    if id:
        item = data.get(id)
    else:
        id, item = random.choice(list(data.items()))
    return {"id": id, "name": item}
''',
    "docs_src/request_files/__init__.py": '''\
"""FastAPI 文档示例：请求体文件上传（File / UploadFile）。"""
''',
    "docs_src/request_files/tutorial001_02_py310.py": '''\
"""教程 001-02：可选文件上传——bytes | None = File(default=None) 与 UploadFile | None。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes | None = File(default=None)):
    """multipart 未附带文件字段时 file 为 None；有文件时读入完整字节并返回长度。"""
    if not file:
        return {"message": "No file sent"}
    else:
        return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile | None = None):
    """UploadFile 流式处理大文件；未上传时返回提示，否则返回原始文件名。"""
    if not file:
        return {"message": "No upload file sent"}
    else:
        return {"filename": file.filename}
''',
    "docs_src/request_files/tutorial001_02_an_py310.py": '''\
"""教程 001-02（Annotated）：Annotated[bytes | None, File()] 声明可选文件字节上传。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes | None, File()] = None):
    """File() 元数据写在 Annotated 内；默认 None 表示文件字段可省略。"""
    if not file:
        return {"message": "No file sent"}
    else:
        return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile | None = None):
    """UploadFile 端点与 tutorial001_02 非 Annotated 版行为相同。"""
    if not file:
        return {"message": "No upload file sent"}
    else:
        return {"filename": file.filename}
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


def tree_guard() -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        if not has_chinese(path.read_text(encoding="utf-8")):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


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
    tree_count = tree_guard()
    print(
        json.dumps(
            {"ok": ok, "failures": failures, "note": MARK_NOTE, "tree_count": tree_count},
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
