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
// canvas_template_seed.go — 内置 Agent 模板种子数据：从 agent/templates/*.json 导入 canvas_template 表，对齐 Python init_data。

//

package dao

import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"

	"ragflow/internal/common"
	"ragflow/internal/entity"

	"go.uber.org/zap"
)

// SeedCanvasTemplates 从 agent/templates/*.json 种子化 canvas_template 表；对齐 Python init_data.add_graph_templates()。
func SeedCanvasTemplates() error {
	dir := findAgentTemplatesDir()
	if dir == "" {
		common.Warn("Agent templates directory not found, skipping canvas template seeding")
		return nil
	}

	entries, err := os.ReadDir(dir)
	if err != nil {
		return fmt.Errorf("failed to read agent templates directory %s: %w", dir, err)
	}

	// 先清空表（对齐 Python filter_delete），使移除/更新的内置模板生效。
	if err := DB.Exec("DELETE FROM canvas_template").Error; err != nil {
		return fmt.Errorf("failed to clear canvas_template: %w", err)
	}

	sort.Slice(entries, func(i, j int) bool { return entries[i].Name() < entries[j].Name() })

	var seeded int
	for _, entry := range entries {
		if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".json") {
			continue
		}
		path := filepath.Join(dir, entry.Name())
		raw, err := os.ReadFile(path)
		if err != nil {
			common.Warn("Failed to read agent template", zap.String("file", path), zap.Error(err))
			continue
		}
		tmpl, err := parseCanvasTemplateFile(raw)
		if err != nil {
			common.Warn("Failed to parse agent template", zap.String("file", path), zap.Error(err))
			continue
		}
		if err := DB.Create(tmpl).Error; err != nil {
			common.Warn("Failed to save agent template", zap.String("file", path), zap.Error(err))
			continue
		}
		seeded++
	}

	common.Info("Seeded canvas templates", zap.Int("count", seeded), zap.String("dir", dir))
	return nil
}

// parseCanvasTemplateFile 解析单个 JSON 模板文件为 CanvasTemplate 实体。
func parseCanvasTemplateFile(raw []byte) (*entity.CanvasTemplate, error) {
	decoder := json.NewDecoder(bytes.NewReader(raw))
	decoder.UseNumber()
	var data map[string]any
	if err := decoder.Decode(&data); err != nil {
		return nil, err
	}

	tmpl := &entity.CanvasTemplate{
		CanvasCategory: "agent_canvas",
	}

	if v, ok := data["id"]; ok {
		tmpl.ID = fmt.Sprint(v)
	}
	if v, ok := data["title"].(map[string]any); ok {
		tmpl.Title = entity.JSONMap(v)
	}
	if v, ok := data["description"].(map[string]any); ok {
		tmpl.Description = entity.JSONMap(v)
	}
	if v, ok := data["avatar"].(string); ok && v != "" {
		tmpl.Avatar = &v
	}
	if v, ok := data["canvas_category"].(string); ok && v != "" {
		tmpl.CanvasCategory = v
	}

	canvasTypes := collectCanvasTypes(data["canvas_type"], data["canvas_types"])
	if len(canvasTypes) > 0 {
		tmpl.CanvasTypes = canvasTypes
		if first, ok := canvasTypes[0].(string); ok {
			tmpl.CanvasType = &first
		}
	}

	if v, ok := data["dsl"].(map[string]any); ok {
		tmpl.DSL = entity.JSONMap(v)
	}

	return tmpl, nil
}

// collectCanvasTypes 去重合并 canvas_type/canvas_types 为 JSONSlice。
func collectCanvasTypes(rawType, rawTypes any) entity.JSONSlice {
	seen := make(map[string]struct{})
	var result entity.JSONSlice

	add := func(s string) {
		s = strings.TrimSpace(s)
		if s == "" {
			return
		}
		if _, ok := seen[s]; ok {
			return
		}
		seen[s] = struct{}{}
		result = append(result, s)
	}

	if s, ok := rawType.(string); ok {
		add(s)
	}

	switch v := rawTypes.(type) {
	case []any:
		for _, item := range v {
			if s, ok := item.(string); ok {
				add(s)
			}
		}
	case []string:
		for _, s := range v {
			add(s)
		}
	case string:
		add(v)
	}

	return result
}

// findAgentTemplatesDir 在多个相对路径中查找 agent/templates 目录。
func findAgentTemplatesDir() string {
	candidates := []string{
		"agent/templates",
		filepath.Join("..", "agent", "templates"),
		filepath.Join("..", "..", "agent", "templates"),
		filepath.Join("..", "..", "..", "agent", "templates"),
	}
	for _, candidate := range candidates {
		if info, err := os.Stat(candidate); err == nil && info.IsDir() {
			return candidate
		}
	}
	return ""
}
