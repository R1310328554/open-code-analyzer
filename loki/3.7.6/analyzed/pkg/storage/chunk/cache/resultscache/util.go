package resultscache

// util 提供结果缓存中间件的辅助类型与并行请求执行器，供 DoRequests 在有限并发下批量调用下游 Handler。

import (
	"context"
)

type HandlerFunc func(context.Context, Request) (Response, error)

// Do 直接调用底层函数，无额外包装开销。
// Do implements Handler.
func (q HandlerFunc) Do(ctx context.Context, req Request) (Response, error) {
	return q(ctx, req)
}

// RequestResponse 配对保存请求与响应，便于缓存层合并或回填结果。
// RequestResponse contains a request response and the respective request that was used.
type RequestResponse struct {
	Request  Request
	Response Response
}

// DoRequests 用 bounded channel 限制并行度；任一失败则 cancel 其余请求并返回首个错误。
// DoRequests executes a list of requests in parallel.
func DoRequests(ctx context.Context, downstream Handler, reqs []Request, parallelism int) ([]RequestResponse, error) {
	// If one of the requests fail, we want to be able to cancel the rest of them.
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()

	// Feed all requests to a bounded intermediate channel to limit parallelism.
	intermediate := make(chan Request)
	go func() {
		for _, req := range reqs {
			intermediate <- req
		}
		close(intermediate)
	}()

	respChan, errChan := make(chan RequestResponse), make(chan error)
	if parallelism > len(reqs) {
		parallelism = len(reqs)
	}
	for i := 0; i < parallelism; i++ {
		go func() {
			for req := range intermediate {
				resp, err := downstream.Do(ctx, req)
				if err != nil {
					errChan <- err
				} else {
					respChan <- RequestResponse{req, resp}
				}
			}
		}()
	}

	resps := make([]RequestResponse, 0, len(reqs))
	var firstErr error
	for range reqs {
		select {
		case resp := <-respChan:
			resps = append(resps, resp)
		case err := <-errChan:
			if firstErr == nil {
				cancel()
				firstErr = err
			}
		}
	}

	return resps, firstErr
}
// parallelism 超过请求数时自动截断；worker 从 intermediate 取任务写入 respChan 或 errChan。
