"""教程 001：HTTP 中间件——在响应头中追加请求处理耗时（X-Process-Time）。"""

import time

from fastapi import FastAPI, Request

app = FastAPI()  # 创建 FastAPI 应用实例


@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    """在每笔 HTTP 请求前后计时，并将耗时写入响应头。"""
    start_time = time.perf_counter()  # 请求进入中间件时开始计时
    response = await call_next(request)  # 调用下游路由/中间件
    process_time = time.perf_counter() - start_time  # 计算总耗时（秒）
    response.headers["X-Process-Time"] = str(process_time)  # 自定义响应头
    return response
