"""教程 001：a2wsgi.WSGIMiddleware 将 Flask 应用挂载到 FastAPI 的 /v1 路径。"""

from a2wsgi import WSGIMiddleware
from fastapi import FastAPI
from flask import Flask, request
from markupsafe import escape

flask_app = Flask(__name__)  # 独立 Flask WSGI 应用


@flask_app.route("/")
def flask_main():
    """Flask 根路由：读取 name 查询参数并 HTML 转义后返回问候语。"""
    name = request.args.get("name", "World")
    return f"Hello, {escape(name)} from Flask!"


app = FastAPI()  # ASGI 主应用


@app.get("/v2")
def read_main():
    """FastAPI 原生路由；与挂载的 Flask /v1 并存于同一进程。"""
    return {"message": "Hello World"}


app.mount("/v1", WSGIMiddleware(flask_app))  # /v1/* 转发至 Flask WSGI 栈
