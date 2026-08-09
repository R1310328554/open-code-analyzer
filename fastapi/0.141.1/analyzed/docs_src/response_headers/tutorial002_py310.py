"""教程 002：注入 Response 参数并直接修改 response.headers。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/headers-and-object/")
def get_headers(response: Response):
    """与 tutorial001 等效思路：先改 headers，再返回将被序列化的 JSON 体。"""
    response.headers["X-Cat-Dog"] = "alone in the world"
    return {"message": "Hello World"}
