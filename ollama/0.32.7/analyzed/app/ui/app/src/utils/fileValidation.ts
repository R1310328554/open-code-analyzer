/**
 * 聊天附件上传与原生文件选择的共享校验逻辑。
 */
import { Model } from "@/gotypes";
// FileUpload 组件与原生对话框共用的校验逻辑
// Shared file validation logic used by both FileUpload and native dialog selection

/** 允许作为文本附件上传的扩展名列表。 */
export const TEXT_FILE_EXTENSIONS = [
  "pdf",
  "docx",
  "txt",
  "md",
  "csv",
  "json",
  "xml",
  "html",
  "htm",
  "js",
  "jsx",
  "ts",
  "tsx",
  "py",
  "java",
  "cpp",
  "c",
  "cc",
  "h",
  "cs",
  "php",
  "rb",
  "go",
  "rs",
  "swift",
  "kt",
  "scala",
  "sh",
  "bat",
  "yaml",
  "yml",
  "toml",
  "ini",
  "cfg",
  "conf",
  "log",
  "rtf",
];

/** 允许的图片扩展名（含 WebP）。 */
export const IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "webp"];

/** validateFile / processFiles 的可选约束。 */
export interface FileValidationOptions {
  maxFileSize?: number; // 上限，单位 MB
  // in MB
  allowedExtensions?: string[];
  hasVisionCapability?: boolean;
  selectedModel?: Model | null;
  customValidator?: (file: File) => { valid: boolean; error?: string };
}

export interface ValidationResult {
  valid: boolean;
  error?: string;
}

/** 校验扩展名、大小与 customValidator；不检查 vision（由 UI 决定）。 */
export function validateFile(
  file: File,
  options: FileValidationOptions = {},
): ValidationResult {
  const {
    maxFileSize = 10,
    allowedExtensions = [...TEXT_FILE_EXTENSIONS, ...IMAGE_EXTENSIONS],
    customValidator,
  } = options;

  const MAX_FILE_SIZE = maxFileSize * 1024 * 1024; // MB 转字节
  // Convert MB to bytes
  const fileExtension = file.name.toLowerCase().split(".").pop();

  // 优先执行调用方自定义校验
  // Custom validation first
  if (customValidator) {
    const customResult = customValidator(file);
    if (!customResult.valid) {
      return customResult;
    }
  }

  // 扩展名白名单检查
  // File extension validation
  if (!fileExtension || !allowedExtensions.includes(fileExtension)) {
    return { valid: false, error: "File type not supported" };
  }

  // 文件大小上限检查
  // File size validation
  if (file.size > MAX_FILE_SIZE) {
    return { valid: false, error: "File too large" };
  }

  return { valid: true };
}

/** 将 File 读为 Uint8Array，供上传 API 序列化。 */
// Helper function to read file as Uint8Array
export function readFileAsBytes(file: File): Promise<Uint8Array> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const arrayBuffer = reader.result as ArrayBuffer;
      resolve(new Uint8Array(arrayBuffer));
    };
    reader.onerror = () => reject(reader.error);
    reader.readAsArrayBuffer(file);
  });
}

/** 批量校验并读取文件，分离 validFiles 与 errors。 */
// Process multiple files with validation
export async function processFiles(
  files: File[],
  options: FileValidationOptions = {},
): Promise<{
  validFiles: Array<{ filename: string; data: Uint8Array; type?: string }>;
  errors: Array<{ filename: string; error: string }>;
}> {
  const validFiles: Array<{
    filename: string;
    data: Uint8Array;
    type?: string;
  }> = [];
  const errors: Array<{ filename: string; error: string }> = [];

  for (const file of files) {
    const validation = validateFile(file, options);

    if (!validation.valid) {
      errors.push({
        filename: file.name,
        error: validation.error || "File validation failed",
      });
      continue;
    }

    try {
      const fileBytes = await readFileAsBytes(file);
      validFiles.push({
        filename: file.name,
        data: fileBytes,
        type: file.type || undefined,
      });
    } catch (error) {
      console.error(`Error reading file ${file.name}:`, error);
      errors.push({
        filename: file.name,
        error: "Error reading file",
      });
    }
  }

  return { validFiles, errors };
}
