"""教程：自定义 HTTPBearer，未认证时返回 403 而非默认 401。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

app = FastAPI()


class HTTPBearer403(HTTPBearer):
    """HTTPBearer 子类，认证失败时抛出 403 Forbidden。"""
    def make_not_authenticated_error(self) -> HTTPException:
        """覆盖默认行为：未认证返回 403 而非 401。"""
        return HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Not authenticated"
        )


# Annotated 依赖：注入 Bearer 令牌凭证
CredentialsDep = Annotated[HTTPAuthorizationCredentials, Depends(HTTPBearer403())]


@app.get("/me")
def read_me(credentials: CredentialsDep):
    """需有效 Bearer 令牌，返回认证成功消息。"""
    return {"message": "You are authenticated", "token": credentials.credentials}
