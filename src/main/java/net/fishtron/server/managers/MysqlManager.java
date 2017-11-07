package net.fishtron.server.managers;

import net.fishtron.utils.F;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/** Created by Tomáš Křen on 16.12.2016. */

public class MysqlManager implements Manager {

    @Override
    public String greetings() {
        return "MysqlManager at your service! Managing your " +dbName+"@"+host+":"+port+".";
    }

    private static final int port = 3306;
    private final String host;
    private final String dbName;
    private final String username;
    private final String password;

    public MysqlManager(JSONObject mysqlConfig) {
        this.host     = mysqlConfig.getString("host");
        this.dbName   = mysqlConfig.getString("db");
        this.username = mysqlConfig.getString("user");
        this.password = mysqlConfig.getString("password");
    }

    public JSONObject execute(String sql, boolean useObjectRows) {
        return execute(sql, rs -> resultToJson(rs, useObjectRows));
    }

    private JSONObject execute(String sql, Function<ResultSet,JSONObject> resultFun) {
        JSONObject ret;

        String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
        Connection connection = null;
        try {
            //F.log(sql);
            //F.log("Connecting database...");
            connection = DriverManager.getConnection(url, username, password);
            //F.log("Database connected!");
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);
            ret = resultFun.apply(resultSet);
            statement.close();
        } catch (SQLException e) {

            ret = F.obj(
                "status","error",
                "msg","Cannot connect to the database."
            );

        } finally {
            //F.log("Closing the connection.");
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignore) {}
            }
        }

        return ret;
    }

    private static JSONObject resultToJson(ResultSet rs, boolean useObjectRows) {

        JSONArray rows = new JSONArray();

        try {

            ResultSetMetaData metaData = rs.getMetaData();
            int numColumns = metaData.getColumnCount();

            //for (int i = 1; i <= numColumns; i++) {F.log(metaData.getColumnClassName(i)+": "+metaData.getColumnTypeName(i));}

            while (rs.next()) {
                if (useObjectRows) {
                    JSONObject row = new JSONObject();
                    for (int i = 1; i <= numColumns; i++) {
                        Object value = castResultValue(i, rs, metaData);
                        row.put(metaData.getColumnName(i), value);
                    }
                    rows.put(row);
                } else {
                    JSONArray row = new JSONArray();
                    for (int i = 1; i <= numColumns; i++) {
                        Object value = castResultValue(i, rs, metaData);
                        row.put(value);
                    }
                    rows.put(row);
                }
            }

            JSONObject ret = F.obj(
                "status","ok",
                "rows",rows
            );

            if (!useObjectRows) {
                JSONArray columnNames = new JSONArray();
                for (int i = 1; i <= numColumns; i++) {
                    String columnName = metaData.getColumnName(i);
                    columnNames.put(columnName);
                }
                ret.put("columns", columnNames);
            }

            return ret;

        } catch (SQLException e) {
            return F.obj(
                    "status","error",
                    "msg",e.getMessage()
            );
        }
    }

    private static Object castResultValue(int i, ResultSet rs, ResultSetMetaData metaData) throws SQLException {

        Object value = rs.getObject(i);
        if (rs.wasNull()) {
            return null;
        }

        switch (metaData.getColumnTypeName(i)) {
            case "INT":   return value;
            case "FLOAT": return rs.getDouble(i);
            default: return rs.getString(i);
        }
    }

    public static String toSqlTime(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
        return formatter.format(instant);
    }


    //todo remove
    /*
    public static void main(String[] args) {

        MysqlManager mysqlMan = new MysqlManager(BrickSim.loadMysqlConfig());

        JSONObject sqlResult = mysqlMan.execute("SELECT * FROM `ebay` ORDER BY id DESC LIMIT 2", false);

        Log.it(sqlResult.toString(2));

        Instant now = Instant.now();
        Log.it(now.toString());
        Log.it(toSqlTime(now));

    }*/


}
