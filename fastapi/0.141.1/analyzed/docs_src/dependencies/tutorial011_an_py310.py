"""教程 011（Annotated）：可调用类实例作为依赖——__call__ 接收查询参数。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


class FixedContentQueryChecker:
    """构造时固定待匹配子串；__call__ 判断查询参数 q 是否包含该子串。"""

    def __init__(self, fixed_content: str):
        self.fixed_content = fixed_content

    def __call__(self, q: str = ""):
        if q:
            return self.fixed_content in q
        return False


checker = FixedContentQueryChecker("bar")  # 预配置实例，Depends(checker) 直接注入


@app.get("/query-checker/")
async def read_query_check(fixed_content_included: Annotated[bool, Depends(checker)]):
    """Depends(实例) 调用 checker.__call__，q 来自查询字符串。"""
    return {"fixed_content_in_query": fixed_content_included}
