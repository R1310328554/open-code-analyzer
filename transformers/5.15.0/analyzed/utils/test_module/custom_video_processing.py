# 自定义视频处理器：继承 LlavaOnevisionVideoProcessor 的测试占位类
from transformers import LlavaOnevisionVideoProcessor


# CustomVideoProcessor：空子类，验证 auto video processor 映射加载
class CustomVideoProcessor(LlavaOnevisionVideoProcessor):
    pass
