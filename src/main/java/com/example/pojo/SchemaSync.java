package com.example.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SchemaSync {

    private String config;
    private List<DbInfo> sourceDb;
    private List<DbInfo> destDb;
}
