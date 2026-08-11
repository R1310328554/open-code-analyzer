from django.core.management.templates import TemplateCommand


# startapp 命令：在当前或指定目录创建 Django 应用目录结构
class Command(TemplateCommand):
    help = (
        "Creates a Django app directory structure for the given app name in "
        "the current directory or optionally in the given directory."
    )
    missing_args_message = "You must provide an application name."
    requires_settings = False

    # 提取应用名与目标目录，委托 TemplateCommand 渲染 app 模板
    def handle(self, **options):
        app_name = options.pop("name")
        target = options.pop("directory")
        super().handle("app", app_name, target, **options)
