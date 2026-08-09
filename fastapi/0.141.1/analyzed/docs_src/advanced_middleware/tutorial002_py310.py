"""教程：TrustedHostMiddleware 限制允许访问的主机名。"""

"""教程：TrustedHostMiddleware 限制允许访问的主机名。"""

from fastapi import FastAPI
from fastapi.middleware.trustedhost import TrustedHostMiddleware

app = FastAPI()

# 仅允许 example.com 及其子域名的 Host 头
# 仅允许 example.com 及其子域名的 Host 头
app.add_middleware(
    TrustedHostMiddleware, allowed_hosts=["example.com", "*.example.com"]
)


@app.get("/")
async def main():
    return {"message": "Hello World"}
