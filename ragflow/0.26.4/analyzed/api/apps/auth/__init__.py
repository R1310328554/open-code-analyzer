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
"""
第三方登录客户端工厂：按配置 type 实例化 OAuth2、OIDC 或 GitHub 客户端。
"""

#

from .oauth import OAuthClient
from .oidc import OIDCClient
from .github import GithubOAuthClient


# 配置 type 到具体客户端实现的映射
CLIENT_TYPES = {"oauth2": OAuthClient, "oidc": OIDCClient, "github": GithubOAuthClient}


def get_auth_client(config) -> OAuthClient:
    """
    根据 channel 配置创建认证客户端；无 type 时按 issuer 推断 OIDC 或 OAuth2。
    """
    channel_type = str(config.get("type", "")).lower()
    # 未显式指定 type 时：有 issuer 视为 OIDC，否则默认 oauth2
    # 未显式指定 type 时：有 issuer 视为 OIDC，否则默认 oauth2
    if channel_type == "":
        if config.get("issuer"):
            channel_type = "oidc"
        else:
            channel_type = "oauth2"
    client_class = CLIENT_TYPES.get(channel_type)
    if not client_class:
        raise ValueError(f"Unsupported type: {channel_type}")

    return client_class(config)
