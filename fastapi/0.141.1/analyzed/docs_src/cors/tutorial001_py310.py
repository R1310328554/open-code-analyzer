"""教程 001：注册 CORSMiddleware，允许指定来源的跨域请求。"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

origins = [
    "http://localhost.tiangolo.com",
    "https://localhost.tiangolo.com",
    "http://localhost",
    "http://localhost:8080",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,  # 白名单来源；生产环境应收紧
    allow_credentials=True,  # 允许携带 Cookie 等凭证
    allow_methods=["*"],  # 允许所有 HTTP 方法
    allow_headers=["*"],  # 允许所有请求头
)


@app.get("/")
async def main():
    """示例路由；浏览器跨域访问时会收到 CORS 响应头。"""
    return {"message": "Hello World"}
