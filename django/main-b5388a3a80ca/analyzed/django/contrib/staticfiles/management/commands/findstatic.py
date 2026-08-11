import os

from django.contrib.staticfiles import finders
from django.core.management.base import LabelCommand


# findstatic 命令 — 查找给定静态文件的绝对路径
class Command(LabelCommand):
    help = "Finds the absolute paths for the given static file(s)."
    label = "staticfile"

    # 添加 --first 选项（仅返回首个匹配）
    def add_arguments(self, parser):
        super().add_arguments(parser)
        parser.add_argument(
            "--first",
            action="store_false",
            dest="all",
            help="Only return the first match for each static file.",
        )

    # 调用 finders.find 并格式化输出路径与搜索目录
    def handle_label(self, path, **options):
        verbosity = options["verbosity"]
        result = finders.find(path, find_all=options["all"])
        if verbosity >= 2:
            searched_locations = (
                "\nLooking in the following locations:\n  %s"
                % "\n  ".join([str(loc) for loc in finders.searched_locations])
            )
        else:
            searched_locations = ""
        if result:
            if not isinstance(result, (list, tuple)):
                result = [result]
            result = (os.path.realpath(path) for path in result)
            if verbosity >= 1:
                file_list = "\n  ".join(result)
                return "Found '%s' here:\n  %s%s" % (
                    path,
                    file_list,
                    searched_locations,
                )
            else:
                return "\n".join(result)
        else:
            message = ["No matching file found for '%s'." % path]
            if verbosity >= 2:
                message.append(searched_locations)
            if verbosity >= 1:
                self.stderr.write("\n".join(message))
