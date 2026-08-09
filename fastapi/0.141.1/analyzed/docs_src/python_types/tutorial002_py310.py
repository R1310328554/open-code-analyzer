"""教程 002：用 `参数: 类型` 添加类型提示——编辑器可据此提供补全与检查。"""


def get_full_name(first_name: str, last_name: str):
    """类型提示不改变运行时行为，仅帮助 IDE 与静态分析工具。"""
    full_name = first_name.title() + " " + last_name.title()
    return full_name


print(get_full_name("john", "doe"))
