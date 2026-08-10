package cfg

// dynamic 扩展 cfg 以支持 ApplyDynamicConfig：在解析 common 段与命令行后再注入动态默认值，并二次加载配置文件覆盖 map 类字段。

import (
	"flag"
)

// DynamicCloneable 在 Cloneable 基础上提供 ApplyDynamicConfig 动态填充 Source。
// DynamicCloneable must be implemented by config structs that can be dynamically unmarshalled
type DynamicCloneable interface {
	Cloneable
	ApplyDynamicConfig() Source
}

// DynamicUnmarshal 按 defaults→文件→flags→动态逻辑→非 strict 二次文件→flags 顺序合并。
// DynamicUnmarshal handles populating a config based on the following precedence:
// 1. Defaults provided by the `RegisterFlags` interface
// 2. Sections populated by dynamic logic. Configs passed to this function must implement ApplyDynamicConfig()
// 3. Any config options specified directly in the config file
// 4. Any config options specified on the command line.
func DynamicUnmarshal(dst DynamicCloneable, args []string, fs *flag.FlagSet) error {
	return Unmarshal(dst,
		// First populate the config with defaults including flags from the command line
		Defaults(fs),
		// Next populate the config from the config file, we do this to populate the `common`
		// section of the config file by taking advantage of the code in ConfigFileLoader which will load
		// and process the config file.
		ConfigFileLoader(args, "config.file", true),
		// Now load the flags again, this will supersede anything set from config file with flags from the command line.
		Flags(args, fs),
		// Apply any dynamic logic to set other defaults in the config. This function is called after parsing the
		// config files so that values from a common, or shared, section can be used in
		// the dynamic evaluation
		dst.ApplyDynamicConfig(),
		// Load configs from the config file a second time, this will supersede anything set by the common
		// config with values specified in the config file.
		// By loading the config file twice and unmarshaling it into the same object,
		// using strict yaml unmarshal causes an `already set in map` error with the `Clients` config,
		// because it's a map that already has the keys we are trying to unmarshal into it.
		// That is why we don't use strict for the second marshaling.
		ConfigFileLoader(args, "config.file", false),
		// Load the flags again, this will supersede anything set from config file with flags from the command line.
		Flags(args, fs),
	)
}
// Clients 等 map 字段二次 strict 反序列化会触发 already set 错误，故第二次用非 strict。
