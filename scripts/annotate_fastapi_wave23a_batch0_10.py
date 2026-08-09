#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-23a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w23a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave23a_batch0_10.py"
MARK_NOTE = "wave23a docs_src [0:10]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/settings/app01_py310/__init__.py": '''\
"""示例 01 包：模块级 Settings 单例——在 config 中实例化后由 main 直接导入使用。"""
''',
    "docs_src/settings/app01_py310/config.py": '''\
"""示例 01 配置：pydantic-settings BaseSettings 从环境变量/.env 加载，模块级单例 settings。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置模型；字段名对应环境变量（不区分大小写），如 ADMIN_EMAIL。"""

    app_name: str = "Awesome API"  # 有默认值，未设置环境变量时使用
    admin_email: str  # 必填，须通过环境变量或 .env 提供
    items_per_user: int = 50


settings = Settings()  # 导入时即解析环境变量，全应用共享同一实例
''',
    "docs_src/settings/app01_py310/main.py": '''\
"""示例 01 主应用：直接导入 config.settings 单例，/info 返回当前配置快照。"""

from fastapi import FastAPI

from .config import settings

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/info")
async def info():
    """读取模块级 settings；启动时已从环境变量加载，此处无 Depends 注入。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
''',
    "docs_src/settings/app02_an_py310/__init__.py": '''\
"""示例 02（Annotated）包：Depends + lru_cache 延迟创建 Settings，便于测试时 dependency_overrides。"""
''',
    "docs_src/settings/app02_an_py310/config.py": '''\
"""示例 02 配置类：仅定义 Settings，不在模块级实例化——由 get_settings 依赖按需创建。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """与 app01 相同字段；admin_email 必填，其余有默认值。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50
''',
    "docs_src/settings/app02_an_py310/main.py": '''\
"""示例 02（Annotated）：get_settings + Depends 注入 Settings，lru_cache 保证进程内单例。"""

from functools import lru_cache
from typing import Annotated

from fastapi import Depends, FastAPI

from .config import Settings

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """缓存 Settings 实例；测试可通过 app.dependency_overrides 替换此依赖。"""
    return Settings()


@app.get("/info")
async def info(settings: Annotated[Settings, Depends(get_settings)]):
    """Annotated[Settings, Depends(get_settings)] 声明配置依赖，便于类型检查。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
    }
''',
    "docs_src/settings/app02_an_py310/test_main.py": '''\
"""示例 02 测试：dependency_overrides 替换 get_settings，无需改生产代码或环境变量。"""

from fastapi.testclient import TestClient

from .config import Settings
from .main import app, get_settings

client = TestClient(app)


def get_settings_override():
    """测试用 Settings：固定 admin_email，绕过真实环境变量。"""
    return Settings(admin_email="testing_admin@example.com")


app.dependency_overrides[get_settings] = get_settings_override  # 覆盖依赖，仅影响本测试客户端


def test_app():
    """GET /info 应返回 override 后的 admin_email，其余字段保持默认。"""
    response = client.get("/info")
    data = response.json()
    assert data == {
        "app_name": "Awesome API",
        "admin_email": "testing_admin@example.com",
        "items_per_user": 50,
    }
''',
    "docs_src/settings/app02_py310/__init__.py": '''\
"""示例 02 包：经典 Depends(get_settings) 写法，与 app02_an 逻辑相同、语法更传统。"""
''',
    "docs_src/settings/app02_py310/config.py": '''\
"""示例 02 配置：Settings 类定义，供 get_settings 依赖函数实例化。"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """BaseSettings 自动读取环境变量；字段 admin_email 无默认值，部署时必须配置。"""

    app_name: str = "Awesome API"
    admin_email: str
    items_per_user: int = 50
''',
    "docs_src/settings/app02_py310/main.py": '''\
"""示例 02 主应用：Depends(get_settings) 经典写法，lru_cache 缓存配置实例。"""

from functools import lru_cache

from fastapi import Depends, FastAPI

from .config import Settings

app = FastAPI()  # 创建 FastAPI 应用实例


@lru_cache
def get_settings():
    """首次调用时解析环境变量并缓存；后续请求复用同一 Settings 对象。"""
    return Settings()


@app.get("/info")
async def info(settings: Settings = Depends(get_settings)):
    """settings: Settings = Depends(get_settings) 等价于 Annotated 写法。"""
    return {
        "app_name": settings.app_name,
        "admin_email": settings.admin_email,
        "items_per_user": settings.items_per_user,
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
    index_file = Path("/tmp/git-index-fastapi-w23a")
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
        "fastapi 0.141.1: Chinese-annotate wave 23a docs_src [0:10]",
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
    queue_paths = [
        "fastapi/0.141.1/_reports/class-queue/done.txt",
        "fastapi/0.141.1/_reports/class-queue/pending.txt",
        "fastapi/0.141.1/_reports/class-queue/batch.json",
        "fastapi/0.141.1/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark fastapi 0.141.1 wave23a docs_src [0:10] done",
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
