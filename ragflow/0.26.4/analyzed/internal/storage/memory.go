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

// memory.go 提供进程内 Storage 实现，用于测试与无外部依赖场景。

import (
	"errors"
	"fmt"
	"sync"
	"time"
)

// ErrMemoryNotFound 当桶或对象键不存在时返回。
var ErrMemoryNotFound = errors.New("memory storage: object not found")

// MemoryEntry 描述单条内存对象元数据，供 Inspect 诊断使用。
type MemoryEntry struct {
	Bucket string
	Key    string
	Size   int
}

// MemoryStorage 进程内 Storage 实现，读写加锁，适合单元测试。
type MemoryStorage struct {
	mu      sync.RWMutex
	objects map[string]map[string][]byte
}

// NewMemoryStorage 创建空的内存存储后端。
func NewMemoryStorage() Storage {
	return &MemoryStorage{objects: make(map[string]map[string][]byte)}
}

// Health 内存后端恒为健康。
func (m *MemoryStorage) Health() bool {
	return true
}

// Put 写入对象；桶不存在则创建，存储内容为 defensive copy。
func (m *MemoryStorage) Put(bucket, fnm string, binary []byte, tenantID ...string) error {
	if bucket == "" {
		return fmt.Errorf("memory storage: bucket is required")
	}
	if fnm == "" {
		return fmt.Errorf("memory storage: key is required")
	}

	m.mu.Lock()
	defer m.mu.Unlock()

	bucketMap, ok := m.objects[bucket]
	if !ok {
		bucketMap = make(map[string][]byte)
		m.objects[bucket] = bucketMap
	}

	cp := make([]byte, len(binary))
	copy(cp, binary)
	bucketMap[fnm] = cp
	return nil
}

// Get 读取对象副本；桶或键缺失时包装 ErrMemoryNotFound。
func (m *MemoryStorage) Get(bucket, fnm string, tenantID ...string) ([]byte, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	bucketMap, ok := m.objects[bucket]
	if !ok {
		return nil, fmt.Errorf("memory storage: bucket %q: %w", bucket, ErrMemoryNotFound)
	}
	data, ok := bucketMap[fnm]
	if !ok {
		return nil, fmt.Errorf("memory storage: object %q in bucket %q: %w", fnm, bucket, ErrMemoryNotFound)
	}

	out := make([]byte, len(data))
	copy(out, data)
	return out, nil
}

// Remove 删除对象；不存在则静默成功。
func (m *MemoryStorage) Remove(bucket, fnm string, tenantID ...string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	bucketMap, ok := m.objects[bucket]
	if !ok {
		return nil
	}
	delete(bucketMap, fnm)
	return nil
}

// ObjExist 判断桶内键是否存在。
func (m *MemoryStorage) ObjExist(bucket, fnm string, tenantID ...string) bool {
	m.mu.RLock()
	defer m.mu.RUnlock()

	bucketMap, ok := m.objects[bucket]
	if !ok {
		return false
	}
	_, ok = bucketMap[fnm]
	return ok
}

// GetPresignedURL 返回 deterministic 测试用 URL：memory://桶/键?exp=过期时间戳。
func (m *MemoryStorage) GetPresignedURL(bucket, fnm string, expires time.Duration, tenantID ...string) (string, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()

	if _, ok := m.objects[bucket]; !ok {
		return "", fmt.Errorf("memory storage: bucket %q: %w", bucket, ErrMemoryNotFound)
	}
	bucketMap := m.objects[bucket]
	if _, ok := bucketMap[fnm]; !ok {
		return "", fmt.Errorf("memory storage: object %q in bucket %q: %w", fnm, bucket, ErrMemoryNotFound)
	}

	exp := time.Now().Add(expires).Unix()
	return fmt.Sprintf("memory://%s/%s?exp=%d", bucket, fnm, exp), nil
}

// BucketExists 判断桶是否已创建。
func (m *MemoryStorage) BucketExists(bucket string) bool {
	m.mu.RLock()
	defer m.mu.RUnlock()

	_, ok := m.objects[bucket]
	return ok
}

// RemoveBucket 删除整个桶及其全部键；不存在则 no-op。
func (m *MemoryStorage) RemoveBucket(bucket string) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	delete(m.objects, bucket)
	return nil
}

// Copy 复制对象到目标路径，源对象保留；源不存在则返回 false。
func (m *MemoryStorage) Copy(srcBucket, srcPath, destBucket, destPath string) bool {
	m.mu.RLock()
	srcBucketMap, ok := m.objects[srcBucket]
	if !ok {
		m.mu.RUnlock()
		return false
	}
	data, ok := srcBucketMap[srcPath]
	if !ok {
		m.mu.RUnlock()
		return false
	}
	cp := make([]byte, len(data))
	copy(cp, data)
	m.mu.RUnlock()

	m.mu.Lock()
	defer m.mu.Unlock()
	destBucketMap, ok := m.objects[destBucket]
	if !ok {
		destBucketMap = make(map[string][]byte)
		m.objects[destBucket] = destBucketMap
	}
	destBucketMap[destPath] = cp
	return true
}

// Move 先 Copy 再删除源对象。
func (m *MemoryStorage) Move(srcBucket, srcPath, destBucket, destPath string) bool {
	if !m.Copy(srcBucket, srcPath, destBucket, destPath) {
		return false
	}
	if err := m.Remove(srcBucket, srcPath); err != nil {
		return false
	}
	return true
}

// Inspect 返回当前全部 (bucket,key,size) 快照，供测试断言。
func (m *MemoryStorage) Inspect() []MemoryEntry {
	m.mu.RLock()
	defer m.mu.RUnlock()

	out := make([]MemoryEntry, 0)
	for bucket, bucketMap := range m.objects {
		for key, data := range bucketMap {
			out = append(out, MemoryEntry{Bucket: bucket, Key: key, Size: len(data)})
		}
	}
	return out
}
// memory.go — 进程内 Storage 实现，供单元测试与临时工具使用。
