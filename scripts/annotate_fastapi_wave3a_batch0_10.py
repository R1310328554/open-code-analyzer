#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-3a slice [0:10]."""
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
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:10]

PREPEND: dict[str, str] = {
    "fastapi/dependencies/utils.py": (
        '"""FastAPI 依赖注入运行时工具：参数解析、依赖求解与请求体验证。"""\n\n'
    ),
    "fastapi/openapi/utils.py": '"""OpenAPI 规范生成工具：路径、操作、参数与安全方案。"""\n\n',
    "fastapi/param_functions.py": (
        '"""参数声明辅助函数：Path、Query、Header、Body、Depends、Security 等。"""\n\n'
    ),
    "fastapi/params.py": '"""FastAPI 参数类型定义：Param、Body、Form、Depends、Security 等。"""\n\n',
    "fastapi/security/oauth2.py": '"""OAuth2 安全方案：密码流、Bearer 令牌与作用域。"""\n\n',
    "docs_src/app_testing/app_b_py310/__init__.py": (
        '"""带请求头认证与 CRUD 的 FastAPI 应用及测试示例（app_b，传统 Header 语法）。"""\n'
    ),
}

# (old, new, replace_all) — applied within listed files when replace_all is True
GLOBAL_IN_FILES: dict[str, list[tuple[str, str, bool]]] = {
    "fastapi/param_functions.py": [
        (
            """            Default value if the parameter field is not set.
            """,
            """            参数未设置时的默认值。
            """,
            True,
        ),
        (
            """            Default value if the parameter field is not set.

            This doesn't affect `Path` parameters as the value is always required.
            The parameter is available only for compatibility.
            """,
            """            参数未设置时的默认值。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """,
            True,
        ),
        (
            """            A callable to generate the default value.

            This doesn't affect `Path` parameters as the value is always required.
            The parameter is available only for compatibility.
            """,
            """            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """,
            True,
        ),
        (
            """            An alternative name for the parameter field.

            This will be used to extract the data and for the generated OpenAPI.
            It is particularly useful when you can't use the name you want because it
            is a Python reserved keyword or similar.
            """,
            """            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """,
            True,
        ),
        (
            """            Priority of the alias. This affects whether an alias generator is used.
            """,
            """            别名优先级，影响是否使用别名生成器。
            """,
            True,
        ),
        (
            """            'Whitelist' validation step. The parameter field will be the single one
            allowed by the alias or set of aliases defined.
            """,
            """            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """,
            True,
        ),
        (
            """            'Blacklist' validation step. The vanilla parameter field will be the
            single one of the alias' or set of aliases' fields and all the other
            fields will be ignored at serialization time.
            """,
            """            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """,
            True,
        ),
        (
            """            Human-readable description.
            """,
            """            人类可读的描述。
            """,
            True,
        ),
        (
            """            Minimum length for strings.
            """,
            """            字符串最小长度。
            """,
            True,
        ),
        (
            """            Maximum length for strings.
            """,
            """            字符串最大长度。
            """,
            True,
        ),
        (
            """            RegEx pattern for strings.
            """,
            """            字符串的正则表达式模式。
            """,
            True,
        ),
        (
            """            Parameter field name for discriminating the type in a tagged union.
            """,
            """            标记联合类型中用于区分类型的参数字段名。
            """,
            True,
        ),
        (
            """            If `True`, strict validation is applied to the field.
            """,
            """            为 `True` 时对该字段启用严格验证。
            """,
            True,
        ),
        (
            """            Value must be a multiple of this. Only applicable to numbers.
            """,
            """            值必须是该数的倍数，仅适用于数值。
            """,
            True,
        ),
        (
            """            Allow `inf`, `-inf`, `nan`. Only applicable to numbers.
            """,
            """            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """,
            True,
        ),
        (
            """            Maximum number of digits allowed for decimal values.
            """,
            """            小数值允许的最大位数。
            """,
            True,
        ),
        (
            """            Maximum number of decimal places allowed for decimal values.
            """,
            """            小数值允许的最大小数位数。
            """,
            True,
        ),
        (
            """            Any additional JSON schema data.
            """,
            """            附加的 JSON Schema 数据。
            """,
            True,
        ),
        (
            """            Include extra fields used by the JSON Schema.
            """,
            """            包含 JSON Schema 使用的额外字段。
            """,
            True,
        ),
        (
            """            The media type of this parameter field. Changing it would affect the
            generated OpenAPI, but currently it doesn't affect the parsing of the data.
            """,
            """            该参数字段的媒体类型。修改会影响生成的 OpenAPI，但目前不影响数据解析。
            """,
            True,
        ),
        (
            """            Don't call it directly, FastAPI will call it for you, just pass the object
            directly.

            Read more about it in the
            [FastAPI docs for Dependencies](https://fastapi.tiangolo.com/tutorial/dependencies/)
            """,
            """            不要直接调用，FastAPI 会自动调用，只需传入对象本身。

            详见
            [FastAPI 依赖项文档](https://fastapi.tiangolo.com/tutorial/dependencies/)
            """,
            True,
        ),
        (
            """            By default, after a dependency is called the first time in a request, if
            the dependency is declared again for the rest of the request (for example
            if the dependency is needed by several dependencies), the value will be
            re-used for the rest of the request.

            Set `use_cache` to `False` to disable this behavior and ensure the
            dependency is called again (if declared more than once) in the same request.

            Read more about it in the
            [FastAPI docs about sub-dependencies](https://fastapi.tiangolo.com/tutorial/dependencies/sub-dependencies/#using-the-same-dependency-multiple-times)
            """,
            """            默认同一次请求中依赖首次调用后，若后续再次声明同一依赖，将复用已求值结果。

            将 `use_cache` 设为 `False` 可禁用该行为，确保同一请求内多次声明时重新调用。

            详见
            [FastAPI 子依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/sub-dependencies/#using-the-same-dependency-multiple-times)
            """,
            True,
        ),
    ],
    "fastapi/security/oauth2.py": [
        (
            """                Security scheme name.

                It will be included in the generated OpenAPI (e.g. visible at `/docs`).
                """,
            """                安全方案名称。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """,
            True,
        ),
        (
            """                Security scheme description.

                It will be included in the generated OpenAPI (e.g. visible at `/docs`).
                """,
            """                安全方案描述。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """,
            True,
        ),
        (
            """                By default, if no HTTP Authorization header is provided, required for
                OAuth2 authentication, it will automatically cancel the request and
                send the client an error.

                If `auto_error` is set to `False`, when the HTTP Authorization header
                is not available, instead of erroring out, the dependency result will
                be `None`.

                This is useful when you want to have optional authentication.

                It is also useful when you want to have authentication that can be
                provided in one of multiple optional ways (for example, with OAuth2
                or in a cookie).
                """,
            """                默认情况下，若未提供 OAuth2 认证所需的 HTTP Authorization 头，
                将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景，也适用于多种可选认证方式之一（例如 OAuth2 或 Cookie）。
                """,
            True,
        ),
        (
            """                The URL to refresh the token and obtain a new one.
                """,
            """                刷新令牌并获取新令牌的 URL。
                """,
            True,
        ),
    ],
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "fastapi/dependencies/utils.py": [
        (
            "        # Import an attribute that can be mocked/deleted in testing",
            "        # 导入可在测试中 mock/删除的属性",
        ),
        (
            "            # __version__ is available in both multiparts, and can be mocked",
            "            # __version__ 在两个 multipart 包中均存在，可被 mock",
        ),
        (
            "                # parse_options_header is only available in the right multipart",
            "                # parse_options_header 仅在正确的 multipart 包中可用",
        ),
        (
            "        # Handle type annotations with if TYPE_CHECKING, not used by FastAPI\n"
            "        # e.g. dependency return types",
            "        # 处理 `if TYPE_CHECKING` 下的类型注解（FastAPI 不使用），例如依赖返回类型",
        ),
        (
            "        # unpack in case PEP 695 type syntax is used",
            "        # 若使用 PEP 695 类型语法则解包",
        ),
        (
            "    # Extract Annotated info",
            "    # 从 Annotated 提取元信息",
        ),
        (
            "        # Set default for Annotated FieldInfo",
            "        # 为 Annotated FieldInfo 设置默认值",
        ),
        (
            "            # Copy `field_info` because we mutate `field_info.default` below.",
            "            # 复制 field_info，因下方会修改 field_info.default",
        ),
        (
            "        # Get Annotated Depends",
            "        # 从 Annotated 获取 Depends",
        ),
        (
            "    # Get Depends from default value",
            "    # 从默认值获取 Depends",
        ),
        (
            "    # Get FieldInfo from default value",
            "    # 从默认值获取 FieldInfo",
        ),
        (
            "    # Get Depends from type annotation",
            "    # 从类型注解获取 Depends",
        ),
        (
            "        # Copy `depends` before mutating it",
            "        # 修改前先复制 depends",
        ),
        (
            "    # Handle non-param type annotations like Request\n"
            "    # Only apply special handling when there's no explicit Depends - if there's a Depends,\n"
            "    # the dependency will be called and its return value used instead of the special injection",
            "    # 处理 Request 等非参数类型注解\n"
            "    # 仅在没有显式 Depends 时做特殊注入；若有 Depends 则调用依赖并使用其返回值",
        ),
        (
            "    # Handle default assignations, neither field_info nor depends was not found in Annotated nor default value",
            "    # 处理默认推断：Annotated 与默认值中均未找到 field_info 或 depends",
        ),
        (
            "            # We might check here that `default_value is RequiredParam`, but the fact is that the same\n"
            "            # parameter might sometimes be a path parameter and sometimes not. See\n"
            "            # `tests/test_infer_param_optionality.py` for an example.",
            "            # 同一参数有时为路径参数有时不是，见 tests/test_infer_param_optionality.py",
        ),
        (
            "    # It's a field_info, not a dependency",
            "    # 这是 field_info，不是依赖",
        ),
        (
            "        # Handle field_info.in_",
            "        # 处理 field_info.in_",
        ),
        (
            "@dataclass\nclass ParamDetails:",
            '@dataclass\nclass ParamDetails:\n    """analyze_param 的返回值：类型注解、Depends 与 ModelField。"""',
        ),
        (
            "@dataclass\nclass SolvedDependency:",
            '@dataclass\nclass SolvedDependency:\n    """solve_dependencies 的求解结果。"""',
        ),
        (
            "    # TODO: remove this parameter later, no longer used, not removing it yet as some\n"
            "    # people might be monkey patching this function (although that's not supported)",
            "    # TODO：此参数已不再使用，稍后移除；暂保留因有人可能 monkey patch（虽不支持）",
        ),
        (
            '    """Check if field type is a Union where all members are BaseModel subclasses."""',
            '    """判断字段类型是否为成员均为 BaseModel 子类的 Union。"""',
        ),
        (
            "    # Check if it's a Union type (covers both typing.Union and types.UnionType in Python 3.10+)",
            "    # 判断是否为 Union（含 typing.Union 与 Python 3.10+ 的 types.UnionType）",
        ),
        (
            "    # More than one dependency could have the same field, it would show up as multiple\n"
            "    # fields but it's the same one, so count them by name",
            "    # 多个依赖可能共享同一字段，按名称去重计数",
        ),
        (
            "    # A top level field has to be a single field, not multiple",
            "    # 顶层 body 字段必须为单个字段",
        ),
        (
            "    # If it explicitly specifies it is embedded, it has to be embedded",
            "    # 若显式指定 embed，则必须嵌入",
        ),
        (
            "    # If it's a Form (or File) field, it has to be a BaseModel (or a union of BaseModels) to be top level\n"
            "    # otherwise it has to be embedded, so that the key value pair can be extracted",
            "    # Form/File 字段须为 BaseModel（或其 Union）才能作为顶层，否则须 embed 以便提取键值对",
        ),
        (
            "            # For types",
            "            # 处理字节序列类型",
        ),
        (
            "            # If the received body is a list, not a dict",
            "            # 若收到的 body 为 list 而非 dict",
        ),
        (
            "        # If headers are in a Pydantic model, the way to disable convert_underscores\n"
            "        # would be with Header(convert_underscores=False) at the Pydantic model level",
            "        # 若 header 在 Pydantic 模型中，可在模型级 Header(convert_underscores=False) 禁用下划线转换",
        ),
        (
            "            # Handle fields extracted from a Pydantic Model for a header, each field\n"
            "            # doesn't have a FieldInfo of type Header with the default convert_underscores=True",
            "            # 从 Pydantic 模型提取的 header 字段可能没有 Header FieldInfo 的 convert_underscores",
        ),
        (
            "        # For headers with convert_underscores=True, mark both the converted\n"
            "        # header name and the original field alias as processed to avoid\n"
            "        # accepting the original alias as an extra header.",
            "        # convert_underscores=True 时，将转换后的 header 名与原别名均标记为已处理",
        ),
        (
            '    """\n    Get a ModelField representing the request body for a path operation, combining\n'
            "    all body parameters into a single field if necessary.\n\n"
            "    Used to check if it's form data (with `isinstance(body_field, params.Form)`)\n"
            "    or JSON and to generate the JSON Schema for a request body.\n\n"
            "    This is **not** used to validate/parse the request body, that's done with each\n"
            "    individual body parameter.\n    \"\"\"",
            '    """\n    获取表示路径操作请求体的 ModelField，必要时合并所有 body 参数。\n\n'
            "    用于判断是否为 form data（`isinstance(body_field, params.Form)`）\n"
            "    或 JSON，并生成请求体的 JSON Schema。\n\n"
            "    **不**用于验证/解析请求体，该工作由各个 body 参数分别完成。\n    \"\"\"",
        ),
    ],
    "fastapi/openapi/utils.py": [
        (
            "@dataclass\nclass _OpenAPIDependencyData:",
            '@dataclass\nclass _OpenAPIDependencyData:\n    """路径操作的 OpenAPI 依赖参数汇总。"""',
        ),
        (
            "    # Use a dict to merge scopes for same security scheme",
            "    # 用字典合并同一安全方案的作用域",
        ),
        (
            "        # Merge scopes for the same security scheme",
            "        # 合并同一安全方案的作用域",
        ),
        (
            "            # field_info = cast(Param, field_info)",
            "            # field_info = cast(Param, field_info)",
        ),
        (
            "                # Make sure required definitions of the same parameter take precedence\n"
            "                # over non-required definitions",
            "                # 同一参数的 required 定义优先于非 required 定义",
        ),
        (
            "                # It would probably make more sense for all response classes to have an\n"
            "                # explicit default status_code, and to extract it from them, instead of\n"
            "                # doing this inspection tricks, that would probably be in the future\n"
            "                # TODO: probably make status_code a default class attribute for all\n"
            "                # responses in Starlette",
            "                # 更合理的做法是为所有响应类提供显式 default status_code\n"
            "                # TODO：考虑在 Starlette 中为所有响应类添加 status_code 类属性",
        ),
        (
            "                # Check for JSONL streaming (generator endpoints)",
            "                # 检查 JSONL 流式响应（生成器端点）",
        ),
    ],
    "fastapi/param_functions.py": [
        (
            """            Human-readable title.

            Read more about it in the
            [FastAPI docs for Path Parameters and Numeric Validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#declare-metadata)
            """,
            """            人类可读的标题。

            详见
            [FastAPI 路径参数与数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#declare-metadata)
            """,
        ),
        (
            """            Greater than. If set, value must be greater than this. Only applicable to
            numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
            """            大于：若设置，值必须大于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
        ),
        (
            """            Greater than or equal. If set, value must be greater than or equal to
            this. Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
            """            大于等于：若设置，值必须大于等于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
        ),
        (
            """            Less than. If set, value must be less than this. Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
            """            小于：若设置，值必须小于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
        ),
        (
            """            Less than or equal. If set, value must be less than or equal to this.
            Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
            """            小于等于：若设置，值必须小于等于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """,
        ),
        (
            """            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """,
            """            该字段的示例值。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """,
        ),
        (
            """            OpenAPI-specific examples.

            It will be added to the generated OpenAPI (e.g. visible at `/docs`).

            Swagger UI (that provides the `/docs` interface) has better support for the
            OpenAPI-specific examples than the JSON Schema `examples`, that's the main
            use case for this.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """,
            """            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """,
            True,
        ),
        (
            """            Mark this parameter field as deprecated.

            It will affect the generated OpenAPI (e.g. visible at `/docs`).
            """,
            """            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """,
            True,
        ),
        (
            """            To include (or not) this parameter field in the generated OpenAPI.
            You probably don't need it, but it's available.

            This affects the generated OpenAPI (e.g. visible at `/docs`).
            """,
            """            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """,
            True,
        ),
        (
            """            Default value if the parameter field is not set.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alternative-old-query-as-the-default-value)
            """,
            """            参数未设置时的默认值。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alternative-old-query-as-the-default-value)
            """,
        ),
        (
            """            An alternative name for the parameter field.

            This will be used to extract the data and for the generated OpenAPI.
            It is particularly useful when you can't use the name you want because it
            is a Python reserved keyword or similar.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alias-parameters)
            """,
            """            参数字段的别名，用于提取数据及生成 OpenAPI。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alias-parameters)
            """,
        ),
        (
            """            Human-readable title.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#declare-more-metadata)
            """,
            """            人类可读的标题。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#declare-more-metadata)
            """,
        ),
        (
            """            Greater than. If set, value must be greater than this. Only applicable to
            numbers.
            """,
            """            大于：若设置，值必须大于此值，仅适用于数值。
            """,
            True,
        ),
        (
            """            Greater than or equal. If set, value must be greater than or equal to
            this. Only applicable to numbers.
            """,
            """            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """,
            True,
        ),
        (
            """            Less than. If set, value must be less than this. Only applicable to numbers.
            """,
            """            小于：若设置，值必须小于此值，仅适用于数值。
            """,
            True,
        ),
        (
            """            Less than or equal. If set, value must be less than or equal to this.
            Only applicable to numbers.
            """,
            """            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """,
            True,
        ),
        (
            """            Automatically convert underscores to hyphens in the parameter field name.

            Read more about it in the
            [FastAPI docs for Header Parameters](https://fastapi.tiangolo.com/tutorial/header-params/#automatic-conversion)
            """,
            """            自动将参数字段名中的下划线转换为连字符。

            详见
            [FastAPI Header 参数文档](https://fastapi.tiangolo.com/tutorial/header-params/#automatic-conversion)
            """,
        ),
        (
            """            When `embed` is `True`, the parameter will be expected in a JSON body as a
            key instead of being the JSON body itself.

            This happens automatically when more than one `Body` parameter is declared.

            Read more about it in the
            [FastAPI docs for Body - Multiple Parameters](https://fastapi.tiangolo.com/tutorial/body-multiple-params/#embed-a-single-body-parameter).
            """,
            """            当 `embed` 为 `True` 时，参数作为 JSON body 中的键而非 body 本身。

            声明多个 `Body` 参数时会自动 embed。

            详见
            [FastAPI 多 Body 参数文档](https://fastapi.tiangolo.com/tutorial/body-multiple-params/#embed-a-single-body-parameter).
            """,
        ),
        (
            """            Human-readable title.
            """,
            """            人类可读的标题。
            """,
            True,
        ),
        (
            """    Declare a path parameter for a *path operation*.

    Read more about it in the
    [FastAPI docs for Path Parameters and Numeric Validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/).""",
            """    为 *path operation* 声明路径参数。

    详见
    [FastAPI 路径参数与数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/).""",
        ),
        (
            """    Declare a FastAPI dependency.

    It takes a single "dependable" callable (like a function).

    Don't call it directly, FastAPI will call it for you.

    Read more about it in the
    [FastAPI docs for Dependencies](https://fastapi.tiangolo.com/tutorial/dependencies/).""",
            """    声明 FastAPI 依赖项。

    接受单个可调用“dependable”（如函数）；不要直接调用，FastAPI 会自动调用。

    详见
    [FastAPI 依赖项文档](https://fastapi.tiangolo.com/tutorial/dependencies/).""",
        ),
        (
            """    Declare a FastAPI Security dependency.

    The only difference with a regular dependency is that it can declare OAuth2
    scopes that will be integrated with OpenAPI and the automatic UI docs (by default
    at `/docs`).

    It takes a single "dependable" callable (like a function).

    Don't call it directly, FastAPI will call it for you.

    Read more about it in the
    [FastAPI docs for Security](https://fastapi.tiangolo.com/tutorial/security/) and
    in the
    [FastAPI docs for OAuth2 scopes](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).""",
            """    声明 FastAPI Security 依赖项。

    与普通依赖的唯一区别是可声明 OAuth2 作用域，并集成到 OpenAPI 与 `/docs` UI。

    接受单个可调用“dependable”；不要直接调用，FastAPI 会自动调用。

    详见
    [FastAPI 安全文档](https://fastapi.tiangolo.com/tutorial/security/) 与
    [OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).""",
        ),
        (
            """            OAuth2 scopes required for the *path operation* that uses this Security
            dependency.

            The term "scope" comes from the OAuth2 specification, it seems to be
            intentionally vague and interpretable. It normally refers to permissions,
            in cases to roles.

            These scopes are integrated with OpenAPI (and the API docs at `/docs`).
            So they are visible in the OpenAPI specification.

            Read more about it in the
            [FastAPI docs about OAuth2 scopes](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/)
            """,
            """            使用该 Security 依赖的 *path operation* 所需的 OAuth2 作用域。

            “scope” 来自 OAuth2 规范，通常表示权限或角色，并集成到 OpenAPI 与 `/docs`。

            详见
            [FastAPI OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/)
            """,
        ),
        (
            """            Mainly for dependencies with `yield`, define when the dependency function
            should start (the code before `yield`) and when it should end (the code
            after `yield`).

            * `"function"`: start the dependency before the *path operation function*
                that handles the request, end the dependency after the *path operation
                function* ends, but **before** the response is sent back to the client.
                So, the dependency function will be executed **around** the *path operation
                **function***.
            * `"request"`: start the dependency before the *path operation function*
                that handles the request (similar to when using `"function"`), but end
                **after** the response is sent back to the client. So, the dependency
                function will be executed **around** the **request** and response cycle.

            Read more about it in the
            [FastAPI docs for FastAPI Dependencies with yield](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-with-yield/#early-exit-and-scope)
            """,
            """            主要用于带 `yield` 的依赖，定义依赖函数何时开始（yield 前）与结束（yield 后）。

            * `"function"`：在 path operation 函数前后执行，但在响应发送给客户端**之前**结束。
            * `"request"`：类似 `"function"`，但在响应发送给客户端**之后**结束。

            详见
            [FastAPI yield 依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-with-yield/#early-exit-and-scope)
            """,
        ),
    ],
    "fastapi/params.py": [
        (
            "class ParamTypes(Enum):",
            'class ParamTypes(Enum):\n    """OpenAPI 参数位置枚举（query、header、path、cookie）。"""',
        ),
        (
            "class Param(FieldInfo):  # type: ignore[misc]  # ty: ignore[subclass-of-final-class]",
            'class Param(FieldInfo):  # type: ignore[misc]  # ty: ignore[subclass-of-final-class]\n    """FastAPI 参数基类，扩展 Pydantic FieldInfo。"""',
        ),
        (
            "class Path(Param):  # type: ignore[misc]",
            'class Path(Param):  # type: ignore[misc]\n    """路径参数；不可有默认值。"""',
        ),
        (
            '        assert default is ..., "Path parameters cannot have a default value"',
            '        assert default is ..., "Path parameters cannot have a default value"  # 路径参数不能有默认值',
        ),
        (
            "class Query(Param):  # type: ignore[misc]",
            'class Query(Param):  # type: ignore[misc]\n    """查询参数。"""',
        ),
        (
            "class Header(Param):  # type: ignore[misc]",
            'class Header(Param):  # type: ignore[misc]\n    """请求头参数；默认将下划线转为连字符。"""',
        ),
        (
            "class Cookie(Param):  # type: ignore[misc]",
            'class Cookie(Param):  # type: ignore[misc]\n    """Cookie 参数。"""',
        ),
        (
            "class Body(FieldInfo):  # type: ignore[misc]  # ty: ignore[subclass-of-final-class]",
            'class Body(FieldInfo):  # type: ignore[misc]  # ty: ignore[subclass-of-final-class]\n    """请求体参数。"""',
        ),
        (
            "class Form(Body):  # type: ignore[misc]",
            'class Form(Body):  # type: ignore[misc]\n    """表单字段参数（application/x-www-form-urlencoded 或 multipart）。"""',
        ),
        (
            "class File(Form):  # type: ignore[misc]",
            'class File(Form):  # type: ignore[misc]\n    """上传文件参数（multipart/form-data）。"""',
        ),
        (
            "@dataclass(frozen=True)\nclass Depends:",
            '@dataclass(frozen=True)\nclass Depends:\n    """依赖项声明：可调用对象及缓存/作用域选项。"""',
        ),
        (
            "@dataclass(frozen=True)\nclass Security(Depends):",
            '@dataclass(frozen=True)\nclass Security(Depends):\n    """安全依赖项声明，可附加 OAuth2 作用域。"""',
        ),
    ],
    "fastapi/security/oauth2.py": [
        (
            'class OAuth2PasswordRequestForm:\n    """\n    This is a dependency class to collect the `username` and `password` as form data\n'
            "    for an OAuth2 password flow.",
            'class OAuth2PasswordRequestForm:\n    """\n    依赖类：以表单数据收集 OAuth2 密码流的 `username` 与 `password`。',
        ),
        (
            "    The OAuth2 specification dictates that for a password flow the data should be\n"
            "    collected using form data (instead of JSON) and that it should have the specific\n"
            "    fields `username` and `password`.",
            "    OAuth2 规范要求密码流使用表单（非 JSON），且字段名必须为 `username` 与 `password`。",
        ),
        (
            "    All the initialization parameters are extracted from the request.",
            "    所有初始化参数均从请求中提取。",
        ),
        (
            "    Read more about it in the\n"
            "    [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).",
            "    详见\n"
            "    [FastAPI 简单 OAuth2 密码与 Bearer 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).",
        ),
        (
            "    Note that for OAuth2 the scope `items:read` is a single scope in an opaque string.\n"
            "    You could have custom internal logic to separate it by colon characters (`:`) or\n"
            "    similar, and get the two parts `items` and `read`. Many applications do that to\n"
            "    group and organize permissions, you could do it as well in your application, just\n"
            "    know that it is application specific, it's not part of the specification.",
            "    OAuth2 中 `items:read` 等 scope 是不透明字符串中的单个作用域；应用可自行用冒号等分隔\n"
            "    以组织权限，但这属于应用层约定，非规范要求。",
        ),
        (
            """                The OAuth2 spec says it is required and MUST be the fixed string
                "password". Nevertheless, this dependency class is permissive and
                allows not passing it. If you want to enforce it, use instead the
                `OAuth2PasswordRequestFormStrict` dependency.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                OAuth2 规范要求 grant_type 必须为固定字符串 "password"。
                本依赖类较宽松，允许不传；若需强制，请使用 `OAuth2PasswordRequestFormStrict`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
        ),
        (
            """                `username` string. The OAuth2 spec requires the exact field name
                `username`.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                `username` 字符串，OAuth2 规范要求字段名必须为 `username`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            True,
        ),
        (
            """                `password` string. The OAuth2 spec requires the exact field name
                `password`.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                `password` 字符串，OAuth2 规范要求字段名必须为 `password`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            True,
        ),
        (
            """                A single string with actually several scopes separated by spaces. Each
                scope is also a string.

                For example, a single string with:

                ```python
                "items:read items:write users:read profile openid"
                ````

                would represent the scopes:

                * `items:read`
                * `items:write`
                * `users:read`
                * `profile`
                * `openid`

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                单个字符串，内含以空格分隔的多个 scope，例如：

                ```python
                "items:read items:write users:read profile openid"
                ```

                表示 scopes：`items:read`、`items:write`、`users:read`、`profile`、`openid`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            True,
        ),
        (
            """                If there's a `client_id`, it can be sent as part of the form fields.
                But the OAuth2 specification recommends sending the `client_id` and
                `client_secret` (if any) using HTTP Basic auth.
                """,
            """                若有 `client_id`，可作为表单字段发送；但 OAuth2 规范建议用 HTTP Basic 发送 client 凭据。
                """,
            True,
        ),
        (
            """                If there's a `client_secret` (and a `client_id`), they can be sent
                as part of the form fields. But the OAuth2 specification recommends
                sending the `client_id` and `client_secret` (if any) using HTTP Basic
                auth.
                """,
            """                若有 `client_secret`（及 `client_id`），可作为表单字段发送；
                但 OAuth2 规范建议用 HTTP Basic 发送。
                """,
            True,
        ),
        (
            'class OAuth2PasswordRequestFormStrict(OAuth2PasswordRequestForm):\n    """\n    This is a dependency class to collect the `username` and `password` as form data\n'
            "    for an OAuth2 password flow.",
            'class OAuth2PasswordRequestFormStrict(OAuth2PasswordRequestForm):\n    """\n    依赖类：以表单数据收集 OAuth2 密码流的 `username` 与 `password`（严格模式）。',
        ),
        (
            "    The only difference between `OAuth2PasswordRequestFormStrict` and\n"
            "    `OAuth2PasswordRequestForm` is that `OAuth2PasswordRequestFormStrict` requires the\n"
            '    client to send the form field `grant_type` with the value `"password"`, which\n'
            "    is required in the OAuth2 specification (it seems that for no particular reason),\n"
            "    while for `OAuth2PasswordRequestForm` `grant_type` is optional.",
            "    与 `OAuth2PasswordRequestForm` 的唯一区别：`OAuth2PasswordRequestFormStrict` 强制\n"
            '    客户端发送 `grant_type="password"`，而宽松版中 `grant_type` 可选。',
        ),
        (
            """                The OAuth2 spec says it is required and MUST be the fixed string
                "password". This dependency is strict about it. If you want to be
                permissive, use instead the `OAuth2PasswordRequestForm` dependency
                class.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                OAuth2 规范要求 grant_type 必须为 "password"；本依赖严格强制。
                若需宽松行为，请使用 `OAuth2PasswordRequestForm`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
        ),
        (
            'class OAuth2(SecurityBase):\n    """\n    This is the base class for OAuth2 authentication, an instance of it would be used\n'
            "    as a dependency. All other OAuth2 classes inherit from it and customize it for\n"
            "    each OAuth2 flow.",
            'class OAuth2(SecurityBase):\n    """\n    OAuth2 认证基类，其实例用作依赖项；其他 OAuth2 类继承并定制各流。',
        ),
        (
            "    You normally would not create a new class inheriting from it but use one of the\n"
            "    existing subclasses, and maybe compose them if you want to support multiple flows.",
            "    通常无需新建子类，使用现有子类即可；支持多流时可组合使用。",
        ),
        (
            "    Read more about it in the\n"
            "    [FastAPI docs for Security](https://fastapi.tiangolo.com/tutorial/security/).",
            "    详见\n"
            "    [FastAPI 安全文档](https://fastapi.tiangolo.com/tutorial/security/).",
        ),
        (
            """                The dictionary of OAuth2 flows.
                """,
            """                OAuth2 流配置字典。
                """,
        ),
        (
            '        """\n        The OAuth 2 specification doesn\'t define the challenge that should be used,\n'
            "        because a `Bearer` token is not really the only option to authenticate.",
            '        """\n        OAuth 2 规范未定义应使用的 challenge，因 Bearer 并非唯一认证方式。',
        ),
        (
            "        But declaring any other authentication challenge would be application-specific\n"
            "        as it's not defined in the specification.",
            "        声明其他 challenge 属于应用特定行为，规范未定义。",
        ),
        (
            "        For practical reasons, this method uses the `Bearer` challenge by default, as\n"
            "        it's probably the most common one.",
            "        出于实用考虑，本方法默认使用 `Bearer` challenge。",
        ),
        (
            "        If you are implementing an OAuth2 authentication scheme other than the provided\n"
            "        ones in FastAPI (based on bearer tokens), you might want to override this.",
            "        若实现非 Bearer 的 OAuth2 方案，可覆盖此方法。",
        ),
        (
            "        Ref: https://datatracker.ietf.org/doc/html/rfc6749",
            "        参考：https://datatracker.ietf.org/doc/html/rfc6749",
        ),
        (
            'class OAuth2PasswordBearer(OAuth2):\n    """\n    OAuth2 flow for authentication using a bearer token obtained with a password.\n'
            "    An instance of it would be used as a dependency.",
            'class OAuth2PasswordBearer(OAuth2):\n    """\n    使用密码流获取 Bearer 令牌的 OAuth2 认证，其实例用作依赖项。',
        ),
        (
            """                The URL to obtain the OAuth2 token. This would be the *path operation*
                that has `OAuth2PasswordRequestForm` as a dependency.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                获取 OAuth2 令牌的 URL，即依赖 `OAuth2PasswordRequestForm` 的 *path operation*。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
        ),
        (
            """                The OAuth2 scopes that would be required by the *path operations* that
                use this dependency.

                Read more about it in the
                [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
            """                使用本依赖的 *path operations* 所需的 OAuth2 作用域。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """,
        ),
        (
            'class OAuth2AuthorizationCodeBearer(OAuth2):\n    """\n    OAuth2 flow for authentication using a bearer token obtained with an OAuth2 code\n'
            "    flow. An instance of it would be used as a dependency.",
            'class OAuth2AuthorizationCodeBearer(OAuth2):\n    """\n    使用授权码流获取 Bearer 令牌的 OAuth2 认证，其实例用作依赖项。',
        ),
        (
            """                The URL to obtain the OAuth2 token.
                """,
            """                获取 OAuth2 令牌的 URL。
                """,
        ),
        (
            """                The OAuth2 scopes that would be required by the *path operations* that
                use this dependency.
                """,
            """                使用本依赖的 *path operations* 所需的 OAuth2 作用域。
                """,
        ),
        (
            'class SecurityScopes:\n    """\n    This is a special class that you can define in a parameter in a dependency to\n'
            "    obtain the OAuth2 scopes required by all the dependencies in the same chain.",
            'class SecurityScopes:\n    """\n    可在依赖参数中声明，以获取同链路上所有依赖所需的 OAuth2 作用域。',
        ),
        (
            "    This way, multiple dependencies can have different scopes, even when used in the\n"
            "    same *path operation*. And with this, you can access all the scopes required in\n"
            "    all those dependencies in a single place.",
            "    这样同一 *path operation* 中多个依赖可有不同 scope，并可在单处访问全部所需 scope。",
        ),
        (
            "    Read more about it in the\n"
            "    [FastAPI docs for OAuth2 scopes](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).",
            "    详见\n"
            "    [FastAPI OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).",
        ),
        (
            """                This will be filled by FastAPI.
                """,
            """                由 FastAPI 自动填充。
                """,
        ),
        (
            """                The list of all the scopes required by dependencies.
                """,
            """                所有依赖所需的作用域列表。
                """,
        ),
        (
            """                All the scopes required by all the dependencies in a single string
                separated by spaces, as defined in the OAuth2 specification.
                """,
            """                所有依赖所需作用域的空格分隔字符串，符合 OAuth2 规范。
                """,
        ),
    ],
    "docs_src/app_testing/app_b_py310/main.py": [
        (
            "from fastapi import FastAPI, Header, HTTPException",
            '"""被测应用：带 X-Token 请求头认证的 Items CRUD API（传统 Header 语法）。"""\n\nfrom fastapi import FastAPI, Header, HTTPException',
        ),
        (
            'fake_secret_token = "coneofsilence"',
            '# 模拟密钥令牌，用于校验 X-Token 请求头\nfake_secret_token = "coneofsilence"',
        ),
        (
            "fake_db = {",
            "# 模拟内存数据库\nfake_db = {",
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 资源的数据模型。"""',
        ),
        (
            "async def read_main(item_id: str, x_token: str = Header()):",
            'async def read_main(item_id: str, x_token: str = Header()):\n    """按 ID 读取 Item，需有效 X-Token。"""',
        ),
        (
            "    if x_token != fake_secret_token:",
            "    # 校验请求头令牌\n    if x_token != fake_secret_token:",
        ),
        (
            "async def create_item(item: Item, x_token: str = Header()) -> Item:",
            'async def create_item(item: Item, x_token: str = Header()) -> Item:\n    """创建 Item，ID 冲突时返回 409。"""',
        ),
    ],
    "docs_src/app_testing/app_b_py310/test_main.py": [
        (
            "from fastapi.testclient import TestClient",
            '"""app_b 的集成测试：覆盖认证、404 与冲突等场景（传统 Header 语法）。"""\n\nfrom fastapi.testclient import TestClient',
        ),
        (
            "client = TestClient(app)",
            "# 针对同一 app 实例创建测试客户端\nclient = TestClient(app)",
        ),
        (
            "def test_read_item():",
            'def test_read_item():\n    """有效令牌时应成功读取已有 Item。"""',
        ),
        (
            "def test_read_item_bad_token():",
            'def test_read_item_bad_token():\n    """无效 X-Token 应返回 400。"""',
        ),
        (
            "def test_read_nonexistent_item():",
            'def test_read_nonexistent_item():\n    """不存在的 Item ID 应返回 404。"""',
        ),
        (
            "def test_create_item():",
            'def test_create_item():\n    """有效请求应成功创建新 Item。"""',
        ),
        (
            "def test_create_item_bad_token():",
            'def test_create_item_bad_token():\n    """创建时无效令牌应返回 400。"""',
        ),
        (
            "def test_create_existing_item():",
            'def test_create_existing_item():\n    """重复 ID 创建应返回 409 Conflict。"""',
        ),
    ],
    "docs_src/app_testing/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：在同一文件中定义 FastAPI 应用并用 TestClient 测试 HTTP GET。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "client = TestClient(app)",
            "# 模块级 TestClient，测试函数共享同一 app 实例\nclient = TestClient(app)",
        ),
        (
            "def test_read_main():",
            'def test_read_main():\n    """验证根路径返回 200 与预期 JSON。"""',
        ),
    ],
    "docs_src/app_testing/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：在同一文件中测试 HTTP GET 与 WebSocket 连接。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "async def websocket(websocket: WebSocket):",
            'async def websocket(websocket: WebSocket):\n    """接受 WebSocket 连接并发送 JSON 后关闭。"""',
        ),
        (
            "def test_read_main():",
            'def test_read_main():\n    """验证 HTTP GET 根路径。"""',
        ),
        (
            "def test_websocket():",
            'def test_websocket():\n    """验证 WebSocket 连接与 JSON 消息。"""',
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, rel: str) -> str:
    for old, new, replace_all in GLOBAL_IN_FILES.get(rel, []):
        if old not in text:
            raise ValueError(f"Global pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new) if replace_all else text.replace(old, new, 1)
    for item in FILE_REPLACEMENTS.get(rel, []):
        if len(item) == 3:
            old, new, replace_all = item
        else:
            old, new = item
            replace_all = False
        if old not in text:
            if has_chinese(text):
                continue
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new) if replace_all else text.replace(old, new, 1)
    return text


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
    if has_chinese(text):
        return
    if rel in PREPEND:
        if text.strip():
            if not text.startswith('"""'):
                text = PREPEND[rel] + text
        else:
            text = PREPEND[rel]
    text = apply_replacements(text, rel)
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
