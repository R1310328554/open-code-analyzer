#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-13b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w13b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave13b metadata/middleware/openapi [10:20]"

PREPEND: dict[str, str] = {
    "docs_src/middleware/__init__.py": (
        '"""FastAPI 文档示例：HTTP 中间件（middleware）。"""\n'
    ),
    "docs_src/openapi_callbacks/__init__.py": (
        '"""FastAPI 文档示例：OpenAPI callbacks（在 schema 中声明外部回调接口）。"""\n'
    ),
    "docs_src/openapi_webhooks/__init__.py": (
        '"""FastAPI 文档示例：OpenAPI webhooks（在 schema 中声明服务端推送事件）。"""\n'
    ),
    "docs_src/path_operation_advanced_configuration/__init__.py": (
        '"""FastAPI 文档示例：路径操作高级配置（operation_id、deprecated 等）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/metadata/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：自定义 OpenAPI JSON schema 的 URL 路径（openapi_url）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            'app = FastAPI(openapi_url="/api/v1/openapi.json")',
            'app = FastAPI(openapi_url="/api/v1/openapi.json")  # 默认 /openapi.json，此处改为 /api/v1/openapi.json',
        ),
        (
            "@app.get(\"/items/\")\nasync def read_items():",
            '@app.get("/items/")\nasync def read_items():\n    """示例路由；OpenAPI schema 仍会为所有路径自动生成。"""',
        ),
    ],
    "docs_src/metadata/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：自定义 Swagger UI 路径（docs_url）并禁用 ReDoc（redoc_url=None）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            'app = FastAPI(docs_url="/documentation", redoc_url=None)',
            'app = FastAPI(docs_url="/documentation", redoc_url=None)  # Swagger UI 在 /documentation；不挂载 ReDoc',
        ),
        (
            "@app.get(\"/items/\")\nasync def read_items():",
            '@app.get("/items/")\nasync def read_items():\n    """示例 GET 路由。"""',
        ),
    ],
    "docs_src/metadata/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 004：用 openapi_tags 为 OpenAPI 分组（tag）提供名称、描述与 externalDocs。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            '"description": "Operations with users. The **login** logic is also here.",',
            '"description": "与用户相关的操作；**登录**逻辑也包含在此分组。",',
        ),
        (
            '"description": "Manage items. So _fancy_ they have their own docs.",',
            '"description": "管理物品。它们非常 _fancy_，因此有独立的外部文档。",',
        ),
        (
            '"description": "Items external docs",',
            '"description": "Items 外部文档",',
        ),
        (
            "app = FastAPI(openapi_tags=tags_metadata)",
            "app = FastAPI(openapi_tags=tags_metadata)  # 将 tags_metadata 注入 OpenAPI schema",
        ),
        (
            "@app.get(\"/users/\", tags=[\"users\"])\nasync def get_users():",
            '@app.get("/users/", tags=["users"])\nasync def get_users():\n    """列出用户；在文档中归入 users 分组。"""',
        ),
        (
            "@app.get(\"/items/\", tags=[\"items\"])\nasync def get_items():",
            '@app.get("/items/", tags=["items"])\nasync def get_items():\n    """列出物品；在文档中归入 items 分组。"""',
        ),
    ],
    "docs_src/middleware/tutorial001_py310.py": [
        (
            "import time",
            '"""教程 001：HTTP 中间件——在响应头中追加请求处理耗时（X-Process-Time）。"""\n\nimport time',
        ),
        (
            "app = FastAPI()",
            "app = FastAPI()  # 创建 FastAPI 应用实例",
        ),
        (
            "@app.middleware(\"http\")\nasync def add_process_time_header(request: Request, call_next):",
            '@app.middleware("http")\nasync def add_process_time_header(request: Request, call_next):\n    """在每笔 HTTP 请求前后计时，并将耗时写入响应头。"""',
        ),
        (
            "    start_time = time.perf_counter()",
            "    start_time = time.perf_counter()  # 请求进入中间件时开始计时",
        ),
        (
            "    response = await call_next(request)",
            "    response = await call_next(request)  # 调用下游路由/中间件",
        ),
        (
            "    process_time = time.perf_counter() - start_time",
            "    process_time = time.perf_counter() - start_time  # 计算总耗时（秒）",
        ),
        (
            '    response.headers["X-Process-Time"] = str(process_time)',
            '    response.headers["X-Process-Time"] = str(process_time)  # 自定义响应头',
        ),
    ],
    "docs_src/openapi_callbacks/tutorial001_py310.py": [
        (
            "from fastapi import APIRouter, FastAPI",
            '"""教程 001：OpenAPI callbacks——创建发票路径附带回调路由，文档中描述外部通知 POST。"""\n\nfrom fastapi import APIRouter, FastAPI',
        ),
        (
            "class Invoice(BaseModel):",
            'class Invoice(BaseModel):\n    """发票请求体模型。"""',
        ),
        (
            "class InvoiceEvent(BaseModel):",
            'class InvoiceEvent(BaseModel):\n    """回调通知的事件载荷（例如支付结果）。"""',
        ),
        (
            "class InvoiceEventReceived(BaseModel):",
            'class InvoiceEventReceived(BaseModel):\n    """回调接口的响应模型。"""',
        ),
        (
            "invoices_callback_router = APIRouter()",
            "invoices_callback_router = APIRouter()  # 单独路由器，仅用于声明 callback 路径",
        ),
        (
            "def invoice_notification(body: InvoiceEvent):",
            'def invoice_notification(body: InvoiceEvent):\n    """回调路径模板：外部开发者需实现的 POST 端点（本函数体仅用于 OpenAPI 文档）。"""',
        ),
        (
            '    """\n    Create an invoice.\n\n    This will (let\'s imagine) let the API user (some external developer) create an\n    invoice.\n\n    And this path operation will:\n\n    * Send the invoice to the client.\n    * Collect the money from the client.\n    * Send a notification back to the API user (the external developer), as a callback.\n        * At this point is that the API will somehow send a POST request to the\n            external API with the notification of the invoice event\n            (e.g. "payment successful").\n    """',
            '    """\n    创建发票。\n\n    假设 API 用户（外部开发者）通过本接口创建发票后，系统将：\n\n    * 把发票发送给客户；\n    * 向客户收款；\n    * 通过 callback 向 API 用户（外部开发者）发送通知。\n        * 届时 API 会向 callback_url 所指的外部地址 POST 发票事件\n          （例如「支付成功」）。\n    """',
        ),
        (
            "    # Send the invoice, collect the money, send the notification (the callback)",
            "    # 发送发票、收款、发送 callback 通知（示例注释）",
        ),
    ],
    "docs_src/openapi_webhooks/tutorial001_py310.py": [
        (
            "from datetime import datetime",
            '"""教程 001：OpenAPI webhooks——在文档中声明 new-subscription 事件的 POST 载荷。"""\n\nfrom datetime import datetime',
        ),
        (
            "class Subscription(BaseModel):",
            'class Subscription(BaseModel):\n    """新订阅事件的请求体结构。"""',
        ),
        (
            '    """\n    When a new user subscribes to your service we\'ll send you a POST request with this\n    data to the URL that you register for the event `new-subscription` in the dashboard.\n    """',
            '    """\n    当有新用户订阅你的服务时，我们会向你在控制台为 `new-subscription`\n    事件注册的 URL 发送包含此数据的 POST 请求。\n    """',
        ),
        (
            "@app.get(\"/users/\")\ndef read_users():",
            '@app.get("/users/")\ndef read_users():\n    """示例 GET 路由（与 webhook 声明无关）。"""',
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
