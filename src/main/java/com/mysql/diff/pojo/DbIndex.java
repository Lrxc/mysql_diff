package com.mysql.diff.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DbIndex {
    private String IndexType;
    private String Name;
    private String SQL;

    // 相关联的表
    private List<String> RelationTables;
}
