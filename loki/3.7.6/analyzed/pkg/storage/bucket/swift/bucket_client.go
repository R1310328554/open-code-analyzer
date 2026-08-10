package swift

// swift 包封装 OpenStack Swift 容器客户端：将 Loki swift.Config 映射到 Thanos objstore Swift provider 并创建 Bucket。

import (
	"net/http"

	"github.com/go-kit/log"
	"github.com/prometheus/common/model"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/exthttp"
	"github.com/thanos-io/objstore/providers/swift"
)

// NewBucketClient 填充认证、项目域、区域、容器名及 HTTP 配置，调用 NewContainerFromConfig。
// NewBucketClient creates a new Swift bucket client
func NewBucketClient(cfg Config, _ string, logger log.Logger, wrapper func(http.RoundTripper) http.RoundTripper) (objstore.Bucket, error) {
	bucketConfig := swift.Config{
		ApplicationCredentialID:     cfg.ApplicationCredentialID,
		ApplicationCredentialName:   cfg.ApplicationCredentialName,
		ApplicationCredentialSecret: cfg.ApplicationCredentialSecret.String(),
		AuthVersion:                 cfg.AuthVersion,
		AuthUrl:                     cfg.AuthURL,
		Username:                    cfg.Username,
		UserDomainName:              cfg.UserDomainName,
		UserDomainID:                cfg.UserDomainID,
		UserId:                      cfg.UserID,
		Password:                    cfg.Password.String(),
		DomainId:                    cfg.DomainID,
		DomainName:                  cfg.DomainName,
		ProjectID:                   cfg.ProjectID,
		ProjectName:                 cfg.ProjectName,
		ProjectDomainID:             cfg.ProjectDomainID,
		ProjectDomainName:           cfg.ProjectDomainName,
		RegionName:                  cfg.RegionName,
		ContainerName:               cfg.ContainerName,
		Retries:                     cfg.MaxRetries,
		ConnectTimeout:              model.Duration(cfg.ConnectTimeout),
		Timeout:                     model.Duration(cfg.RequestTimeout),
		HTTPConfig: exthttp.HTTPConfig{
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

// ChunkSize 与 UseDynamicLargeObjects 使用 Thanos 默认分块大小，DLO 在此禁用。
		// Hard-coded defaults.
		ChunkSize:              swift.DefaultConfig.ChunkSize,
		UseDynamicLargeObjects: false,
	}

	return swift.NewContainerFromConfig(logger, &bucketConfig, false, wrapper)
}
// ConnectTimeout 与 RequestTimeout 转为 model.Duration 传入 Thanos Swift HTTP 配置。
