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
OpenID Connect 客户端：发现元数据、JWKS 验签 ID Token 并合并 userinfo。
"""

#

import jwt
from common.http_client import sync_request
from .oauth import OAuthClient


# OIDC ID Token 允许的非对称签名算法白名单（排除 HS* 与 none 防算法混淆攻击）。
# Symmetric HMAC algorithms (HS*) are intentionally excluded — when the
# verification key is the asymmetric public key fetched from the provider's
# JWKS (as it is for every OIDC ID token), accepting HS256 lets an attacker
# forge tokens by HMAC-signing them with the public key bytes
# (RSA/HMAC algorithm-confusion attack, CWE-347). "none" is excluded for the
# obvious reason that it disables signature verification entirely.
_ALLOWED_OIDC_SIGNING_ALGS = frozenset(
    {
        "RS256",
        "RS384",
        "RS512",
        "ES256",
        "ES384",
        "ES512",
        "PS256",
        "PS384",
        "PS512",
        "EdDSA",
    }
)

# 发现文档未声明安全算法时回退 RS256，绝不信任 JWT header 中的 alg,
# so this is the safe fallback when a provider's discovery document does not
# advertise ``id_token_signing_alg_values_supported`` (or advertises only
# algorithms outside the safe allowlist).
_DEFAULT_OIDC_SIGNING_ALGS = ("RS256",)


def _resolve_id_token_signing_algs(metadata):
    """
    从发现元数据解析 ID Token 验签算法，与安全白名单求交后回退 RS256。

    Return the algorithms to pass to ``jwt.decode(..., algorithms=...)``.

    Intersects the provider-advertised
    ``id_token_signing_alg_values_supported`` with
    :data:`_ALLOWED_OIDC_SIGNING_ALGS`. Falls back to
    :data:`_DEFAULT_OIDC_SIGNING_ALGS` when the provider does not advertise
    the field or advertises only algorithms outside the safe allowlist —
    crucially, the fallback is to RS256, **never** to whatever the JWT
    header claims at verification time.
    """
    advertised = metadata.get("id_token_signing_alg_values_supported") or []
    if not isinstance(advertised, (list, tuple)):
        advertised = []
    safe = [a for a in advertised if isinstance(a, str) and a in _ALLOWED_OIDC_SIGNING_ALGS]
    return safe or list(_DEFAULT_OIDC_SIGNING_ALGS)


class OIDCClient(OAuthClient):
    """OIDC 扩展：自动发现 endpoint、固定验签算法并解析 id_token。"""
    def __init__(self, config):
        """
        以 issuer 拉取 /.well-known/openid-configuration 并填充 OAuth 端点。
        """
        self.issuer = config.get("issuer")
        if not self.issuer:
            raise ValueError("Missing issuer in configuration.")

        oidc_metadata = self._load_oidc_metadata(self.issuer)
        config.update(
            {
                "issuer": oidc_metadata["issuer"],
                "jwks_uri": oidc_metadata["jwks_uri"],
                "authorization_url": oidc_metadata["authorization_endpoint"],
                "token_url": oidc_metadata["token_endpoint"],
                "userinfo_url": oidc_metadata["userinfo_endpoint"],
            }
        )

        super().__init__(config)
        self.issuer = config["issuer"]
        self.jwks_uri = config["jwks_uri"]
        # Pin the accepted ID-token signing algorithms at construction time
        # from a trusted source (provider metadata + safe allowlist) so the
        # JWT verification step in :meth:`parse_id_token` cannot be tricked
        # by attacker-controlled JWT headers (CWE-345 / CWE-347).
        self.id_token_signing_algs = _resolve_id_token_signing_algs(oidc_metadata)

    @staticmethod
    def _load_oidc_metadata(issuer):
        """
        GET issuer/.well-known/openid-configuration 获取 JWKS 与各 endpoint。
        """
        try:
            metadata_url = f"{issuer}/.well-known/openid-configuration"
            response = sync_request("GET", metadata_url, timeout=7)
            response.raise_for_status()
            return response.json()
        except Exception as e:
            raise ValueError(f"Failed to fetch OIDC metadata: {e}")

    def parse_id_token(self, id_token):
        """
        用 PyJWKClient 取公钥，按构造时固定的 algorithms 验签并解码 claims。

        Parse and validate OIDC ID Token (JWT format) with signature verification.

        The accepted signing algorithms come from ``self.id_token_signing_algs``
        (pinned at construction time from the provider's discovery metadata,
        intersected with :data:`_ALLOWED_OIDC_SIGNING_ALGS`). We deliberately
        do **not** read the algorithm from the unverified JWT header — doing
        so would let an attacker bypass signature verification by setting
        ``"alg": "none"`` or pull off the classic RSA / HMAC algorithm
        confusion by setting ``"alg": "HS256"`` and signing with the public
        key fetched from the provider's JWKS (CWE-345 / CWE-347).
        """
        try:
            # PyJWKClient 按 JWT header kid 从 JWKS 选取验签公钥
            # The client reads the ``kid`` from the JWT header internally to
            # look up the key — that's fine: ``kid`` is not a security
            # decision, the signature still proves which key was used.
            jwks_cli = jwt.PyJWKClient(self.jwks_uri)
            signing_key = jwks_cli.get_signing_key_from_jwt(id_token).key

            # Decode and verify signature against the pinned allowlist.
            decoded_token = jwt.decode(
                id_token,
                key=signing_key,
                algorithms=list(self.id_token_signing_algs),
                audience=str(self.client_id),
                issuer=self.issuer,
            )
            return decoded_token
        except Exception as e:
            raise ValueError(f"Error parsing ID Token: {e}")

    def fetch_user_info(self, access_token, id_token=None, **kwargs):
        """
        优先解析 id_token claims，再合并 userinfo 端点结果。
        """
        user_info = {}
        if id_token:
            user_info = self.parse_id_token(id_token)
        user_info.update(super().fetch_user_info(access_token).to_dict())
        return self.normalize_user_info(user_info)

    async def async_fetch_user_info(self, access_token, id_token=None, **kwargs):
        """async_fetch 版本：id_token + userinfo 合并后 normalize。"""
        user_info = {}
        if id_token:
            user_info = self.parse_id_token(id_token)
        user_info.update((await super().async_fetch_user_info(access_token)).to_dict())
        return self.normalize_user_info(user_info)

    def normalize_user_info(self, user_info):
        return super().normalize_user_info(user_info)
