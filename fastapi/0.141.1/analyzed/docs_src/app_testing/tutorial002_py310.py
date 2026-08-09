"""教程 002：在同一文件中测试 HTTP GET 与 WebSocket 连接。"""

from fastapi import FastAPI
from fastapi.testclient import TestClient
from fastapi.websockets import WebSocket

app = FastAPI()


@app.get("/")
async def read_main():
    return {"msg": "Hello World"}


@app.websocket("/ws")
async def websocket(websocket: WebSocket):
    """接受 WebSocket 连接并发送 JSON 后关闭。"""
    await websocket.accept()
    await websocket.send_json({"msg": "Hello WebSocket"})
    await websocket.close()


def test_read_main():
    """验证 HTTP GET 根路径。"""
    client = TestClient(app)
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"msg": "Hello World"}


def test_websocket():
    """验证 WebSocket 连接与 JSON 消息。"""
    client = TestClient(app)
    with client.websocket_connect("/ws") as websocket:
        data = websocket.receive_json()
        assert data == {"msg": "Hello WebSocket"}
