#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-15b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w15b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave15b numeric_validations/pydantic_v1 [10:20]"

PREPEND: dict[str, str] = {
    "docs_src/pydantic_v1_in_v2/__init__.py": (
        '"""FastAPI 文档示例：在 Pydantic v2 环境中通过 pydantic.v1 子模块渐进迁移。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/path_params_numeric_validations/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI, Path",
            '"""教程 004：用 `*` 强制后续参数以关键字传入；Path 的 ge=1 要求 item_id ≥ 1。"""\n\nfrom fastapi import FastAPI, Path',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "async def read_items(\n    *, item_id: int = Path(title=\"The ID of the item to get\", ge=1), q: str\n):",
            'async def read_items(\n    *,\n    item_id: int = Path(title="The ID of the item to get", ge=1),  # ge：greater than or equal，≥1\n    q: str,  # 必填查询参数；* 使其必须以关键字形式传入\n):\n    """读取 item_id 与查询 q；item_id 不满足 ge 时返回 422。"""',
        ),
    ],
    "docs_src/path_params_numeric_validations/tutorial005_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 005（Annotated）：Path 用 gt（大于 0）与 le（≤1000）约束 item_id。"""\n\nfrom typing import Annotated',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "    item_id: Annotated[int, Path(title=\"The ID of the item to get\", gt=0, le=1000)],",
            '    item_id: Annotated[int, Path(title="The ID of the item to get", gt=0, le=1000)],  # gt/le：须满足 0 < item_id ≤ 1000',
        ),
        (
            "    q: str,\n):",
            '    q: str,\n):\n    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""',
        ),
    ],
    "docs_src/path_params_numeric_validations/tutorial005_py310.py": [
        (
            "from fastapi import FastAPI, Path",
            '"""教程 005：Path 声明 gt 与 le 数值约束（非 Annotated 写法）。"""\n\nfrom fastapi import FastAPI, Path',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "    item_id: int = Path(title=\"The ID of the item to get\", gt=0, le=1000),",
            '    item_id: int = Path(title="The ID of the item to get", gt=0, le=1000),  # gt/le：须满足 0 < item_id ≤ 1000',
        ),
        (
            "    q: str,\n):",
            '    q: str,\n):\n    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""',
        ),
    ],
    "docs_src/path_params_numeric_validations/tutorial006_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 006（Annotated）：Path 约束 item_id；float 查询参数 size 用 Query 的 gt/lt 校验。"""\n\nfrom typing import Annotated',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "    item_id: Annotated[int, Path(title=\"The ID of the item to get\", ge=0, le=1000)],",
            '    item_id: Annotated[int, Path(title="The ID of the item to get", ge=0, le=1000)],  # ge/le：0 ≤ item_id ≤ 1000',
        ),
        (
            "    size: Annotated[float, Query(gt=0, lt=10.5)],",
            "    size: Annotated[float, Query(gt=0, lt=10.5)],  # 浮点 gt/lt：须大于 0 且小于 10.5（0.5 有效，0 无效）",
        ),
        (
            "    q: str,\n    size: Annotated[float, Query(gt=0, lt=10.5)],  # 浮点 gt/lt：须大于 0 且小于 10.5（0.5 有效，0 无效）\n):",
            '    q: str,\n    size: Annotated[float, Query(gt=0, lt=10.5)],  # 浮点 gt/lt：须大于 0 且小于 10.5（0.5 有效，0 无效）\n):\n    """组合 path、query 与 float 数值校验示例。"""',
        ),
    ],
    "docs_src/path_params_numeric_validations/tutorial006_py310.py": [
        (
            "from fastapi import FastAPI, Path, Query",
            '"""教程 006：Path 与 float 查询参数 size 的 gt/lt 数值校验（非 Annotated 写法）。"""\n\nfrom fastapi import FastAPI, Path, Query',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "    item_id: int = Path(title=\"The ID of the item to get\", ge=0, le=1000),",
            '    item_id: int = Path(title="The ID of the item to get", ge=0, le=1000),  # ge/le：0 ≤ item_id ≤ 1000',
        ),
        (
            "    size: float = Query(gt=0, lt=10.5),",
            "    size: float = Query(gt=0, lt=10.5),  # 浮点 gt/lt：须大于 0 且小于 10.5",
        ),
        (
            "    q: str,\n    size: float = Query(gt=0, lt=10.5),  # 浮点 gt/lt：须大于 0 且小于 10.5\n):",
            '    q: str,\n    size: float = Query(gt=0, lt=10.5),  # 浮点 gt/lt：须大于 0 且小于 10.5\n):\n    """组合 path、query 与 float 数值校验示例。"""',
        ),
    ],
    "docs_src/pydantic_v1_in_v2/tutorial001_an_py310.py": [
        (
            "from pydantic.v1 import BaseModel",
            '"""教程 001：在 Pydantic v2 安装环境下从 pydantic.v1 导入 BaseModel（迁移过渡期写法）。"""\n\nfrom pydantic.v1 import BaseModel',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """使用 pydantic.v1 子模块定义的物品模型。"""',
        ),
    ],
    "docs_src/pydantic_v1_in_v2/tutorial002_an_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：FastAPI 路径操作可直接使用 pydantic.v1 的 BaseModel 作为请求/响应模型。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """pydantic.v1 物品模型。"""',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "async def create_item(item: Item) -> Item:",
            'async def create_item(item: Item) -> Item:\n    """接收 v1 Item 请求体并原样返回。"""',
        ),
    ],
    "docs_src/pydantic_v1_in_v2/tutorial003_an_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：同一应用中混用 pydantic.v1 输入模型与 Pydantic v2 response_model。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """请求体：pydantic.v1 模型。"""',
        ),
        (
            "class ItemV2(BaseModelV2):",
            'class ItemV2(BaseModelV2):\n    """响应：Pydantic v2 模型（由 response_model 序列化输出）。"""',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "async def create_item(item: Item):",
            'async def create_item(item: Item):\n    """接受 v1 Item，FastAPI 按 ItemV2 校验并序列化响应。"""',
        ),
    ],
    "docs_src/pydantic_v1_in_v2/tutorial004_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 004：迁移期间从 fastapi.temp_pydantic_v1_params 导入 Body 等 v1 参数工具。"""\n\nfrom typing import Annotated',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """pydantic.v1 物品模型。"""',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "async def create_item(item: Annotated[Item, Body(embed=True)]) -> Item:",
            'async def create_item(item: Annotated[Item, Body(embed=True)]) -> Item:\n    """embed=True 时 JSON body 须为 {"item": {...}} 结构。"""',
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
    if not src.exists() and rel.endswith("__init__.py"):
        dst.parent.mkdir(parents=True, exist_ok=True)
        if not dst.exists():
            dst.write_text("", encoding="utf-8")
    elif not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    else:
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
