// registry blob 并行下载：Range 续传、stall/慢速检测与 CDN 重定向解析。
package transfer

import (
	"cmp"
	"context"
	"crypto/sha256"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"slices"
	"sync"
	"sync/atomic"
	"time"

	"golang.org/x/sync/errgroup"
	"golang.org/x/sync/semaphore"
)

var (
	errStalled = errors.New("download stalled")
	errSlow    = errors.New("download too slow")
)

type downloader struct {
	client       *http.Client
	baseURL      string
	destDir      string
	repository   string // blob URL 仓库路径
	// Repository path for blob URLs (e.g., "library/model")
	tokenMu      sync.RWMutex
	token        string
	getToken     func(context.Context, AuthChallenge) (string, error)
	userAgent    string
	stallTimeout time.Duration
	progress     *progressTracker
	speeds       *speedTracker
	logger       *slog.Logger
	// bodySem 限制同时读取响应体的连接数，避免占满家庭带宽
	// bodySem caps the number of simultaneous body-bearing transfers so a
	// modest home downlink isn't saturated. Always set by download(); nil
	// only when tests build downloader directly (in which case holdBody is
	// a no-op).
	bodySem *semaphore.Weighted
}

// authToken 返回当前 Bearer token，可与 refreshToken 并发调用。
// authToken returns the current bearer token. Safe to call concurrently with
// refreshToken.
func (d *downloader) authToken() string {
	d.tokenMu.RLock()
	defer d.tokenMu.RUnlock()
	return d.token
}

// refreshToken 合并并发 401 的 token 刷新，避免惊群。
// refreshToken coalesces token fetches so concurrent 401s don't all hit the
// auth server. prev is the token the caller used in the request that got
// rejected: if the stored token has already moved past prev, another
// goroutine has refreshed and we just observe its result; otherwise the
// caller holds the lock and performs the fetch.
func (d *downloader) refreshToken(ctx context.Context, ch AuthChallenge, prev string) error {
	d.tokenMu.Lock()
	defer d.tokenMu.Unlock()
	if d.token != prev {
		return nil
	}
	if d.getToken == nil {
		return errors.New("no token refresh callback")
	}
	t, err := d.getToken(ctx, ch)
	if err != nil {
		return err
	}
	d.token = t
	return nil
}

// holdBody 获取 body 传输槽位；返回的 release 必须在请求结束后调用一次。
// holdBody acquires a body-transfer slot. The returned release must be
// called exactly once after the body-bearing request completes (defer is fine).
func (d *downloader) holdBody(ctx context.Context) (func(), error) {
	if d.bodySem == nil {
		return func() {}, nil
	}
	if err := d.bodySem.Acquire(ctx, 1); err != nil {
		return nil, err
	}
	return func() { d.bodySem.Release(1) }, nil
}

// download 过滤已存在 blob 后并行下载剩余项。
func download(ctx context.Context, opts DownloadOptions) error {
	if len(opts.Blobs) == 0 {
		return nil
	}

	// 统计全部 blob 总字节以正确报告续传进度
	// Calculate total from all blobs (for accurate progress reporting on resume)
	var total int64
	for _, b := range opts.Blobs {
		total += b.Size
	}

	// 跳过本地已完整存在的 blob
	// Filter out already-downloaded blobs and track completed bytes
	var blobs []Blob
	var alreadyCompleted int64
	for _, b := range opts.Blobs {
		if fi, _ := os.Stat(filepath.Join(opts.DestDir, digestToPath(b.Digest))); fi != nil && fi.Size() == b.Size {
			if opts.Logger != nil {
				opts.Logger.Debug("blob already exists", "digest", b.Digest, "size", b.Size)
			}
			alreadyCompleted += b.Size
			continue
		}
		blobs = append(blobs, b)
	}
	if len(blobs) == 0 {
		return nil
	}

	progress := newProgressTracker(total, opts.Progress)
	progress.add(alreadyCompleted) // 先将已存在字节计入进度
	// Report already-downloaded bytes upfront

	d := &downloader{
		client:       cmp.Or(opts.Client, defaultClient),
		baseURL:      opts.BaseURL,
		destDir:      opts.DestDir,
		repository:   cmp.Or(opts.Repository, "library/_"),
		token:        opts.Token,
		getToken:     opts.GetToken,
		userAgent:    cmp.Or(opts.UserAgent, defaultUserAgent),
		stallTimeout: cmp.Or(opts.StallTimeout, defaultStallTimeout),
		progress:     progress,
		speeds:       &speedTracker{},
		logger:       opts.Logger,
	}
	// BodyConcurrency≤0 时串行 body 传输
	// 0 or negative serializes; never unbounded.
	d.bodySem = semaphore.NewWeighted(int64(max(1, opts.BodyConcurrency)))

	concurrency := cmp.Or(opts.Concurrency, DefaultDownloadConcurrency)
	sem := semaphore.NewWeighted(int64(concurrency))

	start := time.Now()
	g, ctx := errgroup.WithContext(ctx)
	for _, blob := range blobs {
		g.Go(func() error {
			if err := sem.Acquire(ctx, 1); err != nil {
				return err
			}
			defer sem.Release(1)
			return d.download(ctx, blob)
		})
	}
	err := g.Wait()
	elapsed := time.Since(start)
	done := d.progress.completed.Load() - alreadyCompleted
	mbps := float64(done) / 1e6 / max(0.001, elapsed.Seconds())
	slog.Debug("download summary",
		"blobs", len(blobs),
		"bytes", done,
		"duration", elapsed.Round(time.Millisecond),
		"mb_per_sec", fmt.Sprintf("%.1f", mbps),
		"max_transfers", max(1, opts.BodyConcurrency),
	)
	return err
}

// download 单 blob 重试循环：stall 不计次、慢速三次后计次。
func (d *downloader) download(ctx context.Context, blob Blob) error {
	var lastErr error
	var slowRetries int
	attempt := 0

	for attempt < maxRetries {
		if attempt > 0 {
			if err := backoff(ctx, attempt, time.Second<<uint(attempt-1)); err != nil {
				return err
			}
		}

		start := time.Now()
		n, err := d.downloadOnce(ctx, blob)
		if err == nil {
			// 小 blob 不计速度样本，避免 HTTP 开销污染中位数
			// Skip speed recording for tiny blobs — their transfer time is
			// dominated by HTTP overhead, not throughput, and would pollute
			// the median used for stall detection.
			if blob.Size >= smallBlobSpeedThreshold {
				if s := time.Since(start).Seconds(); s > 0 {
					d.speeds.record(float64(blob.Size) / s)
				}
			}
			return nil
		}

		d.progress.add(-n) // rollback

		// 大 blob 失败保留 .tmp 以便 Range 续传
		// Preserve partial .tmp files for large blobs to enable resume
		if blob.Size < resumeThreshold {
			dest := filepath.Join(d.destDir, digestToPath(blob.Digest))
			os.Remove(dest + ".tmp")
		}

		switch {
		case errors.Is(err, context.Canceled), errors.Is(err, context.DeadlineExceeded):
			return err
		case errors.Is(err, errStalled):
			// stall 重试不计入 attempt 上限
			// Don't count stall retries against limit
		case errors.Is(err, errSlow):
			if slowRetries++; slowRetries >= 3 {
				attempt++ // Only count after 3 slow retries
			}
		default:
			attempt++
		}
		lastErr = err
	}
	return fmt.Errorf("%w: %v", errMaxRetriesExceeded, lastErr)
}

// downloadOnce 执行单次 GET：解析 URL、Range 续传、流式写入。
func (d *downloader) downloadOnce(ctx context.Context, blob Blob) (int64, error) {
	if d.logger != nil {
		d.logger.Debug("downloading blob", "digest", blob.Digest, "size", blob.Size)
	}

	// 持有 body 槽位直至响应体读完
	// Hold a body slot for the duration of the GET — released when the body
	// has been read and the response closed.
	release, err := d.holdBody(ctx)
	if err != nil {
		return 0, err
	}
	defer release()

	baseURL, _ := url.Parse(d.baseURL)
	u, err := d.resolve(ctx, fmt.Sprintf("%s/v2/%s/blobs/%s", d.baseURL, d.repository, blob.Digest))
	if err != nil {
		return 0, err
	}

	// 检查 .tmp 部分文件以决定 Range 起点
	// Check for existing partial .tmp file for resume
	dest := filepath.Join(d.destDir, digestToPath(blob.Digest))
	tmp := dest + ".tmp"
	var existingSize int64
	if blob.Size >= resumeThreshold {
		if fi, statErr := os.Stat(tmp); statErr == nil {
			if fi.Size() < blob.Size {
				existingSize = fi.Size()
			} else if fi.Size() > blob.Size {
				// .tmp 大于预期则删除重新下载
				// .tmp larger than expected — discard
				os.Remove(tmp)
			}
			// fi.Size() == blob.Size handled in save (hash check + rename)
		}
	}

	req, _ := http.NewRequestWithContext(ctx, http.MethodGet, u.String(), nil)
	req.Header.Set("User-Agent", d.userAgent)
	// 仅对 registry 同源请求附加 Authorization
	// Add auth only for same-host (not CDN)
	if u.Host == baseURL.Host {
		if t := d.authToken(); t != "" {
			req.Header.Set("Authorization", "Bearer "+t)
		}
	}
	if existingSize > 0 {
		req.Header.Set("Range", fmt.Sprintf("bytes=%d-", existingSize))
	}

	resp, err := d.client.Do(req)
	if err != nil {
		return 0, err
	}
	defer func() { io.Copy(io.Discard, resp.Body); resp.Body.Close() }()

	switch resp.StatusCode {
	case http.StatusOK:
		// 200 全量响应，忽略已有 partial
		// Full response — reset any partial state
		existingSize = 0
	case http.StatusPartialContent:
		// 206 续传成功
		// Resume succeeded
	default:
		return 0, fmt.Errorf("status %d", resp.StatusCode)
	}

	return d.save(ctx, blob, resp.Body, existingSize)
}

// save 流式写入 .tmp、校验 digest 后 rename 为最终路径。
func (d *downloader) save(ctx context.Context, blob Blob, r io.Reader, existingSize int64) (int64, error) {
	dest := filepath.Join(d.destDir, digestToPath(blob.Digest))
	tmp := dest + ".tmp"
	os.MkdirAll(filepath.Dir(dest), 0o755)

	h := sha256.New()

	var f *os.File
	var err error

	if existingSize > 0 {
		// 续传：先哈希已有 partial 再追加新数据
		// Resume — re-hash existing partial data, then append
		f, err = os.OpenFile(tmp, os.O_RDWR, 0o644)
		if err != nil {
			// 无法打开 partial 则从头开始
			// Can't open partial file, start fresh
			existingSize = 0
		} else {
			// Hash the existing data
			if _, hashErr := io.CopyN(h, f, existingSize); hashErr != nil {
				f.Close()
				os.Remove(tmp)
				existingSize = 0
			} else {
				// 将已存在 partial 字节计入进度
				// Report resumed bytes as progress
				d.progress.add(existingSize)
			}
		}
	}

	if existingSize == 0 {
		f, err = os.Create(tmp)
		if err != nil {
			return 0, err
		}
		setSparse(f)
	}
	defer f.Close()

	n, err := d.copy(ctx, f, r, h)
	if err != nil {
		// 此处不删 .tmp，由 download 按 blob 大小决定清理
		// Don't remove .tmp here — download() handles cleanup based on blob size
		return existingSize + n, err
	}
	f.Close()

	if got := fmt.Sprintf("sha256:%x", h.Sum(nil)); got != blob.Digest {
		os.Remove(tmp)
		return existingSize + n, fmt.Errorf("digest mismatch")
	}
	totalWritten := existingSize + n
	if totalWritten != blob.Size {
		os.Remove(tmp)
		return totalWritten, fmt.Errorf("size mismatch")
	}
	return totalWritten, os.Rename(tmp, dest)
}

// copy 带 stall/慢速监控的流式拷贝。
func (d *downloader) copy(ctx context.Context, dst io.Writer, src io.Reader, h io.Writer) (int64, error) {
	var n int64
	var lastRead atomic.Int64
	lastRead.Store(time.Now().UnixNano())
	start := time.Now()

	ctx, cancel := context.WithCancelCause(ctx)
	defer cancel(nil)

	go func() {
		tick := time.NewTicker(time.Second)
		defer tick.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-tick.C:
				if time.Since(time.Unix(0, lastRead.Load())) > d.stallTimeout {
					cancel(errStalled)
					return
				}
				if e := time.Since(start); e > 5*time.Second {
					if m := d.speeds.median(); m > 0 && float64(n)/e.Seconds() < m*0.1 {
						cancel(errSlow)
						return
					}
				}
			}
		}
	}()

	buf := make([]byte, 32*1024)
	for {
		if err := ctx.Err(); err != nil {
			if c := context.Cause(ctx); c != nil {
				return n, c
			}
			return n, err
		}

		nr, err := src.Read(buf)
		if nr > 0 {
			lastRead.Store(time.Now().UnixNano())
			dst.Write(buf[:nr])
			h.Write(buf[:nr])
			d.progress.add(int64(nr))
			n += int64(nr)
		}
		if err == io.EOF {
			return n, nil
		}
		if err != nil {
			return n, err
		}
	}
}

// resolve 跟随重定向解析最终下载 URL（GET 触发 CDN 跳转）。
// resolve follows redirects to find the final download URL.
// Uses GET (not HEAD) because registries may return 200 for HEAD without
// redirecting to CDN, while GET triggers the actual CDN redirect.
func (d *downloader) resolve(ctx context.Context, rawURL string) (*url.URL, error) {
	u, _ := url.Parse(rawURL)
	for range 10 {
		req, _ := http.NewRequestWithContext(ctx, http.MethodGet, u.String(), nil)
		req.Header.Set("User-Agent", d.userAgent)
		prev := d.authToken()
		if prev != "" {
			req.Header.Set("Authorization", "Bearer "+prev)
		}

		resp, err := d.client.Do(req)
		if err != nil {
			return nil, err
		}
		// 关闭前排空 body 以复用连接
		// Drain body before close to enable HTTP connection reuse
		io.Copy(io.Discard, resp.Body)
		resp.Body.Close()

		switch resp.StatusCode {
		case http.StatusOK:
			return u, nil
		case http.StatusUnauthorized:
			if d.getToken == nil {
				return nil, fmt.Errorf("unauthorized")
			}
			ch := parseAuthChallenge(resp.Header.Get("WWW-Authenticate"))
			if err := d.refreshToken(ctx, ch, prev); err != nil {
				return nil, err
			}
		case http.StatusTemporaryRedirect, http.StatusFound, http.StatusMovedPermanently:
			loc, _ := resp.Location()
			if loc.Host != u.Host {
				return loc, nil
			}
			u = loc
		default:
			return nil, fmt.Errorf("status %d", resp.StatusCode)
		}
	}
	return nil, fmt.Errorf("too many redirects")
}

type speedTracker struct {
	mu     sync.Mutex
	speeds []float64
}

// record 记录单次 blob 吞吐样本（滑动窗口最多 30 条）。
func (s *speedTracker) record(v float64) {
	s.mu.Lock()
	s.speeds = append(s.speeds, v)
	if len(s.speeds) > 30 {
		s.speeds = s.speeds[1:]
	}
	s.mu.Unlock()
}

// median 返回速度样本中位数（至少 5 个样本才有效）。
func (s *speedTracker) median() float64 {
	s.mu.Lock()
	defer s.mu.Unlock()
	if len(s.speeds) < 5 {
		return 0
	}
	sorted := make([]float64, len(s.speeds))
	copy(sorted, s.speeds)
	slices.Sort(sorted)
	return sorted[len(sorted)/2]
}

const defaultStallTimeout = 10 * time.Second
