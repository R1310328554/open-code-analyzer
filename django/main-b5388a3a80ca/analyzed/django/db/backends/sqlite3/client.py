from django.db.backends.base.client import BaseDatabaseClient


# SQLite 命令行客户端（sqlite3）启动封装
class DatabaseClient(BaseDatabaseClient):
    executable_name = "sqlite3"

    @classmethod
    # 将 NAME 与额外参数拼为 sqlite3 命令行
    def settings_to_cmd_args_env(cls, settings_dict, parameters):
        args = [cls.executable_name, settings_dict["NAME"], *parameters]
        return args, None
