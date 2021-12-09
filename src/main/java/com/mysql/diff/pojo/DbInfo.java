package com.mysql.diff.pojo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

@Data
public class DbInfo implements Serializable {

    @Alias("Comment")
    private String comment;
    @Alias("Data_free")
    private int dataFree;
    @Alias("Create_options")
    private String createOptions;
    @Alias("Collation")
    private String collation;
    @Alias("Create_time")
    private long createTime;
    @Alias("Name")
    private String name;
    @Alias("Avg_row_length")
    private int avgRowLength;
    @Alias("Row_format")
    private String rowFormat;
    @Alias("Temporary")
    private String temporary;
    @Alias("Version")
    private int version;
    @Alias("Max_data_length")
    private int maxDataLength;
    @Alias("Index_length")
    private int indexLength;
    @Alias("Max_index_length")
    private int maxIndexLength;
    @Alias("Auto_increment")
    private int autoIncrement;
    @Alias("Engine")
    private String engine;
    @Alias("Data_length")
    private int dataLength;
    @Alias("Rows")
    private int rows;
}
