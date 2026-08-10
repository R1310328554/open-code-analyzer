package queryrangebase

// util 提供 queryrangebase 并行请求执行与结果缓存合并适配工具，供 frontend 中间件链并发调用下游 Handler。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/storage/chunk/cache/resultscache"
)

// RequestResponse 将单次下游请求与其响应对应保存，便于并行收集后按序合并。
// RequestResponse contains a request response and the respective request that was used.
type RequestResponse struct {
	Request  Request
	Response Response
}

// DoRequests 通过有界 worker 池并行执行多个 Request，任一失败则取消其余子请求。
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

// queryMergerAsCacheResponseMerger 将 queryrange Merger 适配为 resultscache.ResponseMerger。
type queryMergerAsCacheResponseMerger struct {
	Merger
}

func (m *queryMergerAsCacheResponseMerger) MergeResponse(responses ...resultscache.Response) (resultscache.Response, error) {
	cacheResponses := make([]Response, 0, len(responses))
	for _, r := range responses {
		cacheResponses = append(cacheResponses, r.(Response))
	}
	response, err := m.Merger.MergeResponse(cacheResponses...)
	if err != nil {
		return nil, err
	}
	return response.(resultscache.Response), nil
}

// FromQueryResponseMergerToCacheResponseMerger 供 results cache 中间件复用查询合并逻辑。
func FromQueryResponseMergerToCacheResponseMerger(m Merger) resultscache.ResponseMerger {
	return &queryMergerAsCacheResponseMerger{m}
}
// parallelism 上限为 len(reqs)，避免为少量请求启动过多 goroutine。
