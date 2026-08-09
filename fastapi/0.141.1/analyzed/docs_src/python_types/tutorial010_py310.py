"""教程 010：自定义类可作为类型注解，静态检查器会校验传入对象是否为该类型。"""


class Person:
    """简单人物模型，构造时保存 name 属性。"""

    def __init__(self, name: str):
        self.name = name


def get_person_name(one_person: Person):
    """参数须为 Person 实例；通过 .name 返回姓名字符串。"""
    return one_person.name
