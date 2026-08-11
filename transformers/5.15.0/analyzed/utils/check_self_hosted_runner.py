# 自托管 Runner 监控：GitHub Actions API 检测离线 runner 并写 Slack 报告
import argparse
import json

from github_utils import get_github_json


# get_runner_status：查询目标 runner 在线状态，离线则抛错
def get_runner_status(target_runners, token):
    offline_runners = []

    status = get_github_json("https://api.github.com/repos/huggingface/transformers/actions/runners", token=token)

    runners = status["runners"]
    for runner in runners:
        if runner["name"] in target_runners:
            if runner["status"] == "offline":
                offline_runners.append(runner)

    # save the result so we can report them on Slack
    with open("offline_runners.txt", "w") as fp:
        fp.write(json.dumps(offline_runners))

    if len(offline_runners) > 0:
        failed = "\n".join([x["name"] for x in offline_runners])
        raise ValueError(f"The following runners are offline:\n{failed}")


if __name__ == "__main__":

    def list_str(values):
        return values.split(",")

    parser = argparse.ArgumentParser()
    # Required parameters
    parser.add_argument(
        "--target_runners",
        default=None,
        type=list_str,
        required=True,
        help="Comma-separated list of runners to check status.",
    )

    parser.add_argument(
        "--token", default=None, type=str, required=True, help="A token that has actions:read permission."
    )
    args = parser.parse_args()

    get_runner_status(args.target_runners, args.token)
