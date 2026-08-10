package client

// Compactor HTTP 客户端：通过 REST API 查询删除请求与缓存世代号，
// 适用于无法使用 gRPC 的场景；JobQueue 不支持 HTTP 调用。

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"time"

	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/crypto/tls"

	"github.com/grafana/loki/v3/pkg/compactor/client/grpc"
	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
	"github.com/grafana/loki/v3/pkg/util/log"
)

const (
	orgHeaderKey    = "X-Scope-OrgID"
	getDeletePath   = "/loki/api/v1/delete"
	cacheGenNumPath = "/loki/api/v1/cache/generation_numbers"
)

// HTTPConfig 配置 compactor.client 前缀的 TLS 与连接选项。
type HTTPConfig struct {
	TLSEnabled bool             `yaml:"tls_enabled"`
	TLS        tls.ClientConfig `yaml:",inline"`
}

// RegisterFlags adds the flags required to config this to the given FlagSet.
func (cfg *HTTPConfig) RegisterFlags(f *flag.FlagSet) {
	prefix := "compactor.client"
	f.BoolVar(&cfg.TLSEnabled, prefix+".tls-enabled", false,
		"Enable TLS in the HTTP client. This flag needs to be enabled when any other TLS flag is set. If set to false, insecure connection to HTTP server will be used.")
	cfg.TLS.RegisterFlagsWithPrefix(prefix, f)
}

// compactorHTTPClient 持有 HTTP 客户端及删除请求、缓存世代 URL。
type compactorHTTPClient struct {
	httpClient *http.Client

	deleteRequestsURL *url.URL
	cacheGenURL       string
}

// NewHTTPClient 解析地址、配置连接池与可选 TLS 后返回 HTTP 客户端。
// NewHTTPClient creates a client which talks to compactor over HTTP.
// It uses provided TLS config which creating HTTP client.
func NewHTTPClient(addr string, cfg HTTPConfig) (CompactorClient, error) {
	u, err := url.Parse(addr)
	if err != nil {
		level.Error(log.Logger).Log("msg", "error parsing url", "err", err)
		return nil, err
	}

	deleteRequestsURL := *u
	deleteRequestsURL.Path = getDeletePath

	cacheGenURL := *u
	cacheGenURL.Path = cacheGenNumPath

	transport := http.DefaultTransport.(*http.Transport).Clone()
	transport.MaxIdleConns = 250
	transport.MaxIdleConnsPerHost = 250

	if cfg.TLSEnabled {
		tlsCfg, err := cfg.TLS.GetTLSConfig()
		if err != nil {
			return nil, err
		}

		transport.TLSClientConfig = tlsCfg
	}

	return &compactorHTTPClient{
		httpClient:        &http.Client{Timeout: 5 * time.Second, Transport: transport},
		deleteRequestsURL: &deleteRequestsURL,
		cacheGenURL:       cacheGenURL.String(),
	}, nil
}

func (c *compactorHTTPClient) Name() string {
	return "http_client"
}

func (c *compactorHTTPClient) Stop() {}

// GetAllDeleteRequestsForUser 向 /loki/api/v1/delete 发起 GET 并解析 JSON 响应。
func (c *compactorHTTPClient) GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *deletion.TimeRange) ([]deletionproto.DeleteRequest, error) {
	u := *c.deleteRequestsURL
	q := u.Query()

	if forQuerytimeFiltering {
		q.Set(deletion.ForQuerytimeFilteringQueryParam, "true")
	}

	if timeRange != nil {
		q.Set("start", fmt.Sprintf("%d", timeRange.Start.Unix()))
		q.Set("end", fmt.Sprintf("%d", timeRange.End.Unix()))
	}

	u.RawQuery = q.Encode()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, u.String(), nil)
	if err != nil {
		level.Error(log.Logger).Log("msg", "error getting delete requests from the store", "err", err)
		return nil, err
	}

	req.Header.Set(orgHeaderKey, userID)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		level.Error(log.Logger).Log("msg", "error getting delete requests from the store", "err", err)
		return nil, err
	}
	defer func() {
		_, _ = io.Copy(io.Discard, resp.Body)
		_ = resp.Body.Close()
	}()

	if resp.StatusCode/100 != 2 {
		err := fmt.Errorf("unexpected status code: %d", resp.StatusCode)
		level.Error(log.Logger).Log("msg", "error getting delete requests from the store", "err", err)
		return nil, err
	}

	var deleteRequests []deletionproto.DeleteRequest
	if err := json.NewDecoder(resp.Body).Decode(&deleteRequests); err != nil {
		level.Error(log.Logger).Log("msg", "error marshalling response", "err", err)
		return nil, err
	}

	return deleteRequests, nil
}

// GetCacheGenerationNumber 请求 /loki/api/v1/cache/generation_numbers 端点。
func (c *compactorHTTPClient) GetCacheGenerationNumber(ctx context.Context, userID string) (string, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, c.cacheGenURL, nil)
	if err != nil {
		level.Error(log.Logger).Log("msg", "error getting cache gen numbers from the store", "err", err)
		return "", err
	}

	req.Header.Set(orgHeaderKey, userID)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		level.Error(log.Logger).Log("msg", "error getting cache gen numbers from the store", "err", err)
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		err := fmt.Errorf("unexpected status code: %d", resp.StatusCode)
		level.Error(log.Logger).Log("msg", "error getting cache gen numbers from the store", "err", err)
		return "", err
	}

	var genNumber string
	if err := json.NewDecoder(resp.Body).Decode(&genNumber); err != nil {
		level.Error(log.Logger).Log("msg", "error marshalling response", "err", err)
		return "", err
	}

	return genNumber, err
}

// JobQueueClient HTTP 模式不支持，调用时 panic。
func (c *compactorHTTPClient) JobQueueClient() grpc.JobQueueClient {
	panic("compactor does not support interacting with job queue over HTTP")
}
