#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-14b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w14b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave14b path_operation_configuration/path_params [10:20]"

PREPEND: dict[str, str] = {
    "docs_src/path_params/__init__.py": (
        '"""FastAPI 文档示例：路径参数（path parameters）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/path_operation_configuration/tutorial002b_py310.py": [
        (
            "from enum import Enum",
            '"""教程 002b：用 Enum 成员作为 OpenAPI tags（而非字符串字面量）。"""\n\nfrom enum import Enum',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "class Tags(Enum):",
            'class Tags(Enum):\n    """OpenAPI 分组标签；Enum 值会序列化为字符串写入 schema。"""',
        ),
        (
            "@app.get(\"/items/\", tags=[Tags.items])\nasync def get_items():",
            '@app.get("/items/", tags=[Tags.items])\nasync def get_items():\n    """列出物品；tags 使用 Tags.items 枚举成员。"""',
        ),
        (
            "@app.get(\"/users/\", tags=[Tags.users])\nasync def read_users():",
            '@app.get("/users/", tags=[Tags.users])\nasync def read_users():\n    """列出用户；tags 使用 Tags.users 枚举成员。"""',
        ),
    ],
    "docs_src/path_operation_configuration/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：在路径操作装饰器上设置 summary 与 description（OpenAPI 文档摘要与说明）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """创建物品时的请求/响应模型。"""',
        ),
        (
            '    summary="Create an item",',
            '    summary="Create an item",  # OpenAPI 中显示的简短标题（此处保留英文原文）',
        ),
        (
            '    description="Create an item with all the information, name, description, price, tax and a set of unique tags",',
            '    description="Create an item with all the information, name, description, price, tax and a set of unique tags",  # 路径操作的长描述',
        ),
        (
            "async def create_item(item: Item) -> Item:",
            'async def create_item(item: Item) -> Item:\n    """接收 Item 请求体并原样返回（演示 summary/description 配置）。"""',
        ),
    ],
    "docs_src/path_operation_configuration/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 004：summary 写在装饰器上，详细说明写在函数 docstring（会出现在 OpenAPI 文档中）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """物品数据模型。"""',
        ),
        (
            '@app.post("/items/", summary="Create an item")',
            '@app.post("/items/", summary="Create an item")  # 简短摘要仍在装饰器参数中',
        ),
        (
            '    """\n    Create an item with all the information:\n\n    - **name**: each item must have a name\n    - **description**: a long description\n    - **price**: required\n    - **tax**: if the item doesn\'t have tax, you can omit this\n    - **tags**: a set of unique tag strings for this item\n    """',
            '    """\n    创建包含完整信息的物品：\n\n    - **name**：每个物品必须有名称\n    - **description**：较长描述\n    - **price**：必填\n    - **tax**：若无税可省略\n    - **tags**：该物品的一组唯一标签字符串\n    """',
        ),
    ],
    "docs_src/path_operation_configuration/tutorial005_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 005：用 response_description 描述成功响应的含义（OpenAPI 响应说明）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """物品数据模型。"""',
        ),
        (
            '    response_description="The created item",',
            '    response_description="The created item",  # 200 响应在文档中的说明文字',
        ),
        (
            '    """\n    Create an item with all the information:\n\n    - **name**: each item must have a name\n    - **description**: a long description\n    - **price**: required\n    - **tax**: if the item doesn\'t have tax, you can omit this\n    - **tags**: a set of unique tag strings for this item\n    """',
            '    """\n    创建包含完整信息的物品：\n\n    - **name**：每个物品必须有名称\n    - **description**：较长描述\n    - **price**：必填\n    - **tax**：若无税可省略\n    - **tags**：该物品的一组唯一标签字符串\n    """',
        ),
    ],
    "docs_src/path_operation_configuration/tutorial006_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 006：用 deprecated=True 将路径操作标记为已弃用（OpenAPI 中会显示删除线）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.get(\"/items/\", tags=[\"items\"])\nasync def read_items():",
            '@app.get("/items/", tags=["items"])\nasync def read_items():\n    """列出物品示例路由。"""',
        ),
        (
            "@app.get(\"/users/\", tags=[\"users\"])\nasync def read_users():",
            '@app.get("/users/", tags=["users"])\nasync def read_users():\n    """列出用户示例路由。"""',
        ),
        (
            "@app.get(\"/elements/\", tags=[\"items\"], deprecated=True)\nasync def read_elements():",
            '@app.get("/elements/", tags=["items"], deprecated=True)\nasync def read_elements():\n    """已弃用的 elements 路由；文档中会标注 deprecated。"""',
        ),
    ],
    "docs_src/path_params/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：声明路径参数 item_id（未标注类型时按字符串处理）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.get(\"/items/{item_id}\")\nasync def read_item(item_id):",
            '@app.get("/items/{item_id}")\nasync def read_item(item_id):\n    """从 URL 路径 `/items/{item_id}` 读取 item_id 并回显。"""',
        ),
    ],
    "docs_src/path_params/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：为路径参数标注 int 类型，FastAPI 自动校验与转换。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.get(\"/items/{item_id}\")\nasync def read_item(item_id: int):",
            '@app.get("/items/{item_id}")\nasync def read_item(item_id: int):\n    """item_id 必须为整数；非数字路径会返回 422 校验错误。"""',
        ),
    ],
    "docs_src/path_params/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：固定路径 `/users/me` 须声明在参数化路径 `/users/{user_id}` 之前，避免被误匹配。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.get(\"/users/me\")\nasync def read_user_me():",
            '@app.get("/users/me")\nasync def read_user_me():\n    """返回当前用户标识；必须在 `{user_id}` 路由之前注册。"""',
        ),
        (
            "@app.get(\"/users/{user_id}\")\nasync def read_user(user_id: str):",
            '@app.get("/users/{user_id}")\nasync def read_user(user_id: str):\n    """按 user_id 查询用户；若 `/users/me` 在后，`me` 会被当作 user_id。"""',
        ),
    ],
    "docs_src/path_params/tutorial003b_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003b：同一 HTTP 方法与路径重复注册——后定义会覆盖先定义（文档示例，勿在生产代码中这样写）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.get(\"/users\")\nasync def read_users():",
            '@app.get("/users")\nasync def read_users():\n    """第一个 GET /users 处理器（会被下方同名路由覆盖）。"""',
        ),
        (
            "@app.get(\"/users\")\nasync def read_users2():",
            '@app.get("/users")\nasync def read_users2():\n    """第二个 GET /users 处理器；实际生效的是本函数。"""',
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


def update_batch_queue() -> None:
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
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_queue()
    print(f"Marked {len(BATCH_FILES)} files done in queue (note={MARK_NOTE})")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
