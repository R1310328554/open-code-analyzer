package s3

// s3 包封装 S3 及兼容对象存储客户端创建：将 Loki s3.Config 转换为 Thanos objstore S3 配置，含 SSE、HTTP 与存储类元数据。

import (
	"net/http"

	"github.com/go-kit/log"
	"github.com/prometheus/common/model"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/exthttp"
	"github.com/thanos-io/objstore/providers/s3"
)

// awsStorageClassHeader 为 PUT 请求注入 X-Amz-Storage-Class 用户元数据键名。
const (
	// Applied to PUT operations to denote the desired storage class for S3 Objects
	awsStorageClassHeader = "X-Amz-Storage-Class"
)

// NewBucketClient 构建完整读写 Bucket，支持 wrapRT 注入自定义 HTTP 传输。
// NewBucketClient creates a new S3 bucket client
func NewBucketClient(cfg Config, name string, logger log.Logger, wrapRT func(http.RoundTripper) http.RoundTripper) (objstore.Bucket, error) {
	s3Cfg, err := newS3Config(cfg)
	if err != nil {
		return nil, err
	}

	return s3.NewBucketWithConfig(logger, s3Cfg, name, wrapRT)
}

// NewBucketReaderClient 仅创建 BucketReader，用于只需列举/读取的场景。
// NewBucketReaderClient creates a new S3 bucket client
func NewBucketReaderClient(cfg Config, name string, logger log.Logger, wrapRT func(http.RoundTripper) http.RoundTripper) (objstore.BucketReader, error) {
	s3Cfg, err := newS3Config(cfg)
	if err != nil {
		return nil, err
	}

	return s3.NewBucketWithConfig(logger, s3Cfg, name, wrapRT)
}

// newS3Config 组装 Thanos s3.Config：SSE、HTTP TLS、双栈、分片大小与 STS 端点等。
func newS3Config(cfg Config) (s3.Config, error) {
	sseCfg, err := cfg.SSE.BuildThanosConfig()
	if err != nil {
		return s3.Config{}, err
	}

	putUserMetadata := map[string]string{}

	if cfg.StorageClass != "" {
		putUserMetadata[awsStorageClassHeader] = cfg.StorageClass
	}

	return s3.Config{
		Bucket:             cfg.BucketName,
		Endpoint:           cfg.Endpoint,
		Region:             cfg.Region,
		AccessKey:          cfg.AccessKeyID,
		SecretKey:          cfg.SecretAccessKey.String(),
		SessionToken:       cfg.SessionToken.String(),
		Insecure:           cfg.Insecure,
		PutUserMetadata:    putUserMetadata,
		SendContentMd5:     cfg.SendContentMd5,
		SSEConfig:          sseCfg,
		DisableDualstack:   !cfg.DualstackEnabled,
		ListObjectsVersion: cfg.ListObjectsVersion,
		BucketLookupType:   cfg.BucketLookupType,
		AWSSDKAuth:         cfg.NativeAWSAuthEnabled,
		PartSize:           cfg.PartSize,
		HTTPConfig: s3.HTTPConfig{
			IdleConnTimeout:       model.Duration(cfg.HTTP.IdleConnTimeout),
			ResponseHeaderTimeout: model.Duration(cfg.HTTP.ResponseHeaderTimeout),
			InsecureSkipVerify:    cfg.HTTP.InsecureSkipVerify,
			TLSHandshakeTimeout:   model.Duration(cfg.HTTP.TLSHandshakeTimeout),
			ExpectContinueTimeout: model.Duration(cfg.HTTP.ExpectContinueTimeout),
			MaxIdleConns:          cfg.HTTP.MaxIdleConns,
			MaxIdleConnsPerHost:   cfg.HTTP.MaxIdleConnsPerHost,
			MaxConnsPerHost:       cfg.HTTP.MaxConnsPerHost,
			Transport:             cfg.HTTP.Transport,
			TLSConfig: exthttp.TLSConfig{
				CAFile:     cfg.HTTP.TLSConfig.CAPath,
				CertFile:   cfg.HTTP.TLSConfig.CertPath,
				KeyFile:    cfg.HTTP.TLSConfig.KeyPath,
				ServerName: cfg.HTTP.TLSConfig.ServerName,
			},
		},
		TraceConfig: s3.TraceConfig{
			Enable: cfg.TraceConfig.Enabled,
		},
		STSEndpoint: cfg.STSEndpoint,
		MaxRetries:  cfg.MaxRetries,
	}, nil
}
// DisableDualstack 与 DualstackEnabled 语义相反；NativeAWSAuth 启用 SDK 默认凭证链。
