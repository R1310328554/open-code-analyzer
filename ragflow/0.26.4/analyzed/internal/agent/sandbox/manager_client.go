// manager_client.go — 将 ProviderManager 适配为 CodeExec 工具的 SandboxClient 接口。
package sandbox

import (
	"context"
	"fmt"

	agenttool "ragflow/internal/agent/tool"
)

// ManagerClient adapts the active sandbox provider manager to the CodeExec
// tool's SandboxClient interface.
// ManagerClient 包装 ProviderManager，供 agent/tool CodeExec 调用。
type ManagerClient struct {
	manager *ProviderManager
}

// NewManagerClient 返回绑定 DefaultManager 的客户端。
func NewManagerClient() *ManagerClient {
	return &ManagerClient{manager: DefaultManager()}
}

// ExecuteCode 加载 Provider、创建实例、执行脚本并在 defer 中销毁实例。
func (c *ManagerClient) ExecuteCode(ctx context.Context, req agenttool.SandboxRequest) (*agenttool.SandboxResponse, error) {
	if c == nil || c.manager == nil {
		return nil, fmt.Errorf("sandbox: provider manager unavailable")
	}
	if err := c.manager.LoadFromSettings(ctx); err != nil {
		return nil, err
	}
	provider := c.manager.Provider()
	if provider == nil {
		return nil, fmt.Errorf("sandbox: no active provider configured")
	}

	inst, err := provider.CreateInstance(ctx, req.Lang)
	if err != nil {
		return nil, err
	}
	defer func() { _ = provider.DestroyInstance(context.Background(), inst) }()

	timeout := req.Timeout
	if timeout == 0 {
		timeout = 30
	}
	result, err := provider.ExecuteCode(ctx, inst, req.Script, req.Lang, timeout, req.Arguments)
	if err != nil {
		return nil, err
	}
	if result == nil {
		return &agenttool.SandboxResponse{}, nil
	}

	resp := &agenttool.SandboxResponse{
		Stdout:   result.Stdout,
		Stderr:   result.Stderr,
		ExitCode: result.ExitCode,
		Metadata: result.Metadata,
	}
	if result.Metadata != nil {
		if structured, ok := result.Metadata["structured_result"].(map[string]any); ok {
			resp.StructuredResult = structured
		} else if structured, ok := result.Metadata["result"].(map[string]any); ok {
			resp.StructuredResult = structured
		}
	}
	if resp.StructuredResult != nil {
		if present, _ := resp.StructuredResult["present"].(bool); present {
			resp.Returned = fmt.Sprint(resp.StructuredResult["value"])
		}
	}
	return resp, nil
}
