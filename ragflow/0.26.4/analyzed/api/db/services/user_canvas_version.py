"""
用户画布版本服务：Agent DSL 快照历史、规范化去重保存与未发布版本清理。
"""

import json
import logging
import time

from agent.dsl_migration import normalize_chunker_dsl
from api.db.db_models import UserCanvasVersion, DB
from api.db.services.common_service import CommonService
from peewee import DoesNotExist


class UserCanvasVersionService(CommonService):
    # UserCanvas 的 DSL 版本链：草稿/发布态分离
    model = UserCanvasVersion

    # 生成版本标题：{昵称}_{Agent名}_{时间戳}
    @staticmethod
    def build_version_title(user_nickname, agent_title, ts=None):
        tenant = str(user_nickname or "").strip() or "tenant"
        title = str(agent_title or "").strip() or "agent"
        stamp = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(ts)) if ts is not None else time.strftime("%Y-%m-%d %H:%M:%S")
        return "{0}_{1}_{2}".format(tenant, title, stamp)

    # 写入或比较前规范化 DSL（含 chunker 迁移）
    @staticmethod
    def _normalize_dsl(dsl):
        normalized = dsl
        if isinstance(normalized, str):
            try:
                normalized = json.loads(normalized)
            except Exception as e:
                raise ValueError("Invalid DSL JSON string.") from e

        if not isinstance(normalized, dict):
            raise ValueError("DSL must be a JSON object.")

        try:
            return json.loads(json.dumps(normalize_chunker_dsl(normalized), ensure_ascii=False))
        except Exception as e:
            raise ValueError("DSL is not JSON-serializable.") from e

    @classmethod
    @DB.connection_context()
    def list_by_canvas_id(cls, user_canvas_id):
        # 列出某画布的全部版本元数据（不含 dsl 正文）
        try:
            user_canvas_version = cls.model.select(
                *[cls.model.id, cls.model.create_time, cls.model.title, cls.model.create_date, cls.model.update_date, cls.model.user_canvas_id, cls.model.update_time, cls.model.release]
            ).where(cls.model.user_canvas_id == user_canvas_id)
            return user_canvas_version
        except DoesNotExist:
            return None
        except Exception:
            return None

    @classmethod
    @DB.connection_context()
    def get_all_canvas_version_by_canvas_ids(cls, canvas_ids):
        # 批量分页拉取多个画布的版本 id（每批 100）
        fields = [cls.model.id]
        versions = cls.model.select(*fields).where(cls.model.user_canvas_id.in_(canvas_ids))
        versions.order_by(cls.model.create_time.asc())
        offset, limit = 0, 100
        res = []
        while True:
            version_batch = versions.offset(offset).limit(limit)
            _temp = list(version_batch.dicts())
            if not _temp:
                break
            res.extend(_temp)
            offset += limit
        return res

    @classmethod
    @DB.connection_context()
    def delete_all_versions(cls, user_canvas_id):
        # 保留已发布版本；未发布超过 20 条时删除最旧草稿
        try:
            # Only get unpublished versions (False or None), keep all released versions
            unpublished = cls.model.select().where(cls.model.user_canvas_id == user_canvas_id, (~cls.model.release) | (cls.model.release.is_null(True))).order_by(cls.model.create_time.desc())

            # Only delete old unpublished versions beyond the limit
            if unpublished.count() > 20:
                delete_ids = [v.id for v in unpublished[20:]]
                cls.delete_by_ids(delete_ids)

            return True
        except DoesNotExist:
            return None
        except Exception:
            return None

    @classmethod
    @DB.connection_context()
    def _get_latest_by_canvas_id(cls, user_canvas_id, only_released=False):
        """获取画布最新版本，可选仅已发布。"""
        try:
            query = cls.model.select().where(cls.model.user_canvas_id == user_canvas_id)
            if only_released:
                query = query.where(cls.model.release)
            return query.order_by(cls.model.create_time.desc()).first()
        except DoesNotExist:
            return None
        except Exception as e:
            logging.exception(e)
            return None

    @classmethod
    def get_latest_released(cls, user_canvas_id):
        """获取画布最新已发布版本。"""
        return cls._get_latest_by_canvas_id(user_canvas_id, only_released=True)

    @classmethod
    def get_latest_version_title(cls, user_canvas_id, release_mode=False):
        """按 release_mode 返回最新（或最新已发布）版本的 title。

        Args:
            user_canvas_id: The canvas ID
            release_mode: If True, get the latest released version title;
                         If False, get the latest version title (regardless of release status)
        """
        latest = cls._get_latest_by_canvas_id(user_canvas_id, only_released=release_mode)
        return latest.title if latest else None

    @classmethod
    @DB.connection_context()
    def save_or_replace_latest(cls, user_canvas_id, dsl, title=None, description=None, release=None):
        """
        将画布 DSL 写入版本历史。

        若最新版本 DSL 相同则原地更新（已发布版本受保护时会新建行）
        instead of creating a new row.

        Exception: If the latest version is released (release=True) and current save is not,
        create a new version to protect the released version.
        """
        try:
            normalized_dsl = cls._normalize_dsl(dsl)
            latest = cls.model.select().where(cls.model.user_canvas_id == user_canvas_id).order_by(cls.model.create_time.desc()).first()

            # DSL 未变：刷新最新快照；已发布 + 当前非发布则新建版本
            if latest and cls._normalize_dsl(latest.dsl) == normalized_dsl:
                # Protect released version: if latest is released and current is not,
                # create a new version instead of updating
                if latest.release and not release:
                    insert_data = {"user_canvas_id": user_canvas_id, "dsl": normalized_dsl}
                    if title is not None:
                        insert_data["title"] = title
                    if description is not None:
                        insert_data["description"] = description
                    if release is not None:
                        insert_data["release"] = release
                    cls.insert(**insert_data)
                    cls.delete_all_versions(user_canvas_id)
                    return None, True

                # Normal case: update existing version
                # DSL unchanged: do NOT update title to preserve version identity
                # Only update dsl (for normalization consistency), description, and release
                update_data = {"dsl": normalized_dsl}
                if description is not None:
                    update_data["description"] = description
                if release is not None:
                    update_data["release"] = release
                cls.update_by_id(latest.id, update_data)
                cls.delete_all_versions(user_canvas_id)
                return latest.id, False

            # DSL 有实质变更：插入新版本并触发草稿清理
            insert_data = {"user_canvas_id": user_canvas_id, "dsl": normalized_dsl}
            if title is not None:
                insert_data["title"] = title
            if description is not None:
                insert_data["description"] = description
            if release is not None:
                insert_data["release"] = release
            cls.insert(**insert_data)
            cls.delete_all_versions(user_canvas_id)
            return None, True
        except Exception as e:
            logging.exception(e)
            return None, None
