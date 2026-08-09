"""教程 001：OpenAPI webhooks——在文档中声明 new-subscription 事件的 POST 载荷。"""

from datetime import datetime

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Subscription(BaseModel):
    """新订阅事件的请求体结构。"""
    username: str
    monthly_fee: float
    start_date: datetime


@app.webhooks.post("new-subscription")
def new_subscription(body: Subscription):
    """
    当有新用户订阅你的服务时，我们会向你在控制台为 `new-subscription`
    事件注册的 URL 发送包含此数据的 POST 请求。
    """


@app.get("/users/")
def read_users():
    """示例 GET 路由（与 webhook 声明无关）。"""
    return ["Rick", "Morty"]
