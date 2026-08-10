// Package skill 提供动态技能加载中间件。
//
// 技能是可复用能力，支持三种加载模式：
//   - Inline：将技能内容注入系统指令
//   - Fork：以独立工具形式暴露技能
//   - ForkWithContext：带父上下文注入的 Fork 模式
//
// 技能来源：
//   - FileSystemBackend：从 Markdown 文件读取技能定义
//   - Embedded content：内嵌技能定义
package skill

// doc.go 为 skill 包概览；实现见 skill.go 与 filesystem_backend.go。
