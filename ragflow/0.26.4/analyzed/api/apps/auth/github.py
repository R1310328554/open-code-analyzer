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
GitHub OAuth 客户端：固定 authorize/token/userinfo 端点并补全主邮箱。
"""

#

from common.http_client import async_request, sync_request
from .oauth import OAuthClient, UserInfo


class GithubOAuthClient(OAuthClient):
    """GitHub 专用 OAuth 实现：预置端点并从 /user/emails 取 primary 邮箱。"""
    def __init__(self, config):
        """
        注入 GitHub OAuth 端点与 scope 后调用基类初始化。
        """
        config.update(
            {
                "authorization_url": "https://github.com/login/oauth/authorize",
                "token_url": "https://github.com/login/oauth/access_token",
                "userinfo_url": "https://api.github.com/user",
                "scope": "user:email",
            }
        )
        super().__init__(config)

    def fetch_user_info(self, access_token, **kwargs):
        """
        同步拉取 /user 与 /user/emails，合并主邮箱后 normalize。
        """
        user_info = {}
        try:
            headers = {"Authorization": f"Bearer {access_token}"}
            response = sync_request("GET", self.userinfo_url, headers=headers, timeout=self.http_request_timeout)
            response.raise_for_status()
            user_info.update(response.json())
            email_response = sync_request("GET", self.userinfo_url + "/emails", headers=headers, timeout=self.http_request_timeout)
            email_response.raise_for_status()
            email_info = email_response.json()
            user_info["email"] = next((email for email in email_info if email["primary"]), None)["email"]
            return self.normalize_user_info(user_info)
        except Exception as e:
            raise ValueError(f"Failed to fetch github user info: {e}")

    async def async_fetch_user_info(self, access_token, **kwargs):
        """fetch_user_info 的 httpx 异步版本。"""
        user_info = {}
        headers = {"Authorization": f"Bearer {access_token}"}
        try:
            response = await async_request(
                "GET",
                self.userinfo_url,
                headers=headers,
                timeout=self.http_request_timeout,
            )
            response.raise_for_status()
            user_info.update(response.json())

            email_response = await async_request(
                "GET",
                self.userinfo_url + "/emails",
                headers=headers,
                timeout=self.http_request_timeout,
            )
            email_response.raise_for_status()
            email_info = email_response.json()
            user_info["email"] = next((email for email in email_info if email["primary"]), None)["email"]
            return self.normalize_user_info(user_info)
        except Exception as e:
            raise ValueError(f"Failed to fetch github user info: {e}")

    def normalize_user_info(self, user_info):
        """将 GitHub 原始 JSON 映射为统一 UserInfo。"""
        email = user_info.get("email")
        username = user_info.get("login", str(email).split("@")[0])
        nickname = user_info.get("name", username)
        avatar_url = user_info.get("avatar_url", "")
        return UserInfo(email=email, username=username, nickname=nickname, avatar_url=avatar_url)
