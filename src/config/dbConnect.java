package config;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class dbConnect {

    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:ffos.db");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    
    public int addRecordAndGetId(String sql, Object... params) {
        int generatedKey = 0;
        try (Connection conn = this.connectDB();
            
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            
            for (int i = 0; i < params.length; i++) {
                
                pstmt.setObject(i + 1, params[i]); 
            }

            
            pstmt.executeUpdate();

          
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    
                    generatedKey = rs.getInt(1); 
                }
            }
            System.out.println("Record added successfully and ID retrieved!");

        } catch (SQLException e) {
            System.out.println("Error adding record (with ID retrieval): " + e.getMessage());
            generatedKey = 0; 
        }
        return generatedKey;
    }

    public void addRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]);
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]);
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]);
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            StringBuilder headerLine = new StringBuilder();
            headerLine.append("--------------------------------------------------------------------------------\n| ");
            for (String header : columnHeaders) {
                headerLine.append(String.format("%-20s | ", header));
            }
            headerLine.append("\n--------------------------------------------------------------------------------");

            System.out.println(headerLine.toString());

            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (String colName : columnNames) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-20s | ", value != null ? value : ""));
                }
                System.out.println(row.toString());
            }
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    public void updateRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]);
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]);
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]);
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    public java.util.List<java.util.Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }

        return records;
    }

   
    public void viewRecords(String Query, String[] headers, String[] columns, int cid) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(Query)) {

            pstmt.setInt(1, cid);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("--------------------------------------------------------------------------------");
            for (String header : headers) {
                System.out.printf("%-20s | ", header);
            }
            System.out.println("\n--------------------------------------------------------------------------------");

            while (rs.next()) {
                for (String col : columns) {
                    System.out.printf("%-20s | ", rs.getString(col));
                }
                System.out.println();
            }

            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error viewing records: " + e.getMessage());
        }
    }
    
   
     public void ViewRecords(String Query, String[] headers, String[] columns) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(Query);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("--------------------------------------------------------------------------------");
            for (String header : headers) {
                System.out.printf("%-20s | ", header);
            }
            System.out.println("\n--------------------------------------------------------------------------------");

            while (rs.next()) {
                for (String col : columns) {
                    System.out.printf("%-20s | ", rs.getString(col));
                }
                System.out.println();
            }

            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error viewing records: " + e.getMessage());
        }
    }


    // ✅ FIXED METHOD 2
    public Object getSingleValue(String sqlQuery, String columnName, int fid) {
        Object value = null;
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setInt(1, fid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                value = rs.getObject(columnName);
            }

        } catch (SQLException e) {
            System.out.println("Error getting single value: " + e.getMessage());
        }

        return value;
    }

    // ✅ ADDED: View Foods by Category
    public void viewByCategory(String category) {
        String sql = "SELECT f_id, f_name, f_price, f_category FROM tbl_food WHERE f_category = ?";
        String[] headers = {"Food ID", "Name", "Price", "Category"};
        String[] columns = {"f_id", "f_name", "f_price", "f_category"};

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--------------------------------------------------------------------------------");
            for (String h : headers) System.out.printf("%-20s | ", h);
            System.out.println("\n--------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                for (String col : columns) {
                    System.out.printf("%-20s | ", rs.getString(col));
                }
                System.out.println();
            }

            if (!found) {
                System.out.println("No foods found for category: " + category);
            }

            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error viewing category: " + e.getMessage());
        }
    }

    
    public String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
           
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
    
    
    public int getLastInsertId() {
        int id = -1;
        try {
            String sql = "SELECT last_insert_rowid() AS id"; 
            List<Map<String, Object>> result = fetchRecords(sql);
            if (!result.isEmpty()) {
                id = Integer.parseInt(result.get(0).get("id").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

}