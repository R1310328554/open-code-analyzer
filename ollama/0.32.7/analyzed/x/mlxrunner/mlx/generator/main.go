// MLX CGO 绑定生成器：tree-sitter 解析 C 头并渲染 Go 模板。
package main

import (
	"embed"
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"slices"
	"strings"
	"text/template"

	tree_sitter "github.com/tree-sitter/go-tree-sitter"
	tree_sitter_cpp "github.com/tree-sitter/tree-sitter-cpp/bindings/go"
)

//go:embed *.gotmpl
// 嵌入 Go 模板文件供代码生成使用。
var fsys embed.FS

// optionalSymbols 列出可选符号（如 CUDA 构建无 float16/bfloat16）。
// optionalSymbols lists symbols that may not be present in all builds
// (e.g., float16/bfloat16 are unavailable in CUDA builds of MLX).
var optionalSymbols = map[string]bool{
	"mlx_array_item_float16":  true,
	"mlx_array_item_bfloat16": true,
	"mlx_array_data_float16":  true,
	"mlx_array_data_bfloat16": true,
}

// Function 保存从 C 头解析出的函数签名信息。
type Function struct {
	Type,
	Name,
	Parameters,
	Args string
	Optional bool
}

// ParseFunction 从 AST 节点提取返回类型、名、参数与调用参数列表。
func ParseFunction(node *tree_sitter.Node, tc *tree_sitter.TreeCursor, source []byte) Function {
	var fn Function
	fn.Name = node.ChildByFieldName("declarator").Utf8Text(source)
	if params := node.ChildByFieldName("parameters"); params != nil {
		fn.Parameters = params.Utf8Text(source)
		fn.Args = ParseParameters(params, tc, source)
	}

	var types []string
	for node.Parent() != nil && node.Parent().Kind() != "declaration" {
		if node.Parent().Kind() == "pointer_declarator" {
			types = append(types, "*")
		}
		node = node.Parent()
	}

	for sibling := node.PrevSibling(); sibling != nil; sibling = sibling.PrevSibling() {
		types = append(types, sibling.Utf8Text(source))
	}

	slices.Reverse(types)
	fn.Type = strings.Join(types, " ")
	return fn
}

// ParseParameters 解析形参列表为逗号分隔的标识符串。
func ParseParameters(node *tree_sitter.Node, tc *tree_sitter.TreeCursor, source []byte) string {
	var s []string
	for _, child := range node.Children(tc) {
		if child.IsNamed() {
			child := child.ChildByFieldName("declarator")
			for child != nil && child.Kind() != "identifier" {
				if child.Kind() == "parenthesized_declarator" {
					child = child.Child(1)
				} else {
					child = child.ChildByFieldName("declarator")
				}
			}

			if child != nil {
				s = append(s, child.Utf8Text(source))
			}
		}
	}
	return strings.Join(s, ", ")
}

// main 解析 C 头文件并执行模板生成 generated.h 等。
func main() {
	var output string
	flag.StringVar(&output, "output", ".", "Output directory for generated files")
	flag.Parse()

	parser := tree_sitter.NewParser()
	defer parser.Close()

	language := tree_sitter.NewLanguage(tree_sitter_cpp.Language())
	parser.SetLanguage(language)

	query, _ := tree_sitter.NewQuery(language, `(function_declarator declarator: (identifier)) @func`)
	defer query.Close()

	qc := tree_sitter.NewQueryCursor()
	defer qc.Close()

	var files []string
	for _, arg := range flag.Args() {
		matches, err := filepath.Glob(arg)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error expanding glob %s: %v\n", arg, err)
			continue
		}
		files = append(files, matches...)
	}

	var funs []Function
	for _, arg := range files {
		bts, err := os.ReadFile(arg)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error reading file %s: %v\n", arg, err)
			continue
		}

		tree := parser.Parse(bts, nil)
		defer tree.Close()

		tc := tree.Walk()
		defer tc.Close()

		matches := qc.Matches(query, tree.RootNode(), bts)
		for match := matches.Next(); match != nil; match = matches.Next() {
			for _, capture := range match.Captures {
				fn := ParseFunction(&capture.Node, tc, bts)
				fn.Optional = optionalSymbols[fn.Name]
				funs = append(funs, fn)
			}
		}
	}

	tmpl, err := template.New("").ParseFS(fsys, "*.gotmpl")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error parsing template: %v\n", err)
		return
	}

	for _, tmpl := range tmpl.Templates() {
		name := filepath.Join(output, strings.TrimSuffix(tmpl.Name(), ".gotmpl"))

		fmt.Println("Generating", name)
		f, err := os.Create(name)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error creating file %s: %v\n", name, err)
			continue
		}
		defer f.Close()

		if err := tmpl.Execute(f, map[string]any{
			"Functions": funs,
		}); err != nil {
			fmt.Fprintf(os.Stderr, "Error executing template %s: %v\n", tmpl.Name(), err)
		}
	}
}
