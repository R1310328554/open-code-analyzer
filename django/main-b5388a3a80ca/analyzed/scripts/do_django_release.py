#! /usr/bin/env python

"""
do_django_release — 构建 wheel/tarball、生成校验和并输出发布清单。
"""

"""Helper to build and publish Django artifacts.

Original author: Tim Graham.
Other authors: Mariusz Felisiak, Natalia Bidart.

"""

import hashlib
import os
import re
import subprocess
from datetime import date

# 发布 checksum 文件模板：MD5/SHA1/SHA256 与 Git tag 说明
checksum_file_text = """This file contains MD5, SHA1, and SHA256 checksums for the
source-code tarball and wheel files of Django {django_version}, released {release_date}.

It also includes the commit hash of the release tag, identifying the exact
source revision the artifacts were built from.

To use this file, you will need a working install of PGP or other
compatible public-key encryption software. You will also need to have
the Django release manager's public key in your keyring. This key has
the ID ``{pgp_key_id}`` and can be imported from GitHub, for example, if
using the open-source GNU Privacy Guard implementation of PGP:

    curl {pgp_key_url} | gpg --import -

Once the key is imported, verify this file:

    gpg --verify {checksum_file_name}

Once you have verified this file, you can use normal MD5, SHA1, or SHA256
checksumming applications to generate the checksums of the Django
package and compare them to the checksums listed below.

Release packages
================

https://www.djangoproject.com/download/{django_version}/tarball/
https://www.djangoproject.com/download/{django_version}/wheel/

MD5 checksums
=============

{md5_tarball}  {tarball_name}
{md5_wheel}  {wheel_name}

SHA1 checksums
==============

{sha1_tarball}  {tarball_name}
{sha1_wheel}  {wheel_name}

SHA256 checksums
================

{sha256_tarball}  {tarball_name}
{sha256_wheel}  {wheel_name}

Git tag
=======

The {django_version} tag points to commit {commit_hash}.
"""


# 调用 build 模块生成 dist/ 下的 wheel 与 sdist
def build_artifacts():
    from build.__main__ import main as build_main

    build_main([])


# 对指定发布文件计算十六进制摘要
def do_checksum(checksum_algo, release_file, dist_path):
    with open(os.path.join(dist_path, release_file), "rb") as f:
        return checksum_algo(f.read()).hexdigest()


# 当前 HEAD 的 commit hash，写入 checksum 文件
def get_commit_hash():
    return subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()


# 从完整版本号提取主版本（去掉 a/b/rc 后缀）供 diff 目录名
def parse_major_version(django_version):
    major = ".".join(django_version.split(".")[:2])
    match = re.search("[abrc]", major)
    if match:
        major = major[: match.start()]
    return major


# 在 dist/ 中定位 .whl 与 .tar.gz 文件名
def find_release_artifacts(dist_path):
    wheel_name = None
    tarball_name = None
    for f in os.listdir(dist_path):
        if f.endswith(".whl"):
            wheel_name = f
        elif f.endswith(".tar.gz"):
            tarball_name = f
    return wheel_name, tarball_name


# 填充 checksum 模板并写入 Django-{version}.checksum.txt
def create_checksum_file(
    *,
    django_version,
    release_date,
    checksum_file_path,
    tarball_name,
    wheel_name,
    commit_hash,
    dist_path,
    pgp_key_id,
    pgp_key_url,
):
    kwargs = dict(
        release_date=release_date,
        pgp_key_id=pgp_key_id,
        django_version=django_version,
        pgp_key_url=pgp_key_url,
        checksum_file_name=os.path.basename(checksum_file_path),
        wheel_name=wheel_name,
        tarball_name=tarball_name,
        commit_hash=commit_hash,
    )
    for checksum_name, checksum_algo in (
        ("md5", hashlib.md5),
        ("sha1", hashlib.sha1),
        ("sha256", hashlib.sha256),
    ):
        kwargs[f"{checksum_name}_tarball"] = do_checksum(
            checksum_algo, tarball_name, dist_path
        )
        kwargs[f"{checksum_name}_wheel"] = do_checksum(
            checksum_algo, wheel_name, dist_path
        )
    with open(checksum_file_path, "wb") as f:
        f.write(checksum_file_text.format(**kwargs).encode("ascii"))


# 主流程：清理、构建、校验、diff 源码并打印发布步骤
def main():
    pgp_key_id = os.getenv("PGP_KEY_ID")
    pgp_key_url = os.getenv("PGP_KEY_URL")
    pgp_email = os.getenv("PGP_EMAIL")
    dest_folder = os.path.expanduser(os.getenv("DEST_FOLDER"))

    assert (
        pgp_key_id
    ), "Missing PGP_KEY_ID: Set this env var to your PGP key ID (used for signing)."
    assert (
        pgp_key_url
    ), "Missing PGP_KEY_URL: Set this env var to your PGP public key URL."
    assert dest_folder and os.path.exists(
        dest_folder
    ), "Missing DEST_FOLDER: Set this env var to the path to place the artifacts."

    # 发布前 git clean -fdx 确保工作区干净
    # Ensure the working directory is clean.
    subprocess.call(["git", "clean", "-fdx"])

    commit_hash = get_commit_hash()

    django_repo_path = os.path.abspath(os.path.curdir)
    dist_path = os.path.join(django_repo_path, "dist")

    # 构建 wheel 与源码 tarball
    # Build release files.
    build_artifacts()
    wheel_name, tarball_name = find_release_artifacts(dist_path)

    assert wheel_name is not None
    assert tarball_name is not None

    django_version = wheel_name.split("-")[1]
    django_major_version = parse_major_version(django_version)
    artifacts_path = os.path.join(dest_folder, django_version)
    os.makedirs(artifacts_path, exist_ok=True)
    release_date = date.today().strftime("%B %-d, %Y")
    checksum_file_path = os.path.join(
        artifacts_path, f"Django-{django_version}.checksum.txt"
    )

    create_checksum_file(
        django_version=django_version,
        release_date=release_date,
        checksum_file_path=checksum_file_path,
        wheel_name=wheel_name,
        tarball_name=tarball_name,
        commit_hash=commit_hash,
        dist_path=dist_path,
        pgp_key_id=pgp_key_id,
        pgp_key_url=pgp_key_url,
    )

    # 解压 wheel 与 checkout 中 django/ 做 diff -qr sanity check
    print("\n\nDiffing release with checkout for sanity check.")

    # 解压 wheel、对比包内 django 与当前树，再删除临时目录
    # Unzip and diff...
    unzip_command = [
        "unzip",
        "-q",
        os.path.join(dist_path, wheel_name),
        "-d",
        os.path.join(dist_path, django_major_version),
    ]
    subprocess.run(unzip_command)
    diff_command = [
        "diff",
        "-qr",
        "./django/",
        os.path.join(dist_path, django_major_version, "django"),
    ]
    subprocess.run(diff_command)
    subprocess.run(
        [
            "rm",
            "-rf",
            os.path.join(dist_path, django_major_version),
        ]
    )

    # 以下打印 gpg 签名、打 tag、上传 PyPI 等人工步骤
    print("\n\n=> Commands to run NOW:")

    # 对 checksum 文件 clearsign（可能提示 PGP 口令）
    # Sign the checksum file, this may prompt for a passphrase.
    pgp_email_flag = f"-u {pgp_email} " if pgp_email else ""
    print(f"gpg --clearsign {pgp_email_flag}--digest-algo SHA256 {checksum_file_path}")
    # 创建带签名 release tag 并本地 verify
    # Create, verify and push tag.
    print(f'git tag --sign --message="Tag {django_version}" {django_version}')
    print(f"git tag --verify {django_version}")

    # 将 dist 复制到 DEST_FOLDER 以免 clean 丢失产物
    # Copy binaries outside the current repo tree to avoid lossing them.
    subprocess.run(["cp", "-r", dist_path, artifacts_path])

    # 临近发布时间再上传 djangoproject admin 与 PyPI
    # Make the binaries available to the world
    print(
        "\n\n=> These ONLY 15 MINUTES BEFORE RELEASE TIME (consider new terminal "
        "session with isolated venv)!"
    )

    # 在 releases/release 后台添加上传 tarball、wheel 与 .asc
    # Upload the checksum file and artifacts to the djangoproject admin.
    print(
        "\n==> ACTION Add tarball, wheel, and checksum files to the Release entry at:"
        f"https://www.djangoproject.com/admin/releases/release/{django_version}"
    )
    print(
        f"* Tarball and wheel from {artifacts_path}\n"
        f"* Signed checksum {checksum_file_path}.asc"
    )

    # 运行 verify_release.sh 校验 GPG、摘要与冒烟测试
    # Verify the release artifacts (GPG signature, checksums, and smoke test).
    print("\n==> ACTION Verify the release artifacts:")
    print(f"VERSION={django_version} verify_release.sh")

    # twine upload --repository django 上传 wheel/sdist
    # Upload to PyPI.
    print("\n==> ACTION Upload to PyPI, ensure your release venv is activated:")
    print(f"cd {artifacts_path}")
    print("pip install -U pip twine")
    print("twine upload --repository django dist/*")

    # 最后 git push --tags 推送 release tag
    # Push the tags.
    print("\n==> ACTION Push the tags:")
    print("git push --tags")

    print("\n\nDONE!!!")


if __name__ == "__main__":
    main()
