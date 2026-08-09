"""教程 003：带类型提示时编辑器可发现 str 与 int 直接拼接的类型错误。"""


def get_name_with_age(name: str, age: int):
    """age 为 int；与 str 拼接会在静态检查中报错（运行时亦 TypeError）。"""
    name_with_age = name + " is this old: " + age  # 故意错误：应使用 str(age)
    return name_with_age
