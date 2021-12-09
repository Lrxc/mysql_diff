package com.example.constant;

public interface KeyEnum {


    String indexTypePrimary = "PRIMARY";
    String indexTypeIndex = "INDEX";
    String indexTypeForeignKey = "FOREIGN KEY";

    // 匹配索引字段
    //String indexReg = "^([A-Z]+\\s)?KEY\\s";
    String indexReg = "KEY `.*` \\(`.*`\\)";

    // 匹配外键
    //String foreignKeyReg = "(\"^CONSTRAINT `(.+)` FOREIGN KEY.+ REFERENCES `(.+)` \")";
    String foreignKeyReg = "CONSTRAINT `.*` FOREIGN KEY \\(`.*`\\) REFERENCES `.*` \\(`.*`\\)";
}
