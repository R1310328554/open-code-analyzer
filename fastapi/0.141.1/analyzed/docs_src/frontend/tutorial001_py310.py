"""教程 001：app.frontend 将 dist 目录挂载为根路径 / 的静态文件服务。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

app.frontend("/", directory="dist")  # 将 dist 目录作为 / 下的静态资源根目录
