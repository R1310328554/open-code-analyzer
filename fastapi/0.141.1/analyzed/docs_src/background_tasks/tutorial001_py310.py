"""教程：BackgroundTasks 在响应返回后继续执行写文件等耗时操作。"""

from fastapi import BackgroundTasks, FastAPI

app = FastAPI()


def write_notification(email: str, message=""):
    """后台任务：将通知内容写入 log.txt。"""
    with open("log.txt", mode="w") as email_file:
        content = f"notification for {email}: {message}"
        email_file.write(content)


@app.post("/send-notification/{email}")
async def send_notification(email: str, background_tasks: BackgroundTasks):
    """立即返回响应，通知写入在后台异步执行。"""
    # 注册后台任务，响应发送后 Starlette 会调度执行
    background_tasks.add_task(write_notification, email, message="some notification")
    return {"message": "Notification sent in the background"}
