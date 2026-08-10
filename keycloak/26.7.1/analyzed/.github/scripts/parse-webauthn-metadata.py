#!/usr/bin/env python
#
# 解析通行密钥（Passkey）认证器 AAGUID 注册表（combined_aaguid.json），并生成：
#   1. keycloak-webauthn-metadata.json — Keycloak 服务端运行时消费的元数据
#   2. 通行密钥图标文件 — 从源数据中的 base64 data-URI 解码写入
#
# 图标文件写入两个目录，供登录主题与账户控制台共同引用：
#   - js/apps/account-ui/public/passkeys/
#   - themes/src/main/resources/theme/base/login/resources/img/passkeys/
#
# 完整用法说明见 services/src/main/resources/README.md。

import base64
import json
import os
import re
import sys
import unicodedata

# 仓库根目录（脚本位于 .github/scripts/，向上两级）
REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

# 图标输出目标目录列表
ICON_DEST_DIRS = [
    os.path.join(REPO_ROOT, "js", "apps", "account-ui", "public", "passkeys"),
    os.path.join(REPO_ROOT, "themes", "src", "main", "resources", "theme", "base", "login", "resources", "img", "passkeys"),
]

# WebAuthn 元数据 JSON 输出路径
METADATA_DEST = os.path.join(REPO_ROOT, "services", "src", "main", "resources", "keycloak-webauthn-metadata.json")


def normalize(input_str):
    """去除变音符号，将认证器名称规范化为安全的 ASCII 文件名。"""
    nfkd_form = unicodedata.normalize('NFKD', input_str)
    return u"".join([c for c in nfkd_form if not unicodedata.combining(c)])



def write_file(name, data):
    """将二进制数据写入所有图标目标目录。"""
    if os.sep in name or name in ('.', '..') or '/' in name or not name:
        print(f"Rejecting unsafe filename: '{name}'", file=sys.stderr)
        return
    for dest_dir in ICON_DEST_DIRS:
        os.makedirs(dest_dir, exist_ok=True)
        with open(os.path.join(dest_dir, name), "wb") as f:
            f.write(data)


def decode_base64(base64_str):
    """解码 base64 字符串，自动补齐填充位。"""
    base64_str += "=" * ((4 - len(base64_str) % 4) % 4)
    return base64.b64decode(base64_str)


def process_icon(icon_data_uri, short_name, flavor):
    """从 data-URI 提取图片、写入磁盘，并返回文件名。"""
    if icon_data_uri is None:
        return None

    if icon_data_uri.startswith('data:image/svg+xml;base64,'):
        base64_str = icon_data_uri[len('data:image/svg+xml;base64,'):]
        name = short_name + "-" + flavor + ".svg"
        write_file(name, decode_base64(base64_str))
        return name
    elif icon_data_uri.startswith('data:image/png;base64,'):
        base64_str = icon_data_uri[len('data:image/png;base64,'):]
        name = short_name + "-" + flavor + ".png"
        write_file(name, decode_base64(base64_str))
        return name
    else:
        print("Unknown data image format: " + icon_data_uri[:40], file=sys.stderr)
        return None


def parse_aaguids(input_file):
    """
    读取 combined_aaguid.json 并生成 keycloak-webauthn-metadata.json 及解码后的图标文件。

    @param input_file: combined_aaguid.json 文件路径
    """
    names = set()
    files = {}
    output = {}

    with open(input_file, 'r', encoding='utf-8') as f:
        contents = json.load(f)

    for aaguid, info in contents.items():
        name = info.get('name')
        if name is None:
            continue

        # 从认证器名称推导短且文件系统安全的名称
        short_name = normalize(name)
        short_name = re.split(r'[^0-9a-zA-Z_\-]', short_name)[0].lower()
        if not short_name:
            print(f"Skipping entry with no usable name: '{name}' (AAGUID: {aaguid})", file=sys.stderr)
            continue

        # 名称冲突时追加数字后缀
        prefix = short_name
        i = 0
        while short_name in names:
            i += 1
            short_name = prefix + str(i)
        names.add(short_name)

        icon_light = info.get('icon_light')
        icon_dark = info.get('icon_dark')

        # 跨认证器去重相同图标
        if icon_light is not None and icon_light in files:
            file_light = files[icon_light]
        else:
            file_light = process_icon(icon_light, short_name, 'light')
            if icon_light is not None:
                files[icon_light] = file_light

        if icon_dark is not None and icon_dark in files:
            file_dark = files[icon_dark]
        else:
            file_dark = process_icon(icon_dark, short_name, 'dark')
            if icon_dark is not None:
                files[icon_dark] = file_dark

        entry = {"name": name}
        if file_light is not None:
            entry["icon_light"] = file_light
        if file_dark is not None:
            entry["icon_dark"] = file_dark

        output[aaguid] = entry

    with open(METADATA_DEST, 'w') as f:
        json.dump(output, f, indent=2)
        f.write('\n')

    print(f"Wrote {len(output)} entries to {METADATA_DEST}")
    print(f"Icons written to: {', '.join(ICON_DEST_DIRS)}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <combined_aaguid.json>", file=sys.stderr)
        sys.exit(1)
    parse_aaguids(sys.argv[1])
