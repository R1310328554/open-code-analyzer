//go:build gofuzz
// +build gofuzz

package syntax

// fuzz 为 go-fuzz 提供 ParseExpr 模糊测试入口：解析成功返回 1，语法错误返回 0。

func FuzzParseExpr(data []byte) int {
	_, err := ParseExpr(string(data))
	if err != nil {
		return 0
	}
	return 1
}
// 需在 gofuzz build tag 下编译；错误路径不计入 interesting corpus。
