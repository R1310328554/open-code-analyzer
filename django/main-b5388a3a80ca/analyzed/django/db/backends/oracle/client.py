import shutil

"""
django.db.backends.oracle.client — Oracle 交互式客户端（sqlplus）。
"""
from django.db.backends.base.client import BaseDatabaseClient


# Oracle 客户端：sqlplus 命令行，可选 rlwrap 包装
class DatabaseClient(BaseDatabaseClient):
    executable_name = "sqlplus"
    wrapper_name = "rlwrap"

    @staticmethod
    # 生成 user/"password"@dsn 连接串
    def connect_string(settings_dict):
        from django.db.backends.oracle.utils import dsn

        return '%s/"%s"@%s' % (
            settings_dict["USER"],
            settings_dict["PASSWORD"],
            dsn(settings_dict),
        )

    @classmethod
    # 返回 [rlwrap, sqlplus, -L, connect_string, ...] 参数
    def settings_to_cmd_args_env(cls, settings_dict, parameters):
        args = [cls.executable_name, "-L", cls.connect_string(settings_dict)]
        wrapper_path = shutil.which(cls.wrapper_name)
        if wrapper_path:
            args = [wrapper_path, *args]
        args.extend(parameters)
        return args, None
