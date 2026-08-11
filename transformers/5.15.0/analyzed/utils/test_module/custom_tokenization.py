# 自定义慢速分词器：继承 BertTokenizer 的测试占位类
from transformers import BertTokenizer


# CustomTokenizer：空子类，验证 dynamic module 加载 slow tokenizer
class CustomTokenizer(BertTokenizer):
    pass
