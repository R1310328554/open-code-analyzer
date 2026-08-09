"""教程：GZipMiddleware 对超过阈值的响应体进行 GZip 压缩。"""

"""教程：GZipMiddleware 对超过阈值的响应体进行 GZip 压缩。"""

from fastapi import FastAPI
from fastapi.middleware.gzip import GZipMiddleware

app = FastAPI()

# 响应体 >= 1000 字节时使用压缩级别 5 进行 GZip
# 响应体 >= 1000 字节时使用压缩级别 5 进行 GZip
app.add_middleware(GZipMiddleware, minimum_size=1000, compresslevel=5)


@app.get("/")
async def main():
    # 返回较大内容以触发 GZip 压缩
    # 返回较大内容以触发 GZip 压缩
    return "somebigcontent"
