"""教程 007：tuple 与 set 的泛型注解——tuple 按位置标注，set 标注元素类型。"""


def process_items(items_t: tuple[int, int, str], items_s: set[bytes]):
    """items_t 为 (int, int, str) 三元组；items_s 为 bytes 集合。"""
    return items_t, items_s
