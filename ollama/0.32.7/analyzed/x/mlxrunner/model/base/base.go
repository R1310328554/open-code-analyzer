// MLX 模型基础接口：注册表、权重加载与 draft 构造。
package base

import (
	"encoding/json"
	"fmt"
	"log/slog"
	"sync"

	"github.com/ollama/ollama/x/mlxrunner/batch"
	"github.com/ollama/ollama/x/mlxrunner/cache"
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/mlxrunner/model"
	"github.com/ollama/ollama/x/tokenizer"
)

// Model 为各架构实现必须满足的接口。
// Model is the interface that model implementations must satisfy.
type Model interface {
	// LoadWeights 从 manifest 张量映射加载到模型字段（含 MLA/MoE/量化逻辑）。
	// LoadWeights receives all tensors loaded from the manifest and assigns
	// them to model fields. Model-specific logic (MLA absorption, expert
	// stacking, quantized layer creation) happens here.
	LoadWeights(tensors map[string]*mlx.Array) error

	// NewCaches 构造本模型各层所需的 cache 槽位。
	// NewCaches builds the cache slots this model's layers need.
	NewCaches() []cache.Cache

	// Forward 返回用于 unembed 的 hidden 与 draft 条件 hidden；普通模型二者相同。
	// Forward returns the hidden state to unembed and the state a draft
	// model conditions on; plain models return the final hidden for both.
	Forward(b *batch.Batch, cache []cache.Cache) (hidden, auxHidden *mlx.Array)
	Unembed(x *mlx.Array) *mlx.Array

	Tokenizer() *tokenizer.Tokenizer
	MaxContextLength() int
}

// DraftModel 与 target 并行，负责 speculative token 提议。
// DraftModel is an auxiliary model alongside a target that proposes speculative
// tokens.
type DraftModel interface {
	// LoadWeights 加载 draft 权重；inline head 随 target 一起加载。
	// LoadWeights assigns manifest tensors to the draft model's fields. An
	// inline head has nothing to do here; its weights load with the target's.
	LoadWeights(tensors map[string]*mlx.Array) error

	// NewCaches 构造 draft 写入的 cache；无 KV 时可 nil。
	// NewCaches builds the cache slots this draft model writes, or nil
	// when it keeps no KV.
	NewCaches() []cache.Cache

	// Forward 消费 b.Hidden，可读 target cache；返回 hidden 与下一步 aux hidden。
	// Forward consumes b.Hidden (the draft-conditioning state) and returns
	// its hidden plus the aux hidden that seeds the next step. targetCaches
	// is read-only, for drafts that attend over the target's history.
	Forward(b *batch.Batch, targetCaches, draftCaches []cache.Cache) (hidden, auxHidden *mlx.Array)

	// Unembed 将 hidden 投影为词表 logits。
	// Unembed projects a hidden state to vocabulary logits.
	Unembed(x *mlx.Array) *mlx.Array
}

// BlockDraft 每步前向 draft 整块（块扩散），条件来自 target 中间层而非末 hidden。
// BlockDraft is a DraftModel that drafts a whole block per forward (block
// diffusion), conditioned on features tapped from target layers rather than
// the final hidden state.
type BlockDraft interface {
	DraftModel

	// BlockParams 返回训练 block 长度与 mask token ID。
	// BlockParams returns the trained block length and the mask token
	// standing in for undrafted positions.
	BlockParams() (blockSize int, maskToken int32)
}

// SelfDraft 表示 draft head 与 target 权重同包；无 checkpoint 时返回 nil。
// SelfDraft is implemented by models whose draft head ships inline with the
// target weights; it returns the head, or nil when the checkpoint shipped none.
type SelfDraft interface {
	SelfDraft() DraftModel
}

var (
	mu            sync.Mutex
	registry      = make(map[string]func(root *model.Root) (Model, error))
	draftRegistry = make(map[string]func(root *model.Root, target Model) (DraftModel, error))
)

// Register 按架构名注册模型构造函数；重复注册 panic。
// Register registers a model constructor by architecture name.
// 由各 model 包 init 调用。
// Called from init() in model packages. Panics on duplicate registration.
func Register(arch string, fn func(root *model.Root) (Model, error)) {
	mu.Lock()
	defer mu.Unlock()

	if _, exists := registry[arch]; exists {
		panic(fmt.Sprintf("model architecture %q already registered", arch))
	}
	registry[arch] = fn
}

// RegisterDraft 注册 draft 模型构造函数。
// RegisterDraft registers a draft model constructor by architecture name.
func RegisterDraft(arch string, fn func(root *model.Root, target Model) (DraftModel, error)) {
	mu.Lock()
	defer mu.Unlock()

	if _, exists := draftRegistry[arch]; exists {
		panic(fmt.Sprintf("draft model architecture %q already registered", arch))
	}
	draftRegistry[arch] = fn
}

// New 读 config.json、解析架构并调用已注册构造函数（尚未加载权重）。
// New reads config.json from the manifest, detects the architecture, looks up
// the registered constructor, and calls it to create the model (with config
// parsed and struct created, but weights not yet loaded).
func New(root *model.Root) (Model, error) {
	configData, err := root.Manifest.ReadConfig("config.json")
	if err != nil {
		return nil, fmt.Errorf("failed to read config.json: %w", err)
	}

	var archConfig struct {
		Architectures []string `json:"architectures"`
	}
	if err := json.Unmarshal(configData, &archConfig); err != nil {
		return nil, fmt.Errorf("failed to parse config.json: %w", err)
	}

	if len(archConfig.Architectures) == 0 {
		return nil, fmt.Errorf("no architectures found in config.json")
	}

	arch := archConfig.Architectures[0]
	slog.Info("Model architecture", "arch", arch)

	mu.Lock()
	fn, ok := registry[arch]
	mu.Unlock()

	if !ok {
		return nil, fmt.Errorf("unsupported architecture: %s", arch)
	}

	return fn(root)
}

// NewDraft 按 manifest draft 配置构造 draft 模型；无 draft 返回 nil。
// NewDraft constructs the draft model described by the manifest config, if any.
func NewDraft(root *model.Root, target Model) (DraftModel, error) {
	if root == nil || root.Draft == nil {
		return nil, nil
	}

	configPath := root.Draft.Config
	if configPath == "" {
		configPath = "draft/config.json"
	}
	configData, err := root.Manifest.ReadConfig(configPath)
	if err != nil {
		return nil, fmt.Errorf("failed to read %s: %w", configPath, err)
	}

	var archConfig struct {
		Architectures []string `json:"architectures"`
		ModelType     string   `json:"model_type"`
	}
	if err := json.Unmarshal(configData, &archConfig); err != nil {
		return nil, fmt.Errorf("failed to parse %s: %w", configPath, err)
	}

	arch := root.Draft.Architecture
	if arch == "" && len(archConfig.Architectures) > 0 {
		arch = archConfig.Architectures[0]
	}
	if arch == "" {
		arch = archConfig.ModelType
	}
	if arch == "" {
		return nil, fmt.Errorf("no draft architecture found in %s", configPath)
	}
	slog.Info("Draft model architecture", "arch", arch)

	mu.Lock()
	fn, ok := draftRegistry[arch]
	mu.Unlock()
	if !ok {
		return nil, fmt.Errorf("unsupported draft architecture: %s", arch)
	}

	return fn(root, target)
}

// Weights 返回加载函数：LoadWeights 后 pin 模型内 Array 并 sweep 其余。
// Weights returns a function that loads model weights, then pins all
// arrays reachable from the model struct and sweeps everything else.
func Weights(m Model) func(map[string]*mlx.Array) error {
	return func(tensors map[string]*mlx.Array) error {
		if err := m.LoadWeights(tensors); err != nil {
			return err
		}

		collected := mlx.Collect(m)
		for _, arr := range collected {
			mlx.Pin(arr)
		}
		mlx.Sweep()
		mlx.Eval(collected...)

		return nil
	}
}
