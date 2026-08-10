package iter

// query_client 将 Pattern gRPC Query 流适配为 Iterator：逐批 Recv 响应并委托 Merge 合并多 Series。

import (
	"io"

	"github.com/grafana/loki/v3/pkg/logproto"
)

type queryClientIterator struct {
	client logproto.Pattern_QueryClient
	err    error
	curr   Iterator
}

// NewQueryClientIterator 包装服务端流式 RPC，Next 时自动拉取下一批 QueryPatternsResponse。
// NewQueryClientIterator returns an iterator over a QueryClient.
func NewQueryClientIterator(client logproto.Pattern_QueryClient) Iterator {
	return &queryClientIterator{
		client: client,
	}
}

func (i *queryClientIterator) Next() bool {
	for i.curr == nil || !i.curr.Next() {
		batch, err := i.client.Recv()
		if err == io.EOF {
			return false
		} else if err != nil {
			i.err = err
			return false
		}
		i.curr = NewQueryResponseIterator(batch)
	}

	return true
}

func (i *queryClientIterator) Pattern() string {
	return i.curr.Pattern()
}

func (i *queryClientIterator) Level() string {
	return i.curr.Level()
}

func (i *queryClientIterator) At() logproto.PatternSample {
	return i.curr.At()
}

func (i *queryClientIterator) Err() error {
	return i.err
}

func (i *queryClientIterator) Close() error {
	return i.client.CloseSend()
}

// NewQueryResponseIterator 将单批响应中各 Series 转为 Slice 迭代器再 Merge。
func NewQueryResponseIterator(resp *logproto.QueryPatternsResponse) Iterator {
	iters := make([]Iterator, len(resp.Series))
	for i, s := range resp.Series {
		// todo we should avoid this conversion
		samples := make([]logproto.PatternSample, len(s.Samples))
		for j, sample := range s.Samples {
			samples[j] = *sample
		}
		iters[i] = NewSlice(s.Pattern, s.Level, samples)
	}
	return NewMerge(iters...)
}
// Close 调用 CloseSend 半关闭 gRPC 流，通知服务端客户端不再接收。
