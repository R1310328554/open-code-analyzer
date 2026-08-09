#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-10a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w10a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/dependencies/tutorial010_py310.py": '''\
"""教程 010：在 yield 依赖中使用同步上下文管理器（with ... as db）。"""


class MySuperContextManager:
    """包装 DBSession 的同步上下文管理器，__exit__ 中关闭连接。"""

    def __init__(self):
        self.db = DBSession()

    def __enter__(self):
        return self.db

    def __exit__(self, exc_type, exc_value, traceback):
        self.db.close()


async def get_db():
    """with 块结束后 __exit__ 自动关闭 db；yield 仍向路径函数提供会话。"""
    with MySuperContextManager() as db:
        yield db
''',
    "docs_src/dependencies/tutorial011_an_py310.py": '''\
"""教程 011（Annotated）：可调用类实例作为依赖——__call__ 接收查询参数。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


class FixedContentQueryChecker:
    """构造时固定待匹配子串；__call__ 判断查询参数 q 是否包含该子串。"""

    def __init__(self, fixed_content: str):
        self.fixed_content = fixed_content

    def __call__(self, q: str = ""):
        if q:
            return self.fixed_content in q
        return False


checker = FixedContentQueryChecker("bar")  # 预配置实例，Depends(checker) 直接注入


@app.get("/query-checker/")
async def read_query_check(fixed_content_included: Annotated[bool, Depends(checker)]):
    """Depends(实例) 调用 checker.__call__，q 来自查询字符串。"""
    return {"fixed_content_in_query": fixed_content_included}
''',
    "docs_src/dependencies/tutorial011_py310.py": '''\
"""教程 011：可调用类实例作为依赖的非 Annotated 写法。"""

from fastapi import Depends, FastAPI

app = FastAPI()


class FixedContentQueryChecker:
    """构造时固定待匹配子串；__call__ 判断查询参数 q 是否包含该子串。"""

    def __init__(self, fixed_content: str):
        self.fixed_content = fixed_content

    def __call__(self, q: str = ""):
        if q:
            return self.fixed_content in q
        return False


checker = FixedContentQueryChecker("bar")  # 预配置实例，Depends(checker) 直接注入


@app.get("/query-checker/")
async def read_query_check(fixed_content_included: bool = Depends(checker)):
    """Depends(checker) 等价于调用实例的 __call__(q=...)。"""
    return {"fixed_content_in_query": fixed_content_included}
''',
    "docs_src/dependencies/tutorial012_an_py310.py": '''\
"""教程 012（Annotated）：应用级 dependencies——所有路由共享 Header 校验。"""

from typing import Annotated

from fastapi import Depends, FastAPI, Header, HTTPException


async def verify_token(x_token: Annotated[str, Header()]):
    """校验 X-Token 请求头；无效则 HTTP 400。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: Annotated[str, Header()]):
    """校验 X-Key 请求头；通过时返回 x_key。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


app = FastAPI(dependencies=[Depends(verify_token), Depends(verify_key)])  # 全局依赖


@app.get("/items/")
async def read_items():
    """无需重复声明依赖，应用级列表已对所有路由生效。"""
    return [{"item": "Portal Gun"}, {"item": "Plumbus"}]


@app.get("/users/")
async def read_users():
    """同样受 verify_token / verify_key 保护。"""
    return [{"username": "Rick"}, {"username": "Morty"}]
''',
    "docs_src/dependencies/tutorial012_py310.py": '''\
"""教程 012：应用级 dependencies 的非 Annotated 写法。"""

from fastapi import Depends, FastAPI, Header, HTTPException


async def verify_token(x_token: str = Header()):
    """校验 X-Token 请求头；无效则 HTTP 400。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: str = Header()):
    """校验 X-Key 请求头；通过时返回 x_key。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


app = FastAPI(dependencies=[Depends(verify_token), Depends(verify_key)])  # 全局依赖


@app.get("/items/")
async def read_items():
    """FastAPI(...) 的 dependencies 参数作用于该应用下全部路由。"""
    return [{"item": "Portal Gun"}, {"item": "Plumbus"}]


@app.get("/users/")
async def read_users():
    """每个端点执行前都会先运行 verify_token 与 verify_key。"""
    return [{"username": "Rick"}, {"username": "Morty"}]
''',
    "docs_src/dependencies/tutorial013_an_py310.py": '''\
"""教程 013（Annotated）：路由 dependencies + 流式响应；get_user 校验 user_id 授权。"""

import time
from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from sqlmodel import Field, Session, SQLModel, create_engine

engine = create_engine("postgresql+psycopg://postgres:postgres@localhost/db")


class User(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str


app = FastAPI()


def get_session():
    """yield SQLModel Session，请求结束后自动退出 with 块。"""
    with Session(engine) as session:
        yield session


def get_user(user_id: int, session: Annotated[Session, Depends(get_session)]):
    """根据 user_id 查库；不存在则 403。session 由 get_session 注入。"""
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=403, detail="Not authorized")


def generate_stream(query: str):
    """逐字符 yield 并 sleep，模拟慢速流式输出。"""
    for ch in query:
        yield ch
        time.sleep(0.1)


@app.get("/generate", dependencies=[Depends(get_user)])
def generate(query: str):
    """Depends(get_user) 在 generate 前执行；user_id 仍来自查询参数。"""
    return StreamingResponse(content=generate_stream(query))
''',
    "docs_src/dependencies/tutorial014_an_py310.py": '''\
"""教程 014（Annotated）：演示流式响应依赖中过早 session.close() 的问题。"""

import time
from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from sqlmodel import Field, Session, SQLModel, create_engine

engine = create_engine("postgresql+psycopg://postgres:postgres@localhost/db")


class User(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str


app = FastAPI()


def get_session():
    """yield SQLModel Session，请求结束后自动退出 with 块。"""
    with Session(engine) as session:
        yield session


def get_user(user_id: int, session: Annotated[Session, Depends(get_session)]):
    """校验用户后立即 close session——流式响应期间会话可能已不可用。"""
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=403, detail="Not authorized")
    session.close()  # 过早关闭：StreamingResponse 返回后 generator 可能仍需要 session


def generate_stream(query: str):
    """逐字符 yield 并 sleep，模拟慢速流式输出。"""
    for ch in query:
        yield ch
        time.sleep(0.1)


@app.get("/generate", dependencies=[Depends(get_user)])
def generate(query: str):
    """与 tutorial013 对比：此处 get_user 提前关闭 session 会导致资源管理问题。"""
    return StreamingResponse(content=generate_stream(query))
''',
    "docs_src/dependency_testing/__init__.py": '''\
"""FastAPI 文档示例：测试依赖覆盖（dependency_overrides）。"""
''',
    "docs_src/dependency_testing/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：TestClient 测试时用 app.dependency_overrides 替换依赖。"""

from typing import Annotated

from fastapi import Depends, FastAPI
from fastapi.testclient import TestClient

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """共享依赖：收集 q/skip/limit 查询参数并返回 dict。"""
    return {"q": q, "skip": skip, "limit": limit}


@app.get("/items/")
async def read_items(commons: Annotated[dict, Depends(common_parameters)]):
    """注入 common_parameters 的返回值。"""
    return {"message": "Hello Items!", "params": commons}


@app.get("/users/")
async def read_users(commons: Annotated[dict, Depends(common_parameters)]):
    """同一依赖可复用于多个路由。"""
    return {"message": "Hello Users!", "params": commons}


client = TestClient(app)


async def override_dependency(q: str | None = None):
    """测试替身：固定 skip=5、limit=10，忽略请求中的 skip/limit。"""
    return {"q": q, "skip": 5, "limit": 10}


app.dependency_overrides[common_parameters] = override_dependency  # 键为原依赖可调用对象


def test_override_in_items():
    """无查询参数时，override 返回 skip=5、limit=10。"""
    response = client.get("/items/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": None, "skip": 5, "limit": 10},
    }


def test_override_in_items_with_q():
    """?q=foo 时 override 仍保留 q，skip/limit 被固定。"""
    response = client.get("/items/?q=foo")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }


def test_override_in_items_with_params():
    """即使传 skip/limit，override 仍覆盖为 5/10（演示 overrides 优先级）。"""
    response = client.get("/items/?q=foo&skip=100&limit=200")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }
''',
    "docs_src/dependency_testing/tutorial001_py310.py": '''\
"""教程 001：TestClient 测试时用 app.dependency_overrides 替换依赖。"""

from fastapi import Depends, FastAPI
from fastapi.testclient import TestClient

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """共享依赖：收集 q/skip/limit 查询参数并返回 dict。"""
    return {"q": q, "skip": skip, "limit": limit}


@app.get("/items/")
async def read_items(commons: dict = Depends(common_parameters)):
    """注入 common_parameters 的返回值。"""
    return {"message": "Hello Items!", "params": commons}


@app.get("/users/")
async def read_users(commons: dict = Depends(common_parameters)):
    """同一依赖可复用于多个路由。"""
    return {"message": "Hello Users!", "params": commons}


client = TestClient(app)


async def override_dependency(q: str | None = None):
    """测试替身：固定 skip=5、limit=10，忽略请求中的 skip/limit。"""
    return {"q": q, "skip": 5, "limit": 10}


app.dependency_overrides[common_parameters] = override_dependency  # 键为原依赖可调用对象


def test_override_in_items():
    """无查询参数时，override 返回 skip=5、limit=10。"""
    response = client.get("/items/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": None, "skip": 5, "limit": 10},
    }


def test_override_in_items_with_q():
    """?q=foo 时 override 仍保留 q，skip/limit 被固定。"""
    response = client.get("/items/?q=foo")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }


def test_override_in_items_with_params():
    """即使传 skip/limit，override 仍覆盖为 5/10（演示 overrides 优先级）。"""
    response = client.get("/items/?q=foo&skip=100&limit=200")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }
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
            "wave10a",
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
