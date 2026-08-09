#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-19b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w19b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave19b_batch10_20.py"
MARK_NOTE = "wave19b docs_src [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/request_form_models/tutorial001_py310.py": '''\
"""教程 001：Pydantic BaseModel + Form()——将表单字段绑定到模型并校验。"""

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """登录表单模型：username 与 password 须为字符串。"""

    username: str
    password: str


@app.post("/login/")
async def login(data: FormData = Form()):
    """application/x-www-form-urlencoded 或 multipart 表单解析为 FormData 并返回。"""
    return data
''',
    "docs_src/request_form_models/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：Annotated[FormData, Form()] + extra=forbid 拒绝未知表单字段。"""

from typing import Annotated

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """仅允许 username/password；额外字段触发 422。"""

    username: str
    password: str
    model_config = {"extra": "forbid"}


@app.post("/login/")
async def login(data: Annotated[FormData, Form()]):
    """Annotated 将 Form 元数据与模型类型绑定；校验行为与 tutorial002 一致。"""
    return data
''',
    "docs_src/request_form_models/tutorial002_py310.py": '''\
"""教程 002：FormData 模型设置 extra=forbid，禁止未声明的额外表单键。"""

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """model_config extra=forbid 时多传字段会返回 422 Unprocessable Entity。"""

    username: str
    password: str
    model_config = {"extra": "forbid"}


@app.post("/login/")
async def login(data: FormData = Form()):
    """整表绑定到 Pydantic 模型；仅 username 与 password 被接受。"""
    return data
''',
    "docs_src/request_forms/__init__.py": '''\
"""FastAPI 文档示例：请求体表单字段（Form）。"""
''',
    "docs_src/request_forms/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Annotated[str, Form()] 分别声明 username 与 password 表单字段。"""

from typing import Annotated

from fastapi import FastAPI, Form

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/login/")
async def login(username: Annotated[str, Form()], password: Annotated[str, Form()]):
    """各字段独立声明为 Form；返回 username（示例未回传 password）。"""
    return {"username": username}
''',
    "docs_src/request_forms/tutorial001_py310.py": '''\
"""教程 001：str = Form() 将 username/password 声明为必填表单字段。"""

from fastapi import FastAPI, Form

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/login/")
async def login(username: str = Form(), password: str = Form()):
    """multipart 或 urlencoded 表单提交；缺字段时 FastAPI 返回 422。"""
    return {"username": username}
''',
    "docs_src/request_forms_and_files/__init__.py": '''\
"""FastAPI 文档示例：同一请求中混合表单字段（Form）与文件上传（File）。"""
''',
    "docs_src/request_forms_and_files/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Annotated 同时声明 bytes/UploadFile 文件与 str Form 令牌。"""

from typing import Annotated

from fastapi import FastAPI, File, Form, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(
    file: Annotated[bytes, File()],
    fileb: Annotated[UploadFile, File()],
    token: Annotated[str, Form()],
):
    """multipart 请求可同时携带文件字节、UploadFile 与 token 表单字段。"""
    return {
        "file_size": len(file),
        "token": token,
        "fileb_content_type": fileb.content_type,
    }
''',
    "docs_src/request_forms_and_files/tutorial001_py310.py": '''\
"""教程 001：File() 与 Form() 混用——单 endpoint 接收文件与表单 token。"""

from fastapi import FastAPI, File, Form, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(
    file: bytes = File(), fileb: UploadFile = File(), token: str = Form()
):
    """file 读入完整字节；fileb 保留 UploadFile 元数据；token 来自表单字段。"""
    return {
        "file_size": len(file),
        "token": token,
        "fileb_content_type": fileb.content_type,
    }
''',
    "docs_src/response_change_status_code/__init__.py": '''\
"""FastAPI 文档示例：在响应中自定义 HTTP 状态码。"""
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
    index_file = Path("/tmp/git-index-fastapi-w19b")
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
    """Remove completed wave-19b files from batch.json after marking done."""
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
        "fastapi 0.141.1: Chinese-annotate wave 19b docs_src [10:20]",
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
        "queue: mark fastapi 0.141.1 wave19b docs_src [10:20] done",
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
