// Package pregel 提供 Pregel 远程节点执行支持。
//
// 通过 HTTP 将节点函数委托到远程服务，含 Pregel 消息协议定义。
package pregel

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"ragflow/internal/harness/graph/types"
)

// RemoteRunnable 通过 HTTP 远程执行节点。
type RemoteRunnable struct {
	url        string
	headers    map[string]string
	httpClient *http.Client
	timeout    time.Duration
}

// RemoteConfig 远程执行客户端配置。
type RemoteConfig struct {
	URL        string
	Headers    map[string]string
	Timeout    time.Duration
	HTTPClient *http.Client
}

// NewRemoteRunnable 创建远程 Runnable，默认超时 30 秒。
func NewRemoteRunnable(config *RemoteConfig) *RemoteRunnable {
	timeout := config.Timeout
	if timeout == 0 {
		timeout = 30 * time.Second
	}

	client := config.HTTPClient
	if client == nil {
		client = &http.Client{
			Timeout: timeout,
		}
	}

	headers := make(map[string]string)
	if config.Headers != nil {
		for k, v := range config.Headers {
			headers[k] = v
		}
	}

	return &RemoteRunnable{
		url:        config.URL,
		headers:    headers,
		httpClient: client,
		timeout:    timeout,
	}
}

// Execute 向远程服务 POST /execute 执行节点。
func (r *RemoteRunnable) Execute(ctx context.Context, nodeName string, input any, config *types.RunnableConfig) (any, error) {
	// Build request
	reqBody := &RemoteExecuteRequest{
		Node:   nodeName,
		Input:  input,
		Config: config,
	}

	data, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, "POST", r.url+"/execute", bytes.NewReader(data))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	for k, v := range r.headers {
		req.Header.Set(k, v)
	}

	// Send request
	resp, err := r.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	// Read response
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("remote execution failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Parse response
	var result RemoteExecuteResponse
	if err := json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	if result.Error != "" {
		return nil, fmt.Errorf("remote execution error: %s", result.Error)
	}

	return result.Output, nil
}

// RemoteExecuteRequest 远程执行请求体。
type RemoteExecuteRequest struct {
	Node   string                `json:"node"`
	Input  any                   `json:"input"`
	Config *types.RunnableConfig `json:"config,omitempty"`
}

// RemoteExecuteResponse 远程执行响应体。
type RemoteExecuteResponse struct {
	Output any    `json:"output,omitempty"`
	Error  string `json:"error,omitempty"`
}

// PregelProtocol 远程 Pregel 消息协议接口。
type PregelProtocol interface {
	// Send POST 消息到 /pregel/message。
	Send(ctx context.Context, message *PregelMessage) error
	// Receive GET 拉取远程消息（204 表示无消息）。
	Receive(ctx context.Context) (*PregelMessage, error)
	// Close 关闭协议连接（HTTP 无状态，空实现）。
	Close() error
}

// PregelMessage Pregel 协议消息体。
type PregelMessage struct {
	Type      MessageType    `json:"type"`
	ID        string         `json:"id"`
	NodeName  string         `json:"node_name,omitempty"`
	Input     any            `json:"input,omitempty"`
	Output    any            `json:"output,omitempty"`
	Error     string         `json:"error,omitempty"`
	Metadata  map[string]any `json:"metadata,omitempty"`
	Timestamp time.Time      `json:"timestamp"`
}

// MessageType Pregel 消息类型枚举。
type MessageType string

const (
	// MessageTypeExecute 请求执行节点。
	MessageTypeExecute MessageType = "execute"
	// MessageTypeExecuteResponse 执行响应。
	MessageTypeExecuteResponse MessageType = "execute_response"
	// MessageTypeCheckpoint 发送检查点数据。
	MessageTypeCheckpoint MessageType = "checkpoint"
	// MessageTypeStateUpdate 发送状态更新。
	MessageTypeStateUpdate MessageType = "state_update"
	// MessageTypeInterrupt 发送中断信息。
	MessageTypeInterrupt MessageType = "interrupt"
	// MessageTypeResume 从中断恢复执行。
	MessageTypeResume MessageType = "resume"
	// MessageTypePing 心跳请求。
	MessageTypePing MessageType = "ping"
	// MessageTypePong 心跳响应。
	MessageTypePong MessageType = "pong"
)

// HTTPPregelProtocol 基于 HTTP 的 Pregel 协议实现。
type HTTPPregelProtocol struct {
	baseURL    string
	httpClient *http.Client
	headers    map[string]string
}

// NewHTTPPregelProtocol 创建 HTTP 协议客户端。
func NewHTTPPregelProtocol(baseURL string, headers map[string]string) *HTTPPregelProtocol {
	return &HTTPPregelProtocol{
		baseURL:    baseURL,
		httpClient: &http.Client{Timeout: 30 * time.Second},
		headers:    headers,
	}
}

// Send POST 消息到 /pregel/message。
func (p *HTTPPregelProtocol) Send(ctx context.Context, message *PregelMessage) error {
	data, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal message: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, "POST", p.baseURL+"/pregel/message", bytes.NewReader(data))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	for k, v := range p.headers {
		req.Header.Set(k, v)
	}

	resp, err := p.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send message: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("send failed with status %d: %s", resp.StatusCode, string(body))
	}

	return nil
}

// Receive GET 拉取远程消息（204 表示无消息）。
func (p *HTTPPregelProtocol) Receive(ctx context.Context) (*PregelMessage, error) {
	req, err := http.NewRequestWithContext(ctx, "GET", p.baseURL+"/pregel/message", nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	for k, v := range p.headers {
		req.Header.Set(k, v)
	}

	resp, err := p.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to receive message: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNoContent {
		return nil, nil // No message available
	}

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("receive failed with status %d: %s", resp.StatusCode, string(body))
	}

	var message PregelMessage
	if err := json.NewDecoder(resp.Body).Decode(&message); err != nil {
		return nil, fmt.Errorf("failed to decode message: %w", err)
	}

	return &message, nil
}

// Close 关闭协议连接（HTTP 无状态，空实现）。
func (p *HTTPPregelProtocol) Close() error {
	return nil
}

// RemoteNode 将远程 Runnable 包装为本地节点函数。
type RemoteNode struct {
	runnable *RemoteRunnable
	nodeName string
}

// NewRemoteNode 创建远程节点。
func NewRemoteNode(runnable *RemoteRunnable, nodeName string) *RemoteNode {
	return &RemoteNode{
		runnable: runnable,
		nodeName: nodeName,
	}
}

// Execute 调用远程 Runnable 执行节点。
func (n *RemoteNode) Execute(ctx context.Context, input any) (any, error) {
	return n.runnable.Execute(ctx, n.nodeName, input, nil)
}

// NodeToRemoteRunnable 将本地节点暴露为远程 Runnable（需 HTTP 服务端配合）。
func NodeToRemoteRunnable(node types.NodeFunc, url string) *RemoteRunnable {
	// This would register the node locally and expose it via HTTP
	// Implementation depends on the HTTP server setup
	return NewRemoteRunnable(&RemoteConfig{
		URL: url,
	})
}
