#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-9a docs_src/dependencies slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w9a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/dependencies/tutorial002_py310.py": '''\
"""教程 002：可调用类作为依赖，显式类型注解配合 Depends(CommonQueryParams)。"""

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons: CommonQueryParams = Depends(CommonQueryParams)):
    """Depends(CommonQueryParams) 实例化类并注入 commons。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
''',
    "docs_src/dependencies/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：用 Annotated[Any, Depends(...)] 声明类依赖（类型信息较弱）。"""

from typing import Annotated, Any

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons: Annotated[Any, Depends(CommonQueryParams)]):
    """Annotated 将 Depends 与参数绑定；Any 仅作占位，IDE 提示较弱。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
''',
    "docs_src/dependencies/tutorial003_py310.py": '''\
"""教程 003：无类型注解，仅用 commons=Depends(CommonQueryParams) 注入类依赖。"""

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons=Depends(CommonQueryParams)):
    """省略类型注解时编辑器无法推断 commons 类型，但运行时行为相同。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
''',
    "docs_src/dependencies/tutorial004_an_py310.py": '''\
"""教程 004（Annotated）：Annotated[CommonQueryParams, Depends()] 简写，类型与依赖合一。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons: Annotated[CommonQueryParams, Depends()]):
    """Depends() 无参时从 Annotated 首类型参数推断要实例化的依赖类。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
''',
    "docs_src/dependencies/tutorial004_py310.py": '''\
"""教程 004：CommonQueryParams = Depends() 简写，类型注解与 Depends() 配合。"""

from fastapi import Depends, FastAPI

app = FastAPI()


fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


class CommonQueryParams:
    """依赖类：__init__ 参数自动从查询字符串注入。"""

    def __init__(self, q: str | None = None, skip: int = 0, limit: int = 100):
        self.q = q
        self.skip = skip
        self.limit = limit


@app.get("/items/")
async def read_items(commons: CommonQueryParams = Depends()):
    """Depends() 从左侧类型注解推断 CommonQueryParams，等价于 Depends(CommonQueryParams)。"""
    response = {}
    if commons.q:
        response.update({"q": commons.q})
    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片
    response.update({"items": items})
    return response
''',
    "docs_src/dependencies/tutorial005_an_py310.py": '''\
"""教程 005（Annotated）：子依赖链——query_extractor 被 query_or_cookie_extractor 复用。"""

from typing import Annotated

from fastapi import Cookie, Depends, FastAPI

app = FastAPI()


def query_extractor(q: str | None = None):
    """一级依赖：从查询参数 q 取值。"""
    return q


def query_or_cookie_extractor(
    q: Annotated[str, Depends(query_extractor)],
    last_query: Annotated[str | None, Cookie()] = None,
):
    """二级依赖：q 为空时回退到 Cookie last_query。"""
    if not q:
        return last_query
    return q


@app.get("/items/")
async def read_query(
    query_or_default: Annotated[str, Depends(query_or_cookie_extractor)],
):
    """Depends 可嵌套；FastAPI 先解析子依赖再注入外层。"""
    return {"q_or_cookie": query_or_default}
''',
    "docs_src/dependencies/tutorial005_py310.py": '''\
"""教程 005：子依赖链的非 Annotated 写法，q 依赖 query_extractor 并配合 Cookie。"""

from fastapi import Cookie, Depends, FastAPI

app = FastAPI()


def query_extractor(q: str | None = None):
    """一级依赖：从查询参数 q 取值。"""
    return q


def query_or_cookie_extractor(
    q: str = Depends(query_extractor), last_query: str | None = Cookie(default=None)
):
    """二级依赖：q 为空时回退到 Cookie last_query。"""
    if not q:
        return last_query
    return q


@app.get("/items/")
async def read_query(query_or_default: str = Depends(query_or_cookie_extractor)):
    """Depends 参数可声明对其他依赖函数的引用，形成依赖树。"""
    return {"q_or_cookie": query_or_default}
''',
    "docs_src/dependencies/tutorial006_an_py310.py": '''\
"""教程 006（Annotated）：路由级 dependencies 在路径函数执行前校验请求头。"""

from typing import Annotated

from fastapi import Depends, FastAPI, Header, HTTPException

app = FastAPI()


async def verify_token(x_token: Annotated[str, Header()]):
    """校验 X-Token 请求头；失败时抛出 HTTPException。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: Annotated[str, Header()]):
    """校验 X-Key 请求头；通过时返回 x_key 供后续依赖使用。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


@app.get("/items/", dependencies=[Depends(verify_token), Depends(verify_key)])
async def read_items():
    """dependencies 列表中的依赖在 read_items 之前执行，返回值不注入路径函数。"""
    return [{"item": "Foo"}, {"item": "Bar"}]
''',
    "docs_src/dependencies/tutorial006_py310.py": '''\
"""教程 006：路由级 dependencies 的非 Annotated 写法，Header 参数校验令牌。"""

from fastapi import Depends, FastAPI, Header, HTTPException

app = FastAPI()


async def verify_token(x_token: str = Header()):
    """校验 X-Token 请求头；失败时抛出 HTTPException。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: str = Header()):
    """校验 X-Key 请求头；通过时返回 x_key 供后续依赖使用。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


@app.get("/items/", dependencies=[Depends(verify_token), Depends(verify_key)])
async def read_items():
    """dependencies 列表中的依赖在 read_items 之前执行，返回值不注入路径函数。"""
    return [{"item": "Foo"}, {"item": "Bar"}]
''',
    "docs_src/dependencies/tutorial007_py310.py": '''\
"""教程 007：yield 依赖——请求结束后在 finally 中关闭资源（如数据库会话）。"""


async def get_db():
    """生成器依赖：yield 前创建资源，yield 后执行 cleanup。"""
    db = DBSession()
    try:
        yield db
    finally:
        db.close()
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists():
        shutil.copy2(src, dst)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def tree_guard() -> None:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    if tracked < 59000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected ~59k+)")


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
            "wave9a",
            *BATCH_FILES,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    tree_guard()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
