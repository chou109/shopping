#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for tool in node pnpm java mvn rg; do
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

echo "检查公开测试接口未重新开放"
if rg --files service/Neuedu-admin/src/main/java | rg -q '(^|/)TestController\.java$' || \
  rg -n '@(Get|Post|Put|Delete)Mapping\("/test' service/Neuedu-admin/src/main/java >/dev/null || \
  grep -Fq '.antMatchers("/test/**").permitAll()' \
    service/Neuedu-admin/src/main/java/com/neuedu/web/core/config/SecurityConfig.java; then
  echo "发现遗留测试路由或匿名测试白名单，请检查安全配置" >&2
  exit 1
fi

echo "检查生产配置不含仓库默认凭据"
PROD_CONFIG="service/Neuedu-admin/src/main/resources/application-prod.yml"
if rg -n 'username:\s*root|password:\s*123456|login-username:\s*ruoyi|login-password:\s*123456|profile:\s*D:/ruoyi/uploadPath' "$PROD_CONFIG"; then
  echo "生产配置仍含固定账号、密码或本机上传目录" >&2
  exit 1
fi
for required_setting in \
  'url: ${SHOPPING_DB_URL}' \
  'username: ${SHOPPING_DB_USERNAME}' \
  'password: ${SHOPPING_DB_PASSWORD}' \
  'profile: ${RUOYI_UPLOAD_PATH}' \
  'enabled: ${SHOPPING_DRUID_CONSOLE_ENABLED:false}'; do
  if ! rg -Fq "$required_setting" "$PROD_CONFIG"; then
    echo "生产配置缺少安全设置：$required_setting" >&2
    exit 1
  fi
done

echo "检查登录页不会持久化用户密码"
node scripts/check-login-cookie-security.mjs

echo "安装用户端依赖（锁文件冻结）"
pnpm --dir user install --frozen-lockfile
echo "安装管理端依赖（锁文件冻结）"
pnpm --dir admin install --frozen-lockfile

echo "检查大小写敏感的源码导入"
node scripts/check-case-sensitive-imports.mjs
echo "检查 Vue 模板引用"
node scripts/check-vue-template-bindings.mjs

echo "检查用户端 SVG 源文件"
node scripts/check-svg-sprites.mjs user
echo "运行用户端功能测试"
pnpm --dir user test
echo "检查管理端 SVG 源文件"
node scripts/check-svg-sprites.mjs admin
echo "运行管理端功能测试"
pnpm --dir admin test
echo "运行后端测试"
mvn -f service/pom.xml -pl Neuedu-admin -am test

echo "全项目验证通过"
