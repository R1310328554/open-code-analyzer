"""Exception class definitions"""


class ConnectorMissingCredentialError(Exception):
    # 未调用 load_credentials 或缺少必要字段
    """Missing credentials exception"""

    def __init__(self, connector_name: str):
        super().__init__(f"Missing credentials for {connector_name}")


class ConnectorValidationError(Exception):
    # 设置校验失败（空间不存在、API 错误等）
    """Connector validation exception"""

    pass


class CredentialExpiredError(Exception):
    # HTTP 401 等表明令牌已失效
    """Credential expired exception"""

    pass


class InsufficientPermissionsError(Exception):
    # HTTP 403：scope 或 ACL 不足
    """Insufficient permissions exception"""

    pass


class UnexpectedValidationError(Exception):
    # 校验阶段未预期的网络或服务错误
    """Unexpected validation exception"""

    pass


class RateLimitTriedTooManyTimesError(Exception):
    # 限流退避重试次数用尽
    pass
