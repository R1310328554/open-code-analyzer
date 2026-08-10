"""
渠道 Builder 注册表：按名称注册并在配置中实例化各 account 的 Channel。
"""
from __future__ import annotations

import logging
from typing import Callable, Dict, List

from .base import Channel

LOGGER = logging.getLogger(__name__)


ChannelBuilder = Callable[[str, dict], Channel]

_BUILDERS: Dict[str, ChannelBuilder] = {}


def register_channel(name: str, builder: ChannelBuilder) -> None:
    # 各渠道包在 import 时调用以注册 builder
    _BUILDERS[name] = builder


def registered_channel_ids() -> List[str]:
    return sorted(_BUILDERS)


def build_channels(config: dict) -> List[Channel]:
    """遍历 config.channels.<name>.accounts.<id>，为每个 account 构造一个 Channel 实例。"""
    instances: List[Channel] = []
    channels_cfg = config.get("channels") or {}
    for name, raw in channels_cfg.items():
        if not isinstance(raw, dict) or raw.get("enabled") is False:
            continue
        builder = _BUILDERS.get(name)
        if builder is None:
            LOGGER.warning("no builder registered for channel '%s'; skipping", name)
            continue
        accounts = raw.get("accounts") or {}
        if not accounts:
            # 无 accounts 块时退化为 flat 单账号配置
            accounts = {"default": {k: v for k, v in raw.items() if k != "accounts"}}
        shared = {k: v for k, v in raw.items() if k not in ("accounts", "default_account")}
        for account_id, account_cfg in accounts.items():
            if not isinstance(account_cfg, dict):
                continue
            if account_cfg.get("enabled") is False:
                continue
            merged = {**shared, **account_cfg}
            instances.append(builder(str(account_id), merged))
    return instances
