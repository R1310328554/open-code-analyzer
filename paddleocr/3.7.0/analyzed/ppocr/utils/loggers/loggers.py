# 多后端日志聚合器：将 metrics/model 同步转发至各 Logger
from .wandb_logger import WandbLogger


    # 组合多个 BaseLogger，统一 log_metrics / log_model / close
class Loggers(object):
    def __init__(self, loggers):
        super().__init__()
        self.loggers = loggers

    def log_metrics(self, metrics, prefix=None, step=None):
        for logger in self.loggers:
            logger.log_metrics(metrics, prefix=prefix, step=step)

    def log_model(self, is_best, prefix, metadata=None):
        for logger in self.loggers:
            logger.log_model(is_best=is_best, prefix=prefix, metadata=metadata)

    def close(self):
        for logger in self.loggers:
            logger.close()
