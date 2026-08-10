package planner

// Planner 队列任务包装：将 protos.ProtoTask 与运行时元数据绑定，
// 跟踪入队次数、排队时间与结果 channel，供 BuilderLoop 异步回传构建结果。

import (
	"context"
	"time"

	"go.uber.org/atomic"

	"github.com/grafana/loki/v3/pkg/bloombuild/protos"
)

type TaskMeta struct {
	resultsChannel chan *protos.TaskResult

	// Tracking
	timesEnqueued atomic.Int64
	queueTime     time.Time
	ctx           context.Context
}

// QueueTask 嵌入 ProtoTask 与 TaskMeta，是队列与 builder 之间的传输单元。
type QueueTask struct {
	*protos.ProtoTask
	*TaskMeta
}

func NewQueueTask(
	ctx context.Context,
	queueTime time.Time,
	task *protos.ProtoTask,
	resultsChannel chan *protos.TaskResult,
) *QueueTask {
	return &QueueTask{
		ProtoTask: task,
		TaskMeta: &TaskMeta{
			resultsChannel: resultsChannel,
			ctx:            ctx,
			queueTime:      queueTime,
		},
	}
}
