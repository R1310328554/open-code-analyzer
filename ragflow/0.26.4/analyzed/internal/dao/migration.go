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
// migration.go — 手动数据库迁移：AutoMigrate 无法覆盖的主键改造、列重命名、唯一索引及技能搜索/空间表初始化。

//

package dao

import (
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/entity"
	"strings"

	"go.uber.org/zap"
	"gorm.io/gorm"
)

// RunMigrations 执行全部手动迁移步骤（AutoMigrate 无法单独完成的变更）。
func RunMigrations(db *gorm.DB) error {
	// 检查 tenant_llm 复合主键并迁移为自增 id 主键
	if err := migrateTenantLLMPrimaryKey(db); err != nil {
		return fmt.Errorf("failed to migrate tenant_llm primary key: %w", err)
	}

	// 重命名拼写错误的列（process_duation → process_duration）
	if err := renameColumnIfExists(db, "task", "process_duation", "process_duration"); err != nil {
		return fmt.Errorf("failed to rename task.process_duation: %w", err)
	}
	if err := renameColumnIfExists(db, "document", "process_duation", "process_duration"); err != nil {
		return fmt.Errorf("failed to rename document.process_duation: %w", err)
	}

	// 为 user.email 添加唯一索引
	if err := migrateAddUniqueEmail(db); err != nil {
		return fmt.Errorf("failed to add unique index on user.email: %w", err)
	}

	// 修正 AutoMigrate 可能处理不当的列类型
	if err := modifyColumnTypes(db); err != nil {
		return fmt.Errorf("failed to modify column types: %w", err)
	}

	// 创建技能搜索相关表
	if err := migrateSkillSearchTables(db); err != nil {
		return fmt.Errorf("failed to migrate skill search tables: %w", err)
	}

	// 创建技能空间相关表
	if err := migrateSkillSpaceTables(db); err != nil {
		return fmt.Errorf("failed to migrate skill space tables: %w", err)
	}

	common.Info("All manual migrations completed successfully")
	return nil
}

// migrateTenantLLMPrimaryKey 将 tenant_llm 从复合主键迁移为 id 主键，对齐 Python update_tenant_llm_to_id_primary_key。
func migrateTenantLLMPrimaryKey(db *gorm.DB) error {
	// 表不存在则跳过
	if !db.Migrator().HasTable("tenant_llm") {
		return nil
	}

	// 用 INFORMATION_SCHEMA 检查 id 列是否已存在
	var idColumnExists int64
	err := db.Raw(`
		SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
		WHERE TABLE_NAME = 'tenant_llm' AND COLUMN_NAME = 'id'
	`).Scan(&idColumnExists).Error
	if err != nil {
		return err
	}

	if idColumnExists > 0 {
		// 检查 id 是否已是自增主键
		var count int64
		err := db.Raw(`
			SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
			WHERE TABLE_NAME = 'tenant_llm' 
			AND COLUMN_NAME = 'id' 
			AND EXTRA LIKE '%auto_increment%'
		`).Scan(&count).Error
		if err != nil {
			return err
		}
		if count > 0 {
			// 已迁移完成，直接返回
			return nil
		}
	}

	common.Info("Migrating tenant_llm to use ID primary key...")

	// 在事务中执行迁移
	return db.Transaction(func(tx *gorm.DB) error {
		// 清理历史 temp_id 列
		var tempIdExists int64
		tx.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
			WHERE TABLE_NAME = 'tenant_llm' AND COLUMN_NAME = 'temp_id'`).Scan(&tempIdExists)
		if tempIdExists > 0 {
			if err := tx.Exec("ALTER TABLE tenant_llm DROP COLUMN temp_id").Error; err != nil {
				common.Warn("Failed to drop temp_id column", zap.Error(err))
			}
		}

		// 若已有 id 列则 MODIFY，否则 ADD FIRST
		if idColumnExists > 0 {
			// 将现有 id 改为自增主键
			if err := tx.Exec(`
				ALTER TABLE tenant_llm 
				MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
			`).Error; err != nil {
				return fmt.Errorf("failed to modify id column: %w", err)
			}
		} else {
			// 新增自增 id 主键列
			if err := tx.Exec(`
				ALTER TABLE tenant_llm 
				ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST
			`).Error; err != nil {
				return fmt.Errorf("failed to add id column: %w", err)
			}
		}

		// 添加 (tenant_id, llm_factory, llm_name) 唯一索引
		var idxExists int64
		tx.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
			WHERE TABLE_NAME = 'tenant_llm' AND INDEX_NAME = 'idx_tenant_llm_unique'`).Scan(&idxExists)
		if idxExists == 0 {
			if err := tx.Exec(`
				ALTER TABLE tenant_llm 
				ADD UNIQUE INDEX idx_tenant_llm_unique (tenant_id, llm_factory, llm_name)
			`).Error; err != nil {
				common.Warn("Failed to add unique index idx_tenant_llm_unique", zap.Error(err))
			}
		}

		common.Info("tenant_llm primary key migration completed")
		return nil
	})
}

// migrateAddUniqueEmail 为 user.email 添加唯一索引（重复邮箱时跳过）。
func migrateAddUniqueEmail(db *gorm.DB) error {
	if !db.Migrator().HasTable("user") {
		return nil
	}

	// 检查 idx_user_email_unique 是否已存在
	var count int64
	db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
		WHERE TABLE_NAME = 'user' AND INDEX_NAME = 'idx_user_email_unique'`).Scan(&count)
	if count > 0 {
		return nil
	}

	// 先检测是否存在重复邮箱
	var duplicateCount int64
	err := db.Raw(`
		SELECT COUNT(*) FROM (
			SELECT email FROM user GROUP BY email HAVING COUNT(*) > 1
		) AS duplicates
	`).Scan(&duplicateCount).Error
	if err != nil {
		return err
	}

	if duplicateCount > 0 {
		common.Warn("Found duplicate emails in user table, cannot add unique index", zap.Int64("count", duplicateCount))
		return nil
	}

	common.Info("Adding unique index on user.email...")
	if err = db.Exec(`ALTER TABLE user ADD UNIQUE INDEX idx_user_email_unique (email)`).Error; err != nil {

		// MySQL 1061 重复索引名时视为已存在并跳过
		errStr := err.Error()
		if strings.Contains(errStr, "Error 1061") && strings.Contains(errStr, "Duplicate key name") {
			common.Info("Index already exists, skipping", zap.String("error", errStr))
			return nil
		}
		return fmt.Errorf("failed to add unique index on email: %w", err)
	}

	return nil
}

// modifyColumnTypes 对需显式 ALTER 的列类型进行修正。
func modifyColumnTypes(db *gorm.DB) error {
	// 辅助函数：检查列是否存在
	columnExists := func(table, column string) bool {
		var count int64
		db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
			WHERE TABLE_NAME = ? AND COLUMN_NAME = ?`, table, column).Scan(&count)
		return count > 0
	}

	// dialog.top_k 确保为 BIGINT 默认 1024
	if db.Migrator().HasTable("dialog") && columnExists("dialog", "top_k") {
		if err := db.Exec(`ALTER TABLE dialog MODIFY COLUMN top_k BIGINT NOT NULL DEFAULT 1024`).Error; err != nil {
			common.Warn("Failed to modify dialog.top_k", zap.Error(err))
		}
	}

	// tenant_llm.api_key 确保为 LONGTEXT
	if db.Migrator().HasTable("tenant_llm") && columnExists("tenant_llm", "api_key") {
		if err := db.Exec(`ALTER TABLE tenant_llm MODIFY COLUMN api_key LONGTEXT`).Error; err != nil {
			common.Warn("Failed to modify tenant_llm.api_key", zap.Error(err))
		}
	}

	// api_token.dialog_id 确保 VARCHAR(32)
	if db.Migrator().HasTable("api_token") && columnExists("api_token", "dialog_id") {
		if err := db.Exec(`ALTER TABLE api_token MODIFY COLUMN dialog_id VARCHAR(32)`).Error; err != nil {
			common.Warn("Failed to modify api_token.dialog_id", zap.Error(err))
		}
	}

	// canvas_template title/description 改为 LONGTEXT NULL，对齐 Python JSONField
	if db.Migrator().HasTable("canvas_template") {
		if columnExists("canvas_template", "title") {
			if err := db.Exec(`ALTER TABLE canvas_template MODIFY COLUMN title LONGTEXT NULL`).Error; err != nil {
				common.Warn("Failed to modify canvas_template.title", zap.Error(err))
			}
		}
		if columnExists("canvas_template", "description") {
			if err := db.Exec(`ALTER TABLE canvas_template MODIFY COLUMN description LONGTEXT NULL`).Error; err != nil {
				common.Warn("Failed to modify canvas_template.description", zap.Error(err))
			}
		}
	}

	// system_settings.value 确保 LONGTEXT NOT NULL
	if db.Migrator().HasTable("system_settings") && columnExists("system_settings", "value") {
		if err := db.Exec(`ALTER TABLE system_settings MODIFY COLUMN value LONGTEXT NOT NULL`).Error; err != nil {
			common.Warn("Failed to modify system_settings.value", zap.Error(err))
		}
	}

	// knowledgebase.raptor_task_finish_at 确保 DATETIME 类型
	if db.Migrator().HasTable("knowledgebase") && columnExists("knowledgebase", "raptor_task_finish_at") {
		if err := db.Exec(`ALTER TABLE knowledgebase MODIFY COLUMN raptor_task_finish_at DATETIME`).Error; err != nil {
			common.Warn("Failed to modify knowledgebase.raptor_task_finish_at", zap.Error(err))
		}
	}

	// knowledgebase.mindmap_task_finish_at 确保 DATETIME 类型
	if db.Migrator().HasTable("knowledgebase") && columnExists("knowledgebase", "mindmap_task_finish_at") {
		if err := db.Exec(`ALTER TABLE knowledgebase MODIFY COLUMN mindmap_task_finish_at DATETIME`).Error; err != nil {
			common.Warn("Failed to modify knowledgebase.mindmap_task_finish_at", zap.Error(err))
		}
	}

	return nil
}

// renameColumnIfExists 旧列存在且新列不存在时重命名，两者并存则删旧列。
func renameColumnIfExists(db *gorm.DB, tableName, oldName, newName string) error {
	if !db.Migrator().HasTable(tableName) {
		return nil
	}

	// 检查指定列是否存在
	columnExists := func(column string) bool {
		var count int64
		db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
			WHERE TABLE_NAME = ? AND COLUMN_NAME = ?`, tableName, column).Scan(&count)
		return count > 0
	}

	// 旧列不存在则无需迁移
	if !columnExists(oldName) {
		return nil
	}

	// 新列已存在则删除旧列
	if columnExists(newName) {
		// 新旧列并存：记录警告并删除旧列
		common.Warn("Both old and new columns exist, dropping old one",
			zap.String("table", tableName),
			zap.String("oldColumn", oldName),
			zap.String("newColumn", newName))
		return db.Migrator().DropColumn(tableName, oldName)
	}

	common.Info("Renaming column",
		zap.String("table", tableName),
		zap.String("oldColumn", oldName),
		zap.String("newColumn", newName))
	return db.Migrator().RenameColumn(tableName, oldName, newName)
}

// addColumnIfNotExists 列不存在时执行 ALTER ADD COLUMN。
func addColumnIfNotExists(db *gorm.DB, tableName, columnName, columnDef string) error {
	if !db.Migrator().HasTable(tableName) {
		return nil
	}

	// 用 INFORMATION_SCHEMA 判断列是否存在
	var count int64
	db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
		WHERE TABLE_NAME = ? AND COLUMN_NAME = ?`, tableName, columnName).Scan(&count)
	if count > 0 {
		return nil
	}

	common.Info("Adding column",
		zap.String("table", tableName),
		zap.String("column", columnName))
	sql := fmt.Sprintf("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDef)
	return db.Exec(sql).Error
}

// migrateSkillSearchTables 创建或升级 skill_search_configs 表及索引。
func migrateSkillSearchTables(db *gorm.DB) error {
	// 表不存在则 CREATE，失败时回退 AutoMigrate
	if !db.Migrator().HasTable("skill_search_configs") {
		common.Info("Creating skill_search_configs table...")
		sql := `
		CREATE TABLE IF NOT EXISTS skill_search_configs (
			id VARCHAR(32) PRIMARY KEY,
			tenant_id VARCHAR(32) NOT NULL,
			space_id VARCHAR(128) NOT NULL DEFAULT 'default',
			embd_id VARCHAR(128) NOT NULL,
			vector_similarity_weight FLOAT DEFAULT 0.3,
			similarity_threshold FLOAT DEFAULT 0.2,
			field_config JSON,
			rerank_id VARCHAR(128),
			tenant_rerank_id BIGINT,
			top_k BIGINT DEFAULT 10,
			index_version VARCHAR(32) DEFAULT '1.0.0',
			status VARCHAR(1) DEFAULT '1',
			create_time BIGINT,
			create_date DATETIME,
			update_time BIGINT,
			update_date DATETIME,
			INDEX idx_tenant_id (tenant_id),
			INDEX idx_space_id (space_id),
			UNIQUE INDEX idx_tenant_space_embd (tenant_id, space_id, embd_id)
		)
		`
		if err := db.Exec(sql).Error; err != nil {
			common.Warn("Failed to create skill_search_configs table with MySQL dialect, trying generic", zap.Error(err))
			if err := db.AutoMigrate(&entity.SkillSearchConfig{}); err != nil {
				return err
			}
			// AutoMigrate 不建唯一索引，需显式 ADD UNIQUE
			common.Info("Creating unique indexes for skill_search_configs...")
			if err := db.Exec(`ALTER TABLE skill_search_configs ADD UNIQUE INDEX idx_tenant_space_embd (tenant_id, space_id, embd_id)`).Error; err != nil {
				return fmt.Errorf("failed to create unique index idx_tenant_space_embd: %w", err)
			}
		}
	} else {
		// 已有安装：补充 space_id 等列并迁移索引
		if err := addColumnIfNotExists(db, "skill_search_configs", "space_id", "VARCHAR(128) NOT NULL DEFAULT 'default'"); err != nil {
			return fmt.Errorf("failed to add space_id column to skill_search_configs: %w", err)
		}
		if err := addColumnIfNotExists(db, "skill_search_configs", "create_date", "DATETIME"); err != nil {
			return fmt.Errorf("failed to add create_date column to skill_search_configs: %w", err)
		}
		if err := addColumnIfNotExists(db, "skill_search_configs", "update_date", "DATETIME"); err != nil {
			return fmt.Errorf("failed to add update_date column to skill_search_configs: %w", err)
		}
		if err := db.Exec(`ALTER TABLE skill_search_configs MODIFY COLUMN update_time BIGINT`).Error; err != nil {
			common.Warn("Failed to modify skill_search_configs.update_time", zap.Error(err))
		}

		// 删除旧 idx_tenant_embd 唯一索引以支持按 space 配置
		var legacyIndexExists int64
		db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
			WHERE TABLE_NAME = 'skill_search_configs' AND INDEX_NAME = 'idx_tenant_embd'`).Scan(&legacyIndexExists)
		if legacyIndexExists > 0 {
			common.Info("Dropping legacy unique index idx_tenant_embd from skill_search_configs...")
			if err := db.Exec(`ALTER TABLE skill_search_configs DROP INDEX idx_tenant_embd`).Error; err != nil {
				return fmt.Errorf("failed to drop legacy unique index idx_tenant_embd: %w", err)
			}
		}

		// 表已存在：确保 idx_tenant_space_embd 唯一索引存在
		var indexExists int64
		db.Raw(`SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
			WHERE TABLE_NAME = 'skill_search_configs' AND INDEX_NAME = 'idx_tenant_space_embd'`).Scan(&indexExists)
		if indexExists == 0 {
			common.Info("Adding unique index idx_tenant_space_embd to skill_search_configs...")
			if err := db.Exec(`ALTER TABLE skill_search_configs 
				ADD UNIQUE INDEX idx_tenant_space_embd (tenant_id, space_id, embd_id)`).Error; err != nil {
				return fmt.Errorf("failed to add unique index idx_tenant_space_embd: %w", err)
			}
		}
	}

	return nil
}

// migrateSkillSpaceTables 创建或升级 skill_spaces 表。
func migrateSkillSpaceTables(db *gorm.DB) error {
	if !db.Migrator().HasTable("skill_spaces") {
		common.Info("Creating skill_spaces table...")
		sql := `
		CREATE TABLE IF NOT EXISTS skill_spaces (
			id VARCHAR(32) PRIMARY KEY,
			tenant_id VARCHAR(32) NOT NULL,
			name VARCHAR(128) NOT NULL,
			folder_id VARCHAR(32) NOT NULL,
			description TEXT,
			embd_id VARCHAR(128),
			rerank_id VARCHAR(128),
			top_k INT DEFAULT 10,
			status VARCHAR(1) DEFAULT '1',
			create_time BIGINT,
			create_date DATETIME,
			update_time BIGINT,
			update_date DATETIME,
			INDEX idx_tenant_id (tenant_id),
			UNIQUE INDEX idx_tenant_name_status (tenant_id, name, status)
		)
		`
		if err := db.Exec(sql).Error; err != nil {
			common.Warn("Failed to create skill_spaces table with MySQL dialect, trying generic", zap.Error(err))
			// MySQL DDL 失败时回退 GORM AutoMigrate
			if err := db.AutoMigrate(&entity.SkillSpace{}); err != nil {
				return err
			}
			// AutoMigrate doesn't create unique indexes, so create them explicitly
			common.Info("Creating unique indexes for skill_spaces...")
			if err := db.Exec(`ALTER TABLE skill_spaces ADD UNIQUE INDEX idx_tenant_name_status (tenant_id, name, status)`).Error; err != nil {
				return fmt.Errorf("failed to create unique index idx_tenant_name_status: %w", err)
			}
		}
	} else {
		// 已有表：先加 status 列再迁移唯一索引
		if err := addColumnIfNotExists(db, "skill_spaces", "status", "VARCHAR(1) NOT NULL DEFAULT '1'"); err != nil {
			return fmt.Errorf("failed to add status column to skill_spaces: %w", err)
		}
		if err := addColumnIfNotExists(db, "skill_spaces", "create_date", "DATETIME"); err != nil {
			return fmt.Errorf("failed to add create_date column to skill_spaces: %w", err)
		}
		if err := addColumnIfNotExists(db, "skill_spaces", "update_date", "DATETIME"); err != nil {
			return fmt.Errorf("failed to add update_date column to skill_spaces: %w", err)
		}
		if err := db.Exec(`ALTER TABLE skill_spaces MODIFY COLUMN update_time BIGINT`).Error; err != nil {
			common.Warn("Failed to modify skill_spaces.update_time", zap.Error(err))
		}
		// status 列就绪后迁移 idx_tenant_name → idx_tenant_name_status
		if err := migrateSkillSpaceIndex(db); err != nil {
			return fmt.Errorf("failed to migrate skill_space index: %w", err)
		}
	}

	return nil
}

// migrateSkillSpaceIndex 将唯一索引扩展为含 status 字段。
func migrateSkillSpaceIndex(db *gorm.DB) error {
	// 删除旧 idx_tenant_name 索引
	var oldIndexExists int64
	db.Raw(`
		SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
		WHERE TABLE_NAME = 'skill_spaces' AND INDEX_NAME = 'idx_tenant_name'
	`).Scan(&oldIndexExists)

	if oldIndexExists > 0 {
		common.Info("Dropping old idx_tenant_name index from skill_spaces...")
		if err := db.Exec(`DROP INDEX idx_tenant_name ON skill_spaces`).Error; err != nil {
			return fmt.Errorf("failed to drop old index idx_tenant_name: %w", err)
		}
	}

	// 不存在则创建 idx_tenant_name_status
	var newIndexExists int64
	db.Raw(`
		SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
		WHERE TABLE_NAME = 'skill_spaces' AND INDEX_NAME = 'idx_tenant_name_status'
	`).Scan(&newIndexExists)

	if newIndexExists == 0 {
		common.Info("Creating new idx_tenant_name_status index on skill_spaces...")
		if err := db.Exec(`CREATE UNIQUE INDEX idx_tenant_name_status ON skill_spaces(tenant_id, name, status)`).Error; err != nil {
			return fmt.Errorf("failed to create unique index idx_tenant_name_status: %w", err)
		}
	}

	return nil
}
