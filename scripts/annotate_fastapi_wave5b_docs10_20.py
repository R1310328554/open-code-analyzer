#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-5b docs_src [10:20]."""
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
    "docs_src/body_nested_models/__init__.py": (
        '"""FastAPI 文档示例：嵌套模型与 body 中的 list/set/子模型。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/body_multiple_params/tutorial003_py310.py": [
        (
            "from fastapi import Body, FastAPI",
            '"""教程 003：路径参数、多个 body 模型与单值 body 字段（Body）并存。"""\n\nfrom fastapi import Body, FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "class User(BaseModel):",
            'class User(BaseModel):\n    """User 请求体模型（与 Item 同为 body 参数）。"""',
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item, user: User, importance: int = Body()):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item, user: User, importance: int = Body()):\n    """合并 item、user 两个模型与 importance 单值 body 字段后返回。"""',
        ),
        (
            "    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
            "    # 汇总路径参数与全部 body 字段\n    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
        ),
    ],
    "docs_src/body_multiple_params/tutorial004_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 004（Annotated）：关键字-only 参数、Body 校验与可选 query。"""\n\nfrom typing import Annotated',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "class User(BaseModel):",
            'class User(BaseModel):\n    """User 请求体模型。"""',
        ),
        (
            "    importance: Annotated[int, Body(gt=0)],",
            "    importance: Annotated[int, Body(gt=0)],  # body 单值，必须大于 0",
        ),
        (
            "    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
            "    # 组装响应；q 为可选 query 参数\n    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
        ),
        (
            "    if q:",
            "    if q:\n        # 仅在提供 query q 时附加",
        ),
    ],
    "docs_src/body_multiple_params/tutorial004_py310.py": [
        (
            "from fastapi import Body, FastAPI",
            '"""教程 004：关键字-only 参数与 Body(gt=0) 校验（非 Annotated 写法）。"""\n\nfrom fastapi import Body, FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "class User(BaseModel):",
            'class User(BaseModel):\n    """User 请求体模型。"""',
        ),
        (
            "    importance: int = Body(gt=0),",
            "    importance: int = Body(gt=0),  # body 单值，必须大于 0",
        ),
        (
            "    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
            "    # 组装响应；q 为可选 query 参数\n    results = {\"item_id\": item_id, \"item\": item, \"user\": user, \"importance\": importance}",
        ),
        (
            "    if q:",
            "    if q:\n        # 仅在提供 query q 时附加",
        ),
    ],
    "docs_src/body_multiple_params/tutorial005_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 005（Annotated）：Body(embed=True) 将单模型嵌套在 JSON 键下。"""\n\nfrom typing import Annotated',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Annotated[Item, Body(embed=True)]):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Annotated[Item, Body(embed=True)]):\n    """期望 body 形如 {\"item\": {...}}，而非顶层字段平铺。"""',
        ),
    ],
    "docs_src/body_multiple_params/tutorial005_py310.py": [
        (
            "from fastapi import Body, FastAPI",
            '"""教程 005：Body(embed=True) 将单模型嵌套在 JSON 键下（非 Annotated）。"""\n\nfrom fastapi import Body, FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 请求体模型。"""',
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item = Body(embed=True)):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item = Body(embed=True)):\n    """期望 body 形如 {\"item\": {...}}，而非顶层字段平铺。"""',
        ),
    ],
    "docs_src/body_nested_models/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：body 模型中的 list 字段（未标注元素类型）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 模型，tags 为 list 类型。"""',
        ),
        (
            "    tags: list = []",
            "    tags: list = []  # 默认空列表；生产环境建议用 Field(default_factory=list)",
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item):\n    """接收含 tags 列表的嵌套 body 并返回。"""',
        ),
    ],
    "docs_src/body_nested_models/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：body 模型中的 list[str] 带类型注解。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 模型，tags 为字符串列表。"""',
        ),
        (
            "    tags: list[str] = []",
            "    tags: list[str] = []  # 元素类型为 str，OpenAPI 会展示数组项 schema",
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item):\n    """接收含类型化 tags 列表的 body 并返回。"""',
        ),
    ],
    "docs_src/body_nested_models/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：body 模型中的 set[str] 去重标签集合。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 模型，tags 为字符串集合（自动去重）。"""',
        ),
        (
            "    tags: set[str] = set()",
            "    tags: set[str] = set()  # JSON 数组解析为 set，重复项会被丢弃",
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item):\n    """接收含 set 类型 tags 的 body 并返回。"""',
        ),
    ],
    "docs_src/body_nested_models/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 004：嵌套 Pydantic 子模型（Image）作为 Item 字段。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Image(BaseModel):",
            'class Image(BaseModel):\n    """嵌套子模型：图片 URL 与名称。"""',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 模型，含 tags 集合与可选嵌套 image。"""',
        ),
        (
            "    image: Image | None = None",
            "    image: Image | None = None  # 可选嵌套对象，body 中可含 image 子结构",
        ),
        (
            "@app.put(\"/items/{item_id}\")\nasync def update_item(item_id: int, item: Item):",
            '@app.put("/items/{item_id}")\nasync def update_item(item_id: int, item: Item):\n    """接收含嵌套 Image 与 tags 的复杂 body 并返回。"""',
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
