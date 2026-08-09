"""教程：HTTPSRedirectMiddleware 将所有 HTTP 请求重定向到 HTTPS。"""

"""教程：HTTPSRedirectMiddleware 将所有 HTTP 请求重定向到 HTTPS。"""

from fastapi import FastAPI
from fastapi.middleware.httpsredirect import HTTPSRedirectMiddleware

app = FastAPI()

# 非 HTTPS 请求将被 307 重定向到 HTTPS
# 非 HTTPS 请求将被 307 重定向到 HTTPS
app.add_middleware(HTTPSRedirectMiddleware)


@app.get("/")
async def main():
    return {"message": "Hello World"}
