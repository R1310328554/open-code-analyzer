# 训练指标记录抽象基类：定义 log_metrics / close 接口
import os
from abc import ABC, abstractmethod


    # 日志后端 ABC：创建 save_dir 并约定指标上报与收尾
class BaseLogger(ABC):
    def __init__(self, save_dir):
        self.save_dir = save_dir
        os.makedirs(self.save_dir, exist_ok=True)

    @abstractmethod
    def log_metrics(self, metrics, prefix=None):
        pass

    @abstractmethod
    def close(self):
        pass
