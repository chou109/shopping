#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for tool in node pnpm java mvn; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "缺少必需工具：$tool" >&2
    exit 1
  fi
done

NODE_VERSION="$(node --version)"
PNPM_VERSION="$(pnpm --version)"
EXPECTED_NODE_VERSION="v$(tr -d '\r\n' < .nvmrc)"
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="$(command -v java)"
fi
JAVA_VERSION="$("$JAVA_BIN" -version 2>&1 | awk -F '"' '/version/ { print $2; exit }')"
JAVA_MAJOR="${JAVA_VERSION%%.*}"

if [[ "$NODE_VERSION" != "$EXPECTED_NODE_VERSION" ]]; then
  echo "需要 Node $EXPECTED_NODE_VERSION，当前是 $NODE_VERSION" >&2
  exit 1
fi
if [[ "$PNPM_VERSION" != "12.3.4" ]]; then
  echo "需要 pnpm 12.3.4，当前是 $PNPM_VERSION" >&2
  exit 1
fi
if [[ "$JAVA_MAJOR" != "17" ]]; then
  echo "需要 JDK 17，当前是 ${JAVA_VERSION:-未知版本}" >&2
  exit 1
fi

echo "验证工具链：Node $NODE_VERSION / pnpm $PNPM_VERSION / Java $JAVA_VERSION"

echo "安装用户端依赖（锁文件冻结）"
pnpm --dir user install --frozen-lockfile
echo "安装管理端依赖（锁文件冻结）"
pnpm --dir admin install --frozen-lockfile

echo "运行用户端测试"
pnpm --dir user test
echo "构建用户端"
pnpm --dir user build
echo "构建管理端"
pnpm --dir admin build:prod
echo "运行后端测试"
mvn -f service/pom.xml -pl Neuedu-admin -am test

echo "全项目验证通过"
