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
// registry.go — Agent 工具工厂注册表：按 DSL 可见名称解析为 eino BaseTool 实例。

//

package tool

import (
	"fmt"
	"strings"

	einotool "github.com/cloudwego/eino/components/tool"
)

// Factory builds a tool instance by DSL / Agent-visible name and
// optional node-level configuration. The config map belongs to the
// Agent node / DSL, not to the model-emitted function-call args.
// Factory 按节点级配置 map 构造工具实例，非模型 function-call 参数。
type Factory func(params map[string]any) (einotool.BaseTool, error)

var registry = map[string]Factory{
	"akshare":           buildAkShareTool,
	"arxiv":             noConfig("arxiv", func() einotool.BaseTool { return NewArxivTool() }),
	"bgpt":              noConfig("bgpt", func() einotool.BaseTool { return NewBGPTTool() }),
	"code_exec":         noConfig("code_exec", func() einotool.BaseTool { return NewCodeExecTool() }),
	"crawler":           noConfig("crawler", func() einotool.BaseTool { return NewCrawlerTool() }),
	"deepl":             noConfig("deepl", func() einotool.BaseTool { return NewDeepLTool() }),
	"duckduckgo":        noConfig("duckduckgo", func() einotool.BaseTool { return NewDuckDuckGoTool() }),
	"email":             noConfig("email", func() einotool.BaseTool { return NewEmailTool() }),
	"execute_sql":       buildExeSQLTool,
	"exesql":            buildExeSQLTool,
	"github":            noConfig("github", func() einotool.BaseTool { return NewGitHubTool() }),
	"google":            noConfig("google", func() einotool.BaseTool { return NewGoogleTool() }),
	"google_scholar":    noConfig("google_scholar", func() einotool.BaseTool { return NewGoogleScholarTool() }),
	"jin10":             noConfig("jin10", func() einotool.BaseTool { return NewJin10Tool() }),
	"keenable":          buildKeenableTool,
	"pubmed":            noConfig("pubmed", func() einotool.BaseTool { return NewPubMedTool() }),
	"qweather":          noConfig("qweather", func() einotool.BaseTool { return NewQWeatherTool() }),
	"retrieval":         noConfig("retrieval", func() einotool.BaseTool { return NewRetrievalTool() }),
	"search_my_dataset": noConfig("search_my_dataset", func() einotool.BaseTool { return NewRetrievalTool() }),
	"search_my_dateset": noConfig("search_my_dateset", func() einotool.BaseTool { return NewRetrievalTool() }),
	"searxng":           noConfig("searxng", func() einotool.BaseTool { return NewSearXNGTool() }),
	"tavily":            noConfig("tavily", func() einotool.BaseTool { return NewTavilyTool() }),
	"tushare":           noConfig("tushare", func() einotool.BaseTool { return NewTushareTool() }),
	"wencai":            noConfig("wencai", func() einotool.BaseTool { return NewWencaiTool() }),
	"web_crawler":       noConfig("web_crawler", func() einotool.BaseTool { return NewCrawlerTool() }),
	"wikipedia":         noConfig("wikipedia", func() einotool.BaseTool { return NewWikipediaTool() }),
	"yahoo_finance":     noConfig("yahoo_finance", func() einotool.BaseTool { return NewYahooFinanceTool() }),
}

// noConfig 包装无节点级参数的工具工厂。
func noConfig(name string, fn func() einotool.BaseTool) Factory {
	return func(params map[string]any) (einotool.BaseTool, error) {
		if len(params) != 0 {
			return nil, fmt.Errorf("agent tool: tool %q does not accept node-level params", name)
		}
		return fn(), nil
	}
}

// BuildByName resolves a tool name into an Eino BaseTool.
// BuildByName 按名称（大小写不敏感）查找工厂并构造工具。
func BuildByName(name string, params map[string]any) (einotool.BaseTool, error) {
	key := strings.ToLower(strings.TrimSpace(name))
	if key == "" {
		return nil, fmt.Errorf("agent tool: empty tool name")
	}
	factory, ok := registry[key]
	if !ok {
		return nil, fmt.Errorf("agent tool: unsupported tool %q", name)
	}
	if factory == nil {
		return nil, fmt.Errorf("agent tool: nil factory for %q", name)
	}
	return factory(params)
}

// BuildAll resolves a list of tool names into Eino BaseTool instances.
// perToolParams is keyed by the Agent-visible tool name.
// BuildAll 批量解析工具名列表，perToolParams 按名称索引节点配置。
func BuildAll(names []string, perToolParams map[string]map[string]any) ([]einotool.BaseTool, error) {
	if len(names) == 0 {
		return nil, nil
	}
	tools := make([]einotool.BaseTool, 0, len(names))
	for _, name := range names {
		var params map[string]any
		if perToolParams != nil {
			params = perToolParams[strings.ToLower(strings.TrimSpace(name))]
			if params == nil {
				params = perToolParams[name]
			}
		}
		t, err := BuildByName(name, params)
		if err != nil {
			return nil, err
		}
		tools = append(tools, t)
	}
	return tools, nil
}

// buildAkShareTool 解析 top_n 节点参数并构造 AkShare 工具。
func buildAkShareTool(params map[string]any) (einotool.BaseTool, error) {
	topN := defaultAkShareTopN
	if len(params) != 0 {
		for key := range params {
			if key != "top_n" {
				return nil, fmt.Errorf("agent tool: tool %q only accepts node-level param top_n", "akshare")
			}
		}
		if v, ok := intParam(params, "top_n"); ok {
			topN = v
		}
		if topN <= 0 {
			return nil, fmt.Errorf("agent tool: tool %q requires positive integer node-level param top_n", "akshare")
		}
	}
	return NewAkShareToolWithTopN(nil, topN), nil
}

// buildExeSQLTool 解码数据库连接参数并构造 ExeSQL 工具。
func buildExeSQLTool(params map[string]any) (einotool.BaseTool, error) {
	conn, err := decodeExeSQLConnParams(params)
	if err != nil {
		return nil, err
	}
	return NewExeSQLTool(conn), nil
}

// buildKeenableTool 可选注入 api_key 节点参数。
func buildKeenableTool(params map[string]any) (einotool.BaseTool, error) {
	if len(params) == 0 {
		return NewKeenableTool(), nil
	}
	for key := range params {
		if key != "api_key" {
			return nil, fmt.Errorf("agent tool: tool %q only accepts node-level param api_key", "keenable")
		}
	}
	apiKey, ok := params["api_key"].(string)
	if !ok || strings.TrimSpace(apiKey) == "" {
		return nil, fmt.Errorf("agent tool: tool %q requires non-empty string node-level param api_key", "keenable")
	}
	return NewKeenableToolWithAPIKey(nil, apiKey), nil
}

// decodeExeSQLConnParams 从节点 map 解析 SQL 连接配置。
func decodeExeSQLConnParams(params map[string]any) (exesqlConnParams, error) {
	if len(params) == 0 {
		return exesqlConnParams{}, fmt.Errorf(
			"agent tool: execute_sql requires node-level params " +
				"(db_type/host/port/database/username/password)",
		)
	}
	conn := exesqlConnParams{}
	if v, ok := stringParam(params, "db_type"); ok {
		conn.DBType = v
	}
	if v, ok := stringParam(params, "database"); ok {
		conn.Database = v
	}
	if v, ok := stringParam(params, "username"); ok {
		conn.Username = v
	}
	if v, ok := stringParam(params, "host"); ok {
		conn.Host = v
	}
	if v, ok := intParam(params, "port"); ok {
		conn.Port = v
	}
	if v, ok := stringParam(params, "password"); ok {
		conn.Password = v
	}
	if v, ok := intParam(params, "max_records"); ok {
		conn.MaxRecords = v
	}
	if err := conn.check(); err != nil {
		return exesqlConnParams{}, fmt.Errorf("agent tool: execute_sql config: %w", err)
	}
	return conn, nil
}

// stringParam 安全读取 map 中的字符串参数。
func stringParam(params map[string]any, key string) (string, bool) {
	v, ok := params[key]
	if !ok {
		return "", false
	}
	s, ok := v.(string)
	return s, ok
}

// intParam 安全读取 map 中的整型参数，容忍 JSON 数字类型。
func intParam(params map[string]any, key string) (int, bool) {
	v, ok := params[key]
	if !ok {
		return 0, false
	}
	switch x := v.(type) {
	case int:
		return x, true
	case int32:
		return int(x), true
	case int64:
		return int(x), true
	case float64:
		return int(x), true
	default:
		return 0, false
	}
}
