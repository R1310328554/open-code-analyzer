//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

// code_exec_client.go — CodeExec 沙箱客户端抽象：启动时 SetSandboxClient 注入真实实现，默认 stub 返回 ErrSandboxNotWired。
// CodeExec sandbox client. The Python sandbox service is kept
// as-is (we do NOT rewrite the sandbox). The Go side has a
// client that talks to the Python sandbox subsystem via the
// internal/agent/sandbox providers.
//
// The Python agent's code_exec delegates to a
// `SandboxProvider` subsystem (agent/sandbox/providers/) with
// three backends: self-managed (Docker + gVisor via local
// executor_manager HTTP), Aliyun Code Interpreter (cloud), and
// e2b (cloud SaaS). A `ProviderManager` selects one at
// startup. The Go side's decision is recorded in
// code_exec_sandbox_design.md:
//
//  1. shell out = spawn a Python subprocess that uses
//     ProviderManager to dispatch to whichever provider the
//     operator has configured. Reuses all three providers.
//  2. in-process = reimplement all three provider clients in
//     Go. No Python dep, but three SDK surfaces to maintain.
//
// Decision: shell out (option A). The interface here is stable —
// when the implementation lands, callers see no change.
//
// Until the proto lands, CodeExecTool.InvokableRun surfaces
// ErrCodeExecSandboxMissing (defined in code_exec.go) so callers
// can detect the no-sandbox state.
package tool

import (
	"context"
	"sync"
)

// SandboxClient is the abstract interface for the CodeExec gRPC
// client. Production code calls SetSandboxClient at boot to install
// a real client; the default is a stub returning
// ErrSandboxNotWired.
// SandboxClient 为 CodeExec 与沙箱子系统之间的 ExecuteCode 契约。
type SandboxClient interface {
	ExecuteCode(ctx context.Context, req SandboxRequest) (*SandboxResponse, error)
}

// SandboxRequest is the wire shape between the CodeExec tool and
// the sandbox subsystem. Mirrors `agent.sandbox.client.execute_code`'s
// input surface. Arguments and Timeout are optional — the bridge
// applies defaults (no args, 30s timeout) when zero-valued.
// SandboxRequest 为工具到沙箱的 wire 形状，Timeout=0 时使用 Provider 默认 30s。
type SandboxRequest struct {
	Lang      string         // "python" | "javascript"
	Script    string         // the user's code
	Arguments map[string]any // optional, passed to main(**args)
	Timeout   int            // seconds, 0 = use provider default (30s)
}

// SandboxResponse is what the sandbox returns. Stdout / Stderr are
// captured streams; Returned is the legacy alias for the
// structured main() return value; StructuredResult carries the
// full extracted payload; Metadata holds provider-specific extras.
// SandboxResponse 携带 stdout/stderr/结构化结果与 Provider 元数据。
type SandboxResponse struct {
	Stdout           string
	Stderr           string
	Returned         string
	ExitCode         int
	StructuredResult map[string]any
	Metadata         map[string]any
}

// ErrSandboxNotWired is the sentinel returned by the default stub
// client. Kept as an alias of ErrCodeExecSandboxMissing for backward
// compatibility with existing code_exec_test.go callers — the tool's
// public error surface is ErrCodeExecSandboxMissing; the client
// surface here is the same condition, named for code-path clarity.
var ErrSandboxNotWired = ErrCodeExecSandboxMissing

var (
	sandboxClientMu   sync.RWMutex
	sandboxClientImpl SandboxClient = stubSandboxClient{}
)

// SetSandboxClient 在进程启动时注册生产沙箱客户端。
func SetSandboxClient(c SandboxClient) {
	sandboxClientMu.Lock()
	defer sandboxClientMu.Unlock()
	if c == nil {
		sandboxClientImpl = stubSandboxClient{}
		return
	}
	sandboxClientImpl = c
}

// GetSandboxClient 返回当前注册的 SandboxClient（默认 stub）。
func GetSandboxClient() SandboxClient {
	sandboxClientMu.RLock()
	defer sandboxClientMu.RUnlock()
	return sandboxClientImpl
}

type stubSandboxClient struct{}

func (stubSandboxClient) ExecuteCode(_ context.Context, _ SandboxRequest) (*SandboxResponse, error) {
	return nil, ErrSandboxNotWired
}

func (stubSandboxClient) IsStubSandboxClient() bool { return true }
