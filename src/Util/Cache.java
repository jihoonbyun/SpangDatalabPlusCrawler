package Util;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import Connection.MySQLConnector2;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Cache {

    MySQLConnector2 mysql = new MySQLConnector2();
    String slave_ip = Conf.NAVER_DB_IP_SERVICE;
    Connection slave_con = mysql.initConnect(slave_ip);
    String master_ip = Conf.NAVER_DB_IP;
    Connection master_con = mysql.initConnect(master_ip);
    JSONParser parser = new JSONParser();
    String title = null;
    JSONObject raw_data = null;
    int price = -1;
    String image_url = null;
    double season_spring = -1;
    double season_summer= -1;
    double season_fall = -1;
    double season_winter = -1;
    String season = null;
    JSONObject original_data = null;
    PreparedStatement preparedStmt = null;
    ResultSet rs = null;

    //μΊμ1μ revenume3m > 30000 or revenume > 100000000
    public void cache1() {

        try {

            int rownum = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");
            //int rownum_SLAVE = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");

            Statement st = slave_con.createStatement();
            for(int id =0; id <= rownum; id++){
                original_data = mysql.selectDatalabPlusTotal2(slave_con, st, rs, "select * from datalab_plus where id=" + id);
                cache1_func(original_data);
                System.out.println(id + " /" + rownum);
            }
            st.close();
            slave_con.close();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }


    public void cache1_func(JSONObject original_data) {

        try {

            if (original_data.size() > 0) {
                if (original_data.get("cat_big") != null && original_data.get("cat_middle") != null) {
                    original_data.put("cat_mix", original_data.get("cat_big") + ">" + original_data.get("cat_middle"));
                } else {
                    original_data.put("cat_mix", null);
                }
                if ((Double) original_data.get("revenue3m") > 30000000 || (Double) original_data.get("revenue") > 100000000) {

                    if ((Double) original_data.get("revenue") < 100000000000L) {


                        title = (String) original_data.get("title");
                        raw_data = (JSONObject) parser.parse((String) original_data.get("data"));
                        price = (int) Integer.parseInt(((String) raw_data.get("price")).replaceAll("\\,", ""));
                        image_url = (String) raw_data.get("image_url");
                        season_spring = (double) raw_data.get("λ΄");
                        season_summer = (double) raw_data.get("μ¬λ¦");
                        season_fall = (double) raw_data.get("κ°μ");
                        season_winter = (double) raw_data.get("κ²¨μΈ");

                        if (Math.round(season_spring + season_summer) >= 60) {
                            season = "λ΄/μ¬λ¦";
                        }
                        if (Math.round(season_fall + season_winter) >= 60) {
                            season = "κ°μ/κ²¨μΈ";
                        }
                        if (Math.round(season_spring + season_summer) < 60 && Math.round(season_fall + season_winter) < 60) {
                            season = "μ¬κ³μ ";
                        }


                        //ν΄μνκ·Έ λ§λ€κΈ°
                        JSONArray hashtag = new JSONArray();
                        double revenue = (Double) original_data.get("revenue");
                        if(revenue > 9000000 && revenue < 42000000) {
                            hashtag.put("#κ³ λ§€μΆ");
                        }
                        if(revenue >= 42000000) {
                            hashtag.put("#λλ°λ§€μΆ");
                        }

                        double growth = (double) raw_data.get("μνκ· μ±μ₯λ₯ ");
                        if(growth > 1.2) {
                            hashtag.put("#μ±μ₯κ°λ₯μ±");
                        }

                        org.json.simple.JSONArray  monthdata = (org.json.simple.JSONArray) raw_data.get("μλ³λ§€μΆκ·Έλν");
                        int m_int = 0;
                        for(int m=0; m < monthdata.size(); m++) {
                            if((long) monthdata.get(m) >=1000000) {
                                m_int++;
                            }
                        }
                        if(m_int == 12) {
                            hashtag.put("#μ€νλμλ¬");
                        }

                        int revenue3m = Integer.parseInt(((String)raw_data.get("λμ λ§€μΆμ‘3κ°μ")).replaceAll("\\,", ""));
                        if(revenue3m >= 5400000) {
                            hashtag.put("#μ΅κ·Όλ§€μΆμμ");
                        }

                        try {
                            String seller_grade = (String) original_data.get("seller_grade");
                            if (seller_grade.equals("λΉνμ")) {
                                hashtag.put("#λΉνμ");
                            }
                            if (seller_grade.equals("νμ")) {
                                hashtag.put("#νμ");
                            }
                        }catch(Exception ex) {

                        }

                        try {
                            int overseas = (int) original_data.get("overseas");
                            if (overseas == 1) {
                                hashtag.put("#κ΅¬λ§€λν");
                            }
                        }catch(Exception ex){

                        }

                        try {
                            String brand = (String) raw_data.get("λΈλλ");
                            if ((brand == null) || brand.equals("")) {
                                hashtag.put("#λΈλλμμ");
                            }
                        }catch(Exception ex) {}

                        if(season.equals("λ΄/μ¬λ¦") || season.equals("κ°μ/κ²¨μΈ")) {
                            hashtag.put("#μμ¦μν");
                        }

                        try {

                            //κ²½μμ¬
                            int ent = 0;
                            long price_sum = 0;
                            int review_sum = 0;
                            double deliver_sum = 0;
                            double star_sum = 0;
                            long opt_sum = 0;

                                for (int tt = 1; tt < 11; tt++) {
                                    JSONObject enemy = (JSONObject) raw_data.get("κ²½μμνΉμ±" + tt);
                                    price_sum += (long) enemy.get("κ°κ²©");
                                    review_sum += Integer.parseInt((String) enemy.get("λ¦¬λ·°"));
                                    deliver_sum += (double) enemy.get("λ°°μ‘");
                                    star_sum += (double) enemy.get("νμ ");
                                    opt_sum += (long) enemy.get("κ΅¬λ§€μ΅μ");
                                    ent++;
                                }


                            JSONObject this_obj = (JSONObject) raw_data.get("λ³Έμν_νΉμ±");
                            if ((long) this_obj.get("κ°κ²©") > (price_sum / ent)) {
                                hashtag.put("#λμλ¨κ°");
                            }
                            if ((long) this_obj.get("λ¦¬λ·°") > (review_sum / ent)) {
                                hashtag.put("#λ¦¬λ·°λ§μ");
                            }
                            if ((double) this_obj.get("νμ ") > (star_sum / ent)) {
                                hashtag.put("#λ³μ λμ");
                            }
                        }catch(Exception ex) {

                        }

                        String hashtag_str = "";
                        for(int kk=0; kk < hashtag.length(); kk++){
                            hashtag_str += hashtag.get(kk);
                            if(kk != (hashtag.length()-1)) {
                                hashtag_str += " ";
                            }
                        }


                        original_data.put("title", title);
                        original_data.put("price", price);
                        original_data.put("image_url", image_url);
                        original_data.put("season", season);
                        original_data.put("id", original_data.get("id"));
                        original_data.put("hashtag", hashtag_str);


                        preparedStmt = slave_con.prepareStatement("replace into datalab_plus_cache1 (product_no, title, price, season, cat_mix, cat_big, cat_middle,  revenue, revenue3m, image_url, insert_time_datalabplus, datalab_plus_id, hashtag) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        mysql.replaceCache1(slave_con, preparedStmt, original_data);

                    }
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public void cache_title() {

        try {

            int rownum = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");
            //int rownum_SLAVE = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");

            // create the java statement
            Statement st = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            for(int id =0; id <= rownum; id++){

                original_data = mysql.selectDatalabPlusTotal2(slave_con, st, rs, "select * from datalab_plus where id=" + id);

                if(original_data.size() != 0) {
                    preparedStmt = slave_con.prepareStatement("replace into datalab_plus_title (id,product_no,title) values(?,?,?)");
                    mysql.replaceTitle(slave_con, preparedStmt, original_data);
                }


                System.out.println(id + " /" + rownum);
            }

            st.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }



}
