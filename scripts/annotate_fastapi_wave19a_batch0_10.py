#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-19a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w19a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave19a_batch0_10.py"
MARK_NOTE = "wave19a docs_src [0:10]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/request_files/tutorial001_py310.py": '''\
"""教程 001：File() 读取字节与 UploadFile 流式接收 multipart 上传文件。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes = File()):
    """File() 将整文件读入内存；返回字节长度供客户端确认上传大小。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile):
    """UploadFile 适合大文件；此处仅返回客户端提供的原始文件名。"""
    return {"filename": file.filename}
''',
    "docs_src/request_files/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Annotated[bytes, File()] 声明 multipart 文件字节参数。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes, File()]):
    """Annotated 将 File() 元数据绑定到类型；行为与 tutorial001 非 Annotated 版一致。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(file: UploadFile):
    """UploadFile 端点无需 Annotated；FastAPI 自动识别为文件上传参数。"""
    return {"filename": file.filename}
''',
    "docs_src/request_files/tutorial001_03_py310.py": '''\
"""教程 001-03：File(description=...) 为 OpenAPI/Swagger 文档补充文件字段说明。"""

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: bytes = File(description="A file read as bytes")):
    """description 仅影响 API 文档展示；运行时仍将文件完整读入 bytes。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(
    file: UploadFile = File(description="A file read as UploadFile"),
):
    """UploadFile 同样可通过 File(description=...) 标注文档说明文字。"""
    return {"filename": file.filename}
''',
    "docs_src/request_files/tutorial001_03_an_py310.py": '''\
"""教程 001-03（Annotated）：Annotated + File(description=...) 声明文件参数文档元数据。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_file(file: Annotated[bytes, File(description="A file read as bytes")]):
    """Annotated 集中声明类型与 OpenAPI 描述；校验与解析行为不变。"""
    return {"file_size": len(file)}


@app.post("/uploadfile/")
async def create_upload_file(
    file: Annotated[UploadFile, File(description="A file read as UploadFile")],
):
    """UploadFile 也可包在 Annotated 内并附加 File(description=...)。"""
    return {"filename": file.filename}
''',
    "docs_src/request_files/tutorial002_py310.py": '''\
"""教程 002：list[bytes] / list[UploadFile] 接收同一字段名的多文件上传，并提供 HTML 测试表单。"""

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(files: list[bytes] = File()):
    """HTML multiple 属性使浏览器提交多个文件；FastAPI 解析为 bytes 列表。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(files: list[UploadFile]):
    """UploadFile 列表适合逐个流式处理大文件；此处汇总各文件名。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """返回含两个 multipart 表单的页面，便于在浏览器中手动测试多文件上传。"""
    content = """
<body>
<form action="/files/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
<form action="/uploadfiles/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
</body>
    """
    return HTMLResponse(content=content)
''',
    "docs_src/request_files/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：Annotated[list[bytes], File()] 声明多文件字节上传参数。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(files: Annotated[list[bytes], File()]):
    """Annotated 写法；多值文件解析与 tutorial002 非 Annotated 版等价。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(files: list[UploadFile]):
    """UploadFile 列表端点保持常规类型注解即可。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """提供 HTML 表单页面，演示 bytes 与 UploadFile 两种多文件上传路径。"""
    content = """
<body>
<form action="/files/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
<form action="/uploadfiles/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
</body>
    """
    return HTMLResponse(content=content)
''',
    "docs_src/request_files/tutorial003_py310.py": '''\
"""教程 003：多文件上传并为 File 字段设置 description，同时保留 HTML 测试页。"""

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(
    files: list[bytes] = File(description="Multiple files as bytes"),
):
    """description 帮助 API 使用者理解该字段接收多个 bytes 文件。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(
    files: list[UploadFile] = File(description="Multiple files as UploadFile"),
):
    """UploadFile 列表同样可用 File(description=...) 标注 OpenAPI 说明。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """根路径返回双表单 HTML，分别 POST 到 /files/ 与 /uploadfiles/。"""
    content = """
<body>
<form action="/files/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
<form action="/uploadfiles/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
</body>
    """
    return HTMLResponse(content=content)
''',
    "docs_src/request_files/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：Annotated 形式为多文件 File 参数附加 description 元数据。"""

from typing import Annotated

from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/files/")
async def create_files(
    files: Annotated[list[bytes], File(description="Multiple files as bytes")],
):
    """Annotated 将类型、来源与文档描述绑定在同一声明中。"""
    return {"file_sizes": [len(file) for file in files]}


@app.post("/uploadfiles/")
async def create_upload_files(
    files: Annotated[
        list[UploadFile], File(description="Multiple files as UploadFile")
    ],
):
    """与 tutorial003 非 Annotated 版行为一致；适合复杂 File 元数据的集中写法。"""
    return {"filenames": [file.filename for file in files]}


@app.get("/")
async def main():
    """浏览器访问 / 可打开多文件上传测试表单。"""
    content = """
<body>
<form action="/files/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
<form action="/uploadfiles/" enctype="multipart/form-data" method="post">
<input name="files" type="file" multiple>
<input type="submit">
</form>
</body>
    """
    return HTMLResponse(content=content)
''',
    "docs_src/request_form_models/__init__.py": '''\
"""FastAPI 文档示例：将 application/x-www-form-urlencoded 表单字段映射为 Pydantic 模型。"""
''',
    "docs_src/request_form_models/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Annotated[FormData, Form()] 将表单字段解析为 Pydantic BaseModel。"""

from typing import Annotated

from fastapi import FastAPI, Form
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class FormData(BaseModel):
    """登录表单模型：username 与 password 对应 HTML 表单字段名。"""

    username: str
    password: str


@app.post("/login/")
async def login(data: Annotated[FormData, Form()]):
    """Form() 触发 multipart/form 或 urlencoded 解析；字段自动填充 FormData 实例。"""
    return data
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
    index_file = Path("/tmp/git-index-fastapi-w19a")
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
        "fastapi 0.141.1: Chinese-annotate wave 19a docs_src [0:10]",
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
        "queue: mark fastapi 0.141.1 wave19a docs_src [0:10] done",
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
