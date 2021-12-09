package com.mysql.diff.pojo;

import lombok.Data;

@Data
public class SchemaDiff {

    private String Table;
    private MySchema Source;
    private MySchema Dest;
}
