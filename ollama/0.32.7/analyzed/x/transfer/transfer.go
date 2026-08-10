// Package transfer 为张量模型提供轻量、高并发的 blob 传输（x/ 实验包）。
// Package transfer provides minimal, fast blob transfer for tensor-based models.
//
// 位于 x/ 因张量存储格式仍在演进；稳定后拟并入 server/download 与 upload。
// This package is in x/ because the tensor model storage format is under development.
// It provides optimized transfer for models with many small blobs (tensor models)
// rather than few large blobs (typical LLMs).
//
// TODO (jmorganca): Integrate into server/download.go and server/upload.go when stable.
//
// 设计要点：
// Design Philosophy:
// This package is intentionally simpler than the main server's download/upload
// code. Key simplifications for many-small-blob workloads:
//
//   - Whole-blob downloads: Each blob downloads as one unit, with HTTP Range
//     resume for blobs >= 64MB on retry; small blobs restart from scratch.
//   - Whole-blob uploads by default: A single PUT per blob. When the server
//     returns a direct-upload URL the body goes straight to the storage
//     backend; otherwise the body goes to the registry in one shot.
//   - Multi-part upload fallback: If the server requires it, blobs are split
//     into parts and sent via PATCH with a finalize PUT carrying a composite
//     etag. This is a server-side compatibility path, not the fast path.
//   - Inline hashing: digests computed during streaming.
//   - Stall and speed detection (downloads): cancels on no data (stall) or
//     speed below 10% of median.
//
// For large models (multi-GB), use the server's download/upload code which
// has resumable downloads with JSON state files, async hashing from OS page
// cache, and per-part speed tracking with rolling median.
package transfer

import (
	"context"
	"errors"
	"log/slog"
	"math/rand/v2"
	"net/http"
	"strings"
	"sync/atomic"
	"time"
)

// Blob 表示待传输的内容寻址 blob（sha256 摘要 + 大小）。
// Blob represents a content-addressed blob to transfer.
type Blob struct {
	Digest string // sha256:...
	Size   int64

	// From 启用跨仓库 blob 挂载（仅上传）；可避免重复上传已存在 blob。
	// From enables cross-repository blob mounting (upload only).
	// When set, the upload will first attempt to mount the blob from this source
	// repository instead of uploading the data. This is a Docker Registry v2 API
	// feature that avoids re-uploading blobs that already exist elsewhere.
	//
	// Example: From="library/source-model" will add ?mount=<digest>&from=library/source-model
	// to the POST /blobs/uploads/ request. If the registry returns 201 Created,
	// the blob was mounted successfully and no upload is needed.
	//
	// See: https://distribution.github.io/distribution/spec/api/#cross-repository-blob-mount
	From string
}

// DownloadOptions 配置并行下载参数。
// DownloadOptions configures a parallel download operation.
type DownloadOptions struct {
	Blobs           []Blob                                                             // 待下载 blob 列表
	// Blobs to download
	BaseURL         string                                                             //  registry 根 URL
	// Registry base URL
	DestDir         string                                                             // blob 落盘目录
	// Destination directory for blobs
	Repository      string                                                             // blob URL 仓库路径
	// Repository path for blob URLs (e.g., "library/model")
	Concurrency     int                                                                // 最大并行下载数
	// Max parallel downloads (default DefaultDownloadConcurrency)
	BodyConcurrency int                                                                // 同时传输响应体的上限
	// Max simultaneous body-bearing transfers; 0 or negative serializes (capacity 1)
	Progress        func(completed, total int64)                                       // Progress callback (optional)
	Client          *http.Client                                                       // HTTP client (optional, uses default)
	Token           string                                                             // Auth token (optional)
	GetToken        func(ctx context.Context, challenge AuthChallenge) (string, error) // Token refresh callback
	Logger          *slog.Logger                                                       // Optional structured logger
	UserAgent       string                                                             // User-Agent header (optional, has default)
	StallTimeout    time.Duration                                                      // Timeout for stall detection (default 10s)
}

// UploadOptions 配置并行上传参数。
// UploadOptions configures a parallel upload operation.
type UploadOptions struct {
	Blobs           []Blob                                                             // Blobs to upload
	BaseURL         string                                                             // Registry base URL
	SrcDir          string                                                             // Source directory containing blobs
	Concurrency     int                                                                // Max parallel uploads (default DefaultUploadConcurrency)
	BodyConcurrency int                                                                // Max simultaneous body-bearing transfers; 0 or negative serializes (capacity 1)
	Progress        func(completed, total int64)                                       // Progress callback (optional)
	Client          *http.Client                                                       // HTTP client (optional, uses default)
	Token           string                                                             // Auth token (optional)
	GetToken        func(ctx context.Context, challenge AuthChallenge) (string, error) // Token refresh callback
	Logger          *slog.Logger                                                       // Optional structured logger
	UserAgent       string                                                             // User-Agent header (optional, has default)

	// 可选 manifest：全部 blob 完成后推送
	// Manifest fields (optional) - if set, manifest is pushed after all blobs complete
	Manifest    []byte // Raw manifest JSON to push
	ManifestRef string // Tag or digest for the manifest (e.g., "latest", "sha256:...")
	Repository  string // Repository path for manifest URL (e.g., "library/model")
}

// AuthChallenge 表示解析后的 WWW-Authenticate Bearer 挑战。
// AuthChallenge represents a parsed WWW-Authenticate challenge.
type AuthChallenge struct {
	Realm   string
	Service string
	Scope   string
}

// 默认并发、重试与 resume 阈值常量。
// Default concurrency limits and settings
const (
	DefaultDownloadConcurrency = 64
	DefaultUploadConcurrency   = 64
	maxRetries                 = 6
	defaultUserAgent           = "ollama-transfer/1.0"

	// resumeThreshold：≥64MB 失败时保留 .tmp 以支持 Range 续传
	// resumeThreshold is the minimum blob size for resume support.
	// Only blobs above this size keep partial .tmp files on failure.
	resumeThreshold = 64 << 20 // 64 MB

	// smallBlobSpeedThreshold：小于 100KB 的 blob 不计入速度中位数
	// smallBlobSpeedThreshold is the size below which speed samples are skipped,
	// since their transfer time is dominated by HTTP overhead, not throughput.
	smallBlobSpeedThreshold = 100 << 10 // 100 KB
)

var errMaxRetriesExceeded = errors.New("max retries exceeded")

// defaultClient 共享 HTTP 客户端，禁用自动重定向以便手动解析 CDN。
// defaultClient is a shared HTTP client with connection pooling.
var defaultClient = &http.Client{
	Transport: &http.Transport{
		MaxIdleConns:        100,
		MaxIdleConnsPerHost: 100,
		IdleConnTimeout:     90 * time.Second,
	},
	CheckRedirect: func(req *http.Request, via []*http.Request) error {
		return http.ErrUseLastResponse
	},
}

// progressTracker 聚合多 goroutine 的字节进度并回调。
// progressTracker aggregates progress across concurrent operations.
type progressTracker struct {
	completed atomic.Int64
	total     int64
	callback  func(completed, total int64)
}

// newProgressTracker 创建带总量与回调的进度跟踪器。
func newProgressTracker(total int64, callback func(completed, total int64)) *progressTracker {
	return &progressTracker{
		total:    total,
		callback: callback,
	}
}

// add 原子累加已完成字节并触发回调（panic 安全）。
func (p *progressTracker) add(n int64) {
	if p == nil || p.callback == nil {
		return
	}
	completed := p.completed.Add(n)
	defer func() {
		if r := recover(); r != nil {
			slog.Debug("progress callback panic (likely closed channel)", "recovered", r)
		}
	}()
	p.callback(completed, p.total)
}

// Download 并行下载 blob，流式校验 sha256。
// Download downloads blobs in parallel with streaming hash verification.
func Download(ctx context.Context, opts DownloadOptions) error {
	return download(ctx, opts)
}

// Upload 并行上传 blob，可选最后推送 manifest。
// Upload uploads blobs in parallel.
func Upload(ctx context.Context, opts UploadOptions) error {
	return upload(ctx, opts)
}

// digestToPath 将 sha256:hex 转为文件系统路径 sha256-hex。
// digestToPath converts sha256:abc123 to sha256-abc123
func digestToPath(digest string) string {
	if len(digest) > 7 && digest[6] == ':' {
		return digest[:6] + "-" + digest[7:]
	}
	return digest
}

// parseAuthChallenge 解析 Bearer realm/service/scope。
// parseAuthChallenge parses a WWW-Authenticate header value.
// Example: Bearer realm="https://auth.example.com",service="registry",scope="repository:foo:pull"
func parseAuthChallenge(header string) AuthChallenge {
	header = strings.TrimPrefix(header, "Bearer ")

	getValue := func(key string) string {
		startIdx := strings.Index(header, key+"=")
		if startIdx == -1 {
			return ""
		}
		startIdx += len(key) + 1
		if startIdx >= len(header) {
			return ""
		}

		// 处理引号包裹的值
		// Handle quoted values
		if header[startIdx] == '"' {
			startIdx++
			endIdx := strings.Index(header[startIdx:], "\"")
			if endIdx == -1 {
				return header[startIdx:]
			}
			return header[startIdx : startIdx+endIdx]
		}

		// 无引号值以逗号或字符串结尾
		// Unquoted value - ends at comma or end of string
		endIdx := strings.Index(header[startIdx:], ",")
		if endIdx == -1 {
			return header[startIdx:]
		}
		return header[startIdx : startIdx+endIdx]
	}

	return AuthChallenge{
		Realm:   getValue("realm"),
		Service: getValue("service"),
		Scope:   getValue("scope"),
	}
}

// backoff 在重试前执行带抖动的指数退避等待。
// backoff returns a function that sleeps with exponential backoff.
func backoff(ctx context.Context, attempt int, maxBackoff time.Duration) error {
	if ctx.Err() != nil {
		return ctx.Err()
	}

	// n² 退避并加随机抖动
	// n^2 backoff with jitter
	d := min(time.Duration(attempt*attempt)*10*time.Millisecond, maxBackoff)
	d = time.Duration(float64(d) * (rand.Float64() + 0.5))

	t := time.NewTimer(d)
	defer t.Stop()
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-t.C:
		return nil
	}
}
