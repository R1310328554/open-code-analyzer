#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-16a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w16a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/python_types/__init__.py": '''\
"""FastAPI 文档示例：Python 类型提示入门（type hints / annotations）。"""
''',
    "docs_src/python_types/tutorial001_py310.py": '''\
"""教程 001：无类型提示的基础函数——拼接姓名字符串并打印。"""


def get_full_name(first_name, last_name):
    """将 first_name、last_name 首字母大写后用空格连接。"""
    full_name = first_name.title() + " " + last_name.title()  # title() 首字母大写
    return full_name


print(get_full_name("john", "doe"))  # 输出: John Doe
''',
    "docs_src/python_types/tutorial002_py310.py": '''\
"""教程 002：用 `参数: 类型` 添加类型提示——编辑器可据此提供补全与检查。"""


def get_full_name(first_name: str, last_name: str):
    """类型提示不改变运行时行为，仅帮助 IDE 与静态分析工具。"""
    full_name = first_name.title() + " " + last_name.title()
    return full_name


print(get_full_name("john", "doe"))
''',
    "docs_src/python_types/tutorial003_py310.py": '''\
"""教程 003：带类型提示时编辑器可发现 str 与 int 直接拼接的类型错误。"""


def get_name_with_age(name: str, age: int):
    """age 为 int；与 str 拼接会在静态检查中报错（运行时亦 TypeError）。"""
    name_with_age = name + " is this old: " + age  # 故意错误：应使用 str(age)
    return name_with_age
''',
    "docs_src/python_types/tutorial004_py310.py": '''\
"""教程 004：用 str(age) 将 int 转为 str 后再与字符串拼接。"""


def get_name_with_age(name: str, age: int):
    """修正 tutorial003 的类型/运行时错误。"""
    name_with_age = name + " is this old: " + str(age)
    return name_with_age
''',
    "docs_src/python_types/tutorial005_py310.py": '''\
"""教程 005：声明标准 Python 简单类型——str、int、float、bool、bytes。"""


def get_items(item_a: str, item_b: int, item_c: float, item_d: bool, item_e: bytes):
    """演示多种内置类型的参数注解；FastAPI 路径/查询/body 参数同理可用。"""
    return item_a, item_b, item_c, item_d, item_e
''',
    "docs_src/python_types/tutorial006_py310.py": '''\
"""教程 006：泛型 list[str]——列表元素类型写在方括号内（type parameter）。"""


def process_items(items: list[str]):
    """items 为字符串列表；遍历时编辑器知道 item 为 str。"""
    for item in items:
        print(item)
''',
    "docs_src/python_types/tutorial007_py310.py": '''\
"""教程 007：tuple 与 set 的泛型注解——tuple 按位置标注，set 标注元素类型。"""


def process_items(items_t: tuple[int, int, str], items_s: set[bytes]):
    """items_t 为 (int, int, str) 三元组；items_s 为 bytes 集合。"""
    return items_t, items_s
''',
    "docs_src/python_types/tutorial008_py310.py": '''\
"""教程 008：dict 泛型需两个类型参数——键类型与值类型，逗号分隔。"""


def process_items(prices: dict[str, float]):
    """prices 键为商品名 str，值为价格 float。"""
    for item_name, item_price in prices.items():
        print(item_name)
        print(item_price)
''',
    "docs_src/python_types/tutorial008b_py310.py": '''\
"""教程 008b：联合类型 int | str——变量可为竖线分隔的多种类型之一。"""


def process_item(item: int | str):
    """`|` 表示 union；item 接受 int 或 str。"""
    print(item)
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


def update_batch_json() -> None:
    """Mark w16a done; advance batch.json to next pending slice."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    pending_path = QUEUE / "pending.txt"
    pending = [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    batch["files"] = pending[:10]
    done_path = QUEUE / "done.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
            "wave16a",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
