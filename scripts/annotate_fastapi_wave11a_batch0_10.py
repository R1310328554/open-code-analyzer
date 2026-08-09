#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-11a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w11a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/extra_data_types/tutorial001_py310.py": '''\
"""教程 001：请求体中使用 UUID、datetime、timedelta、time 等额外类型（非 Annotated 写法）。"""

from datetime import datetime, time, timedelta
from uuid import UUID

from fastapi import Body, FastAPI

app = FastAPI()


@app.put("/items/{item_id}")
async def read_items(
    item_id: UUID,  # 路径参数自动转为 UUID 实例
    start_datetime: datetime = Body(),  # JSON 字符串 → datetime
    end_datetime: datetime = Body(),  # 结束时间
    process_after: timedelta = Body(),  # 延迟时长（如 ISO 8601 duration）
    repeat_at: time | None = Body(default=None),  # 可选的每日重复时刻
):
    start_process = start_datetime + process_after  # datetime + timedelta 运算
    duration = end_datetime - start_process  # 两 datetime 相减得 timedelta
    return {
        "item_id": item_id,
        "start_datetime": start_datetime,
        "end_datetime": end_datetime,
        "process_after": process_after,
        "repeat_at": repeat_at,
        "start_process": start_process,
        "duration": duration,
    }
''',
    "docs_src/extra_models/__init__.py": '''\
"""FastAPI 文档示例：多个 Pydantic 模型与 response_model 过滤输出字段。"""
''',
    "docs_src/extra_models/tutorial001_py310.py": '''\
"""教程 001：UserIn/UserOut/UserInDB 分离——response_model 过滤 password 与 hashed_password。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()


class UserIn(BaseModel):
    """请求体模型：含明文 password，客户端创建用户时提交。"""
    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


class UserOut(BaseModel):
    """响应模型：不含 password，仅返回可公开字段。"""
    username: str
    email: EmailStr
    full_name: str | None = None


class UserInDB(BaseModel):
    """内部存储模型：含 hashed_password，不直接暴露给客户端。"""
    username: str
    hashed_password: str
    email: EmailStr
    full_name: str | None = None


def fake_password_hasher(raw_password: str):
    """模拟密码哈希（示例用，非真实加密）。"""
    return "supersecret" + raw_password


def fake_save_user(user_in: UserIn):
    """将 UserIn 转为 UserInDB 并持久化（此处仅 print）。"""
    hashed_password = fake_password_hasher(user_in.password)
    user_in_db = UserInDB(**user_in.model_dump(), hashed_password=hashed_password)
    print("User saved! ..not really")
    return user_in_db


@app.post("/user/", response_model=UserOut)
async def create_user(user_in: UserIn):
    """返回 UserInDB 实例；response_model=UserOut 过滤掉 hashed_password。"""
    user_saved = fake_save_user(user_in)
    return user_saved
''',
    "docs_src/extra_models/tutorial002_py310.py": '''\
"""教程 002：UserBase 基类继承——UserIn/UserOut/UserInDB 共享公共字段定义。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()


class UserBase(BaseModel):
    """基类：username、email、full_name 三字段由子类复用。"""
    username: str
    email: EmailStr
    full_name: str | None = None


class UserIn(UserBase):
    """输入模型：在基类基础上增加 password。"""
    password: str


class UserOut(UserBase):
    """输出模型：继承基类字段，无需额外声明。"""
    pass


class UserInDB(UserBase):
    """数据库模型：在基类基础上增加 hashed_password。"""
    hashed_password: str


def fake_password_hasher(raw_password: str):
    """模拟密码哈希（示例用，非真实加密）。"""
    return "supersecret" + raw_password


def fake_save_user(user_in: UserIn):
    """将 UserIn 转为 UserInDB 并持久化（此处仅 print）。"""
    hashed_password = fake_password_hasher(user_in.password)
    user_in_db = UserInDB(**user_in.model_dump(), hashed_password=hashed_password)
    print("User saved! ..not really")
    return user_in_db


@app.post("/user/", response_model=UserOut)
async def create_user(user_in: UserIn):
    """继承写法与 tutorial001 等价；response_model 仍过滤敏感字段。"""
    user_saved = fake_save_user(user_in)
    return user_saved
''',
    "docs_src/extra_models/tutorial003_py310.py": '''\
"""教程 003：Union 响应模型 PlaneItem | CarItem——按返回数据动态选择 schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class BaseItem(BaseModel):
    """物品基类：description 与 type 字段。"""
    description: str
    type: str


class CarItem(BaseItem):
    """汽车物品：type 固定为 car。"""
    type: str = "car"


class PlaneItem(BaseItem):
    """飞机物品：type 固定为 plane，额外含 size。"""
    type: str = "plane"
    size: int


items = {
    "item1": {"description": "All my friends drive a low rider", "type": "car"},
    "item2": {
        "description": "Music is my aeroplane, it's my aeroplane",
        "type": "plane",
        "size": 5,
    },
}


@app.get("/items/{item_id}", response_model=PlaneItem | CarItem)
async def read_item(item_id: str):
    """返回 dict；FastAPI 按 type 字段匹配 PlaneItem 或 CarItem schema。"""
    return items[item_id]
''',
    "docs_src/extra_models/tutorial004_py310.py": '''\
"""教程 004：response_model=list[Item]——声明响应为 Item 对象数组。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """列表元素模型：name 与 description。"""
    name: str
    description: str


items = [
    {"name": "Foo", "description": "There comes my hero"},
    {"name": "Red", "description": "It's my aeroplane"},
]


@app.get("/items/", response_model=list[Item])
async def read_items():
    """返回 dict 列表；FastAPI 逐项校验并按 Item schema 序列化。"""
    return items
''',
    "docs_src/extra_models/tutorial005_py310.py": '''\
"""教程 005：response_model=dict[str, float]——声明响应为字符串键、浮点值的字典。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/keyword-weights/", response_model=dict[str, float])
async def read_keyword_weights():
    """返回关键词权重映射；FastAPI 校验键为 str、值为 float。"""
    return {"foo": 2.3, "bar": 3.4}
''',
    "docs_src/first_steps/__init__.py": '''\
"""FastAPI 文档示例：入门第一步——创建应用与 Hello World 路由。"""
''',
    "docs_src/first_steps/tutorial001_py310.py": '''\
"""教程 001：最小 FastAPI 应用——async 路由返回 JSON Hello World。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/")
async def root():
    """GET / 返回 {"message": "Hello World"}；async 路由在 I/O 等待时可让出事件循环。"""
    return {"message": "Hello World"}
''',
    "docs_src/first_steps/tutorial003_py310.py": '''\
"""教程 003：同步 def 路由——无 await 时 FastAPI 在线程池中运行，避免阻塞事件循环。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/")
def root():
    """普通 def 路由；FastAPI 自动包装为 async，内部在线程池执行。"""
    return {"message": "Hello World"}
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
            "wave11a",
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
