"""教程 011：Pydantic BaseModel 自动校验并转换外部数据（类型强制与默认值）。"""

from datetime import datetime

from pydantic import BaseModel


class User(BaseModel):
    """用户模型：id 转 int，signup_ts 解析 datetime，friends 各元素转 int。"""
    id: int
    name: str = "John Doe"  # 缺省时使用默认名
    signup_ts: datetime | None = None
    friends: list[int] = []  # 模型内可变默认值可安全复用


external_data = {
    "id": "123",  # 字符串会被强制转为 int
    "signup_ts": "2017-06-01 12:22",  # 自动解析为 datetime
    "friends": [1, "2", b"3"],  # 各元素统一转为 int
}
user = User(**external_data)
print(user)
# > User id=123 name='John Doe' signup_ts=datetime.datetime(2017, 6, 1, 12, 22) friends=[1, 2, 3]
print(user.id)
# > 123
