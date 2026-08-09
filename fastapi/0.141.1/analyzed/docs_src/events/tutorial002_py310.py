"""教程 002：@app.on_event("shutdown")  # 应用停止时触发 在应用关闭时执行清理。"""

from fastapi import FastAPI

app = FastAPI()


@app.on_event("shutdown")
def shutdown_event():
    """关闭钩子：追加写入日志，演示资源释放。"""
    with open("log.txt", mode="a") as log:
        log.write("Application shutdown")  # 记录关停事件


@app.get("/items/")
async def read_items():
    """示例路由；shutdown 与具体请求无关，在进程退出前运行。"""
    return [{"name": "Foo"}]
