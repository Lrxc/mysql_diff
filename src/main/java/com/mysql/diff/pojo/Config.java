package com.mysql.diff.pojo;

public interface Config {

    // SourceDSN 同步的源头
    String SourceDSN = "source";

    // DestDSN 将被同步
    String DestDSN = "dest";

    // AlterIgnore 忽略配置， eg:   "tb1*":{"column":["aaa","a*"],"index":["aa"],"foreign":[]}
    String AlterIgnore = "alter_ignore";

    // Tables 同步表的白名单，若为空，则同步全库
    String Tables = "tables";

    // TablesIGNORE 不同步的表
    String TablesIGNORE = "tables_ignore";

    // Email 完成同步后发送同步信息的邮件账号信息
    String Email = "email";
    String ConfigPath = "";

    // Sync 是否真正的执行同步操作
    boolean Sync = true;

    // Drop 若目标数据库表比源头多了字段、索引，是否删除
    boolean Drop = true;

    // SingleSchemaChange 生成sql ddl语言每条命令只会进行单个修改操作
    boolean SingleSchemaChange = false;
}
