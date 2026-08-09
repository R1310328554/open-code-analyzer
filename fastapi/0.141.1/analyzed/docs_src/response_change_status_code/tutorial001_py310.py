"""教程 001：根据资源是否存在动态修改 Response.status_code（200 或 201）。"""

from fastapi import FastAPI, Response, status

app = FastAPI()  # 创建 FastAPI 应用实例

tasks = {"foo": "Listen to the Bar Fighters"}  # 模拟内存任务存储


@app.put("/get-or-create-task/{task_id}", status_code=200)
def get_or_create_task(task_id: str, response: Response):
    """路径装饰器默认 200；若任务不存在则写入后改为 201 Created。"""
    if task_id not in tasks:
        tasks[task_id] = "This didn't exist before"
        response.status_code = status.HTTP_201_CREATED
    return tasks[task_id]
