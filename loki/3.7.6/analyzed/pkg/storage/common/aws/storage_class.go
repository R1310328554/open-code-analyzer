package aws

// aws 包定义 S3 PutObject 支持的 Storage Class 常量及校验逻辑，用于配置对象存储层级与成本策略。

import (
	"fmt"
	"strings"

	"github.com/grafana/loki/v3/pkg/util"
)

const (

// 以下常量对应 AWS S3 Storage Class，影响访问延迟、持久性与计费模式。
	// S3 Storage Class options which define the data access, resiliency & cost requirements of objects
	// https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html#API_PutObject_RequestSyntax
	StorageClassGlacier                  = "GLACIER"
	StorageClassDeepArchive              = "DEEP_ARCHIVE"
	StorageClassGlacierInstantRetrieval  = "GLACIER_IR"
	StorageClassIntelligentTiering       = "INTELLIGENT_TIERING"
	StorageClassOneZoneInfrequentAccess  = "ONEZONE_IA"
	StorageClassOutposts                 = "OUTPOSTS"
	StorageClassReducedRedundancy        = "REDUCED_REDUNDANCY"
	StorageClassStandard                 = "STANDARD"
	StorageClassStandardInfrequentAccess = "STANDARD_IA"
)

// SupportedStorageClasses 列出 ValidateStorageClass 允许的全部存储类字符串。
var (
	SupportedStorageClasses = []string{StorageClassGlacier, StorageClassDeepArchive, StorageClassGlacierInstantRetrieval, StorageClassIntelligentTiering, StorageClassOneZoneInfrequentAccess, StorageClassOutposts, StorageClassReducedRedundancy, StorageClassStandard, StorageClassStandardInfrequentAccess}
)

// ValidateStorageClass 检查 storageClass 是否在 SupportedStorageClasses 中，否则返回详细错误。
func ValidateStorageClass(storageClass string) error {
	if !util.StringsContain(SupportedStorageClasses, storageClass) {
		return fmt.Errorf("unsupported S3 storage class: %s. Supported values: %s", storageClass, strings.Join(SupportedStorageClasses, ", "))
	}

	return nil
}
// STANDARD 为默认热存储；GLACIER/DEEP_ARCHIVE 适用于归档，检索需额外解冻时间。
