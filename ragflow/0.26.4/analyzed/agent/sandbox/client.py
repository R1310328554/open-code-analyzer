#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

"""
Agent 组件使用的沙箱客户端。

从系统设置加载沙箱 Provider，并提供统一的代码执行、健康检查
与 Provider 信息查询入口。
"""

import json
import logging
from typing import Dict, Any, Optional

from api.db.services.system_settings_service import SystemSettingsService
from agent.sandbox.providers import ProviderManager
from agent.sandbox.providers.base import ExecutionResult, SandboxProviderConfigError

logger = logging.getLogger(__name__)


# 全局 Provider 管理器单例
_provider_manager: Optional[ProviderManager] = None


def get_provider_manager() -> ProviderManager:
    """
    获取全局 Provider 管理器；首次调用时从系统设置加载并初始化。
    """
    global _provider_manager

    if _provider_manager is not None:
        return _provider_manager

    _provider_manager = ProviderManager()
    _load_provider_from_settings()

    return _provider_manager


def _load_provider_from_settings() -> None:
    """
    从系统设置解析 Provider 类型与配置，实例化并注册到管理器。
    """
    global _provider_manager

    if _provider_manager is None:
        return

    try:
        provider_type = _resolve_provider_type()
        config = _load_provider_config(provider_type)

        # 按类型映射到具体 Provider 类并实例化
        from agent.sandbox.providers import (
            SelfManagedProvider,
            AliyunCodeInterpreterProvider,
            E2BProvider,
            LocalProvider,
            SSHProvider,
        )

        provider_classes = {
            "self_managed": SelfManagedProvider,
            "aliyun_codeinterpreter": AliyunCodeInterpreterProvider,
            "e2b": E2BProvider,
            "local": LocalProvider,
            "ssh": SSHProvider,
        }

        if provider_type not in provider_classes:
            logger.error(f"Unknown provider type: {provider_type}")
            return

        provider_class = provider_classes[provider_type]
        provider = provider_class()

        # 初始化 Provider；local/ssh 配置错误时直接抛出
        if not provider.initialize(config):
            message = f"Failed to initialize sandbox provider: {provider_type}. Config keys: {list(config.keys())}"
            if provider_type in {"local", "ssh"}:
                raise SandboxProviderConfigError(message)
            logger.error(message)
            return

        # 注册为当前活跃 Provider
        _provider_manager.set_provider(provider_type, provider)
        logger.info(f"Sandbox provider '{provider_type}' initialized successfully")

    except SandboxProviderConfigError:
        raise
    except Exception as e:
        logger.error(f"Failed to load sandbox provider from settings: {e}")
        import traceback

        traceback.print_exc()


def _load_provider_config_from_settings(provider_type: str) -> Dict[str, Any]:
    """从系统设置读取 sandbox.{provider_type} 的 JSON 配置。"""
    """从系统设置读取 sandbox.{provider_type} 的 JSON 配置。"""
    provider_config_settings = SystemSettingsService.get_by_name(f"sandbox.{provider_type}")
    if not provider_config_settings:
        logger.warning(f"No configuration found for provider: {provider_type}")
        return {}

    try:
        return json.loads(provider_config_settings[0].value)
    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse sandbox config for {provider_type}: {e}")
        return {}


def _resolve_provider_type() -> str:
    """解析 sandbox.provider_type，缺省为 self_managed。"""
    """解析 sandbox.provider_type，缺省为 self_managed。"""
    provider_type_settings = SystemSettingsService.get_by_name("sandbox.provider_type")
    if not provider_type_settings:
        return "self_managed"
    return provider_type_settings[0].value


def _load_provider_config(provider_type: str) -> Dict[str, Any]:
    return _load_provider_config_from_settings(provider_type)


def reload_provider() -> None:
    """
    清空单例并重新从系统设置加载 Provider（管理端修改配置后调用）。
    """
    global _provider_manager
    _provider_manager = None
    _load_provider_from_settings()


def execute_code(code: str, language: str = "python", timeout: int = 30, arguments: Optional[Dict[str, Any]] = None) -> ExecutionResult:
    """
    在已配置沙箱中执行代码（Agent 组件主入口）。

    创建临时实例、执行代码并在 finally 中销毁实例。
    """
    provider_manager = get_provider_manager()

    if not provider_manager.is_configured():
        raise RuntimeError("No sandbox provider configured. Please configure sandbox settings in the admin panel.")

    provider = provider_manager.get_provider()
    provider_name = provider_manager.get_provider_name() or getattr(provider, "__class__", type(provider)).__name__

    logger.info(
        "CodeExec using sandbox provider '%s' (language=%s, timeout=%ss)",
        provider_name,
        language,
        timeout,
    )

    # 按语言模板创建一次性沙箱实例
    instance = provider.create_instance(template=language)

    try:
        # 在实例内执行用户代码
        result = provider.execute_code(instance_id=instance.instance_id, code=code, language=language, timeout=timeout, arguments=arguments)

        return result

    finally:
        # 无论成功与否都尝试销毁实例
        try:
            provider.destroy_instance(instance.instance_id)
        except Exception as e:
            logger.warning(f"Failed to destroy sandbox instance {instance.instance_id}: {e}")


def health_check() -> bool:
    """
    检查沙箱 Provider 是否已配置且健康。
    """
    try:
        provider_manager = get_provider_manager()

        if not provider_manager.is_configured():
            return False

        provider = provider_manager.get_provider()
        return provider.health_check()

    except Exception as e:
        logger.error(f"Sandbox health check failed: {e}")
        return False


def get_provider_info() -> Dict[str, Any]:
    """
    返回当前 Provider 类型、是否已配置及健康状态摘要。
    """
    try:
        provider_manager = get_provider_manager()

        return {
            "provider_type": provider_manager.get_provider_name(),
            "configured": provider_manager.is_configured(),
            "healthy": health_check(),
        }

    except Exception as e:
        logger.error(f"Failed to get provider info: {e}")
        return {
            "provider_type": None,
            "configured": False,
            "healthy": False,
        }
