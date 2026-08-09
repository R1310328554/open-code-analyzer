"""教程 009：`str | None` 联合类型表示可选参数；默认 None 时走通用问候分支。"""


def say_hi(name: str | None = None):
    """name 为 None 时打印 Hello World，否则按名字个性化问候。"""
    if name is not None:
        print(f"Hey {name}!")
    else:
        print("Hello World")
