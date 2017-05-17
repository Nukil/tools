package hbase;

import constant.Constant;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Nukil on 2017/4/26
 */
public class HBaseUtil {
    private static Configuration hConfiguration = null;
    private static Logger logger = LoggerFactory.getLogger(HBaseUtil.class);
    private static Connection hConnection = null;

    private HBaseUtil(){}
    private static HBaseUtil instance = null;
    public static HBaseUtil getInstance() {
        if (null == instance) {
            synchronized (HBaseUtil.class) {
                if (null == instance) {
                    instance = new HBaseUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化HBase配置
     */
    public static boolean init() {
        hConfiguration = HBaseConfiguration.create();
        hConfiguration.addResource(new Path("hbase-site.xml"));
        hConfiguration.addResource(new Path("core-site.xml"));
        if (null != hConfiguration) {
            logger.info("HBase初始化成功!");
            return true;
        }
        return false;
    }

    /**
     * 获取hbase连接
     * @return hConnection
     */
    private static Connection getConn() {
        try {
            if (null == hConnection || hConnection.isClosed()) {
                hConnection = ConnectionFactory.createConnection(hConfiguration);
            }
        } catch(IOException e) {
            logger.error("fail to get HBase connection" + e.toString(), e);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException x) {
                logger.error(x.getMessage(), x);
            }
            hConnection = getConn();
        }
        return hConnection;
    }

    /**
     * 创建HBase表
     * @param tableName 表名
     * @param columnFamilies 列族
     * @param splitKeys
     * @return 成功返回true
     */
    public static boolean createTable(String tableName, String[] columnFamilies, byte[][] splitKeys) {
        Admin admin = null;
        try {
            Connection conn = getConn();
            admin = conn.getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                logger.warn("此表已存在 : " + tableName);
                return true;
            } else {
                HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName));
                for (String columnFamily : columnFamilies) {
                    tableDesc.addFamily(new HColumnDescriptor(columnFamily));
                }
                if (null != splitKeys) {
                    admin.createTable(tableDesc, splitKeys);
                } else {
                    admin.createTable(tableDesc);
                }
                logger.info(String.format("%s 表创建成功", tableName));
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (null != admin) {
                    admin.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 表是否存在
     * @param tableName 表名
     * @return 存在返回true
     */
    public static boolean tableExists(String tableName) {
        Admin admin = null;
        try {
            Connection conn = getConn();
            admin = conn.getAdmin();
            return admin.tableExists(TableName.valueOf(tableName));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (null != admin) {
                    admin.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     *
     * @param num
     * @return
     */
    public static byte[][] getSplitKeys(int num) {
        byte[][] splitKeys = new byte[num - 1][];
        int span = 1;
        int splitKey = 0;
        String format = "%0" + Constant.DIGIT + "d";
        for (int i = 0; i < num; ++i) {
            splitKey = splitKey + span;
            splitKeys[i - 1] = Bytes.toBytes(String.format(format, splitKey));
        }
        return splitKeys;
    }

    /**
     *
     * @param tableName
     * @param columnFamilys
     */
    public static boolean createOrCoverTable(String tableName, String[] columnFamilys) {
        Admin admin = null;
        try {
            Connection conn = getConn();
            admin = conn.getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                logger.info("此表已存在 : " + tableName);
                deleteTable(tableName);
            }
            HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tableName));
            for (String columnFamily : columnFamilys) {
                tableDesc.addFamily(new HColumnDescriptor(columnFamily));
            }
            admin.createTable(tableDesc);
            logger.info(String.format("创建 %s 表成功!", tableName));
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (null != admin) {
                    admin.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 删除一张Hbase表
     * @param tableName 表名
     * @return 成功返回true
     */
    public static boolean deleteTable(String tableName) {
        Admin admin = null;
        try {
            Connection conn = getConn();
            admin = conn.getAdmin();
            TableName name = TableName.valueOf(tableName);
            if (admin.tableExists(name)) {
                admin.disableTable(name);
                admin.deleteTable(name);
                logger.info("删除表成功 : " + tableName);
            } else {
                logger.warn("表不存在 : " + tableName);
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (null != admin) {
                    admin.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 插入一条数据
     * @param tableName 表名
     * @param rowKey
     * @param family
     * @param map
     * @return 成功返回true
     */
    public static boolean insertTable(String tableName, String rowKey, String family, Map<String, byte[]> map) {
        Table table = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Put put = new Put(rowKey.getBytes());
            for (Entry<String, byte[]> entry : map.entrySet()) {
                put.addColumn(family.getBytes(), entry.getKey().getBytes(), entry.getValue());
            }
            table.put(put);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != table) {
                try {
                    table.close();
                } catch(IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 批量插入数据
     * @param put
     * @param tableName
     * @return
     */
    public static boolean insertTable(Put put, String tableName) {
        Table table = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            table.put(put);
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != table) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 批量插入数据
     * @param put
     * @param tableName
     * @return
     */
    public static boolean insertTable(List<Put> put, String tableName) {
        Table table = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            table.put(put);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (null != table) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 根据rowkey查询一条数据
     * @param tableName 表名
     * @param rowKey
     * @return result
     */
    public static Result getOneRow(String tableName, String rowKey) {
        Table table = null;
        Result result = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            result = table.get(get);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
        return result;
    }

    /**
     * 查询指定rowKey的某几个列
     * @param tableName
     * @param rowKey
     * @param cols 键为列族名称,值为列族下多个单元格的名称
     * @return
     */
    public static Result getOneRowAndMultiColumn(String tableName, String rowKey, Map<String, String[]> cols) {
        Table table = null;
        Result result = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Get get = new Get(rowKey.getBytes());
            for (Entry<String, String[]> entry : cols.entrySet()) {
                for (int i = 0; i < entry.getValue().length; i++) {
                    get.addColumn(entry.getKey().getBytes(), entry.getValue()[i].getBytes());
                }
            }
            get.setCheckExistenceOnly(true);
            result = table.get(get);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * 根据rowKey前缀查询数据
     * @param tableName 表名
     * @param rowKeyLike rowKey前缀字符串
     * @return list结果集
     */
    public static List<Result> getRows(String tableName, String rowKeyLike) {
        Table table = null;
        List<Result> list = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     * 根据rowKey前缀查询指定的列
     * @param tableName 表名
     * @param rowKeyLike rowKey前缀
     * @param cols 键为列族名称,值为列族下多个单元格的名称
     * @return list结果集
     */
    public static List<Result> getRows(String tableName, String rowKeyLike, Map<String, String[]> cols) {
        Table table = null;
        List<Result> list = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            for (Entry<String, String[]> entry : cols.entrySet()) {
                for (int i = 0; i < entry.getValue().length; i++) {
                    scan.addColumn(entry.getKey().getBytes(), entry.getValue()[i].getBytes());
                }
            }
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     * 范围查询
     * @param tableName 表名
     * @param startRow 开始row
     * @param stopRow 结束row
     * @return list结果集
     */
    public static List<Result> getRows(String tableName, String startRow, String stopRow) {
        Table table = null;
        List<Result> list = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(stopRow.getBytes());
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rsResult : scanner) {
                list.add(rsResult);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     * 范围查询
     * @param tableName 表名
     * @param startRow 开始row
     * @param stopRow 结束row
     * @return list结果集
     */
    public static List<Result> getRows(String tableName, String startRow, String stopRow,Map<String, String[]> cols,Filter filter) {
        Table table = null;
        List<Result> list = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(stopRow.getBytes());
            for (Entry<String, String[]> entry : cols.entrySet()) {
                for (int i = 0; i < entry.getValue().length; i++) {
                    scan.addColumn(entry.getKey().getBytes(), entry.getValue()[i].getBytes());
                }
            }
            if(filter != null && filter.hasFilterRow()){
                scan.setFilter(filter);
            }
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<>();
            for (Result rsResult : scanner) {
                list.add(rsResult);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     *
     * @param tableName
     * @param family
     * @param qualifier
     * @return
     */
    public static List<Result> scanForQualifier(String tableName, String family, String qualifier) {
        Table table = null;
        List<Result> list = new ArrayList<>();
        try{
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.addColumn(family.getBytes(), qualifier.getBytes());
            ResultScanner scanner = table.getScanner(scan);
            for (Result rsResult : scanner) {
                list.add(rsResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据前缀删除记录
     * @param tableName 表名
     * @param rowKeyLike rowKey前缀
     */
    public static boolean deleteRecords(String tableName, String rowKeyLike) {
        Table table = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            List<Delete> list = new ArrayList<Delete>();
            for (Result rs : scanner) {
                Delete del = new Delete(rs.getRow());
                list.add(del);
            }
            table.delete(list);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 删除指定名称的列族
     * @param tableName 表名
     * @param family
     * @return 成功返回true
     */
    public static boolean deleteFamily(String tableName, String family) {
        Admin admin = null;
        try {
            Connection conn = getConn();
            admin = conn.getAdmin();
            admin.deleteColumn(TableName.valueOf(tableName), family.getBytes());
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (admin != null) {
                    admin.close();// 关闭释放资源
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     *
     * @param deletes
     * @param tableName
     * @return
     */
    public static boolean delete(List<Delete> deletes, String tableName) {
        Table table = null;
        try {
            Connection conn = getConn();
            table = conn.getTable(TableName.valueOf(tableName));
            table.delete(deletes);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     *
     */
    public static void close() {
        if (hConnection != null) {
            try {
                hConnection.close();
            } catch (IOException e) {
                logger.error("close hbase connection failed! msg is : " + e.getMessage(), e);
            }
        }
    }

    /**
     * 获得相等过滤器。相当于SQL的 [字段] = [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter eqFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.EQUAL, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 获得大于过滤器。相当于SQL的 [字段] > [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter gtFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.GREATER, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 获得大于等于过滤器。相当于SQL的 [字段] >= [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter gteqFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.GREATER_OR_EQUAL, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 获得小于过滤器。相当于SQL的 [字段] < [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter ltFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.LESS, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 获得小于等于过滤器。相当于SQL的 [字段] <= [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter lteqFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.LESS_OR_EQUAL, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 获得不等于过滤器。相当于SQL的 [字段] != [值]
     * @param cf 列族名
     * @param col 列名
     * @param val 值
     * @return 过滤器
     */
    public static Filter neqFilter(String cf, String col, byte[] val) {
        SingleColumnValueFilter f = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.NOT_EQUAL, val);
        f.setLatestVersionOnly(true);
        f.setFilterIfMissing(true);
        return f;
    }

    /**
     * 和过滤器 相当于SQL的 的 and
     * @param filters 多个过滤器
     * @return 过滤器
     */
    public static Filter andFilter(Filter... filters) {
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        if(filters!=null && filters.length > 0) {
            if(filters.length > 1) {
                for (Filter f : filters) {
                    filterList.addFilter(f);
                }
            }
            if(filters.length == 1) {
                return filters[0];
            }
        }
        return filterList;
    }

    /**
     * 和过滤器 相当于SQL的 的 and
     * @param filters 多个过滤器
     * @return 过滤器
     */
    public static Filter andFilter(Collection<Filter> filters) {
        return andFilter(filters.toArray(new Filter[0]));
    }



    /**
     * 或过滤器 相当于SQL的 or
     * @param filters 多个过滤器
     * @return 过滤器
     */
    public static Filter orFilter(Filter... filters) {
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        if(filters!=null && filters.length > 0) {
            for(Filter f : filters) {
                filterList.addFilter(f);
            }
        }
        return filterList;
    }

    /**
     * 或过滤器 相当于SQL的 or
     * @param filters 多个过滤器
     * @return 过滤器
     */
    public static Filter orFilter(Collection<Filter> filters) {
        return orFilter(filters.toArray(new Filter[0]));
    }

    /**
     * 非空过滤器 相当于SQL的 is not null
     * @param cf 列族
     * @param col 列
     * @return 过滤器
     */
    public static Filter notNullFilter(String cf,String col) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(cf.getBytes(),col.getBytes(), CompareFilter.CompareOp.NOT_EQUAL,new NullComparator());
        filter.setFilterIfMissing(true);
        filter.setLatestVersionOnly(true);
        return filter;
    }

    /**
     * 空过滤器 相当于SQL的 is null
     * @param cf 列族
     * @param col 列
     * @return 过滤器
     */
    public static Filter nullFilter(String cf,String col) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(cf.getBytes(),col.getBytes(), CompareFilter.CompareOp.EQUAL,new NullComparator());
        filter.setFilterIfMissing(false);
        filter.setLatestVersionOnly(true);
        return filter;
    }

    /**
     * 子字符串过滤器 相当于SQL的 like '%[val]%'
     * @param cf 列族
     * @param col 列
     * @param sub 子字符串
     * @return 过滤器
     */
    public static Filter subStringFilter(String cf, String col, String sub) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.EQUAL, new SubstringComparator(sub));
        filter.setFilterIfMissing(true);
        filter.setLatestVersionOnly(true);
        return filter;
    }

    /**
     * 正则过滤器 相当于SQL的 rlike '[regex]'
     * @param cf 列族
     * @param col 列
     * @param regex 正则表达式
     * @return 过滤器
     */
    public static Filter regexFilter(String cf, String col , String regex) {
        SingleColumnValueFilter filter = new SingleColumnValueFilter(cf.getBytes(), col.getBytes(), CompareFilter.CompareOp.EQUAL, new RegexStringComparator(regex));
        filter.setFilterIfMissing(true);
        filter.setLatestVersionOnly(true);
        return filter;
    }
}
