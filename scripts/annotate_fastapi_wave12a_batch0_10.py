#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-12a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w12a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W12B_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w12b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/generate_clients/tutorial003_py310.py": '''\
"""教程 003：自定义 generate_unique_id_function——operationId 形如 {tag}-{route.name}。"""

from fastapi import FastAPI
from fastapi.routing import APIRoute
from pydantic import BaseModel


def custom_generate_unique_id(route: APIRoute):
    """按路由首个 tag 与函数名生成唯一 operationId，供 openapi-generator 等工具使用。"""
    return f"{route.tags[0]}-{route.name}"


app = FastAPI(generate_unique_id_function=custom_generate_unique_id)  # 全局自定义 operationId


class Item(BaseModel):
    """商品资源模型。"""
    name: str
    price: float


class ResponseMessage(BaseModel):
    """通用操作结果消息体。"""
    message: str


class User(BaseModel):
    """用户资源模型。"""
    username: str
    email: str


@app.post("/items/", response_model=ResponseMessage, tags=["items"])
async def create_item(item: Item):
    """创建商品；operationId 为 items-create_item。"""
    return {"message": "Item received"}


@app.get("/items/", response_model=list[Item], tags=["items"])
async def get_items():
    """列出商品；operationId 为 items-get_items。"""
    return [
        {"name": "Plumbus", "price": 3},
        {"name": "Portal Gun", "price": 9001},
    ]


@app.post("/users/", response_model=ResponseMessage, tags=["users"])
async def create_user(user: User):
    """创建用户；operationId 为 users-create_user。"""
    return {"message": "User received"}
''',
    "docs_src/generate_clients/tutorial004_py310.py": '''\
"""教程 004：后处理 openapi.json——从 operationId 中剥离 tag 前缀以匹配客户端命名习惯。"""

import json
from pathlib import Path

file_path = Path("./openapi.json")  # 由 FastAPI 导出的 OpenAPI 文档
openapi_content = json.loads(file_path.read_text())

for path_data in openapi_content["paths"].values():
    for operation in path_data.values():
        tag = operation["tags"][0]  # 与 tutorial003 中 custom_generate_unique_id 的 tag 一致
        operation_id = operation["operationId"]
        to_remove = f"{tag}-"
        new_operation_id = operation_id[len(to_remove) :]  # 去掉 "items-" / "users-" 前缀
        operation["operationId"] = new_operation_id

file_path.write_text(json.dumps(openapi_content))  # 写回供 openapi-generator 读取
''',
    "docs_src/graphql_/__init__.py": '''\
"""FastAPI 文档示例：GraphQL 集成（Strawberry + GraphQLRouter 挂载到 FastAPI）。"""
''',
    "docs_src/graphql_/tutorial001_py310.py": '''\
"""教程 001：Strawberry GraphQL——定义 Query 与 User 类型，挂载到 /graphql。"""

import strawberry
from fastapi import FastAPI
from strawberry.fastapi import GraphQLRouter


@strawberry.type
class User:
    """GraphQL User 对象类型。"""
    name: str
    age: int


@strawberry.type
class Query:
    """GraphQL 根查询类型。"""

    @strawberry.field
    def user(self) -> User:
        """返回示例用户。"""
        return User(name="Patrick", age=100)


schema = strawberry.Schema(query=Query)  # 构建 GraphQL schema


graphql_app = GraphQLRouter(schema)  # ASGI 子应用，处理 GraphQL 请求


app = FastAPI()
app.include_router(graphql_app, prefix="/graphql")  # POST/GET /graphql 访问 GraphiQL 与查询
''',
    "docs_src/handling_errors/__init__.py": '''\
"""FastAPI 文档示例：错误处理——HTTPException、自定义异常与全局 exception_handler。"""
''',
    "docs_src/handling_errors/tutorial001_py310.py": '''\
"""教程 001：HTTPException——资源不存在时返回 404 与 detail 消息。"""

from fastapi import FastAPI, HTTPException

app = FastAPI()

items = {"foo": "The Foo Wrestlers"}  # 模拟内存存储


@app.get("/items/{item_id}")
async def read_item(item_id: str):
    """按 ID 查询；不存在则抛出 HTTPException(404)。"""
    if item_id not in items:
        raise HTTPException(status_code=404, detail="Item not found")
    return {"item": items[item_id]}
''',
    "docs_src/handling_errors/tutorial002_py310.py": '''\
"""教程 002：HTTPException 自定义响应头——404 时在 headers 中附加 X-Error。"""

from fastapi import FastAPI, HTTPException

app = FastAPI()

items = {"foo": "The Foo Wrestlers"}


@app.get("/items-header/{item_id}")
async def read_item_header(item_id: str):
    """404 时除 detail 外还可设置自定义 HTTP 头供客户端或代理识别。"""
    if item_id not in items:
        raise HTTPException(
            status_code=404,
            detail="Item not found",
            headers={"X-Error": "There goes my error"},
        )
    return {"item": items[item_id]}
''',
    "docs_src/handling_errors/tutorial003_py310.py": '''\
"""教程 003：自定义异常与 @app.exception_handler——UnicornException 映射为 418 JSON。"""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse


class UnicornException(Exception):
    """业务自定义异常，携带触发异常的名称。"""

    def __init__(self, name: str):
        self.name = name


app = FastAPI()


@app.exception_handler(UnicornException)
async def unicorn_exception_handler(request: Request, exc: UnicornException):
    """将 UnicornException 转为 418 的 JSONResponse，而非默认 500。"""
    return JSONResponse(
        status_code=418,
        content={"message": f"Oops! {exc.name} did something. There goes a rainbow..."},
    )


@app.get("/unicorns/{name}")
async def read_unicorn(name: str):
    """name 为 yolo 时触发 UnicornException，由上方 handler 处理。"""
    if name == "yolo":
        raise UnicornException(name=name)
    return {"unicorn_name": name}
''',
    "docs_src/handling_errors/tutorial004_py310.py": '''\
"""教程 004：覆盖默认异常处理器——StarletteHTTPException 与 RequestValidationError 返回纯文本。"""

from fastapi import FastAPI, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import PlainTextResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

app = FastAPI()


@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request, exc):
    """HTTP 异常（含 HTTPException）统一返回 PlainTextResponse，内容为 detail 字符串。"""
    return PlainTextResponse(str(exc.detail), status_code=exc.status_code)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    """请求体验证失败时汇总各字段 loc/msg，返回 400 纯文本。"""
    message = "Validation errors:"
    for error in exc.errors():
        message += f"\\nField: {error['loc']}, Error: {error['msg']}"
    return PlainTextResponse(message, status_code=400)


@app.get("/items/{item_id}")
async def read_item(item_id: int):
    """item_id=3 时主动抛出 HTTPException(418)，由 http_exception_handler 处理。"""
    if item_id == 3:
        raise HTTPException(status_code=418, detail="Nope! I don't like 3.")
    return {"item_id": item_id}
''',
    "docs_src/handling_errors/tutorial005_py310.py": '''\
"""教程 005：RequestValidationError 返回 JSON——detail 与原始 body 一并序列化。"""

from fastapi import FastAPI, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

app = FastAPI()


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """422 JSON：包含 Pydantic 错误列表与无法解析的原始请求体。"""
    return JSONResponse(
        status_code=422,
        content=jsonable_encoder({"detail": exc.errors(), "body": exc.body}),
    )


class Item(BaseModel):
    """POST /items/ 的请求体模型。"""
    title: str
    size: int


@app.post("/items/")
async def create_item(item: Item):
    """校验通过则原样返回 Item；失败由 validation_exception_handler 处理。"""
    return item
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
    """Mark w12a done; keep w12b files in batch.json."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["files"] = W12B_FILES
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
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
            "wave12a generate_clients/graphql/handling_errors [0:10]",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
