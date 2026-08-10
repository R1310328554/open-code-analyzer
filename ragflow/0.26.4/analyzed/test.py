"""FastAPI 回显服务：POST / 原样返回请求体，用于 Webhook 调试。"""

from fastapi import FastAPI, Request

# 最小 FastAPI 应用
app = FastAPI()


@app.post("/")
async def echo(request: Request):
    # 读取原始 body 并回显
    body = await request.body()
    return body


if __name__ == "__main__":
    # 开发模式：0.0.0.0:8000 启动 uvicorn
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
