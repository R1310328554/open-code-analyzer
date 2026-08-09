"""教程 004：后处理 openapi.json——从 operationId 中剥离 tag 前缀以匹配客户端命名习惯。"""

import json
from pathlib import Path

file_path = Path("./openapi.json")  # 由 FastAPI 导出的 OpenAPI 文档
openapi_content = json.loads(file_path.read_text())

for path_data in openapi_content["paths"].values():
    for operation in path_data.values():
        tag = operation["tags"][0]  # 与 tutorial003 中 custom_generate_unique_id 的 tag 一致
        operation_id = operation["operationId"]
        to_remove = f"{tag}-"
        new_operation_id = operation_id[len(to_remove) :]  # 去掉 "items-" / "users-" 前缀
        operation["operationId"] = new_operation_id

file_path.write_text(json.dumps(openapi_content))  # 写回供 openapi-generator 读取
