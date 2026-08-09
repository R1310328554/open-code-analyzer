"""教程 005：fallback=None 禁用回退页，仅服务 dist 中真实存在的文件。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

app.frontend("/", directory="dist", fallback=None)  # 无 fallback，缺失文件直接 404
