# 数据库后端字段校验抽象基类
class BaseDatabaseValidation:
    """Encapsulate backend-specific validation."""

    # 绑定 BaseDatabaseWrapper 实例
    def __init__(self, connection):
        self.connection = connection

    def __del__(self):
        del self.connection

    # 系统检查入口，默认无错误
    def check(self, **kwargs):
        return []

    # 校验单个字段类型是否被后端支持
    def check_field(self, field, **kwargs):
        errors = []
        # Backends may implement a check_field_type() method.
        if (
            hasattr(self, "check_field_type")
            and
            # Ignore any related fields.
            not getattr(field, "remote_field", None)
        ):
            # Ignore fields with unsupported features.
            db_supports_all_required_features = all(
                getattr(self.connection.features, feature, False)
                for feature in field.model._meta.required_db_features
            )
            if db_supports_all_required_features:
                field_type = field.db_type(self.connection)
                # Ignore non-concrete fields.
                if field_type is not None:
                    errors.extend(self.check_field_type(field, field_type))
        return errors
