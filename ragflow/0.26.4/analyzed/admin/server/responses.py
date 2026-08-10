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
Admin API 统一 JSON 响应构造。

约定字段：code、message、data；成功默认 HTTP 200。
"""
from flask import jsonify


def success_response(data=None, message="Success", code=0):
    return jsonify({"code": code, "message": message, "data": data}), 200


"""
构造带错误码与消息的 JSON 响应（HTTP 400）。
"""

def error_response(message="Error", code=-1, data=None):
    return jsonify({"code": code, "message": message, "data": data}), 400
