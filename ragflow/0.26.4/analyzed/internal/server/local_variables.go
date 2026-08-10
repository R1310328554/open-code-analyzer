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

// local_variables — 进程内可热更新的本地运行时变量（如服务名）。
package server

import (
	"ragflow/internal/common"
	"sync"
)

// LocalVariables 存放可在运行时修改的进程级变量。
type LocalVariables struct {
	ServerName *string // 服务显示名称，运行时可改
}

var (
	localVariables     *LocalVariables
	localVariablesOnce sync.Once
	localVariablesMu   sync.RWMutex
)

// InitLocalVariables 初始化本地变量单例。
func InitLocalVariables() error {
	var initErr error
	localVariablesOnce.Do(func() {
		localVariables = &LocalVariables{}
		common.Info("Local variables initialized successfully")
	})
	return initErr
}

// SetServerName 设置当前进程服务名称。
func SetServerName(serverName string) {
	localVariablesMu.Lock()
	defer localVariablesMu.Unlock()
	localVariables.ServerName = &serverName
}

// GetServerName 读取当前服务名称。
func GetServerName() string {
	localVariablesMu.RLock()
	defer localVariablesMu.RUnlock()
	return *localVariables.ServerName
}
// local_variables.go — 运行时本地变量：可热更新的服务名称等进程级状态。
