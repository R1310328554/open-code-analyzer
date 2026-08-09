"""教程 004：用 str(age) 将 int 转为 str 后再与字符串拼接。"""


def get_name_with_age(name: str, age: int):
    """修正 tutorial003 的类型/运行时错误。"""
    name_with_age = name + " is this old: " + str(age)
    return name_with_age
