// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// logs 包 S3 后端，将构建步骤日志存储到 AWS S3 或兼容对象存储。
package logs

import (
	"context"
	"fmt"
	"io"
	"path"
	"strings"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"

	"github.com/drone/drone/core"
)

// NewS3Env 从 bucket、前缀与 endpoint 环境变量构造 S3 LogStore。
func NewS3Env(bucket, prefix, endpoint string, pathStyle bool) core.LogStore {
	disableSSL := false

	if endpoint != "" {
		disableSSL = !strings.HasPrefix(endpoint, "https://")
	}

	return &s3store{
		bucket: bucket,
		prefix: prefix,
		session: session.Must(
			session.NewSession(&aws.Config{
				Endpoint:         aws.String(endpoint),
				DisableSSL:       aws.Bool(disableSSL),
				S3ForcePathStyle: aws.Bool(pathStyle),
			}),
		),
	}
}

// NewS3 使用已有 AWS Session 构造 S3 LogStore。
func NewS3(session *session.Session, bucket, prefix string) core.LogStore {
	return &s3store{
		bucket:  bucket,
		prefix:  prefix,
		session: session,
	}
}

// s3store 以 prefix/stepID 为对象键存储日志。
type s3store struct {
	bucket  string
	prefix  string
	session *session.Session
}

// Find 从 S3 下载指定步骤的日志对象流。
func (s *s3store) Find(ctx context.Context, step int64) (io.ReadCloser, error) {
	svc := s3.New(s.session)
	out, err := svc.GetObject(&s3.GetObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(s.key(step)),
	})
	if err != nil {
		return nil, err
	}
	return out.Body, nil
}

// Create 以 private ACL 上传日志到 S3。
func (s *s3store) Create(ctx context.Context, step int64, r io.Reader) error {
	uploader := s3manager.NewUploader(s.session)
	input := &s3manager.UploadInput{
		ACL:    aws.String("private"),
		Bucket: aws.String(s.bucket),
		Key:    aws.String(s.key(step)),
		Body:   r,
	}
	_, err := uploader.Upload(input)
	return err
}

// Update 覆盖上传，语义同 Create。
func (s *s3store) Update(ctx context.Context, step int64, r io.Reader) error {
	return s.Create(ctx, step, r)
}

// Delete 删除 S3 上对应步骤的日志对象。
func (s *s3store) Delete(ctx context.Context, step int64) error {
	svc := s3.New(s.session)
	_, err := svc.DeleteObject(&s3.DeleteObjectInput{
		Bucket: aws.String(s.bucket),
		Key:    aws.String(s.key(step)),
	})
	return err
}

// key 生成 S3 对象键：/{prefix}/{stepID}。
func (s *s3store) key(step int64) string {
	return path.Join("/", s.prefix, fmt.Sprint(step))
}
