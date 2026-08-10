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
// state.go — Admin 全局服务状态存储：线程安全的 ServerStore，跟踪 API/Ingestion/FileSyncer 等进程心跳。

//

package admin

import (
	"ragflow/internal/common"
	"sync"
	"time"
)

// API 服务进程状态

// ServerStore 线程安全的全局服务状态存储
type ServerStore struct {
	mu      sync.RWMutex
	servers map[string]*common.BaseMessage // key: server_id
}

// GlobalServerStore 全局单例实例。
var GlobalServerStore = &ServerStore{
	servers: make(map[string]*common.BaseMessage),
}

// UpdateServerInfo 更新或新增指定服务的上报状态。
func (s *ServerStore) UpdateServerInfo(serverName string, status *common.BaseMessage) {

	//switch serviceType {
	//case "meta_data":
	//	return s.getMySQLStatus(name)

	switch status.ServerType {
	case common.ServerTypeAPI:
		s.mu.Lock()
		defer s.mu.Unlock()
		s.servers[serverName] = status
		return
	case common.ServerTypeIngestion:
		s.mu.Lock()
		defer s.mu.Unlock()
		s.servers[serverName] = status
		return
	case common.ServerTypeFileSyncer:
		s.mu.Lock()
		defer s.mu.Unlock()
		s.servers[serverName] = status
		return
	}
}

// GetServerInfo gets a single server status
func (s *ServerStore) GetServerInfo(serverName string) (*common.BaseMessage, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	status, ok := s.servers[serverName]
	return status, ok
}

// ListInfos gets all server infos
func (s *ServerStore) ListInfos() []*common.BaseMessage {
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make([]*common.BaseMessage, 0, len(s.servers))
	for _, status := range s.servers {
		result = append(result, status)
	}
	return result
}

// ListInfosByType gets server infos by type
func (s *ServerStore) ListInfosByType(serverType common.ServerType) []*common.BaseMessage {
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make([]*common.BaseMessage, 0)
	for _, status := range s.servers {
		if status.ServerType == serverType {
			result = append(result, status)
		}
	}
	return result
}

// RemoveStatus removes a server status
func (s *ServerStore) RemoveStatus(serverID string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	delete(s.servers, serverID)
}

// CleanupStaleStatuses 清理超过 maxAge 未上报的过期服务记录。
func (s *ServerStore) CleanupStaleStatuses(maxAge time.Duration) {
	s.mu.Lock()
	defer s.mu.Unlock()
	now := time.Now()
	for id, status := range s.servers {
		if now.Sub(status.Timestamp) > maxAge {
			delete(s.servers, id)
		}
	}
}
