"""OpenAPI 生成所用的常量定义。"""

# 可能携带请求体的 HTTP 方法集合
METHODS_WITH_BODY = {"GET", "HEAD", "POST", "PUT", "DELETE", "PATCH"}
# OpenAPI 组件 schema 引用前缀
REF_PREFIX = "#/components/schemas/"
# schema 引用模板，{model} 为模型名
REF_TEMPLATE = "#/components/schemas/{model}"
