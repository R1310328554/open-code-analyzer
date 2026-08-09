"""教程 002：generate_unique_id_function 全局自定义 operation_id 生成规则。"""

from fastapi import FastAPI
from fastapi.routing import APIRoute


def custom_generate_unique_id(route: APIRoute) -> str:
    """以路由处理函数名 route.name 作为 operation_id（覆盖默认路径+方法拼接）。"""
    return route.name


app = FastAPI(generate_unique_id_function=custom_generate_unique_id)


@app.get("/items/")
async def read_items():
    """本例 operation_id 为 read_items，而非默认的 read_items_items__get。"""
    return [{"item_id": "Foo"}]
