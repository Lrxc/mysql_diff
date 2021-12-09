package com.mysql.diff.service;

import cn.hutool.json.JSONUtil;
import com.github.biyanwen.json2sql.Json2sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 数据同步
 */
@Service
public class SyncDataService {

    @Autowired
    private JdbcTemplate destJdbcTemplate;
    @Autowired
    @Qualifier("sourceJdbcTemplate")//不指定则使用默认数据源
    private JdbcTemplate sourceJdbcTemplate;


    public void syncData(String tableName) {

        List<Map<String, Object>> lists1 = destJdbcTemplate.queryForList("select * from " + tableName);
        List<Map<String, Object>> lists2 = sourceJdbcTemplate.queryForList("select * from " + tableName);

        String jsonStr = JSONUtil.toJsonStr(lists1);

        //根据json字符串生成sql文件
        String string = Json2sql.parse2String(jsonStr, tableName);
        System.out.println("\n\n\n");
        System.out.println(string);
    }
}
