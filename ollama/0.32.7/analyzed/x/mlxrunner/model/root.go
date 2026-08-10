// 模型 Root：manifest 包装、量化元数据扫描与 draft 配置。
package model

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"sort"
	"strconv"
	"strings"

	modeltypes "github.com/ollama/ollama/types/model"
	"github.com/ollama/ollama/x/imagegen/manifest"
)

// TensorQuantInfo 描述单张量量化元数据。
// TensorQuantInfo describes per-tensor quantization metadata.
// TensorQuantInfo 存储 QuantType 与 GroupSize。
type TensorQuantInfo struct {
	QuantType string
	GroupSize int
}

// Root 包装 ModelManifest 并预扫描各 blob 量化信息。
// Root wraps a ModelManifest with pre-scanned quantization metadata.
type Root struct {
	Manifest *manifest.ModelManifest
	Draft    *modeltypes.Draft

	// 兼容旧接口的模型级量化元数据（首个 tensor blob）。
	// Backwards-compatible model-level quant metadata (first tensor blob).
	quantType string
	groupSize int

	// 逐张量量化元数据表。
	// Per-tensor quantization metadata.
	tensorQuant map[string]*TensorQuantInfo
}

// Open 加载 manifest 并扫描 tensor blob 头中的量化元数据。
// Open loads a manifest for the given model name and scans tensor blobs for
// quantization metadata.
func Open(modelName string) (*Root, error) {
	m, err := manifest.LoadManifest(modelName)
	if err != nil {
		return nil, err
	}

	root := &Root{
		Manifest:    m,
		tensorQuant: make(map[string]*TensorQuantInfo),
	}
	root.Draft = readDraftConfig(m)

	for _, layer := range m.GetTensorLayers("") {
		blobPath := m.BlobPath(layer.Digest)

		infos, blobQuantType, blobGroupSize, err := readBlobTensorQuantInfo(blobPath)
		if err != nil {
			continue
		}

		for name, info := range infos {
			root.tensorQuant[name] = info
		}

		if root.quantType == "" && blobQuantType != "" {
			root.quantType = strings.ToUpper(blobQuantType)
			root.groupSize = blobGroupSize
			if root.groupSize == 0 {
				root.groupSize = defaultGroupSize(root.quantType)
			}
		}
	}

	return root, nil
}

// readDraftConfig 从 config 或 draft/config.json 推断 draft 配置。
func readDraftConfig(m *manifest.ModelManifest) *modeltypes.Draft {
	if m == nil || m.Manifest == nil || m.Manifest.Config.Digest == "" {
		return nil
	}

	data, err := os.ReadFile(m.BlobPath(m.Manifest.Config.Digest))
	if err != nil {
		return nil
	}

	var cfg modeltypes.ConfigV2
	if err := json.Unmarshal(data, &cfg); err != nil {
		return nil
	}
	if cfg.Draft != nil {
		return cfg.Draft
	}

	if m.GetConfigLayer("draft/config.json") != nil {
		return &modeltypes.Draft{
			ModelFormat:  "safetensors",
			TensorPrefix: "draft.",
			Config:       "draft/config.json",
		}
	}
	return nil
}

// Close 当前无操作（预留释放资源）。
// Close is a no-op for now (future: release resources).
func (r *Root) Close() {}

// QuantType 返回首个 tensor blob 检测到的量化类型。
// QuantType returns the quantization type detected from the first tensor blob metadata.
func (r *Root) QuantType() string { return r.quantType }

// GroupSize 返回首个 blob 检测到的 group size。
// GroupSize returns the quantization group size detected from the first tensor blob metadata.
func (r *Root) GroupSize() int { return r.groupSize }

// TensorQuant 按名返回 per-tensor 量化信息。
// TensorQuant returns per-tensor quantization metadata if available.
func (r *Root) TensorQuant(name string) *TensorQuantInfo {
	if r == nil {
		return nil
	}
	return r.tensorQuant[name]
}

// AllTensorQuant 返回 per-tensor 映射的副本。
// AllTensorQuant returns a copy of the per-tensor quantization metadata.
func (r *Root) AllTensorQuant() map[string]*TensorQuantInfo {
	out := make(map[string]*TensorQuantInfo, len(r.tensorQuant))
	for k, v := range r.tensorQuant {
		if v == nil {
			continue
		}
		copy := *v
		out[k] = &copy
	}
	return out
}

func defaultGroupSize(quantType string) int {
	groupSize, _, _ := QuantizationParams(quantType)
	return groupSize
}

// readBlobTensorQuantInfo 解析 safetensors 头中的全局与 per-tensor 量化信息。
func readBlobTensorQuantInfo(path string) (map[string]*TensorQuantInfo, string, int, error) {
	f, err := os.Open(path)
	if err != nil {
		return nil, "", 0, err
	}
	defer f.Close()

	var headerSize uint64
	if err := binary.Read(f, binary.LittleEndian, &headerSize); err != nil {
		return nil, "", 0, err
	}
	if headerSize > 100*1024*1024 {
		return nil, "", 0, fmt.Errorf("header too large: %d", headerSize)
	}

	data := make([]byte, headerSize)
	if _, err := io.ReadFull(f, data); err != nil {
		return nil, "", 0, err
	}

	var header map[string]json.RawMessage
	if err := json.Unmarshal(data, &header); err != nil {
		return nil, "", 0, err
	}

	globalQuantType, globalGroupSize := parseGlobalQuantMetadata(header)
	globalQuantType = strings.ToUpper(globalQuantType)

	// 解析 __metadata__ 获取 per-tensor 量化字段。
	// Parse full metadata for per-tensor quant info
	var metaMap map[string]string
	if metaRaw, ok := header["__metadata__"]; ok {
		json.Unmarshal(metaRaw, &metaMap)
	}

	mainNames := mainTensorNames(header)
	infos := make(map[string]*TensorQuantInfo)
	for _, name := range mainNames {
		if _, ok := header[name+".scale"]; !ok {
			continue
		}

		quantType := globalQuantType
		groupSize := globalGroupSize

		// 读取 per-tensor 元数据（如混合精度 expert blob）。
		// Check per-tensor metadata (e.g. from packed expert blobs with mixed precision)
		if metaMap != nil {
			if qt, ok := metaMap[name+".quant_type"]; ok && qt != "" {
				quantType = strings.ToUpper(qt)
			}
			if gs, ok := metaMap[name+".group_size"]; ok && gs != "" {
				if v, err := strconv.Atoi(gs); err == nil {
					groupSize = v
				}
			}
		}

		inferredType, inferredGroup := inferQuantTypeFromShapes(header, name, quantType)
		if quantType == "" {
			quantType = inferredType
		}
		if groupSize == 0 {
			groupSize = inferredGroup
		}
		if quantType == "" {
			continue
		}
		if groupSize == 0 {
			groupSize = defaultGroupSize(quantType)
		}

		infos[name] = &TensorQuantInfo{QuantType: quantType, GroupSize: groupSize}
	}

	return infos, globalQuantType, globalGroupSize, nil
}

func parseGlobalQuantMetadata(header map[string]json.RawMessage) (quantType string, groupSize int) {
	metaRaw, ok := header["__metadata__"]
	if !ok {
		return "", 0
	}

	var meta map[string]string
	if err := json.Unmarshal(metaRaw, &meta); err != nil {
		return "", 0
	}

	quantType = meta["quant_type"]
	if gs := meta["group_size"]; gs != "" {
		groupSize, _ = strconv.Atoi(gs)
	}
	return quantType, groupSize
}

// mainTensorNames 收集 header 中主张量名（排除 scale/bias/metadata）。
func mainTensorNames(header map[string]json.RawMessage) []string {
	names := make([]string, 0, len(header))
	for name := range header {
		if name == "__metadata__" || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".bias") {
			continue
		}
		names = append(names, name)
	}
	sort.Strings(names)
	return names
}

// inferQuantTypeFromShapes 由 weight/scale shape 比例推断 INT4/INT8 与 groupSize。
func inferQuantTypeFromShapes(header map[string]json.RawMessage, tensorName string, hintQuantType string) (string, int) {
	type tensorShape struct {
		Shape []int64 `json:"shape"`
	}

	mainRaw, ok := header[tensorName]
	if !ok {
		return "", 0
	}
	scaleRaw, ok := header[tensorName+".scale"]
	if !ok {
		return "", 0
	}

	var mainInfo tensorShape
	if err := json.Unmarshal(mainRaw, &mainInfo); err != nil || len(mainInfo.Shape) == 0 {
		return "", 0
	}

	var scaleInfo tensorShape
	if err := json.Unmarshal(scaleRaw, &scaleInfo); err != nil || len(scaleInfo.Shape) == 0 {
		return "", 0
	}

	weightCols := int(mainInfo.Shape[len(mainInfo.Shape)-1])
	scalesCols := int(scaleInfo.Shape[len(scaleInfo.Shape)-1])
	if weightCols <= 0 || scalesCols <= 0 {
		return "", 0
	}

	groupSize4 := weightCols * 8 / scalesCols
	groupSize8 := weightCols * 4 / scalesCols

	switch {
	case groupSize4 == 32:
		return "INT4", 32
	case groupSize8 == 64:
		return "INT8", 64
	case groupSize4 == 64 && groupSize8 == 32:
		h := strings.ToUpper(hintQuantType)
		if strings.Contains(h, "8") {
			return "INT8", 32
		}
		if strings.Contains(h, "4") {
			return "INT4", 64
		}
	}

	if isCommonGroupSize(groupSize4) && !isCommonGroupSize(groupSize8) {
		return "INT4", groupSize4
	}
	if isCommonGroupSize(groupSize8) && !isCommonGroupSize(groupSize4) {
		return "INT8", groupSize8
	}

	return "", 0
}
