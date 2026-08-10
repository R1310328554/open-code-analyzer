"""
聊天渠道核心抽象：入站/出站消息结构与 Channel 生命周期接口。
"""
from __future__ import annotations

import logging
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any, Awaitable, Callable, ClassVar, Optional

LOGGER = logging.getLogger(__name__)


@dataclass
class IncomingMessage:
    # 平台入站消息的标准化表示
    channel: str
    account_id: str
    chat_id: str
    chat_type: str
    message_id: str
    sender_id: str
    text: str
    raw: Any = None


@dataclass
class OutgoingMessage:
    # 回发给用户的出站消息
    chat_id: str
    text: str
    reply_to_message_id: Optional[str] = None


MessageHandler = Callable[[IncomingMessage], Awaitable[None]]


class Channel(ABC):
    """单个 messaging 平台上的一条已配置 Bot 身份。"""

    channel_id: ClassVar[str]
    account_id: str

    def __init__(self) -> None:
        self._handler: Optional[MessageHandler] = None

    def set_message_handler(self, handler: MessageHandler) -> None:
        self._handler = handler

    async def _dispatch(self, message: IncomingMessage) -> None:
        if self._handler is None:
            return
        try:
            await self._handler(message)
        except Exception:  # 框架边界：单条消息处理失败不终止整个渠道
            LOGGER.error("[%s:%s] handler error", self.channel_id, self.account_id, exc_info=True)

    @abstractmethod
    async def start(self) -> None: ...

    @abstractmethod
    async def stop(self) -> None: ...

    @abstractmethod
    async def send(self, message: OutgoingMessage) -> None: ...
