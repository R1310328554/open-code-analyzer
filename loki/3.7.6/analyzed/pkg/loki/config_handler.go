package loki

// config_handler 提供 /config 与租户限额 HTTP 端点：支持 diff/defaults 模式对比配置，并按 allowlist 过滤对外发布的 limits 字段。

import (
	"encoding/json"
	"fmt"
	"net/http"
	"reflect"

	"github.com/grafana/dskit/tenant"
	"gopkg.in/yaml.v2"

	"github.com/grafana/loki/v3/pkg/util/build"
	"github.com/grafana/loki/v3/pkg/validation"
)

// yamlMarshalUnmarshal 通过 YAML 往返序列化将任意配置结构转为 map，便于递归 diff。
func yamlMarshalUnmarshal(in interface{}) (map[interface{}]interface{}, error) {
	yamlBytes, err := yaml.Marshal(in)
	if err != nil {
		return nil, err
	}

	object := make(map[interface{}]interface{})
	if err := yaml.Unmarshal(yamlBytes, object); err != nil {
		return nil, err
	}

	return object, nil
}

// diffConfig 递归比较实际配置与默认值，仅输出与默认不同的键值对。
func diffConfig(defaultConfig, actualConfig map[interface{}]interface{}) (map[interface{}]interface{}, error) {
	output := make(map[interface{}]interface{})

	for key, value := range actualConfig {

		defaultValue, ok := defaultConfig[key]
		if !ok {
			output[key] = value
			continue
		}

		switch v := value.(type) {
		case int:
			defaultV, ok := defaultValue.(int)
			if !ok || defaultV != v {
				output[key] = v
			}
		case string:
			defaultV, ok := defaultValue.(string)
			if !ok || defaultV != v {
				output[key] = v
			}
		case bool:
			defaultV, ok := defaultValue.(bool)
			if !ok || defaultV != v {
				output[key] = v
			}
		case []interface{}:
			defaultV, ok := defaultValue.([]interface{})
			if !ok || !reflect.DeepEqual(defaultV, v) {
				output[key] = v
			}
		case float64:
			defaultV, ok := defaultValue.(float64)
			if !ok || !reflect.DeepEqual(defaultV, v) {
				output[key] = v
			}
		case map[interface{}]interface{}:
			defaultV, ok := defaultValue.(map[interface{}]interface{})
			if !ok {
				output[key] = value
			}
			diff, err := diffConfig(defaultV, v)
			if err != nil {
				return nil, err
			}
			if len(diff) > 0 {
				output[key] = diff
			}
		default:
			return nil, fmt.Errorf("unsupported type %T", v)
		}
	}

	return output, nil
}

// configHandler 根据 query mode 返回完整配置、默认值或 diff 结果，以 YAML 文本响应。
func configHandler(actualCfg any, defaultCfg any) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var output any
		switch r.URL.Query().Get("mode") {
		case "diff":
			defaultCfgObj, err := yamlMarshalUnmarshal(defaultCfg)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			actualCfgObj, err := yamlMarshalUnmarshal(actualCfg)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			diff, err := diffConfig(defaultCfgObj, actualCfgObj)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}
			output = diff

		case "defaults":
			output = defaultCfg
		default:
			output = actualCfg
		}

		writeYAMLResponse(w, output)
	}
}

func filterLimitFields(limits any, allowlist []string) (map[string]any, error) {
	// filterLimitFields 经 JSON 序列化获取 limits 的 JSON 字段名，避免 YAML 键名不一致。
// Convert limits to map via JSON marshaling to get proper field names
	// This avoids YAML conversion and gives us the JSON field names directly
	jsonBytes, err := json.Marshal(limits)
	if err != nil {
		return nil, err
	}

	var limitsMap map[string]any
	if err := json.Unmarshal(jsonBytes, &limitsMap); err != nil {
		return nil, err
	}

	// If no allowlist, return all fields
	if len(allowlist) == 0 {
		return limitsMap, nil
	}

	// Create allowlist set for O(1) lookup
	allowSet := make(map[string]bool)
	for _, field := range allowlist {
		allowSet[field] = true
	}

	// Filter to only allowed fields
	filtered := make(map[string]any)
	for key, value := range limitsMap {
		if allowSet[key] {
			filtered[key] = value
		}
	}

	return filtered, nil
}

// tenantLimitsHandler 按租户解析 limits，支持 drilldown 场景返回 JSON 包装（含版本与 pattern 开关）。
func (t *Loki) tenantLimitsHandler(forDrilldown bool) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		user, _, err := tenant.ExtractTenantIDFromHTTPRequest(r)
		if err != nil {
			http.Error(w, err.Error(), http.StatusUnauthorized)
			return
		}

		// Get tenant limits or defaults
		var limit *validation.Limits
		if t.TenantLimits != nil {
			limit = t.TenantLimits.TenantLimits(user)
		}
		if limit == nil && t.Overrides != nil {
			// There is no limit for this tenant, so we default to the default limits.
			limit = t.Overrides.DefaultLimits()
		}
		if limit == nil {
			// This should not happen, but we handle it gracefully.
			http.Error(w, "No default limits configured", http.StatusNotFound)
			return
		}

		// Apply allowlist filtering if configured
		allowlist := t.Cfg.TenantLimitsAllowPublish
		filteredLimits, err := filterLimitFields(limit, allowlist)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		if !forDrilldown {
			writeYAMLResponse(w, filteredLimits)
			return
		}

		// Build response
		version := build.GetVersion().Version
		if version == "" {
			version = "unknown"
		}
		response := DrilldownConfigResponse{
			Limits:                 filteredLimits,
			PatternIngesterEnabled: t.Cfg.Pattern.Enabled,
			Version:                version,
		}

		// Return JSON response
		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(response); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
}

// writeYAMLResponse 以 text/plain 输出 YAML，便于浏览器直接展示配置内容。
// writeYAMLResponse writes some YAML as a HTTP response.
func writeYAMLResponse(w http.ResponseWriter, v any) {
	// There is not standardised content-type for YAML, text/plain ensures the
	// YAML is displayed in the browser instead of offered as a download
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")

	data, err := yaml.Marshal(v)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	// We ignore errors here, because we cannot do anything about them.
	// Write will trigger sending Status code, so we cannot send a different status code afterwards.
	// Also this isn't internal error, but error communicating with client.
	_, _ = w.Write(data)
}
// 租户无专属 limits 时回退到 DefaultLimits；allowlist 为空则发布全部 limit 字段。
