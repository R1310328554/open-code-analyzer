# 存储设置混入：MEDIA_ROOT/URL 变更时清除缓存属性
class StorageSettingsMixin:
    def _clear_cached_properties(self, setting, **kwargs):
        """Reset setting based property values."""
        if setting == "MEDIA_ROOT":
            self.__dict__.pop("base_location", None)
            self.__dict__.pop("location", None)
        elif setting == "MEDIA_URL":
            self.__dict__.pop("base_url", None)
        elif setting == "FILE_UPLOAD_PERMISSIONS":
            self.__dict__.pop("file_permissions_mode", None)
        elif setting == "FILE_UPLOAD_DIRECTORY_PERMISSIONS":
            self.__dict__.pop("directory_permissions_mode", None)

    # 构造参数优先，否则回退到 Django 设置值
    def _value_or_setting(self, value, setting):
        return setting if value is None else value
