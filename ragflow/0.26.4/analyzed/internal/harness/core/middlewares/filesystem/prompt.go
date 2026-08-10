package filesystem

// fileSystemPrompt 文件系统能力说明，注入 Agent 系统提示
const fileSystemPrompt = `You have access to the file system. You can:
- Read files with read_file
- Write files with write_file
- Search files with glob and grep
- Execute shell commands with execute

Use these capabilities to accomplish file-system related tasks.`

// 列举 read_file/write_file/glob/grep/execute 等工具用途。
