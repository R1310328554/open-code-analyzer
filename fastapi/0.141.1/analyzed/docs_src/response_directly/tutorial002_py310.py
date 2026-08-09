"""教程 002：直接返回 Response 并指定 media_type 输出非 JSON 内容（如 XML）。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/legacy/")
def get_legacy_data():
    """media_type=application/xml 告知客户端响应体格式；绕过默认 JSON 序列化。"""
    data = """<?xml version="1.0"?>
    <shampoo>
    <Header>
        Apply shampoo here.
    </Header>
    <Body>
        You'll have to use soap here.
    </Body>
    </shampoo>
    """
    return Response(content=data, media_type="application/xml")
