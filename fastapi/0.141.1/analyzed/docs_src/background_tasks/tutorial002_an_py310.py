"""教程 002（Annotated）：依赖项中注册 BackgroundTasks，响应返回后继续写日志。"""

from typing import Annotated

from fastapi import BackgroundTasks, Depends, FastAPI

app = FastAPI()


def write_log(message: str):
    """后台任务：将消息追加写入 log.txt。"""
    with open("log.txt", mode="a") as log:
        log.write(message)


def get_query(background_tasks: BackgroundTasks, q: str | None = None):
    """依赖项：若存在查询参数 q，将其记录到后台日志并返回 q。"""
    if q:
        message = f"found query: {q}\n"
        # 依赖项内也可向 BackgroundTasks 注册任务，与路径操作共享同一任务队列
        background_tasks.add_task(write_log, message)
    return q


@app.post("/send-notification/{email}")
async def send_notification(
    email: str, background_tasks: BackgroundTasks, q: Annotated[str, Depends(get_query)]
):
    """发送通知；邮件与查询参数均通过后台任务写入日志。"""
    message = f"message to {email}\n"
    background_tasks.add_task(write_log, message)
    return {"message": "Message sent"}
