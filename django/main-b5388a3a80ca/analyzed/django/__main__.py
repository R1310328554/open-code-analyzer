"""
Invokes django-admin when the django module is run as a script.

Example: python -m django check
"""

# 支持 python -m django 调用 django-admin 命令行
from django.core import management

if __name__ == "__main__":
    management.execute_from_command_line()
