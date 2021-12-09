package com.example.service;

import cn.hutool.json.JSONUtil;
import com.example.constant.KeyEnum;
import com.example.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DbService {

    @Autowired// 默认数据源
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("secondJdbcTemplate")//不指定则使用默认数据源
    private JdbcTemplate secondJdbcTemplate;

    /**
     * 获取所有的表信息
     */
    public SchemaSync getDbInfo() {
        System.out.println(("----- getDbInfo ------"));

        List<Map<String, Object>> source = jdbcTemplate.queryForList("show table status");
        List<DbInfo> sourceDb = JSONUtil.toList(JSONUtil.toJsonStr(source), DbInfo.class);

        List<Map<String, Object>> desc = secondJdbcTemplate.queryForList("show table status");
        List<DbInfo> destDb = JSONUtil.toList(JSONUtil.toJsonStr(desc), DbInfo.class);

        SchemaSync schemaSync = new SchemaSync();
        schemaSync.setSourceDb(sourceDb);
        schemaSync.setDestDb(destDb);

        return schemaSync;
    }

    public List<String> getTableNames(SchemaSync schemaSync) {
        System.out.println(("----- getTableNames ------"));

        Set<String> sourceTableNames = schemaSync.getSourceDb().stream().map(DbInfo::getName).collect(Collectors.toSet());
        Set<String> destTableNames = schemaSync.getDestDb().stream().map(DbInfo::getName).collect(Collectors.toSet());

        sourceTableNames.addAll(destTableNames);
        return new ArrayList<>(sourceTableNames);
    }

    public TableAlterData getAlterDataByTable(SchemaSync schemaSync, String table) {

        String sSchema = "";
        try {
            Map<String, Object> map = jdbcTemplate.queryForMap("show create table " + table);
            sSchema = map.get("Create Table").toString();
        } catch (DataAccessException ignored) {
        }
        String dSchema = "";
        try {
            Map<String, Object> map = secondJdbcTemplate.queryForMap("show create table " + table);
            dSchema = map.get("Create Table").toString();
        } catch (DataAccessException ignored) {
        }

        SchemaDiff schemaDiff = new SchemaDiff();
        schemaDiff.setTable(table);
        schemaDiff.setSource(parseSchema(sSchema));
        schemaDiff.setDest(parseSchema(dSchema));

        TableAlterData alter = new TableAlterData();
        alter.setTable(table);
        alter.setType("iota");
        alter.setSchemaDiff(schemaDiff);


        if (dSchema.equals(sSchema)) {
            return alter;
        }

        if ("".equals(sSchema)) {
            alter.setType("alterTypeDrop");
            alter.setSQL(alter.getSQL().stream().map(sql -> sql + "drop table " + table).collect(Collectors.toList()));
            return alter;
        }

        if ("".equals(dSchema)) {
            alter.setType("alterTypeCreate");
            alter.setSQL(List.of(sSchema));
            return alter;
        }

        List<String> diff = getSchemaDiff(alter);
        if (diff.size() == 0) {
            return alter;
        }

        alter.setType("alterTypeAlter");

        if (Config.SingleSchemaChange) {
            alter.setSQL(diff.stream().map(s -> "ALTER TABLE `" + table + "`\n" + s + ";").collect(Collectors.toList()));
        } else {
            alter.setSQL(List.of("ALTER TABLE `" + table + "`\n" + String.join(",\n", diff) + ";"));
        }
        return alter;
    }

    private List<String> getSchemaDiff(TableAlterData alter) {
        MySchema source = alter.getSchemaDiff().getSource();
        MySchema dest = alter.getSchemaDiff().getDest();
        String table = alter.getTable();

        List<String> alterLines = new ArrayList<>();

        //增加缺少的字段
        //source.getFields().forEach((key, value) -> {
        //    String s = dest.getFields().get(key);
        //    if (s == null) {
        //        String alterSQL = "ADD " + value;
        //        alterLines.add(alterSQL);
        //    }
        //});

        AtomicReference<String> beforeFieldName = new AtomicReference<>();
        source.getFields().forEach((key, value) -> {
            //自己的如果没有,则需要新增
            if (!dest.getFields().containsKey(key)) {
                if (beforeFieldName.get() == null) {
                    String alterSQL = "ADD " + value + " FIRST";
                    alterLines.add(alterSQL);
                } else {
                    String alterSQL = "ADD " + value + " AFTER " + beforeFieldName;
                    alterLines.add(alterSQL);
                }
            }
            beforeFieldName.set(key);
        });

        //删除多的字段
        dest.getFields().forEach((key, value) -> {
            String s = source.getFields().get(key);
            if (s == null) {
                String alterSQL = "DROP " + key;
                alterLines.add(alterSQL);
            }
        });

        //增加缺少的索引
        source.getIndexAll().forEach((indexName, idx) -> {
            List<String> alterSQLs = new ArrayList<>();

            Map<String, DbIndex> dIdx = dest.getIndexAll();
            //是否包含该索引
            if (dIdx.containsKey(indexName)) {
                //索引不一样
                if (!dIdx.get(indexName).getSQL().equals(idx.getSQL())) {
                    alterSQLs.addAll(alterAddSQL(idx, true));
                }
            } else {
                alterSQLs.addAll(alterAddSQL(idx, false));
            }

            if (alterSQLs.size() > 0) {
                alterLines.addAll(alterSQLs);
                //System.out.println("index.alter" + table + "." + indexName + ", alterSQL=" + alterSQLs);
            } else {
                //System.out.println("index.alter" + table + "." + indexName + "not change");
            }
        });

        //删除多余的索引
        if (Config.Drop) {
            dest.getIndexAll().forEach((indexName, dIdx) -> {
                String dropSQL = null;
                //是否包含该索引
                if (!source.getIndexAll().containsKey(indexName)) {
                    dropSQL = alterDropSQL(dIdx);
                }

                if (dropSQL != null) {
                    alterLines.add(dropSQL);
                    //System.out.println("index.alter" + table + "." + indexName + ", alterSQL=" + dropSQL);
                } else {
                    //System.out.println("index.alter" + table + "." + indexName + "not change");
                }
            });
        }

        // 增加缺少的外键
        source.getForeignAll().forEach((foreignName, idx) -> {
            List<String> alterSQLs = new ArrayList<>();

            Map<String, DbIndex> dIdx = dest.getForeignAll();
            //是否包含该索引
            if (dIdx.containsKey(foreignName)) {
                //索引不一样
                if (!dIdx.get(foreignName).getSQL().equals(idx.getSQL())) {
                    alterSQLs.addAll(alterAddSQL(idx, true));
                }
            } else {
                alterSQLs.addAll(alterAddSQL(idx, false));
            }

            if (alterSQLs.size() > 0) {
                alterLines.addAll(alterSQLs);
                //System.out.println("index.alter" + table + "." + indexName + ", alterSQL=" + alterSQLs);
            } else {
                //System.out.println("index.alter" + table + "." + indexName + "not change");
            }
        });

        //删除多余的外键
        if (Config.Drop) {
            dest.getForeignAll().forEach((foreignName, dIdx) -> {
                String dropSQL = null;
                //是否包含该索引
                if (!source.getForeignAll().containsKey(foreignName)) {
                    dropSQL = alterDropSQL(dIdx);
                }

                if (dropSQL != null) {
                    alterLines.add(dropSQL);
                    //System.out.println("index.alter" + table + "." + indexName + ", alterSQL=" + dropSQL);
                } else {
                    //System.out.println("index.alter" + table + "." + indexName + "not change");
                }
            });
        }

        return alterLines;
    }

    private String alterDropSQL(DbIndex idx) {
        switch (idx.getIndexType()) {
            case KeyEnum.indexTypePrimary:
                return "DROP PRIMARY KEY";
            case KeyEnum.indexTypeIndex:
                return "DROP INDEX `" + idx.getName() + "`";
            case KeyEnum.indexTypeForeignKey:
                return "DROP FOREIGN KEY `" + idx.getName() + "`";
            default:
                System.out.println("unknown indexType" + idx.getIndexType());
        }
        return "";
    }

    private List<String> alterAddSQL(DbIndex idx, boolean b) {
        List<String> alterSQL = new ArrayList<>();

        if (b) {
            String dropSQL = alterDropSQL(idx);
            if (!"".equals(dropSQL)) {
                alterSQL.add(dropSQL);
            }
        }

        switch (idx.getIndexType()) {
            case KeyEnum.indexTypePrimary:
            case KeyEnum.indexTypeIndex:
            case KeyEnum.indexTypeForeignKey:
                alterSQL.add("ADD " + idx.getSQL());
                break;
            default:
                System.out.println("unknown indexType" + idx.getIndexType());
        }
        return alterSQL;
    }

    private MySchema parseSchema(String sourceSql) {
        MySchema mySchema = new MySchema();
        mySchema.setFields(new LinkedHashMap<>());
        mySchema.setSchemaRaw("");
        mySchema.setIndexAll(new LinkedHashMap<>());
        mySchema.setForeignAll(new LinkedHashMap<>());

        for (String sql : sourceSql.split("\n")) {
            String line = sql.trim().replace(",", "");

            if (line.startsWith("`")) {
                int index = line.indexOf("`", 2);

                String name = line.substring(1, index);
//                System.out.println(name);

                //mySchema.setFields(Map.of(name, line));
                mySchema.getFields().put(name, line);
            } else {
                DbIndex idx = parseDbIndexLine(line);

                if (idx == null) {
                    continue;
                }

                switch (idx.getIndexType()) {
                    case KeyEnum.indexTypeForeignKey:
                        mySchema.getForeignAll().put(idx.getName(), idx);
                        break;
                    default:
                        mySchema.getIndexAll().put(idx.getName(), idx);
                }
            }
        }

        return mySchema;
    }

    private DbIndex parseDbIndexLine(String line) {
        DbIndex dbIndex = new DbIndex();
        dbIndex.setSQL(line);

        if (line.startsWith(KeyEnum.indexTypePrimary)) {
            dbIndex.setName("PRIMARY KEY");
            dbIndex.setIndexType(KeyEnum.indexTypePrimary);
            return dbIndex;
        }

        if (line.matches(KeyEnum.indexReg)) {
            String[] split = line.split("`");
            dbIndex.setName(split[1]);
            dbIndex.setIndexType(KeyEnum.indexTypeIndex);
            return dbIndex;
        }

        if (line.matches(KeyEnum.foreignKeyReg)) {
            String[] split = line.split("`");
            dbIndex.setName(split[1]);
            dbIndex.setIndexType(KeyEnum.indexTypeForeignKey);
            dbIndex.setRelationTables(List.of(split[1]));
            return dbIndex;
        }
        return null;
    }

    /**
     * 执行sql
     *
     * @param tableName tableName
     * @param sql       sql
     */
    public void syncSQL4Dest(String tableName, String sql) {
        try {
            System.out.println("sql sync " + tableName);
            secondJdbcTemplate.execute(sql);
            System.out.println("sql sync success");
        } catch (DataAccessException e) {
            System.out.println("sql sync error! " + e.getMessage());
        }
    }
}
