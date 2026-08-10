//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// guard.go — 技能安装安全策略：解析信任级别、执行安装决策并格式化扫描报告。

//

package security

import (
	"fmt"
	"strings"
)

// Guard provides security policy enforcement
// Guard 持有受信仓库列表与各信任级别的安装策略。
type Guard struct {
	trustedRepos map[string]bool
	policy       map[string][3]string
}

// NewGuard creates a new security guard
// NewGuard 使用默认 TrustedRepos 与 InstallPolicy 构造 Guard。
func NewGuard() *Guard {
	return &Guard{
		trustedRepos: TrustedRepos,
		policy:       InstallPolicy,
	}
}

// extractCanonicalRepo extracts the canonical owner/repo from an identifier
// Supports formats: "owner/repo", "github.com/owner/repo/path", "owner/repo/path"
// extractCanonicalRepo 从多种 URL 格式提取 owner/repo 规范键。
func extractCanonicalRepo(identifier string) string {
	// Normalize the identifier
	identifier = strings.TrimSpace(identifier)
	identifier = strings.ToLower(identifier)

	// Remove protocol prefix if present
	if idx := strings.Index(identifier, "://"); idx != -1 {
		identifier = identifier[idx+3:]
	}

	// Remove github.com prefix if present
	if strings.HasPrefix(identifier, "github.com/") {
		identifier = strings.TrimPrefix(identifier, "github.com/")
	}

	// Split into parts
	parts := strings.Split(identifier, "/")
	if len(parts) < 2 {
		return ""
	}

	// Extract owner and repo (first two components)
	owner := strings.TrimSpace(parts[0])
	repo := strings.TrimSpace(parts[1])

	if owner == "" || repo == "" {
		return ""
	}

	return owner + "/" + repo
}

// ResolveTrustLevel determines the trust level based on source and identifier
// ResolveTrustLevel 根据来源与标识符判定 builtin/trusted/community。
func (g *Guard) ResolveTrustLevel(source, identifier string) string {
	// Official/builtin source
	if source == "official" || source == "builtin" {
		return "builtin"
	}

	// Extract canonical repo key and check against trusted repositories
	canonicalRepo := extractCanonicalRepo(identifier)
	if canonicalRepo != "" && g.trustedRepos[canonicalRepo] {
		return "trusted"
	}

	// Default to community
	return "community"
}

// ShouldAllowInstall determines if installation should be allowed based on scan results
// Returns (allowed bool, reason string)
// ShouldAllowInstall 依据扫描结果与策略决定是否允许安装。
func (g *Guard) ShouldAllowInstall(result *ScanResult, force bool) (bool, string) {
	policy, ok := g.policy[result.TrustLevel]
	if !ok {
		policy = g.policy["community"]
	}

	vi, ok := VerdictIndex[result.Verdict]
	if !ok {
		vi = 2 // dangerous
	}

	decision := policy[vi]

	switch decision {
	case "allow":
		return true, fmt.Sprintf("Allowed (%s source, %s verdict)", result.TrustLevel, result.Verdict)
	case "ask":
		return false, fmt.Sprintf("Requires confirmation (%s source + %s verdict, %d findings)",
			result.TrustLevel, result.Verdict, len(result.Findings))
	case "block":
		if force {
			return true, fmt.Sprintf("Force-installed despite %s verdict (%d findings)",
				result.Verdict, len(result.Findings))
		}
		return false, fmt.Sprintf("Blocked (%s source + %s verdict, %d findings). Use --force to override.",
			result.TrustLevel, result.Verdict, len(result.Findings))
	}

	return false, "Unknown policy decision"
}

// FormatScanReport formats a scan result for display
// FormatScanReport 将扫描结果格式化为终端可读报告。
func (g *Guard) FormatScanReport(result *ScanResult) string {
	var sb strings.Builder

	sb.WriteString("╔════════════════════════════════════════════════════════════════╗\n")
	sb.WriteString(fmt.Sprintf("║ Security Scan Report: %-40s ║\n", result.SkillName))
	sb.WriteString("╚════════════════════════════════════════════════════════════════╝\n")
	sb.WriteString(fmt.Sprintf("Source:      %s\n", result.Source))
	sb.WriteString(fmt.Sprintf("Trust Level: %s\n", result.TrustLevel))
	sb.WriteString(fmt.Sprintf("Verdict:     %s\n", result.Verdict))
	sb.WriteString(fmt.Sprintf("Findings:    %d\n", len(result.Findings)))

	if len(result.Findings) > 0 {
		sb.WriteString("\n─── Findings ───\n")

		// Group by severity
		severityOrder := []string{"critical", "high", "medium", "low"}
		for _, sev := range severityOrder {
			for _, f := range result.Findings {
				if f.Severity == sev {
					sb.WriteString(fmt.Sprintf("\n[%s] %s\n", strings.ToUpper(sev), f.PatternID))
					sb.WriteString(fmt.Sprintf("  Category: %s\n", f.Category))
					sb.WriteString(fmt.Sprintf("  File: %s:%d\n", f.File, f.Line))
					sb.WriteString(fmt.Sprintf("  Match: %s\n", f.Match))
					sb.WriteString(fmt.Sprintf("  Description: %s\n", f.Description))
				}
			}
		}
	}

	sb.WriteString("\n")
	return sb.String()
}

// AddTrustedRepo adds a repository to the trusted list
// AddTrustedRepo 动态添加受信 GitHub 仓库。
func (g *Guard) AddTrustedRepo(repo string) {
	g.trustedRepos[repo] = true
}

// IsTrustedRepo checks if a repository is trusted
// IsTrustedRepo 检查仓库是否在受信列表中。
func (g *Guard) IsTrustedRepo(repo string) bool {
	return g.trustedRepos[repo]
}
