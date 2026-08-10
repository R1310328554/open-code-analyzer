/**
 * 主题相关文件读取工具。
 */

/**
 * 将用户选择的 File 对象读为 Data URI（base64），供主题预览或上传表单使用。
 */
export const fileToDataUri = (file: File) =>
  new Promise<string>((resolve) => {
    const reader = new FileReader();
    reader.onload = (event) => {
      resolve(event.target?.result as string);
    };
    reader.readAsDataURL(file);
  });
