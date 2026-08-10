package generator

// stream-generator distributor 推送路径：将 KeyedStream 批次转为 logproto.PushRequest，经 dskit user 注入租户 org ID 后调用 distributor gRPC Push。

import (
	"context"
	"fmt"

	"github.com/grafana/dskit/user"
	"github.com/grafana/loki/v3/pkg/distributor"
	"github.com/grafana/loki/v3/pkg/logproto"
)

func (s *Generator) sendStreams(ctx context.Context, tenant string, batch []distributor.KeyedStream, errCh chan<- error) {
	batchSize := len(batch)

	// InjectOrgID 与 InjectIntoGRPCRequest 在 gRPC metadata 中写入 X-Scope-OrgID。
userCtx, err := user.InjectIntoGRPCRequest(user.InjectOrgID(ctx, tenant))
	if err != nil {
		errCh <- fmt.Errorf("failed to inject user context (tenant: %s, batch_size: %d): %w", tenant, batchSize, err)
		return
	}

	pushStreams := make([]logproto.Stream, len(batch))
	for i, stream := range batch {
		pushStreams[i] = logproto.Stream{
			Labels:  stream.Stream.Labels,
			Entries: stream.Stream.Entries,
		}
	}

	pushReq := &logproto.PushRequest{
		Streams: pushStreams,
	}

	_, err = s.distributorClient.Push(userCtx, pushReq)
	if err != nil {
		errCh <- fmt.Errorf("failed to push streams to distributor (tenant: %s, batch_size: %d): %w", tenant, batchSize, err)
		return
	}
}
// 推送失败时向 errCh 返回带 tenant 与 batch_size 上下文的格式化错误。
