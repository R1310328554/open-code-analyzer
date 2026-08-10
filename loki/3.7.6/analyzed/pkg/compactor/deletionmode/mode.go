package deletionmode

// deletionmode 定义租户删除模式枚举：
// disabled 禁用、filter-only 仅查询过滤、filter-and-delete 过滤并物理删除。

import (
	"errors"
	"fmt"
	"strings"
)

type Mode int16

var (
	ErrUnknownMode = errors.New("unknown deletion mode")
)

const (
	disabled        = "disabled"
	filterOnly      = "filter-only"
	filterAndDelete = "filter-and-delete"
	unknown         = "unknown"

	Disabled Mode = iota
	FilterOnly
	FilterAndDelete
)

// String 返回模式的配置字符串表示。
func (m Mode) String() string {
	switch m {
	case Disabled:
		return disabled
	case FilterOnly:
		return filterOnly
	case FilterAndDelete:
		return filterAndDelete
	}
	return unknown
}

func (m Mode) DeleteEnabled() bool {
	return m == FilterOnly || m == FilterAndDelete
}

// AllModes 返回所有合法 deletion 模式字符串列表。
func AllModes() []string {
	return []string{Disabled.String(), FilterOnly.String(), FilterAndDelete.String()}
}

func ParseMode(in string) (Mode, error) {
	switch in {
	case disabled:
		return Disabled, nil
	case filterOnly:
		return FilterOnly, nil
	case filterAndDelete:
		return FilterAndDelete, nil
	}
	return 0, fmt.Errorf("%w: must be one of %s", ErrUnknownMode, strings.Join(AllModes(), "|"))
}

// Enabled 解析模式字符串并返回是否启用删除功能。
func Enabled(in string) (bool, error) {
	deleteMode, err := ParseMode(in)
	if err != nil {
		return false, err
	}

	return deleteMode.DeleteEnabled(), nil
}
