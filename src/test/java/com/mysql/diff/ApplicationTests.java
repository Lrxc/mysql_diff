package com.mysql.diff;

import com.mysql.diff.pojo.Config;
import com.mysql.diff.pojo.SchemaSync;
import com.mysql.diff.pojo.TableAlterData;
import com.mysql.diff.service.DbService;
import com.mysql.diff.service.SyncDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SpringBootTest
class ApplicationTests {
    @Autowired
    private DbService dbService;
    @Autowired
    private SyncDataService syncDataService;

    @Test
    void tt1() {
        SchemaSync schemaSync = dbService.getDbInfo();
        //获取所有的表名
        List<String> tableNames = dbService.getTableNames(schemaSync);

        for (String tableName : tableNames) {
            //差异对比
            TableAlterData sd = dbService.getAlterDataByTable(schemaSync, tableName);

            Stream.of(sd.getSQL()).filter(Objects::nonNull)
                    .forEach(s -> {
                        System.out.println("\n--------------");
                        //s.forEach(System.out::println);

                        s.forEach(s1 -> {
                            System.out.println(s);

                            //是否执行同步表结构sql
                            if (Config.Sync) {
                                dbService.syncSQL4Dest(tableName, s1);
                            }
                        });
                    });

            syncDataService.syncData(tableName);
        }
    }
}
