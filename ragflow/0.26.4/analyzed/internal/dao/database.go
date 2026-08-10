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
// database.go — GORM 数据库初始化：MySQL 连接池、AutoMigrate、手动迁移、模板种子与模型 Provider 加载。

//

package dao

import (
	"fmt"
	"os"
	"path/filepath"
	"ragflow/internal/common"
	"ragflow/internal/entity"
	"ragflow/internal/entity/models"
	"strings"
	"sync"
	"time"

	"ragflow/internal/server"

	"go.uber.org/zap"
	gormLogger "gorm.io/gorm/logger"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

// DB 全局 GORM 数据库句柄，供各 DAO 包直接使用。
var DB *gorm.DB
// modelProviderManager 缓存的 LLM Provider 管理器单例。
var modelProviderManager *models.ProviderManager
var modelProviderManagerMu sync.Mutex

// LLMFactoryConfig 单个 LLM 厂商（Factory）的配置结构。
type LLMFactoryConfig struct {
	Name   string      `json:"name"`
	Logo   string      `json:"logo"`
	Tags   string      `json:"tags"`
	Status string      `json:"status"`
	Rank   string      `json:"rank"`
	LLM    []LLMConfig `json:"llm"`
}

// LLMConfig 单个 LLM 模型的名称、类型与能力标签。
type LLMConfig struct {
	LLMName   string `json:"llm_name"`
	Tags      string `json:"tags"`
	MaxTokens int64  `json:"max_tokens"`
	ModelType string `json:"model_type"`
	IsTools   bool   `json:"is_tools"`
}

// LLMFactoriesFile llm_factories.json 文件的顶层结构。
type LLMFactoriesFile struct {
	FactoryLLMInfos []LLMFactoryConfig `json:"factory_llm_infos"`
}

// InitDB 建立 MySQL 连接、可选 AutoMigrate、种子模板并加载模型 Provider。
func InitDB(migrateDB bool) error {
	cfg := server.GetConfig()
	dbCfg := cfg.Database

	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=%s&parseTime=True&loc=Local",
		dbCfg.Username,
		dbCfg.Password,
		dbCfg.Host,
		dbCfg.Port,
		dbCfg.Database,
		dbCfg.Charset,
	)

	// 按 server.mode 设置 GORM 日志级别（debug 为 Info，否则 Silent）。
	var gormLogLevel gormLogger.LogLevel
	if cfg.Server.Mode == "debug" {
		gormLogLevel = gormLogger.Info
	} else {
		gormLogLevel = gormLogger.Silent
	}

	// 打开 MySQL 连接，启用 parseTime 与本地时区。
	var err error
	DB, err = gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: gormLogger.Default.LogMode(gormLogLevel),
		NowFunc: func() time.Time {
			return time.Now().Local()
		},
		TranslateError: true,
	})
	if err != nil {
		return fmt.Errorf("failed to connect database: %w", err)
	}

	// 获取底层 *sql.DB 以配置连接池。
	sqlDB, err := DB.DB()
	if err != nil {
		return fmt.Errorf("failed to get database instance: %w", err)
	}

	// 连接池：最大空闲 10、最大打开 100、生命周期 1 小时。
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetMaxOpenConns(100)
	sqlDB.SetConnMaxLifetime(time.Hour)

	// 全部 entity 模型列表，migrateDB 为 true 时逐一 AutoMigrate。
	dataModels := []interface{}{
		&entity.User{},
		&entity.Tenant{},
		&entity.UserTenant{},
		&entity.File{},
		&entity.File2Document{},
		&entity.TenantLLM{},
		&entity.Chat{},
		&entity.ChatSession{},
		&entity.Task{},
		&entity.APIToken{},
		&entity.API4Conversation{},
		&entity.Knowledgebase{},
		&entity.InvitationCode{},
		&entity.Document{},
		&entity.UserCanvas{},
		&entity.CanvasTemplate{},
		&entity.UserCanvasVersion{},
		&entity.LLMFactories{},
		&entity.LLM{},
		&entity.TenantLangfuse{},
		&entity.SystemSettings{},
		&entity.Connector{},
		&entity.Connector2Kb{},
		&entity.SyncLogs{},
		&entity.MCPServer{},
		&entity.Memory{},
		&entity.Search{},
		&entity.PipelineOperationLog{},
		&entity.EvaluationDataset{},
		&entity.EvaluationCase{},
		&entity.EvaluationRun{},
		&entity.EvaluationResult{},
		&entity.TimeRecord{},
		&entity.License{},
		&entity.SkillSearchConfig{},
		&entity.TenantModelInstance{},
		&entity.TenantModel{},
		&entity.TenantModelGroupMapping{},
		&entity.TenantModelProvider{},
		&entity.TenantModelGroup{},
		&entity.IngestionTask{},
		&entity.IngestionTaskLog{},
		&entity.IngestionTasklet{},
		&entity.IngestionTaskletLog{},
		&entity.FileCommit{},
		&entity.FileCommitItem{},
	}

	if migrateDB {
		common.Info("Migrating database schema...")
		for _, m := range dataModels {
			if err = autoMigrateSafely(DB, m); err != nil {
				return fmt.Errorf("failed to migrate model %T: %w", m, err)
			}
		}

		// 复杂 schema 变更走 RunMigrations 手动迁移。
		if err = RunMigrations(DB); err != nil {
			return fmt.Errorf("failed to run manual migrations: %w", err)
		}
		common.Info("Database schema migrated successfully")
	}
	// 种子化内置 Agent 模板，使 Go 后端独立提供「从模板创建」目录。
	if err = SeedCanvasTemplates(); err != nil {
		common.Warn("Failed to seed canvas templates", zap.Error(err))
	}

	common.Info("Database connected and migrated successfully")

	err = models.InitProviderManager("conf/models")
	if err != nil {
		common.Fatal("Failed to load model providers", zap.Error(err))
	}

	modelProviderManager = models.GetProviderManager()
	common.Info("Model providers loaded successfully")

	return nil
}

// GetDB 返回全局 DB 句柄。
func GetDB() *gorm.DB {
	return DB
}

// GetModelProviderManager 懒加载并返回模型 Provider 管理器（双重检查锁）。
func GetModelProviderManager() *models.ProviderManager {
	if modelProviderManager != nil {
		return modelProviderManager
	}

	modelProviderManagerMu.Lock()
	defer modelProviderManagerMu.Unlock()
	if modelProviderManager != nil {
		return modelProviderManager
	}
	if existing := models.GetProviderManager(); existing != nil {
		modelProviderManager = existing
		return modelProviderManager
	}
	modelConfigDir, err := findModelConfigDir()
	if err != nil {
		common.Fatal("Failed to locate model providers", zap.Error(err))
	}
	if err := models.InitProviderManager(modelConfigDir); err != nil {
		common.Fatal("Failed to load model providers", zap.Error(err))
	}
	modelProviderManager = models.GetProviderManager()
	return modelProviderManager
}

// findModelConfigDir 在多个相对路径中查找 conf/models 目录。
func findModelConfigDir() (string, error) {
	candidates := []string{
		"conf/models",
		filepath.Join("..", "..", "conf", "models"),
		filepath.Join("..", "..", "..", "conf", "models"),
	}
	for _, candidate := range candidates {
		if info, err := os.Stat(candidate); err == nil && info.IsDir() {
			return candidate, nil
		}
	}
	return "", fmt.Errorf("conf/models not found")
}

// autoMigrateSafely 执行 AutoMigrate，忽略 MySQL 重复索引/列/表等已知错误。
func autoMigrateSafely(db *gorm.DB, model interface{}) error {
	//err := db.Debug().AutoMigrate(model) // to print debug info
	err := db.AutoMigrate(model)
	if err == nil {
		return nil
	}

	// MySQL 1061 重复索引：Python 后端可能已创建，跳过即可。
	errStr := err.Error()
	if strings.Contains(errStr, "Error 1061") && strings.Contains(errStr, "Duplicate key name") {
		common.Warn("Index already exists, skipping", zap.String("error", errStr))
		return nil
	}

	if strings.Contains(errStr, "Error 1060") && strings.Contains(errStr, "Duplicate column name") {
		common.Warn("Column already exists, skipping", zap.String("error", errStr))
		return nil
	}

	if strings.Contains(errStr, "Error 1050") && strings.Contains(errStr, "Table") {
		common.Warn("Table already exists, skipping", zap.String("error", errStr))
		return nil
	}

	if strings.Contains(errStr, "Error 1091") && strings.Contains(errStr, "Can't DROP") {
		common.Warn("Index/column already dropped, skipping", zap.String("error", errStr))
		return nil
	}

	if strings.Contains(errStr, "Error 1138") && strings.Contains(errStr, "Invalid use of NULL") {
		common.Warn("NULL value in existing rows, skipping migration change", zap.String("error", errStr))
		return nil
	}

	return err
}
