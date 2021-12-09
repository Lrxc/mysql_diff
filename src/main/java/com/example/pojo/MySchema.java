package com.example.pojo;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class MySchema {

    private String SchemaRaw;
    private LinkedHashMap<String, String> Fields;
    private LinkedHashMap<String, DbIndex> IndexAll;
    private LinkedHashMap<String, DbIndex> ForeignAll;
}
