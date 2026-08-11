# 自定义 Processor：组合 feature_extractor 与 tokenizer 的多模态处理器
from transformers import ProcessorMixin


# CustomProcessor：ProcessorMixin 包装类，用于 trust_remote_code 加载测试
class CustomProcessor(ProcessorMixin):
    # __init__：将特征提取器与分词器注册到 ProcessorMixin
    def __init__(self, feature_extractor, tokenizer):
        super().__init__(feature_extractor, tokenizer)
