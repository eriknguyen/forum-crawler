package DB;

import Entities.Row;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Khanh Nguyen on 1/13/2016.
 */
public class DBManager {

    public final String dbPrefix = "jdbc:mysql://";

    //Local WampServer in default port
    public final String dbHost = "localhost/";
    public final String dbName = "crawler";
    public final String dbUser = "root";
    public final String dbPassword = "";

    public Connection conn = null;
    public String dbUrl = dbPrefix + dbHost + dbName;

    public DBManager() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/crawler");
            System.out.println("Connection built");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayResult(String query) throws SQLException {
        ResultSet rs = dbExecuteQuery(query);
        while (rs.next()) {
            StringBuffer bf = new StringBuffer();
            bf.append(rs.getInt("RecordID") + ": ");
            bf.append(rs.getString("URL"));
            System.out.println(bf.toString());
        }
    }

    public void displayAllRows(String tableName) throws SQLException {
        List<Row> table = new ArrayList<Row>();
        String query = "SELECT * FROM " + tableName;
        ResultSet rs = dbExecuteQuery(query);
        System.out.println("Result from " + tableName);

        Row.formTable(rs, table);
        for (Row row : table) {
            for (Map.Entry<Object, Class> col : row.row) {
                System.out.print(" > " + ((col.getValue()).cast(col.getKey())));
            }
            System.out.println();
        }
    }

    public void getRow(int id) {
        String query;
        query = "SELECT * FROM hwzSubForum WHERE adminId = ?";
    }


    public ResultSet dbExecuteQuery(String sql) throws SQLException {
        Statement sta = conn.createStatement();
        return sta.executeQuery(sql);
    }

    public boolean dbExecute(String sql) throws SQLException {
        Statement sta = conn.createStatement();
        return sta.execute(sql);
    }

    public int dbExecuteUpdate(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sql);
    }

    @Override
    protected void finalize() throws Throwable {
        if (conn != null || !conn.isClosed()) {
            conn.close();
        }
    }

}
