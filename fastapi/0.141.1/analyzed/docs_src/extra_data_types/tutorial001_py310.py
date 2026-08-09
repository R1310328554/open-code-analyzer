"""教程 001：请求体中使用 UUID、datetime、timedelta、time 等额外类型（非 Annotated 写法）。"""

from datetime import datetime, time, timedelta
from uuid import UUID

from fastapi import Body, FastAPI

app = FastAPI()


@app.put("/items/{item_id}")
async def read_items(
    item_id: UUID,  # 路径参数自动转为 UUID 实例
    start_datetime: datetime = Body(),  # JSON 字符串 → datetime
    end_datetime: datetime = Body(),  # 结束时间
    process_after: timedelta = Body(),  # 延迟时长（如 ISO 8601 duration）
    repeat_at: time | None = Body(default=None),  # 可选的每日重复时刻
):
    start_process = start_datetime + process_after  # datetime + timedelta 运算
    duration = end_datetime - start_process  # 两 datetime 相减得 timedelta
    return {
        "item_id": item_id,
        "start_datetime": start_datetime,
        "end_datetime": end_datetime,
        "process_after": process_after,
        "repeat_at": repeat_at,
        "start_process": start_process,
        "duration": duration,
    }
