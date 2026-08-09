#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-4b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][10:20]

PREPEND: dict[str, str] = {
    "docs_src/bigger_applications/app_an_py310/internal/__init__.py": (
        '"""大型应用示例：internal 子包（内部管理路由等）。"""\n'
    ),
    "docs_src/bigger_applications/app_an_py310/routers/__init__.py": (
        '"""大型应用示例：routers 子包（users、items 等 APIRouter）。"""\n'
    ),
    "docs_src/body/__init__.py": '"""FastAPI 文档示例：请求体（Request Body）。"""\n',
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/bigger_applications/app_an_py310/dependencies.py": [
        (
            "from typing import Annotated",
            '"""大型应用示例：共享依赖项（X-Token 请求头与 query token 校验）。"""\n\nfrom typing import Annotated',
        ),
        (
            "async def get_token_header(x_token: Annotated[str, Header()]):",
            'async def get_token_header(x_token: Annotated[str, Header()]):\n    """校验 X-Token 请求头，无效时抛出 400。"""',
        ),
        (
            "    if x_token != \"fake-super-secret-token\":",
            '    # 模拟密钥校验\n    if x_token != "fake-super-secret-token":',
        ),
        (
            "async def get_query_token(token: str):",
            'async def get_query_token(token: str):\n    """校验 query 参数 token，非 jessica 时抛出 400。"""',
        ),
    ],
    "docs_src/bigger_applications/app_an_py310/internal/admin.py": [
        (
            "from fastapi import APIRouter",
            '"""内部 admin 路由：挂载于 /admin，需 X-Token 依赖。"""\n\nfrom fastapi import APIRouter',
        ),
        (
            "router = APIRouter()",
            "# 子路由，由 main 以 prefix=/admin 挂载\nrouter = APIRouter()",
        ),
        (
            "@router.post(\"/\")\nasync def update_admin():",
            '@router.post("/")\nasync def update_admin():\n    """管理员占位更新端点。"""',
        ),
    ],
    "docs_src/bigger_applications/app_an_py310/main.py": [
        (
            "from fastapi import Depends, FastAPI",
            '"""大型应用示例入口：组合全局依赖、users/items 路由与 internal admin。"""\n\nfrom fastapi import Depends, FastAPI',
        ),
        (
            "app = FastAPI(dependencies=[Depends(get_query_token)])",
            "# 应用级依赖：所有路径操作均需有效 query token\napp = FastAPI(dependencies=[Depends(get_query_token)])",
        ),
        (
            "app.include_router(users.router)",
            "# 注册用户路由（无额外 prefix）\napp.include_router(users.router)",
        ),
        (
            "app.include_router(items.router)",
            "# items 路由自带 /items prefix 与 X-Token 依赖\napp.include_router(items.router)",
        ),
        (
            "app.include_router(\n    admin.router,\n    prefix=\"/admin\",\n    tags=[\"admin\"],\n    dependencies=[Depends(get_token_header)],\n    responses={418: {\"description\": \"I'm a teapot\"}},\n)",
            "app.include_router(\n    admin.router,\n    prefix=\"/admin\",\n    tags=[\"admin\"],\n    dependencies=[Depends(get_token_header)],  # admin 路由额外要求 X-Token\n    responses={418: {\"description\": \"I'm a teapot\"}},\n)",
        ),
        (
            "@app.get(\"/\")\nasync def root():",
            '@app.get("/")\nasync def root():\n    """根路径健康检查。"""',
        ),
    ],
    "docs_src/bigger_applications/app_an_py310/routers/items.py": [
        (
            "from fastapi import APIRouter, Depends, HTTPException",
            '"""items 路由：带 router 级 X-Token 依赖的 CRUD 示例。"""\n\nfrom fastapi import APIRouter, Depends, HTTPException',
        ),
        (
            "router = APIRouter(\n    prefix=\"/items\",\n    tags=[\"items\"],\n    dependencies=[Depends(get_token_header)],\n    responses={404: {\"description\": \"Not found\"}},\n)",
            "router = APIRouter(\n    prefix=\"/items\",\n    tags=[\"items\"],\n    dependencies=[Depends(get_token_header)],  # 本 router 下所有端点需 X-Token\n    responses={404: {\"description\": \"Not found\"}},\n)",
        ),
        (
            "fake_items_db = {\"plumbus\": {\"name\": \"Plumbus\"}, \"gun\": {\"name\": \"Portal Gun\"}}",
            "# 模拟内存 item 存储\nfake_items_db = {\"plumbus\": {\"name\": \"Plumbus\"}, \"gun\": {\"name\": \"Portal Gun\"}}",
        ),
        (
            "@router.get(\"/\")\nasync def read_items():",
            '@router.get("/")\nasync def read_items():\n    """列出全部 items。"""',
        ),
        (
            "@router.get(\"/{item_id}\")\nasync def read_item(item_id: str):",
            '@router.get("/{item_id}")\nasync def read_item(item_id: str):\n    """按 ID 读取单个 item，不存在时 404。"""',
        ),
        (
            "@router.put(\n    \"/{item_id}\",\n    tags=[\"custom\"],\n    responses={403: {\"description\": \"Operation forbidden\"}},\n)\nasync def update_item(item_id: str):",
            '@router.put(\n    "/{item_id}",\n    tags=["custom"],\n    responses={403: {"description": "Operation forbidden"}},\n)\nasync def update_item(item_id: str):\n    """更新 item；仅允许更新 plumbus，否则 403。"""',
        ),
        (
            "    if item_id != \"plumbus\":",
            "    # 演示路径操作级自定义响应与业务限制\n    if item_id != \"plumbus\":",
        ),
    ],
    "docs_src/bigger_applications/app_an_py310/routers/users.py": [
        (
            "from fastapi import APIRouter",
            '"""users 路由：演示多路径与 tags 组织。"""\n\nfrom fastapi import APIRouter',
        ),
        (
            "router = APIRouter()",
            "# 路径在装饰器中写全（含 /users 前缀）\nrouter = APIRouter()",
        ),
        (
            "@router.get(\"/users/\", tags=[\"users\"])\nasync def read_users():",
            '@router.get("/users/", tags=["users"])\nasync def read_users():\n    """返回用户列表。"""',
        ),
        (
            "@router.get(\"/users/me\", tags=[\"users\"])\nasync def read_user_me():",
            '@router.get("/users/me", tags=["users"])\nasync def read_user_me():\n    """返回当前用户（示例固定值）。"""',
        ),
        (
            "@router.get(\"/users/{username}\", tags=[\"users\"])\nasync def read_user(username: str):",
            '@router.get("/users/{username}", tags=["users"])\nasync def read_user(username: str):\n    """按 username 路径参数返回用户信息。"""',
        ),
    ],
    "docs_src/body/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：使用 Pydantic 模型声明 POST 请求体并原样返回。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "@app.post(\"/items/\")\nasync def create_item(item: Item):",
            '@app.post("/items/")\nasync def create_item(item: Item):\n    """接收 JSON body，FastAPI 自动解析为 Item 并返回。"""',
        ),
    ],
    "docs_src/body/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：读取 body 后计算含税价并返回扩展字段。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型（含可选 tax）。"""',
        ),
        (
            "@app.post(\"/items/\")\nasync def create_item(item: Item):",
            '@app.post("/items/")\nasync def create_item(item: Item):\n    """解析 body，若有 tax 则附加 price_with_tax 后返回 dict。"""',
        ),
        (
            "    item_dict = item.model_dump()",
            "    # 转为普通 dict 以便添加计算字段\n    item_dict = item.model_dump()",
        ),
        (
            "    if item.tax is not None:",
            "    # 仅在提供 tax 时计算含税价\n    if item.tax is not None:",
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


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if has_chinese(text):
        return
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
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
