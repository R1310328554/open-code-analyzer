#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-20b docs_src [10:20]."""
from __future__ import annotations

import json
import os
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
SCRIPTS = ROOT / "scripts"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w20b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave20b_batch10_20.py"
MARK_NOTE = "wave20b docs_src [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/response_model/__init__.py": '''\
"""FastAPI 文档示例：响应模型（response_model）与返回类型声明。"""
''',
    "docs_src/response_model/tutorial001_01_py310.py": '''\
"""教程 001-01：函数返回类型注解 Item / list[Item]——FastAPI 据此生成 OpenAPI 响应 schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品模型：name/price 必填；description、tax 与 tags 可选。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list[str] = []


@app.post("/items/")
async def create_item(item: Item) -> Item:
    """返回类型 Item 声明 POST 响应结构；FastAPI 校验并序列化输出。"""
    return item


@app.get("/items/")
async def read_items() -> list[Item]:
    """返回类型 list[Item] 声明 GET 列表响应为 Item 数组。"""
    return [
        Item(name="Portal Gun", price=42.0),
        Item(name="Plumbus", price=32.0),
    ]
''',
    "docs_src/response_model/tutorial001_py310.py": '''\
"""教程 001：response_model=Item / list[Item]——显式声明响应模型，返回 Any 也可被过滤序列化。"""

from typing import Any

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品字段定义；response_model 会据此过滤/校验实际返回的 JSON。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list[str] = []


@app.post("/items/", response_model=Item)
async def create_item(item: Item) -> Any:
    """response_model=Item 覆盖返回类型注解；即使标注 Any 也按 Item 序列化。"""
    return item


@app.get("/items/", response_model=list[Item])
async def read_items() -> Any:
    """可返回 dict 列表；FastAPI 按 list[Item] 校验并转换为 Item JSON。"""
    return [
        {"name": "Portal Gun", "price": 42.0},
        {"name": "Plumbus", "price": 32.0},
    ]
''',
    "docs_src/response_model/tutorial002_py310.py": '''\
"""教程 002（反例）：直接返回 UserIn 会把 password 一并暴露给客户端——生产环境勿用。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class UserIn(BaseModel):
    """含 password 的输入模型；若作为响应返回则敏感字段会泄露。"""

    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


# Don't do this in production!
@app.post("/user/")
async def create_user(user: UserIn) -> UserIn:
    """返回 UserIn 时 password 会出现在响应 JSON 中；应改用 response_model 过滤。"""
    return user
''',
    "docs_src/response_model/tutorial003_01_py310.py": '''\
"""教程 003-01：返回类型 BaseUser——运行时仍接收 UserIn，但响应 JSON 不含 password。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class BaseUser(BaseModel):
    """对外暴露的用户字段：username、email 与 full_name。"""

    username: str
    email: EmailStr
    full_name: str | None = None


class UserIn(BaseUser):
    """请求体模型，在 BaseUser 基础上增加 password。"""

    password: str


@app.post("/user/")
async def create_user(user: UserIn) -> BaseUser:
    """注解 -> BaseUser 使 FastAPI 过滤 password；仅 BaseUser 字段写入响应。"""
    return user
''',
    "docs_src/response_model/tutorial003_02_py310.py": '''\
"""教程 003-02：返回类型 Response——同一 endpoint 可返回 JSONResponse 或 RedirectResponse。"""

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse, RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal")
async def get_portal(teleport: bool = False) -> Response:
    """teleport=True 时 307 重定向；否则返回 JSONResponse 正文。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return JSONResponse(content={"message": "Here's your interdimensional portal."})
''',
    "docs_src/response_model/tutorial003_03_py310.py": '''\
"""教程 003-03：返回类型 RedirectResponse——FastAPI 跳过 JSON 序列化，直接发送重定向。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/teleport")
async def get_teleport() -> RedirectResponse:
    """明确返回 RedirectResponse；OpenAPI 文档会标注 307 Temporary Redirect。"""
    return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
''',
    "docs_src/response_model/tutorial003_04_py310.py": '''\
"""教程 003-04：返回类型 Response | dict——混用 Starlette Response 与普通 dict。"""

from fastapi import FastAPI, Response
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal")
async def get_portal(teleport: bool = False) -> Response | dict:
    """dict 分支由 FastAPI 自动 JSON 化；RedirectResponse 分支直接作为 HTTP 响应。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return {"message": "Here's your interdimensional portal."}
''',
    "docs_src/response_model/tutorial003_05_py310.py": '''\
"""教程 003-05：response_model=None——禁用响应模型校验，允许 Response | dict 联合返回。"""

from fastapi import FastAPI, Response
from fastapi.responses import RedirectResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/portal", response_model=None)
async def get_portal(teleport: bool = False) -> Response | dict:
    """response_model=None 告知 FastAPI 勿对返回值做 Pydantic 过滤/校验。"""
    if teleport:
        return RedirectResponse(url="https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    return {"message": "Here's your interdimensional portal."}
''',
    "docs_src/response_model/tutorial003_py310.py": '''\
"""教程 003：response_model=UserOut——输入 UserIn、输出 UserOut，过滤 password 字段。"""

from typing import Any

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class UserIn(BaseModel):
    """请求体：含 password 的完整注册信息。"""

    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


class UserOut(BaseModel):
    """响应体：仅暴露 username、email 与 full_name，不含 password。"""

    username: str
    email: EmailStr
    full_name: str | None = None


@app.post("/user/", response_model=UserOut)
async def create_user(user: UserIn) -> Any:
    """返回 UserIn 实例；response_model=UserOut 自动剔除 password 再序列化。"""
    return user
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


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-fastapi-w20b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def update_batch_json() -> None:
    """Remove completed wave-20b files from batch.json after marking done."""
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
    if not batch["files"]:
        batch["claimed_at"] = None
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {
        rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_FILES
    }


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

    analyzed_paths = [f"fastapi/0.141.1/analyzed/{rel}" for rel in BATCH_FILES]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "fastapi 0.141.1: Chinese-annotate wave 20b docs_src [10:20]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
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
    update_batch_json()
    queue_paths = [
        "fastapi/0.141.1/_reports/class-queue/done.txt",
        "fastapi/0.141.1/_reports/class-queue/pending.txt",
        "fastapi/0.141.1/_reports/class-queue/batch.json",
        "fastapi/0.141.1/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark fastapi 0.141.1 wave20b docs_src [10:20] done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
