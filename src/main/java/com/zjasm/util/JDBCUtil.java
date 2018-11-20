package com.zjasm.util;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;

public class JDBCUtil {
    public static void main(String[] args) {
        String content = readBlod();
        writeBlod(content);
    }

    public static void  writeBlod(String content) {
        Connection conn = getConnection();
        PreparedStatement pstmst = null;
        ByteArrayInputStream stream = null;
        try {
            String sql = "update regexregisteredservice set attribute_release=? where id=9 ";//?位置是blob类型的
            pstmst = conn.prepareStatement(sql);
            stream = new ByteArrayInputStream(content.getBytes());
            pstmst.setBinaryStream(1, stream, stream.available());
            pstmst.executeUpdate();
            pstmst.close();
            stream.close();
            conn.commit();
            conn.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String  readBlod(){
        String result = "";
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select attribute_release from regexregisteredservice where id=8");
            InputStream inStream = null;
            if (rs.next()) {
                inStream = rs.getBinaryStream("attribute_release");//config是blob类型的
            }

            ByteArrayInputStream msgContent = (ByteArrayInputStream) rs.getBinaryStream("attribute_release");
            byte[] byte_data = new byte[msgContent.available()];
            msgContent.read(byte_data, 0, byte_data.length);
            result = new String(byte_data,"utf-8");
            System.out.println(result);
            inStream.close();
            conn.commit();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://112.124.113.167:3306/zjdzywcs", "root", "zjasmssj@)!%");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

}

