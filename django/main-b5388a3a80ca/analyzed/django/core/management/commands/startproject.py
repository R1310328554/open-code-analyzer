from django.core.checks.security.base import SECRET_KEY_INSECURE_PREFIX
from django.core.management.templates import TemplateCommand
from django.core.management.utils import get_random_secret_key


# startproject 命令：创建 Django 项目目录结构
class Command(TemplateCommand):
    help = (
        "Creates a Django project directory structure for the given project "
        "name in the current directory or optionally in the given directory."
    )
    missing_args_message = "You must provide a project name."
    requires_settings = False

    # 生成随机 SECRET_KEY 并委托 TemplateCommand 渲染 project 模板
    def handle(self, **options):
        project_name = options.pop("name")
        target = options.pop("directory")

        # Create a random SECRET_KEY to put it in the main settings.
        options["secret_key"] = SECRET_KEY_INSECURE_PREFIX + get_random_secret_key()

        super().handle("project", project_name, target, **options)
