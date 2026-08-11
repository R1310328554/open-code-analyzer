from django.core import checks
"""
django.db.backends.mysql.validation — MySQL 数据库配置与字段校验。

检查 sql_mode 严格模式及 varchar 唯一索引长度等 MySQL 特有约束。
"""
from django.db.backends.base.validation import BaseDatabaseValidation
from django.utils.version import get_docs_version


# MySQL 校验器：sql_mode 与字段类型兼容性
class DatabaseValidation(BaseDatabaseValidation):
    # 合并基类检查与 sql_mode 严格模式警告
    def check(self, **kwargs):
        issues = super().check(**kwargs)
        issues.extend(self._check_sql_mode(**kwargs))
        return issues

    # 未启用 STRICT_TRANS_TABLES/STRICT_ALL_TABLES 时发出 W002 警告
    def _check_sql_mode(self, **kwargs):
        if not (
            self.connection.sql_mode & {"STRICT_TRANS_TABLES", "STRICT_ALL_TABLES"}
        ):
            return [
                checks.Warning(
                    "%s Strict Mode is not set for database connection '%s'"
                    % (self.connection.display_name, self.connection.alias),
                    hint=(
                        "%s's Strict Mode fixes many data integrity problems in "
                        "%s, such as data truncation upon insertion, by "
                        "escalating warnings into errors. It is strongly "
                        "recommended you activate it. See: "
                        "https://docs.djangoproject.com/en/%s/ref/databases/"
                        "#mysql-sql-mode"
                        % (
                            self.connection.display_name,
                            self.connection.display_name,
                            get_docs_version(),
                        ),
                    ),
                    id="mysql.W002",
                )
            ]
        return []

    # 唯一 varchar 长度 >255 及受限类型索引不可建时发出警告
    def check_field_type(self, field, field_type):
        """
        MySQL has the following field length restriction:
        No character (varchar) fields can have a length exceeding 255
        characters if they have a unique index on them.
        MySQL doesn't support a database index on some data types.
        """
        errors = []
        if (
            field_type.startswith("varchar")
            and field.unique
            and (field.max_length is None or int(field.max_length) > 255)
        ):
            errors.append(
                checks.Warning(
                    "%s may not allow unique CharFields to have a max_length "
                    "> 255." % self.connection.display_name,
                    obj=field,
                    hint=(
                        "See: https://docs.djangoproject.com/en/%s/ref/"
                        "databases/#mysql-character-fields" % get_docs_version()
                    ),
                    id="mysql.W003",
                )
            )

        if field.db_index and field_type.lower() in self.connection._limited_data_types:
            errors.append(
                checks.Warning(
                    "%s does not support a database index on %s columns."
                    % (self.connection.display_name, field_type),
                    hint=(
                        "An index won't be created. Silence this warning if "
                        "you don't care about it."
                    ),
                    obj=field,
                    id="fields.W162",
                )
            )
        return errors
