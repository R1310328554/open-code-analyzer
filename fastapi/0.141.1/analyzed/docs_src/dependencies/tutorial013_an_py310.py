"""教程 013（Annotated）：路由 dependencies + 流式响应；get_user 校验 user_id 授权。"""

import time
from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from sqlmodel import Field, Session, SQLModel, create_engine

engine = create_engine("postgresql+psycopg://postgres:postgres@localhost/db")


class User(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str


app = FastAPI()


def get_session():
    """yield SQLModel Session，请求结束后自动退出 with 块。"""
    with Session(engine) as session:
        yield session


def get_user(user_id: int, session: Annotated[Session, Depends(get_session)]):
    """根据 user_id 查库；不存在则 403。session 由 get_session 注入。"""
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=403, detail="Not authorized")


def generate_stream(query: str):
    """逐字符 yield 并 sleep，模拟慢速流式输出。"""
    for ch in query:
        yield ch
        time.sleep(0.1)


@app.get("/generate", dependencies=[Depends(get_user)])
def generate(query: str):
    """Depends(get_user) 在 generate 前执行；user_id 仍来自查询参数。"""
    return StreamingResponse(content=generate_stream(query))
