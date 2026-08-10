#
#  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
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
租户模型 Provider 服务：按租户管理 LLM 厂商凭证与 Provider 记录的 CRUD。
"""

#
from api.db.db_models import DB, TenantModelProvider
from api.db.services.common_service import CommonService


class TenantModelProviderService(CommonService):
    # 租户 ↔ 模型厂商（OpenAI、Azure 等）绑定关系
    model = TenantModelProvider

    @classmethod
    @DB.connection_context()
    def get_by_tenant_id_and_provider_name(cls, tenant_id, provider_name):
        # 按租户 + 厂商名查单条 Provider 配置
        return cls.model.get_or_none(
            cls.model.tenant_id == tenant_id,
            cls.model.provider_name == provider_name,
        )

    @classmethod
    @DB.connection_context()
    def get_by_tenant_id_and_provider_id(cls, tenant_id, provider_id):
        # 按租户 + Provider 主键查单条
        return cls.model.get_or_none(
            cls.model.tenant_id == tenant_id,
            cls.model.id == provider_id,
        )

    @classmethod
    @DB.connection_context()
    def get_by_tenant_id(cls, tenant_id):
        # 列出某租户下全部 Provider 记录
        return list(cls.model.select().where(cls.model.tenant_id == tenant_id))

    @classmethod
    @DB.connection_context()
    def delete_by_tenant_id(cls, tenant_id):
        # 删除租户下所有 Provider（级联清理前置）
        return cls.model.delete().where(cls.model.tenant_id == tenant_id).execute()

    @classmethod
    @DB.connection_context()
    def delete_by_tenant_id_and_provider_name(cls, tenant_id, provider_name):
        # 按租户 + 厂商名删除单条 Provider
        return (
            cls.model.delete()
            .where(
                cls.model.tenant_id == tenant_id,
                cls.model.provider_name == provider_name,
            )
            .execute()
        )

    @classmethod
    @DB.connection_context()
    def list_provider_names_by_tenant_id(cls, tenant_id):
        # 返回租户已配置的厂商名称列表
        return [row.provider_name for row in cls.model.select(cls.model.provider_name).where(cls.model.tenant_id == tenant_id)]
