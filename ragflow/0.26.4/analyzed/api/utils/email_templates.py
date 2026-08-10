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
#

"""
可复用邮件模板与注册表：团队邀请与密码重置验证码。
"""

# 团队邀请邮件模板（Jinja 占位：email/inviter/tenant_id/invite_url）
INVITE_EMAIL_TMPL = """
Hi {{email}},
{{inviter}} has invited you to join their team (ID: {{tenant_id}}).
Click the link below to complete your registration:
{{invite_url}}
If you did not request this, please ignore this email.
"""

# 密码重置验证码邮件模板
RESET_CODE_EMAIL_TMPL = """
Hello,
Your password reset code is: {{ code }}
This code will expire in {{ ttl_min }} minutes.
"""

# 模板名 → 正文注册表
EMAIL_TEMPLATES = {
    "invite": INVITE_EMAIL_TMPL,
    "reset_code": RESET_CODE_EMAIL_TMPL,
}
