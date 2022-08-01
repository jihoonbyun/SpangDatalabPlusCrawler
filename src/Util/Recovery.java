package Util;

import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import Connection.MySQLConnector2;

public class Recovery {

    MySQLConnector2 mysql = new MySQLConnector2();


    String master_ip = new Conf().NAVER_DB_IP;
    String slave_ip = new Conf().NAVER_DB_IP_SERVICE;

    Connection master_con = mysql.initConnect(master_ip);
    Connection slave_con = mysql.initConnect(slave_ip);

    public void datalabPlus(String part_number, String total_numbers) {

        try {

            int rownum = mysql.selectRowNumber(master_con, "select max(id) as rownum from datalab_plus");
            //int rownum_SLAVE = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");

            // create the java statement
            Statement st = master_con.createStatement();
            Statement st_slave = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            int master_id= -1;
            int slave_id = -1;


            int slice_counts = Math.round(rownum / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point- slice_counts;
            if(rownum-end_point < slice_counts) {
                end_point = rownum;
            }

            for(int id=starting_point; id < end_point; id++) {

            //for(int id =rownum; id >= 0; id--){


                master_id = mysql.selectIdOnly(master_con, st,rs, "select id from datalab_plus where id=" + id);
                //slave_id = mysql.selectIdOnly(slave_con, st_slave,rs, "select id from datalab_plus_copy where id=" + id);

                //TODO: 잠시마스터로 변경시킴 SLAVE로 바꿔나야 함
                slave_id = mysql.selectIdOnly(master_con, st_slave,rs, "select id from datalab_plus_backup where id=" + id);

                if(slave_id != master_id) {
                    original_data = mysql.selectDatalabPlusTotal2(master_con, st, rs, "select * from datalab_plus where id=" + id);
                    if (original_data.size() > 0) {
                        //TODO: 잠시마스터로 변경시킴 SLAVE로 바꿔나야 함
                        preparedStmt = master_con.prepareStatement("insert into datalab_plus_backup (product_no,title,growth,price,revenue,revenue3m,revenue6m,revenue12m,data,make_country,url, overseas, send_place, phone_number, email, address, revenue_ss, revenue_fw, reviews, store_name, seller_grade, card_value, cat_full, cat_big, cat_middle,cat_small, cat_product, brand, make_company, category_comparison, firstpage_keyword, id, notsell, interest, source, buy_url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        mysql.insertDatalabPlusWithId(slave_con, preparedStmt, original_data);
                        System.out.println("인서트성공"+id);
                    }
                }
                System.out.println(id + " /" + rownum);
            }

            st.close();

            master_con.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }


    //data_review는 oom을 일으키므로 복제하지 않는다.
    public void naverLight() {

        try {

            int rownum = mysql.selectRowNumber(master_con, "select max(id) as rownum from naver_light");
            Statement st = master_con.createStatement();
            Statement st2 = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            int master_id= -1;
            int slave_id = -1;

            JSONObject obj = new JSONObject();

            for(int id =0; id <= rownum; id++){


                master_id = mysql.selectIdOnly(master_con, st,rs, "select id from naver_light where id=" + id);
                slave_id = mysql.selectIdOnly(slave_con, st2,rs, "select id from naver_light2 where id=" + id);


                if(master_id != -1 && slave_id == -1) {
                    obj = new JSONObject();
                    original_data = mysql.selectNaverLightTotal(master_con, st, rs, obj, "select * from naver_light where id=" + id);
                    obj = null;
                if (original_data.size() > 0) {
                    preparedStmt = slave_con.prepareStatement("insert into naver_light2 (product_no, title, store_name, store_grade, star, product_img, product_url, insert_time, insert_timestamp, register_timestamp, register_date, cat_full, cat_big, cat_middle, cat_small, cat_product,count_review, count_zzim, count_buy, price,  pricexcount_review, firstpage_keyword, category_comparison, deletes, user_update_timestamp, datalab_update_timestamp, id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" );
                    mysql.insertNaverLightWithId(slave_con, preparedStmt, original_data);

                }
                }
                System.out.println(id + " /" + rownum);
            }

            st.close();

            master_con.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }



    public void naverStore() {

        try {

            int rownum = mysql.selectRowNumber(master_con, "select max(id) as rownum from naver_store");

            Statement st = master_con.createStatement();
            Statement st2 = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            int master_id= -1;
            int slave_id = -1;

            JSONObject obj = new JSONObject();

            for(int id =rownum; id >=0; id--){


                master_id = mysql.selectIdOnly(master_con, st,rs, "select id from naver_store where id=" + id);
                slave_id = mysql.selectIdOnly(slave_con, st2,rs, "select id from naver_store2 where id=" + id);


                if(master_id != -1 && slave_id == -1) {
                    obj = new JSONObject();
                    original_data = mysql.selectNaverStoreTotal(master_con, st, rs, obj, "select * from naver_store where id=" + id);
                    obj = null;
                    if (original_data.size() > 0) {
                        String insert_query0 = "insert into naver_store2(store_id, store_name, store_url_name, store_desc, store_grade, talk_id, address, register_number, tongsin_number, logo_image, goodservice, product_count, store_url, revenue, review_count, visit_count, zzim_count, best10_image, data) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        preparedStmt = slave_con.prepareStatement(insert_query0);
                        mysql.insertNaverStore2(slave_con, preparedStmt, original_data);

                    }
                }
                System.out.println(id + " /" + rownum);
            }

            st.close();

            master_con.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    public void naverUsers() {

        try {

            int rownum = mysql.selectRowNumber(master_con, "select max(review_id) as rownum from naver_users");

            Statement st = master_con.createStatement();
            Statement st2 = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            int master_id= -1;
            int slave_id = -1;

            JSONObject obj = new JSONObject();

            for(int id =rownum; id >=0; id--){


                master_id = mysql.selectIdOnly(master_con, st,rs, "select review_id as id from naver_users where review_id=" + id);
                slave_id = mysql.selectIdOnly(slave_con, st2,rs, "select review_id as id from naver_users2 where review_id=" + id);


                if(master_id != -1 && slave_id == -1) {
                    obj = new JSONObject();
                    original_data = mysql.selectNaverStoreTotal(master_con, st, rs, obj, "select * from naver_users where id=" + id);
                    obj = null;
                    if (original_data.size() > 0) {
                        String insert_query0 = "insert into naver_users2(user_id, nickname, review_date, review_image, review_text, review_option, review_star, product_no, product_title, product_image, product_category, product_price, cat_big, cat_middle, cat_small, cat_product,product_keyword, product_url, insert_timestamp) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        preparedStmt = slave_con.prepareStatement(insert_query0);
                        mysql.insertNaverStore2(slave_con, preparedStmt, original_data);

                    }
                }
                System.out.println(id + " /" + rownum);
            }

            st.close();

            master_con.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }




    public void hashtagFromSlave() {

        try {

            int rownum = mysql.selectRowNumber(slave_con, "select max(keyword_hashtag_id) as rownum from keyword_hashtag2");

            Statement st = slave_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            for(int id =rownum; id >= 0; id--){

                original_data = mysql.selectKeywordHashTag(slave_con, st, rs, "select * from keyword_hashtag2 where keyword_hashtag_id='" + id + "'");

                preparedStmt = master_con.prepareStatement("replace keyword_hashtag (keyword_hashtag_id,keyword_product_no,cat_middle,product_no,keyword,product_img,title,product_url,store_name,count_review,price,pricexcount_review,cat_big,cat_small,cat_product,ranking,insert_time,search_count,click_count,compete,ad_count,total_cnt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                if(preparedStmt != null && original_data != null) {
                    if(original_data.size() > 0) {
                        mysql.replaceKeywordHashtag(master_con, preparedStmt, original_data);
                    }
                }

                System.out.println(id + " /" + rownum);
            }

            st.close();
            slave_con.close();


        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }


    public void hashtagFromMaster(String part_number, String total_numbers) {

        try {
            int pricexcount_review = 0;

            int rownum = mysql.selectRowNumber(master_con, "select max(keyword_hashtag_id) as rownum from keyword_hashtag");
            //int rownum_SLAVE = mysql.selectRowNumber(slave_con, "select max(id) as rownum from datalab_plus");

            // create the java statement
            Statement st = master_con.createStatement();
            JSONObject original_data = null;
            PreparedStatement preparedStmt = null;
            ResultSet rs = null;

            int slice_counts = Math.round(rownum/ Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point- slice_counts;


            for(int id =starting_point; id <= end_point; id++){


                original_data = mysql.selectKeywordHashTag(master_con, st, rs, "select * from keyword_hashtag where keyword_hashtag_id=" + id);

                preparedStmt = slave_con.prepareStatement("replace into keyword_hashtag1 (keyword_hashtag_id,keyword_product_no,cat_middle,product_no,keyword,product_img,title,product_url,store_name,count_review,price,pricexcount_review,cat_big,cat_small,cat_product,ranking,insert_time,search_count,click_count,compete,ad_count,total_cnt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                if(preparedStmt != null && original_data.size() != 0) {
                    pricexcount_review = (int) original_data.get("price") * (int)original_data.get("count_review");
                    original_data.put("pricexcount_review", pricexcount_review);
                    mysql.replaceKeywordHashtag(slave_con, preparedStmt, original_data);
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
