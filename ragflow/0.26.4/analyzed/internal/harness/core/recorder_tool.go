package core

// recorder_tool.go — 工具调用事件记录：ToolInvokeMiddleware 记录每次工具调用的参数、结果与耗时。


import (
	"context"
	"encoding/json"
	"time"

	"ragflow/internal/harness/core/schema"
	"ragflow/internal/harness/events"
)

// NewEventRecorderToolMiddleware 创建工具调用记录中间件。
// 每次 InvokeTool 前后计时并写入 EventRecorder。
//
// Usage:
//
//	cfg := &ReActConfig[*schema.Message]{
//	    ToolsConfig: &ToolsNodeConfig{
//	        ToolInvokeMiddlewares: []ToolInvokeMiddleware{
//	            NewEventRecorderToolMiddleware(),
//	        },
//	    },
//	}
//	ctx = events.ContextWithRecorder(ctx, recorder)
func NewEventRecorderToolMiddleware() ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			rec := events.RecorderFromContext(ctx)

			start := time.Now()
			result, err := next(ctx, ictx)
			durMs := time.Since(start).Milliseconds()

			if rec == nil {
				return result, err
			}

			// 将 JSON 参数字符串解析为 map 记录。
			var args map[string]any
			if ictx.Arguments != nil && ictx.Arguments.Arguments != "" {
				json.Unmarshal([]byte(ictx.Arguments.Arguments), &args)
			}

			errStr := ""
			retryCount := 0
			if ictx.RetryConfig != nil {
				retryCount = ictx.RetryConfig.MaxAttempts
			}
			if err != nil {
				errStr = err.Error()
			} else if result != nil && result.Error != "" {
				errStr = result.Error
			}

			rec.RecordToolCall(ctx, ictx.Name, args, result, durMs, retryCount, errStr)
			return result, err
		}
	}
}

// 配置于 ToolsNodeConfig.ToolInvokeMiddlewares；无 Recorder 时零开销透传。
