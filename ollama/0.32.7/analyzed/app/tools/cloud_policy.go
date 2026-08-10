//go:build windows || darwin

// Package tools 云端策略校验：工具执行前确认 Ollama 服务未禁用云端功能。
package tools

import (
	"context"
	"errors"

	"github.com/ollama/ollama/api"
	internalcloud "github.com/ollama/ollama/internal/cloud"
)

// ensureCloudEnabledForTool 查询已连接服务器的 /api/status；无法确认时 fail-closed 拒绝操作。
// ensureCloudEnabledForTool checks cloud policy from the connected Ollama server.
// If policy cannot be determined, this fails closed and blocks the operation.
func ensureCloudEnabledForTool(ctx context.Context, operation string) error {
	// Reuse shared message formatting; policy evaluation is still done via
	// the connected server's /api/status endpoint below.
	disabledMessage := internalcloud.DisabledError(operation)

	client, err := api.ClientFromEnvironment()
	if err != nil {
		return errors.New(disabledMessage + " (unable to verify server cloud policy)")
	}

	status, err := client.CloudStatusExperimental(ctx)
	if err != nil {
		return errors.New(disabledMessage + " (unable to verify server cloud policy)")
	}

	if status.Cloud.Disabled {
		return errors.New(disabledMessage)
	}

	return nil
}
