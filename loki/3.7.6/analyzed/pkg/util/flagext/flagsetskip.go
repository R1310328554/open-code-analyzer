package flagext

// FlagSetWithSkip 包装标准 flag.FlagSet，注册时可跳过指定 flag 名，避免重复定义冲突。

import (
	"flag"
	"time"
)

type FlagSetWithSkip struct {
	*flag.FlagSet
	skip map[string]struct{}
}

func NewFlagSetWithSkip(f *flag.FlagSet, skip []string) *FlagSetWithSkip {
	skipMap := make(map[string]struct{}, len(skip))
	for _, s := range skip {
		skipMap[s] = struct{}{}
	}
	return &FlagSetWithSkip{f, skipMap}
}

func (f *FlagSetWithSkip) ToFlagSet() *flag.FlagSet {
	return f.FlagSet
}

// DurationVar 等包装方法仅在 name 不在 skip 集合时才调用底层 FlagSet 注册。
func (f *FlagSetWithSkip) DurationVar(p *time.Duration, name string, value time.Duration, usage string) {
	if _, ok := f.skip[name]; !ok {
		f.FlagSet.DurationVar(p, name, value, usage)
	}
}

func (f *FlagSetWithSkip) StringVar(p *string, name string, value string, usage string) {
	if _, ok := f.skip[name]; !ok {
		f.FlagSet.StringVar(p, name, value, usage)
	}
}

func (f *FlagSetWithSkip) BoolVar(p *bool, name string, value bool, usage string) {
	if _, ok := f.skip[name]; !ok {
		f.FlagSet.BoolVar(p, name, value, usage)
	}
}

func (f *FlagSetWithSkip) IntVar(p *int, name string, value int, usage string) {
	if _, ok := f.skip[name]; !ok {
		f.FlagSet.IntVar(p, name, value, usage)
	}
}

func (f *FlagSetWithSkip) Var(value flag.Value, name string, usage string) {
	if _, ok := f.skip[name]; !ok {
		f.FlagSet.Var(value, name, usage)
	}
}

// 可按需继续包装 Int64Var、Float64Var 等 flag 注册方法以扩展跳过能力。
// TODO: Add more methods as needed.
