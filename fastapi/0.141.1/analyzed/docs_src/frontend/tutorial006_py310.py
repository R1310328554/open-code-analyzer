"""教程 006：check_dir=False 跳过启动时对 directory 是否存在的校验。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

app.frontend("/", directory="dist", check_dir=False)  # 构建产物尚未生成时也可启动应用
