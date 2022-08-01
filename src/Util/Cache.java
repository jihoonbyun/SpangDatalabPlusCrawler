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

    //캐시1은 revenume3m > 30000 or revenume > 100000000
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
                        season_spring = (double) raw_data.get("봄");
                        season_summer = (double) raw_data.get("여름");
                        season_fall = (double) raw_data.get("가을");
                        season_winter = (double) raw_data.get("겨울");

                        if (Math.round(season_spring + season_summer) >= 60) {
                            season = "봄/여름";
                        }
                        if (Math.round(season_fall + season_winter) >= 60) {
                            season = "가을/겨울";
                        }
                        if (Math.round(season_spring + season_summer) < 60 && Math.round(season_fall + season_winter) < 60) {
                            season = "사계절";
                        }


                        //해시태그 만들기
                        JSONArray hashtag = new JSONArray();
                        double revenue = (Double) original_data.get("revenue");
                        if(revenue > 9000000 && revenue < 42000000) {
                            hashtag.put("#고매출");
                        }
                        if(revenue >= 42000000) {
                            hashtag.put("#대박매출");
                        }

                        double growth = (double) raw_data.get("월평균성장률");
                        if(growth > 1.2) {
                            hashtag.put("#성장가능성");
                        }

                        org.json.simple.JSONArray  monthdata = (org.json.simple.JSONArray) raw_data.get("월별매출그래프");
                        int m_int = 0;
                        for(int m=0; m < monthdata.size(); m++) {
                            if((long) monthdata.get(m) >=1000000) {
                                m_int++;
                            }
                        }
                        if(m_int == 12) {
                            hashtag.put("#스테디셀러");
                        }

                        int revenue3m = Integer.parseInt(((String)raw_data.get("누적매출액3개월")).replaceAll("\\,", ""));
                        if(revenue3m >= 5400000) {
                            hashtag.put("#최근매출상위");
                        }

                        try {
                            String seller_grade = (String) original_data.get("seller_grade");
                            if (seller_grade.equals("빅파워")) {
                                hashtag.put("#빅파워");
                            }
                            if (seller_grade.equals("파워")) {
                                hashtag.put("#파워");
                            }
                        }catch(Exception ex) {

                        }

                        try {
                            int overseas = (int) original_data.get("overseas");
                            if (overseas == 1) {
                                hashtag.put("#구매대행");
                            }
                        }catch(Exception ex){

                        }

                        try {
                            String brand = (String) raw_data.get("브랜드");
                            if ((brand == null) || brand.equals("")) {
                                hashtag.put("#브랜드없음");
                            }
                        }catch(Exception ex) {}

                        if(season.equals("봄/여름") || season.equals("가을/겨울")) {
                            hashtag.put("#시즌상품");
                        }

                        try {

                            //경쟁사
                            int ent = 0;
                            long price_sum = 0;
                            int review_sum = 0;
                            double deliver_sum = 0;
                            double star_sum = 0;
                            long opt_sum = 0;

                                for (int tt = 1; tt < 11; tt++) {
                                    JSONObject enemy = (JSONObject) raw_data.get("경쟁자특성" + tt);
                                    price_sum += (long) enemy.get("가격");
                                    review_sum += Integer.parseInt((String) enemy.get("리뷰"));
                                    deliver_sum += (double) enemy.get("배송");
                                    star_sum += (double) enemy.get("평점");
                                    opt_sum += (long) enemy.get("구매옵션");
                                    ent++;
                                }


                            JSONObject this_obj = (JSONObject) raw_data.get("본상품_특성");
                            if ((long) this_obj.get("가격") > (price_sum / ent)) {
                                hashtag.put("#높은단가");
                            }
                            if ((long) this_obj.get("리뷰") > (review_sum / ent)) {
                                hashtag.put("#리뷰많음");
                            }
                            if ((double) this_obj.get("평점") > (star_sum / ent)) {
                                hashtag.put("#별점높음");
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
