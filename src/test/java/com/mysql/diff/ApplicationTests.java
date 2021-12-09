package com.mysql.diff;

import com.mysql.diff.pojo.Config;
import com.mysql.diff.pojo.SchemaSync;
import com.mysql.diff.pojo.TableAlterData;
import com.mysql.diff.service.DbService;
import com.mysql.diff.service.SyncDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
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

        List<TableAlterData> list = new ArrayList<>();
        for (String tableName : tableNames) {
            //差异对比
            TableAlterData sd = dbService.getAlterDataByTable(tableName);
            list.add(sd);
        }

        //排序,先执行创建,再执行修改,最后删除删除
        list.stream().sorted(Comparator.comparing(TableAlterData::getType).reversed())
                .forEach(tableAlterData -> {
                    Stream.of(tableAlterData.getSQL()).filter(Objects::nonNull)
                            .forEach(sql -> {
                                System.out.println("\n--------------");
                                //s.forEach(System.out::println);

                                sql.forEach(sqll -> {
                                    System.out.println(sqll);

                                    //是否执行同步表结构sql
                                    if (Config.Sync) {
                                        dbService.syncSQL2Dest(tableAlterData.getTable(), sqll);
                                    }
                                });
                            });

                    syncDataService.syncData(tableAlterData.getTable());
                });
    }
}
