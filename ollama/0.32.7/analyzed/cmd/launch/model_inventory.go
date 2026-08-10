package launch

import (
	"context"
	"slices"
	"strings"
	"sync"

	"github.com/ollama/ollama/api"
	modelpkg "github.com/ollama/ollama/types/model"
)

// model_inventory 维护单次启动会话的模型清单，供集成配置写入与选择器解析。
// LaunchModel 是经清单解析后传给集成配置写入器的模型元数据。
// writers after resolving selected model names through the per-run inventory.
type LaunchModel struct {
	Name            string
	Remote          bool
	ToolCapable     bool
	Capabilities    []modelpkg.Capability
	ContextLength   int
	MaxOutputTokens int
	EmbeddingLength int
	Size            int64
	Details         api.ModelDetails
}

// modelInfo 为 LaunchModel 的内部别名。
type modelInfo = LaunchModel

// ModelInfo 对外 re-export LaunchModel，供调用方引用清单详情。
type ModelInfo = LaunchModel

// HasCapability 判断模型是否具备指定能力标签。
func (m LaunchModel) HasCapability(capability modelpkg.Capability) bool {
	return slices.Contains(m.Capabilities, capability)
}

// WithCloudLimits 为云端模型补全上下文与最大输出 token 限制。
func (m LaunchModel) WithCloudLimits() LaunchModel {
	if limit, ok := lookupCloudModelLimit(m.Name); ok {
		if m.ContextLength <= 0 {
			m.ContextLength = limit.Context
		}
		if m.MaxOutputTokens <= 0 {
			m.MaxOutputTokens = limit.Output
		}
	}
	return m
}

type modelInventory struct {
	client *api.Client

	mu     sync.Mutex
	loaded bool
	models []LaunchModel
	err    error
}

// newModelInventory 创建绑定 API 客户端的懒加载清单。
func newModelInventory(client *api.Client) *modelInventory {
	return &modelInventory{client: client}
}

// Load 首次或缓存命中时返回本地/远程模型列表副本。
func (i *modelInventory) Load(ctx context.Context) ([]LaunchModel, error) {
	return i.load(ctx, false)
}

// Refresh 强制重新 List 并更新缓存。
func (i *modelInventory) Refresh(ctx context.Context) ([]LaunchModel, error) {
	return i.load(ctx, true)
}

func (i *modelInventory) load(ctx context.Context, force bool) ([]LaunchModel, error) {
	if i == nil || i.client == nil {
		return nil, nil
	}

	i.mu.Lock()
	defer i.mu.Unlock()

	if i.loaded && !force {
		return cloneLaunchModels(i.models), i.err
	}

	resp, err := i.client.List(ctx)
	if err != nil {
		i.models = nil
		i.err = err
		i.loaded = true
		return nil, err
	}

	i.models = make([]LaunchModel, 0, len(resp.Models))
	for _, model := range resp.Models {
		i.models = append(i.models, launchModelFromListResponse(model))
	}
	i.err = nil
	i.loaded = true

	return cloneLaunchModels(i.models), i.err
}

// Resolve 将模型名列表解析为 LaunchModel；本地缺失时会 Refresh 重试。
func (i *modelInventory) Resolve(ctx context.Context, names []string) []LaunchModel {
	names = dedupeModelList(names)
	if len(names) == 0 {
		return nil
	}

	models, err := i.Load(ctx)
	if err != nil {
		models = nil
	}

	resolved, localMiss := resolveLaunchModels(names, models)
	if localMiss {
		if refreshed, err := i.Refresh(ctx); err == nil {
			resolved, _ = resolveLaunchModels(names, refreshed)
		}
	}
	return resolved
}

// resolveLaunchModels 按名称匹配清单；第二个返回值表示是否存在未解析的本地模型。
func resolveLaunchModels(names []string, models []LaunchModel) ([]LaunchModel, bool) {
	resolved := make([]LaunchModel, 0, len(names))
	localMiss := false
	for _, name := range names {
		if model, ok := findLaunchModel(models, name); ok {
			resolved = append(resolved, model.WithCloudLimits())
			continue
		}
		if !isCloudModelName(name) {
			localMiss = true
		}
		resolved = append(resolved, fallbackLaunchModel(name))
	}
	return resolved, localMiss
}

// launchModelFromListResponse 将 List API 响应转为 LaunchModel。
func launchModelFromListResponse(model api.ListModelResponse) LaunchModel {
	return LaunchModel{
		Name:            model.Name,
		Remote:          model.RemoteModel != "",
		ToolCapable:     slices.Contains(model.Capabilities, modelpkg.CapabilityTools),
		Capabilities:    append([]modelpkg.Capability(nil), model.Capabilities...),
		ContextLength:   model.Details.ContextLength,
		EmbeddingLength: model.Details.EmbeddingLength,
		Size:            model.Size,
		Details:         model.Details,
	}.WithCloudLimits()
}

// fallbackLaunchModel 在清单中找不到名称时的占位元数据。
func fallbackLaunchModel(name string) LaunchModel {
	return LaunchModel{Name: name, Remote: isCloudModelName(name)}.WithCloudLimits()
}

// findLaunchModel 在清单中按名称或 :latest 后缀匹配模型。
func findLaunchModel(models []LaunchModel, name string) (LaunchModel, bool) {
	for _, model := range models {
		if launchModelMatches(model.Name, name) {
			return cloneLaunchModel(model), true
		}
	}
	return LaunchModel{}, false
}

// launchModelMatches 比较候选名与用户输入名是否等价。
func launchModelMatches(candidate, name string) bool {
	if candidate == name {
		return true
	}
	return strings.TrimSuffix(candidate, ":latest") == name
}

func cloneLaunchModel(model LaunchModel) LaunchModel {
	model.Capabilities = append([]modelpkg.Capability(nil), model.Capabilities...)
	model.Details.Families = append([]string(nil), model.Details.Families...)
	return model
}

func cloneLaunchModels(models []LaunchModel) []LaunchModel {
	cloned := make([]LaunchModel, len(models))
	for i, model := range models {
		cloned[i] = cloneLaunchModel(model)
	}
	return cloned
}

func launchModelNames(models []LaunchModel) []string {
	names := make([]string, 0, len(models))
	for _, model := range models {
		if model.Name != "" {
			names = append(names, model.Name)
		}
	}
	return names
}

// launchModelsFromNames 由纯名称列表构造 fallback LaunchModel 切片。
func launchModelsFromNames(names []string) []LaunchModel {
	models := make([]LaunchModel, 0, len(names))
	for _, name := range names {
		if name == "" {
			continue
		}
		models = append(models, fallbackLaunchModel(name))
	}
	return models
}
