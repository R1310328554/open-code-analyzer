# 自定义图像处理器：继承 CLIPImageProcessor 的测试占位类
from transformers import CLIPImageProcessor


# CustomImageProcessor：空子类，验证 auto image processor 映射加载
class CustomImageProcessor(CLIPImageProcessor):
    pass
