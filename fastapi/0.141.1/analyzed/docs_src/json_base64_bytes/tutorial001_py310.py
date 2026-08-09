"""教程 001：Pydantic model_config 控制 JSON 请求/响应中 bytes 的 Base64 序列化与反序列化。"""

from fastapi import FastAPI
from pydantic import BaseModel


class DataInput(BaseModel):
    """请求体：data 字段从 JSON Base64 解码为 bytes。"""
    description: str
    data: bytes

    model_config = {"val_json_bytes": "base64"}  # 反序列化：Base64 -> bytes


class DataOutput(BaseModel):
    """响应体：data 字段序列化为 JSON Base64 字符串。"""
    description: str
    data: bytes

    model_config = {"ser_json_bytes": "base64"}  # 序列化：bytes -> Base64


class DataInputOutput(BaseModel):
    """请求与响应均使用 Base64 传输 bytes。"""
    description: str
    data: bytes

    model_config = {
        "val_json_bytes": "base64",
        "ser_json_bytes": "base64",
    }


app = FastAPI()


@app.post("/data")
def post_data(body: DataInput):
    """接收 Base64 编码的 data，解码后以 UTF-8 文本回显 content。"""
    content = body.data.decode("utf-8")
    return {"description": body.description, "content": content}


@app.get("/data")
def get_data() -> DataOutput:
    """返回 DataOutput，data 在 OpenAPI/JSON 中以 Base64 呈现。"""
    data = "hello".encode("utf-8")
    return DataOutput(description="A plumbus", data=data)


@app.post("/data-in-out")
def post_data_in_out(body: DataInputOutput) -> DataInputOutput:
    """原样回传请求体，bytes 字段全程 Base64 往返。"""
    return body
