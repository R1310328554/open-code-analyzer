"""教程 001：路径装饰器 status_code=201——创建资源成功时返回 201 Created。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/items/", status_code=201)
async def create_item(name: str):
    """status_code=201 覆盖默认 200；OpenAPI 文档同步标注创建成功状态码。"""
    return {"name": name}
