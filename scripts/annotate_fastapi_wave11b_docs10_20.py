#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-11b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w11b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

PREPEND: dict[str, str] = {
    "docs_src/frontend/__init__.py": (
        '"""FastAPI 文档示例：静态前端托管（app.frontend / APIRouter.frontend 挂载 SPA 构建产物）。"""\n'
    ),
    "docs_src/generate_clients/__init__.py": (
        '"""FastAPI 文档示例：OpenAPI 客户端代码生成（为 openapi-generator 等工具提供示例 API）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/frontend/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：app.frontend 将 dist 目录挂载为根路径 / 的静态文件服务。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            'app.frontend("/", directory="dist")',
            'app.frontend("/", directory="dist")  # 将 dist 目录作为 / 下的静态资源根目录',
        ),
    ],
    "docs_src/frontend/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：fallback 指定 SPA 入口页，未匹配路径回退到 index.html。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            'app.frontend("/", directory="dist", fallback="index.html")',
            'app.frontend("/", directory="dist", fallback="index.html")  # 客户端路由未命中时返回 index.html',
        ),
    ],
    "docs_src/frontend/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：fallback 设为 404.html，未匹配静态文件时返回自定义 404 页。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            'app.frontend("/", directory="dist", fallback="404.html")',
            'app.frontend("/", directory="dist", fallback="404.html")  # 无对应文件时回退到 404.html',
        ),
    ],
    "docs_src/frontend/tutorial004_py310.py": [
        (
            "from fastapi import APIRouter, FastAPI",
            '"""教程 004：在 APIRouter 上挂载 frontend，再通过 prefix 挂到子路径 /app。"""\n\nfrom fastapi import APIRouter, FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 主应用",
        ),
        (
            "router = APIRouter()",
            "router = APIRouter()  # 独立路由组，便于模块化挂载前端",
        ),
        (
            'router.frontend("/", directory="dist", fallback="index.html")',
            'router.frontend("/", directory="dist", fallback="index.html")  # 路由组内挂载 SPA',
        ),
        (
            'app.include_router(router, prefix="/app")',
            'app.include_router(router, prefix="/app")  # 前端实际访问路径为 /app/...',
        ),
    ],
    "docs_src/frontend/tutorial005_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 005：fallback=None 禁用回退页，仅服务 dist 中真实存在的文件。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            'app.frontend("/", directory="dist", fallback=None)',
            'app.frontend("/", directory="dist", fallback=None)  # 无 fallback，缺失文件直接 404',
        ),
    ],
    "docs_src/frontend/tutorial006_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 006：check_dir=False 跳过启动时对 directory 是否存在的校验。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            'app.frontend("/", directory="dist", check_dir=False)',
            'app.frontend("/", directory="dist", check_dir=False)  # 构建产物尚未生成时也可启动应用',
        ),
    ],
    "docs_src/generate_clients/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：最小 CRUD 风格 API，供 openapi-generator 生成客户端 SDK。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "from pydantic import BaseModel",
            "from pydantic import BaseModel  # 请求/响应模型，驱动 OpenAPI schema",
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 自动生成 /openapi.json 供代码生成工具读取",
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """商品模型；字段类型会写入 OpenAPI components/schemas。"""',
        ),
        (
            "    name: str",
            "    name: str  # 商品名称",
        ),
        (
            "    price: float",
            "    price: float  # 单价",
        ),
        (
            "class ResponseMessage(BaseModel):",
            'class ResponseMessage(BaseModel):\n    """通用操作结果消息体。"""',
        ),
        (
            "    message: str",
            "    message: str  # 人类可读反馈",
        ),
        (
            "@app.post(\"/items/\", response_model=ResponseMessage)",
            '@app.post("/items/", response_model=ResponseMessage)  # POST 创建，响应 schema 固定为 ResponseMessage',
        ),
        (
            "async def create_item(item: Item):",
            'async def create_item(item: Item):\n    """接收 Item JSON，返回确认消息。"""',
        ),
        (
            '    return {"message": "item received"}',
            '    return {"message": "item received"}  # 实际项目可持久化 item',
        ),
        (
            "@app.get(\"/items/\", response_model=list[Item])",
            '@app.get("/items/", response_model=list[Item])  # GET 列表，响应为 Item 数组',
        ),
        (
            "async def get_items():",
            'async def get_items():\n    """返回示例商品列表。"""',
        ),
        (
            "    return [",
            "    return [  # 硬编码示例数据，生成器据此推断 list[Item] 结构",
        ),
    ],
    "docs_src/generate_clients/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：为路由添加 tags，生成客户端时可按 tag 分组 API 类/模块。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "from pydantic import BaseModel",
            "from pydantic import BaseModel  # 请求/响应模型",
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # OpenAPI 文档含 tags 元数据",
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """商品资源模型。"""',
        ),
        (
            "    name: str",
            "    name: str  # 商品名称",
        ),
        (
            "    price: float",
            "    price: float  # 单价",
        ),
        (
            "class ResponseMessage(BaseModel):",
            'class ResponseMessage(BaseModel):\n    """操作结果消息。"""',
        ),
        (
            "    message: str",
            "    message: str  # 反馈文本",
        ),
        (
            "class User(BaseModel):",
            'class User(BaseModel):\n    """用户资源模型。"""',
        ),
        (
            "    username: str",
            "    username: str  # 登录名",
        ),
        (
            "    email: str",
            "    email: str  # 邮箱地址",
        ),
        (
            '@app.post("/items/", response_model=ResponseMessage, tags=["items"])',
            '@app.post("/items/", response_model=ResponseMessage, tags=["items"])  # items 分组',
        ),
        (
            "async def create_item(item: Item):",
            'async def create_item(item: Item):\n    """创建商品。"""',
        ),
        (
            '    return {"message": "Item received"}',
            '    return {"message": "Item received"}  # 成功确认',
        ),
        (
            '@app.get("/items/", response_model=list[Item], tags=["items"])',
            '@app.get("/items/", response_model=list[Item], tags=["items"])  # 同 tag 下列表接口',
        ),
        (
            "async def get_items():",
            'async def get_items():\n    """列出所有商品。"""',
        ),
        (
            '@app.post("/users/", response_model=ResponseMessage, tags=["users"])',
            '@app.post("/users/", response_model=ResponseMessage, tags=["users"])  # users 分组，客户端可生成 UsersApi',
        ),
        (
            "async def create_user(user: User):",
            'async def create_user(user: User):\n    """创建用户。"""',
        ),
        (
            '    return {"message": "User received"}',
            '    return {"message": "User received"}  # 成功确认',
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
    if failures:
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
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
            "wave11b",
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
    print(f"Marked {len(BATCH_FILES)} files done in queue (note=wave11b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
