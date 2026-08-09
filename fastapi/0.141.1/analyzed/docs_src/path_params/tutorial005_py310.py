"""教程 005：路径参数使用 Enum，仅接受枚举成员对应的字符串值。"""

from enum import Enum

from fastapi import FastAPI


class ModelName(str, Enum):
    """可选模型名称；路径中须为 alexnet、resnet 或 lenet 之一。"""
    alexnet = "alexnet"
    resnet = "resnet"
    lenet = "lenet"


app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/models/{model_name}")
async def get_model(model_name: ModelName):
    """非法枚举值会返回 422；合法值按成员分支返回不同 message。"""
    if model_name is ModelName.alexnet:
        return {"model_name": model_name, "message": "Deep Learning FTW!"}

    if model_name.value == "lenet":
        return {"model_name": model_name, "message": "LeCNN all the images"}

    return {"model_name": model_name, "message": "Have some residuals"}
