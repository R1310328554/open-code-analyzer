"""教程 001（Annotated）：TestClient 测试时用 app.dependency_overrides 替换依赖。"""

from typing import Annotated

from fastapi import Depends, FastAPI
from fastapi.testclient import TestClient

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """共享依赖：收集 q/skip/limit 查询参数并返回 dict。"""
    return {"q": q, "skip": skip, "limit": limit}


@app.get("/items/")
async def read_items(commons: Annotated[dict, Depends(common_parameters)]):
    """注入 common_parameters 的返回值。"""
    return {"message": "Hello Items!", "params": commons}


@app.get("/users/")
async def read_users(commons: Annotated[dict, Depends(common_parameters)]):
    """同一依赖可复用于多个路由。"""
    return {"message": "Hello Users!", "params": commons}


client = TestClient(app)


async def override_dependency(q: str | None = None):
    """测试替身：固定 skip=5、limit=10，忽略请求中的 skip/limit。"""
    return {"q": q, "skip": 5, "limit": 10}


app.dependency_overrides[common_parameters] = override_dependency  # 键为原依赖可调用对象


def test_override_in_items():
    """无查询参数时，override 返回 skip=5、limit=10。"""
    response = client.get("/items/")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": None, "skip": 5, "limit": 10},
    }


def test_override_in_items_with_q():
    """?q=foo 时 override 仍保留 q，skip/limit 被固定。"""
    response = client.get("/items/?q=foo")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }


def test_override_in_items_with_params():
    """即使传 skip/limit，override 仍覆盖为 5/10（演示 overrides 优先级）。"""
    response = client.get("/items/?q=foo&skip=100&limit=200")
    assert response.status_code == 200
    assert response.json() == {
        "message": "Hello Items!",
        "params": {"q": "foo", "skip": 5, "limit": 10},
    }
