"""
Google Drive MIME 常量：文件夹、快捷方式及占位不支持类型内容。
"""

# 暂不支持类型的索引正文占位（保持空串）
UNSUPPORTED_FILE_TYPE_CONTENT = ""  # keep empty for now
# Google Drive 文件夹 MIME
DRIVE_FOLDER_TYPE = "application/vnd.google-apps.folder"
DRIVE_SHORTCUT_TYPE = "application/vnd.google-apps.shortcut"
DRIVE_FILE_TYPE = "application/vnd.google-apps.file"
