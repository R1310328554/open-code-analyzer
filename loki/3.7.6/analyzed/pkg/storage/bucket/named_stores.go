package bucket

// named_stores 支持在同一 Loki 进程中配置多套命名对象存储：按名称引用 S3/GCS/Azure/Swift/Filesystem 后端，供多租户或冷热分层使用。

import (
	"fmt"
	"slices"

	"github.com/grafana/loki/v3/pkg/storage/bucket/azure"
	"github.com/grafana/loki/v3/pkg/storage/bucket/filesystem"
	"github.com/grafana/loki/v3/pkg/storage/bucket/gcs"
	"github.com/grafana/loki/v3/pkg/storage/bucket/s3"
	"github.com/grafana/loki/v3/pkg/storage/bucket/swift"

	"github.com/grafana/dskit/flagext"
)

// NamedStores 以 map 形式持有各 provider 的命名配置，storeType 缓存名称到后端类型映射。
// NamedStores helps configure additional object stores from a given storage provider
type NamedStores struct {
	Azure      map[string]NamedAzureStorageConfig      `yaml:"azure"`
	Filesystem map[string]NamedFilesystemStorageConfig `yaml:"filesystem"`
	GCS        map[string]NamedGCSStorageConfig        `yaml:"gcs"`
	S3         map[string]NamedS3StorageConfig         `yaml:"s3"`
	Swift      map[string]NamedSwiftStorageConfig      `yaml:"swift"`

// storeType 在 Validate/populateStoreType 后填充，供 LookupStoreType 与 Exists 查询。
	// contains mapping from named store reference name to store type
	storeType map[string]string `yaml:"-"`
}

func (ns *NamedStores) Validate() error {
	for name, s3Cfg := range ns.S3 {
		if err := s3Cfg.Validate(); err != nil {
			return fmt.Errorf("invalid S3 Storage config with name %s: %w", name, err)
		}
	}

	return ns.populateStoreType()
}

// populateStoreType 遍历五类 map，禁止名称与 SupportedBackends 常量重复或跨类型重名。
func (ns *NamedStores) populateStoreType() error {
	ns.storeType = make(map[string]string)

	checkForDuplicates := func(name string) error {
		if slices.Contains(SupportedBackends, name) {
			return fmt.Errorf("named store %q should not match with the name of a predefined storage type", name)
		}

		if st, ok := ns.storeType[name]; ok {
			return fmt.Errorf("named store %q is already defined under %s", name, st)
		}

		return nil
	}

	for name := range ns.S3 {
		if err := checkForDuplicates(name); err != nil {
			return err
		}
		ns.storeType[name] = S3
	}

	for name := range ns.Azure {
		if err := checkForDuplicates(name); err != nil {
			return err
		}
		ns.storeType[name] = Azure
	}

	for name := range ns.Filesystem {
		if err := checkForDuplicates(name); err != nil {
			return err
		}
		ns.storeType[name] = Filesystem
	}

	for name := range ns.GCS {
		if err := checkForDuplicates(name); err != nil {
			return err
		}
		ns.storeType[name] = GCS
	}

	for name := range ns.Swift {
		if err := checkForDuplicates(name); err != nil {
			return err
		}
		ns.storeType[name] = Swift
	}

	return nil
}

// LookupStoreType 返回命名存储对应的后端类型字符串（如 s3、gcs）。
func (ns *NamedStores) LookupStoreType(name string) (string, bool) {
	st, ok := ns.storeType[name]
	return st, ok
}

func (ns *NamedStores) Exists(name string) bool {
	_, ok := ns.storeType[name]
	return ok
}

// OverrideConfig 将命名存储的配置覆盖到主 Config 的对应字段，供 NewClient 使用。
// OverrideConfig overrides the store config with the named store config
func (ns *NamedStores) OverrideConfig(storeCfg *Config, namedStore string) error {
	storeType, ok := ns.LookupStoreType(namedStore)
	if !ok {
		return fmt.Errorf("unrecognized named storage config %s", namedStore)
	}

	switch storeType {
	case GCS:
		nsCfg, ok := ns.GCS[namedStore]
		if !ok {
			return fmt.Errorf("unrecognized named gcs storage config %s", namedStore)
		}

		storeCfg.GCS = (gcs.Config)(nsCfg)
	case S3:
		nsCfg, ok := ns.S3[namedStore]
		if !ok {
			return fmt.Errorf("unrecognized named s3 storage config %s", namedStore)
		}

		storeCfg.S3 = (s3.Config)(nsCfg)
	case Filesystem:
		nsCfg, ok := ns.Filesystem[namedStore]
		if !ok {
			return fmt.Errorf("unrecognized named filesystem storage config %s", namedStore)
		}

		storeCfg.Filesystem = (filesystem.Config)(nsCfg)
	case Azure:
		nsCfg, ok := ns.Azure[namedStore]
		if !ok {
			return fmt.Errorf("unrecognized named azure storage config %s", namedStore)
		}

		storeCfg.Azure = (azure.Config)(nsCfg)
	case Swift:
		nsCfg, ok := ns.Swift[namedStore]
		if !ok {
			return fmt.Errorf("unrecognized named swift storage config %s", namedStore)
		}

		storeCfg.Swift = (swift.Config)(nsCfg)
	default:
		return fmt.Errorf("unrecognized named storage type: %s", storeType)
	}

	return nil
}

// 命名存储不走 RegisterFlags，故通过 Named*StorageConfig.UnmarshalYAML 先 DefaultValues 再解析。
// Storage configs defined as Named stores don't get any defaults as they do not
// register flags. To get around this we implement Unmarshaler interface that
// assigns the defaults before calling unmarshal.

// We cannot implement Unmarshaler directly on s3.Config or other stores
// as it would end up overriding values set as part of ApplyDynamicConfig().
// Note: we unmarshal a second time after applying dynamic configs
//
// Implementing the Unmarshaler for Named*StorageConfig types is fine as
// we do not apply any dynamic config on them.

// NamedS3StorageConfig 为 s3.Config 类型别名，实现独立 UnmarshalYAML 以注入默认值。
type NamedS3StorageConfig s3.Config

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (cfg *NamedS3StorageConfig) UnmarshalYAML(unmarshal func(interface{}) error) error {
	flagext.DefaultValues((*s3.Config)(cfg))
	return unmarshal((*s3.Config)(cfg))
}

func (cfg *NamedS3StorageConfig) Validate() error {
	return (*s3.Config)(cfg).Validate()
}

// NamedGCSStorageConfig 同理，避免与 ApplyDynamicConfig 冲突的双重 Unmarshal。
type NamedGCSStorageConfig gcs.Config

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (cfg *NamedGCSStorageConfig) UnmarshalYAML(unmarshal func(interface{}) error) error {
	flagext.DefaultValues((*gcs.Config)(cfg))
	return unmarshal((*gcs.Config)(cfg))
}

// NamedAzureStorageConfig 封装 Azure 命名存储的 YAML 解析与默认填充。
type NamedAzureStorageConfig azure.Config

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (cfg *NamedAzureStorageConfig) UnmarshalYAML(unmarshal func(interface{}) error) error {
	flagext.DefaultValues((*azure.Config)(cfg))
	return unmarshal((*azure.Config)(cfg))
}

// NamedSwiftStorageConfig 封装 OpenStack Swift 命名存储配置。
type NamedSwiftStorageConfig swift.Config

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (cfg *NamedSwiftStorageConfig) UnmarshalYAML(unmarshal func(interface{}) error) error {
	flagext.DefaultValues((*swift.Config)(cfg))
	return unmarshal((*swift.Config)(cfg))
}

// NamedFilesystemStorageConfig 为本地目录型命名存储提供默认值注入。
type NamedFilesystemStorageConfig filesystem.Config

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (cfg *NamedFilesystemStorageConfig) UnmarshalYAML(unmarshal func(interface{}) error) error {
	flagext.DefaultValues((*filesystem.Config)(cfg))
	return unmarshal((*filesystem.Config)(cfg))
}
// 命名存储名称不得与 filesystem/s3 等预定义后端字符串相同，否则 Validate 报错。
