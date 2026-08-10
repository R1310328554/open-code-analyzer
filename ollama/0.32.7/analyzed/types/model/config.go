// 模型配置元数据 ConfigV2 与 draft 辅助模型描述。
package model

// ConfigV2 表示 manifest 中的模型配置元数据。
// ConfigV2 represents the configuration metadata for a model.
type ConfigV2 struct {
	ModelFormat   string   `json:"model_format"`
	ModelFamily   string   `json:"model_family"`
	ModelFamilies []string `json:"model_families"`
	ModelType     string   `json:"model_type"` // 展示为参数量
	// shown as Parameter Size
	FileType      string   `json:"file_type"`  // 展示为量化级别
	// shown as Quantization Level
	Renderer      string   `json:"renderer,omitempty"`
	Parser        string   `json:"parser,omitempty"`
	Requires      string   `json:"requires,omitempty"`

	RemoteHost  string `json:"remote_host,omitempty"`
	RemoteModel string `json:"remote_model,omitempty"`

	// 远程模型相关字段。
	// used for remotes
	Capabilities []string `json:"capabilities,omitempty"`
	ContextLen   int      `json:"context_length,omitempty"`
	EmbedLen     int      `json:"embedding_length,omitempty"`
	BaseName     string   `json:"base_name,omitempty"`
	Draft        *Draft   `json:"draft,omitempty"`

	// OCI 规范必填字段。
	// required by spec
	Architecture string `json:"architecture"`
	OS           string `json:"os"`
}

// Draft 描述同 manifest 中的 speculative draft 辅助模型。
// Draft describes an auxiliary draft model stored in the same manifest.
type Draft struct {
	ModelFormat  string `json:"model_format,omitempty"`
	Architecture string `json:"architecture,omitempty"`
	TensorPrefix string `json:"tensor_prefix,omitempty"`
	Config       string `json:"config,omitempty"`
}
