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
Azure Blob 存储（SAS 令牌）连接器：通过共享访问签名上传/下载 RAG 文件。
"""



import logging
import os
import time
from io import BytesIO
from common.decorator import singleton
from azure.storage.blob import ContainerClient
from common import settings


@singleton
class RAGFlowAzureSasBlob:
    # 单例 Azure ContainerClient，SAS URL + token 认证
    def __init__(self):
        self.conn = None
        self.container_url = os.getenv("CONTAINER_URL", settings.AZURE["container_url"])
        self.sas_token = os.getenv("SAS_TOKEN", settings.AZURE["sas_token"])
        self.__open__()

    def __open__(self):
        # 重建 ContainerClient 连接（失败时记录异常）
        try:
            if self.conn:
                self.__close__()
        except Exception:
            pass

        try:
            self.conn = ContainerClient.from_container_url(self.container_url + "?" + self.sas_token)
        except Exception:
            logging.exception("Fail to connect %s " % self.container_url)

    def __close__(self):
        del self.conn
        self.conn = None

    def health(self):
        # 上传探针 blob 验证写入权限
        _bucket, fnm, binary = "txtxtxtxt1", "txtxtxtxt1", b"_t@@@1"
        return self.conn.upload_blob(name=f"{_bucket}/{fnm}", data=BytesIO(binary), length=len(binary))

    def put(self, bucket, fnm, binary, tenant_id=None):
        # 上传二进制到 bucket/fnm，失败重试 3 次并重连
        blob_name = f"{bucket}/{fnm}"
        for _ in range(3):
            try:
                return self.conn.upload_blob(name=blob_name, data=BytesIO(binary), length=len(binary))
            except Exception:
                logging.exception(f"Fail put {blob_name}")
                self.__open__()
                time.sleep(1)

    def rm(self, bucket, fnm):
        # 删除指定 blob
        try:
            self.conn.delete_blob(f"{bucket}/{fnm}")
        except Exception:
            logging.exception(f"Fail rm {bucket}/{fnm}")

    def get(self, bucket, fnm):
        # 下载 blob 字节内容
        blob_name = f"{bucket}/{fnm}"
        for _ in range(1):
            try:
                r = self.conn.download_blob(blob_name)
                return r.read()
            except Exception:
                logging.exception(f"fail get {blob_name}")
                self.__open__()
                time.sleep(1)
        return None

    def obj_exist(self, bucket, fnm):
        # 检查 blob 是否存在
        blob_name = f"{bucket}/{fnm}"
        try:
            return self.conn.get_blob_client(f"{blob_name}").exists()
        except Exception:
            logging.exception(f"Fail put {blob_name}")
        return False

    def get_presigned_url(self, bucket, fnm, expires):
        # 生成带过期时间的 GET 预签名 URL
        blob_name = f"{bucket}/{fnm}"
        for _ in range(10):
            try:
                return self.conn.get_presigned_url("GET", bucket, blob_name, expires)
            except Exception:
                logging.exception(f"fail get {blob_name}")
                self.__open__()
                time.sleep(1)
        return None
