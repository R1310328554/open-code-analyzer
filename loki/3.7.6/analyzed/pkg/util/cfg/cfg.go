package cfg

// cfg 定义可组合的配置 Source 链：按顺序合并 defaults、配置文件与命令行，目标对象须实现 Cloneable 以支持 json/yaml 反序列化。

import (
	"flag"
	"reflect"

	"github.com/grafana/dskit/flagext"
	"github.com/pkg/errors"
)

// Source 函数从某一来源读取配置并写入 dst，可叠加先前 source 已填充的字段。
// Source is a generic configuration source. This function may do whatever is
// required to obtain the configuration. It is passed a pointer to the
// destination, which will be something compatible to `json.Unmarshal`. The
// obtained configuration may be written to this object, it may also contain
// data from previous sources.
type Source func(Cloneable) error

// Cloneable 要求 Clone 不修改原对象，供 flag 注册与解析时使用副本。
// Cloneable is a config which can be cloned into a flagext.Registerer
// Contract: the cloned value must not mutate the original.
type Cloneable interface {
	Clone() flagext.Registerer
}

var (
	ErrNotPointer = errors.New("dst is not a pointer")
)

// Unmarshal 依次执行各 Source，dst 必须是指针否则返回 ErrNotPointer。
// Unmarshal merges the values of the various configuration sources and sets them on
// `dst`. The object must be compatible with `json.Unmarshal`.
func Unmarshal(dst Cloneable, sources ...Source) error {
	if len(sources) == 0 {
		panic("No sources supplied to cfg.Unmarshal(). This is most likely a programming issue and should never happen. Check the code!")
	}
	if reflect.ValueOf(dst).Kind() != reflect.Ptr {
		return ErrNotPointer
	}

	for _, source := range sources {
		if err := source(dst); err != nil {
			return err
		}
	}
	return nil
}

// DefaultUnmarshal 组合 Defaults、ConfigFileLoader 与 Flags 完成标准启动配置加载。
// DefaultUnmarshal is a higher level wrapper for Unmarshal that automatically parses flags and a .yaml file
func DefaultUnmarshal(dst Cloneable, args []string, fs *flag.FlagSet) error {
	return Unmarshal(dst,
		Defaults(fs),
		ConfigFileLoader(args, "config.file", true),
		Flags(args, fs),
	)
}
// sources 为空时 Unmarshal 直接 panic，属于编程错误而非运行时配置问题。
