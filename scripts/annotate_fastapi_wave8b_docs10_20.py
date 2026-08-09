#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-8b docs_src [10:20]."""
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
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w8b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

PREPEND: dict[str, str] = {
    "docs_src/debugging/__init__.py": (
        '"""FastAPI 文档示例：调试应用（Debugging）。"""\n'
    ),
    "docs_src/dependencies/__init__.py": (
        '"""FastAPI 文档示例：依赖注入（Dependencies）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/dataclasses_/tutorial001_py310.py": [
        (
            "from dataclasses import dataclass",
            '"""教程 001：标准库 @dataclass 作为请求体，FastAPI 自动校验与序列化。"""\n\nfrom dataclasses import dataclass',
        ),
        (
            "@dataclass\nclass Item:",
            '@dataclass\nclass Item:\n    """商品数据类；可选字段 description、tax 默认为 None。"""',
        ),
        (
            "async def create_item(item: Item):",
            'async def create_item(item: Item):\n    """接收 JSON 请求体并映射为 Item；原样返回以演示响应序列化。"""',
        ),
    ],
    "docs_src/dataclasses_/tutorial002_py310.py": [
        (
            "from dataclasses import dataclass, field",
            '"""教程 002：dataclass 含可变默认值（field default_factory）与 response_model。"""\n\nfrom dataclasses import dataclass, field',
        ),
        (
            "@dataclass\nclass Item:",
            '@dataclass\nclass Item:\n    """Item 含 tags 列表；default_factory 避免可变默认值陷阱。"""',
        ),
        (
            "@app.get(\"/items/next\", response_model=Item)",
            '@app.get("/items/next", response_model=Item)  # 声明响应按 Item 过滤/校验',
        ),
        (
            "async def read_next_item():",
            'async def read_next_item():\n    """返回 dict；FastAPI 按 response_model=Item 校验输出字段。"""',
        ),
    ],
    "docs_src/dataclasses_/tutorial003_py310.py": [
        (
            "from dataclasses import field  # (1)",
            '"""教程 003：pydantic.dataclasses 嵌套数据类与 response_model 组合。"""\n\nfrom dataclasses import field  # (1) 可变默认值仍用 stdlib field',
        ),
        (
            "from pydantic.dataclasses import dataclass  # (2)",
            "from pydantic.dataclasses import dataclass  # (2) Pydantic 增强 dataclass，支持校验与 OpenAPI",
        ),
        (
            "@dataclass\nclass Item:",
            '@dataclass\nclass Item:\n    """嵌套子项数据类。"""',
        ),
        (
            "@dataclass\nclass Author:",
            '@dataclass\nclass Author:\n    """作者模型，含 Item 列表。"""',
        ),
        (
            "    items: list[Item] = field(default_factory=list)  # (3)",
            "    items: list[Item] = field(default_factory=list)  # (3) 嵌套 dataclass 列表",
        ),
        (
            '@app.post("/authors/{author_id}/items/", response_model=Author)  # (4)',
            '@app.post("/authors/{author_id}/items/", response_model=Author)  # (4) 声明响应模型为 Author',
        ),
        (
            "async def create_author_items(author_id: str, items: list[Item]):  # (5)",
            "async def create_author_items(author_id: str, items: list[Item]):  # (5) 路径参数 + 请求体 Item 列表",
        ),
        (
            '    return {"name": author_id, "items": items}  # (6)',
            '    return {"name": author_id, "items": items}  # (6) dict 输出按 Author 校验',
        ),
        (
            '@app.get("/authors/", response_model=list[Author])  # (7)',
            '@app.get("/authors/", response_model=list[Author])  # (7) 响应为 Author 数组',
        ),
        (
            "def get_authors():  # (8)",
            "def get_authors():  # (8) 同步路径操作函数同样可用",
        ),
        (
            "    return [  # (9)",
            "    return [  # (9) 嵌套 dict 自动映射为 dataclass",
        ),
    ],
    "docs_src/debugging/tutorial001_py310.py": [
        (
            "import uvicorn",
            '"""教程 001：直接运行 uvicorn 启动开发服务器，便于 IDE 断点调试。"""\n\nimport uvicorn',
        ),
        (
            "def root():",
            'def root():\n    """简单端点；可在函数内设断点调试变量 a、b。"""',
        ),
        (
            "if __name__ == \"__main__\":",
            '# 以 python tutorial001_py310.py 启动，无需命令行 uvicorn\nif __name__ == "__main__":',
        ),
        (
            "    uvicorn.run(app, host=\"0.0.0.0\", port=8000)",
            "    uvicorn.run(app, host=\"0.0.0.0\", port=8000)  # 绑定 0.0.0.0:8000",
        ),
    ],
    "docs_src/dependencies/tutorial001_py310.py": [
        (
            "from fastapi import Depends, FastAPI",
            '"""教程 001：Depends 共享查询参数依赖，多路由复用 common_parameters。"""\n\nfrom fastapi import Depends, FastAPI',
        ),
        (
            "async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):",
            'async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):\n    """依赖函数：从查询字符串解析 q、skip、limit 并返回 dict。"""',
        ),
        (
            "async def read_items(commons: dict = Depends(common_parameters)):",
            'async def read_items(commons: dict = Depends(common_parameters)):\n    """Depends(common_parameters) 注入共享查询参数字典。"""',
        ),
        (
            "async def read_users(commons: dict = Depends(common_parameters)):",
            'async def read_users(commons: dict = Depends(common_parameters)):\n    """同一依赖可在多个路径操作中复用。"""',
        ),
    ],
    "docs_src/dependencies/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 001（Annotated）：用 Annotated[dict, Depends(...)] 声明依赖。"""\n\nfrom typing import Annotated',
        ),
        (
            "async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):",
            'async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):\n    """与 tutorial001 相同的共享查询参数依赖。"""',
        ),
        (
            "async def read_items(commons: Annotated[dict, Depends(common_parameters)]):",
            'async def read_items(commons: Annotated[dict, Depends(common_parameters)]):\n    """Annotated 将类型与 Depends 元数据合并，便于复用与 IDE 提示。"""',
        ),
        (
            "async def read_users(commons: Annotated[dict, Depends(common_parameters)]):",
            'async def read_users(commons: Annotated[dict, Depends(common_parameters)]):\n    """users 端点同样注入 commons。"""',
        ),
    ],
    "docs_src/dependencies/tutorial001_02_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 001-02（Annotated）：将 Annotated 依赖提取为类型别名 CommonsDep。"""\n\nfrom typing import Annotated',
        ),
        (
            "async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):",
            'async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):\n    """共享查询参数依赖函数。"""',
        ),
        (
            "CommonsDep = Annotated[dict, Depends(common_parameters)]",
            "# 类型别名：多处复用时避免重复写 Annotated[dict, Depends(...)]\nCommonsDep = Annotated[dict, Depends(common_parameters)]",
        ),
        (
            "async def read_items(commons: CommonsDep):",
            'async def read_items(commons: CommonsDep):\n    """使用 CommonsDep 简化参数声明。"""',
        ),
        (
            "async def read_users(commons: CommonsDep):",
            'async def read_users(commons: CommonsDep):\n    """别名在 items 与 users 路由间保持一致。"""',
        ),
    ],
    "docs_src/dependencies/tutorial002_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 002（Annotated）：可调用类作为依赖，封装查询参数逻辑。"""\n\nfrom typing import Annotated',
        ),
        (
            "fake_items_db = [{\"item_name\": \"Foo\"}, {\"item_name\": \"Bar\"}, {\"item_name\": \"Baz\"}]",
            "# 模拟数据库条目\nfake_items_db = [{\"item_name\": \"Foo\"}, {\"item_name\": \"Bar\"}, {\"item_name\": \"Baz\"}]",
        ),
        (
            "class CommonQueryParams:",
            'class CommonQueryParams:\n    """依赖类：__init__ 参数自动从查询字符串注入。"""',
        ),
        (
            "async def read_items(commons: Annotated[CommonQueryParams, Depends(CommonQueryParams)]):",
            'async def read_items(commons: Annotated[CommonQueryParams, Depends(CommonQueryParams)]):\n    """Depends(CommonQueryParams) 实例化类并注入 commons。"""',
        ),
        (
            "    items = fake_items_db[commons.skip : commons.skip + commons.limit]",
            "    items = fake_items_db[commons.skip : commons.skip + commons.limit]  # 按 skip/limit 切片",
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
