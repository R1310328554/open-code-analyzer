"""教程 003：TimedRoute 为指定 router 的路由添加 X-Response-Time 响应头。"""

import time
from collections.abc import Callable

from fastapi import APIRouter, FastAPI, Request, Response
from fastapi.routing import APIRoute


class TimedRoute(APIRoute):
    """测量 handler 耗时并写入响应头 X-Response-Time。"""
    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()

        async def custom_route_handler(request: Request) -> Response:
            before = time.time()  # 记录请求开始时间
            response: Response = await original_route_handler(request)
            duration = time.time() - before  # 计算处理耗时（秒）
            response.headers["X-Response-Time"] = str(duration)  # 暴露给客户端
            print(f"route duration: {duration}")
            print(f"route response: {response}")
            print(f"route response headers: {response.headers}")
            return response

        return custom_route_handler


app = FastAPI()
# 仅挂载在此 router 上的路由会计时
router = APIRouter(route_class=TimedRoute)


@app.get("/")
async def not_timed():
    """直接注册在 app 上，不使用 TimedRoute。"""
    return {"message": "Not timed"}


@router.get("/timed")
async def timed():
    """通过 TimedRoute 注册，响应含 X-Response-Time。"""
    return {"message": "It's the time of my life"}


app.include_router(router)  # 合并带计时的子路由
