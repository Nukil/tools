package jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;

/**
 * Created by Nukil on 2017/4/25
 */
public class JDBC {
    private Logger logger = Logger.getLogger(this.getClass());
    private String driverName;
    private String url;
    private String userName;
    private String password;
    private Connection conn = null;
    private PreparedStatement ps = null;
    private ResultSet res = null;

    public JDBC(String driverName, String url, String userName, String password) {
        this.driverName = driverName;
        this.url =url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 建立数据库连接
     * @return conn
     * @throws ClassNotFoundException 类不存在异常
     * @throws SQLException sql异常
     */
    private Connection getConn() throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        if (null != conn)
            conn = DriverManager.getConnection(url, userName, password);
        return conn;
    }

    /**
     * 关闭所有连接
     */
    private void closeAll() {
        try {
            if (null != conn) {
                conn.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != res) {
                res.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 更新数据库
     * @param sql sql语句
     * @param params 参数
     * @return 成功返回0
     */
    public int executeUpdate(String sql, Object[] params) {
        try {
            this.getConn();
            ps = conn.prepareStatement(sql);
            if (null != params && 0 != params.length) {
                for (int i = 0; i < params.length; ++i) {
                    ps.setObject(i+1, params[i]);
                }
            }
            return ps.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e.getMessage());
        } finally {
            this.closeAll();
        }
        return 0;
    }

    /**
     * 查询数据库
     * @param sql sql语句
     * @param params 参数
     * @return 结果集res
     */
    public ResultSet executeQuery(String sql, Object[] params) {
        try {
            this.getConn();
            ps = conn.prepareStatement(sql);
            if (null != params && 0 != params.length) {
                for (int i = 0; i < params.length; ++i) {
                    ps.setObject(i+1, params[i]);
                }
            }
            res = ps.executeQuery();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e.getMessage());
        }
        return res;
    }

    /**
     *
     * @param sql
     * @param params
     * @param cla
     * @return
     */
    public List executeQuery(String sql, Object[] params, Class cla) {
        List list = new ArrayList();
        try {
            ps = this.getConn().prepareStatement(sql);
            if (null != params && 0 != params.length) {
                for (int i = 0; i < params.length; ++i) {
                    ps.setObject(i+1, params[i]);
                }
            }
            res = ps.executeQuery();
            ResultSetMetaData resmt = res.getMetaData();

            while (res.next()) {
                Object obj = cla.newInstance();
                for (int i = 0; i < resmt.getColumnCount(); ++i) {
                    String columnName = resmt.getColumnName(i+1);
                    Field feild = cla.getDeclaredField(columnName);
                    String methodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method = cla.getDeclaredMethod(methodName, feild.getType());
                    method.invoke(obj, ConvertUtils.convert(res.getString(i+1), feild.getType()));
                }
                list.add(obj);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return list;
    }

    public int insertAndReturnId(String sql, Object[] params) {
        try {
            this.getConn();
            ps = conn.prepareStatement(sql);
            if (null != params && 0 != params.length) {
                for (int i = 0; i < params.length; ++i) {
                    ps.setObject(i+1, params[i]);
                }
            }
            ps.executeUpdate();

            ps = conn.prepareStatement("select last_insert_id()");
            ResultSet currentId =ps.executeQuery();
            while (currentId.next()) {
                return currentId.getInt(1);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            this.closeAll();
        }
        return 0;
    }
}
