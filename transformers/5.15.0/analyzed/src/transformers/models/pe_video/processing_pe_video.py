from ...processing_utils import ProcessorMixin


# PeVideo 处理器：视频帧预处理与 tokenizer 联合封装

# PeVideoProcessor：PeVideo 视频处理器与 tokenizer 联合封装
class PeVideoProcessor(ProcessorMixin):
    attributes = ["video_processor", "tokenizer"]
    video_processor_class = "PeVideoVideoProcessor"
    tokenizer_class = "AutoTokenizer"


__all__ = ["PeVideoProcessor"]
