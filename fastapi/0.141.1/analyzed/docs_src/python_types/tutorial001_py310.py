"""教程 001：无类型提示的基础函数——拼接姓名字符串并打印。"""


def get_full_name(first_name, last_name):
    """将 first_name、last_name 首字母大写后用空格连接。"""
    full_name = first_name.title() + " " + last_name.title()  # title() 首字母大写
    return full_name


print(get_full_name("john", "doe"))  # 输出: John Doe
