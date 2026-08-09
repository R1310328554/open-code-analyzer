"""教程 001：Strawberry GraphQL——定义 Query 与 User 类型，挂载到 /graphql。"""

import strawberry
from fastapi import FastAPI
from strawberry.fastapi import GraphQLRouter


@strawberry.type
class User:
    """GraphQL User 对象类型。"""
    name: str
    age: int


@strawberry.type
class Query:
    """GraphQL 根查询类型。"""

    @strawberry.field
    def user(self) -> User:
        """返回示例用户。"""
        return User(name="Patrick", age=100)


schema = strawberry.Schema(query=Query)  # 构建 GraphQL schema


graphql_app = GraphQLRouter(schema)  # ASGI 子应用，处理 GraphQL 请求


app = FastAPI()
app.include_router(graphql_app, prefix="/graphql")  # POST/GET /graphql 访问 GraphiQL 与查询
