"""教程 008：dict 泛型需两个类型参数——键类型与值类型，逗号分隔。"""


def process_items(prices: dict[str, float]):
    """prices 键为商品名 str，值为价格 float。"""
    for item_name, item_price in prices.items():
        print(item_name)
        print(item_price)
