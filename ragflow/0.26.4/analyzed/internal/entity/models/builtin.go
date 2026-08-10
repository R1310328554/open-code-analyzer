// builtin.go — 内置本地嵌入驱动：通过 TEI（Text Embeddings Inference）服务提供 Embed，不支持 Chat/Rerank。

package models

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

// builtinHTTPClient 带 30s 超时的共享 HTTP 客户端，避免 TEI 挂起阻塞协程
var builtinHTTPClient = &http.Client{
	Timeout: 30 * time.Second,
	Transport: &http.Transport{
		MaxIdleConnsPerHost:   10,
		ResponseHeaderTimeout: 10 * time.Second,
	},
}

// BuiltinModel 本地 TEI 嵌入驱动，实现 ModelDriver 的 Embed/ListModels/CheckConnection
type BuiltinModel struct {
	baseURL string
	model   string
}

// NewBuiltinModel 创建 Builtin 驱动（baseURL + 默认模型名）
func NewBuiltinModel(baseURL, model string) *BuiltinModel {
	return &BuiltinModel{
		baseURL: baseURL,
		model:   model,
	}
}

// Name 返回提供商标识 "builtin"，供工厂层路由
func (b *BuiltinModel) Name() string {
	return "builtin"
}

// NewInstance 按租户/区域 BaseURL 创建新的 Builtin 驱动实例
func (b *BuiltinModel) NewInstance(baseURL map[string]string) ModelDriver {
	return &BuiltinModel{
		baseURL: b.baseURL,
		model:   b.model,
	}
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (b *BuiltinModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	return nil, fmt.Errorf("builtin model does not support chat")
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (b *BuiltinModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("builtin model does not support chat")
}

// Embed 向 TEI POST /embed 获取向量，可选 dimensions 截断
// Embed 将文本列表编码为向量嵌入
func (b *BuiltinModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	baseURL := b.baseURL
	if baseURL == "" {
		baseURL = "http://localhost:6380"
	}

	url := fmt.Sprintf("%s/embed", baseURL)

	reqBody := map[string]interface{}{
		"inputs": texts,
	}

	// Add dimension if specified
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody["dimensions"] = embeddingConfig.Dimension
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, err
	}

	req.Header.Set("Content-Type", "application/json")
	// Note: TEI server typically doesn't require auth for local deployments

	resp, err := builtinHTTPClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Builtin embeddings API error: status %d, body: %s", resp.StatusCode, string(body))
	}

	// TEI returns a simple array of embeddings by default
	var embeddings [][]float64
	if err = json.Unmarshal(body, &embeddings); err != nil {
		return nil, fmt.Errorf("failed to parse TEI response: %w, body: %s", err, string(body))
	}

	result := make([]EmbeddingData, len(embeddings))
	for i, emb := range embeddings {
		result[i] = EmbeddingData{
			Embedding: emb,
			Index:     i,
		}
	}

	return result, nil
}

// Rerank 对候选文档按 query 相关性重排序
func (b *BuiltinModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("builtin model does not support rerank")
}

// TranscribeAudio 语音转文字（ASR）
func (b *BuiltinModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("builtin model does not support transcription")
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (b *BuiltinModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("builtin model does not support transcription")
}

// AudioSpeech 文字转语音（TTS）
func (b *BuiltinModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("builtin model does not support TTS")
}

// AudioSpeechWithSender 流式 TTS 输出
func (b *BuiltinModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("builtin model does not support TTS")
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (b *BuiltinModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("builtin model does not support OCR")
}

// ParseFile 解析文档为结构化文本
func (b *BuiltinModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("builtin model does not support parse file")
}

// ListModels 列出当前 API Key 可见的模型目录
func (b *BuiltinModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return []ListModelResponse{
		{
			Name: b.model,
		},
	}, nil
}

// Balance 查询账户余额（若上游支持）
func (b *BuiltinModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("builtin model does not support balance")
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (b *BuiltinModel) CheckConnection(apiConfig *APIConfig) error {
	// Try to get model info to verify connection
	_, err := b.Embed(nil, []string{"test"}, apiConfig, nil)
	return err
}

// ListTasks 列出异步任务状态
func (b *BuiltinModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("builtin model does not support tasks")
}

// ShowTask 按 taskID 查询单个异步任务详情
func (b *BuiltinModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("builtin model does not support tasks")
}

// GetBuiltinEmbeddingModel 按模型名构造 Builtin 驱动，TEI 地址取自 TEI_BASE_URL 环境变量
func GetBuiltinEmbeddingModel(modelName string) ModelDriver {
	// Get TEI base URL from environment or config
	// Default to port 6380 where docker-tei-cpu-1 maps internal port 80
	teiBaseURL := getEnv("TEI_BASE_URL", "http://localhost:6380")

	// Create a builtin model instance with TEI endpoint
	driver := NewBuiltinModel(teiBaseURL, modelName)
	return driver
}

// getEnv 读取环境变量并规范化反斜杠，缺省返回 defaultValue
func getEnv(key, defaultValue string) string {
	if value := strings.TrimSpace(strings.Replace(os.Getenv(key), "\\", "/", -1)); value != "" {
		return value
	}
	return defaultValue
}

// Builtin 仅支持 Embed；Chat/Rerank/TTS 等接口返回不支持错误。默认 TEI 端点 http://localhost:6380（docker-tei-cpu 映射）。CheckConnection 以单条 test 嵌入探活。
