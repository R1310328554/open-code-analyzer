//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package storage

// types.go 定义 Storage 抽象接口与各后端类型常量。

import (
	"time"
)

// StorageType 存储后端类型枚举。
type StorageType int

const (
	StorageMinio    StorageType = 1
	StorageAzureSpn StorageType = 2
	StorageAzureSas StorageType = 3
	StorageAWSS3    StorageType = 4
	StorageOSS      StorageType = 5
	StorageOpenDAL  StorageType = 6
	StorageGCS      StorageType = 7
)

func (s StorageType) String() string {
	switch s {
	case StorageMinio:
		return "MINIO"
	case StorageAzureSpn:
		return "AZURE_SPN"
	case StorageAzureSas:
		return "AZURE_SAS"
	case StorageAWSS3:
		return "AWS_S3"
	case StorageOSS:
		return "OSS"
	case StorageOpenDAL:
		return "OPENDAL"
	case StorageGCS:
		return "GCS"
	default:
		return "UNKNOWN"
	}
}

// Storage 对象存储统一接口，Ragflow 文档/附件读写均经此抽象。
type Storage interface {
	// Health 检查后端服务是否可用
	Health() bool

	// Put 上传对象；bucket 桶名，fnm 对象键，binary 内容，tenantID 可选租户标识
	Put(bucket, fnm string, binary []byte, tenantID ...string) error

	// Get 下载对象字节；不存在时返回 error
	Get(bucket, fnm string, tenantID ...string) ([]byte, error)

	// Remove 删除指定对象
	Remove(bucket, fnm string, tenantID ...string) error

	// ObjExist 判断对象是否存在
	ObjExist(bucket, fnm string, tenantID ...string) bool

	// GetPresignedURL 生成带过期时间的预签名访问 URL
	GetPresignedURL(bucket, fnm string, expires time.Duration, tenantID ...string) (string, error)

	// BucketExists 判断桶是否存在
	BucketExists(bucket string) bool

	// RemoveBucket 删除桶及其全部对象
	RemoveBucket(bucket string) error

	// Copy 服务端复制对象
	Copy(srcBucket, srcPath, destBucket, destPath string) bool

	// Move 移动对象（复制后删源）
	Move(srcBucket, srcPath, destBucket, destPath string) bool
}
// types.go — Storage 接口与后端类型枚举定义。
