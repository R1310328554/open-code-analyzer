"""教程 001.1：license_info 使用 identifier（SPDX 标识符）而非 url。"""

from fastapi import FastAPI

description = """
ChimichangApp API 帮你做很酷的事。🚀

## Items

你可以 **读取 items**。

## Users

你将能够：

* **创建 users**（_未实现_）。
* **读取 users**（_未实现_）。
"""

app = FastAPI(
    title="ChimichangApp",
    description=description,  # 出现在 /docs 顶部的 Markdown 描述
    summary="Deadpool 最爱的应用，无需多言。",
    version="0.0.1",
    terms_of_service="http://example.com/terms/",
    contact={
        "name": "Deadpoolio the Amazing",
        "url": "http://x-force.example.com/contact/",
        "email": "dp@x-force.example.com",
    },
    license_info={
        "name": "Apache 2.0",
        "identifier": "Apache-2.0",  # SPDX 许可证标识符
    },
)


@app.get("/items/")
async def read_items():
    """示例路由；元数据不影响业务逻辑，仅丰富 OpenAPI 文档。"""
    return [{"name": "Katana"}]
