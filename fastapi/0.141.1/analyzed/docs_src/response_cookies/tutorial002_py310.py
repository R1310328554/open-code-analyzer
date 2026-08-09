"""教程 002：注入 Response 参数并在返回 JSON 前设置 Cookie。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/cookie-and-object/")
def create_cookie(response: Response):
    """FastAPI 将 response 参数识别为 Starlette Response；可直接 set_cookie。"""
    response.set_cookie(key="fakesession", value="fake-cookie-session-value")
    return {"message": "Come to the dark side, we have cookies"}
