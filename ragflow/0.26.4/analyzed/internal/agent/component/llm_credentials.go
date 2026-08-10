// llm_credentials.go — 租户级 LLM API 凭证解析：画布 DSL 未携带 api_key/base_url 时从 tenant_llm 或 tenant_model_instance 回填。

package component

import (
	"context"
	"encoding/json"
	"strings"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"

	"go.uber.org/zap"
)

// resolveTenantLLMConfig fills tenant-scoped API credentials for the supplied
// driver/model pair when the canvas DSL omitted them. It first checks the old
// tenant_llm table, then falls back to tenant_model_provider +
// tenant_model_instance when the composite llm_id carries an instance name.
// resolveTenantLLMConfig 从画布 state 取 tenant_id 并解析缺失的 API 凭证。
func resolveTenantLLMConfig(ctx context.Context, driver, modelID, apiKey, baseURL, originalModelID string) (string, string) {
	if apiKey != "" || driver == "" || modelID == "" {
		return apiKey, baseURL
	}
	state, _, err := runtime.GetStateFromContext[*runtime.CanvasState](ctx)
	if err != nil || state == nil {
		common.Debug("llm credentials: no canvas state in ctx")
		return apiKey, baseURL
	}
	tid, _ := state.Sys["tenant_id"].(string)
	if tid == "" {
		common.Debug("llm credentials: state.Sys has no tenant_id")
		return apiKey, baseURL
	}

	if resolvedKey, resolvedBaseURL, ok := resolveTenantLLMCredentials(tid, driver, modelID, baseURL); ok {
		return resolvedKey, resolvedBaseURL
	}
	if originalModelID == "" {
		return apiKey, baseURL
	}
	if resolvedKey, resolvedBaseURL, ok := resolveTenantModelInstanceCredentials(tid, originalModelID, baseURL); ok {
		return resolvedKey, resolvedBaseURL
	}
	return apiKey, baseURL
}

// resolveTenantLLMCredentials looks up the old tenant_llm table for the given
// tenant / factory / model. Returns true when credentials were found.
// resolveTenantLLMCredentials 查旧 tenant_llm 表获取 api_key/base_url。
func resolveTenantLLMCredentials(tid, driver, modelID, baseURL string) (string, string, bool) {
	common.Debug("llm credentials: tenant_llm lookup", zap.String("tid", tid), zap.String("factory", driver), zap.String("model", modelID))
	row, err := dao.NewTenantLLMDAO().GetByTenantFactoryAndModelName(tid, driver, modelID)
	if err != nil {
		common.Debug("llm credentials: tenant_llm lookup", zap.Error(err))
		return "", baseURL, false
	}
	if row == nil {
		common.Debug("llm credentials: tenant_llm lookup: no row")
		return "", baseURL, false
	}

	apiKey := ""
	if row.APIKey != nil {
		apiKey = *row.APIKey
	}
	if baseURL == "" && row.APIBase != nil {
		baseURL = *row.APIBase
	}
	common.Debug("llm credentials: tenant_llm OK",
		zap.Bool("api_key_present", apiKey != ""),
		zap.Bool("base_url_present", baseURL != ""))
	return apiKey, baseURL, apiKey != ""
}

// resolveTenantModelInstanceCredentials attempts to resolve llm credentials
// through tenant_model_provider + tenant_model_instance using the original
// composite llm_id (which still carries the instance name).
// resolveTenantModelInstanceCredentials 经 tenant_model_provider + instance 解析凭证。
func resolveTenantModelInstanceCredentials(tid, compositeLLMID, baseURL string) (string, string, bool) {
	modelName, instanceName, providerName := parseLLMIDParts(compositeLLMID)
	if instanceName == "" {
		common.Debug("llm credentials: new-table fallback skipped: no instance name", zap.String("composite_llm_id", compositeLLMID))
		return "", baseURL, false
	}

	common.Debug("llm credentials: new-table fallback",
		zap.String("tid", tid),
		zap.String("provider", providerName),
		zap.String("model", modelName),
		zap.String("instance", instanceName))

	provider, err := dao.NewTenantModelProviderDAO().GetByTenantIDAndProviderName(tid, providerName)
	if err != nil || provider == nil {
		common.Debug("llm credentials: new-table fallback: provider not found", zap.String("provider", providerName), zap.Error(err))
		return "", baseURL, false
	}

	instance, err := dao.NewTenantModelInstanceDAO().GetByProviderIDAndInstanceName(provider.ID, instanceName)
	if err != nil || instance == nil {
		if instanceName == "default" {
			if fallback := findSoleActiveProviderInstance(provider.ID); fallback != nil {
				common.Debug("llm credentials: new-table fallback: remapped default instance to sole active instance",
					zap.String("instance", fallback.InstanceName),
					zap.String("provider", providerName))
				instance = fallback
				err = nil
			}
		}
	}
	if err != nil || instance == nil {
		common.Debug("llm credentials: new-table fallback: instance not found",
			zap.String("instance", instanceName),
			zap.String("provider", providerName),
			zap.Error(err))
		return "", baseURL, false
	}

	apiKey := instance.APIKey
	if instance.Extra != "" && baseURL == "" {
		var extra map[string]string
		if err := json.Unmarshal([]byte(instance.Extra), &extra); err == nil {
			if u := extra["base_url"]; u != "" {
				baseURL = u
			}
		}
	}

	common.Debug("llm credentials: new-table OK",
		zap.String("provider", providerName),
		zap.String("instance", instance.InstanceName),
		zap.Bool("api_key_present", apiKey != ""),
		zap.Bool("base_url_present", baseURL != ""))
	return apiKey, baseURL, apiKey != ""
}

func findSoleActiveProviderInstance(providerID string) *entity.TenantModelInstance {
	instances, err := dao.NewTenantModelInstanceDAO().GetAllInstancesByProviderID(providerID)
	if err != nil {
		common.Debug("llm credentials: list provider instances", zap.Error(err))
		return nil
	}
	active := make([]*entity.TenantModelInstance, 0, len(instances))
	for _, inst := range instances {
		if inst == nil {
			continue
		}
		if strings.EqualFold(strings.TrimSpace(inst.Status), "inactive") {
			continue
		}
		active = append(active, inst)
	}
	if len(active) != 1 {
		return nil
	}
	return active[0]
}

// parseLLMIDParts splits a composite llm_id into model, instance, and
// provider segments.
//
//	"model@provider"          -> ("model", "default", "provider")
//	"model@instance@provider" -> ("model", "instance", "provider")
//	4+ parts                  -> ("parts[0]", "parts[1]", "parts[2]")
// parseLLMIDParts 将复合 llm_id 拆为 model、instance、provider 三段。
func parseLLMIDParts(s string) (modelName, instanceName, providerName string) {
	parts := strings.Split(strings.TrimSpace(s), "@")
	switch len(parts) {
	case 2:
		return parts[0], "default", parts[1]
	case 3:
		return parts[0], parts[1], parts[2]
	default:
		if len(parts) >= 4 {
			return parts[0], parts[1], parts[2]
		}
		return s, "", ""
	}
}
