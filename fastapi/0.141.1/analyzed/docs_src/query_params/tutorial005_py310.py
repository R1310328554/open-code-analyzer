"""教程 005：必填查询参数——无默认值的 needy 必须出现在 ?needy= 中，否则 422。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_user_item(item_id: str, needy: str):
    """needy 无默认值，FastAPI 将其视为必填查询参数。"""
    item = {"item_id": item_id, "needy": needy}
    return item
