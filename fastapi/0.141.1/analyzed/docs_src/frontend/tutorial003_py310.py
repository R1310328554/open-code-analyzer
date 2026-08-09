"""教程 003：fallback 设为 404.html，未匹配静态文件时返回自定义 404 页。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

app.frontend("/", directory="dist", fallback="404.html")  # 无对应文件时回退到 404.html
