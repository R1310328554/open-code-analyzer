#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-18a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w18a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave18a_batch0_10.py"
MARK_NOTE = "wave18a"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/query_params_str_validations/tutorial007_py310.py": '''\
"""教程 007：Query 的 title 与 min_length——为 OpenAPI 文档标注字段名并限制最短 3 字符。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(default=None, title="Query string", min_length=3),
):
    """title 出现在 Swagger UI；min_length=3 时过短 q 返回 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial007_an_py310.py": '''\
"""教程 007（Annotated）：Annotated + Query(title=..., min_length=3) 声明文档标题与长度下限。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[str | None, Query(title="Query string", min_length=3)] = None,
):
    """Annotated 将校验与 OpenAPI 元数据绑定到类型；行为与 tutorial007 等价。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial008_py310.py": '''\
"""教程 008：Query 增加 description——在 OpenAPI/Swagger 中展示参数说明文字。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(
        default=None,
        title="Query string",
        description="Query string for the items to search in the database that have a good match",
        min_length=3,
    ),
):
    """description 帮助 API 使用者理解 q 的用途；校验规则与 tutorial007 相同。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial008_an_py310.py": '''\
"""教程 008（Annotated）：Query 同时设置 title、description 与 min_length。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[
        str | None,
        Query(
            title="Query string",
            description="Query string for the items to search in the database that have a good match",
            min_length=3,
        ),
    ] = None,
):
    """Annotated 写法集中声明 OpenAPI 元数据与校验约束。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial009_py310.py": '''\
"""教程 009：Query(alias="item-query")——URL 查询键与 Python 参数名可不同。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, alias="item-query")):
    """客户端须用 ?item-query= 传值；函数内仍通过 q 访问。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial009_an_py310.py": '''\
"""教程 009（Annotated）：Annotated + Query(alias=...) 指定外部查询参数名。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(alias="item-query")] = None):
    """alias 仅影响请求解析与 OpenAPI 展示；Python 变量名保持 q。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial010_py310.py": '''\
"""教程 010：Query 组合 alias、title、description、长度/正则校验与 deprecated 标记。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(
        default=None,
        alias="item-query",
        title="Query string",
        description="Query string for the items to search in the database that have a good match",
        min_length=3,
        max_length=50,
        pattern="^fixedquery$",
        deprecated=True,
    ),
):
    """deprecated=True 在文档中标记该参数已弃用；其余约束同时生效。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial010_an_py310.py": '''\
"""教程 010（Annotated）：Annotated 形式汇总 alias、文档元数据、校验与 deprecated。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[
        str | None,
        Query(
            alias="item-query",
            title="Query string",
            description="Query string for the items to search in the database that have a good match",
            min_length=3,
            max_length=50,
            pattern="^fixedquery$",
            deprecated=True,
        ),
    ] = None,
):
    """与 tutorial010 非 Annotated 版等价；适合复杂 Query 元数据的集中声明。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial011_py310.py": '''\
"""教程 011：list[str] 查询参数——同一键可重复出现，FastAPI 解析为字符串列表。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list[str] | None = Query(default=None)):
    """例如 ?q=a&q=b 得到 ["a","b"]；省略 q 时返回 {"q": null}。"""
    query_items = {"q": q}
    return query_items
''',
    "docs_src/query_params_str_validations/tutorial011_an_py310.py": '''\
"""教程 011（Annotated）：Annotated[list[str] | None, Query()] 声明多值查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list[str] | None, Query()] = None):
    """Query() 无额外约束时仅声明参数来源；多值行为与 tutorial011 一致。"""
    query_items = {"q": q}
    return query_items
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
    index_file = Path("/tmp/git-index-fastapi-w18a")
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
    """Advance batch.json after w18a files are marked done."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done_set = {ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()}
    pending = [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    batch["files"] = pending[:10] if pending else []
    batch["done"] = len(done_set)
    batch["remaining_pending"] = len(pending)
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
        "fastapi 0.141.1: Chinese-annotate wave 18a docs_src [0:10]",
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
        "queue: mark fastapi 0.141.1 wave18a docs_src [0:10] done",
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
