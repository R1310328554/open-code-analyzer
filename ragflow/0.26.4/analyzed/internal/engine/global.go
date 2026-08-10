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

// global.go — 全局文档引擎与消息队列单例：根据配置初始化 Elasticsearch 或 Infinity，并可选初始化 NATS JetStream。
//

package engine

import (
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/engine/nats"
	"ragflow/internal/server"
	"sync"

	"ragflow/internal/engine/elasticsearch"
	"ragflow/internal/engine/infinity"

	"ragflow/internal/tokenizer"

	"go.uber.org/zap"
)

var (
	globalEngine       DocEngine
	engineType         EngineType
	messageQueueEngine MessageQueue
	once               sync.Once
)

// Init 一次性初始化全局 DocEngine（sync.Once），并注册 tokenizer 引擎类型回调
func Init(cfg *server.DocEngineConfig) error {
	var initErr error
	once.Do(func() {
		tokenizer.RegisterEngineType(func() string {
			return string(GetEngineType())
		})

		engineType = EngineType(cfg.Type)
		var err error
		switch engineType {
		case EngineElasticsearch:
			globalEngine, err = elasticsearch.NewEngine(cfg.ES)
		case EngineInfinity:
			globalEngine, err = infinity.NewEngine(cfg.Infinity)
		default:
			err = fmt.Errorf("unsupported doc engine type: %s", cfg.Type)
		}

		if err != nil {
			initErr = fmt.Errorf("failed to create doc engine: %w", err)
			return
		}
		common.Info("Doc engine initialized", zap.String("type", string(cfg.Type)))
	})
	return initErr
}

// GetEngineType 返回当前文档引擎类型
func GetEngineType() EngineType {
	return engineType
}

// Get 获取全局 DocEngine 实例
func Get() DocEngine {
	return globalEngine
}

// Close 关闭全局引擎连接
func Close() error {
	if globalEngine != nil {
		return globalEngine.Close()
	}
	return nil
}

// GetMessageQueueEngine 返回全局消息队列引擎实例。
func GetMessageQueueEngine() MessageQueue {
	return messageQueueEngine
}

// InitMessageQueueEngine 按类型初始化消息队列（当前支持 nats）。
func InitMessageQueueEngine(messageQueueType string) error {
	config := server.GetConfig()
	switch messageQueueType {
	case "nats":
		messageQueueEngine = nats.NewNatsEngine(config.Nats.Host, config.Nats.Port)
		err := messageQueueEngine.Init()
		if err != nil {
			return err
		}
	case "":
		return fmt.Errorf("message queue type is empty")
	default:
		return fmt.Errorf("unsupported message queue type: %s", messageQueueType)
	}
	return nil
}
