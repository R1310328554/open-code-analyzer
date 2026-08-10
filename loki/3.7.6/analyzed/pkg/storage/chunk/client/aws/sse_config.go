package aws

// sse_config 将 bucket_s3.SSEConfig 解析为 S3 PutObject 所需的 SSE 字段，支持 AES256（SSE-S3）与 aws:kms（SSE-KMS）及 KMS 加密上下文。

import (
	"encoding/base64"
	"encoding/json"

	"github.com/pkg/errors"

	bucket_s3 "github.com/grafana/loki/v3/pkg/storage/bucket/s3"
)

const (
	sseKMSType = "aws:kms"
	sseS3Type  = "AES256"
)

// SSEParsedConfig configures server side encryption (SSE)
// struct used internally to configure AWS S3
// SSEParsedConfig 为内部结构，ServerSideEncryption 与可选 KMS 键/上下文指针供 SDK 使用。
type SSEParsedConfig struct {
	ServerSideEncryption string
	KMSKeyID             *string
	KMSEncryptionContext *string
}

// NewSSEParsedConfig creates a struct to configure server side encryption (SSE)
// NewSSEParsedConfig 校验 SSE 类型；KMS 模式要求 KMSKeyID 并解析 JSON 加密上下文。
func NewSSEParsedConfig(cfg bucket_s3.SSEConfig) (*SSEParsedConfig, error) {
	switch cfg.Type {
	case bucket_s3.SSES3:
		return &SSEParsedConfig{
			ServerSideEncryption: sseS3Type,
		}, nil
	case bucket_s3.SSEKMS:
		if cfg.KMSKeyID == "" {
			return nil, errors.New("KMS key id must be passed when SSE-KMS encryption is selected")
		}

		parsedKMSEncryptionContext, err := parseKMSEncryptionContext(cfg.KMSEncryptionContext)
		if err != nil {
			return nil, errors.Wrap(err, "failed to parse KMS encryption context")
		}

		return &SSEParsedConfig{
			ServerSideEncryption: sseKMSType,
			KMSKeyID:             &cfg.KMSKeyID,
			KMSEncryptionContext: parsedKMSEncryptionContext,
		}, nil
	default:
		return nil, errors.New("SSE type is empty or invalid")
	}
}

// parseKMSEncryptionContext 校验 JSON 后 base64 编码，供 x-amz-server-side-encryption-context 使用。
func parseKMSEncryptionContext(kmsEncryptionContext string) (*string, error) {
	if kmsEncryptionContext == "" {
		return nil, nil
	}

	// validates if kmsEncryptionContext is a valid JSON
	jsonKMSEncryptionContext, err := json.Marshal(json.RawMessage(kmsEncryptionContext))
	if err != nil {
		return nil, errors.Wrap(err, "failed to marshal KMS encryption context")
	}

	parsedKMSEncryptionContext := base64.StdEncoding.EncodeToString(jsonKMSEncryptionContext)

	return &parsedKMSEncryptionContext, nil
}
// 空 SSE 类型返回错误；SSE-S3 仅设置 AES256 算法无需额外密钥材料。
