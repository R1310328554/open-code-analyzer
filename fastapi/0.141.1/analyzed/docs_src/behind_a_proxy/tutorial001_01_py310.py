"""教程 001-01：无代理时的基础应用（对照组）。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/")
def read_items():
    """返回示例物品列表。"""
    return ["plumbus", "portal gun"]
