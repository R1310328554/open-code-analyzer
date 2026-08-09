"""HTTP 安全方案辅助工具。"""

def get_authorization_scheme_param(
    authorization_header_value: str | None,
) -> tuple[str, str]:
    """
    解析 Authorization 头，返回 `(scheme, credentials)` 元组。

    按第一个空格拆分；若无头或为空，返回 `("", "")`。
    """
    if not authorization_header_value:
        return "", ""
    scheme, _, param = authorization_header_value.partition(" ")
    return scheme, param.strip()
