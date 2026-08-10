package congestion

// interfaces 定义拥塞控制核心抽象：Controller 包装 ObjectClient 并协调重试与 hedge；Retrier 负责 GetObject 重试；Hedger 为慢请求提供并行副本请求能力。

import (
	"io"
	"net/http"

	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/hedging"
)

// Controller 通过判断是否可重试、施加背压并集中管理 Retrier/Hedger 来处理存储拥塞。
// Controller handles congestion by:
// - determining if calls to object storage can be retried
// - defining and enforcing a back-pressure mechanism
// - centralising retries & hedging
type Controller interface {
	client.ObjectClient

	// Wrap wraps a given object store client and handles congestion against its backend service
	Wrap(client client.ObjectClient) client.ObjectClient

	withLogger(log.Logger) Controller
	withRetrier(Retrier) Controller
	withHedger(Hedger) Controller
	withMetrics(*Metrics) Controller

	getRetrier() Retrier
	getHedger() Hedger
	getMetrics() *Metrics
}

// DoRequestFunc 封装单次 GetObject 尝试，attempt 为当前尝试序号（含重试）。
type DoRequestFunc func(attempt int) (io.ReadCloser, int64, error)
type IsRetryableErrFunc func(err error) bool

// Retrier 编排 GetObject 请求及后续重试；count 参数 0 表示首次尝试，正数表示第几次重试。
// Retrier orchestrates requests & subsequent retries (if configured).
// NOTE: this only supports ObjectClient.GetObject calls right now.
type Retrier interface {
	// Do executes a given function which is expected to be a GetObject call, and its return signature matches that.
	// Any failed requests will be retried.
	//
	// count is the current request count; any positive number indicates retries, 0 indicates first attempt.
	Do(fn DoRequestFunc, isRetryable IsRetryableErrFunc, onSuccess func(), onError func()) (io.ReadCloser, int64, error)

	withLogger(log.Logger) Retrier
}

// Hedger 在旧请求超时时发起新请求并返回先完成的响应；建议不对重试请求做 hedge。
// Hedger orchestrates request "hedging", which is the process of sending a new request when the old request is
// taking too long, and returning the response that is received first
type Hedger interface {
	// HTTPClient returns an HTTP client which is responsible for handling both the initial and all hedged requests.
	// It is recommended that retries are not hedged.
	// Bear in mind this function can be called several times, and should return the same client each time.
	HTTPClient(cfg hedging.Config) (*http.Client, error)

	withLogger(log.Logger) Hedger
}
// IsRetryableErrFunc 由调用方提供，区分可重试的服务端错误与应立即失败的客户端错误。
