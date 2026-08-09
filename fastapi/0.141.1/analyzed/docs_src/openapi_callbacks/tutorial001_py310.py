"""教程 001：OpenAPI callbacks——创建发票路径附带回调路由，文档中描述外部通知 POST。"""

from fastapi import APIRouter, FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Invoice(BaseModel):
    """发票请求体模型。"""
    id: str
    title: str | None = None
    customer: str
    total: float


class InvoiceEvent(BaseModel):
    """回调通知的事件载荷（例如支付结果）。"""
    description: str
    paid: bool


class InvoiceEventReceived(BaseModel):
    """回调接口的响应模型。"""
    ok: bool


invoices_callback_router = APIRouter()  # 单独路由器，仅用于声明 callback 路径


@invoices_callback_router.post(
    "{$callback_url}/invoices/{$request.body.id}", response_model=InvoiceEventReceived
)
def invoice_notification(body: InvoiceEvent):
    """回调路径模板：外部开发者需实现的 POST 端点（本函数体仅用于 OpenAPI 文档）。"""
    pass


@app.post("/invoices/", callbacks=invoices_callback_router.routes)
def create_invoice(invoice: Invoice, callback_url: HttpUrl | None = None):
    """
    创建发票。

    假设 API 用户（外部开发者）通过本接口创建发票后，系统将：

    * 把发票发送给客户；
    * 向客户收款；
    * 通过 callback 向 API 用户（外部开发者）发送通知。
        * 届时 API 会向 callback_url 所指的外部地址 POST 发票事件
          （例如「支付成功」）。
    """
    # 发送发票、收款、发送 callback 通知（示例注释）
    return {"msg": "Invoice received"}
