package util //nolint:revive

// util 包封装索引查询并行化、目录创建与带 context 取消的 ReadCloser 包装等存储层辅助逻辑。

import (
	"context"
	"fmt"
	"io"
	"io/fs"
	"os"

	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

// DoSingleQuery 表示尚不支持批量查询的索引后端单次查询回调签名。
// DoSingleQuery is the interface for indexes that don't support batching yet.
type DoSingleQuery func(context.Context, index.Query, index.QueryPagesCallback) error

// QueryParallelism 限制 DoParallelQueries 同时运行的子查询 goroutine 上限，默认 100。
// QueryParallelism is the maximum number of subqueries run in
// parallel per higher-level query
var QueryParallelism = 100

// DoParallelQueries translates between our interface for query batching,
// and indexes that don't yet support batching.
// DoParallelQueries 将多路 index.Query 分发到 worker 池，单查询时直接调用 doSingleQuery。
func DoParallelQueries(
	ctx context.Context, doSingleQuery DoSingleQuery, queries []index.Query,
	callback index.QueryPagesCallback,
) error {
	if len(queries) == 1 {
		return doSingleQuery(ctx, queries[0], callback)
	}

	queue := make(chan index.Query)
	incomingErrors := make(chan error)
	n := min(len(queries), QueryParallelism)
	// Run n parallel goroutines fetching queries from the queue
	for i := 0; i < n; i++ {
		go func() {
			ctx, sp := tracer.Start(ctx, "DoParallelQueries-worker")
			defer sp.End()
			for {
				query, ok := <-queue
				if !ok {
					return
				}
				incomingErrors <- doSingleQuery(ctx, query, callback)
			}
		}()
	}
	// Send all the queries into the queue
	go func() {
		for _, query := range queries {
			queue <- query
		}
		close(queue)
	}()

	// Now receive all the results.
	var lastErr error
	for i := 0; i < len(queries); i++ {
		err := <-incomingErrors
		if err != nil {
			lastErr = err
		}
	}
	return lastErr
}

// EnsureDirectory makes sure directory is there, if not creates it if not
// EnsureDirectory 确保目录存在，不存在则以 0o777 权限递归创建。
func EnsureDirectory(dir string) error {
	return EnsureDirectoryWithDefaultPermissions(dir, 0o777)
}

func EnsureDirectoryWithDefaultPermissions(dir string, mode fs.FileMode) error {
	info, err := os.Stat(dir)
	if os.IsNotExist(err) {
		return os.MkdirAll(dir, mode)
	} else if err == nil && !info.IsDir() {
		return fmt.Errorf("not a directory: %s", dir)
	}
	return err
}

// RequirePermissions 校验路径权限位是否包含 required 要求的模式。
func RequirePermissions(path string, required fs.FileMode) error {
	info, err := os.Stat(path)
	if err != nil {
		return err
	}

	if mode := info.Mode(); mode&required != required {
		return fmt.Errorf("insufficient permissions for path %s: required %s but found %s", path, required.String(), mode.String())
	}
	return nil
}

// ReadCloserWithContextCancelFunc helps with cancelling the context when closing a ReadCloser.
// NOTE: The consumer of ReadCloserWithContextCancelFunc should always call the Close method when it is done reading which otherwise could cause a resource leak.
// ReadCloserWithContextCancelFunc 在 Close 时调用 cancel，防止 context 泄漏。
type ReadCloserWithContextCancelFunc struct {
	io.ReadCloser
	cancel context.CancelFunc
}

func NewReadCloserWithContextCancelFunc(readCloser io.ReadCloser, cancel context.CancelFunc) io.ReadCloser {
	return ReadCloserWithContextCancelFunc{
		ReadCloser: readCloser,
		cancel:     cancel,
	}
}

func (r ReadCloserWithContextCancelFunc) Close() error {
	defer r.cancel()
	return r.ReadCloser.Close()
}
// 调用方必须在读完 ReadCloser 后 Close，否则 cancel 不会触发导致资源泄漏。
