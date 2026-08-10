package util //nolint:revive

// util 包 regex 工具对 PromQL/LogQL 解析后的 syntax.Regexp AST 做变换：检测大小写折叠、量化符改非贪婪、移除无用捕获组。

import "github.com/grafana/regexp/syntax"

func IsCaseInsensitive(reg *syntax.Regexp) bool {
	return (reg.Flags & syntax.FoldCase) != 0
}

// AllNonGreedy 递归将 *、+ 改为非贪婪，仅安全用于 Match 而非定位最长匹配。
// AllNonGreedy turns greedy quantifiers such as `.*` and `.+` into non-greedy ones. This is the same effect as writing
// `.*?` and `.+?`. This is only safe because we use `Match`. If we were to find the exact position and length of the match
// we would not be allowed to make this optimization. `Match` can return quicker because it is not looking for the longest match.
// Prepending the expression with `(?U)` or passing `NonGreedy` to the expression compiler is not enough since it will
// just negate `.*` and `.*?`.
func AllNonGreedy(regs ...*syntax.Regexp) {
	ClearCapture(regs...)
	for _, re := range regs {
		switch re.Op {
		case syntax.OpCapture, syntax.OpConcat, syntax.OpAlternate:
			AllNonGreedy(re.Sub...)
		case syntax.OpStar, syntax.OpPlus:
			re.Flags = re.Flags | syntax.NonGreedy
		default:
			continue
		}
	}
}

// ClearCapture 内联 OpCapture 子节点，过滤路径不需要保留捕获编号。
// ClearCapture removes capture operation as they are not used for filtering.
func ClearCapture(regs ...*syntax.Regexp) {
	for _, r := range regs {
		if r.Op == syntax.OpCapture {
			*r = *r.Sub[0]
		}
	}
}
// AllNonGreedy 会先调用 ClearCapture，避免在捕获组内部误改量化符标志。
