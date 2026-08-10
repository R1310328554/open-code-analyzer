package rules

// rules.parser 从磁盘 YAML 加载 Prometheus/Loki 规则文件：支持多文档流式解码、namespace 默认取文件名及 Loki ruler 校验路径。

import (
	"bytes"
	"errors"
	"io"
	"os"
	"path/filepath"
	"strings"

	"github.com/prometheus/prometheus/model/rulefmt"
	log "github.com/sirupsen/logrus"
	yaml "go.yaml.in/yaml/v3"

	"github.com/grafana/loki/v3/pkg/ruler"
)

const (
	LokiBackend = "loki"
)

var (
	errFileReadError = errors.New("file read error")
)

// ParseFiles 遍历文件列表，默认 ParseLoki 解析并保证 namespace 唯一不重复。
// ParseFiles returns a formatted set of prometheus rule groups
func ParseFiles(files []string) (map[string]RuleNamespace, error) {
	ruleSet := map[string]RuleNamespace{}
	var parseFn = ParseLoki

	for _, f := range files {
		nss, errs := parseFn(f)
		for _, err := range errs {
			log.WithError(err).WithField("file", f).Errorln("unable parse rules file")
			return nil, errFileReadError
		}

		for _, ns := range nss {
			ns.Filepath = f

			// Determine if the namespace is explicitly set. If not
			// the file name without the extension is used.
			namespace := ns.Namespace
			if namespace == "" {
				namespace = strings.TrimSuffix(filepath.Base(f), filepath.Ext(f))
				ns.Namespace = namespace
			}

			_, exists := ruleSet[namespace]
			if exists {
				log.WithFields(log.Fields{
					"namespace": namespace,
					"file":      f,
				}).Errorln("repeated namespace attempted to be loaded")
				return nil, errFileReadError
			}
			ruleSet[namespace] = ns
		}
	}
	return ruleSet, nil
}

// Parse 读取单文件字节后调用 ParseBytes，KnownFields 严格拒绝未知 YAML 键。
// Parse parses and validates a set of rules.
func Parse(f string) ([]RuleNamespace, []error) {
	content, err := loadFile(f)
	if err != nil {
		log.WithError(err).WithField("file", f).Errorln("unable load rules file")
		return nil, []error{errFileReadError}
	}

	return ParseBytes(content)
}

func ParseBytes(content []byte) ([]RuleNamespace, []error) {
	decoder := yaml.NewDecoder(bytes.NewReader(content))
	decoder.KnownFields(true)

	var nss []RuleNamespace
	for {
		var ns RuleNamespace
		err := decoder.Decode(&ns)
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, []error{err}
		}

		if errs := ns.Validate(); len(errs) > 0 {
			return nil, errs
		}

		nss = append(nss, ns)
	}
	return nss, nil
}

// ParseLoki 提取 rulefmt.RuleGroup 后调用 ruler.ValidateGroups 做 Loki 侧校验。
func ParseLoki(f string) ([]RuleNamespace, []error) {
	content, err := loadFile(f)
	if err != nil {
		log.WithError(err).WithField("file", f).Errorln("unable load rules file")
		return nil, []error{errFileReadError}
	}

	decoder := yaml.NewDecoder(bytes.NewReader(content))
	decoder.KnownFields(true)

	var nss []RuleNamespace
	for {
		var ns RuleNamespace
		err := decoder.Decode(&ns)
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, []error{err}
		}

		// the upstream loki validator only validates the rulefmt rule groups,
		// not the remote write configs this type attaches.
		var grps []rulefmt.RuleGroup
		for _, g := range ns.Groups {
			grps = append(grps, g.RuleGroup)
		}

		if errs := ruler.ValidateGroups(grps...); len(errs) > 0 {
			return nil, errs
		}

		nss = append(nss, ns)

	}
	return nss, nil
}

// loadFile 一次性读取整个规则文件到内存 buffer，供 decoder 解析。
func loadFile(filename string) ([]byte, error) {
	file, err := os.Open(filename)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	fileinfo, err := file.Stat()
	if err != nil {
		return nil, err
	}

	filesize := fileinfo.Size()
	buffer := make([]byte, filesize)

	_, err = file.Read(buffer)
	if err != nil {
		return nil, err
	}

	return buffer, nil
}
// 未显式指定 namespace 时用文件名（去扩展名）作为 ruler 中的逻辑 namespace。
