"""教程 001：直接运行 uvicorn 启动开发服务器，便于 IDE 断点调试。"""

import uvicorn
from fastapi import FastAPI

app = FastAPI()


@app.get("/")
def root():
    """简单端点；可在函数内设断点调试变量 a、b。"""
    a = "a"
    b = "b" + a
    return {"hello world": b}


# 以 python tutorial001_py310.py 启动，无需命令行 uvicorn
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)  # 绑定 0.0.0.0:8000
