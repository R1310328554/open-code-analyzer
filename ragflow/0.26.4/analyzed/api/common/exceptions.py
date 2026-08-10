#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
管理后台专用异常层次：统一 type/code/message 供 Admin API 返回结构化错误。
"""

#


class AdminException(Exception):
    # 管理端异常基类
    def __init__(self, message, code=400):
        super().__init__(message)
        self.type = "admin"
        self.code = code
        self.message = message


class UserNotFoundError(AdminException):
    # 404：用户不存在
    def __init__(self, username):
        super().__init__(f"User '{username}' not found", 404)


class UserAlreadyExistsError(AdminException):
    # 409：邮箱/用户名冲突
    def __init__(self, username):
        super().__init__(f"User '{username}' already exists", 409)


class CannotDeleteAdminError(AdminException):
    # 403：禁止删除超级管理员
    def __init__(self):
        super().__init__("Cannot delete admin account", 403)


class NotAdminError(AdminException):
    # 403：非管理员访问管理接口
    def __init__(self, username):
        super().__init__(f"User '{username}' is not admin", 403)
