"""教程 006：泛型 list[str]——列表元素类型写在方括号内（type parameter）。"""


def process_items(items: list[str]):
    """items 为字符串列表；遍历时编辑器知道 item 为 str。"""
    for item in items:
        print(item)
