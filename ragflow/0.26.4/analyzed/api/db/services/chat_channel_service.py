#
#  Copyright 2024 The InfiniFlow Authors. All Rights Reserved.
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
聊天渠道 Bot 服务：租户渠道列表、全量启用 Bot 与跨租户访问校验。
"""

#
import logging

from peewee import JOIN

from api.db.db_models import DB, ChatChannel, Dialog
from api.db.services.common_service import CommonService

LOGGER = logging.getLogger(__name__)


class ChatChannelService(CommonService):
    """第三方 IM 渠道与 Dialog 绑定关系的 CRUD 与权限。"""

    model = ChatChannel

    @classmethod
    @DB.connection_context()
    def list(cls, tenant_id):
        """列出租户渠道 Bot（不含凭据），LEFT JOIN Dialog 取对话名称。"""
        fields = [
            cls.model.id,
            cls.model.name,
            cls.model.channel,
            cls.model.chat_id,
            cls.model.status,
            Dialog.name.alias("dialog_name"),
        ]
        return list(
            cls.model.select(*fields)
            .join(
                Dialog,
                join_type=JOIN.LEFT_OUTER,
                on=(Dialog.id == cls.model.chat_id),
            )
            .where(cls.model.tenant_id == tenant_id)
            .order_by(cls.model.create_time.desc())
            .dicts()
        )

    @classmethod
    @DB.connection_context()
    def list_active(cls):
        """返回全租户 status=1 的启用 Bot（含凭据，供 worker 消费）。"""
        return list(cls.model.select().where(cls.model.status == 1))

    @classmethod
    @DB.connection_context()
    def accessible(cls, channel_id: str, user_id: str) -> bool:
        """校验用户是否为渠道租户所有者或已加入该团队。"""
        e, channel = cls.get_by_id(channel_id)
        if not e:
            LOGGER.warning("chat channel access denied: not found channel_id=%s user_id=%s", channel_id, user_id)
            return False

        if channel.tenant_id == user_id:
            return True

        from api.db.services.user_service import TenantService

        joined_tenants = TenantService.get_joined_tenants_by_user_id(user_id)
        has_access = any(tenant["tenant_id"] == channel.tenant_id for tenant in joined_tenants)
        if not has_access:
            LOGGER.warning(
                "chat channel access denied: tenant mismatch channel_id=%s user_id=%s tenant_id=%s",
                channel_id,
                user_id,
                channel.tenant_id,
            )
        return has_access
