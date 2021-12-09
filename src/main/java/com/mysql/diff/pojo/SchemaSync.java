package com.mysql.diff.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SchemaSync {

    private Config config;
    private List<DbInfo> sourceDb;
    private List<DbInfo> destDb;
}
