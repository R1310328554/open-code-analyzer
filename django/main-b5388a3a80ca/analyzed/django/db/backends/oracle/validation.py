from django.core import checks
"""
django.db.backends.oracle.validation — Oracle 字段类型校验。
"""
from django.db.backends.base.validation import BaseDatabaseValidation


# Oracle 校验：CLOB/NCLOB/BLOB 列不支持 db_index
class DatabaseValidation(BaseDatabaseValidation):
    # 受限类型建索引时发出 fields.W162 警告
    def check_field_type(self, field, field_type):
        """Oracle doesn't support a database index on some data types."""
        errors = []
        if field.db_index and field_type.lower() in self.connection._limited_data_types:
            errors.append(
                checks.Warning(
                    "Oracle does not support a database index on %s columns."
                    % field_type,
                    hint=(
                        "An index won't be created. Silence this warning if "
                        "you don't care about it."
                    ),
                    obj=field,
                    id="fields.W162",
                )
            )
        return errors
