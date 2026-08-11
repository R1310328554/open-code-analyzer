# 动态属性设置器 — 构造时通过 setattr 绑定单个命名属性
class AttributeSetter:
    # 将 value 赋给实例属性 name
    def __init__(self, name, value):
        setattr(self, name, value)
