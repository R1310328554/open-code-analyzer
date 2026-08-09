"""教程 001：BaseSettings 控制 openapi_url；设为 None 可关闭 OpenAPI 端点。"""

from fastapi import FastAPI
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置；openapi_url 默认 "/openapi.json"，生产环境可设为 None。"""
    openapi_url: str = "/openapi.json"


settings = Settings()

app = FastAPI(openapi_url=settings.openapi_url)  # 从环境变量/配置读取


@app.get("/")
def root():
    """根路由；OpenAPI 是否可用由 settings 决定。"""
    return {"message": "Hello World"}
