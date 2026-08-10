"""
Admin 模块专用异常类型。
"""

class AdminException(Exception):
    def __init__(self, message, code=400):
        super().__init__(message)
        self.code = code
        self.message = message


"""
指定用户不存在（404）。
"""

class UserNotFoundError(AdminException):
    def __init__(self, username):
        super().__init__(f"User '{username}' not found", 404)


"""
创建用户时邮箱已占用（409）。
"""

class UserAlreadyExistsError(AdminException):
    def __init__(self, username):
        super().__init__(f"User '{username}' already exists", 409)


"""
禁止删除管理员账户（403）。
"""

class CannotDeleteAdminError(AdminException):
    def __init__(self):
        super().__init__("Cannot delete admin account", 403)
