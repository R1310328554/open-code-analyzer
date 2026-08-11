# 自定义快速分词器：BertTokenizerFast 子类，绑定 slow 对应类与 Auto 映射
from transformers import BertTokenizerFast

from .custom_tokenization import CustomTokenizer


# CustomTokenizerFast：Fast 分词器，指向 CustomTokenizer 并注册 _auto_map
class CustomTokenizerFast(BertTokenizerFast):
    slow_tokenizer_class = CustomTokenizer
    _auto_map = {
        "AutoTokenizer": ("custom_tokenization.CustomTokenizer", "custom_tokenization_fast.CustomTokenizerFast")
    }
