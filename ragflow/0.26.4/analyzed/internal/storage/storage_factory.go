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

// storage_factory.go 管理全局 Storage 单例与按类型创建实例。

import (
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/server"
	"sync"
)

var (
	globalFactory *StorageFactory
	once          sync.Once
)

// StorageFactory 根据 server.StorageConfig 持有当前 Storage 实例。
type StorageFactory struct {
	storageType StorageType
	storage     Storage
	config      *server.StorageConfig
	mu          sync.RWMutex
}

// GetStorageFactory 返回全局 StorageFactory 单例（sync.Once 初始化）。
func GetStorageFactory() *StorageFactory {
	once.Do(func() {
		globalFactory = &StorageFactory{}
	})
	return globalFactory
}

// InitStorageFactory 从全局配置读取 storage 类型并完成 initStorage。
func InitStorageFactory() error {
	factory := GetStorageFactory()

	globalConfig := server.GetConfig()
	factory.config = &globalConfig.StorageEngine
	// 按配置类型实例化具体后端
	if err := factory.initStorage(); err != nil {
		return err
	}

	common.Info(fmt.Sprintf("Storage initialized: %s", factory.config.Type))

	return nil
}

// initStorage 分发到 minio/s3/oss 初始化分支。
func (f *StorageFactory) initStorage() error {
	switch f.config.Type {
	case "minio":
		return f.initMinio(f.config.Minio)
	case "s3":
		return f.initS3(f.config.S3)
	case "oss":
		return f.initOSS(f.config.OSS)
	default:
		return fmt.Errorf("unsupported storage type: %s", f.config.Type)
	}
}

func (f *StorageFactory) initMinio(minioConfig *server.MinioConfig) error {
	storage, err := NewMinioStorage(minioConfig)
	if err != nil {
		return fmt.Errorf("failed to create MinIO storage: %w", err)
	}

	f.mu.Lock()
	defer f.mu.Unlock()
	f.storageType = StorageMinio
	f.storage = storage
	f.config.Minio = minioConfig

	return nil
}

func (f *StorageFactory) initS3(s3Config *server.S3Config) error {
	storage, err := NewS3Storage(s3Config)
	if err != nil {
		return fmt.Errorf("failed to create S3 storage: %w", err)
	}

	f.mu.Lock()
	defer f.mu.Unlock()
	f.storageType = StorageAWSS3
	f.storage = storage
	f.config.S3 = s3Config

	return nil
}

func (f *StorageFactory) initOSS(ossConfig *server.OSSConfig) error {

	storage, err := NewOSSStorage(ossConfig)
	if err != nil {
		return fmt.Errorf("failed to create OSS storage: %w", err)
	}

	f.mu.Lock()
	defer f.mu.Unlock()
	f.storageType = StorageOSS
	f.storage = storage
	f.config.OSS = ossConfig

	return nil
}

// GetStorage 读锁返回当前 Storage 接口实现。
func (f *StorageFactory) GetStorage() Storage {
	f.mu.RLock()
	defer f.mu.RUnlock()
	return f.storage
}

// GetStorageType 返回当前 StorageType 枚举值。
func (f *StorageFactory) GetStorageType() StorageType {
	f.mu.RLock()
	defer f.mu.RUnlock()
	return f.storageType
}

// Create 按类型新建 Storage 实例，等价 Python StorageFactory.create()。
func (f *StorageFactory) Create(storageType StorageType) (Storage, error) {
	var storage Storage
	var err error

	switch storageType {
	case StorageMinio:
		if f.config.Minio != nil {
			storage, err = NewMinioStorage(f.config.Minio)
		} else {
			return nil, fmt.Errorf("MinIO config not available")
		}
	case StorageAWSS3:
		if f.config.S3 != nil {
			storage, err = NewS3Storage(f.config.S3)
		} else {
			return nil, fmt.Errorf("S3 config not available")
		}
	case StorageOSS:
		if f.config.OSS != nil {
			storage, err = NewOSSStorage(f.config.OSS)
		} else {
			return nil, fmt.Errorf("OSS config not available")
		}
	default:
		return nil, fmt.Errorf("unsupported storage type: %v", storageType)
	}

	if err != nil {
		return nil, err
	}

	return storage, nil
}

// SetStorage 注入 Storage 实现，便于单测 mock。
func (f *StorageFactory) SetStorage(storage Storage) {
	f.mu.Lock()
	defer f.mu.Unlock()
	f.storage = storage
}

// StorageTypeMapping 类型到构造函数的映射表，对齐 Python storage_mapping。
var StorageTypeMapping = map[StorageType]func(*server.StorageConfig) (Storage, error){
	StorageMinio: func(config *server.StorageConfig) (Storage, error) {
		if config.Minio == nil {
			return nil, fmt.Errorf("MinIO config not available")
		}
		return NewMinioStorage(config.Minio)
	},
	StorageAWSS3: func(config *server.StorageConfig) (Storage, error) {
		if config.S3 == nil {
			return nil, fmt.Errorf("S3 config not available")
		}
		return NewS3Storage(config.S3)
	},
	StorageOSS: func(config *server.StorageConfig) (Storage, error) {
		if config.OSS == nil {
			return nil, fmt.Errorf("OSS config not available")
		}
		return NewOSSStorage(config.OSS)
	},
}
// storage_factory.go — 全局 Storage 工厂单例，按配置初始化 minio/s3/oss。
