"""教程 004：在 APIRouter 上挂载 frontend，再通过 prefix 挂到子路径 /app。"""

from fastapi import APIRouter, FastAPI

app = FastAPI()  # 主应用
router = APIRouter()  # 独立路由组，便于模块化挂载前端

router.frontend("/", directory="dist", fallback="index.html")  # 路由组内挂载 SPA
app.include_router(router, prefix="/app")  # 前端实际访问路径为 /app/...
