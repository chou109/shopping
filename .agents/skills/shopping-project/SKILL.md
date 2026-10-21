---
name: shopping-project
description: Use when implementing, debugging, reviewing, or validating features in this repository's shopping frontend or Spring Boot backend.
---

# Shopping project

Use this skill for changes in the `shopping` repository. First inspect the target module and its nearby implementation; this repository contains two Vue applications and a multi-module Java backend, so conventions and commands differ by directory.

## Project map

- `user/`: consumer storefront, Vue 3 + Vite + Element Plus + Vuex. Pages and API wrappers are under `src/`; Vite proxies `/dev-api` and WebSocket traffic to the backend on port `9091`.
- `admin/`: merchant/administrator console, Vue 3 + Vite + Element Plus + Pinia. Its development proxy also targets port `9091`.
- `service/`: Spring Boot 2.5 multi-module backend. `Neuedu-admin` is the application and controller entry point; `Neuedu-sports` contains the shopping domain, with shared framework/system code in the other modules.
- `sports.sql`: database schema and seed/import data. It does not contain uploaded image files.

Follow the existing API wrapper, router, permission, component, and backend controller/service/mapper patterns in the module being changed. When changing an endpoint, check both frontends' callers and preserve the response shape and authentication expectations. Public product browsing and authenticated cart/order/account actions have different access requirements; verify the intended boundary instead of broadly disabling authorization.

## Run and validate

Run frontend commands from the relevant frontend directory:

```sh
cd user && pnpm dev
cd user && pnpm build:prod
cd admin && pnpm dev
cd admin && pnpm build:prod
```

The consumer dev server uses port `18183`; the backend uses `9091`. Check each `vite.config.js` and `.env.*` before changing ports or API targets. For backend checks, run from `service/`:

```sh
mvn -pl Neuedu-admin -am test
mvn -pl Neuedu-admin -am package -DskipTests
```

Choose checks that cover the changed behavior, then run the relevant frontend build or backend test. If a check depends on MySQL, Redis, payment, QQ login, or an external AI service that is unavailable, report that limit rather than claiming the integration passed.

## Database and image troubleshooting

When database-backed product, avatar, or shop images appear missing, trace the full chain before changing data: the stored URL/path, the active `ruoyi.profile` value in the selected Spring configuration, the static-resource mapping, and whether the corresponding file exists under that storage root. A URL stored in MySQL is not the image itself, and importing `sports.sql` cannot restore uploaded files. Search existing upload directories and backups before concluding the files are gone.

Treat `sports.sql` and local database contents as potentially valuable user data. Inspect migration/import scripts and confirm the target database before running them. Do not drop, truncate, reset, or re-import over an existing database unless the user explicitly asks for that operation.

## Repository workflow

Before editing, inspect `git status` and preserve unrelated user changes. After each completed change, commit it with a concise Chinese commit message, as requested for this project. Push only when the user asks to upload or publish the changes.
