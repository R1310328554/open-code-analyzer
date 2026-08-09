from collections.abc import Callable
from typing import Annotated, Any

from annotated_doc import Doc
from starlette.background import BackgroundTasks as StarletteBackgroundTasks
from typing_extensions import ParamSpec

P = ParamSpec("P")


class BackgroundTasks(StarletteBackgroundTasks):
    """
    后台任务集合，响应发送给客户端后才会执行这些任务。

    详见 [FastAPI 后台任务文档](https://fastapi.tiangolo.com/tutorial/background-tasks/)。

    ## 示例

    ```python
    from fastapi import BackgroundTasks, FastAPI

    app = FastAPI()


    def write_notification(email: str, message=""):
        with open("log.txt", mode="w") as email_file:
            content = f"notification for {email}: {message}"
            email_file.write(content)


    @app.post("/send-notification/{email}")
    async def send_notification(email: str, background_tasks: BackgroundTasks):
        background_tasks.add_task(write_notification, email, message="some notification")
        return {"message": "Notification sent in the background"}
    ```
    """

    def add_task(
        self,
        func: Annotated[
            Callable[P, Any],
            Doc(
                """
                响应发送后要调用的函数。

                可以是普通 `def` 函数，也可以是 `async def` 函数。
                """
            ),
        ],
        *args: P.args,
        **kwargs: P.kwargs,
    ) -> None:
        """
        添加一个在响应发送后于后台执行的函数。

        详见 [FastAPI 后台任务文档](https://fastapi.tiangolo.com/tutorial/background-tasks/)。
        """
        return super().add_task(func, *args, **kwargs)
