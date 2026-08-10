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

// admin_status — 进程内 Admin 服务心跳/授权状态缓存。
package local

import (
	"fmt"
	"ragflow/internal/common"
	"sync"
)

// AdminStatus 管理端可用性：0 可用，1 不可用及原因。
type AdminStatus struct {
	Status int    `json:"status"` // 0 = available, 1 = not available
	Reason string `json:"reason"` // reason for invalid status
}

var (
	adminStatus     *AdminStatus
	adminStatusMu   sync.RWMutex
	adminStatusOnce sync.Once
)

// InitAdminStatus 进程启动时一次性初始化全局 Admin 状态。
func InitAdminStatus(status int, reason string) {
	adminStatusOnce.Do(func() {
		adminStatus = &AdminStatus{
			Status: status,
			Reason: reason,
		}
	})
}

// GetAdminStatus 线程安全读取当前 Admin 状态副本。
func GetAdminStatus() AdminStatus {
	adminStatusMu.RLock()
	defer adminStatusMu.RUnlock()
	if adminStatus == nil {
		return AdminStatus{Status: 1, Reason: "not initialized"}
	}
	return AdminStatus{
		Status: adminStatus.Status,
		Reason: adminStatus.Reason,
	}
}

// SetAdminStatus 更新状态；不可用时写 Warn 日志。
func SetAdminStatus(status int, reason string) {
	adminStatusMu.Lock()
	defer adminStatusMu.Unlock()
	if adminStatus == nil {
		adminStatus = &AdminStatus{}
	}
	adminStatus.Status = status
	adminStatus.Reason = reason

	if adminStatus.Status != 0 {
		common.Warn(fmt.Sprintf("Admin server is unavailable, reason: %s", adminStatus.Reason))
	}
}

// IsAdminAvailable 判断 Admin 是否可用（Status==0）。
func IsAdminAvailable() bool {
	adminStatusMu.RLock()
	defer adminStatusMu.RUnlock()
	if adminStatus == nil {
		return false
	}
	return adminStatus.Status == 0
}
// admin_status.go — 进程内 Admin 服务可用性状态（0 可用 / 1 不可用）。
