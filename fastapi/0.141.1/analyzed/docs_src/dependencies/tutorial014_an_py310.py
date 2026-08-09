"""教程 014（Annotated）：演示流式响应依赖中过早 session.close() 的问题。"""

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
    """校验用户后立即 close session——流式响应期间会话可能已不可用。"""
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=403, detail="Not authorized")
    session.close()  # 过早关闭：StreamingResponse 返回后 generator 可能仍需要 session


def generate_stream(query: str):
    """逐字符 yield 并 sleep，模拟慢速流式输出。"""
    for ch in query:
        yield ch
        time.sleep(0.1)


@app.get("/generate", dependencies=[Depends(get_user)])
def generate(query: str):
    """与 tutorial013 对比：此处 get_user 提前关闭 session 会导致资源管理问题。"""
    return StreamingResponse(content=generate_stream(query))
