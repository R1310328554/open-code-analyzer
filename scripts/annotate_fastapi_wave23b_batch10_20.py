#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-23b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w23b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave23b_batch10_20.py"
MARK_NOTE = "wave23b docs_src [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/settings/app02_py310/test_main.py": '''\
"""app02 测试：用 dependency_overrides 替换 get_settings，隔离真实环境变量。"""

from fastapi.testclient import TestClient

from .config import Settings
from .main import app, get_settings

client = TestClient(app)  # 基于 app02 main 模块创建测试客户端


def get_settings_override():
    """测试替身：固定 admin_email，无需设置真实 ADMIN_EMAIL 环境变量。"""
    return Settings(admin_email="testing_admin@example.com")


app.dependency_overrides[get_settings] = get_settings_override  # 键为原依赖函数


def test_app():
    """GET /info 应返回 override 后的配置，而非 .env 或系统环境中的值。"""
    response = client.get("/info")
    data = response.json()
    assert data == {
        "app_name": "Awesome API",
        "admin_email": "testing_admin@example.com",
        "items_per_user": 50,
    }
''',
    "docs_src/settings/app03_an_py310/__init__.py": '''\
"""FastAPI 文档示例：app03（Annotated）——多文件 Settings + lru_cache 依赖注入。"""
''',
    "docs_src/settings/app03_an_py310/config.py": '''\
"""app03 配置模块：Settings 从 .env 文件与环境变量加载。"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """应用配置；admin_email 无默认值，必须在 .env 或环境中提供。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50

    model_config = SettingsConfigDict(env_file=".env")  # 自动读取项目根 .env
''',
    "docs_src/settings/app03_an_py310/main.py": '''\
"""app03 主模块（Annotated）：Depends + lru_cache 单例 Settings 依赖。"""

from functools import lru_cache
from typing import Annotated

from fastapi import Depends, FastAPI

from . import config

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；进程内只解析一次 .env/环境变量。"""
    return config.Settings()


@app.get("/info")
async def info(settings: Annotated[config.Settings, Depends(get_settings)]):
    """注入 Settings；Annotated 写法便于 IDE 类型提示与 OpenAPI 描述。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
''',
    "docs_src/settings/app03_py310/__init__.py": '''\
"""FastAPI 文档示例：app03——多文件 Settings + lru_cache 依赖注入（非 Annotated）。"""
''',
    "docs_src/settings/app03_py310/config.py": '''\
"""app03 配置模块：Settings 从 .env 文件与环境变量加载。"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """应用配置；admin_email 无默认值，必须在 .env 或环境中提供。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50

    model_config = SettingsConfigDict(env_file=".env")  # 自动读取项目根 .env
''',
    "docs_src/settings/app03_py310/main.py": '''\
"""app03 主模块：Depends(get_settings) 注入配置，与 app03_an 逻辑相同。"""

from functools import lru_cache

from fastapi import Depends, FastAPI

from . import config

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；进程内只解析一次 .env/环境变量。"""
    return config.Settings()


@app.get("/info")
async def info(settings: config.Settings = Depends(get_settings)):
    """经典 Depends 写法；settings 类型为 config.Settings。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
''',
    "docs_src/settings/tutorial001_py310.py": '''\
"""教程 001：最简 Settings——模块级单例，启动时一次性加载环境变量。"""

from fastapi import FastAPI
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置；admin_email 必填，app_name/items_per_user 有默认值。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50


settings = Settings()  # 导入时即解析环境变量
app = FastAPI()


@app.get("/info")
async def info():
    """直接读取模块级 settings；适合小型应用，测试时需 monkeypatch 环境变量。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
''',
    "docs_src/sql_databases/__init__.py": '''\
"""FastAPI 文档示例：SQL 数据库集成（SQLModel + SQLite CRUD）。"""
''',
    "docs_src/sql_databases/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：SQLModel + SQLite——Hero CRUD 与 SessionDep 依赖注入。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException, Query
from sqlmodel import Field, Session, SQLModel, create_engine, select


class Hero(SQLModel, table=True):
    """英雄表模型；secret_name 不对外暴露时可配合 response_model 过滤。"""

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(index=True)
    age: int | None = Field(default=None, index=True)
    secret_name: str


sqlite_file_name = "database.db"
sqlite_url = f"sqlite:///{sqlite_file_name}"

connect_args = {"check_same_thread": False}  # SQLite 多线程访问所需
engine = create_engine(sqlite_url, connect_args=connect_args)


def create_db_and_tables():
    """根据 SQLModel.metadata 创建所有已声明的表。"""
    SQLModel.metadata.create_all(engine)


def get_session():
    """请求级数据库会话；yield 后 FastAPI 自动关闭 session。"""
    with Session(engine) as session:
        yield session


SessionDep = Annotated[Session, Depends(get_session)]  # 复用会话依赖类型别名

app = FastAPI()


@app.on_event("startup")
def on_startup():
    """应用启动时建表；生产环境通常改用 Alembic 迁移。"""
    create_db_and_tables()


@app.post("/heroes/")
def create_hero(hero: Hero, session: SessionDep) -> Hero:
    """POST 创建英雄；commit 后 refresh 以获取数据库生成的 id。"""
    session.add(hero)
    session.commit()
    session.refresh(hero)
    return hero


@app.get("/heroes/")
def read_heroes(
    session: SessionDep,
    offset: int = 0,
    limit: Annotated[int, Query(le=100)] = 100,
) -> list[Hero]:
    """分页查询；limit 最大 100，防止一次拉取过多行。"""
    heroes = session.exec(select(Hero).offset(offset).limit(limit)).all()
    return heroes


@app.get("/heroes/{hero_id}")
def read_hero(hero_id: int, session: SessionDep) -> Hero:
    """按主键查询；不存在时返回 404。"""
    hero = session.get(Hero, hero_id)
    if not hero:
        raise HTTPException(status_code=404, detail="Hero not found")
    return hero


@app.delete("/heroes/{hero_id}")
def delete_hero(hero_id: int, session: SessionDep):
    """删除英雄；成功返回 {"ok": true}。"""
    hero = session.get(Hero, hero_id)
    if not hero:
        raise HTTPException(status_code=404, detail="Hero not found")
    session.delete(hero)
    session.commit()
    return {"ok": True}
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
    index_file = Path("/tmp/git-index-fastapi-w23b")
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
    """Remove completed wave-23b files from batch.json after marking done."""
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
        "fastapi 0.141.1: Chinese-annotate wave 23b docs_src [10:20]",
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
        "queue: mark fastapi 0.141.1 wave23b docs_src [10:20] done",
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
