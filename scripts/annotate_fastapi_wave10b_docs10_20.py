#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-10b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w10b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

PREPEND: dict[str, str] = {
    "docs_src/encoder/__init__.py": (
        '"""FastAPI 文档示例：JSON 编码器（jsonable_encoder 将模型转为可 JSON 序列化数据）。"""\n'
    ),
    "docs_src/events/__init__.py": (
        '"""FastAPI 文档示例：启动/关闭事件与 lifespan 生命周期管理。"""\n'
    ),
    "docs_src/extending_openapi/__init__.py": (
        '"""FastAPI 文档示例：自定义与扩展 OpenAPI schema。"""\n'
    ),
    "docs_src/extra_data_types/__init__.py": (
        '"""FastAPI 文档示例：额外数据类型（UUID、datetime、timedelta、time 等）。"""\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/encoder/tutorial001_py310.py": [
        (
            "from datetime import datetime",
            '"""教程 001：jsonable_encoder 将含 datetime 的 Pydantic 模型转为 JSON 兼容 dict。"""\n\nfrom datetime import datetime',
        ),
        (
            "fake_db = {}",
            "# 模拟持久化存储（内存 dict）\nfake_db = {}",
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """请求体模型；timestamp 为 datetime，默认 JSON 无法直接序列化。"""',
        ),
        (
            "    timestamp: datetime",
            "    timestamp: datetime  # datetime 需经 jsonable_encoder 转为 ISO 字符串",
        ),
        (
            "def update_item(id: str, item: Item):",
            'def update_item(id: str, item: Item):\n    """PUT 更新；jsonable_encoder 处理 datetime 等非 JSON 原生类型。"""',
        ),
        (
            "    json_compatible_item_data = jsonable_encoder(item)",
            "    json_compatible_item_data = jsonable_encoder(item)  # 转为 str/float/list/dict 等可 JSON 类型",
        ),
        (
            "    fake_db[id] = json_compatible_item_data",
            "    fake_db[id] = json_compatible_item_data  # 存入 fake_db 供后续读取",
        ),
    ],
    "docs_src/events/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：@app.on_event("startup") 在应用启动时预加载数据。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "items = {}",
            "# 应用级共享状态，startup 时填充\nitems = {}",
        ),
        (
            '@app.on_event("startup")',
            '@app.on_event("startup")  # 首个请求到达前执行',
        ),
        (
            "async def startup_event():",
            'async def startup_event():\n    """启动钩子：初始化 items 字典，供后续路由读取。"""',
        ),
        (
            '    items["foo"] = {"name": "Fighters"}',
            '    items["foo"] = {"name": "Fighters"}  # 预置示例数据',
        ),
        (
            "async def read_items(item_id: str):",
            'async def read_items(item_id: str):\n    """读取 startup 阶段写入的 item。"""',
        ),
    ],
    "docs_src/events/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：@app.on_event("shutdown") 在应用关闭时执行清理。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            '@app.on_event("shutdown")',
            '@app.on_event("shutdown")  # 应用停止时触发',
        ),
        (
            "def shutdown_event():",
            'def shutdown_event():\n    """关闭钩子：追加写入日志，演示资源释放。"""',
        ),
        (
            '        log.write("Application shutdown")',
            '        log.write("Application shutdown")  # 记录关停事件',
        ),
        (
            "async def read_items():",
            'async def read_items():\n    """示例路由；shutdown 与具体请求无关，在进程退出前运行。"""',
        ),
    ],
    "docs_src/events/tutorial003_py310.py": [
        (
            "from contextlib import asynccontextmanager",
            '"""教程 003：lifespan 上下文管理器（推荐替代 on_event 的现代写法）。"""\n\nfrom contextlib import asynccontextmanager',
        ),
        (
            "def fake_answer_to_everything_ml_model(x: float):",
            'def fake_answer_to_everything_ml_model(x: float):\n    """模拟 ML 模型推理函数。"""',
        ),
        (
            "ml_models = {}",
            "# 全局模型注册表，lifespan 启动时填充、关闭时清空\nml_models = {}",
        ),
        (
            "async def lifespan(app: FastAPI):",
            'async def lifespan(app: FastAPI):\n    """yield 前加载资源，yield 后清理——替代 startup/shutdown 事件。"""',
        ),
        (
            "    # Load the ML model",
            "    # yield 之前：加载 ML 模型等资源",
        ),
        (
            '    ml_models["answer_to_everything"] = fake_answer_to_everything_ml_model',
            '    ml_models["answer_to_everything"] = fake_answer_to_everything_ml_model  # 注册模型',
        ),
        (
            "    # Clean up the ML models and release the resources",
            "    # yield 之后：释放模型占用的资源",
        ),
        (
            "    ml_models.clear()",
            "    ml_models.clear()  # 关停时清空模型缓存",
        ),
        (
            "app = FastAPI(lifespan=lifespan)",
            "app = FastAPI(lifespan=lifespan)  # 将 lifespan 传给 FastAPI 构造函数",
        ),
        (
            "async def predict(x: float):",
            'async def predict(x: float):\n    """调用 lifespan 阶段加载的模型进行预测。"""',
        ),
        (
            '    result = ml_models["answer_to_everything"](x)',
            '    result = ml_models["answer_to_everything"](x)  # 使用 startup 注册的模型',
        ),
    ],
    "docs_src/extending_openapi/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：覆盖 app.openapi 以自定义 OpenAPI schema（标题、版本、扩展字段）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "def custom_openapi():",
            'def custom_openapi():\n    """生成并缓存自定义 OpenAPI schema；重复调用直接返回缓存。"""',
        ),
        (
            "    if app.openapi_schema:",
            "    if app.openapi_schema:  # 已生成则复用，避免重复计算",
        ),
        (
            "    openapi_schema = get_openapi(",
            "    openapi_schema = get_openapi(  # 基于当前 routes 构建 schema",
        ),
        (
            '        title="Custom title",',
            '        title="Custom title",  # 自定义 API 标题',
        ),
        (
            '        version="2.5.0",',
            '        version="2.5.0",  # 自定义 schema 版本号',
        ),
        (
            '        summary="This is a very custom OpenAPI schema",',
            '        summary="This is a very custom OpenAPI schema",  # 简短摘要',
        ),
        (
            '        description="Here\'s a longer description of the custom **OpenAPI** schema",',
            '        description="Here\'s a longer description of the custom **OpenAPI** schema",  # 详细描述（支持 Markdown）',
        ),
        (
            "        routes=app.routes,",
            "        routes=app.routes,  # 从已注册路由自动提取 paths",
        ),
        (
            '    openapi_schema["info"]["x-logo"] = {',
            '    openapi_schema["info"]["x-logo"] = {  # 扩展字段：文档页 logo',
        ),
        (
            "    app.openapi_schema = openapi_schema",
            "    app.openapi_schema = openapi_schema  # 缓存到 app，供 /openapi.json 使用",
        ),
        (
            "app.openapi = custom_openapi",
            "app.openapi = custom_openapi  # 替换默认 openapi 生成函数",
        ),
    ],
    "docs_src/extra_data_types/tutorial001_an_py310.py": [
        (
            "from datetime import datetime, time, timedelta",
            '"""教程 001（Annotated）：请求体中使用 UUID、datetime、timedelta、time 等额外类型。"""\n\nfrom datetime import datetime, time, timedelta',
        ),
        (
            "async def read_items(",
            "async def read_items(\n    # 路径参数 UUID + 多个 Body 字段，FastAPI/Pydantic 自动解析与校验",
        ),
        (
            "    item_id: UUID,",
            "    item_id: UUID,  # 路径参数自动转为 UUID 实例",
        ),
        (
            "    start_datetime: Annotated[datetime, Body()],",
            "    start_datetime: Annotated[datetime, Body()],  # JSON 字符串 → datetime",
        ),
        (
            "    end_datetime: Annotated[datetime, Body()],",
            "    end_datetime: Annotated[datetime, Body()],  # 结束时间",
        ),
        (
            "    process_after: Annotated[timedelta, Body()],",
            "    process_after: Annotated[timedelta, Body()],  # 延迟时长（如 ISO 8601 duration）",
        ),
        (
            "    repeat_at: Annotated[time | None, Body()] = None,",
            "    repeat_at: Annotated[time | None, Body()] = None,  # 可选的每日重复时刻",
        ),
        (
            "    start_process = start_datetime + process_after",
            "    start_process = start_datetime + process_after  # datetime + timedelta 运算",
        ),
        (
            "    duration = end_datetime - start_process",
            "    duration = end_datetime - start_process  # 两 datetime 相减得 timedelta",
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
            "wave10b",
            *BATCH_FILES,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Marked {len(BATCH_FILES)} files done in queue (note=wave10b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
