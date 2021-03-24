package org.copydays.projects.user.sql;


import org.copydays.projects.user.domain.User;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DBConnectionManager {

    @Resource(name = "jdbc/UserPlateformDB")
    private DataSource dataSource;

    @Resource(name = "bean/EntityManager")
    private EntityManager entityManager;

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void releaseConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    public static final String DROP_USERS_TABLE_DDL_SQL = "DROP TABLE users";

    public static final String CREATE_USERS_TABLE_DDL_SQL = "CREATE TABLE users" +
            "(" +
            "    id          INT         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
            "    name        VARCHAR(16) NOT NULL," +
            "    password    VARCHAR(64) NOT NULL," +
            "    email       VARCHAR(64) NOT NULL," +
            "    phoneNumber VARCHAR(32) NOT NULL" +
            ")";

    public static final String INSERT_USER_DML_SQL = "INSERT INTO users(name, password, email, phoneNumber) VALUES" +
            "('A', '********', 'a@gmail.com', '1')" +
            "('B', '********', 'b@gmail.com', '1')" +
            "('C', '********', 'c@gmail.com', '1')" +
            "('D', '********', 'd@gmail.com', '1')" +
            "('E', '********', 'e@gmail.com', '1')";

    public static void main(String[] args) throws Exception {
        // 通过 ClassLoader 加载 java.sql.DriverManager -> static 模块 {}
        //DriverManager.setLogWriter(new PrintWriter(System.out));
        //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        //Driver driver = DriverManager.getDriver("jdbc:derby:/db/user-plateform;create=true");
        //Connection connection = driver.connect("jdbc:derby:/db/user-plateform;create=true", new Properties());

        String databaseURL = "jdbc:derby:/db/user-plateform;create=true";
        Connection connection = DriverManager.getConnection(databaseURL);

        Statement statement = connection.createStatement();
        // 删除 users 表
        System.out.println(statement.execute(DROP_USERS_TABLE_DDL_SQL));  // false
        // 创建 users 表
        System.out.println(statement.execute(CREATE_USERS_TABLE_DDL_SQL));  // false
        System.out.println(statement.executeUpdate(INSERT_USER_DML_SQL));  // 5

        // 执行查询语句（DML）
        ResultSet resultSet = statement.executeQuery("SELECT id, name, password, email, phoneNumber FROM users");

        // BeanInfo
        BeanInfo userBeanInfo = Introspector.getBeanInfo(User.class, Object.class);
        // 所有的 Properties 信息
        for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
            System.out.println(propertyDescriptor.getName() + "," + propertyDescriptor.getPropertyType());
        }

        // 写一个 ORM 框架
        while (resultSet.next()) {  // 如果存在并且游标滚动
            User user = new User();

            // 直接取出来所有结果值，进行数据的强赋值
            /*
            user.setId(resultSet.getLong("id"));
            user.setName(resultSet.getString("name"));
            user.setPassword(resultSet.getString("password"));
            user.setEmail(resultSet.getString("email"));
            user.setPhoneNumber(resultSet.getString("phoneNumber"));
            System.out.println(user);*/

            // ResultSetMetaData --> 数据表的元信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            System.out.println("当前表的名称：" + metaData.getTableName(1));
            System.out.println("当前表的列个数：" + metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println("列名称：" + metaData.getColumnLabel(i) + "，类型：" + metaData.getColumnClassName(i));
            }

            // 创建查询语句
            StringBuilder queryAllUserSQLBuilder = new StringBuilder("SELECT");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                queryAllUserSQLBuilder.append(" ").append(metaData.getColumnLabel(i)).append(",");
            }
            // 移除最后一个 “,”
            queryAllUserSQLBuilder.deleteCharAt(queryAllUserSQLBuilder.length() - 1);
            queryAllUserSQLBuilder.append("FROM ").append(metaData.getTableName(1));
            System.out.println(queryAllUserSQLBuilder);

            // User 类是通过配置文件，类名称
            // ClassLoader.loadClass  --> Class.newInstance()
            // ORM 映射核心思想：通过反射执行代码（性能开销大）
            for (PropertyDescriptor propertyDescriptor : userBeanInfo.getPropertyDescriptors()) {
                String fieldName = propertyDescriptor.getName();
                Class fieldType = propertyDescriptor.getPropertyType();
                String methodName = typeMethodMappings.get(fieldType);
                // 可能存在映射关系（不过此处是相等的）
                String columnLabel = mapColumnLabel(fieldName);
                Method resultSetMethod = ResultSet.class.getMethod(methodName, String.class);
                // 通过反射调用 getXXX(String) 方法
                Object resultValue = resultSetMethod.invoke(resultSet, columnLabel);
                // 获取 User 类 Setter 方法
                // propertyDescriptor ReadMethod 等于 Getter 方法
                // propertyDescriptor WriteMethod 等于 Setter 方法
                Method setterMethodFromUser = propertyDescriptor.getWriteMethod();
                // 以 id 为例， user.setId(resultSet.getLong("id"));
                setterMethodFromUser.invoke(user, resultValue);
            }

            System.out.println(user);
        }

        connection.close();
    }

    private static String mapColumnLabel(String fieldName) {
        return fieldName;
    }

    /**
     * 数据类型与 ResultSet 方法名映射
     */
    static Map<Class, String> typeMethodMappings = new HashMap<>();

    static {
        typeMethodMappings.put(Long.class, "getLong");
        typeMethodMappings.put(String.class, "getString");
    }
}