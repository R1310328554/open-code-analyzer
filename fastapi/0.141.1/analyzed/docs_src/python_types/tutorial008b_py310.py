"""教程 008b：联合类型 int | str——变量可为竖线分隔的多种类型之一。"""


def process_item(item: int | str):
    """`|` 表示 union；item 接受 int 或 str。"""
    print(item)
