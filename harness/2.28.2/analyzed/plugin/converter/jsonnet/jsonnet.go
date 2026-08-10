// jsonnet 子包实现 Jsonnet 配置求值：注入仓库/构建上下文、远程 import 与 YAML 输出。
package jsonnet

import (
	"bytes"
	"context"
	"fmt"
	"path"
	"strconv"
	"strings"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/errors"

	"github.com/google/go-jsonnet"
)

// repo 为注入 Jsonnet 外部变量的仓库字段前缀。
const repo = "repo."
// build 为注入 Jsonnet 外部变量的构建字段前缀。
const build = "build."
// param 为构建自定义参数在 ExtVar 中的前缀。
const param = "param."

// noContext 供 FileService 调用使用的背景上下文。
var noContext = context.Background()

// importer 实现 jsonnet.Importer，从仓库按 commit/ref 拉取被 import 的文件并缓存。
type importer struct {
	repo  *core.Repository
	build *core.Build

	// cache 缓存已拉取的 import 内容，避免 jsonnet 重复请求同一文件。
	cache map[string]jsonnet.Contents

	// limit 单次构建允许的最大 outbound import 次数，防止滥用 Git API 配额。
	limit int

	// count 已执行的 import 请求计数，超过 limit 时返回错误。
	count int

	fileService core.FileService
	user        *core.User
}

// Import 解析相对路径、校验仓库内范围，经 FileService 获取文件内容并写入缓存。
func (i *importer) Import(importedFrom, importedPath string) (contents jsonnet.Contents, foundAt string, err error) {
	if i.cache == nil {
		i.cache = map[string]jsonnet.Contents{}
	}

	// the import is relative to the imported from path. the
	// imported path must resolve to a filepath relative to
	// the root of the repository.
	importedPath = path.Join(
		path.Dir(importedFrom),
		importedPath,
	)

	if strings.HasPrefix(importedFrom, "../") {
		err = fmt.Errorf("jsonnet: cannot resolve import: %s", importedPath)
		return contents, foundAt, err
	}

	// if the contents exist in the cache, return the
	// cached item.
	if contents, ok := i.cache[importedPath]; ok {
		return contents, importedPath, nil
	}

	defer func() {
		i.count++
	}()

	// if the import limit is exceeded log an error message.
	if i.limit > 0 && i.count >= i.limit {
		return contents, foundAt, errors.New("jsonnet: import limit exceeded")
	}

	find, err := i.fileService.Find(noContext, i.user, i.repo.Slug, i.build.After, i.build.Ref, importedPath)

	if err != nil {
		return contents, foundAt, err
	}

	i.cache[importedPath] = jsonnet.MakeContents(string(find.Data))

	return i.cache[importedPath], importedPath, err
}

// Parse 配置 Jsonnet VM、映射仓库/构建/模板输入，求值后合并多文档 YAML 流。
func Parse(req *core.ConvertArgs, fileService core.FileService, limit int, template *core.Template, templateData map[string]interface{}) (string, error) {
	vm := jsonnet.MakeVM()
	vm.MaxStack = 500
	vm.StringOutput = false
	vm.ErrorFormatter.SetMaxStackTraceSize(20)
	if fileService != nil && limit > 0 {
		vm.Importer(
			&importer{
				repo:        req.Repo,
				build:       req.Build,
				limit:       limit,
				user:        req.User,
				fileService: fileService,
			},
		)
	}

	// 将 build/repo 元数据映射为 Jsonnet 外部变量。
	if req.Build != nil {
		mapBuild(req.Build, vm)
	}
	if req.Repo != nil {
		mapRepo(req.Repo, vm)
	}

	var jsonnetFile string
	var jsonnetFileName string
	if template != nil {
		jsonnetFile = template.Data
		jsonnetFileName = template.Name
	} else {
		jsonnetFile = req.Config.Data
		jsonnetFileName = req.Repo.Config
	}
	// 将模板或调用方传入的 input.* 键值对设为 ExtVar。
	if len(templateData) != 0 {
		for k, v := range templateData {
			key := fmt.Sprintf("input.%s", k)
			val := fmt.Sprint(v)
			vm.ExtVar(key, val)
		}
	}

	// 求值 Jsonnet：优先按文档流解析，失败时退化为单文档。
	buf := new(bytes.Buffer)
	docs, err := vm.EvaluateAnonymousSnippetStream(jsonnetFileName, jsonnetFile)
	if err != nil {
		doc, err2 := vm.EvaluateAnonymousSnippet(jsonnetFileName, jsonnetFile)
		if err2 != nil {
			return "", err
		}
		docs = append(docs, doc)
	}

	// 将多段 YAML 文档用 --- 分隔符合并为单一字符串。
	for _, doc := range docs {
		buf.WriteString("---")
		buf.WriteString("\n")
		buf.WriteString(doc)
	}

	return buf.String(), nil
}

// mapBuild 将 core.Build 字段写入 build.* 外部变量，并展开 Params。
func mapBuild(v *core.Build, vm *jsonnet.VM) {
	vm.ExtVar(build+"event", v.Event)
	vm.ExtVar(build+"action", v.Action)
	vm.ExtVar(build+"environment", v.Deploy)
	vm.ExtVar(build+"link", v.Link)
	vm.ExtVar(build+"branch", v.Target)
	vm.ExtVar(build+"source", v.Source)
	vm.ExtVar(build+"before", v.Before)
	vm.ExtVar(build+"after", v.After)
	vm.ExtVar(build+"target", v.Target)
	vm.ExtVar(build+"ref", v.Ref)
	vm.ExtVar(build+"commit", v.After)
	vm.ExtVar(build+"ref", v.Ref)
	vm.ExtVar(build+"title", v.Title)
	vm.ExtVar(build+"message", v.Message)
	vm.ExtVar(build+"source_repo", v.Fork)
	vm.ExtVar(build+"author_login", v.Author)
	vm.ExtVar(build+"author_name", v.AuthorName)
	vm.ExtVar(build+"author_email", v.AuthorEmail)
	vm.ExtVar(build+"author_avatar", v.AuthorAvatar)
	vm.ExtVar(build+"sender", v.Sender)
	fromMap(v.Params, vm)
}

// mapRepo 将 core.Repository 元数据写入 repo.* 外部变量。
func mapRepo(v *core.Repository, vm *jsonnet.VM) {
	vm.ExtVar(repo+"uid", v.UID)
	vm.ExtVar(repo+"name", v.Name)
	vm.ExtVar(repo+"namespace", v.Namespace)
	vm.ExtVar(repo+"slug", v.Slug)
	vm.ExtVar(repo+"git_http_url", v.HTTPURL)
	vm.ExtVar(repo+"git_ssh_url", v.SSHURL)
	vm.ExtVar(repo+"link", v.Link)
	vm.ExtVar(repo+"branch", v.Branch)
	vm.ExtVar(repo+"config", v.Config)
	vm.ExtVar(repo+"private", strconv.FormatBool(v.Private))
	vm.ExtVar(repo+"visibility", v.Visibility)
	vm.ExtVar(repo+"active", strconv.FormatBool(v.Active))
	vm.ExtVar(repo+"trusted", strconv.FormatBool(v.Trusted))
	vm.ExtVar(repo+"protected", strconv.FormatBool(v.Protected))
	vm.ExtVar(repo+"ignore_forks", strconv.FormatBool(v.IgnoreForks))
	vm.ExtVar(repo+"ignore_pull_requests", strconv.FormatBool(v.IgnorePulls))
}

// fromMap 将构建参数字典映射为 build.param* ExtVar。
func fromMap(m map[string]string, vm *jsonnet.VM) {
	for k, v := range m {
		vm.ExtVar(build+param+k, v)
	}
}
