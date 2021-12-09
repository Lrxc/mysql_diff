package com.mysql.diff.pojo;

import lombok.Data;

import java.util.List;

@Data
public class TableAlterData {
    private String Table;
    private String Type;
    private List<String> SQL;
    private SchemaDiff SchemaDiff;
}
