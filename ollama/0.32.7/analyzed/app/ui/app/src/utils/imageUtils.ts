/**
 * 根据文件名扩展名判断是否为支持的图片类型。
 */
/** 匹配 png/jpg/jpeg/gif/webp（大小写不敏感）。 */
export function isImageFile(filename: string): boolean {
  const extension = filename.toLowerCase().split(".").pop();
  return ["png", "jpg", "jpeg", "gif", "webp"].includes(extension || "");
}
