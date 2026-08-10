//go:build windows || darwin

// not 包提供桌面应用通用的“未找到/不可用”哨兵错误，供跨层传递语义一致的错误类型。
package not

import (
	"errors"
)

// Found 表示目标值未找到；底层包可向上层传递该语义，避免各包重复 errors.New("not found")。
//
// 不应直接使用 Found，应通过 errors.Join 或 fmt.Errorf 等包装或合并，例如：
// fmt.Errorf("%w: %s", not.Found, key)
//
// Found is an error that indicates that a value was not found. It
// may be used by low-level packages to signal to higher-level
// packages that a value was not found.
//
// It exists to avoid using errors.New("not found") in multiple
// packages to mean the same thing.
//
// Found should not be used directly. Instead it should be wrapped
// or joined using errors.Join or fmt.Errorf, etc.
//
// Errors wrapping Found should provide additional context, e.g.
// fmt.Errorf("%w: %s", not.Found, key)
//
//lint:ignore ST1012 This is a sentinel error intended to be read like not.Found.
var Found = errors.New("not found")

// Available 表示目标值当前不可用。
//
// Available is an error that indicates that a value is not available.
//
//lint:ignore ST1012 This is a sentinel error intended to be read like not.Available.
var Available = errors.New("not available")
