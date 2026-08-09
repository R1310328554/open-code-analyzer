"""教程 002：fallback 指定 SPA 入口页，未匹配路径回退到 index.html。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

app.frontend("/", directory="dist", fallback="index.html")  # 客户端路由未命中时返回 index.html
