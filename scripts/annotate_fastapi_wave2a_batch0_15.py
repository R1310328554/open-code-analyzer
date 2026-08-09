#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-2a slice [0:15]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:15]

PREPEND: dict[str, str] = {
    "fastapi/_compat/v2.py": '"""Pydantic v2 兼容层：模型字段、JSON Schema 生成与序列化。"""\n\n',
    "fastapi/openapi/models.py": '"""OpenAPI 3.1 规范对应的 Pydantic 模型定义。"""\n\n',
    "fastapi/security/utils.py": '"""HTTP 安全方案辅助工具。"""\n\n',
    "fastapi/staticfiles.py": '"""从 Starlette 重新导出静态文件服务。"""\n\n',
    "fastapi/templating.py": '"""从 Starlette 重新导出 Jinja2 模板引擎。"""\n\n',
    "fastapi/testclient.py": '"""从 Starlette 重新导出测试客户端。"""\n\n',
    "fastapi/types.py": '"""FastAPI 内部使用的类型别名与泛型定义。"""\n\n',
    "fastapi/websockets.py": '"""从 Starlette 重新导出 WebSocket 相关类型。"""\n\n',
    "docs_src/additional_responses/__init__.py": '"""附加响应示例文档源码包。"""\n',
    "docs_src/additional_responses/tutorial001_py310.py": (
        '"""\n附加响应示例 001：为路径操作声明额外状态码（如 404）的响应模型。\n"""\n\n'
    ),
    "docs_src/additional_responses/tutorial002_py310.py": (
        '"""\n附加响应示例 002：为 200 响应声明多种 media type（JSON 与 image/png）。\n"""\n\n'
    ),
    "docs_src/additional_responses/tutorial003_py310.py": (
        '"""\n附加响应示例 003：为不同状态码配置描述、示例与额外响应模型。\n"""\n\n'
    ),
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "fastapi/_compat/v2.py": [
        (
            "    # eval_type_lenient has been deprecated since Pydantic v2.10.0b1 (PR #10530)",
            "    # eval_type_lenient 自 Pydantic v2.10.0b1 起已弃用（PR #10530）",
        ),
        (
            "    # TODO: remove when this is merged (or equivalent): https://github.com/pydantic/pydantic/pull/12841\n"
            "    # and dropping support for any version of Pydantic before that one (so, in a very long time)",
            "    # TODO：待 https://github.com/pydantic/pydantic/pull/12841 合并后移除\n"
            "    # 并停止支持更早的 Pydantic 版本（预计还需很久）",
        ),
        (
            "# TODO: remove when dropping support for Pydantic < v2.12.3\n_Attrs = {",
            "# TODO：停止支持 Pydantic < v2.12.3 后移除\n_Attrs = {",
        ),
        (
            "# TODO: remove when dropping support for Pydantic < v2.12.3\ndef asdict(field_info: FieldInfo)",
            "# TODO：停止支持 Pydantic < v2.12.3 后移除\ndef asdict(field_info: FieldInfo)",
        ),
        (
            "@dataclass\nclass ModelField:",
            '@dataclass\nclass ModelField:\n    """封装 Pydantic FieldInfo 与 TypeAdapter，供验证与序列化使用。"""',
        ),
        (
            "            # Pydantic >= 2.12.0 warns about field specific metadata that is unused\n"
            "            # (e.g. `TypeAdapter(Annotated[int, Field(alias='b')])`). In some cases, we\n"
            "            # end up building the type adapter from a model field annotation so we\n"
            "            # need to ignore the warning:",
            "            # Pydantic >= 2.12.0 会对未使用的字段级元数据发出警告\n"
            "            #（例如 `TypeAdapter(Annotated[int, Field(alias='b')])`）。有时我们\n"
            "            # 从模型字段注解构建 TypeAdapter，因此需要忽略该警告：",
        ),
        (
            "            # TODO: remove after setting the min Pydantic to v2.12.3\n"
            "            # that adds asdict(), and use self.field_info.asdict() instead",
            "            # TODO：最低 Pydantic 版本升至 v2.12.3 后移除，改用 self.field_info.asdict()",
        ),
        (
            "                # this FieldInfo needs to be created again so that it doesn't include\n"
            "                # the old field info metadata and only the rest of the attributes",
            "                # 需重新创建 FieldInfo，避免携带旧元数据，仅保留其余属性",
        ),
        (
            "        # What calls this code passes a value that already called\n"
            "        # self._type_adapter.validate_python(value)\n"
            "        return self._type_adapter.dump_python(",
            "        # 调用方传入的值已通过 self._type_adapter.validate_python(value) 验证\n"
            "        return self._type_adapter.dump_python(",
        ),
        (
            "        # What calls this code passes a value that already called\n"
            "        # self._type_adapter.validate_python(value)\n"
            "        # This uses Pydantic's dump_json() which serializes directly to JSON\n"
            "        # bytes in one pass (via Rust), avoiding the intermediate Python dict\n"
            "        # step of dump_python(mode=\"json\") + json.dumps().",
            "        # 调用方传入的值已通过 self._type_adapter.validate_python(value) 验证\n"
            "        # 使用 Pydantic dump_json() 经 Rust 一步序列化为 JSON 字节，\n"
            "        # 避免 dump_python(mode=\"json\") + json.dumps() 的中间 dict 步骤",
        ),
        (
            "        # Each ModelField is unique for our purposes, to allow making a dict from\n"
            "        # ModelField to its JSON Schema.",
            "        # 每个 ModelField 实例唯一，便于建立 ModelField 到 JSON Schema 的映射",
        ),
        (
            "    # This expects that GenerateJsonSchema was already used to generate the definitions",
            "    # 假定 GenerateJsonSchema 已生成 definitions",
        ),
        (
            "        # TODO remove when deprecating Pydantic v1\n"
            "        # Ref: https://github.com/pydantic/pydantic/blob/d61792cc42c80b13b23e3ffa74bc37ec7c77f7d1/pydantic/schema.py#L207",
            "        # TODO：弃用 Pydantic v1 后移除\n"
            "        # 参考：https://github.com/pydantic/pydantic/blob/d61792cc42c80b13b23e3ffa74bc37ec7c77f7d1/pydantic/schema.py#L207",
        ),
        (
            "    # definitions: dict[DefsRef, dict[str, Any]]\n"
            "    # but mypy complains about general str in other places that are not declared as\n"
            "    # DefsRef, although DefsRef is just str:\n"
            "    # DefsRef = NewType('DefsRef', str)\n"
            "    # So, a cast to simplify the types here",
            "    # definitions: dict[DefsRef, dict[str, Any]]\n"
            "    # 但 mypy 在其他未声明为 DefsRef 的 str 处报错，而 DefsRef 本质是 str：\n"
            "    # DefsRef = NewType('DefsRef', str)\n"
            "    # 因此此处用 cast 简化类型",
        ),
        (
            "    if origin_type is Union or origin_type is UnionType:  # Handle optional sequences",
            "    if origin_type is Union or origin_type is UnionType:  # 处理可选序列类型",
        ),
        (
            "# Duplicate of several schema functions from Pydantic v1 to make them compatible with\n"
            "# Pydantic v2 and allow mixing the models",
            "# 复制自 Pydantic v1 的若干 schema 函数，以兼容 v2 并允许混用模型",
        ),
    ],
    "fastapi/openapi/models.py": [
        (
            "    assert email_validator  # make autoflake ignore the unused import",
            "    assert email_validator  # 使 autoflake 忽略未使用的导入",
        ),
        (
            "# Ref JSON Schema 2020-12: https://json-schema.org/draft/2020-12/json-schema-validation#name-type",
            "# 参考 JSON Schema 2020-12：https://json-schema.org/draft/2020-12/json-schema-validation#name-type",
        ),
        (
            "    # Ref: JSON Schema 2020-12: https://json-schema.org/draft/2020-12/json-schema-core.html#name-the-json-schema-core-vocabu\n"
            "    # Core Vocabulary",
            "    # 参考 JSON Schema 2020-12 核心词汇表\n"
            "    # Core Vocabulary",
        ),
        (
            "    # Ref: JSON Schema 2020-12: https://json-schema.org/draft/2020-12/json-schema-core.html#name-a-vocabulary-for-applying-s\n"
            "    # A Vocabulary for Applying Subschemas",
            "    # 参考 JSON Schema 2020-12 子 schema 应用词汇表\n"
            "    # A Vocabulary for Applying Subschemas",
        ),
        (
            "    # Ref: JSON Schema Validation 2020-12: https://json-schema.org/draft/2020-12/json-schema-validation.html#name-a-vocabulary-for-structural\n"
            "    # A Vocabulary for Structural Validation",
            "    # 参考 JSON Schema Validation 2020-12 结构验证词汇表\n"
            "    # A Vocabulary for Structural Validation",
        ),
        (
            "    # Ref: JSON Schema Validation 2020-12: https://json-schema.org/draft/2020-12/json-schema-validation.html#name-vocabularies-for-semantic-c\n"
            "    # Vocabularies for Semantic Content With \"format\"",
            "    # 参考 JSON Schema Validation 2020-12 语义内容（format）词汇表\n"
            "    # Vocabularies for Semantic Content With \"format\"",
        ),
        (
            "    # Ref: JSON Schema Validation 2020-12: https://json-schema.org/draft/2020-12/json-schema-validation.html#name-a-vocabulary-for-the-conten\n"
            "    # A Vocabulary for the Contents of String-Encoded Data",
            "    # 参考 JSON Schema Validation 2020-12 字符串编码内容词汇表\n"
            "    # A Vocabulary for the Contents of String-Encoded Data",
        ),
        (
            "    # Ref: JSON Schema Validation 2020-12: https://json-schema.org/draft/2020-12/json-schema-validation.html#name-a-vocabulary-for-basic-meta\n"
            "    # A Vocabulary for Basic Meta-Data Annotations",
            "    # 参考 JSON Schema Validation 2020-12 基础元数据注解词汇表\n"
            "    # A Vocabulary for Basic Meta-Data Annotations",
        ),
        (
            "    # Ref: OpenAPI 3.1.0: https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#schema-object\n"
            "    # Schema Object",
            "    # 参考 OpenAPI 3.1.0 Schema Object\n"
            "    # Schema Object",
        ),
        (
            "# Ref: https://json-schema.org/draft/2020-12/json-schema-core.html#name-json-schema-documents\n"
            "# A JSON Schema MUST be an object or a boolean.",
            "# 参考：https://json-schema.org/draft/2020-12/json-schema-core.html#name-json-schema-documents\n"
            "# JSON Schema 必须是对象或布尔值",
        ),
        (
            "    # Serialization rules for simple scenarios",
            "    # 简单场景的序列化规则",
        ),
        (
            "    # Serialization rules for more complex scenarios",
            "    # 复杂场景的序列化规则",
        ),
        (
            "    # Using Any for Specification Extensions",
            "    # 规范扩展字段使用 Any 类型",
        ),
    ],
    "fastapi/security/http.py": [
        (
            'class HTTPBasicCredentials(BaseModel):\n    """\n    The HTTP Basic credentials given as the result of using `HTTPBasic` in a\n    dependency.\n\n    Read more about it in the\n    [FastAPI docs for HTTP Basic Auth](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/).\n    """',
            'class HTTPBasicCredentials(BaseModel):\n    """\n    使用 `HTTPBasic` 作为依赖项时返回的 HTTP Basic 凭据。\n\n    详见\n    [FastAPI HTTP Basic 认证文档](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/)。\n    """',
        ),
        (
            'Doc("The HTTP Basic username.")',
            'Doc("HTTP Basic 用户名。")',
        ),
        (
            'Doc("The HTTP Basic password.")',
            'Doc("HTTP Basic 密码。")',
        ),
        (
            'class HTTPAuthorizationCredentials(BaseModel):\n    """\n    The HTTP authorization credentials in the result of using `HTTPBearer` or\n    `HTTPDigest` in a dependency.\n\n    The HTTP authorization header value is split by the first space.\n\n    The first part is the `scheme`, the second part is the `credentials`.\n\n    For example, in an HTTP Bearer token scheme, the client will send a header\n    like:\n\n    ```\n    Authorization: Bearer deadbeef12346\n    ```\n\n    In this case:\n\n    * `scheme` will have the value `"Bearer"`\n    * `credentials` will have the value `"deadbeef12346"`\n    """',
            'class HTTPAuthorizationCredentials(BaseModel):\n    """\n    使用 `HTTPBearer` 或 `HTTPDigest` 作为依赖项时返回的 HTTP 授权凭据。\n\n    Authorization 头按第一个空格拆分：\n\n    前半部分为 `scheme`，后半部分为 `credentials`。\n\n    例如 HTTP Bearer 令牌方案下，客户端发送：\n\n    ```\n    Authorization: Bearer deadbeef12346\n    ```\n\n    此时：\n\n    * `scheme` 为 `"Bearer"`\n    * `credentials` 为 `"deadbeef12346"`\n    """',
        ),
        (
            'Doc(\n            """\n            The HTTP authorization scheme extracted from the header value.\n            """\n        )',
            'Doc(\n            """\n            从 Authorization 头提取的认证方案名。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The HTTP authorization credentials extracted from the header value.\n            """\n        )',
            'Doc(\n            """\n            从 Authorization 头提取的凭据字符串。\n            """\n        )',
        ),
        (
            'class HTTPBasic(HTTPBase):\n    """\n    HTTP Basic authentication.\n\n    Ref: https://datatracker.ietf.org/doc/html/rfc7617\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be an `HTTPBasicCredentials` object containing the\n    `username` and the `password`.\n\n    Read more about it in the\n    [FastAPI docs for HTTP Basic Auth](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/).\n\n    ## Example\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPBasic, HTTPBasicCredentials\n\n    app = FastAPI()\n\n    security = HTTPBasic()\n\n\n    @app.get("/users/me")\n    def read_current_user(credentials: Annotated[HTTPBasicCredentials, Depends(security)]):\n        return {"username": credentials.username, "password": credentials.password}\n    ```\n    """',
            'class HTTPBasic(HTTPBase):\n    """\n    HTTP Basic 认证。\n\n    参考：https://datatracker.ietf.org/doc/html/rfc7617\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 `username` 与 `password` 的 `HTTPBasicCredentials` 对象。\n\n    详见\n    [FastAPI HTTP Basic 认证文档](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/)。\n\n    ## 示例\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPBasic, HTTPBasicCredentials\n\n    app = FastAPI()\n\n    security = HTTPBasic()\n\n\n    @app.get("/users/me")\n    def read_current_user(credentials: Annotated[HTTPBasicCredentials, Depends(security)]):\n        return {"username": credentials.username, "password": credentials.password}\n    ```\n    """',
        ),
        (
            'Doc(\n                """\n                Security scheme name.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案名称。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                HTTP Basic authentication realm.\n                """\n            )',
            'Doc(\n                """\n                HTTP Basic 认证的 realm 域。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                Security scheme description.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案描述。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                By default, if the HTTP Basic authentication is not provided (a\n                header), `HTTPBasic` will automatically cancel the request and send the\n                client an error.\n\n                If `auto_error` is set to `False`, when the HTTP Basic authentication\n                is not available, instead of erroring out, the dependency result will\n                be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in HTTP Basic\n                authentication or in an HTTP Bearer token).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供 HTTP Basic 认证（Authorization 头），\n                `HTTPBasic` 将自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当 Basic 认证不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如 HTTP Basic 或 Bearer 令牌）。\n                """\n            )',
        ),
        (
            'class HTTPBearer(HTTPBase):\n    """\n    HTTP Bearer token authentication.\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be an `HTTPAuthorizationCredentials` object containing\n    the `scheme` and the `credentials`.\n\n    ## Example\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer\n\n    app = FastAPI()\n\n    security = HTTPBearer()\n\n\n    @app.get("/users/me")\n    def read_current_user(\n        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]\n    ):\n        return {"scheme": credentials.scheme, "credentials": credentials.credentials}\n    ```\n    """',
            'class HTTPBearer(HTTPBase):\n    """\n    HTTP Bearer 令牌认证。\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 `scheme` 与 `credentials` 的 `HTTPAuthorizationCredentials` 对象。\n\n    ## 示例\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer\n\n    app = FastAPI()\n\n    security = HTTPBearer()\n\n\n    @app.get("/users/me")\n    def read_current_user(\n        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]\n    ):\n        return {"scheme": credentials.scheme, "credentials": credentials.credentials}\n    ```\n    """',
        ),
        (
            'Doc("Bearer token format.")',
            'Doc("Bearer 令牌格式说明。")',
        ),
        (
            'Doc(\n                """\n                By default, if the HTTP Bearer token is not provided (in an\n                `Authorization` header), `HTTPBearer` will automatically cancel the\n                request and send the client an error.\n\n                If `auto_error` is set to `False`, when the HTTP Bearer token\n                is not available, instead of erroring out, the dependency result will\n                be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in an HTTP\n                Bearer token or in a cookie).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未在 `Authorization` 头中提供 Bearer 令牌，\n                `HTTPBearer` 将自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当 Bearer 令牌不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如 Bearer 令牌或 Cookie）。\n                """\n            )',
        ),
        (
            'class HTTPDigest(HTTPBase):\n    """\n    HTTP Digest authentication.\n\n    **Warning**: this is only a stub to connect the components with OpenAPI in FastAPI,\n    but it doesn\'t implement the full Digest scheme, you would need to subclass it\n    and implement it in your code.\n\n    Ref: https://datatracker.ietf.org/doc/html/rfc7616\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be an `HTTPAuthorizationCredentials` object containing\n    the `scheme` and the `credentials`.\n\n    ## Example\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPAuthorizationCredentials, HTTPDigest\n\n    app = FastAPI()\n\n    security = HTTPDigest()\n\n\n    @app.get("/users/me")\n    def read_current_user(\n        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]\n    ):\n        return {"scheme": credentials.scheme, "credentials": credentials.credentials}\n    ```\n    """',
            'class HTTPDigest(HTTPBase):\n    """\n    HTTP Digest 认证。\n\n    **警告**：这仅是用于在 FastAPI 中将组件与 OpenAPI 关联的桩实现，\n    并未实现完整的 Digest 方案，需在代码中子类化并实现。\n\n    参考：https://datatracker.ietf.org/doc/html/rfc7616\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 `scheme` 与 `credentials` 的 `HTTPAuthorizationCredentials` 对象。\n\n    ## 示例\n\n    ```python\n    from typing import Annotated\n\n    from fastapi import Depends, FastAPI\n    from fastapi.security import HTTPAuthorizationCredentials, HTTPDigest\n\n    app = FastAPI()\n\n    security = HTTPDigest()\n\n\n    @app.get("/users/me")\n    def read_current_user(\n        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]\n    ):\n        return {"scheme": credentials.scheme, "credentials": credentials.credentials}\n    ```\n    """',
        ),
        (
            'Doc(\n                """\n                By default, if the HTTP Digest is not provided, `HTTPDigest` will\n                automatically cancel the request and send the client an error.\n\n                If `auto_error` is set to `False`, when the HTTP Digest is not\n                available, instead of erroring out, the dependency result will\n                be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in HTTP\n                Digest or in a cookie).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供 HTTP Digest 认证，`HTTPDigest` 将\n                自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当 Digest 认证不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如 HTTP Digest 或 Cookie）。\n                """\n            )',
        ),
    ],
    "fastapi/security/utils.py": [
        (
            "def get_authorization_scheme_param(\n    authorization_header_value: str | None,\n) -> tuple[str, str]:",
            'def get_authorization_scheme_param(\n    authorization_header_value: str | None,\n) -> tuple[str, str]:\n    """\n    解析 Authorization 头，返回 `(scheme, credentials)` 元组。\n\n    按第一个空格拆分；若无头或为空，返回 `("", "")`。\n    """',
        ),
    ],
    "fastapi/sse.py": [
        (
            "# Canonical SSE event schema matching the OpenAPI 3.2 spec\n"
            "# (Section 4.14.4 \"Special Considerations for Server-Sent Events\")",
            "# 符合 OpenAPI 3.2 规范的 SSE 事件 schema\n"
            '#（第 4.14.4 节 "Special Considerations for Server-Sent Events"）',
        ),
        (
            'class EventSourceResponse(StreamingResponse):\n    """Streaming response with `text/event-stream` media type.\n\n    Use as `response_class=EventSourceResponse` on a *path operation* that uses `yield`\n    to enable Server Sent Events (SSE) responses.\n\n    Works with **any HTTP method** (`GET`, `POST`, etc.), which makes it compatible\n    with protocols like MCP that stream SSE over `POST`.\n\n    The actual encoding logic lives in the FastAPI routing layer. This class\n    serves mainly as a marker and sets the correct `Content-Type`.\n    """',
            'class EventSourceResponse(StreamingResponse):\n    """`text/event-stream` 媒体类型的流式响应。\n\n    在带 `yield` 的路径操作上设置 `response_class=EventSourceResponse`\n    以启用 Server-Sent Events (SSE) 响应。\n\n    支持**任意 HTTP 方法**（`GET`、`POST` 等），兼容 MCP 等通过 `POST` 流式传输 SSE 的协议。\n\n    实际编码逻辑在 FastAPI 路由层；本类主要作为标记并设置正确的 `Content-Type`。\n    """',
        ),
        (
            'class ServerSentEvent(BaseModel):\n    """Represents a single Server-Sent Event.\n\n    When `yield`ed from a *path operation function* that uses\n    `response_class=EventSourceResponse`, each `ServerSentEvent` is encoded\n    into the [SSE wire format](https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream)\n    (`text/event-stream`).\n\n    If you yield a plain object (dict, Pydantic model, etc.) instead, it is\n    automatically JSON-encoded and sent as the `data:` field.\n\n    All `data` values **including plain strings** are JSON-serialized.\n\n    For example, `data="hello"` produces `data: "hello"` on the wire (with\n    quotes).\n    """',
            'class ServerSentEvent(BaseModel):\n    """表示单个 Server-Sent Event。\n\n    在使用 `response_class=EventSourceResponse` 的路径操作函数中 `yield` 时，\n    每个 `ServerSentEvent` 会编码为\n    [SSE 线路格式](https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream)\n    （`text/event-stream`）。\n\n    若 yield 普通对象（dict、Pydantic 模型等），会自动 JSON 编码并作为 `data:` 字段发送。\n\n    所有 `data` 值**包括普通字符串**都会 JSON 序列化。\n\n    例如 `data="hello"` 在线路上产生 `data: "hello"`（带引号）。\n    """',
        ),
        (
            'Doc(\n            """\n            The event payload.\n\n            Can be any JSON-serializable value: a Pydantic model, dict, list,\n            string, number, etc. It is **always** serialized to JSON: strings\n            are quoted (`"hello"` becomes `data: "hello"` on the wire).\n\n            Mutually exclusive with `raw_data`.\n            """\n        )',
            'Doc(\n            """\n            事件负载。\n\n            可为任意 JSON 可序列化值：Pydantic 模型、dict、list、字符串、数字等。\n            **始终**序列化为 JSON：字符串会加引号（`"hello"` 在线路上为 `data: "hello"`）。\n\n            与 `raw_data` 互斥。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Raw string to send as the `data:` field **without** JSON encoding.\n\n            Use this when you need to send pre-formatted text, HTML fragments,\n            CSV lines, or any non-JSON payload. The string is placed directly\n            into the `data:` field as-is.\n\n            Mutually exclusive with `data`.\n            """\n        )',
            'Doc(\n            """\n            作为 `data:` 字段发送的原始字符串，**不**经 JSON 编码。\n\n            适用于预格式化文本、HTML 片段、CSV 行等非 JSON 负载。\n            字符串原样写入 `data:` 字段。\n\n            与 `data` 互斥。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional event type name.\n\n            Maps to `addEventListener(event, ...)` on the browser. When omitted,\n            the browser dispatches on the generic `message` event. Must be a\n            single line.\n            """\n        )',
            'Doc(\n            """\n            可选事件类型名。\n\n            对应浏览器 `addEventListener(event, ...)`。省略时使用通用 `message` 事件。\n            必须为单行。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional event ID.\n\n            The browser sends this value back as the `Last-Event-ID` header on\n            automatic reconnection. **Must be a single line** and must not contain\n            null (`\\\\0`) characters.\n            """\n        )',
            'Doc(\n            """\n            可选事件 ID。\n\n            浏览器自动重连时会将其作为 `Last-Event-ID` 头发回。\n            **必须为单行**且不得包含空字符（`\\\\0`）。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional reconnection time in **milliseconds**.\n\n            Tells the browser how long to wait before reconnecting after the\n            connection is lost. Must be a non-negative integer.\n            """\n        )',
            'Doc(\n            """\n            可选重连等待时间，单位为**毫秒**。\n\n            告知浏览器连接断开后等待多久再重连。须为非负整数。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional comment line(s).\n\n            Comment lines start with `:` in the SSE wire format and are ignored by\n            `EventSource` clients. Useful for keep-alive pings to prevent\n            proxy/load-balancer timeouts.\n            """\n        )',
            'Doc(\n            """\n            可选注释行。\n\n            SSE 线路格式中以 `:` 开头的注释行会被 `EventSource` 客户端忽略。\n            可用于 keep-alive 心跳，防止代理/负载均衡超时。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Pre-serialized data string to use as the `data:` field.\n            """\n        )',
            'Doc(\n            """\n            用作 `data:` 字段的预序列化数据字符串。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional event type name (`event:` field).\n            """\n        )',
            'Doc(\n            """\n            可选事件类型名（`event:` 字段）。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional event ID (`id:` field).\n            """\n        )',
            'Doc(\n            """\n            可选事件 ID（`id:` 字段）。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional reconnection time in milliseconds (`retry:` field).\n            """\n        )',
            'Doc(\n            """\n            可选重连时间，毫秒（`retry:` 字段）。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Optional comment line(s) (`:` prefix).\n            """\n        )',
            'Doc(\n            """\n            可选注释行（`:` 前缀）。\n            """\n        )',
        ),
        (
            '    """Build SSE wire-format bytes from **pre-serialized** data.\n\n    The result always ends with `\\\\n\\\\n` (the event terminator).\n    """',
            '    """从**预序列化**数据构建 SSE 线路格式字节。\n\n    结果始终以 `\\\\n\\\\n`（事件终止符）结尾。\n    """',
        ),
        (
            "    # Split on SSE-spec line terminators only (\\n, \\r\\n, \\r), preserving\n    # trailing empty strings.",
            "    # 仅按 SSE 规范行终止符（\\n、\\r\\n、\\r）拆分，保留末尾空字符串",
        ),
        (
            "# Keep-alive comment, per the SSE spec recommendation",
            "# SSE 规范建议的 keep-alive 注释",
        ),
        (
            "# Seconds between keep-alive pings when a generator is idle.\n# Private but importable so tests can monkeypatch it.",
            "# 生成器空闲时 keep-alive 心跳间隔（秒）。\n# 私有但可导入，供测试 monkeypatch",
        ),
    ],
    "fastapi/utils.py": [
        (
            "    # Ref: https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#patterned-fields-1",
            "    # 参考：https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#patterned-fields-1",
        ),
        (
            '    """\n    Pass items or `DefaultPlaceholder`s by descending priority.\n\n    The first one to _not_ be a `DefaultPlaceholder` will be returned.\n\n    Otherwise, the first item (a `DefaultPlaceholder`) will be returned.\n    """',
            '    """\n    按优先级从高到低传入项或 `DefaultPlaceholder`。\n\n    返回第一个**不是** `DefaultPlaceholder` 的项。\n\n    若全部为 `DefaultPlaceholder`，则返回第一项。\n    """',
        ),
    ],
    "docs_src/additional_responses/tutorial003_py310.py": [
        (
            '        404: {"model": Message, "description": "The item was not found"},',
            '        404: {"model": Message, "description": "未找到该条目"},',
        ),
        (
            '            "description": "Item requested by ID",',
            '            "description": "按 ID 请求的条目",',
        ),
    ],
    "docs_src/additional_responses/tutorial002_py310.py": [
        (
            '            "description": "Return the JSON item or an image.",',
            '            "description": "返回 JSON 条目或图片。",',
        ),
    ],
}

# HTTPBearer/HTTPDigest share security scheme Doc blocks with HTTPBasic
for _rel in ("fastapi/security/http.py",):
    for old, new in [
        (
            'Doc(\n                """\n                Security scheme name.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案名称。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                Security scheme description.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案描述。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
    ]:
        pairs = FILE_REPLACEMENTS.setdefault(_rel, [])
        if (old, new) not in pairs:
            pairs.append((old, new))


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if rel in PREPEND and not text.startswith('"""'):
        text = PREPEND[rel] + text
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old == new:
            continue
        if old in text:
            text = text.replace(old, new)
        elif has_chinese(text):
            continue
        else:
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
