"""教程 004：`:path` 转换器捕获含斜杠的完整路径段（如 files/home/user/file.txt）。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/files/{file_path:path}")
async def read_file(file_path: str):
    """`{file_path:path}` 匹配 `/files/` 后的整段路径，含多级目录。"""
    return {"file_path": file_path}
