package Connection;


import Naver.NaverProductDetailClass;
import Util.Conf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MySQLConnector {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/commerce?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false&autoReconnect=true";
    static final String USER = Conf.DB_ID;
    static final String PASS = Conf.DB_PASSWORD;
    Connection conn;

    public MySQLConnector(){
        //this.conn = this.initConnect();
    }

    public Connection initConnect() {
        try {

            Class.forName(JDBC_DRIVER);
            System.out.println("데이터베이스 연결...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
        } catch(Exception e){
            System.out.println(e);
        }
        return conn;
    }

    public Connection initConnect(String db_url) {
        try {

            System.out.println("데이터베이스 연결...");
            conn = DriverManager.getConnection("jdbc:mysql://" + db_url + ":3306/commerce?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false&autoReconnect=true",USER,PASS);
        } catch(Exception e){
            System.out.println(e);
        }

        return conn;
    }


    public void closeConnect() {
        try {
            conn.close();

        } catch(Exception e){
            System.out.println(e);
        }
    }


    public void insertKeywordOnepage(Connection conn, String query, String keyword, int review_3m, double revenue_avg_3m,double adcost,double rev_ad, double std, double cv, double skewness, double kurtosis, double ctr,int expect_exposed, int expect_click, int expect_clickcost, double price_avg, double price_median, int revenue_sum, String seller_data, int seller_count, Double seller_inflow_tangent)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                preparedStmt.setString(1, keyword);
                preparedStmt.setDouble(2, review_3m);
                preparedStmt.setDouble(3, revenue_avg_3m);
                preparedStmt.setDouble(4, adcost);
                preparedStmt.setDouble(5, rev_ad);
                preparedStmt.setDouble(6, std);
                preparedStmt.setDouble(7, cv);
                preparedStmt.setDouble(8, skewness);
                preparedStmt.setDouble(9, kurtosis);
                preparedStmt.setDouble(10, ctr);
                preparedStmt.setInt(11, expect_exposed);
                preparedStmt.setInt(12, expect_click);
                preparedStmt.setInt(13, expect_clickcost);
                preparedStmt.setDouble(14, price_avg);
                preparedStmt.setDouble(15,price_median);
                preparedStmt.setInt(16,revenue_sum);
                preparedStmt.setString(17, seller_data);
                preparedStmt.setInt(18, seller_count);
                preparedStmt.setDouble(19, seller_inflow_tangent);
                // execute the preparedstatement
                preparedStmt.execute();

            }catch(Exception eee){
                // create the mysql insert preparedstatement
                String ud_query = "update keyword_1page set review_3m=?,revenue_avg_3m=?,adcost=?,revenue_adcost=?,std=?,cv=?,skewness=?,kurtosis=?, ctr=?,expect_exposed=?, expect_click=?, expect_clickcost=?,price_avg=?,price_median=?,revenue_sum=?, seller_data=?, seller_count=?, seller_inflow_tangent=? where keyword=?";
                PreparedStatement preparedStmt2 = conn.prepareStatement(ud_query);

                try {
                    preparedStmt2.setDouble(1, review_3m);
                    preparedStmt2.setDouble(2, revenue_avg_3m);
                    preparedStmt2.setDouble(3, adcost);
                    preparedStmt2.setDouble(4, rev_ad);
                    preparedStmt2.setDouble(5, std);
                    preparedStmt2.setDouble(6, cv);
                    preparedStmt2.setDouble(7, skewness);
                    preparedStmt2.setDouble(8, kurtosis);
                    preparedStmt2.setDouble(9, ctr);
                    preparedStmt2.setInt(10, expect_exposed);
                    preparedStmt2.setInt(11, expect_click);
                    preparedStmt2.setInt(12, expect_clickcost);
                    preparedStmt2.setDouble(13, price_avg);
                    preparedStmt2.setDouble(14,price_median);
                    preparedStmt2.setInt(15,revenue_sum);
                    preparedStmt2.setString(16, seller_data);
                    preparedStmt2.setInt(17, seller_count);
                    preparedStmt2.setDouble(18, seller_inflow_tangent);
                    preparedStmt2.setString(19, keyword);
                    // execute the java preparedstatement
                    preparedStmt2.executeUpdate();
                    System.out.println("업데이트 성공:" + keyword);

                } catch (Exception e22) {
                    System.err.println("예외 발생 - insertKeywordOnepage");
                    System.out.println(e22.getMessage());
                }
                preparedStmt2.close();


            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordOnepage");
            System.err.println(e.getMessage());
        }
    }

    public void insertCateogryId(Connection conn, String query, String big_cat, String middle_cat, String small_cat, String product_cat,String category_code)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, big_cat);
                preparedStmt.setString(2, middle_cat);
                preparedStmt.setString(3, small_cat);
                preparedStmt.setString(4, product_cat);
                preparedStmt.setString(5, category_code);
                // execute the preparedstatement
                preparedStmt.execute();

            }catch(Exception eee){
                System.out.println(eee.getMessage());

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertCateogryId");
            System.err.println(e.getMessage());
        }
    }
    public ArrayList<HashMap<String,String>> selectUserCard(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();

                Long user_card_id = rs.getLong("user_card_id");
                String user_id = rs.getString("user_id");
                String product_no = rs.getString("product_no");
                int is_open = rs.getInt("is_open");
                int is_save = rs.getInt("is_save");
                int is_store = rs.getInt("is_store");
                int card_level = rs.getInt("card_level");


                hs.put("user_card_id", user_card_id);
                hs.put("user_id", user_id);
                hs.put("product_no",product_no);
                hs.put("is_open", String.valueOf(is_open));
                hs.put("is_save", String.valueOf(is_save));
                hs.put("is_store", String.valueOf(is_store));
                hs.put("card_level", String.valueOf(card_level));
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectUserCard");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectReport(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                String product_url = rs.getString("url");

                hs.put("product_no", sk_id);
                hs.put("url", product_url);
                hs.put("type","datalabplus");

                try {
                    hs.put("data", rs.getString("data"));
                    hs.put("reviews", rs.getString("reviews"));
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlus");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectDatalabPlus(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                String product_url = rs.getString("url");
                String revenue = String.valueOf(rs.getDouble("revenue"));
                String revenue6m = String.valueOf(rs.getDouble("revenue6m"));
                String revenue3m = String.valueOf(rs.getDouble("revenue3m"));
                String revenue12m = String.valueOf(rs.getDouble("revenue12m"));
                String cat_full = rs.getString("cat_full");

                hs.put("product_no", sk_id);
                hs.put("url", product_url);
                hs.put("type","datalabplus");
                hs.put("revenue", revenue);
                hs.put("revenue3m", revenue3m);
                hs.put("revenue6m", revenue6m);
                hs.put("revenue12m", revenue12m);
                hs.put("cat_full", cat_full);

                try {
                    hs.put("data", rs.getString("data"));
                    hs.put("reviews", rs.getString("reviews"));
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlus");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<JSONObject> selectDatalabPlusOnlyTime(Connection conn, String query)
    {


        ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                JSONObject obj = new JSONObject();
                String sk_id = rs.getString("product_no");
                String product_url = rs.getString("url");
                obj.put("product_no", sk_id);
                obj.put("url",product_url);

                try {
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    obj.put("insert_time", insert_time);
                }catch(Exception ex){
                    //
                }
                arr.add(obj);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusOnlyTime");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public JSONArray selectGrowth(Connection conn, String query)
    {


        int rank = 1;
        JSONArray arr = new JSONArray();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                JSONObject hs = new JSONObject();
                Integer id = rs.getInt("id");
                String data =rs.getString("data");


                hs.put("id", id);
                hs.put("data", data);

                try {
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectGrowth");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public JSONArray selectDatalabPlusForGrowth(Connection conn, String query)
    {


        int rank = 1;
        JSONArray arr = new JSONArray();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                JSONObject hs = new JSONObject();
                String sk_id = rs.getString("product_no");
                String title = rs.getString("title");
                String cat_full = rs.getString("cat_full");
                String product_url = rs.getString("url");
                String revenue = String.valueOf(rs.getDouble("revenue"));
                String revenue6m = String.valueOf(rs.getDouble("revenue6m"));
                String revenue3m = String.valueOf(rs.getDouble("revenue3m"));
                String revenue12m = String.valueOf(rs.getDouble("revenue12m"));
                String growth_3m = rs.getString("growth");
                String store_name = rs.getString("store_name");
                Integer category_comparison = rs.getInt("category_comparison");
                String seller_grade = rs.getString("seller_grade");
                Integer overseas = rs.getInt("overseas");

                String data =rs.getString("data");


                hs.put("ranking", String.valueOf(rank));
                rank++;
                hs.put("product_no", sk_id);
                hs.put("url", product_url);
                hs.put("revenue", revenue);
                hs.put("revenue3m", revenue3m);
                hs.put("revenue6m", revenue6m);
                hs.put("revenue12m", revenue12m);
                hs.put("cat_full", cat_full);
                hs.put("title", title);
                hs.put("growth_3m", growth_3m);
                hs.put("store_name", store_name);
                hs.put("category_comparison", category_comparison.toString());
                hs.put("seller_grade", seller_grade);
                hs.put("overseas", overseas.toString());

                hs.put("data", data);

                try {
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusForGrowth");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<JSONObject> selectNaverStore(Connection conn, String query){


        ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();

        try {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next()) {

                JSONObject obj = new JSONObject();

                obj.put("store_id", rs.getString("store_id"));
                obj.put("store_name", rs.getString("store_name"));
                obj.put("store_url_name", rs.getString("store_url_name"));
                obj.put("store_desc", rs.getString("store_desc"));
                obj.put("store_grade", rs.getString("store_grade"));
                obj.put("talk_id", rs.getString("talk_id"));
                obj.put("address", rs.getString("address"));
                obj.put("register_number", rs.getString("register_number"));
                obj.put("tongsin_number", rs.getString("tongsin_number"));
                obj.put("logo_image", rs.getString("logo_image"));
                obj.put("goodservice", rs.getString("goodservice"));
                obj.put("product_count", rs.getInt("product_count"));
                obj.put("store_url", rs.getString("store_url"));
                obj.put("merge_data", rs.getString("merge_data"));
                if(rs.wasNull()) {
                    obj.put("merge_data", null);
                }

                if ((Integer) rs.getInt("revenue") == -1)
                    obj.put("revenue", null);
                else
                    obj.put("revenue", rs.getInt("revenue"));

                if ((Integer) rs.getInt("review_count") == -1)
                    obj.put("review_count", null);
                else
                    obj.put("review_count", rs.getInt("review_count"));


                if ((Integer) rs.getInt("visit_count") == -1)
                    obj.put("visit_count", null);
                else
                    obj.put("visit_count", rs.getInt("visit_count"));

                if ((Integer) rs.getInt("zzim_count") == -1)
                    obj.put("zzim_count", null);
                else
                    obj.put("zzim_count", rs.getInt("zzim_count"));

                if ((String) rs.getString("best10_image") == null)
                    obj.put("best10_image", null);
                else
                    obj.put("best10_image", rs.getString("best10_image"));

                arr.add(obj);
            }
        } catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverStore");
            System.err.println(e.getMessage());
        }

        return arr;
    }

    public ArrayList<JSONObject> selectDatalabPlusMall(Connection conn, String query){


        ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();

        try {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next()) {

                JSONObject obj = new JSONObject();

                obj.put("seller_id", rs.getString("seller_id"));
                obj.put("seller_name", rs.getString("seller_name"));
                obj.put("data", rs.getString("data"));
                if(rs.wasNull()) {
                    obj.put("data", null);
                }
                obj.put("enemy", rs.getString("enemy"));
                if(rs.wasNull()) {
                    obj.put("enemy", null);
                }


                arr.add(obj);
            }
        } catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusMall");
            System.err.println(e.getMessage());
        }

        return arr;
    }

    public ArrayList<HashMap<String,String>> selectKeywordCache(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                HashMap hs = new HashMap();

                try {
                    hs.put("data", rs.getString("keyword"));
                    hs.put("data", rs.getString("data"));
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);

                }catch(Exception ex){

                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordCache");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public void insertKeywordCache(Connection conn, String query, String keyword, String data)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, keyword);
                preparedStmt.setString(2, data);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertKeywordCache):" + keyword);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update keyword_cache set data=? where keyword=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, data);
                        preparedStmt2.setString(2, keyword);

                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertKeywordCache):" + keyword);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertKeywordCache ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordCache");
            System.err.println(e.getMessage());
        }
    }

    public ArrayList<HashMap<String,String>> selectDplusBasic(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                HashMap hs = new HashMap();

                try {
                    hs.put("data", rs.getString("product_no"));
                    hs.put("data", rs.getString("data"));
                    Timestamp insert_time = rs.getTimestamp("insert_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String time = sdf.format(insert_time);
                    hs.put("insert_time", time);

                }catch(Exception ex){

                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDplusBasic");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public void insertGrowth(Connection conn, String query, String data)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {
                preparedStmt.setString(1, data);
                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertGrowth):");

            }catch(Exception eee){
                preparedStmt.close();
            }

            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertGrowth");
            System.err.println(e.getMessage());
        }
    }


    public void insertGrowthDatalab(Connection conn, String query, String data)
    {
        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {
                preparedStmt.setString(1, data);
                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertGrowthDatalab):");

            }catch(Exception eee){
                preparedStmt.close();
            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertGrowthDatalab");
            System.err.println(e.getMessage());
        }
    }
    public String selectUserAgent(Connection conn, String query)
    {
        String user_agent = "";

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                user_agent = rs.getString("user_agent");

            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! - selectUserAgent");
            System.err.println(e.getMessage());
        }
        return user_agent;
    }
    public  JSONArray selectUpdateList(Connection conn, String query)
    {
        JSONArray arr = new JSONArray();
        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                Timestamp insert_time = rs.getTimestamp("insert_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String time = sdf.format(insert_time);

                String update_name = rs.getString("update_name");
                Integer success_count = rs.getInt("success_count");
                Integer total_count = rs.getInt("total_count");
                String message = rs.getString("message");
                String update_type = rs.getString("update_type");

                JSONObject obj = new JSONObject();
                obj.put("update_name", update_name);
                obj.put("success_count",success_count);
                obj.put("total_count", total_count);
                obj.put("message", message);
                obj.put("update_type", update_type);

                arr.add(obj);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectUpdateList");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public void startTransaction(Connection conn)
    {
        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement("START TRANSACTION;");

            try {

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("startTransaction");

            }catch(Exception eee){
                preparedStmt.close();
            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - startTransaction");
            System.err.println(e.getMessage());
        }
    }
    public void commit(Connection conn)
    {
        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement("commit;");

            try {

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("commit");

            }catch(Exception eee){
                preparedStmt.close();
            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - commit");
            System.err.println(e.getMessage());
        }
    }
    public void insertUpdateList(Connection conn, String query, String update_type, String update_name, String message,Integer success_count,Integer total_count)
    {
        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1,update_type);
                preparedStmt.setString(2, update_name);
                preparedStmt.setString(3, message);
                preparedStmt.setInt(4, success_count);
                preparedStmt.setInt(5, total_count);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertUpdateList):" + update_name);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update update_list set message=?, success_count=?,total_count=?,update_type=? where update_name=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, message);
                        preparedStmt2.setInt(2, success_count);
                        preparedStmt2.setInt(3, total_count);
                        preparedStmt2.setString(4, update_type);
                        preparedStmt2.setString(5, update_name);

                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertUpdateList):" + update_name);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertUpdateList ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertUpdateList");
            System.err.println(e.getMessage());
        }
    }

    public void updateSuccessCount(Connection conn, String query, int success_count,String service_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, success_count);
                preparedStmt2.setString(2, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + service_id);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateSuccessCount");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }
    }
    public void updateUpdateListRegisterType(Connection conn, String query, String register_type, String service_id) {
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, register_type);
                preparedStmt2.setString(2, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + service_id);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateUpdateListRegisterType");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    public void updateMessage(Connection conn, String query, String msg,String service_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, msg);
                preparedStmt2.setString(2, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + service_id);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateMessage");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }
    }
    public void updateSuccessCountMessage(Connection conn, String query, String msg, int cnt, String service_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, msg);
                preparedStmt2.setInt(2, cnt);
                preparedStmt2.setString(3, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + service_id);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateSuccessCountMessage");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    public void insertDatalabPlusBasic(Connection conn, String query, String product_no, String data)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, product_no);
                preparedStmt.setString(2, data);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertDplusBasic):" + product_no);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update datalab_plus_basic set data=? where product_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, data);
                        preparedStmt2.setString(2, product_no);

                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertDplusBasic):" + product_no);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertDplusBasic ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDplusBasic");
            System.err.println(e.getMessage());
        }
    }
    public void insertDatalabPlusMall(Connection conn, String query, String seller_id, String seller_name,String data, String enemy)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, seller_id);
                preparedStmt.setString(2, seller_name);
                preparedStmt.setString(3, data);
                preparedStmt.setString(4, enemy);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertDatalabPlusMall):" + seller_id);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update datalab_plus_mall set seller_name=?, data=?, enemy=? where seller_id=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, seller_name);
                        preparedStmt2.setString(2, data);
                        preparedStmt2.setString(3, enemy);
                        preparedStmt2.setString(4, seller_id);


                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertDatalabPlusMall):" + seller_id);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertDatalabPlusMall ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlusMall");
            System.err.println(e.getMessage());
        }
    }
    public void insertDatalabPlusMall2(Connection conn, String query, String seller_id, String seller_name, String data)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, seller_id);
                preparedStmt.setString(2, seller_name);
                preparedStmt.setString(3, data);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertDatalabPlusMall):" + seller_id);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update datalab_plus_mall set seller_name=?, data=? where seller_id=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, seller_name);
                        preparedStmt2.setString(2, data);
                        preparedStmt2.setString(3, seller_id);

                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertDatalabPlusMall):" + seller_id);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertDatalabPlusMall ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlusMall");
            System.err.println(e.getMessage());
        }
    }
    public String selectInserttime(Connection conn, String query)
    {

        String time = "";
        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                Timestamp insert_time = rs.getTimestamp("insert_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                time = sdf.format(insert_time);



            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlus");
            System.err.println(e.getMessage());
        }
        return time;
    }
    public ArrayList<HashMap<String,String>> selectDatalabPlusMakeCountry(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                String product_url = rs.getString("url");
                String make_country = rs.getString("make_country");
                hs.put("product_no", sk_id);
                hs.put("url", product_url);
                hs.put("type","datalabplus");
                hs.put("make_country", make_country);
                hs.put("insert_time", rs.getTimestamp("insert_time").toString());

                hs.put("source", rs.getString("source"));


                try {
                    hs.put("data", rs.getString("data"));
                    hs.put("reviews", rs.getString("reviews"));
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusMakeCountry");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    int temp_id = -1;

    public Integer selectIdOnly(Connection conn, Statement st, ResultSet rs, String query)
    {

        temp_id= -1;

        try
        {

            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                temp_id = rs.getInt("id");



            }

        }
        catch (Exception e)
        {

        }
        rs = null;
        return temp_id;
    }



    public ArrayList<HashMap<String,String>> selectUsersRequest(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                int user_id = rs.getInt("user_id");
                String product_url = rs.getString("url");
                String product_no = rs.getString("product_no");

                int is_store = rs.getInt("is_store");
                int is_save = rs.getInt("is_save");

                try {
                    hs.put("user_id", String.valueOf(user_id));
                    hs.put("is_store", String.valueOf(is_store));
                    hs.put("is_save", String.valueOf(is_save));
                    hs.put("url", product_url);
                    hs.put("product_no", product_no);
                    hs.put("type","user-request");

                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectUsersRequest");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public JSONObject selectDatalabPlusTotal2(Connection conn, Statement st, ResultSet rs , String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("id", rs.getInt("id"));
                obj.put("product_no", rs.getString("product_no"));
                obj.put("card_value", rs.getInt("card_value"));
                if(rs.wasNull()) {
                    obj.put("card_value", null);
                }
                obj.put("title", rs.getString("title"));
                obj.put("url", rs.getString("url"));
                obj.put("cat_full", rs.getString("cat_full"));
                obj.put("cat_big", rs.getString("cat_big"));
                obj.put("cat_middle", rs.getString("cat_middle"));
                obj.put("cat_small", rs.getString("cat_small"));
                obj.put("cat_product", rs.getString("cat_product"));
                obj.put("growth", rs.getDouble("growth"));
                obj.put("price", rs.getInt("price"));
                if(rs.wasNull()) {
                    obj.put("price", null);
                }

                obj.put("revenue", rs.getDouble("revenue"));
                obj.put("revenue3m", rs.getDouble("revenue3m"));
                obj.put("revenue6m", rs.getDouble("revenue6m"));
                obj.put("revenue12m", rs.getDouble("revenue12m"));
                obj.put("make_country", rs.getString("make_country"));
                obj.put("brand", rs.getString("brand"));
                obj.put("make_company", rs.getString("make_company"));
                obj.put("reviews", rs.getString("reviews"));
                obj.put("data", rs.getString("data"));
                obj.put("insert_time", rs.getTimestamp("insert_time"));
                obj.put("buy_url", rs.getString("buy_url"));
                obj.put("overseas", rs.getInt("overseas"));
                if(rs.wasNull()) {
                    obj.put("overseas", null);
                }

                obj.put("store_name", rs.getString("store_name"));
                obj.put("seller_grade", rs.getString("seller_grade"));
                obj.put("send_place", rs.getString("send_place"));
                obj.put("phone_number", rs.getString("phone_number"));
                obj.put("email", rs.getString("email"));
                obj.put("address", rs.getString("address"));
                obj.put("revenue_ss", rs.getInt("revenue_ss"));
                if(rs.wasNull()) {
                    obj.put("revenue_ss", null);
                }
                obj.put("revenue_fw", rs.getInt("revenue_fw"));
                if(rs.wasNull()) {
                    obj.put("revenue_fw", null);
                }
                obj.put("firstpage_keyword", rs.getString("firstpage_keyword"));
                obj.put("category_comparison", rs.getInt("category_comparison"));
                if(rs.wasNull()) {
                    obj.put("category_comparison", null);
                }
                obj.put("notsell", rs.getInt("notsell"));
                if(rs.wasNull()) {
                    obj.put("notsell", null);
                }
                obj.put("interest", rs.getInt("interest"));
                if(rs.wasNull()) {
                    obj.put("interest", null);
                }
                obj.put("source", rs.getString("source"));


                //arr.add(obj);


            }
           // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusTotal");
            System.err.println(e.getMessage());
        }
        return obj;

    }
    public JSONObject selectDatalabPlusTotal3(Connection conn, Statement st, ResultSet rs , String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("id", rs.getInt("id"));
                obj.put("product_no", rs.getString("product_no"));
                obj.put("data", rs.getString("data"));
                obj.put("cat_big", rs.getString("cat_big"));
                obj.put("notsell", rs.getInt("notsell"));
                if(rs.wasNull()) {
                    obj.put("notsell", null);
                }

                obj.put("insert_time", rs.getTimestamp("insert_time"));


            }
            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusTotal3");
            System.err.println(e.getMessage());
        }
        return obj;

    }
    public JSONObject selectKeywordHashTag(Connection conn, Statement st, ResultSet rs , String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("keyword_hashtag_id", rs.getInt("keyword_hashtag_id"));
                obj.put("keyword_product_no", rs.getString("keyword_product_no"));
                obj.put("cat_middle", rs.getString("cat_middle"));
                if(rs.wasNull()) {
                    obj.put("cat_middle", null);
                }
                obj.put("product_no", rs.getBigDecimal("product_no"));
                obj.put("keyword", rs.getString("keyword"));
                if(rs.wasNull()) {
                    obj.put("keyword", null);
                }
                obj.put("product_img", rs.getString("product_img"));
                if(rs.wasNull()) {
                    obj.put("product_img", null);
                }
                obj.put("title", rs.getString("title"));
                if(rs.wasNull()) {
                    obj.put("title", null);
                }
                obj.put("product_url", rs.getString("product_url"));
                if(rs.wasNull()) {
                    obj.put("product_url", null);
                }
                obj.put("store_name", rs.getString("store_name"));
                if(rs.wasNull()) {
                    obj.put("store_name", null);
                }

                obj.put("count_review", rs.getInt("count_review"));
                if(rs.wasNull()) {
                    obj.put("count_review", null);
                }
                obj.put("price", rs.getInt("price"));
                if(rs.wasNull()) {
                    obj.put("price", null);
                }
                obj.put("pricexcount_review", rs.getLong("pricexcount_review"));
                if(rs.wasNull()) {
                    obj.put("pricexcount_review", null);
                }

                obj.put("cat_big", rs.getString("cat_big"));
                if(rs.wasNull()) {
                    obj.put("cat_big", null);
                }
                obj.put("cat_small", rs.getString("cat_small"));
                if(rs.wasNull()) {
                    obj.put("cat_small", null);
                }
                obj.put("cat_product", rs.getString("cat_product"));
                if(rs.wasNull()) {
                    obj.put("cat_product", null);
                }

                obj.put("ranking", rs.getInt("ranking"));
                if(rs.wasNull()) {
                    obj.put("ranking", null);
                }

                obj.put("insert_time", rs.getTimestamp("insert_time"));
                if(rs.wasNull()) {
                    obj.put("insert_time", null);
                }

                obj.put("search_count", rs.getInt("search_count"));
                if(rs.wasNull()) {
                    obj.put("search_count", null);
                }

                obj.put("click_count", rs.getInt("click_count"));
                if(rs.wasNull()) {
                    obj.put("click_count", null);
                }

                obj.put("compete", rs.getInt("compete"));
                if(rs.wasNull()) {
                    obj.put("compete", null);
                }

                obj.put("ad_count", rs.getInt("ad_count"));
                if(rs.wasNull()) {
                    obj.put("ad_count", null);
                }

                obj.put("total_cnt", rs.getInt("total_cnt"));
                if(rs.wasNull()) {
                    obj.put("total_cnt", null);
                }

                //arr.add(obj);

            }
            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordHashTag");
            System.err.println(e.getMessage());
        }
        return obj;

    }

    public JSONObject selectNaverLightTotal(Connection conn, Statement st, ResultSet rs , JSONObject obj, String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        //JSONObject obj = new JSONObject();

        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("id", rs.getInt("id"));
                obj.put("title", rs.getString("title"));
                obj.put("product_no", rs.getString("product_no"));
                obj.put("store_name", rs.getString("store_name"));
                if(rs.wasNull()) {
                    obj.put("store_name", null);
                }
                obj.put("store_grade", rs.getString("store_grade"));
                if(rs.wasNull()) {
                    obj.put("store_grade", null);
                }
                obj.put("star", rs.getDouble("star"));
                if(rs.wasNull()) {
                    obj.put("star", null);
                }
                obj.put("product_img", rs.getString("product_img"));
                if(rs.wasNull()) {
                    obj.put("product_img", null);
                }
                obj.put("product_url", rs.getString("product_url"));
                if(rs.wasNull()) {
                    obj.put("product_url", null);
                }
                obj.put("insert_time", rs.getTimestamp("insert_time"));

                obj.put("insert_timestamp", rs.getInt("insert_timestamp"));
                if(rs.wasNull()) {
                    obj.put("insert_timestamp", null);
                }
                obj.put("register_timestamp", rs.getInt("register_timestamp"));
                if(rs.wasNull()) {
                    obj.put("register_timestamp", null);
                }
                obj.put("register_date", rs.getString("register_date"));
                if(rs.wasNull()) {
                    obj.put("register_date", null);
                }
                obj.put("cat_full", rs.getString("cat_full"));
                if(rs.wasNull()) {
                    obj.put("cat_full", null);
                }
                obj.put("cat_big", rs.getString("cat_big"));
                if(rs.wasNull()) {
                    obj.put("cat_big", null);
                }
                obj.put("cat_middle", rs.getString("cat_middle"));
                if(rs.wasNull()) {
                    obj.put("cat_middle", null);
                }
                obj.put("cat_small", rs.getString("cat_small"));
                if(rs.wasNull()) {
                    obj.put("cat_small", null);
                }
                obj.put("cat_product", rs.getString("cat_product"));
                if(rs.wasNull()) {
                    obj.put("cat_product", null);
                }
                obj.put("count_review", rs.getInt("count_review"));
                if(rs.wasNull()) {
                    obj.put("count_review", null);
                }
                obj.put("count_zzim", rs.getInt("count_zzim"));
                if(rs.wasNull()) {
                    obj.put("count_zzim", null);
                }
                obj.put("count_buy", rs.getInt("count_buy"));
                if(rs.wasNull()) {
                    obj.put("count_buy", null);
                }
                obj.put("price", rs.getInt("price"));
                if(rs.wasNull()) {
                    obj.put("price", null);
                }
                obj.put("data_review", rs.getString("data_review"));
                if(rs.wasNull()) {
                    obj.put("data_review", null);
                }
                obj.put("pricexcount_review", rs.getLong("pricexcount_review"));
                if(rs.wasNull()) {
                    obj.put("pricexcount_review", null);
                }
                obj.put("firstpage_keyword", rs.getString("firstpage_keyword"));
                if(rs.wasNull()) {
                    obj.put("firstpage_keyword", null);
                }
                obj.put("category_comparison", rs.getInt("category_comparison"));
                if(rs.wasNull()) {
                    obj.put("category_comparison", null);
                }
                obj.put("deletes", rs.getInt("deletes"));
                if(rs.wasNull()) {
                    obj.put("deletes", null);
                }
                obj.put("user_update_timestamp", rs.getTimestamp("user_update_timestamp"));
                if(rs.wasNull()) {
                    obj.put("user_update_timestamp", null);
                }
                obj.put("datalab_update_timestamp", rs.getTimestamp("datalab_update_timestamp"));
                if(rs.wasNull()) {
                    obj.put("datalab_update_timestamp", null);
                }
                obj.put("id", rs.getInt("id"));
                if(rs.wasNull()) {
                    obj.put("id", null);
                }
            }
            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusTotal");
            System.err.println(e.getMessage());
        }
        return obj;

    }


    public JSONObject selectKeywordHashtagTotal(Connection conn, Statement st, ResultSet rs , String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("keyword_hashtag_id", rs.getInt("keyword_hashtag_id"));
                obj.put("keyword_product_no", rs.getString("keyword_product_no"));
                obj.put("cat_middle", rs.getString("cat_middle"));
                obj.put("store_name", rs.getString("store_name"));
                if(rs.wasNull()) {
                    obj.put("store_name", null);
                }

            }
            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusTotal");
            System.err.println(e.getMessage());
        }
        return obj;

    }

    public ArrayList<JSONObject> selectDatalabPlusTotal(Connection conn, String query)
    {


        ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();

        try
        {

            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                JSONObject obj = new JSONObject();

                obj.put("id", rs.getInt("id"));
                obj.put("product_no", rs.getString("product_no"));
                obj.put("card_value", rs.getInt("card_value"));
                if(rs.wasNull()) {
                    obj.put("card_value", null);
                }
                obj.put("title", rs.getString("title"));
                obj.put("url", rs.getString("url"));
                obj.put("cat_full", rs.getString("cat_full"));
                obj.put("cat_big", rs.getString("cat_big"));
                obj.put("cat_middle", rs.getString("cat_middle"));
                obj.put("cat_small", rs.getString("cat_small"));
                obj.put("cat_product", rs.getString("cat_product"));
                obj.put("growth", rs.getDouble("growth"));
                obj.put("price", rs.getInt("price"));
                if(rs.wasNull()) {
                    obj.put("price", null);
                }

                obj.put("revenue", rs.getDouble("revenue"));
                obj.put("revenue3m", rs.getDouble("revenue3m"));
                obj.put("revenue6m", rs.getDouble("revenue6m"));
                obj.put("revenue12m", rs.getDouble("revenue12m"));
                obj.put("make_country", rs.getString("make_country"));
                obj.put("brand", rs.getString("brand"));
                obj.put("make_company", rs.getString("make_company"));
                obj.put("reviews", rs.getString("reviews"));
                obj.put("data", rs.getString("data"));
                obj.put("insert_time", rs.getTimestamp("insert_time"));
                obj.put("buy_url", rs.getString("buy_url"));
                obj.put("overseas", rs.getInt("overseas"));
                if(rs.wasNull()) {
                    obj.put("overseas", null);
                }

                obj.put("store_name", rs.getString("store_name"));
                obj.put("seller_grade", rs.getString("seller_grade"));
                obj.put("send_place", rs.getString("send_place"));
                obj.put("phone_number", rs.getString("phone_number"));
                obj.put("email", rs.getString("email"));
                obj.put("address", rs.getString("address"));
                obj.put("revenue_ss", rs.getInt("revenue_ss"));
                if(rs.wasNull()) {
                    obj.put("revenue_ss", null);
                }
                obj.put("revenue_fw", rs.getInt("revenue_fw"));
                if(rs.wasNull()) {
                    obj.put("revenue_fw", null);
                }
                obj.put("firstpage_keyword", rs.getString("firstpage_keyword"));
                obj.put("category_comparison", rs.getInt("category_comparison"));
                if(rs.wasNull()) {
                    obj.put("category_comparison", null);
                }
                obj.put("notsell", rs.getInt("notsell"));
                if(rs.wasNull()) {
                    obj.put("notsell", null);
                }
                obj.put("interest", rs.getInt("interest"));
                if(rs.wasNull()) {
                    obj.put("interest", null);
                }
                obj.put("source", rs.getString("source"));


                arr.add(obj);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusTotal");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList<HashMap<String,String>> selectDatalabPlusId(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = String.valueOf(rs.getInt("id"));
                String product_no = rs.getString("product_no");
                hs.put("id", sk_id);
                try {
                    hs.put("id", sk_id);
                    hs.put("product_no", product_no);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusId");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList<HashMap<String,Integer>> selectDatalabPlusMaxId(Connection conn, String query)
    {


        ArrayList<HashMap<String,Integer>> arr = new  ArrayList<HashMap<String,Integer>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                int sk_id = rs.getInt("id");
                hs.put("id", sk_id);
                try {
                    hs.put("id", sk_id);
                }catch(Exception ex){
                    //
                }
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusMaxId");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList<HashMap<String,String>> selectDatalabPlusProductNo(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                hs.put("product_no", sk_id);

                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusProductNo");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectDatalabPlusProductNoId(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                int id = rs.getInt("id");
                hs.put("product_no", sk_id);
                hs.put("id", String.valueOf(id));

                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusProductNo");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectDatalabPlusProductData(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String data = rs.getString("data");
                hs.put("data", data);

                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusProductData");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList<HashMap<String,String>> selectDatalabPlusImageUrl(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String sk_id = rs.getString("product_no");
                String product_url = rs.getString("url");
                String image_url = rs.getString("image_url");
                hs.put("product_no", sk_id);
                hs.put("image_url", image_url);
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabPlusImageUrl");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectCategoryIds(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String sk_id = rs.getString("id");
                String category_code = rs.getString("category_code");
                String big_name = rs.getString("big_name");
                String middle_name = rs.getString("middle_name");
                String small_name = rs.getString("small_name");
                String product_name = rs.getString("product_name");
                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("id", sk_id);
                hs.put("big_name", big_name);
                hs.put("middle_name",middle_name);
                hs.put("small_name",small_name);
                hs.put("product_name",product_name);
                hs.put("category_code",category_code);
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectCategoryIds");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectCategoryComparsionList(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                HashMap hs = new HashMap();
                String service_id = rs.getString("service_id");
                String category = rs.getString("category");
                hs.put("service_id",service_id);
                hs.put("category",category);
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectCategoryComparsionList");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public void insertCategoryMatching(Connection conn, String query, String image_src, String service_now, String service_name, String service_id, String service_man, String service_brand,String category, String category_name, int lowest_price, int reviews)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, image_src);
                preparedStmt.setString(2, service_now);
                preparedStmt.setString(3, service_name);
                preparedStmt.setString(4, service_id);
                preparedStmt.setString(5, service_man);
                preparedStmt.setString(6, service_brand);
                preparedStmt.setString(7, category);
                preparedStmt.setString(8, category_name);
                preparedStmt.setInt(9, lowest_price);
                preparedStmt.setInt(10, reviews);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt.close();
                System.out.println("인서트 성공(insertCategoryMatching):" + service_name);

            }catch(Exception eee){
                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update category_comparison_list set lowest_price=?, reviews=?, service_image=?, service_brand=?, service_manufacturer=? , service_name=? where service_id=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setInt(1, lowest_price);
                        preparedStmt2.setInt(2, reviews);
                        preparedStmt2.setString(3,  image_src);
                        preparedStmt2.setString(4,  service_brand);
                        preparedStmt2.setString(5,  service_man);
                        preparedStmt2.setString(6,  service_name);
                        preparedStmt2.setString(7,  service_id);

                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertCategoryMatching):" + service_name);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertCategoryMatching ");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertCategoryMatching");
            System.err.println(e.getMessage());
        }
    }

    public void insertDatalabPlus(Connection conn, String query, long product_no, String title, double growth3m, int price, double revenue,double revenue3m,double revenue6m,double revenue12m,String data, String make_country,String url,int overseas, String send_place,String  phone_number, String email, String address, double revenue_ss, double revenue_fw, String review_total, String store_name, String seller_grade, Long product_value, String cat_full, String cat_big,String cat_middle,String cat_small, String cat_product,String brand, String make_company, int category_comparison, String firstpage_keyword)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, String.valueOf(product_no));
                preparedStmt.setString(2, title);
                preparedStmt.setDouble(3, growth3m);
                preparedStmt.setInt(4, price);
                preparedStmt.setDouble(5, revenue);
                preparedStmt.setDouble(6, revenue3m);
                preparedStmt.setDouble(7, revenue6m);
                preparedStmt.setDouble(8, revenue12m);
                preparedStmt.setString(9, data);
                preparedStmt.setString(10, make_country);
                preparedStmt.setString(11, url);

                preparedStmt.setInt(12, overseas);
                preparedStmt.setString(13, send_place);
                preparedStmt.setString(14, phone_number);
                preparedStmt.setString(15, email);
                preparedStmt.setString(16, address);
                preparedStmt.setDouble(17, revenue_ss);
                preparedStmt.setDouble(18, revenue_fw);
                preparedStmt.setString(19,review_total);
                preparedStmt.setString(20,store_name);
                preparedStmt.setString(21,seller_grade);
                preparedStmt.setLong(22,product_value);
                preparedStmt.setString(23,cat_full);
                preparedStmt.setString(24,cat_big);
                preparedStmt.setString(25,cat_middle);
                preparedStmt.setString(26,cat_small);
                preparedStmt.setString(27,cat_product);
                preparedStmt.setString(28,brand);
                preparedStmt.setString(29,make_company);

                if(category_comparison == -1)
                    preparedStmt.setNull(30, Types.INTEGER);
                else
                    preparedStmt.setInt(30,category_comparison);

                if(firstpage_keyword == null)
                    preparedStmt.setNull(31, Types.VARCHAR);
                else
                    preparedStmt.setString(31,firstpage_keyword);

                preparedStmt.execute();
                System.out.println("인서트 성공(Datalabplus):" + product_no);
                preparedStmt.close();

            }catch(Exception eee){

                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update datalab_plus set title=?,growth=?,price=?,revenue=?,revenue3m=?,revenue6m=?,revenue12m=?,data=?,make_country=?,url=?, overseas=?,send_place=?,phone_number=?,email=?,address=?,revenue_ss=?, revenue_fw=?, reviews=?, store_name=?, seller_grade=?, card_value=?, cat_full=?, cat_big=?, cat_middle=?, cat_small=?, cat_product=? , brand=?, make_company=?, category_comparison=?, firstpage_keyword=? where product_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, title);
                        preparedStmt2.setDouble(2, growth3m);
                        preparedStmt2.setInt(3, price);
                        preparedStmt2.setDouble(4, revenue);
                        preparedStmt2.setDouble(5, revenue3m);
                        preparedStmt2.setDouble(6, revenue6m);
                        preparedStmt2.setDouble(7, revenue12m);
                        preparedStmt2.setString(8, data);
                        preparedStmt2.setString(9, make_country);
                        preparedStmt2.setString(10, url);

                        preparedStmt2.setInt(11, overseas);
                        preparedStmt2.setString(12, send_place);
                        preparedStmt2.setString(13, phone_number);
                        preparedStmt2.setString(14, email);
                        preparedStmt2.setString(15, address);
                        preparedStmt2.setDouble(16, revenue_ss);
                        preparedStmt2.setDouble(17, revenue_fw);
                        preparedStmt2.setString(18, review_total);
                        preparedStmt2.setString(19, store_name);
                        preparedStmt2.setString(20, seller_grade);
                        preparedStmt2.setLong(21, product_value);

                        preparedStmt2.setString(22, cat_full);
                        preparedStmt2.setString(23, cat_big);
                        preparedStmt2.setString(24, cat_middle);
                        preparedStmt2.setString(25, cat_small);
                        preparedStmt2.setString(26, cat_product);
                        preparedStmt2.setString(27, brand);
                        preparedStmt2.setString(28, make_company);

                        if(category_comparison == -1)
                            preparedStmt2.setNull(29, Types.INTEGER);
                        else
                            preparedStmt2.setInt(29,category_comparison);


                        if(firstpage_keyword == null)
                            preparedStmt2.setNull(30, Types.VARCHAR);
                        else
                            preparedStmt2.setString(30,firstpage_keyword);

                        preparedStmt2.setString(31, String.valueOf(product_no));


                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(Datalabplus):" + product_no);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertDatalabPlus --update!!");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }
            }




        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlus");
            System.err.println(e.getMessage());
        }
    }


    public void insertChina(Connection conn, String query, JSONObject obj) {

        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);



            try {



                preparedStmt.setString(1, (String) obj.get("china_no"));
                preparedStmt.setString(2, (String) obj.get("플랫폼"));
                preparedStmt.setString(3, (String) obj.get("title"));
                preparedStmt.setDouble(4, (Double) obj.get("가격_최고가"));
                preparedStmt.setDouble(5, (Double) obj.get("가격_최저가"));
                preparedStmt.setString(6,  (String) obj.get("url"));
                preparedStmt.setString(7, obj.get("메인썸네일").toString());
                preparedStmt.setInt(8, Integer.parseInt((String) obj.get("구매자수")));
                preparedStmt.setInt(9, Integer.parseInt((String) obj.get("구매수")));
                preparedStmt.setString(10, (String)obj.get("무게"));
                preparedStmt.setString(11, (String)obj.get("상점URL"));

                preparedStmt.setString(12, (String)obj.get("상점명"));
                preparedStmt.setString(13, (String)obj.get("업력"));
                preparedStmt.setString(14,  (String)obj.get("아리왕왕URL"));
                preparedStmt.setString(15, (String)obj.get("신용도"));
                preparedStmt.setString(16, (String)obj.get("비지니스모델"));
                preparedStmt.setString(17, (String)obj.get("주소"));
                preparedStmt.setString(18,  (String)obj.get("설명일치율(업계평균대비)"));
                preparedStmt.setString(19, (String)obj.get("응답속도(업계평균대비)"));
                preparedStmt.setString(20, (String)obj.get("배송속도(업계평균대비)"));
                preparedStmt.setString(21, (String)obj.get("반품률"));
                preparedStmt.setLong(22, (Long)obj.get("최소수량"));
                preparedStmt.setString(23, (String)obj.get("카테고리"));
                preparedStmt.setString(24, (String)obj.get("cat_big"));
                preparedStmt.setString(25, (String)obj.get("cat_middle"));
                preparedStmt.setString(26, (String)obj.get("cat_small"));
                preparedStmt.setString(27, (String)obj.get("cat_product"));
                preparedStmt.setString(28, obj.toString());

                preparedStmt.execute();

            } catch (Exception eee) {

                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update china set platform=?,title=?,price_highest=?,price_lowest=?,url=?,thumbnail=?,buyer_count=?,sell_count=?,weight=?,store_url=?,store_name=?,store_year=?,store_ariwangwang=?,store_credit=?,store_bm=?,store_address=?,store_desc_rate=?,store_response_rate=?, store_sendspeed_rate=?, store_refund_rate=?, begin_amount=?, category=?,cat_big=?, cat_middle=?, cat_small=?, cat_product=?, data=? where china_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);




                    try {
                        preparedStmt2.setString(1, (String) obj.get("플랫폼"));
                        preparedStmt2.setString(2, (String) obj.get("title"));
                        preparedStmt2.setDouble(3, (Double) obj.get("가격_최고가"));
                        preparedStmt2.setDouble(4, (Double) obj.get("가격_최저가"));
                        preparedStmt2.setString(5,  (String) obj.get("url"));
                        preparedStmt2.setString(6, obj.get("메인썸네일").toString());
                        preparedStmt2.setInt(7, Integer.parseInt((String) obj.get("구매자수")));
                        preparedStmt2.setDouble(8, Integer.parseInt((String) obj.get("구매수")));
                        preparedStmt2.setString(9, (String)obj.get("무게"));
                        preparedStmt2.setString(10, (String)obj.get("상점URL"));

                        preparedStmt2.setString(11, (String)obj.get("상점명"));
                        preparedStmt2.setString(12, (String)obj.get("업력"));
                        preparedStmt2.setString(13,  (String)obj.get("아리왕왕URL"));
                        preparedStmt2.setString(14, (String)obj.get("신용도"));
                        preparedStmt2.setString(15, (String)obj.get("비지니스모델"));
                        preparedStmt2.setString(16, (String)obj.get("주소"));
                        preparedStmt2.setString(17,  (String)obj.get("설명일치율(업계평균대비)"));
                        preparedStmt2.setString(18, (String)obj.get("응답속도(업계평균대비)"));
                        preparedStmt2.setString(19, (String)obj.get("배송속도(업계평균대비)"));
                        preparedStmt2.setString(20, (String)obj.get("반품률"));
                        preparedStmt2.setLong(21, (Long)obj.get("최소수량"));

                        preparedStmt2.setString(22, (String)obj.get("카테고리"));
                        preparedStmt2.setString(23, (String)obj.get("cat_big"));
                        preparedStmt2.setString(24, (String)obj.get("cat_middle"));
                        preparedStmt2.setString(25, (String)obj.get("cat_small"));
                        preparedStmt2.setString(26, (String)obj.get("cat_product"));

                        preparedStmt2.setString(27, obj.toString());
                        preparedStmt2.setString(28, (String) obj.get("china_no"));


                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(china):" + (String) obj.get("china_no"));

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertChina --update!!");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("예외 발생 - insertChina");
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public void insertChinaLight(Connection conn, String query, JSONObject obj) {

        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, (String) obj.get("china_no"));
                preparedStmt.setString(2, (String) obj.get("platform"));
                preparedStmt.setString(3, (String) obj.get("title"));
                preparedStmt.setString(4, (String) obj.get("url"));
                preparedStmt.setString(5, (String) obj.get("image"));
                preparedStmt.setDouble(6, (Double) obj.get("price"));
                preparedStmt.setString(7, (String) obj.get("store_name"));
                preparedStmt.setString(8, (String) obj.get("store_url"));
                preparedStmt.setString(9, (String) obj.get("store_ariwangwang"));
                preparedStmt.setDouble(10, (Double) obj.get("반복구매율"));
                preparedStmt.setInt(11, (Integer) obj.get("sell_amount"));
                preparedStmt.setInt(12, (Integer) obj.get("buyer_count"));
                preparedStmt.setString(13, obj.toString());
                preparedStmt.setString(14, (String) obj.get("ss_product_no"));

                preparedStmt.setInt(15, (Integer) obj.get("ranking"));
                System.out.println("인서트 성공(china):" + (String) obj.get("china_no"));
                preparedStmt.execute();

            } catch (Exception eee) {

                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update china_light set platform=?,title=?,url=?,image=?,price=?,store_name=?,store_url=?,store_ariwangwang=?,resell_rate=?,sell_amount=?,buyer_count=?,data=?, ss_product_no=?, ranking=? where china_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, (String) obj.get("platform"));
                        preparedStmt2.setString(2, (String) obj.get("title"));
                        preparedStmt2.setString(3, (String) obj.get("url"));
                        preparedStmt2.setString(4, (String) obj.get("image"));
                        preparedStmt2.setDouble(5, (Double) obj.get("price"));
                        preparedStmt2.setString(6, (String) obj.get("store_name"));
                        preparedStmt2.setString(7, (String) obj.get("store_url"));
                        preparedStmt2.setString(8, (String) obj.get("store_ariwangwang"));
                        preparedStmt2.setDouble(9, (Double) obj.get("반복구매율"));
                        preparedStmt2.setInt(10, (Integer) obj.get("sell_amount"));
                        preparedStmt2.setInt(11, (Integer) obj.get("buyer_count"));
                        preparedStmt2.setString(12, obj.toString());
                        preparedStmt2.setString(13, (String) obj.get("ss_product_no"));

                        preparedStmt2.setInt(14, (Integer) obj.get("ranking"));
                        preparedStmt2.setString(15, (String) obj.get("china_no"));


                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(china):" + (String) obj.get("china_no"));

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertChinaLight --update!!");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("예외 발생 - insertChina");
            System.err.println(e.getMessage());
        }
    }
    public void insertDatalabplusChina(Connection conn, String query, String product_no, String china_no, int match_count) {

        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, (String) product_no);
                preparedStmt.setString(2, (String) china_no);
                preparedStmt.setInt(3, match_count);
                preparedStmt.execute();

            } catch (Exception eee) {

                preparedStmt.close();

            }

        } catch (Exception e) {
            System.err.println("예외 발생 - insertChina");
            System.err.println(e.getMessage());
        }
    }

    public void insertDatalabplusChina2(Connection conn, String query, String product_no, String china_no, String thumbnail, double price_highest, double price_lowest, String url, int match_count) {

        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, (String) product_no);
                preparedStmt.setString(2, (String) china_no);
                preparedStmt.setString(3, (String) thumbnail);
                preparedStmt.setDouble(4, (Double) price_highest);
                preparedStmt.setDouble(5, (Double) price_lowest);
                preparedStmt.setString(6, (String) url);
                preparedStmt.setInt(7, match_count);
                preparedStmt.execute();

            } catch (Exception eee) {

                preparedStmt.close();

            }

        } catch (Exception e) {
            System.err.println("예외 발생 - insertChina");
            System.err.println(e.getMessage());
        }
    }
    public void replaceCache1(Connection conn,    PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setString(1, (String)String.valueOf(obj.get("product_no")));

                if((String)obj.get("title") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2,(String)obj.get("title"));

                if((int)obj.get("price") == -1)
                    preparedStmt.setNull(3, Types.INTEGER);
                else
                    preparedStmt.setInt(3,(int)obj.get("price"));

                if((String)obj.get("season") == null)
                    preparedStmt.setNull(4, Types.VARCHAR);
                else
                    preparedStmt.setString(4,(String)obj.get("season"));


                if((String)obj.get("cat_mix") == null)
                    preparedStmt.setNull(5, Types.VARCHAR);
                else
                    preparedStmt.setString(5,(String)obj.get("cat_mix"));

                if((String)obj.get("cat_big") == null)
                    preparedStmt.setNull(6, Types.VARCHAR);
                else
                    preparedStmt.setString(6,(String)obj.get("cat_big"));


                if((String)obj.get("cat_middle") == null)
                    preparedStmt.setNull(7, Types.VARCHAR);
                else
                    preparedStmt.setString(7,(String)obj.get("cat_middle"));


                if((Double)obj.get("revenue") == null)
                    preparedStmt.setNull(8, Types.DOUBLE);
                else
                    preparedStmt.setDouble(8, (Double)obj.get("revenue"));

                if((Double)obj.get("revenue3m") == null)
                    preparedStmt.setNull(9, Types.DOUBLE);
                else
                    preparedStmt.setDouble(9, (Double) obj.get("revenue3m"));



                if((String)obj.get("image_url") == null)
                    preparedStmt.setNull(10, Types.VARCHAR);
                else
                    preparedStmt.setString(10,(String)obj.get("image_url"));



                if((Timestamp)obj.get("insert_time") == null)
                    preparedStmt.setNull(11, Types.TIMESTAMP);
                else
                    preparedStmt.setTimestamp(11, (Timestamp)obj.get("insert_time"));


                preparedStmt.setInt(12,(Integer)obj.get("id"));


                preparedStmt.setString(13,(String)obj.get("hashtag"));

                preparedStmt.execute();

                System.out.println("인서트 성공! id:" + obj.get("id"));

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - replaceDatalabPlus");
            System.err.println(e.getMessage());
        }
    }

    public void replaceTitle(Connection conn,    PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setString(1, (String)String.valueOf(obj.get("id")));

                if((String)obj.get("product_no") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2,(String)obj.get("product_no"));

                if((String)obj.get("title") == null)
                    preparedStmt.setNull(3, Types.VARCHAR);
                else
                    preparedStmt.setString(3,(String)obj.get("title"));


                preparedStmt.execute();

                System.out.println("인서트 성공!");

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - replaceTitle");
            System.err.println(e.getMessage());
        }
    }


    public void replaceKeywordHashtag(Connection conn,PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setInt(1, (int)obj.get("keyword_hashtag_id"));

                if((String)obj.get("keyword_product_no") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2,(String)obj.get("keyword_product_no"));


                if((String)obj.get("cat_middle") == null)
                    preparedStmt.setNull(3, Types.VARCHAR);
                else
                    preparedStmt.setString(3,(String)obj.get("cat_middle"));

                if(obj.get("product_no") == null)
                    preparedStmt.setNull(4,Types.BIGINT);
                else
                    preparedStmt.setBigDecimal(4,(BigDecimal) obj.get("product_no"));

                if((String)obj.get("keyword") == null)
                    preparedStmt.setNull(5, Types.VARCHAR);
                else
                    preparedStmt.setString(5, (String)obj.get("keyword"));

                if((String)obj.get("product_img") == null)
                    preparedStmt.setNull(6, Types.VARCHAR);
                else
                    preparedStmt.setString(6, (String)obj.get("product_img"));

                if((String)obj.get("title") == null)
                    preparedStmt.setNull(7, Types.VARCHAR);
                else
                    preparedStmt.setString(7, (String)obj.get("title"));

                if((String)obj.get("product_url") == null)
                    preparedStmt.setNull(8, Types.VARCHAR);
                else
                    preparedStmt.setString(8, (String)obj.get("product_url"));

                if((String)obj.get("store_name") == null)
                    preparedStmt.setNull(9, Types.VARCHAR);
                else
                    preparedStmt.setString(9, (String)obj.get("store_name"));

                if(String.valueOf((int)obj.get("count_review")) ==null)
                    preparedStmt.setNull(10, Types.INTEGER);
                else
                    preparedStmt.setInt(10, (int)obj.get("count_review"));

                if(String.valueOf((int)obj.get("price")) ==null)
                    preparedStmt.setNull(11, Types.INTEGER);
                else
                    preparedStmt.setInt(11, (int)obj.get("price"));


                if(String.valueOf(obj.get("pricexcount_review")) == null)
                    preparedStmt.setNull(12, Types.BIGINT);
                else
                    if (obj.get("pricexcount_review").getClass().getSimpleName().equals("Long")) {
                        preparedStmt.setLong(12, (long)obj.get("pricexcount_review"));
                    }
                    else {
                        preparedStmt.setLong(12, (int)obj.get("pricexcount_review"));
                    }

                if((String)obj.get("cat_big") == null)
                    preparedStmt.setNull(13, Types.VARCHAR);
                else
                    preparedStmt.setString(13, (String)obj.get("cat_big"));


                if((String)obj.get("cat_small") == null)
                    preparedStmt.setNull(14, Types.VARCHAR);
                else
                    preparedStmt.setString(14, (String)obj.get("cat_small"));


                if((String)obj.get("cat_product") == null)
                    preparedStmt.setNull(15, Types.VARCHAR);
                else
                    preparedStmt.setString(15, (String)obj.get("cat_product"));


                if(obj.get("ranking") == null)
                    preparedStmt.setNull(16, Types.INTEGER);
                else
                    preparedStmt.setInt(16, (int)obj.get("ranking"));


                if((Timestamp)obj.get("insert_time") == null)
                    preparedStmt.setNull(17, Types.INTEGER);
                else
                    preparedStmt.setTimestamp(17, (Timestamp) obj.get("insert_time"));



                if(obj.get("search_count") == null)
                    preparedStmt.setNull(18, Types.INTEGER);
                else
                    preparedStmt.setInt(18, (int)obj.get("search_count"));


                if(obj.get("click_count") == null)
                    preparedStmt.setNull(19, Types.INTEGER);
                else
                    preparedStmt.setInt(19, (int)obj.get("click_count"));


                if(obj.get("compete") == null)
                    preparedStmt.setNull(20, Types.INTEGER);
                else
                    preparedStmt.setInt(20, (int)obj.get("compete"));

                if(obj.get("ad_count") == null)
                    preparedStmt.setNull(21, Types.INTEGER);
                else
                    preparedStmt.setInt(21, (int)obj.get("ad_count"));

                if(obj.get("total_cnt") == null)
                    preparedStmt.setNull(22, Types.INTEGER);
                else
                    preparedStmt.setInt(22, (int)obj.get("total_cnt"));

                preparedStmt.execute();

                System.out.println("인서트 성공!:" + (int)obj.get("keyword_hashtag_id") );

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - replaceKeywordHashtag");
            System.err.println(e.getMessage());
        }
    }
    public void insertDatalabPlusWithId(Connection conn,    PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setString(1, (String)String.valueOf(obj.get("product_no")));

                if((String)obj.get("title") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2, (String)obj.get("title"));



                if((Double)obj.get("growth") == null)
                    preparedStmt.setNull(3, Types.DOUBLE);
                else
                    preparedStmt.setDouble(3, (Double)obj.get("growth"));


                if((Integer)obj.get("price") == null)
                    preparedStmt.setNull(4, Types.INTEGER);
                else
                    preparedStmt.setInt(4, (int)obj.get("price"));

                if((Double)obj.get("revenue") == null)
                    preparedStmt.setNull(5, Types.DOUBLE);
                else
                    preparedStmt.setDouble(5, (Double)obj.get("revenue"));

                if((Double)obj.get("revenue3m") == null)
                    preparedStmt.setNull(6, Types.DOUBLE);
                else
                    preparedStmt.setDouble(6, (Double) obj.get("revenue3m"));


                if((Double)obj.get("revenue6m") == null)
                    preparedStmt.setNull(7, Types.DOUBLE);
                else
                    preparedStmt.setDouble(7, (Double)obj.get("revenue6m"));


                if((Double)obj.get("revenue12m") == null)
                    preparedStmt.setNull(8, Types.DOUBLE);
                else
                    preparedStmt.setDouble(8, (Double)obj.get("revenue12m"));


                if((String)obj.get("data") == null)
                    preparedStmt.setNull(9, Types.VARCHAR);
                else
                    preparedStmt.setString(9, (String) obj.get("data"));


                if((String)obj.get("make_country") == null)
                    preparedStmt.setNull(10, Types.VARCHAR);
                else
                    preparedStmt.setString(10, (String)obj.get("make_country"));



                if((String)obj.get("url") == null)
                    preparedStmt.setNull(11, Types.VARCHAR);
                else
                    preparedStmt.setString(11, (String)obj.get("url"));



                if((Integer)obj.get("overseas") == null)
                    preparedStmt.setNull(12, Types.INTEGER);
                else
                    preparedStmt.setInt(12, (int) obj.get("overseas"));

                if((String)obj.get("send_place") == null)
                    preparedStmt.setNull(13, Types.VARCHAR);
                else
                    preparedStmt.setString(13, (String)obj.get("send_place"));

                if((String)obj.get("phone_number") == null)
                    preparedStmt.setNull(14, Types.VARCHAR);
                else
                    preparedStmt.setString(14, (String)obj.get("phone_number"));

                if((String)obj.get("email") == null)
                    preparedStmt.setNull(15, Types.VARCHAR);
                else
                    preparedStmt.setString(15, (String)obj.get("email"));

                if((String)obj.get("address") == null)
                    preparedStmt.setNull(16, Types.VARCHAR);
                else
                    preparedStmt.setString(16, (String)obj.get("address"));

                if((Integer)obj.get("revenue_ss") == null)
                    preparedStmt.setNull(17, Types.INTEGER);
                else
                    preparedStmt.setInt(17, (int)obj.get("revenue_ss"));


                if((Integer)obj.get("revenue_fw") == null)
                    preparedStmt.setNull(18, Types.INTEGER);
                else
                    preparedStmt.setInt(18, (int)obj.get("revenue_fw"));

                if((String)obj.get("reviews") == null)
                    preparedStmt.setNull(19, Types.VARCHAR);
                else
                    preparedStmt.setString(19,(String)obj.get("reviews"));

                if((String)obj.get("store_name") == null)
                    preparedStmt.setNull(20, Types.VARCHAR);
                else
                    preparedStmt.setString(20,(String)obj.get("store_name"));

                if((String)obj.get("seller_grade") == null)
                    preparedStmt.setNull(21, Types.VARCHAR);
                else
                    preparedStmt.setString(21,(String)obj.get("seller_grade"));


                if((Integer)obj.get("card_value") == null)
                    preparedStmt.setNull(22, Types.INTEGER);
                else
                    preparedStmt.setInt(22,(int)obj.get("card_value"));

                if((String)obj.get("cat_full") == null)
                    preparedStmt.setNull(23, Types.VARCHAR);
                else
                    preparedStmt.setString(23,(String)obj.get("cat_full"));

                if((String)obj.get("cat_big") == null)
                    preparedStmt.setNull(24, Types.VARCHAR);
                else
                    preparedStmt.setString(24,(String)obj.get("cat_big"));


                if((String)obj.get("cat_middle") == null)
                    preparedStmt.setNull(25, Types.VARCHAR);
                else
                    preparedStmt.setString(25,(String)obj.get("cat_middle"));


                if((String)obj.get("cat_small") == null)
                    preparedStmt.setNull(26, Types.VARCHAR);
                else
                    preparedStmt.setString(26,(String)obj.get("cat_small"));


                if((String)obj.get("cat_product") == null)
                    preparedStmt.setNull(27, Types.VARCHAR);
                else
                    preparedStmt.setString(27,(String) obj.get("cat_product"));

                if((String)obj.get("brand") == null)
                    preparedStmt.setNull(28, Types.VARCHAR);
                else
                    preparedStmt.setString(28,(String) obj.get("brand"));

                if((String)obj.get("make_company") == null)
                    preparedStmt.setNull(29, Types.VARCHAR);
                else
                    preparedStmt.setString(29, (String) obj.get("make_company"));

                if((Integer)obj.get("category_comparison") == null)
                    preparedStmt.setNull(30, Types.INTEGER);
                else
                    preparedStmt.setInt(30,(int)obj.get("category_comparison"));

                if((String)obj.get("firstpage_keyword") == null)
                    preparedStmt.setNull(31, Types.VARCHAR);
                else
                    preparedStmt.setString(31,(String) obj.get("firstpage_keyword"));

                preparedStmt.setInt(32,(int)obj.get("id"));

                if((Integer)obj.get("notsell") == null)
                    preparedStmt.setNull(33, Types.INTEGER);
                else
                    preparedStmt.setInt(33, (int)obj.get("notsell"));

                if((Integer)obj.get("interest") == null)
                    preparedStmt.setNull(34, Types.INTEGER);
                else
                    preparedStmt.setInt(34, (int)obj.get("interest"));


                if((String)obj.get("source") == null)
                    preparedStmt.setNull(35, Types.VARCHAR);
                else
                    preparedStmt.setString(35,(String) obj.get("source"));

                if((String)obj.get("buy_url") == null)
                    preparedStmt.setNull(36, Types.VARCHAR);
                else
                    preparedStmt.setString(36,(String) obj.get("buy_url"));

                preparedStmt.execute();

                System.out.println("인서트 성공!");

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlus");
            System.err.println(e.getMessage());
        }
    }


    public void insertNaverLightWithId(Connection conn,PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setString(1, (String)String.valueOf(obj.get("product_no")));

                if((String)obj.get("title") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2, (String)obj.get("title"));


                if((String)obj.get("store_name") == null)
                    preparedStmt.setNull(3, Types.VARCHAR);
                else
                    preparedStmt.setString(3, (String)obj.get("store_name"));

                if((String)obj.get("store_grade") == null)
                    preparedStmt.setNull(4, Types.VARCHAR);
                else
                    preparedStmt.setString(4, (String)obj.get("store_grade"));

                if((Double)obj.get("star") == null)
                    preparedStmt.setNull(5, Types.DOUBLE);
                else
                    preparedStmt.setDouble(5, (Double)obj.get("star"));


                if((String)obj.get("product_img") == null)
                    preparedStmt.setNull(6, Types.VARCHAR);
                else
                    preparedStmt.setString(6, (String)obj.get("product_img"));


                if((String)obj.get("product_url") == null)
                    preparedStmt.setNull(7, Types.VARCHAR);
                else
                    preparedStmt.setString(7, (String)obj.get("product_url"));


                if((Timestamp)obj.get("insert_time") == null)
                    preparedStmt.setNull(8, Types.TIMESTAMP);
                else
                    preparedStmt.setTimestamp(8, (Timestamp)obj.get("insert_time"));



                if((Integer)obj.get("insert_timestamp") == null)
                    preparedStmt.setNull(9, Types.INTEGER);
                else
                    preparedStmt.setInt(9, (Integer)obj.get("insert_timestamp"));


                if((Integer)obj.get("register_timestamp") == null)
                    preparedStmt.setNull(10, Types.INTEGER);
                else
                    preparedStmt.setInt(10, (Integer)obj.get("register_timestamp"));


                if((String)obj.get("register_date") == null)
                    preparedStmt.setNull(11, Types.VARCHAR);
                else
                    preparedStmt.setString(11, (String)obj.get("register_date"));


                if((String)obj.get("cat_full") == null)
                    preparedStmt.setNull(12, Types.VARCHAR);
                else
                    preparedStmt.setString(12, (String)obj.get("cat_full"));


                if((String)obj.get("cat_big") == null)
                    preparedStmt.setNull(13, Types.VARCHAR);
                else
                    preparedStmt.setString(13, (String)obj.get("cat_big"));

                if((String)obj.get("cat_middle") == null)
                    preparedStmt.setNull(14, Types.VARCHAR);
                else
                    preparedStmt.setString(14, (String)obj.get("cat_middle"));

                if((String)obj.get("cat_small") == null)
                    preparedStmt.setNull(15, Types.VARCHAR);
                else
                    preparedStmt.setString(15, (String)obj.get("cat_small"));

                if((String)obj.get("cat_product") == null)
                    preparedStmt.setNull(16, Types.VARCHAR);
                else
                    preparedStmt.setString(16, (String)obj.get("cat_product"));

                if((Integer)obj.get("count_review") == null)
                    preparedStmt.setNull(17, Types.INTEGER);
                else
                    preparedStmt.setInt(17, (Integer)obj.get("count_review"));


                if((Integer)obj.get("count_zzim") == null)
                    preparedStmt.setNull(18, Types.INTEGER);
                else
                    preparedStmt.setInt(18, (Integer)obj.get("count_zzim"));


                if((Integer)obj.get("count_buy") == null)
                    preparedStmt.setNull(19, Types.INTEGER);
                else
                    preparedStmt.setInt(19, (Integer)obj.get("count_buy"));

                if((Integer)obj.get("price") == null)
                    preparedStmt.setNull(20, Types.INTEGER);
                else
                    preparedStmt.setInt(20, (Integer)obj.get("price"));


                if((Long)obj.get("pricexcount_review") == null)
                    preparedStmt.setNull(21, Types.INTEGER);
                else
                    preparedStmt.setLong(21, (Long)obj.get("pricexcount_review"));



                if((String)obj.get("firstpage_keyword") == null)
                    preparedStmt.setNull(22, Types.VARCHAR);
                else
                    preparedStmt.setString(22, (String)obj.get("firstpage_keyword"));



                if((Integer)obj.get("category_comparison") == null)
                    preparedStmt.setNull(23, Types.INTEGER);
                else
                    preparedStmt.setInt(23, (Integer)obj.get("category_comparison"));


                if((Integer)obj.get("deletes") == null)
                    preparedStmt.setNull(24, Types.INTEGER);
                else
                    preparedStmt.setInt(24, (Integer)obj.get("deletes"));


                if((Timestamp)obj.get("user_update_timestamp") == null)
                    preparedStmt.setNull(25, Types.TIMESTAMP);
                else
                    preparedStmt.setTimestamp(25, (Timestamp) obj.get("user_update_timestamp"));


                if((Timestamp)obj.get("datalab_update_timestamp") == null)
                    preparedStmt.setNull(26, Types.TIMESTAMP);
                else
                    preparedStmt.setTimestamp(26, (Timestamp) obj.get("datalab_update_timestamp"));



                preparedStmt.setInt(27, (Integer)obj.get("id"));


                preparedStmt.execute();

                System.out.println("인서트 성공!");

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlus");
            //System.err.println(e.getMessage());
        }
    }



    public void insertNaverUsersWithId(Connection conn,PreparedStatement preparedStmt , JSONObject obj)
    {

        try

        {
//preparedStmt.setNull(5, Types.VARCHAR);
            try {

                preparedStmt.setString(1, (String)String.valueOf(obj.get("user_id")));

                if((String)obj.get("nickname") == null)
                    preparedStmt.setNull(2, Types.VARCHAR);
                else
                    preparedStmt.setString(2, (String)obj.get("nickname"));

                if((String)obj.get("review_date") == null)
                    preparedStmt.setNull(3, Types.VARCHAR);
                else
                    preparedStmt.setString(3, (String)obj.get("review_date"));

                if((String)obj.get("review_image") == null)
                    preparedStmt.setNull(4, Types.VARCHAR);
                else
                    preparedStmt.setString(4, (String)obj.get("review_image"));

                if((String)obj.get("review_text") == null)
                    preparedStmt.setNull(5, Types.VARCHAR);
                else
                    preparedStmt.setString(5, (String)obj.get("review_text"));


                if((String)obj.get("review_option") == null)
                    preparedStmt.setNull(6, Types.VARCHAR);
                else
                    preparedStmt.setString(6, (String)obj.get("review_option"));


                if((String)obj.get("review_star") == null)
                    preparedStmt.setNull(7, Types.VARCHAR);
                else
                    preparedStmt.setString(7, (String)obj.get("review_star"));


                if((String)obj.get("product_id") == null)
                    preparedStmt.setNull(8, Types.VARCHAR);
                else
                    preparedStmt.setString(8, (String)obj.get("product_id"));



                if((String)obj.get("product_title") == null)
                    preparedStmt.setNull(9, Types.VARCHAR);
                else
                    preparedStmt.setString(9, (String)obj.get("product_title"));


                if((String)obj.get("product_image") == null)
                    preparedStmt.setNull(10, Types.VARCHAR);
                else
                    preparedStmt.setString(10, (String)obj.get("product_image"));


                if((String)obj.get("product_category") == null)
                    preparedStmt.setNull(11, Types.VARCHAR);
                else
                    preparedStmt.setString(11, (String)obj.get("product_category"));


                if((Integer)obj.get("product_price") == null)
                    preparedStmt.setNull(12, Types.INTEGER);
                else
                    preparedStmt.setInt(12, (Integer)obj.get("product_price"));

                if((String)obj.get("cat_big") == null)
                    preparedStmt.setNull(13, Types.VARCHAR);
                else
                    preparedStmt.setString(13, (String)obj.get("cat_big"));

                if((String)obj.get("cat_middle") == null)
                    preparedStmt.setNull(14, Types.VARCHAR);
                else
                    preparedStmt.setString(14, (String)obj.get("cat_middle"));

                if((String)obj.get("cat_small") == null)
                    preparedStmt.setNull(15, Types.VARCHAR);
                else
                    preparedStmt.setString(15, (String)obj.get("cat_small"));

                if((String)obj.get("cat_product") == null)
                    preparedStmt.setNull(16, Types.VARCHAR);
                else
                    preparedStmt.setString(16, (String)obj.get("cat_product"));

                if((String)obj.get("product_keyword") == null)
                    preparedStmt.setNull(17, Types.VARCHAR);
                else
                    preparedStmt.setString(17, (String)obj.get("product_keyword"));


                if((String)obj.get("product_url") == null)
                    preparedStmt.setNull(18, Types.VARCHAR);
                else
                    preparedStmt.setString(18, (String)obj.get("product_url"));



                if((Timestamp)obj.get("insert_timestamp") == null)
                    preparedStmt.setNull(19, Types.TIMESTAMP);
                else
                    preparedStmt.setTimestamp(19, (Timestamp) obj.get("insert_timestamp"));




                preparedStmt.execute();

                System.out.println("인서트 성공!");

            }catch(Exception eee){
                eee.printStackTrace();

            }


        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlus");
            //System.err.println(e.getMessage());
        }
    }

    public void insertUserStore(Connection conn, String query, String user_id, JSONObject obj)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, user_id);
                preparedStmt.setString(2, obj.toString());

                preparedStmt.execute();

            }catch(Exception eee){

                preparedStmt.close();

                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update user_store set store_data=? where user_id=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, obj.toString());
                        preparedStmt2.setString(2, user_id);
                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공:");

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertUserStore --update!!");
                        System.out.println(e22.getMessage());
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }
            }




        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabPlus");
            System.err.println(e.getMessage());
        }
    }


    public void updateCategoryComparisonList(Connection conn, String query, int lowest_price, int reviews, String google_image_yn, String google_image_search, String img_src, String china_url, String service_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, lowest_price);
                preparedStmt2.setInt(2, reviews);
                preparedStmt2.setString(3, google_image_yn);
                preparedStmt2.setString(4, google_image_search);
                preparedStmt2.setString(5, img_src);
                preparedStmt2.setString(6, china_url);
                preparedStmt2.setString(7, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateCategoryComparisonList");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void   updateDatalabPlusCateogryComparsion(Connection conn, String query, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 -   updateDatalabPlusCateogryComparsion");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void updateDatalabPlusData(Connection conn, String query, String data, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, data);
                preparedStmt2.setString(2, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabPlusData");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }


    public void deleteDatalabPlusData(Connection conn, String query){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.executeUpdate();
                System.out.println("삭제 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - deleteDatalabPlusData");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void deleteUpdateList(Connection conn, String query){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.executeUpdate();
                System.out.println("삭제 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - deleteUpdateList");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateCategoryComparisonListFirstPageKeyword(Connection conn, String query, String keyword, String service_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, keyword);
                preparedStmt2.setString(2, service_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateCategoryComparisonListFirstPageKeyword");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateDatalabPlus(Connection conn, String query, String url, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, url);
                Long longs = Long.parseLong(product_no);
                preparedStmt2.setLong(2, longs);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabPlus");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void updateGrowth (Connection conn, String query, String data, Integer id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, data);
                preparedStmt2.setInt(2, id);
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateGrowth");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateDatalabPlusId(Connection conn, String query, int id, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, id);
                preparedStmt2.setString(2, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabPlusId");
                System.out.println(e22.getMessage());


            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateDatalabPlusSourceSite(Connection conn, String query, String source, double margin_rate, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, source);
                preparedStmt2.setDouble(2, margin_rate);
                preparedStmt2.setString(3,product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + product_no);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabPlusSourceSite");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateDatalabPlusNotSell(Connection conn, String query, int notsell, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, notsell);
                Long longs = Long.parseLong(product_no);
                preparedStmt2.setString(2, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabPlusNotSell");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void updateDatalabClickPriceAvg(Connection conn, String query, int click, int price, int click_median, int price_median, int click_1st, int price_1st, int click_3rd, int price_3rd, String sk_id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, click);
                preparedStmt2.setInt(2, price);
                preparedStmt2.setInt(3, click_median);
                preparedStmt2.setInt(4, price_median);
                preparedStmt2.setInt(5, click_1st);
                preparedStmt2.setInt(6, price_1st);
                preparedStmt2.setInt(7, click_3rd);
                preparedStmt2.setInt(8, price_3rd);
                preparedStmt2.setString(9, sk_id);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabClickPriceAvg");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }


    public void updateDatalabImageUrl(Connection conn, String query, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabImageUrl");
                System.out.println(e22.getMessage());
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public Boolean updateNaverById(Connection conn, String query, int delete, int id){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2222222 = conn.prepareStatement(query);

            try {
                //preparedStmt2.setInt(1, id);
                preparedStmt2222222.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaver");
            }
            preparedStmt2222222.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

        return true;

    }
    public void updateNaver(Connection conn, String query, int delete, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, delete);
                preparedStmt2.setString(2, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaver");
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void updateNaver2(Connection conn, String query, int delete, String product_no){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setInt(1, delete);
                preparedStmt2.setString(2, product_no);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:");

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaver");
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void update1pageCatalog(Connection conn, String query, Long catalog_size, String category, String keyword){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setLong(1, catalog_size);
                preparedStmt2.setString(2, category);
                preparedStmt2.setString(3, keyword);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + keyword);

            } catch (Exception e22) {
                System.err.println("예외 발생 - update1pageCatalog");
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }

    public void updateNaverLightFirstPage(Connection conn, String query, String keyword, String product_url){
        try {
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {
                preparedStmt2.setString(1, keyword);
                preparedStmt2.setString(2, product_url);
                // execute the java preparedstatement
                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + keyword);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaverReviewFirstPage");
            }
            preparedStmt2.close();

        } catch (Exception er) {
            er.printStackTrace();
        }

    }
    public void insertNaverReview(Connection conn, String query, String product_no, String cat1,String cat2,String cat3,String cat4,String data_review,int review_count, String register_date, Long register_timestamp) {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                preparedStmt.setString(1, product_no);
                preparedStmt.setString(2, cat1);
                preparedStmt.setString(3, cat2);
                preparedStmt.setString(4, cat3);
                if (cat4 == null)
                    preparedStmt.setNull(5, Types.VARCHAR);
                else
                    preparedStmt.setString(5, cat4);
                if (data_review == null)
                    preparedStmt.setNull(6, Types.LONGVARCHAR);
                else
                    preparedStmt.setString(6, data_review);
                preparedStmt.setInt(7, review_count);

                if(register_date == null)
                    preparedStmt.setNull(8, Types.VARCHAR);
                else
                    preparedStmt.setString(8, register_date);

                if(register_timestamp == null)
                    preparedStmt.setNull(9,Types.INTEGER);
                else
                    preparedStmt.setLong(9, register_timestamp);

                // execute the preparedstatement
                preparedStmt.execute();

                preparedStmt.close();

                System.out.println("저장 성공:" + product_no);

            } catch (SQLIntegrityConstraintViolationException eee) {
                //중복
                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update naver_review set data_review=?,review_count=? where product_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {
                        preparedStmt2.setString(1, data_review);
                        preparedStmt2.setInt(2, review_count);
                        preparedStmt2.setString(3, product_no);
                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공:" + product_no);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertNaverReview");
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            } catch (Exception e) {
                System.err.println("예외 발생 - insertNaverReview");
                System.err.println(e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("예외 발생 - insertNaverReview");
            System.err.println(e.getMessage());
        }
    }




    public void insertUserCard(Connection conn, String query, int user_id, String product_no, int is_open, int is_save, int is_store, Long card_value)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                preparedStmt.setInt(1, user_id);
                preparedStmt.setString(2, product_no);
                preparedStmt.setInt(3, is_open);
                preparedStmt.setInt(4, is_save);
                preparedStmt.setInt(5, is_store);
                preparedStmt.setLong(6, card_value);

                // execute the preparedstatement
                preparedStmt.execute();

            }catch(Exception eee){
                System.out.println(eee.getMessage());


            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertUserCard");
            System.err.println(e.getMessage());
        }
    }
    public void updateNaverReview(Connection conn, String query2, String review, String product_no){

        try {
            //String query2 = "update naver_light set count_review=?, count_buy=?, count_zzim=?, insert_timestamp=? where product_no=?";
            PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

            try {

                preparedStmt2.setString(1, review);
                preparedStmt2.setString(2, product_no);

                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + product_no);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaverReview");
            }
            preparedStmt2.close();
        }catch(Exception erer){
            System.out.println(erer.getMessage());
        }
    }


    public Boolean insertProductInfoOfNaverLight(Connection conn, String query, String product_no, String product_url, String product_img, int price, String cat_full, String cat_big, String cat_middle, String cat_small, String cat_product, int count_review, int count_buy, int count_zzim,Long insert_timestamp, String register_date, Long register_timestamp, String title, String store_name, String store_grade, Double star, int category_comparison, String docid, String parentid,String normhit, String similar_image_cnt, String keep_cnt, String isExceptedBest100, String is_brand, String is_hotdeal, String search_keyword, String open_date)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                preparedStmt.setString(1, product_no);
                preparedStmt.setString(2, product_url);
                preparedStmt.setString(3, product_img);
                preparedStmt.setInt(4, price);

                preparedStmt.setString(5, cat_full);
                preparedStmt.setString(6, cat_big);
                preparedStmt.setString(7, cat_middle);
                preparedStmt.setString(8, cat_small);
                preparedStmt.setString(9, cat_product);

                if(count_review == -1)
                    preparedStmt.setNull(10,Types.INTEGER);
                else
                    preparedStmt.setInt(10,count_review);
                if(count_buy == -1)
                    preparedStmt.setNull(11, Types.INTEGER);
                else
                    preparedStmt.setInt(11,count_buy);
                if(count_zzim == -1)
                    preparedStmt.setNull(12, Types.INTEGER);
                else
                    preparedStmt.setInt(12,count_zzim);

                preparedStmt.setLong(13, insert_timestamp);

                if(register_date == null)
                    preparedStmt.setNull(14, Types.VARCHAR);
                else
                    preparedStmt.setString(14,register_date);

                if(register_timestamp == -1)
                    preparedStmt.setNull(15, Types.INTEGER);
                else
                    preparedStmt.setLong(15,register_timestamp);

                preparedStmt.setString(16,title);
                preparedStmt.setString(17,store_name);
                preparedStmt.setString(18,store_grade);
                if(star == -1.0)
                    preparedStmt.setNull(19,Types.DOUBLE);
                else
                    preparedStmt.setDouble(19,star);

                preparedStmt.setInt(20,category_comparison);

                preparedStmt.setString(21,docid);
                preparedStmt.setString(22,parentid);
                preparedStmt.setString(23,normhit);
                preparedStmt.setString(24,similar_image_cnt);
                preparedStmt.setString(25,keep_cnt);
                preparedStmt.setString(26,isExceptedBest100);
                preparedStmt.setString(27,is_brand);
                preparedStmt.setString(28,is_hotdeal);
                preparedStmt.setString(29,search_keyword);
                preparedStmt.setString(30,open_date);






                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("저장 성공!:" + product_no);

            }catch(Exception eee){


                System.out.println(eee.getMessage());
                try {
                    String query2 = "update naver_light set count_review=?, count_buy=?, count_zzim=?, cat_full=?, cat_big=?, cat_middle=?, cat_small=?, cat_product=?, insert_timestamp=?, product_url=?, title=?, store_name=?, store_grade=?, star=?, category_comparison=?, docid=?, parentid=?,normhit=?,similar_image_cnt=?, keep_cnt=?, isExceptedBest100=?, is_brand=?,is_hotdeal=?, search_keyword=?, open_date=? where product_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {

                        if(count_review == -1)
                            preparedStmt2.setNull(1,Types.INTEGER);
                        else
                            preparedStmt2.setInt(1,count_review);


                        if(count_buy == -1)
                            preparedStmt2.setNull(2, Types.INTEGER);
                        else
                            preparedStmt2.setInt(2,count_buy);


                        if(count_zzim == -1)
                            preparedStmt2.setNull(3, Types.INTEGER);
                        else
                            preparedStmt2.setInt(3,count_zzim);

                        preparedStmt2.setString(4, cat_full);
                        preparedStmt2.setString(5, cat_big);
                        preparedStmt2.setString(6, cat_middle);
                        preparedStmt2.setString(7, cat_small);
                        preparedStmt2.setString(8, cat_product);

                        preparedStmt2.setLong(9, insert_timestamp);
                        preparedStmt2.setString(10, product_url);
                        preparedStmt2.setString(11, title);
                        preparedStmt2.setString(12, store_name);
                        preparedStmt2.setString(13,store_grade);
                        if(star == -1.0)
                            preparedStmt2.setNull(14,Types.DOUBLE);
                        else
                            preparedStmt2.setDouble(14,star);



                        preparedStmt2.setInt(15,category_comparison);


                        preparedStmt2.setString(16,docid);
                        preparedStmt2.setString(17,parentid);
                        preparedStmt2.setString(18,normhit);
                        preparedStmt2.setString(19,similar_image_cnt);
                        preparedStmt2.setString(20,keep_cnt);
                        preparedStmt2.setString(21,isExceptedBest100);
                        preparedStmt2.setString(22,is_brand);
                        preparedStmt2.setString(23,is_hotdeal);
                        preparedStmt2.setString(24,search_keyword);
                        preparedStmt2.setString(25,open_date);







                        preparedStmt2.setString(26, product_no);


                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(naver-light):" + product_no);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - updateNaver");
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertProductInfoOfNaverLight");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }

    public ArrayList<String> selectSNSData(Connection conn, String query)
    {


        ArrayList<String> arr = new ArrayList<>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                JSONObject obj = new JSONObject();
                String data = rs.getString("data");
                arr.add(data);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectSNSData");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public Boolean insertYoutube(Connection conn, String query, String keyword, String category, int youtube_view_count, double avg_view, long delta, long days, String data)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, keyword);
                preparedStmt.setString(2, category);

                if(youtube_view_count == -1)
                    preparedStmt.setNull(3,Types.INTEGER);
                else
                    preparedStmt.setInt(3, youtube_view_count);

                if(avg_view == -1)
                    preparedStmt.setNull(4,Types.DOUBLE);
                else
                    preparedStmt.setDouble(4, avg_view);

                preparedStmt.setLong(5, delta);
                preparedStmt.setLong(6, days);

                preparedStmt.setString(7, data);

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("insertSNS 저장 성공:" +keyword);

            }catch(Exception eee){

                try {
                    String query2 = "update sns set category=?, youtube_view_count=?, avg_view=?, youtube_delta=?, youtube_delta_days=?, data=? where keyword=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {


                        preparedStmt2.setString(1, category);
                        if(youtube_view_count == -1)
                            preparedStmt2.setNull(2,Types.INTEGER);
                        else
                            preparedStmt2.setInt(2, youtube_view_count);

                        if(avg_view == -1)
                            preparedStmt2.setNull(3,Types.DOUBLE);
                        else
                            preparedStmt2.setDouble(3, avg_view);


                        preparedStmt2.setLong(4, delta);
                        preparedStmt2.setLong(5, days);


                        preparedStmt2.setString(6, data);
                        preparedStmt2.setString(7 , keyword);


                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertSNS):"  + keyword);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertYoutube");
                        e22.printStackTrace();
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordHashtag");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }
    public Boolean insertInstagram(Connection conn, String query, String keyword, String category, int instagram_post_count, double avg_post, long instagram_delta, long instagram_delta_days, String data)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, keyword);
                preparedStmt.setString(2, category);

                if(instagram_post_count == -1)
                    preparedStmt.setNull(3,Types.INTEGER);
                else
                    preparedStmt.setInt(3, instagram_post_count);

                if(avg_post == -1)
                    preparedStmt.setNull(4,Types.DOUBLE);
                else
                    preparedStmt.setDouble(4, avg_post);


                preparedStmt.setLong(5, instagram_delta);

                preparedStmt.setLong(6, instagram_delta_days);

                preparedStmt.setString(7, data);

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("insertSNS 저장 성공:" +keyword);

            }catch(Exception eee){

                try {
                    String query2 = "update sns set category=?, instagram_post_count=?, avg_post=?, instagram_delta=?, instagram_delta_days=?, data=? where keyword=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {


                        preparedStmt2.setString(1, category);
                        if(instagram_post_count == -1)
                            preparedStmt2.setNull(2,Types.INTEGER);
                        else
                            preparedStmt2.setInt(2, instagram_post_count);



                        if(avg_post == -1)
                            preparedStmt2.setNull(3,Types.DOUBLE);
                        else
                            preparedStmt2.setDouble(3, avg_post);


                        preparedStmt2.setLong(4, instagram_delta);
                        preparedStmt2.setLong(5, instagram_delta_days);

                        preparedStmt2.setString(6, data);
                        preparedStmt2.setString(7 , keyword);
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertSNS):"  + keyword);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertKeywordHashtag");
                        e22.printStackTrace();
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordHashtag");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }
    public Boolean insertFacebook(Connection conn, String query, String keyword, String category, int facebook_like_count, double avg_like, long facebook_delta, long facebook_delta_days, String data)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, keyword);
                preparedStmt.setString(2, category);

                if(facebook_like_count == -1)
                    preparedStmt.setNull(3,Types.INTEGER);
                else
                    preparedStmt.setInt(3, facebook_like_count);

                if(avg_like == -1)
                    preparedStmt.setNull(4,Types.DOUBLE);
                else
                    preparedStmt.setDouble(4, avg_like);

                preparedStmt.setLong(5, facebook_delta);

                preparedStmt.setLong(6, facebook_delta_days);

                preparedStmt.setString(7, data);

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("insertSNS 저장 성공:" +keyword);

            }catch(Exception eee){

                try {
                    String query2 = "update sns set category=?, facebook_like_count=?, avg_like=?, facebook_delta=?, facebook_delta_days=?, data=? where keyword=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {


                        preparedStmt2.setString(1, category);
                        if(facebook_like_count == -1)
                            preparedStmt2.setNull(2,Types.INTEGER);
                        else
                            preparedStmt2.setInt(2, facebook_like_count);

                        if(avg_like == -1)
                            preparedStmt2.setNull(3,Types.DOUBLE);
                        else
                            preparedStmt2.setDouble(3, avg_like);

                        preparedStmt2.setLong(4, facebook_delta);
                        preparedStmt2.setLong(5, facebook_delta_days);

                        preparedStmt2.setString(6, data);
                        preparedStmt2.setString(7 , keyword);
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertSNS):"  + keyword);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertKeywordHashtag");
                        e22.printStackTrace();
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordHashtag");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }

    public Boolean insertNaverBlog(Connection conn, String query, String keyword, String category, int naver_blog_count, double avg_blog, long naver_delta, long naver_delta_days, String data)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());
                preparedStmt.setString(1, keyword);
                preparedStmt.setString(2, category);

                if(naver_blog_count == -1)
                    preparedStmt.setNull(3,Types.INTEGER);
                else
                    preparedStmt.setInt(3, naver_blog_count);

                if(avg_blog == -1)
                    preparedStmt.setNull(4,Types.DOUBLE);
                else
                    preparedStmt.setDouble(4, avg_blog);

                preparedStmt.setLong(5, naver_delta);

                preparedStmt.setLong(6, naver_delta_days);

                preparedStmt.setString(7, data);

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("insertSNS 저장 성공:" +keyword);

            }catch(Exception eee){

                try {
                    String query2 = "update sns set category=?, naver_blog_count=?, avg_blog=?, naver_delta=?, naver_delta_days=?, data=? where keyword=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {


                        preparedStmt2.setString(1, category);
                        if(naver_blog_count == -1)
                            preparedStmt2.setNull(2,Types.INTEGER);
                        else
                            preparedStmt2.setInt(2, naver_blog_count);

                        if(avg_blog == -1)
                            preparedStmt2.setNull(3,Types.DOUBLE);
                        else
                            preparedStmt2.setDouble(3, avg_blog);

                        preparedStmt2.setLong(4, naver_delta);
                        preparedStmt2.setLong(5, naver_delta_days);

                        preparedStmt2.setString(6, data);
                        preparedStmt2.setString(7 , keyword);
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertSNS):"  + keyword);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - insertKeywordHashtag");
                        e22.printStackTrace();
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordHashtag");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }


    public Boolean insertKeywordHashtag(Connection conn, String query, String keyword_product_no, String keyword, String product_no, String product_name, String product_url,String store_name, int count_review, int price, long pricexcount_review, String cat_big, String cat_middle, String cat_small, String cat_product, String product_img, int rank, int total_cnt)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                preparedStmt.setString(1, keyword_product_no);
                preparedStmt.setString(2, keyword);
                preparedStmt.setString(3, product_no);
                preparedStmt.setString(4, product_name);

                preparedStmt.setString(5, product_url);
                preparedStmt.setString(6, store_name);

                if(count_review == -1) {
                    preparedStmt.setNull(7, Types.INTEGER);
                }
                else {
                    preparedStmt.setInt(7, count_review);
                }

                preparedStmt.setInt(8, price);

                if(count_review == -1) {
                    preparedStmt.setNull(9, Types.INTEGER);
                }
                else {
                    preparedStmt.setLong(9, pricexcount_review);
                }

                preparedStmt.setString(10,cat_big);
                preparedStmt.setString(11,cat_middle);
                preparedStmt.setString(12,cat_small);
                preparedStmt.setString(13,cat_product);
                preparedStmt.setString(14,product_img);
                preparedStmt.setInt(15,rank);
                preparedStmt.setInt(16,total_cnt);
                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("해시태그 저장 성공:" + product_no);

            }catch(Exception eee){


                //eee.printStackTrace();
                System.out.println(String.valueOf(pricexcount_review));
                System.out.println(eee.getMessage());

                try {
                    String query2 = "update keyword_hashtag set keyword=?, product_no=?, title=?, product_url=?, store_name=?, count_review=?, price=?, pricexcount_review=?, cat_big=?, cat_middle=?, cat_small=?, cat_product=?, product_img=?, ranking=?, total_cnt=? where keyword_product_no=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {

                        preparedStmt2.setString(1, keyword);
                        preparedStmt2.setString(2, product_no);
                        preparedStmt2.setString(3, product_name);

                        preparedStmt2.setString(4, product_url);
                        preparedStmt2.setString(5, store_name);

                        if(count_review == -1)
                            preparedStmt2.setNull(6,Types.INTEGER);
                        else
                            preparedStmt2.setInt(6, count_review);
                        preparedStmt2.setInt(7, price);
                        if(count_review == -1)
                            preparedStmt2.setNull(8,Types.INTEGER);
                        else
                            preparedStmt2.setLong(8,pricexcount_review);
                        preparedStmt2.setString(9,cat_big);
                        preparedStmt2.setString(10,cat_middle);
                        preparedStmt2.setString(11,cat_small);
                        preparedStmt2.setString(12,cat_product);
                        preparedStmt2.setString(13,product_img);
                        preparedStmt2.setInt(14,rank);
                        preparedStmt2.setInt(15, total_cnt);
                        preparedStmt2.setString(16,keyword_product_no);
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(keyword-hashtag):" + keyword_product_no);

                    } catch (Exception e22) {
                        e22.printStackTrace();
                        System.err.println("예외 발생 - insertKeywordHashtag");
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println("insertKeywordHashtag  " + erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertKeywordHashtag");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }

    public Boolean insertNaverStore(Connection conn, String query, JSONObject obj)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);


            try {
                preparedStmt.setString(1, (String)obj.get("store_id"));
                preparedStmt.setString(2, (String)obj.get("store_name"));
                preparedStmt.setString(3, (String)obj.get("store_url_name"));
                preparedStmt.setString(4, (String)obj.get("store_desc"));
                preparedStmt.setString(5, (String)obj.get("store_grade"));
                preparedStmt.setString(6, (String)obj.get("talk_id"));
                preparedStmt.setString(7, (String)obj.get("address"));
                preparedStmt.setString(8, (String)obj.get("register_number"));
                preparedStmt.setString(9, (String)obj.get("tongsin_number"));
                preparedStmt.setString(10, (String)obj.get("logo_image"));
                preparedStmt.setString(11, (String)obj.get("goodservice"));
                preparedStmt.setInt(12, (Integer)obj.get("product_count"));
                preparedStmt.setString(13, (String)obj.get("store_url"));
                try {

                    if((Integer)obj.get("revenue") == -1)
                        preparedStmt.setNull(14,Types.INTEGER);
                    else
                        preparedStmt.setInt(14, (Integer)obj.get("revenue"));

                    if((Integer)obj.get("review_count") == -1)
                        preparedStmt.setNull(15,Types.INTEGER);
                    else
                        preparedStmt.setInt(15, (Integer)obj.get("review_count"));


                    if((Integer)obj.get("visit_count") == -1)
                        preparedStmt.setNull(16,Types.INTEGER);
                    else
                        preparedStmt.setInt(16, (Integer)obj.get("visit_count"));

                    if((Integer)obj.get("zzim_count") == -1)
                        preparedStmt.setNull(17,Types.INTEGER);
                    else
                        preparedStmt.setInt(17, (Integer)obj.get("zzim_count"));

                    if((String)obj.get("best10_image") == null)
                        preparedStmt.setNull(18,Types.VARCHAR);
                    else
                        preparedStmt.setString(18, (String)obj.get("best10_image"));

                }catch(Exception ex) {

                    ex.printStackTrace();

                }

                preparedStmt.setString (19, obj.toString());

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("스토어 저장 성공:" + (String)obj.get("store_name"));

            }catch(Exception eee){


                //eee.printStackTrace();
                System.out.println(eee.getMessage());

                try {
                    String query2 = "update naver_store set store_name=?, store_desc=?, store_grade=?, talk_id=?, address=?, register_number=?, tongsin_number=?, logo_image=?, goodservice=?, product_count=?, store_url=?, revenue=?, review_count=?, visit_count=?, zzim_count=?, best10_image=?, data=? where store_url_name=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {

                        preparedStmt2.setString(1, (String)obj.get("store_name"));
                        preparedStmt2.setString(2, (String)obj.get("store_desc"));
                        preparedStmt2.setString(3, (String)obj.get("store_grade"));
                        preparedStmt2.setString(4, (String)obj.get("talk_id"));
                        preparedStmt2.setString(5, (String)obj.get("address"));
                        preparedStmt2.setString(6, (String)obj.get("register_number"));
                        preparedStmt2.setString(7, (String)obj.get("tongsin_number"));
                        preparedStmt2.setString(8, (String)obj.get("logo_image"));
                        preparedStmt2.setString(9, (String)obj.get("goodservice"));
                        preparedStmt2.setInt(10, (Integer)obj.get("product_count"));
                        preparedStmt2.setString(11, (String)obj.get("store_url"));


                        if((Integer)obj.get("revenue") == -1)
                            preparedStmt2.setNull(12,Types.INTEGER);
                        else
                            preparedStmt2.setInt(12, (Integer)obj.get("revenue"));

                        if((Integer)obj.get("review_count") == -1)
                            preparedStmt2.setNull(13,Types.INTEGER);
                        else
                            preparedStmt2.setInt(13, (Integer)obj.get("review_count"));


                        if((Integer)obj.get("visit_count") == -1)
                            preparedStmt2.setNull(14,Types.INTEGER);
                        else
                            preparedStmt2.setInt(14, (Integer)obj.get("visit_count"));

                        if((Integer)obj.get("zzim_count") == -1)
                            preparedStmt2.setNull(15,Types.INTEGER);
                        else
                            preparedStmt2.setInt(15, (Integer)obj.get("zzim_count"));

                        if((String)obj.get("best10_image") == null)
                            preparedStmt2.setNull(16,Types.VARCHAR);
                        else
                            preparedStmt2.setString(16, (String)obj.get("best10_image"));

                        preparedStmt2.setString (17, obj.toString());

                        preparedStmt2.setString(18, (String)obj.get("store_url_name"));

                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertNaverStore):" + (String)obj.get("store_name"));

                    } catch (Exception e22) {
                        e22.printStackTrace();
                        System.err.println("예외 발생 - insertNaverStore");
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertNaverStore");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }

    public Boolean insertNaverStore2(Connection conn, PreparedStatement preparedStmt, JSONObject obj)
    {

        Boolean rs = true;

        try

        {

            try {
                preparedStmt.setString(1, (String)obj.get("store_id"));
                preparedStmt.setString(2, (String)obj.get("store_name"));
                preparedStmt.setString(3, (String)obj.get("store_url_name"));
                preparedStmt.setString(4, (String)obj.get("store_desc"));
                preparedStmt.setString(5, (String)obj.get("store_grade"));
                preparedStmt.setString(6, (String)obj.get("talk_id"));
                preparedStmt.setString(7, (String)obj.get("address"));
                preparedStmt.setString(8, (String)obj.get("register_number"));
                preparedStmt.setString(9, (String)obj.get("tongsin_number"));
                preparedStmt.setString(10, (String)obj.get("logo_image"));
                preparedStmt.setString(11, (String)obj.get("goodservice"));
                preparedStmt.setInt(12, (Integer)obj.get("product_count"));
                preparedStmt.setString(13, (String)obj.get("store_url"));
                try {

                    if((Integer)obj.get("revenue") == -1)
                        preparedStmt.setNull(14,Types.INTEGER);
                    else
                        preparedStmt.setInt(14, (Integer)obj.get("revenue"));

                    if((Integer)obj.get("review_count") == -1)
                        preparedStmt.setNull(15,Types.INTEGER);
                    else
                        preparedStmt.setInt(15, (Integer)obj.get("review_count"));


                    if((Integer)obj.get("visit_count") == -1)
                        preparedStmt.setNull(16,Types.INTEGER);
                    else
                        preparedStmt.setInt(16, (Integer)obj.get("visit_count"));

                    if((Integer)obj.get("zzim_count") == -1)
                        preparedStmt.setNull(17,Types.INTEGER);
                    else
                        preparedStmt.setInt(17, (Integer)obj.get("zzim_count"));

                    if((String)obj.get("best10_image") == null)
                        preparedStmt.setNull(18,Types.VARCHAR);
                    else
                        preparedStmt.setString(18, (String)obj.get("best10_image"));

                }catch(Exception ex) {

                    ex.printStackTrace();

                }

                preparedStmt.setString (19, obj.toString());

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("스토어 저장 성공:" + (String)obj.get("store_name"));

            }catch(Exception eee){


                //eee.printStackTrace();
                System.out.println(eee.getMessage());

                try {
                    String query2 = "update naver_store set store_name=?, store_desc=?, store_grade=?, talk_id=?, address=?, register_number=?, tongsin_number=?, logo_image=?, goodservice=?, product_count=?, store_url=?, revenue=?, review_count=?, visit_count=?, zzim_count=?, best10_image=?, data=? where store_url_name=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {

                        preparedStmt2.setString(1, (String)obj.get("store_name"));
                        preparedStmt2.setString(2, (String)obj.get("store_desc"));
                        preparedStmt2.setString(3, (String)obj.get("store_grade"));
                        preparedStmt2.setString(4, (String)obj.get("talk_id"));
                        preparedStmt2.setString(5, (String)obj.get("address"));
                        preparedStmt2.setString(6, (String)obj.get("register_number"));
                        preparedStmt2.setString(7, (String)obj.get("tongsin_number"));
                        preparedStmt2.setString(8, (String)obj.get("logo_image"));
                        preparedStmt2.setString(9, (String)obj.get("goodservice"));
                        preparedStmt2.setInt(10, (Integer)obj.get("product_count"));
                        preparedStmt2.setString(11, (String)obj.get("store_url"));


                        if((Integer)obj.get("revenue") == -1)
                            preparedStmt2.setNull(12,Types.INTEGER);
                        else
                            preparedStmt2.setInt(12, (Integer)obj.get("revenue"));

                        if((Integer)obj.get("review_count") == -1)
                            preparedStmt2.setNull(13,Types.INTEGER);
                        else
                            preparedStmt2.setInt(13, (Integer)obj.get("review_count"));


                        if((Integer)obj.get("visit_count") == -1)
                            preparedStmt2.setNull(14,Types.INTEGER);
                        else
                            preparedStmt2.setInt(14, (Integer)obj.get("visit_count"));

                        if((Integer)obj.get("zzim_count") == -1)
                            preparedStmt2.setNull(15,Types.INTEGER);
                        else
                            preparedStmt2.setInt(15, (Integer)obj.get("zzim_count"));

                        if((String)obj.get("best10_image") == null)
                            preparedStmt2.setNull(16,Types.VARCHAR);
                        else
                            preparedStmt2.setString(16, (String)obj.get("best10_image"));

                        preparedStmt2.setString (17, obj.toString());

                        preparedStmt2.setString(18, (String)obj.get("store_url_name"));

                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertNaverStore):" + (String)obj.get("store_name"));

                    } catch (Exception e22) {
                        e22.printStackTrace();
                        System.err.println("예외 발생 - insertNaverStore");
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertNaverStore");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }



    public Boolean insertNaverStore3(Connection conn, PreparedStatement preparedStmt, JSONObject obj)
    {

        Boolean rs = true;

        try

        {

            try {
                preparedStmt.setString(1, (String)obj.get("store_id"));
                preparedStmt.setString(2, (String)obj.get("store_name"));
                preparedStmt.setString(3, (String)obj.get("store_url_name"));
                preparedStmt.setString(4, (String)obj.get("store_desc"));
                preparedStmt.setString(5, (String)obj.get("store_grade"));
                preparedStmt.setString(6, (String)obj.get("talk_id"));
                preparedStmt.setString(7, (String)obj.get("address"));
                preparedStmt.setString(8, (String)obj.get("register_number"));
                preparedStmt.setString(9, (String)obj.get("tongsin_number"));
                preparedStmt.setString(10, (String)obj.get("logo_image"));
                preparedStmt.setString(11, (String)obj.get("goodservice"));
                preparedStmt.setInt(12, (Integer)obj.get("product_count"));
                preparedStmt.setString(13, (String)obj.get("store_url"));
                try {

                    if((Integer)obj.get("revenue") == -1)
                        preparedStmt.setNull(14,Types.INTEGER);
                    else
                        preparedStmt.setInt(14, (Integer)obj.get("revenue"));

                    if((Integer)obj.get("review_count") == -1)
                        preparedStmt.setNull(15,Types.INTEGER);
                    else
                        preparedStmt.setInt(15, (Integer)obj.get("review_count"));


                    if((Integer)obj.get("visit_count") == -1)
                        preparedStmt.setNull(16,Types.INTEGER);
                    else
                        preparedStmt.setInt(16, (Integer)obj.get("visit_count"));

                    if((Integer)obj.get("zzim_count") == -1)
                        preparedStmt.setNull(17,Types.INTEGER);
                    else
                        preparedStmt.setInt(17, (Integer)obj.get("zzim_count"));

                    if((String)obj.get("best10_image") == null)
                        preparedStmt.setNull(18,Types.VARCHAR);
                    else
                        preparedStmt.setString(18, (String)obj.get("best10_image"));

                }catch(Exception ex) {

                    ex.printStackTrace();

                }

                preparedStmt.setString (19, obj.toString());

                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("스토어 저장 성공:" + (String)obj.get("store_name"));

            }catch(Exception eee){


                //eee.printStackTrace();
                System.out.println(eee.getMessage());

                try {
                    String query2 = "update naver_store set store_name=?, store_desc=?, store_grade=?, talk_id=?, address=?, register_number=?, tongsin_number=?, logo_image=?, goodservice=?, product_count=?, store_url=?, revenue=?, review_count=?, visit_count=?, zzim_count=?, best10_image=?, data=? where store_url_name=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {

                        preparedStmt2.setString(1, (String)obj.get("store_name"));
                        preparedStmt2.setString(2, (String)obj.get("store_desc"));
                        preparedStmt2.setString(3, (String)obj.get("store_grade"));
                        preparedStmt2.setString(4, (String)obj.get("talk_id"));
                        preparedStmt2.setString(5, (String)obj.get("address"));
                        preparedStmt2.setString(6, (String)obj.get("register_number"));
                        preparedStmt2.setString(7, (String)obj.get("tongsin_number"));
                        preparedStmt2.setString(8, (String)obj.get("logo_image"));
                        preparedStmt2.setString(9, (String)obj.get("goodservice"));
                        preparedStmt2.setInt(10, (Integer)obj.get("product_count"));
                        preparedStmt2.setString(11, (String)obj.get("store_url"));


                        if((Integer)obj.get("revenue") == -1)
                            preparedStmt2.setNull(12,Types.INTEGER);
                        else
                            preparedStmt2.setInt(12, (Integer)obj.get("revenue"));

                        if((Integer)obj.get("review_count") == -1)
                            preparedStmt2.setNull(13,Types.INTEGER);
                        else
                            preparedStmt2.setInt(13, (Integer)obj.get("review_count"));


                        if((Integer)obj.get("visit_count") == -1)
                            preparedStmt2.setNull(14,Types.INTEGER);
                        else
                            preparedStmt2.setInt(14, (Integer)obj.get("visit_count"));

                        if((Integer)obj.get("zzim_count") == -1)
                            preparedStmt2.setNull(15,Types.INTEGER);
                        else
                            preparedStmt2.setInt(15, (Integer)obj.get("zzim_count"));

                        if((String)obj.get("best10_image") == null)
                            preparedStmt2.setNull(16,Types.VARCHAR);
                        else
                            preparedStmt2.setString(16, (String)obj.get("best10_image"));

                        preparedStmt2.setString (17, obj.toString());

                        preparedStmt2.setString(18, (String)obj.get("store_url_name"));

                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공(insertNaverStore):" + (String)obj.get("store_name"));

                    } catch (Exception e22) {
                        e22.printStackTrace();
                        System.err.println("예외 발생 - insertNaverStore");
                    }
                    preparedStmt2.close();
                }catch(Exception erer){
                    System.out.println(erer.getMessage());
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertNaverStore");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }


    public Boolean insertUserInfo(Connection conn, String query, HashMap<String,String> user_data)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                preparedStmt.setString(1,  user_data.get("cat_small") +user_data.get("nickname"));
                preparedStmt.setString(2, user_data.get("product_no"));
                preparedStmt.setString(3, user_data.get("nickname"));
                preparedStmt.setString(4, user_data.get("review_date"));
                preparedStmt.setString(5, user_data.get("review_image"));
                preparedStmt.setString(6, user_data.get("review_text"));
                preparedStmt.setString(7,user_data.get("review_option"));
                preparedStmt.setString(8, user_data.get("review_star"));
                preparedStmt.setString(9,user_data.get("product_title"));
                preparedStmt.setString(10,user_data.get("product_image"));
                preparedStmt.setString(11,user_data.get("product_category"));
                preparedStmt.setString(12,user_data.get("cat_big"));
                preparedStmt.setString(13,user_data.get("cat_middle"));
                preparedStmt.setString(14,user_data.get("cat_small"));
                preparedStmt.setString(15,user_data.get("cat_product"));
                preparedStmt.setString(16,user_data.get("product_keyword"));
                preparedStmt.setInt(17,Integer.parseInt(user_data.get("price")));
                preparedStmt.setString(18,user_data.get("product_url"));


                // execute the preparedstatement
                preparedStmt.execute();
                System.out.println("저장 성공:" + user_data.get("nickname"));

            }catch(Exception eee){
                System.err.println("예외 발생 - insertUserInfo");
                System.err.println(eee.getMessage());
                rs =false;

            }

            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertUserInfo");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }
    public void updateNaverReview(Connection conn, String query, String data_review, int count_review, String product_no){

        try {
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {

                if (data_review == null)
                    preparedStmt2.setNull(1, Types.INTEGER);
                else
                    preparedStmt2.setString(1, data_review);


                if (count_review == -1)
                    preparedStmt2.setNull(2, Types.INTEGER);
                else
                    preparedStmt2.setInt(2, count_review);

                preparedStmt2.setString(3, product_no);

                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + product_no);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateNaverReview");
            }
        }catch(Exception er){

        }
    }

    public void updateDatalabTimestamp(Connection conn, String query, Timestamp time, String product_no){

        try {
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {

                java.util.Date date = new java.util.Date();
                java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
                preparedStmt2.setTimestamp(1, time);
                preparedStmt2.setString(2,product_no);

                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + timestamp);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabTimestamp");
            }
        }catch(Exception er){

        }
    }

    public void updateDatalabTimestampWithDelete(Connection conn, String query, Timestamp time, int deletes, int id){

        try {
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {

                java.util.Date date = new java.util.Date();
                java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
                preparedStmt2.setTimestamp(1, time);
                preparedStmt2.setInt(2, deletes);
                preparedStmt2.setInt(3,id);

                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + timestamp);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabTimestampWithDelete");
            }
        }catch(Exception er){

        }
    }
    public void updateDatalabTimestampWithDelete(Connection conn, String query, Timestamp time, int deletes, String product_no){

        try {
            PreparedStatement preparedStmt2 = conn.prepareStatement(query);

            try {

                java.util.Date date = new java.util.Date();
                java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
                preparedStmt2.setTimestamp(1, time);
                preparedStmt2.setInt(2, deletes);
                preparedStmt2.setString(3,product_no);

                preparedStmt2.executeUpdate();
                System.out.println("업데이트 성공:" + timestamp);

            } catch (Exception e22) {
                System.err.println("예외 발생 - updateDatalabTimestampWithDelete");
            }
        }catch(Exception er){

        }
    }
    public Boolean insertProductInfoOfNaver(Connection conn, String query, NaverProductDetailClass<String,Integer,Double,Long,Timestamp> pd)
    {

        Boolean rs = true;

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());

                String sk_id = now_time + "," + pd.product_no;

                preparedStmt.setString(1, sk_id);
                preparedStmt.setString(2, pd.product_no);
                preparedStmt.setString(3, pd.product_url);
                preparedStmt.setString(4,pd.product_img);

                preparedStmt.setString(5, pd.cat_big);
                preparedStmt.setString(6, pd.cat_middle);
                preparedStmt.setString(7, pd.cat_small);
                preparedStmt.setString(8, pd.cat_product);
                preparedStmt.setString(9,pd.title);
                preparedStmt.setString(10,pd.title_sub);

                preparedStmt.setDouble(11,pd.star_avg);
                preparedStmt.setDouble(12,pd.star_5);
                preparedStmt.setDouble(13,pd.star_4);
                preparedStmt.setDouble(14,pd.star_3);
                preparedStmt.setDouble(15,pd.star_2);
                preparedStmt.setDouble(16,pd.star_1);

                preparedStmt.setInt(17,pd.count_review);
                preparedStmt.setInt(18,pd.photo_review);
                preparedStmt.setInt(19,pd.count_like);
                preparedStmt.setInt(20,pd.count_qna);

                preparedStmt.setString(21,pd.store_name);
                preparedStmt.setString(22,pd.store_company);
                preparedStmt.setString(23,pd.store_email);
                preparedStmt.setString(24,pd.store_address);
                preparedStmt.setString(25,pd.store_phone);
                preparedStmt.setString(26,pd.store_url);


                preparedStmt.setDouble(27,pd.store_star);
                preparedStmt.setString(28,pd.made_country);
                preparedStmt.setString(29,pd.halbu);
                preparedStmt.setString(30,pd.info_flag);
                preparedStmt.setInt(31,pd.discount);
                preparedStmt.setInt(32,pd.price);

                preparedStmt.setInt(33,pd.store_zzim);
                preparedStmt.setInt(34,pd.toktok_friends);

                preparedStmt.setString(35, pd.register_date);

                preparedStmt.setString(36, pd.cat_full);
                preparedStmt.setLong(37, pd.insert_timestamp);
                preparedStmt.setLong(38, pd.register_timestamp);


                // execute the preparedstatement
                preparedStmt.execute();

            }catch(Exception eee){
                System.out.println(eee.getMessage());
                rs= false;

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertProductInfoOfNaver");
            System.err.println(e.getMessage());
            rs =false;
        }
        return rs;
    }



    public void insertDatalabTrend(Connection conn, String query, String big_cat,String middle_cat, String small_cat, String product_cat, Double mobile, Double pc, Double male, Double female,Double age10,Double age20,Double age30,Double age40,Double age50,Double age60,String data_m,String data_3m, String data_total,String keywords_m,String keywords_3m, String keywords_total)
    {

        try

        {
            PreparedStatement preparedStmt = conn.prepareStatement(query);

            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh");
                Calendar cal = Calendar.getInstance();
                String now_time = dateFormat.format(cal.getTime());


                String sk_id = (big_cat + "," + middle_cat + "," + small_cat);
                if(product_cat != null){
                    sk_id += ",";
                    sk_id += product_cat;
                }
                preparedStmt.setString(1, sk_id);
                preparedStmt.setString(2, big_cat);
                preparedStmt.setString(3, middle_cat);
                preparedStmt.setString(4, small_cat);
                preparedStmt.setString(5, product_cat);
                preparedStmt.setDouble(6, mobile);
                preparedStmt.setDouble(7, pc);
                preparedStmt.setDouble(8, male);
                preparedStmt.setDouble(9, female);
                preparedStmt.setDouble(10, age10);
                preparedStmt.setDouble(11, age20);
                preparedStmt.setDouble(12, age30);
                preparedStmt.setDouble(13, age40);
                preparedStmt.setDouble(14, age50);
                preparedStmt.setDouble(15, age60);
                preparedStmt.setString(16, data_m);
                preparedStmt.setString(17, data_3m);
                preparedStmt.setString(18, data_total);
                preparedStmt.setString(19, keywords_m);
                preparedStmt.setString(20, keywords_3m);
                preparedStmt.setString(21, keywords_total);


                // execute the preparedstatement
                preparedStmt.execute();

            }catch(Exception eee){
                try {
                    // create the mysql insert preparedstatement
                    String query2 = "update datalab_insight set mobile=?,pc=?,male=?,female=?,age10=?,age20=?,age30=?,age40=?,age50=?,age60=?,data_1m=?,data_3m=?,data_total=?,keyword_1m=?,keyword_3m=?,keyword_total=? where sk_id=?";
                    PreparedStatement preparedStmt2 = conn.prepareStatement(query2);

                    try {


                        String sk_id = (big_cat + "," + middle_cat + "," + small_cat);
                        if(product_cat != null){
                            sk_id += ",";
                            sk_id += product_cat;
                        }

                        preparedStmt2.setDouble(1, mobile);
                        preparedStmt2.setDouble(2, pc);
                        preparedStmt2.setDouble(3, male);
                        preparedStmt2.setDouble(4, female);
                        preparedStmt2.setDouble(5, age10);
                        preparedStmt2.setDouble(6, age20);
                        preparedStmt2.setDouble(7, age30);
                        preparedStmt2.setDouble(8, age40);
                        preparedStmt2.setDouble(9, age50);
                        preparedStmt2.setDouble(10, age60);
                        preparedStmt2.setString(11, data_m);
                        preparedStmt2.setString(12, data_3m);
                        preparedStmt2.setString(13, data_total);
                        preparedStmt2.setString(14, keywords_m);
                        preparedStmt2.setString(15, keywords_3m);
                        preparedStmt2.setString(16, keywords_total);
                        preparedStmt2.setString(17, sk_id);
                        // execute the java preparedstatement
                        preparedStmt2.executeUpdate();
                        System.out.println("업데이트 성공" + sk_id);

                    } catch (Exception e22) {
                        System.err.println("예외 발생 - updateNaver");
                    }
                    preparedStmt2.close();

                } catch (Exception er) {
                    er.printStackTrace();
                }

            }


            preparedStmt.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - insertDatalabTrend");
            System.err.println(e.getMessage());
        }
    }
    public ArrayList<HashMap<String,String>> selectDatalab(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String sk_id = rs.getString("sk_id");
                String data_total = rs.getString("data_total");
                String keyword_total = rs.getString("keyword_total");
                String click_300_avg = rs.getString("click_300_avg");
                String price_300_avg = rs.getString("price_300_avg");
                String click_300_median = rs.getString("click_300_median");
                String price_300_median = rs.getString("price_300_median");
                String click_300_1st = rs.getString("click_300_1st");
                String price_300_1st = rs.getString("price_300_1st");
                String click_300_3rd = rs.getString("click_300_3rd");
                String price_300_3rd = rs.getString("price_300_3rd");

                String big_cat = rs.getString("big_cat");
                String middle_cat = rs.getString("middle_cat");
                String small_cat = rs.getString("small_cat");
                String product_cat = rs.getString("product_cat");

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("sk_id", sk_id);
                hs.put("data_total", data_total);
                hs.put("keyword_total",keyword_total);
                hs.put("click_300_avg",click_300_avg);
                hs.put("price_300_avg", price_300_avg);
                hs.put("price_300_median", price_300_median);
                hs.put("click_300_median", click_300_median);
                hs.put("price_300_1st", price_300_1st);
                hs.put("click_300_1st", click_300_1st);
                hs.put("price_300_3rd", price_300_3rd);
                hs.put("click_300_3rd", click_300_3rd);
                hs.put("big_cat", big_cat);
                hs.put("middle_cat", middle_cat);
                hs.put("small_cat", small_cat);
                hs.put("product_cat", product_cat);


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalab");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<String> selectDatalabKeywords(Connection conn, String query)
    {

        String keyword_total = "";
        ArrayList<String> arr = new  ArrayList<String>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
               keyword_total = rs.getString("keyword_total");
               arr.add(keyword_total);



            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabKeywords");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public int selectDatalabCount(Connection conn, String query)
    {

        int count = 0;


        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                count = rs.getInt("count(*)");


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalabCount");
            System.err.println(e.getMessage());
        }
        return count;
    }


    public ArrayList<HashMap<String,String>> selectDatalabAll(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String sk_id = rs.getString("sk_id");
                String data_total = rs.getString("data_total");
                String keyword_total = rs.getString("keyword_total");
                String click_300_avg = rs.getString("click_300_avg");
                String price_300_avg = rs.getString("price_300_avg");
                String click_300_median = rs.getString("click_300_median");
                String price_300_median = rs.getString("price_300_median");
                String click_300_1st = rs.getString("click_300_1st");
                String price_300_1st = rs.getString("price_300_1st");
                String click_300_3rd = rs.getString("click_300_3rd");
                String price_300_3rd = rs.getString("price_300_3rd");
                String big_cat = rs.getString("big_cat");
                String middle_cat = rs.getString("middle_cat");
                String small_cat = rs.getString("small_cat");
                String product_cat = rs.getString("product_cat");



                Double mobile = rs.getDouble("mobile");
                Double pc = rs.getDouble("pc");
                Double male = rs.getDouble("male");
                Double female = rs.getDouble("female");
                Double age10 = rs.getDouble("age10");
                Double age20 = rs.getDouble("age20");
                Double age30 = rs.getDouble("age30");
                Double age40 = rs.getDouble("age40");
                Double age50 = rs.getDouble("age50");
                Double age60 = rs.getDouble("age60");




                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("sk_id", sk_id);
                hs.put("data_total", data_total);
                hs.put("keyword_total",keyword_total);
                hs.put("click_300_avg",click_300_avg);
                hs.put("price_300_avg", price_300_avg);
                hs.put("price_300_median", price_300_median);
                hs.put("click_300_median", click_300_median);
                hs.put("price_300_1st", price_300_1st);
                hs.put("click_300_1st", click_300_1st);
                hs.put("price_300_3rd", price_300_3rd);
                hs.put("click_300_3rd", click_300_3rd);
                hs.put("big_cat", big_cat);
                hs.put("middle_cat", middle_cat);
                hs.put("small_cat", small_cat);
                hs.put("product_cat", product_cat);


                hs.put("mobile", String.valueOf(mobile));
                hs.put("male", String.valueOf(male));
                hs.put("female", String.valueOf(female));
                hs.put("pc", String.valueOf(pc));
                hs.put("mobile", String.valueOf(mobile));
                hs.put("age10", String.valueOf(age10));
                hs.put("age20", String.valueOf(age20));
                hs.put("age30", String.valueOf(age30));
                hs.put("age40", String.valueOf(age40));
                hs.put("age50", String.valueOf(age50));
                hs.put("age60", String.valueOf(age60));


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectDatalab");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public JSONObject selectNaverStoreTotal(Connection conn, Statement st, ResultSet rs , JSONObject obj, String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        //JSONObject obj = new JSONObject();

        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {

                obj.put("store_id",  rs.getString("store_id"));
                obj.put("store_name", rs.getString("store_name"));
                obj.put("store_url_name", rs.getString("store_url_name"));
                obj.put("store_desc", rs.getString("store_desc"));
                obj.put("store_grade", rs.getString("store_grade"));
                obj.put("talk_id", rs.getString("talk_id"));
                obj.put("address", rs.getString("address"));
                obj.put("register_number", rs.getString("register_number"));
                obj.put("tongsin_number", rs.getString("tongsin_number"));
                obj.put("logo_image", rs.getString("logo_image"));
                obj.put("goodservice", rs.getString("goodservice"));
                obj.put("product_count", rs.getInt("product_count"));
                obj.put("store_url", rs.getString("store_url"));


                if ( (Integer) rs.getInt("revenue") == -1)
                    obj.put("revenue", null);
                else
                    obj.put("revenue",  rs.getInt("revenue"));

                if ((Integer)  rs.getInt("review_count") == -1)
                    obj.put("review_count", null);
                else
                    obj.put("review_count", rs.getInt("review_count"));


                if ((Integer) rs.getInt("visit_count") == -1)
                    obj.put("visit_count", null);
                else
                    obj.put("visit_count", rs.getInt("visit_count"));

                if ((Integer) rs.getInt("zzim_count")== -1)
                    obj.put("zzim_count", null);
                else
                    obj.put("zzim_count", rs.getInt("zzim_count"));

                if ((String) rs.getString("best10_image") == null)
                    obj.put("best10_image", null);
                else
                    obj.put("best10_image", rs.getString("best10_image"));
            }

            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverStoreTotal");
            System.err.println(e.getMessage());
        }
        return obj;

    }


    public ArrayList<HashMap<String,String>> selectNaverUsers(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                int review_id = rs.getInt("review_id");
                String user_id = rs.getString("user_id");
                String nickname = rs.getString("nickname");
                String review_date = rs.getString("review_date");
                String review_star = rs.getString("review_star");
                String product_id = rs.getString("product_id");
                String product_title = rs.getString("product_title");
                String product_image = rs.getString("product_image");
                String product_category = rs.getString("product_category");
                int product_price = rs.getInt("product_price");
                String product_keyword = rs.getString("product_keyword");
                String product_url = rs.getString("product_url");

                int count = rs.getInt("count");


                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("user_id", user_id);
                hs.put("nickname", nickname);
                hs.put("review_date",review_date);
                hs.put("review_star",review_star);
                hs.put("product_id", product_id);
                hs.put("product_title", product_title);
                hs.put("product_image", product_image);
                hs.put("product_price", String.valueOf(product_price));
                hs.put("product_category", product_category);
                hs.put("product_keyword", product_keyword);
                hs.put("product_url", product_url);
                hs.put("count",String.valueOf(count));


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverUsers");
            System.err.println(e.getMessage());
        }
        return arr;
    }



    public JSONObject selectNaverUsersForRecovery(Connection conn, Statement st, ResultSet rs , JSONObject obj, String query)
    {


        //ArrayList<JSONObject> arr = new  ArrayList<JSONObject>();
        //JSONObject obj = new JSONObject();

        try
        {

            // create the java statement
            //Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                obj.put("review_id", rs.getInt("review_id"));
                obj.put("user_id", rs.getString("user_id"));
                obj.put("nickname", rs.getString("nickname"));
                obj.put("review_date", rs.getString("review_date"));
                if(rs.wasNull()) {
                    obj.put("review_date", null);
                }
                obj.put("review_image", rs.getString("review_image"));
                if(rs.wasNull()) {
                    obj.put("review_image", null);
                }
                obj.put("review_text", rs.getString("review_text"));
                if(rs.wasNull()) {
                    obj.put("review_text", null);
                }
                obj.put("review_option", rs.getString("review_option"));
                if(rs.wasNull()) {
                    obj.put("review_option", null);
                }
                obj.put("review_star", rs.getString("review_star"));
                if(rs.wasNull()) {
                    obj.put("review_star", null);
                }
                obj.put("product_id", rs.getString("product_id"));

                obj.put("product_title", rs.getString("product_title"));
                if(rs.wasNull()) {
                    obj.put("product_title", null);
                }
                obj.put("product_image", rs.getString("product_image"));
                if(rs.wasNull()) {
                    obj.put("product_image", null);
                }
                obj.put("product_category", rs.getString("product_category"));
                if(rs.wasNull()) {
                    obj.put("product_category", null);
                }
                obj.put("product_price", rs.getInt("product_price"));
                if(rs.wasNull()) {
                    obj.put("product_price", null);
                }
                obj.put("cat_big", rs.getString("cat_big"));
                if(rs.wasNull()) {
                    obj.put("cat_big", null);
                }
                obj.put("cat_middle", rs.getString("cat_middle"));
                if(rs.wasNull()) {
                    obj.put("cat_middle", null);
                }
                obj.put("cat_small", rs.getString("cat_small"));
                if(rs.wasNull()) {
                    obj.put("cat_small", null);
                }
                obj.put("cat_product", rs.getString("cat_product"));
                if(rs.wasNull()) {
                    obj.put("cat_product", null);
                }
                obj.put("product_keyword", rs.getString("product_keyword"));
                if(rs.wasNull()) {
                    obj.put("product_keyword", null);
                }
                obj.put("product_url", rs.getString("product_url"));
                if(rs.wasNull()) {
                    obj.put("product_url", null);
                }

                obj.put("insert_timestamp", rs.getTimestamp("insert_timestamp"));
                if(rs.wasNull()) {
                    obj.put("insert_timestamp", null);
                }

            }
            // st.close();
            rs.close();

        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverUsersForRecovery");
            System.err.println(e.getMessage());
        }
        return obj;

    }







    public ArrayList<HashMap<String,String>> selectNaverUsersOnlyUserid(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                String user_id = rs.getString("user_id");

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("user_id", user_id);


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverUsersOnlyUserid");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectKeywordHashtag(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String keyword_product_no = rs.getString("keyword_product_no");
                String keyword = rs.getString("keyword");
                String product_no = rs.getString("product_no");
                String title = rs.getString("title");
                String product_url = rs.getString("product_url");
                String store_name = rs.getString("store_name");
                String count_review = rs.getString("count_review");
                String price = rs.getString("price");
                String pricexcount_review = rs.getString("pricexcount_review");
                String cat_big = rs.getString("cat_big");
                String cat_middle = rs.getString("cat_middle");
                String cat_small = rs.getString("cat_small");
                String cat_product = rs.getString("cat_product");
                String insert_time = rs.getString("insert_time");
                String product_img = rs.getString("product_img");
                Timestamp insert_times = rs.getTimestamp("insert_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String time_yyyymmdd = sdf.format(insert_times);

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("keyword_product_no", keyword_product_no);
                hs.put("keyword", keyword);
                hs.put("product_no",product_no);
                hs.put("title",title);
                hs.put("product_url", product_url);
                hs.put("store_name", store_name);
                hs.put("count_review", count_review);
                hs.put("price", price);
                hs.put("pricexcount_review", pricexcount_review);
                hs.put("cat_big", cat_big);
                hs.put("cat_middle", cat_middle);
                hs.put("cat_small", cat_small);
                hs.put("cat_product", cat_product);
                hs.put("insert_time", insert_time);
                hs.put("product_img",product_img);
                hs.put("time_yyyymmdd", time_yyyymmdd);


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordHashtag");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public int selectKeywordHashtagId(Connection conn, String query)
    {


        int count = 0;

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                count = rs.getInt("keyword_hashtag_id");

            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordHashtagId");
            System.err.println(e.getMessage());
        }
        return count;
    }
    public int selectCount(Connection conn, String query)
    {


        int count = 0;

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                 count = rs.getInt("count");

            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordHashtag");
            System.err.println(e.getMessage());
        }
        return count;
    }
    public int selectCountCatMiddle(Connection conn, String query, String category)
    {


        int count = 0;
        String cat_middle = null;

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                cat_middle = rs.getString("cat_middle");
                if(category.equals(cat_middle)) {
                    count = rs.getInt("count");
                }

            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectCountCatMiddle");
            System.err.println(e.getMessage());
        }
        return count;
    }
    public ArrayList<HashMap<String,String>> selectKeywordHashtagOnlyKeyword(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String keyword = rs.getString("keyword");

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("keyword", keyword);


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordHashtag");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<HashMap<String,String>> selectKeywordDup(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String keyword = rs.getString("keyword");
                String insert_time = rs.getString("insert_time");
                String seller_data = rs.getString("seller_data");
                int seller_count = rs.getInt("seller_count");

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("keyword", keyword);
                hs.put("insert_time", insert_time);
                hs.put("seller_data",seller_data);
                hs.put("seller_count",String.valueOf(seller_count));


                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectKeywordDup");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList<HashMap<String,String>> selectProductData(Connection conn, String query)
    {


        ArrayList<HashMap<String,String>> arr = new  ArrayList<HashMap<String,String>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                String product_url = rs.getString("product_url");
                String cat_big = rs.getString("cat_big");
                String cat_middle = rs.getString("cat_middle");
                String cat_small = rs.getString("cat_small");
                String cat_product = rs.getString("cat_product");

                HashMap<String,String> hs = new HashMap<String,String>();
                hs.put("cat_big", cat_big);
                hs.put("cat_middle", cat_middle);
                hs.put("cat_small", cat_small);
                hs.put("cat_product", cat_product);
                hs.put("product_url", product_url);
                arr.add(hs);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectProductData");
            System.err.println(e.getMessage());
        }
        return arr;
    }


    public ArrayList selectNaver(Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass> arr = new  ArrayList<NaverProductDetailClass>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();

            // iterate through the java resultset
            while (rs.next())
            {
                cpc.cat_big= rs.getString("cat_big");
                cpc.cat_middle = rs.getString("cat_middle");
                cpc.cat_small = rs.getString("cat_small");
                cpc.cat_product = rs.getString("cat_product");
                cpc.count_like = rs.getInt("count_like");
                cpc.count_qna = rs.getInt("count_qna");
                cpc.count_review = rs.getInt("count_review");
                cpc.discount = rs.getInt("discount");
                cpc.halbu = rs.getString("halbu");
                cpc.info_flag = rs.getString("info_flag");
                cpc.limit_time = rs.getInt("limit_time");
                cpc.product_no = rs.getString("product_no");
                cpc.product_img = rs.getString("product_img");
                cpc.product_url = rs.getString("product_url");
                cpc.title = rs.getString("title");
                cpc.title_sub = rs.getString("title_sub");
                cpc.price = rs.getInt("price");


                cpc.star_avg = rs.getDouble("star_avg");
                cpc.star_5 = rs.getDouble("star_5");
                cpc.star_4 = rs.getDouble("star_4");
                cpc.star_3 = rs.getDouble("star_3");
                cpc.star_3 = rs.getDouble("star_2");
                cpc.star_3 = rs.getDouble("star_1");

                cpc.store_name = rs.getString("store_name");
                cpc.store_company = rs.getString("store_company");
                cpc.store_email = rs.getString("store_email");
                cpc.store_address = rs.getString("store_address");
                cpc.store_phone = rs.getString("store_phone");
                cpc.store_url = rs.getString("store_url");
                cpc.store_star = rs.getDouble("store_star");
                cpc.made_country = rs.getString("made_country");

                cpc.setInsertTIme(rs.getString("insert_time"));
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectProductData");
            System.err.println(e.getMessage());
        }
        return arr;
    }

    public ArrayList selectNaverLightComparisonKeyword(Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass> arr = new  ArrayList<NaverProductDetailClass>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();
                cpc.category_comparison = rs.getInt("category_comparison");
                cpc.firstpage_keyword = rs.getString("firstpage_keyword");
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverLightComparison");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> selectNaverLight (Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> arr = new  ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();
                cpc.cat_big= rs.getString("cat_big");
                cpc.cat_middle = rs.getString("cat_middle");
                cpc.cat_small = rs.getString("cat_small");
                cpc.cat_product = rs.getString("cat_product");

                cpc.count_review = rs.getInt("count_review");

                cpc.product_no = rs.getString("product_no");
                cpc.product_img = rs.getString("product_img");
                cpc.product_url = rs.getString("product_url");
                cpc.title = rs.getString("title");
                cpc.price = rs.getInt("price");
                cpc.store_name = rs.getString("store_name");

                cpc.price = rs.getInt("price");
                cpc.data_review = rs.getString("data_review");
                cpc.user_update_timestamp = rs.getString("user_update_timestamp");


                cpc.setInsertTIme(rs.getString("insert_time"));
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectProductData");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public int selectRowNumber(Connection conn, String query)
    {

        int rownum= 0;
        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                rownum= rs.getInt("rownum");


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverLightUrl");
            System.err.println(e.getMessage());
        }
        return rownum;
    }
    public ArrayList selectDatalabPlusTemp(Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass> arr = new  ArrayList<NaverProductDetailClass>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();
                cpc.product_url = rs.getString("url");
                cpc.product_no =rs.getString("product_no");
                cpc.category_comparison = rs.getInt("category_comparison");
                cpc.setInsertTIme(rs.getString("insert_time"));
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverLightUrl");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList selectNaverLightUrl(Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass> arr = new  ArrayList<NaverProductDetailClass>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();
                cpc.product_url = rs.getString("product_url");
                cpc.product_no =rs.getString("product_no");
                cpc.category_comparison = rs.getInt("category_comparison");
                cpc.datalab_update_timestamp = rs.getTimestamp("datalab_update_timestamp");
                cpc.deletes = rs.getInt("deletes");
                cpc.setInsertTIme(rs.getString("insert_time"));
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverLightUrl");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList selectNaverLightUrlId(Connection conn, String query)
    {
        ArrayList<NaverProductDetailClass> arr = new  ArrayList<NaverProductDetailClass>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                NaverProductDetailClass cpc = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();
                cpc.id = rs.getInt("id");
                arr.add(cpc);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectNaverLightUrl");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public int selectCacheId(Connection conn, String query)
    {

        int id = 0;
        try
        {

            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                id = rs.getInt("id");


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectCacheId");
            System.err.println(e.getMessage());
        }
        return id;
    }
    public ArrayList<Integer> selectCacheIds(Connection conn, String query)
    {

        ArrayList<Integer> arr = new ArrayList<>();


        try
        {

            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {

                arr.add(rs.getInt("id"));


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectCacheId");
            System.err.println(e.getMessage());
        }
        return arr;
    }
    public ArrayList selectNaverProductNo(Connection conn, String query)
    {
        ArrayList<String> arr = new  ArrayList<String>();

        try
        {
            // create the java statement
            Statement st = conn.createStatement();
            // execute arrthe query, and get a java resultset
            ResultSet rs = st.executeQuery(query);


            // iterate through the java resultset
            while (rs.next())
            {
                String product_no= rs.getString("product_no");
                arr.add(product_no);


            }
            st.close();
            rs.close();
        }
        catch (Exception e)
        {
            System.err.println("예외 발생 - selectProductData");
            System.err.println(e.getMessage());
        }
        return arr;
    }

}
