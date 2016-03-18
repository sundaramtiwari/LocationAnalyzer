package com.cleartrip.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcFactory {

    private static final String driverName = "oracle.jdbc.OracleDriver"; // ctb@//ctoradb.cleartrip.com:1521/cleardb
    
    public static Connection getConnection(String url, String username, String password) {
        Connection con = null;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

}
