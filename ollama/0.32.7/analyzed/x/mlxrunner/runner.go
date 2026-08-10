// MLX Runner 核心：模型加载、manifest 张量映射、wired 内存配置与请求调度。
package mlxrunner

import (
	"context"
	"errors"
	"log/slog"
	"net"
	"net/http"
	"slices"
	"strings"

	"golang.org/x/sync/errgroup"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/x/internal/mlxthread"
	"github.com/ollama/ollama/x/mlxrunner/cache"
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/mlxrunner/model"
	"github.com/ollama/ollama/x/mlxrunner/model/base"
	"github.com/ollama/ollama/x/mlxrunner/sample"
	"github.com/ollama/ollama/x/tokenizer"
)

// Request 在 HTTP 与 runner 协程间传递补全请求；Ctx 跨 channel 传播取消。
// Request is a short-lived struct that carries a completion request through
// a channel from the HTTP handler to the runner goroutine. The ctx field
// must travel with the request so that cancellation propagates across the
// channel boundary.
// Request 携带补全参数、响应通道、pipeline 与采样选项。
type Request struct {
	CompletionRequest
	Responses chan CompletionResponse
	Pipeline  func(context.Context, Request) error

	Ctx         context.Context //nolint:containedctx // Queued requests carry caller cancellation to the runner.
	Tokens      []int32
	MediaItems  []mediaItem
	Layout      any // opaque PrepareMedia layout state, stamped on every batch
	SamplerOpts sample.Options
}

// Runner 持有模型、分词器、前缀缓存与投机解码子系统。
type Runner struct {
	Model         base.Model
	Tokenizer     *tokenizer.Tokenizer
	Requests      chan Request
	Sampler       *sample.Sampler
	cache         *prefixCache
	contextLength int
	mlxThread     *mlxthread.Thread
	// spec 为投机解码子系统；checkpoint 无 draft head 时为 nil。
	// spec is the speculative-decoding subsystem. Nil when the model ships no
	// draft head.
	spec *speculation
}

// Load 加载 target/draft、Pin 权重、初始化缓存与投机解码并启用 Compile。
func (r *Runner) Load(modelName string) error {
	root, err := model.Open(modelName)
	if err != nil {
		return err
	}
	defer root.Close()

	m, err := base.New(root)
	if err != nil {
		return err
	}

	// Load all tensor blobs from manifest
	tensors, err := loadTensorsFromManifest(root)
	if err != nil {
		return err
	}

	// Assign weights to model (model-specific logic). Target and draft weights
	// must be loaded before sweeping so tensors from a combined manifest are
	// not discarded before the draft model can retain them.
	if err := m.LoadWeights(tensors); err != nil {
		return err
	}

	var draftModel base.DraftModel
	draft, err := base.NewDraft(root, m)
	if err != nil {
		return err
	}
	if draft != nil {
		if err := draft.LoadWeights(tensors); err != nil {
			return err
		}
		draftModel = draft
	} else if sd, ok := m.(base.SelfDraft); ok {
		// Inline draft head: already loaded with the target; nil if none shipped.
		draftModel = sd.SelfDraft()
	}

	collected := mlx.Collect(m)
	if draft != nil {
		draftArrays := mlx.Collect(draft)
		collected = append(collected, draftArrays...)
		if root.Draft != nil {
			slog.Info("Loaded draft model", "tensor_prefix", root.Draft.TensorPrefix, "config", root.Draft.Config, "arrays", len(draftArrays))
		} else {
			slog.Info("Loaded draft model", "arrays", len(draftArrays))
		}
	}
	for _, arr := range collected {
		mlx.Pin(arr)
	}
	mlx.Sweep()
	mlx.Eval(collected...)
	configureWiredMemory()

	r.Model = m
	r.Tokenizer = m.Tokenizer()
	r.contextLength = m.MaxContextLength()
	caches := m.NewCaches()
	draftCaches := newDraftCaches(draftModel)
	r.cache = newPrefixCache(slices.Concat(caches, draftCaches))
	r.Sampler = sample.New(r.contextLength)
	r.spec = newSpeculation(r, draftModel, caches, draftCaches)

	mlx.EnableCompile()

	return nil
}

// newDraftCaches 无 draft 时返回 nil。
// newDraftCaches returns nil when the model ships no draft.
// newDraftCaches 为 draft 模型创建 KV 缓存。
func newDraftCaches(draft base.DraftModel) []cache.Cache {
	if draft == nil {
		return nil
	}
	return draft.NewCaches()
}

// configureWiredMemory 设置 MLX wired limit 为 min(active, recommended)。
func configureWiredMemory() {
	if !mlx.GPUIsAvailable() {
		return
	}

	active := mlx.ActiveMemory()
	maxRecommended, err := mlx.MaxRecommendedWorkingSetSize()
	if err != nil {
		slog.Warn("Unable to query MLX recommended working set; using pageable memory", "error", err)
		return
	}

	limit := min(active, maxRecommended)
	previous, err := mlx.SetWiredLimit(limit)
	if err != nil {
		slog.Warn("Unable to configure MLX wired memory; using pageable memory",
			"active", mlx.PrettyBytes(active),
			"limit", mlx.PrettyBytes(limit),
			"error", err)
		return
	}

	if active > maxRecommended {
		slog.Warn("MLX model exceeds the recommended working set; performance may be degraded",
			"active", mlx.PrettyBytes(active),
			"recommended", mlx.PrettyBytes(maxRecommended))
	}
	// 限制 wired 常驻为活跃分配，避免为 growing KV 预留。
	// Limiting residency to the loaded model's active allocations avoids
	// reserving the remaining capacity for growing KV caches.
	slog.Debug("Configured MLX wired memory",
		"active", mlx.PrettyBytes(active),
		"limit", mlx.PrettyBytes(limit),
		"previous", mlx.PrettyBytes(previous))
}

// loadTensorsFromManifest 分三阶段加载 manifest 张量并 remap 量化 bias。
// loadTensorsFromManifest loads all tensor blobs from the manifest into a
// flat map, deduplicating by digest and remapping safetensors key suffixes.
//
// Uses a two-phase approach: first loads all raw tensors, then remaps
// .bias → _qbias with complete knowledge of which base names have .scale
// entries. This avoids a race condition where Go map iteration order could
// cause .bias to be processed before .scale within the same blob.
// loadTensorsFromManifest 从 manifest 加载 safetensors 并去重 digest。
func loadTensorsFromManifest(root *model.Root) (map[string]*mlx.Array, error) {
	// 阶段 1：从各 blob 原始加载全部张量。
	// Phase 1: Load all tensors raw from all blobs
	rawTensors := make(map[string]*mlx.Array)
	seen := make(map[string]bool)
	for _, layer := range root.Manifest.GetTensorLayers("") {
		if seen[layer.Digest] {
			continue
		}
		seen[layer.Digest] = true
		blobPath := root.Manifest.BlobPath(layer.Digest)
		for name, arr := range mlx.Load(blobPath) {
			rawTensors[name] = arr
		}
	}

	// 阶段 2：识别含 .scale 的基名并重映射为 _scale/_qbias。
	// Phase 2: Identify all base names that have .scale tensors and remap them
	scaleBaseNames := make(map[string]bool)
	allTensors := make(map[string]*mlx.Array, len(rawTensors))
	for name, arr := range rawTensors {
		if strings.HasSuffix(name, ".scale") {
			baseName := strings.TrimSuffix(name, ".scale")
			allTensors[baseName+"_scale"] = arr
			scaleBaseNames[baseName] = true
		}
	}

	// 阶段 3：在完整 scale 知识下处理其余张量。
	// Phase 3: Process remaining tensors with complete scale knowledge
	for name, arr := range rawTensors {
		if strings.HasSuffix(name, ".scale") {
			continue // already handled
		}
		if strings.HasSuffix(name, ".bias") && !strings.HasSuffix(name, ".weight_qbias") {
			baseName := strings.TrimSuffix(name, ".bias")
			if scaleBaseNames[baseName] {
				allTensors[baseName+"_qbias"] = arr
			} else {
				allTensors[name] = arr
			}
		} else {
			allTensors[name] = arr
		}
	}

	slog.Info("Loaded tensors from manifest", "count", len(allTensors))
	return allTensors, nil
}

// Run 启动请求循环与 HTTP 服务。
func (r *Runner) Run(host, port string, mux http.Handler) error {
	g, ctx := errgroup.WithContext(context.Background())

	g.Go(func() error {
		for {
			select {
			case <-ctx.Done():
				return nil
			case request := <-r.Requests:
				err := r.runRequest(request)
				if err != nil {
					slog.Info("Request terminated", "error", err)
					var statusErr api.StatusError
					if !errors.As(err, &statusErr) {
						statusErr = api.StatusError{
							StatusCode:   http.StatusInternalServerError,
							ErrorMessage: err.Error(),
						}
					}
					select {
					case request.Responses <- CompletionResponse{Error: &statusErr}:
					case <-request.Ctx.Done():
					}
				}

				close(request.Responses)
			}
		}
	})

	g.Go(func() error {
		slog.Info("Starting HTTP server", "host", host, "port", port)
		return http.ListenAndServe(net.JoinHostPort(host, port), mux)
	})

	return g.Wait()
}

// runRequest 在 MLX 线程上执行 pipeline。
func (r *Runner) runRequest(request Request) error {
	if r.mlxThread == nil {
		return request.Pipeline(request.Ctx, request)
	}

	return r.mlxThread.Do(request.Ctx, func() error {
		return request.Pipeline(request.Ctx, request)
	})
}
