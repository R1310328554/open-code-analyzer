"""教程 003：lifespan 上下文管理器（推荐替代 on_event 的现代写法）。"""

from contextlib import asynccontextmanager

from fastapi import FastAPI


def fake_answer_to_everything_ml_model(x: float):
    """模拟 ML 模型推理函数。"""
    return x * 42


# 全局模型注册表，lifespan 启动时填充、关闭时清空
ml_models = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    """yield 前加载资源，yield 后清理——替代 startup/shutdown 事件。"""
    # yield 之前：加载 ML 模型等资源
    ml_models["answer_to_everything"] = fake_answer_to_everything_ml_model  # 注册模型
    yield
    # yield 之后：释放模型占用的资源
    ml_models.clear()  # 关停时清空模型缓存


app = FastAPI(lifespan=lifespan)  # 将 lifespan 传给 FastAPI 构造函数


@app.get("/predict")
async def predict(x: float):
    """调用 lifespan 阶段加载的模型进行预测。"""
    result = ml_models["answer_to_everything"](x)  # 使用 startup 注册的模型
    return {"result": result}
