package filesystem

// filesystem 包配置本地文件系统对象存储：Directory 指定块与索引数据的根目录，适合开发、测试及单节点小规模部署。

import "flag"

// Config 保存本地文件系统后端的根目录路径，对应 YAML 字段 dir。
// Config stores the configuration for storing and accessing objects in the local filesystem.
type Config struct {
	Directory string `yaml:"dir"`
}

// RegisterFlags 注册无前缀的 filesystem.dir 命令行标志。
// RegisterFlags registers the flags for filesystem storage
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("", f)
}

// RegisterFlagsWithPrefixAndDefaultDirectory 允许调用方指定 flag 前缀与默认目录。
// RegisterFlagsWithPrefixAndDefaultDirectory registers the flags for filesystem
// storage with the provided prefix and sets the default directory to dir.
func (cfg *Config) RegisterFlagsWithPrefixAndDefaultDirectory(prefix, dir string, f *flag.FlagSet) {
	f.StringVar(&cfg.Directory, prefix+"filesystem.dir", dir, "Local filesystem storage directory.")
}

// RegisterFlagsWithPrefix 仅注入前缀，默认目录为空字符串。
// RegisterFlagsWithPrefix registers the flags for filesystem storage with the provided prefix
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefixAndDefaultDirectory(prefix, "", f)
}
// 本地 filesystem 后端无网络传输层，NamedStores 中需通过 UnmarshalYAML 补全默认值。
