"""教程 003：自定义异常与 @app.exception_handler——UnicornException 映射为 418 JSON。"""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse


class UnicornException(Exception):
    """业务自定义异常，携带触发异常的名称。"""

    def __init__(self, name: str):
        self.name = name


app = FastAPI()


@app.exception_handler(UnicornException)
async def unicorn_exception_handler(request: Request, exc: UnicornException):
    """将 UnicornException 转为 418 的 JSONResponse，而非默认 500。"""
    return JSONResponse(
        status_code=418,
        content={"message": f"Oops! {exc.name} did something. There goes a rainbow..."},
    )


@app.get("/unicorns/{name}")
async def read_unicorn(name: str):
    """name 为 yolo 时触发 UnicornException，由上方 handler 处理。"""
    if name == "yolo":
        raise UnicornException(name=name)
    return {"unicorn_name": name}
