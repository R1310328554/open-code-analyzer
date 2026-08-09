from fastapi.openapi.models import SecurityBase as SecurityBaseModel


class SecurityBase:
    """所有安全方案依赖的基类。"""

    model: SecurityBaseModel
    scheme_name: str
