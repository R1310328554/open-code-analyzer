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

// minio.go 基于 minio-go 的对象存储适配层。

import (
	"bytes"
	"context"
	"crypto/tls"
	"fmt"
	"net/http"
	"ragflow/internal/common"
	"ragflow/internal/server"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"go.uber.org/zap"
)

// MinioStorage MinIO 后端，支持单桶+前缀路径映射模式。
type MinioStorage struct {
	client     *minio.Client
	bucket     string // default bucket
	prefixPath string // default prefix path
	config     *server.MinioConfig
}

// NewMinioStorage 根据配置连接 MinIO 并返回 Storage 实例。
func NewMinioStorage(config *server.MinioConfig) (*MinioStorage, error) {
	storage := &MinioStorage{
		bucket:     config.Bucket,
		prefixPath: config.PrefixPath,
		config:     config,
	}

	if err := storage.connect(); err != nil {
		return nil, err
	}

	return storage, nil
}

func (m *MinioStorage) connect() error {
	var transport http.RoundTripper

	// 配置 TLS：Secure 模式下可按 verify 开关跳过证书校验
	if m.config.Secure {
		verify := m.config.Verify
		transport = &http.Transport{
			TLSClientConfig: &tls.Config{
				InsecureSkipVerify: !verify,
			},
		}
	}

	client, err := minio.New(m.config.Host, &minio.Options{
		Creds:     credentials.NewStaticV4(m.config.User, m.config.Password, ""),
		Secure:    m.config.Secure,
		Transport: transport,
		Region:    m.config.Region,
	})
	if err != nil {
		return fmt.Errorf("failed to connect to MinIO: %w", err)
	}

	m.client = client
	return nil
}

func (m *MinioStorage) reconnect() {
	if err := m.connect(); err != nil {
		common.Fatal(fmt.Sprintf("Failed to reconnect to MinIO, %s", err.Error()))
	}
}

func (m *MinioStorage) resolveBucketAndPath(bucket, fnm string) (string, string) {
	actualBucket := bucket
	if m.bucket != "" {
		actualBucket = m.bucket
	}

	actualPath := fnm
	if m.bucket != "" {
		if m.prefixPath != "" {
			actualPath = fmt.Sprintf("%s/%s/%s", m.prefixPath, bucket, fnm)
		} else {
			actualPath = fmt.Sprintf("%s/%s", bucket, fnm)
		}
	} else if m.prefixPath != "" {
		actualPath = fmt.Sprintf("%s/%s", m.prefixPath, fnm)
	}

	return actualBucket, actualPath
}

// Health 调用 MinIO HealthCheck 并检查客户端在线状态。
func (m *MinioStorage) Health() bool {
	cancelFunction, err := m.client.HealthCheck(time.Second * 5)
	if cancelFunction != nil {
		defer cancelFunction()
	}

	if err != nil {
		common.Warn("Failed to check MinIO health", zap.Error(err))
		return false
	}

	return m.client.IsOnline()
}

// Put 上传对象；非单桶模式时自动建桶，失败最多重试 3 次并重连。
func (m *MinioStorage) Put(bucket, fnm string, binary []byte, tenantID ...string) error {
	bucket, fnm = m.resolveBucketAndPath(bucket, fnm)

	ctx := context.Background()

	var err error

	for i := 0; i < 3; i++ {
		var exists bool
		// 单桶模式关闭时确保目标桶存在
		if m.bucket == "" {
			exists, err = m.client.BucketExists(ctx, bucket)
			if err != nil {
				common.Warn("Failed to check bucket existence", zap.String("bucket", bucket), zap.Error(err))
				m.reconnect()
				time.Sleep(time.Second)
				continue
			}
			if !exists {
				if err = m.client.MakeBucket(ctx, bucket, minio.MakeBucketOptions{}); err != nil {
					common.Warn("Failed to create bucket", zap.String("bucket", bucket), zap.Error(err))
					m.reconnect()
					time.Sleep(time.Second)
					continue
				}
			}
		}

		reader := bytes.NewReader(binary)
		_, err = m.client.PutObject(ctx, bucket, fnm, reader, int64(len(binary)), minio.PutObjectOptions{})
		if err != nil {
			common.Warn("Failed to put object", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
			m.reconnect()
			time.Sleep(time.Second)
			continue
		}

		return nil
	}

	return err
}

// Get 下载对象内容，失败时重连并重试。
func (m *MinioStorage) Get(bucket, fnm string, tenantID ...string) ([]byte, error) {
	bucket, fnm = m.resolveBucketAndPath(bucket, fnm)

	ctx := context.Background()

	for i := 0; i < 2; i++ {
		obj, err := m.client.GetObject(ctx, bucket, fnm, minio.GetObjectOptions{})
		if err != nil {
			common.Warn("failed to get object", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
			m.reconnect()
			time.Sleep(time.Second)
			continue
		}
		defer obj.Close()

		buf := new(bytes.Buffer)
		if _, err = buf.ReadFrom(obj); err != nil {
			common.Warn("failed to read object data", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
			m.reconnect()
			time.Sleep(time.Second)
			continue
		}

		return buf.Bytes(), nil
	}

	return nil, fmt.Errorf("failed to get object after retries")
}

// Remove 删除 MinIO 对象。
func (m *MinioStorage) Remove(bucket, fnm string, tenantID ...string) error {
	bucket, fnm = m.resolveBucketAndPath(bucket, fnm)

	ctx := context.Background()

	if err := m.client.RemoveObject(ctx, bucket, fnm, minio.RemoveObjectOptions{}); err != nil {
		common.Warn("Failed to remove object", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
		return err
	}

	return nil
}

// ObjExist 先检查桶再 StatObject 判断键是否存在。
func (m *MinioStorage) ObjExist(bucket, fnm string, tenantID ...string) bool {
	bucket, fnm = m.resolveBucketAndPath(bucket, fnm)

	ctx := context.Background()

	exists, err := m.client.BucketExists(ctx, bucket)
	if err != nil || !exists {
		return false
	}

	_, err = m.client.StatObject(ctx, bucket, fnm, minio.StatObjectOptions{})
	if err != nil {
		errResponse := minio.ToErrorResponse(err)
		if errResponse.Code == "NoSuchKey" || errResponse.Code == "NoSuchBucket" {
			return false
		}
		common.Warn("Failed to stat object", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
		return false
	}

	return true
}

// GetPresignedURL 生成预签名 GET URL，最多重试 10 次。
func (m *MinioStorage) GetPresignedURL(bucket, fnm string, expires time.Duration, tenantID ...string) (string, error) {
	bucket, fnm = m.resolveBucketAndPath(bucket, fnm)

	ctx := context.Background()

	for i := 0; i < 10; i++ {
		url, err := m.client.PresignedGetObject(ctx, bucket, fnm, expires, nil)
		if err != nil {
			common.Warn("Failed to get presigned URL", zap.String("bucket", bucket), zap.String("key", fnm), zap.Error(err))
			m.reconnect()
			time.Sleep(time.Second)
			continue
		}

		return url.String(), nil
	}

	return "", fmt.Errorf("failed to get presigned URL after 10 retries")
}

// BucketExists 检查桶是否存在（单桶模式映射到配置的默认桶）。
func (m *MinioStorage) BucketExists(bucket string) bool {
	actualBucket := bucket
	if m.bucket != "" {
		actualBucket = m.bucket
	}

	ctx := context.Background()

	exists, err := m.client.BucketExists(ctx, actualBucket)
	if err != nil {
		common.Warn("Failed to check bucket existence", zap.String("bucket", actualBucket), zap.Error(err))
		return false
	}

	return exists
}

// RemoveBucket 按前缀列出并批量删除对象；单桶模式仅删前缀下对象。
func (m *MinioStorage) RemoveBucket(bucket string) error {
	actualBucket := bucket
	origBucket := bucket

	if m.bucket != "" {
		actualBucket = m.bucket
	}

	ctx := context.Background()

	// 单桶模式下构造待删除对象的前缀
	prefix := ""
	if m.bucket != "" {
		if m.prefixPath != "" {
			prefix = fmt.Sprintf("%s/", m.prefixPath)
		}
		prefix += fmt.Sprintf("%s/", origBucket)
	}

	// 异步列出带前缀对象并批量 RemoveObjects
	objectsCh := make(chan minio.ObjectInfo)

	go func() {
		defer close(objectsCh)
		for obj := range m.client.ListObjects(ctx, actualBucket, minio.ListObjectsOptions{
			Prefix:    prefix,
			Recursive: true,
		}) {
			if obj.Err != nil {
				common.Warn("Failed to list objects", zap.Error(obj.Err))
				return
			}
			objectsCh <- obj
		}
	}()

	for err := range m.client.RemoveObjects(ctx, actualBucket, objectsCh, minio.RemoveObjectsOptions{}) {
		common.Warn(fmt.Sprintf("Failed to remove object, key: %s", err.ObjectName), zap.Error(err.Err))
	}

	// 非单桶模式才调用 RemoveBucket 删除物理桶
	if m.bucket == "" {
		if err := m.client.RemoveBucket(ctx, actualBucket); err != nil {
			common.Warn("Failed to remove bucket", zap.String("bucket", actualBucket), zap.Error(err))
			return err
		}
	}

	return nil
}

// Copy 服务端复制对象，必要时创建目标桶。
func (m *MinioStorage) Copy(srcBucket, srcPath, destBucket, destPath string) bool {
	srcBucket, srcPath = m.resolveBucketAndPath(srcBucket, srcPath)
	destBucket, destPath = m.resolveBucketAndPath(destBucket, destPath)

	ctx := context.Background()

	// Ensure destination bucket exists
	if m.bucket == "" {
		exists, err := m.client.BucketExists(ctx, destBucket)
		if err != nil {
			common.Warn("Failed to check bucket existence", zap.String("bucket", destBucket), zap.Error(err))
			return false
		}
		if !exists {
			if err = m.client.MakeBucket(ctx, destBucket, minio.MakeBucketOptions{}); err != nil {
				common.Warn("Failed to create bucket", zap.String("bucket", destBucket), zap.Error(err))
				return false
			}
		}
	}

	// 复制前 Stat 源对象确认存在
	_, err := m.client.StatObject(ctx, srcBucket, srcPath, minio.StatObjectOptions{})
	if err != nil {
		common.Warn("Failed to stat source object", zap.String("bucket", srcBucket), zap.String("key", srcPath), zap.Error(err))
		return false
	}

	// 执行 CopyObject
	srcOpts := minio.CopySrcOptions{
		Bucket: srcBucket,
		Object: srcPath,
	}
	destOpts := minio.CopyDestOptions{
		Bucket: destBucket,
		Object: destPath,
	}

	_, err = m.client.CopyObject(ctx, destOpts, srcOpts)
	if err != nil {
		common.Warn("Failed to copy object", zap.String("src", fmt.Sprintf("%s/%s", srcBucket, srcPath)), zap.String("dest", fmt.Sprintf("%s/%s", destBucket, destPath)), zap.Error(err))
		return false
	}

	return true
}

// Move 复制成功后删除源对象。
func (m *MinioStorage) Move(srcBucket, srcPath, destBucket, destPath string) bool {
	if m.Copy(srcBucket, srcPath, destBucket, destPath) {
		if err := m.Remove(srcBucket, srcPath); err != nil {
			common.Warn("Failed to remove source object after copy", zap.String("bucket", srcBucket), zap.String("key", srcPath), zap.Error(err))
			return false
		}
		return true
	}
	return false
}
// minio.go — MinIO 对象存储适配：上传、预签名 URL 与桶前缀映射。
