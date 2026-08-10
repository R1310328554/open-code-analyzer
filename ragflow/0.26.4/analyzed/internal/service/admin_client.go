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

// admin_client — 向 Admin 服务周期性发送心跳与版本上报。
package service

import (
	"encoding/json"
	"errors"
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/server"
	"ragflow/internal/utility"
	"time"

	"go.uber.org/zap"
)

var AdminServiceClient *AdminClient

// AdminClient 负责向管理端 POST 心跳（服务名、类型、host/port、版本）。
type AdminClient struct {
	client       *utility.HTTPClient
	logger       *zap.Logger
	serverType   common.ServerType
	serverName   string
	host         string
	port         int
	version      string
	lastSuccess  bool
	attemptCount int
}

// NewAdminClient 构造心跳客户端实例。
func NewAdminClient(logger *zap.Logger, serverType common.ServerType, serverName, host string, port int) *AdminClient {
	return &AdminClient{
		logger:       logger,
		serverType:   serverType,
		serverName:   serverName,
		host:         host,
		port:         port,
		version:      utility.GetRAGFlowVersion(),
		lastSuccess:  false,
		attemptCount: 0,
	}
}

// InitHTTPClient 按 AdminConfig 构建 HTTP 客户端（10s 超时）。
func (h *AdminClient) InitHTTPClient() error {
	adminConfig := server.GetAdminConfig()
	if adminConfig == nil {
		return fmt.Errorf("admin configuration not found")
	}

	h.client = utility.NewHTTPClientBuilder().
		WithHost(adminConfig.Host).
		WithPort(adminConfig.Port).
		WithTimeout(10 * time.Second).
		Build()

	h.logger.Info("Heartbeat HTTP client initialized",
		zap.String("admin_host", adminConfig.Host),
		zap.Int("admin_port", adminConfig.Port),
	)

	return nil
}

// SendHeartbeat 发送心跳；连续成功时降频（attemptCount<10 时跳过）。
func (h *AdminClient) SendHeartbeat() error {

	if h.attemptCount < 10 {
		if h.lastSuccess {
			h.attemptCount++
			return nil
		}
	}
	h.attemptCount = 0
	h.lastSuccess = false

	if h.client == nil {
		if err := h.InitHTTPClient(); err != nil {
			h.logger.Error("Failed to initialize HTTP client", zap.Error(err))
			return err
		}
	}

	message := &common.BaseMessage{
		MessageID:   time.Now().UnixNano(),
		MessageType: common.MessageHeartbeat,
		ServerName:  h.serverName,
		ServerType:  h.serverType,
		Host:        h.host,
		Port:        h.port,
		Version:     h.version,
		Timestamp:   time.Now(),
		Ext:         nil,
	}

	jsonData, err := json.Marshal(message)
	if err != nil {
		h.logger.Error("Failed to marshal heartbeat message", zap.Error(err))
		return err
	}

	resp, err := h.client.PostJSON("/api/v1/admin/reports", jsonData)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		// extract the Code and Message field of the response
		var responseBody map[string]interface{}
		err = json.NewDecoder(resp.Body).Decode(&responseBody)
		if err != nil {
			return err
		}
		code, ok := responseBody["code"].(float64)
		if !ok {
			return fmt.Errorf("unexpected heartbeat response (status %d): missing or non-numeric \"code\" field", resp.StatusCode)
		}
		responseCode := common.ErrorCode(code)
		if responseCode != common.CodeLicenseValid {
			return errors.New(responseCode.Message())
		}
	}

	h.logger.Debug("Heartbeat sent successfully",
		zap.String("server_id", h.serverName),
		zap.String("server_type", string(h.serverType)),
	)

	h.lastSuccess = true

	return nil
}
// admin_client.go — Admin 心跳客户端：向管理端上报服务存活与版本信息。
