# 自定义 Pipeline：文本对分类示例，含 softmax 与 logits 后处理
import numpy as np

from transformers import Pipeline


# softmax：对 logits 做数值稳定 softmax，返回各类别概率
def softmax(outputs):
    maxes = np.max(outputs, axis=-1, keepdims=True)
    shifted_exp = np.exp(outputs - maxes)
    return shifted_exp / shifted_exp.sum(axis=-1, keepdims=True)


# PairClassificationPipeline：句子对分类 Pipeline，复用 tokenizer 与模型推理
class PairClassificationPipeline(Pipeline):
    # _sanitize_parameters：从 kwargs 提取 second_text 传入预处理
    def _sanitize_parameters(self, **kwargs):
        preprocess_kwargs = {}
        if "second_text" in kwargs:
            preprocess_kwargs["second_text"] = kwargs["second_text"]
        return preprocess_kwargs, {}, {}

    # preprocess：将主句与第二句编码为模型输入张量
    def preprocess(self, text, second_text=None):
        return self.tokenizer(text, text_pair=second_text, return_tensors="pt")

    # _forward：调用底层模型前向传播
    def _forward(self, model_inputs):
        return self.model(**model_inputs)

    # postprocess：softmax 取 argmax 标签、置信度与原始 logits
    def postprocess(self, model_outputs):
        logits = model_outputs.logits[0].numpy()
        probabilities = softmax(logits)

        best_class = np.argmax(probabilities)
        label = self.model.config.id2label[best_class]
        score = probabilities[best_class].item()
        logits = logits.tolist()
        return {"label": label, "score": score, "logits": logits}
