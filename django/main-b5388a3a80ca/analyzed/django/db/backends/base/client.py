import os
import subprocess


# 数据库客户端 shell 抽象基类（如 psql、mysql）
class BaseDatabaseClient:
    """Encapsulate backend-specific methods for opening a client shell."""

    # This should be a string representing the name of the executable
    # (e.g., "psql"). Subclasses must override this.
    executable_name = None

    # connection 为 BaseDatabaseWrapper 实例
    def __init__(self, connection):
        # connection is an instance of BaseDatabaseWrapper.
        self.connection = connection

    def __del__(self):
        del self.connection

    @classmethod
    # 将 settings 转为命令行参数与环境变量（子类实现）
    def settings_to_cmd_args_env(cls, settings_dict, parameters):
        raise NotImplementedError(
            "subclasses of BaseDatabaseClient must provide a "
            "settings_to_cmd_args_env() method or override a runshell()."
        )

    # 启动交互式数据库客户端
    def runshell(self, parameters):
        args, env = self.settings_to_cmd_args_env(
            self.connection.settings_dict, parameters
        )
        env = {**os.environ, **env} if env else None
        subprocess.run(args, env=env, check=True)
