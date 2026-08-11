# 自定义特征提取器：继承 Wav2Vec2FeatureExtractor 的测试占位类
from transformers import Wav2Vec2FeatureExtractor


# CustomFeatureExtractor：空子类，验证 auto feature extractor 映射加载
class CustomFeatureExtractor(Wav2Vec2FeatureExtractor):
    pass
