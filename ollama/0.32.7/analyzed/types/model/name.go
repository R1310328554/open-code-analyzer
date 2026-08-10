// Package model 提供模型名称解析、校验与 digest 相关工具。
// Package model contains types and utilities for parsing, validating, and// Package model contains types and utilities for parsing, validating, and
// working with model names and digests.
package model

import (
	"cmp"
	"errors"
	"fmt"
	"log/slog"
	"net/url"
	"path/filepath"
	"strings"
)

// Errors 包级错误变量。
// Errors
var (
	// ErrUnqualifiedName 表示名称未完全限定。
// ErrUnqualifiedName represents an error where a name is not fully
	// qualified. It is not used directly in this package, but is here
	// to avoid other packages inventing their own error type.
	// Additionally, it can be conveniently used via [Unqualified].
	ErrUnqualifiedName = errors.New("unqualified name")
)

// Unqualified 包装 ErrUnqualifiedName 并附带名称。
// Unqualified is a helper function that returns an error with
// ErrUnqualifiedName as the cause and the name as the message.
func Unqualified(n Name) error {
	return fmt.Errorf("%w: %s", ErrUnqualifiedName, n)
}

// MissingPart 标记分隔符存在但对应部分缺失的占位符。
// MissingPart is used to indicate any part of a name that was "promised" by
// the presence of a separator, but is missing.
//
// The value was chosen because it is deemed unlikely to be set by a user,
// not a valid part name valid when checked by [Name.IsValid], and easy to
// spot in logs.
const MissingPart = "!MISSING!"

// 默认 registry 主机、命名空间、标签与协议。
const (
	defaultHost           = "registry.ollama.ai"
	defaultNamespace      = "library"
	defaultTag            = "latest"
	defaultProtocolScheme = "https"
)

// DefaultName 返回带默认 host/namespace/tag/scheme 的名称。
// DefaultName returns a name with the default values for the host, namespace,
// tag, and protocol scheme parts. The model and digest parts are empty.
//
//   - The default host is ("registry.ollama.ai")
//   - The default namespace is ("library")
//   - The default tag is ("latest")
//   - The default protocol scheme is ("https")
func DefaultName() Name {
	return Name{
		Host:           defaultHost,
		Namespace:      defaultNamespace,
		Tag:            defaultTag,
		ProtocolScheme: defaultProtocolScheme,
	}
}

// partKind 标识名称各组成部分。
type partKind int

const (
	kindHost partKind = iota
	kindNamespace
	kindModel
	kindTag
	kindDigest
)

func (k partKind) String() string {
	switch k {
	case kindHost:
		return "host"
	case kindNamespace:
		return "namespace"
	case kindModel:
		return "model"
	case kindTag:
		return "tag"
	case kindDigest:
		return "digest"
	default:
		return "unknown"
	}
}

// Name 为模型名称的结构化表示，不保证合法，需用 IsValid 校验。
// Name is a structured representation of a model name string, as defined by
// [ParseNameNoDefaults].
//
// It is not guaranteed to be valid. Use [Name.IsValid] to check if the name
// is valid.
type Name struct {
	Host           string
	Namespace      string
	Model          string
	Tag            string
	ProtocolScheme string
}

// ParseName 解析名称字符串并与 DefaultName 合并缺省部分。
// ParseName parses and assembles a Name from a name string. The
// format of a valid name string is:
//
//	  s:
//		  { host } "/" { namespace } "/" { model } ":" { tag } "@" { digest }
//		  { host } "/" { namespace } "/" { model } ":" { tag }
//		  { host } "/" { namespace } "/" { model } "@" { digest }
//		  { host } "/" { namespace } "/" { model }
//		  { namespace } "/" { model } ":" { tag } "@" { digest }
//		  { namespace } "/" { model } ":" { tag }
//		  { namespace } "/" { model } "@" { digest }
//		  { namespace } "/" { model }
//		  { model } ":" { tag } "@" { digest }
//		  { model } ":" { tag }
//		  { model } "@" { digest }
//		  { model }
//		  "@" { digest }
//	  host:
//	      pattern: { alphanum | "_" } { alphanum | "-" | "_" | "." | ":" }*
//	      length:  [1, 350]
//	  namespace:
//	      pattern: { alphanum | "_" } { alphanum | "-" | "_" }*
//	      length:  [1, 80]
//	  model:
//	      pattern: { alphanum | "_" } { alphanum | "-" | "_" | "." }*
//	      length:  [1, 80]
//	  tag:
//	      pattern: { alphanum | "_" } { alphanum | "-" | "_" | "." }*
//	      length:  [1, 80]
//	  digest:
//	      pattern: { alphanum | "_" } { alphanum | "-" | ":" }*
//	      length:  [1, 80]
//
// Most users should use [ParseName] instead, unless need to support
// different defaults than DefaultName.
//
// The name returned is not guaranteed to be valid. If it is not valid, the
// field values are left in an undefined state. Use [Name.IsValid] to check
// if the name is valid.
func ParseName(s string) Name {
	return Merge(ParseNameBare(s), DefaultName())
}

// ParseNameBare 解析名称但不合并默认值。
// ParseNameBare parses s as a name string and returns a Name. No merge with
// [DefaultName] is performed.
func ParseNameBare(s string) Name {
	var n Name
	var promised bool

	// "/" 非法出现在 tag 中，故可用 "/" 从右向左切分 host。
	// "/" is an illegal tag character, so we can use it to split the host
	if strings.LastIndex(s, ":") > strings.LastIndex(s, "/") {
		s, n.Tag, _ = cutPromised(s, ":")
	}

	s, n.Model, promised = cutPromised(s, "/")
	if !promised {
		n.Model = s
		return n
	}

	s, n.Namespace, promised = cutPromised(s, "/")
	if !promised {
		n.Namespace = s
		return n
	}

	scheme, host, ok := strings.Cut(s, "://")
	if ok {
		n.ProtocolScheme = scheme
	} else {
		host = scheme
	}
	n.Host = host

	return n
}

// ParseNameFromFilepath 将四段文件路径解析为 Name。
// ParseNameFromFilepath parses a 4-part filepath as a Name. The parts are
// expected to be in the form:
//
// { host } "/" { namespace } "/" { model } "/" { tag }
func ParseNameFromFilepath(s string) (n Name) {
	parts := strings.Split(s, string(filepath.Separator))
	if len(parts) != 4 {
		return Name{}
	}

	n.Host = parts[0]
	n.Namespace = parts[1]
	n.Model = parts[2]
	n.Tag = parts[3]
	if !n.IsFullyQualified() {
		return Name{}
	}

	return n
}

// Merge 合并两名称的 host/namespace/tag/scheme，优先 a 的非空部分。
// Merge merges the host, namespace, tag, and protocol scheme parts of the two names,
// preferring the non-empty parts of a.
func Merge(a, b Name) Name {
	a.Host = cmp.Or(a.Host, b.Host)
	a.Namespace = cmp.Or(a.Namespace, b.Namespace)
	a.Tag = cmp.Or(a.Tag, b.Tag)
	a.ProtocolScheme = cmp.Or(a.ProtocolScheme, b.ProtocolScheme)
	return a
}

// String 在名称合法时返回 ParseName 可接受的字符串形式。
// String returns the name string, in the format that [ParseNameNoDefaults]
// accepts as valid, if [Name.IsValid] reports true; otherwise the empty
// string is returned.
func (n Name) String() string {
	var b strings.Builder
	if n.Host != "" {
		b.WriteString(n.Host)
		b.WriteByte('/')
	}
	if n.Namespace != "" {
		b.WriteString(n.Namespace)
		b.WriteByte('/')
	}
	b.WriteString(n.Model)
	if n.Tag != "" {
		b.WriteByte(':')
		b.WriteString(n.Tag)
	}
	return b.String()
}

// DisplayShortest 返回省略默认 host/namespace 的短显示名。
// DisplayShortest returns a short string version of the name.
func (n Name) DisplayShortest() string {
	var sb strings.Builder

	if !strings.EqualFold(n.Host, defaultHost) {
		sb.WriteString(n.Host)
		sb.WriteByte('/')
		sb.WriteString(n.Namespace)
		sb.WriteByte('/')
	} else if !strings.EqualFold(n.Namespace, defaultNamespace) {
		sb.WriteString(n.Namespace)
		sb.WriteByte('/')
	}

	// always include model and tag
	sb.WriteString(n.Model)
	sb.WriteString(":")
	sb.WriteString(n.Tag)
	return sb.String()
}

// IsValidNamespace 校验命名空间字符串是否合法。
// IsValidNamespace reports whether the provided string is a valid
// namespace.
func IsValidNamespace(s string) bool {
	return isValidPart(kindNamespace, s)
}

// IsValid 检查名称各部分是否完整且合法。
// IsValid reports whether all parts of the name are present and valid. The
// digest is a special case, and is checked for validity only if present.
//
// Note: The digest check has been removed as is planned to be added back in
// at a later time.
func (n Name) IsValid() bool {
	return n.IsFullyQualified()
}

// IsFullyQualified 判断 host/namespace/model/tag 是否均合法存在。
// IsFullyQualified returns true if all parts of the name are present and
// valid without the digest.
func (n Name) IsFullyQualified() bool {
	parts := []string{
		n.Host,
		n.Namespace,
		n.Model,
		n.Tag,
	}
	for i, part := range parts {
		if !isValidPart(partKind(i), part) {
			return false
		}
	}
	return true
}

// Filepath 返回 host/namespace/model/tag 四段 canonical 路径。
// Filepath returns a canonical filepath that represents the name with each part from
// host to tag as a directory in the form:
//
//	{host}/{namespace}/{model}/{tag}
//
// It uses the system's filepath separator and ensures the path is clean.
//
// It panics if the name is not fully qualified. Use [Name.IsFullyQualified]
// to check if the name is fully qualified.
func (n Name) Filepath() string {
	if !n.IsFullyQualified() {
		panic("illegal attempt to get filepath of invalid name")
	}
	return filepath.Join(
		n.Host,
		n.Namespace,
		n.Model,
		n.Tag,
	)
}

// LogValue 供 slog 结构化日志输出名称字符串。
// LogValue returns a slog.Value that represents the name as a string.
func (n Name) LogValue() slog.Value {
	return slog.StringValue(n.String())
}

// EqualFold 大小写不敏感比较两名称各段。
func (n Name) EqualFold(o Name) bool {
	return strings.EqualFold(n.Host, o.Host) &&
		strings.EqualFold(n.Namespace, o.Namespace) &&
		strings.EqualFold(n.Model, o.Model) &&
		strings.EqualFold(n.Tag, o.Tag)
}

// BaseURL 构造 registry 的 scheme+host URL。
// BaseURL returns the base URL for the registry.
func (n Name) BaseURL() *url.URL {
	return &url.URL{
		Scheme: n.ProtocolScheme,
		Host:   n.Host,
	}
}

// DisplayNamespaceModel 返回 namespace/model 拼接字符串。
// DisplayNamespaceModel returns the namespace and model joined by "/".
func (n Name) DisplayNamespaceModel() string {
	var b strings.Builder
	b.WriteString(n.Namespace)
	b.WriteByte('/')
	b.WriteString(n.Model)
	return b.String()
}

func isValidLen(kind partKind, s string) bool {
	switch kind {
	case kindHost:
		return len(s) >= 1 && len(s) <= 350
	case kindTag:
		return len(s) >= 1 && len(s) <= 80
	default:
		return len(s) >= 1 && len(s) <= 80
	}
}

func isValidPart(kind partKind, s string) bool {
	if !isValidLen(kind, s) {
		return false
	}
	for i := range s {
		if i == 0 {
			if !isAlphanumericOrUnderscore(s[i]) {
				return false
			}
			continue
		}
		switch s[i] {
		case '_', '-':
		case '.':
			if kind == kindNamespace {
				return false
			}
		case ':':
			if kind != kindHost && kind != kindDigest {
				return false
			}
		default:
			if !isAlphanumericOrUnderscore(s[i]) {
				return false
			}
		}
	}
	return true
}

func isAlphanumericOrUnderscore(c byte) bool {
	return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_'
}

func cutLast(s, sep string) (before, after string, ok bool) {
	i := strings.LastIndex(s, sep)
	if i >= 0 {
		return s[:i], s[i+len(sep):], true
	}
	return s, "", false
}

// cutPromised 在最后一个 sep 处切分，空段替换为 MissingPart。
// cutPromised cuts the last part of s at the last occurrence of sep. If sep is
// found, the part before and after sep are returned as-is unless empty, in
// which case they are returned as MissingPart, which will cause
// [Name.IsValid] to return false.
func cutPromised(s, sep string) (before, after string, ok bool) {
	before, after, ok = cutLast(s, sep)
	if !ok {
		return before, after, false
	}
	return cmp.Or(before, MissingPart), cmp.Or(after, MissingPart), true
}
