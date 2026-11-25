# MyHub Server 开发约定

- 新增、修改或移动的项目文件必须纳入 Git 版本管理；完成改动时检查 `git status`，并将本次需要交付的文件加入 Git 暂存区（`git add`）。
- 后端包根为 `com.myhub`，按 `controller`、`service`、`mapper`、`entity`、`dto`、`config`、`common`、`util` 分层，具体约定见仓库外项目设计 `ARCHITECTURE.md`。
- 所有业务数据访问统一通过 MyBatis / MyBatis-Plus Mapper；Controller、Service 和配置类不得使用 `JdbcTemplate` 或直接执行 JDBC。
- Mapper 接口放在 `src/main/java/com/myhub/mapper/`，对应 SQL XML 放在 `src/main/resources/mapper/`，XML 的 `namespace` 必须与 Mapper 接口全限定名一致。
