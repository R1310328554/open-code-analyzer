package executor

// parse_regexp：编译带命名捕获组的正则，逐行 FindStringSubmatch 提取字段列。

import (
	"errors"
	"regexp"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
)

// regexpParser 缓存 SubexpNames 到 nameIndex，无命名组时 newRegexpParser 报错。
// regexpParser parses log lines using a compiled regular expression with named capture groups.
type regexpParser struct {
	regex     *regexp.Regexp
	nameIndex map[int]string
}

// newRegexpParser 编译 pattern 并校验至少存在一个命名捕获组。
// newRegexpParser creates a new regexp parser with the given pattern.
// Returns an error if the pattern is invalid or has no named capture groups.
func newRegexpParser(pattern string) (*regexpParser, error) {
	regex, err := regexp.Compile(pattern)
	if err != nil {
		return nil, err
	}

	// Build name index for capture groups
	nameIndex := make(map[int]string)
	for i, name := range regex.SubexpNames() {
		if name != "" {
			nameIndex[i] = name
		}
	}

	if len(nameIndex) == 0 {
		return nil, errors.New("at least one named capture must be supplied")
	}

	return &regexpParser{
		regex:     regex,
		nameIndex: nameIndex,
	}, nil
}

// process 未匹配时返回空 map 且无 error，匹配行按捕获组名填充 result。
// process parses a single line and returns the extracted key-value pairs.
func (r *regexpParser) process(line string) (map[string]string, error) {
	result := make(map[string]string)
	matches := r.regex.FindStringSubmatch(line)

	if matches == nil {
		return result, nil
	}

	for i, value := range matches {
		if name, ok := r.nameIndex[i]; ok {
			result[name] = value
		}
	}
	return result, nil
}

// buildRegexpColumns 构造 regexpParser 后复用 buildColumns 输出各命名组 String 列。
func buildRegexpColumns(input *array.String, pattern string) ([]string, []arrow.Array) {
	parser, err := newRegexpParser(pattern)
	if err != nil {
		return buildErrorColumns(input, "RegexpParseErr", err.Error())
	}

	parseFunc := func(line string) (map[string]string, error) {
		return parser.process(line)
	}

	return buildColumns(input, nil, parseFunc, "RegexpParseErr")
}
// 模式无效或缺少命名组在 pipeline 构造期失败；与 logfmt/json 共用 error 列约定。
