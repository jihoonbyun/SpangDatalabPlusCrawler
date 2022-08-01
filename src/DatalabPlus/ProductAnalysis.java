package DatalabPlus;

import Naver.*;
import Connection.MySQLConnector;
import Util.DriverControl;
import Util.Utils;
import Util.Conf;
import Util.Recorder;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ProductAnalysis {

    public static MySQLConnector mysql = new MySQLConnector();
    public static Recorder rec = new Recorder();
    public static NaverReviewScrapper nrs = new NaverReviewScrapper(null);
    public static JSONParser parser = new JSONParser();
    public static final SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    public String DB_IP;
    final NaverShoppingLightScrapper nsls;
    double e = (Math.random() / 10) + 3.55;
    public Boolean isHeadless = false;
    public ChromeDriver driver = null;
    public Date week_ago = null;

    public ProductAnalysis(String DB_IP) {
        this.nsls = new NaverShoppingLightScrapper(DB_IP);
        this.DB_IP = DB_IP;
    }

    /**
     * 데이터랩 키워드 가져와서 네이버 쇼핑에 검색 후 노출 상품 데이터 수집
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할수
     */
    public void updateProcessDatalabPlusFromDatalabKeywords(String part_number, String total_numbers){

        ArrayList<String> keywords_array;
        ArrayList<String> url_list = new ArrayList<>();
        ArrayList<HashMap<String, String>>  urls;
        String target_url;
        driver = DriverControl.getGeneralDriver(isHeadless);
        driver.get("https://shopping.naver.com");


        try {
            Connection conn = mysql.initConnect(DB_IP);
            int total_counts = mysql.selectDatalabCount(conn, "select count(*) from datalab_insight");
            int slice_counts = Math.round(total_counts / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            if(total_counts-end_point < slice_counts) { end_point = total_counts; }
            int starting_point = end_point- slice_counts;slice_counts += 1;
            keywords_array = mysql.selectDatalabKeywords(conn, "select * from datalab_insight limit " + starting_point +"," + slice_counts);conn.close();

            for (int d = 0; d < keywords_array.size(); d++) {
                String keyword_total_str = (String) keywords_array.get(d);
                JSONArray keyword_total = (JSONArray) parser.parse(keyword_total_str);
                for (int k = 0; k < keyword_total.size(); k++) {
                    String keyword = (String) keyword_total.get(k);
                    keyword = Utils.StringReplace(keyword);
                    String shop_url = "https://search.shopping.naver.com/search/all?where=all&frm=NVSCTAB&query=" + keyword;
                    url_list.add(shop_url);
                }
            }


            for(int p=0; p < url_list.size();p++) {
                String list_url = url_list.get(p);
                boolean skip =  false;
                while(true) {
                    try {
                        driver.get(list_url);
                        String location_href = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                        if(location_href.contains("stopit")) { Thread.sleep(5000);driver.get(list_url); }
                        else if(location_href.contains("nidlogin.login")) { skip=true;break; }
                        else { break; }
                    }catch(Exception ex){
                        try {
                            DriverControl.quitDriver(driver);
                            driver = DriverControl.getGeneralDriver(isHeadless);
                            driver.get(list_url);
                        }catch(Exception ex2){
                            ex2.printStackTrace();
                        }
                    }
                }
                if(skip) { continue; }
                urls = nsls.getListDataFast(driver, null, list_url);
                for(int t=0; t < urls.size(); t++){
                    target_url = urls.get(t).get("product_url");
                    rec.setOff();
                    executeProcess(target_url, null, "enemy-extends", "not-quit");
                }
            }
        }catch(SQLException SQL){
            SQL.printStackTrace();
        }catch(org.json.simple.parser.ParseException prase){
            prase.printStackTrace();
        }
        DriverControl.quitDriver(driver);
    }

    /**
     * 데이터랩플러스 데이터 업데이트
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할수
     */
    public void updateProcessDatalabPlus(String part_number, String total_numbers) {

        try {
            Connection conns = mysql.initConnect(Conf.NAVER_DB_IP);
            int max = mysql.selectCacheId(conns, "select max(id) as id from datalab_plus");
            ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> urls;
            int deletes = 0;
            Timestamp data_timestamp = null;
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
            Calendar c1 = Calendar.getInstance();
            c1.add(Calendar.DATE, -30);
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            Date one_month_ago = c1.getTime();
            int time_pass, delete_pass;
            int slice_counts = Math.round(max / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point - slice_counts;
            if(max-end_point < slice_counts) { end_point = max; }
            driver = DriverControl.getGeneralDriver(isHeadless);


            for (int num = starting_point; num <= end_point; num++) {
                System.out.println(num  + "/" + end_point);
                time_pass = 0;
                delete_pass = 0;
                int id = num;
                ArrayList<String> product_nos = mysql.selectNaverProductNo(conns, "select product_no from datalab_plus where id=" + id);
                if (product_nos.size() == 0) {
                    continue;
                }
                String product_no = product_nos.get(0);
                urls = mysql.selectNaverLightUrl(conns, "select distinct(product_url), product_no, insert_time, category_comparison, datalab_update_timestamp, deletes from naver_light where product_no='" + product_no + "'");
                if (urls.size() > 0) {
                    deletes = urls.get(0).deletes;
                    data_timestamp =  urls.get(0).datalab_update_timestamp;
                    if (data_timestamp == null) {
                        time_pass = 1;
                    } else {

                        int compare = one_month_ago.compareTo(data_timestamp);
                        if (compare > 0) {
                            time_pass = 1;
                        }
                    }

                    if (deletes == 0) {
                        delete_pass = 1;
                    }
                }

                if ( time_pass == 1 /*&& delete_pass == 1*/) {
                    if (urls.get(0).product_url == null) { continue; }
                    if (urls.get(0).product_url.split("http").length == 1) { continue; }
                    try {
                        if (urls.get(0).product_url.split("adcr.nhn").length == 1) {//continue;
                        }
                    } catch (NullPointerException e2) {
                        continue;
                    }
                    try {
                        JSONObject res = executeProcess(urls.get(0).product_url, null, "enemy-extends", "not-quit");
                        if((Boolean)res.get("retry") == true) { res = executeProcess(urls.get(0).product_url, null, "enemy-extends", "not-quit"); }
                        if (res.get("valid_url").equals("스마트스토어 URL이 아닙니다")) {
                            Connection connn = mysql.initConnect(DB_IP);
                            mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where product_no=?", ourJavaTimestampObject, 1, product_no);
                            connn.close();
                            continue;
                        } else if (res.get("valid_url").equals("리뷰에러가 발생하였습니다")) {
                            System.out.println("리뷰수집에 문제가 있음!! 점검바람");
                            continue;
                        }
                    } catch (NoSuchMethodError e) {
                        e.printStackTrace();
                        DriverControl.alertClick(driver);
                        executeProcess(urls.get(0).product_url, null, "enemy-extends", "not-quit");

                    } catch (NullPointerException e3) {
                        mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where product_no=?", ourJavaTimestampObject, 1, product_no);
                        continue;
                    }
                    mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where product_no=?", ourJavaTimestampObject, 0, product_no);
                }
            }
            conns.close();
            DriverControl.quitDriver(driver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    /**
     * 네이버라이트 기반 데이터랩플러스 데이터 업데이트
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할수
     * @param special_query 네이버라이트 SELECT 쿼리 (NULL일 경우 전체)
     */
    public void updateProcessDatalabPlusFromNaverLight(String part_number, String total_numbers, String special_query) {

        try {

            Calendar calendar = Calendar.getInstance();
            Calendar c1 = Calendar.getInstance();
            c1.add(Calendar.DATE, -30);
            Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.DATE, -21);
            Date one_month_ago = c1.getTime();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            Connection conns = mysql.initConnect(DB_IP);
            String query = "select max(id) as id from naver_light";
            ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> ids = mysql.selectNaverLightUrlId(conns, query);
            conns.close();
            ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> urls;
            int deletes = 0;
            Timestamp data_timestamp = null;
            if (special_query != null) {
                query = special_query;
            }
            int time_pass = 0;
            int delete_pass = 0;
            int slice_counts = Math.round(ids.get(0).id / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point - slice_counts;
            if(ids.get(0).id-end_point < slice_counts) {
                end_point = ids.get(0).id;
            }
            driver = DriverControl.getGeneralDriver(isHeadless);
            Connection conns22 = mysql.initConnect(DB_IP);
            for (int id = starting_point; id <= end_point; id++) {
                time_pass = 0;
                delete_pass = 0;
                urls = mysql.selectNaverLightUrl(conns22, "select distinct(product_url), product_no, insert_time, category_comparison, datalab_update_timestamp, deletes from naver_light where id=" + id);

                if (urls.size() > 0) {
                    deletes =  urls.get(0).deletes;
                    data_timestamp = urls.get(0).datalab_update_timestamp;
                    if (data_timestamp == null) {
                        time_pass = 1;
                    } else {
                        int compare = one_month_ago.compareTo(data_timestamp);
                        if (compare > 0) {
                            time_pass = 1;
                        }
                    }
                    if (deletes == 0) {
                        delete_pass = 1;
                    }
                }
                if (time_pass == 1) {
                    if (urls.get(0).product_url == null) {
                        continue;
                    }
                    if (urls.get(0).product_url.split("http").length == 1) {
                        continue;
                    }
                    try {
                        if (urls.get(0).product_url.split("adcr.nhn").length == 1) {

                        }
                    } catch (NullPointerException eeeee) {
                        continue;
                    }
                    try {

                        System.out.println("check : " + urls.get(0).product_url);
                        JSONObject res = executeProcess(urls.get(0).product_url, null, "enemy-extends", "not-quit");

                        if (res.get("valid_url").equals("스마트스토어 URL이 아닙니다")) {
                            mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where id=?", ourJavaTimestampObject, 1, id);
                            continue;
                        } else if (res.get("valid_url").equals("리뷰에러가 발생하였습니다")) {
                            System.out.println("리뷰수집에 문제가 있음!! 점검바람");
                            continue;

                        } else if (res.get("valid_url").equals("deleted")) {
                            mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where id=?", ourJavaTimestampObject, 1, id);
                            continue;
                        }
                    } catch (NoSuchMethodError e) {
                        e.printStackTrace();
                        DriverControl.alertClick(driver);
                        executeProcess(urls.get(0).product_url, null, "enemy-extends","not-quit");
                    } catch (NullPointerException ee333) {
                        mysql.updateDatalabTimestampWithDelete(conns, "update naver_light set datalab_update_timestamp=?, deletes=? where id=?", ourJavaTimestampObject, 1, id);
                        continue;
                    }

                    Connection conns2 = mysql.initConnect(DB_IP);
                    mysql.updateDatalabTimestampWithDelete(conns2, "update naver_light set datalab_update_timestamp=?, deletes=? where id=?", ourJavaTimestampObject, 0, id);
                    conns2.close();
                }
            }

            conns22.close();
            DriverControl.quitDriver(driver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 프로덕트 점수 계산 (엔트로피)
     *
     * @param pastGrowth 과거성장률
     * @param currentGrowth 현재성장률
     */
    public Long getProductValue(double pastGrowth, double currentGrowth) {
        double growth = 0.0;
        if (pastGrowth == 0.0) {
            growth = currentGrowth;
        } else {
            if (pastGrowth >= currentGrowth) {
                growth = pastGrowth;
            } else {
                growth = currentGrowth;
            }
        }
        if (growth > 999) {
            growth = 0.0;
        }
        Double entrophy = -Math.log((2.6482 * Math.pow(0.83399, 2.6482)) / (Math.pow(growth, 3.6482)));
        Double productValue = entrophy * 100.0;
        if (productValue < 0) {
            productValue = 0.0;
        }
        return Math.round(productValue);
    }

    /**
     * 상품 기본 정보 수집
     *
     * @param driver 크롬드라이버
     * @param product_url 상품URL
     */
    public JSONObject getBasicProductDetailInfo(ChromeDriver driver, String product_url) {

        JSONObject basic_infos = new JSONObject();
        JSONObject option_obj = new JSONObject();
        JSONObject product_obj = null;
        ArrayList<String> thumb_arr = new ArrayList<>();
        String category = "";
        String image_url = "";
        String cat_big = "";
        String cat_middle = "";
        String cat_small = "";
        String cat_product = "";
        String cumulationSaleCount = "";
        String recentSaleCount = "";
        String cat_full = "";
        String title = "";
        String qna = "";
        String store_name = "";
        String make_country = "";
        String send_place = "";
        String seller_grade = "씨앗";
        String register_number = "";
        String tongsin_number = "";
        String address = "";
        String phone_number = "";
        String email = "";
        String business_name = "";
        String meta_keywords = "";
        String jsons = null;
        Long six_month_salecount = -1L;
        double prob_double_this = 0;
        double star_this = 4.0;
        int total_reviews_cnt = 0;
        int price = 0;
        int overseas = 0;
        int total_cnt = 0;
        int total_props = 0;
        try {
            driver.get(product_url);
            try {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[1].textContent");
                price = Format.parse(price_str).intValue();
            } catch (NoSuchMethodError e1) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            } catch (Exception e2) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Long cum_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].innerHTML.split(\"cumulationSaleCount\\\":\").length");
            Long recent_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].innerHTML.split(\"recentSaleCount\\\":\").length");
            cumulationSaleCount = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].innerHTML.split(\"cumulationSaleCount\\\":\")[" + String.valueOf(cum_leng - 1) + "].split(',')[0]");
            recentSaleCount = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].innerHTML.split(\"recentSaleCount\\\":\")[" + String.valueOf(recent_leng - 1) + "].split('}')[0]");
            six_month_salecount = Long.parseLong(cumulationSaleCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
            ArrayList option_groups = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.combinationOptions");
            for (int a = 0; a < option_array.size(); a++) {
                Map opt_obj = (Map) option_array.get(a);
                String opt_name = "";
                for (int b = 1; b < 100; b++) {
                    if (opt_obj.containsKey("optionName" + b)) {
                        opt_name += (((Map) option_groups.get(b - 1)).get("groupName") + ": " + opt_obj.get("optionName" + b));
                        opt_name += " / ";
                    } else {
                        break;
                    }
                }
                if (!opt_name.equals("")) {
                    opt_name = opt_name.substring(0, opt_name.length() - 3);
                }
                int plus_price = Integer.parseInt(String.valueOf((Long) opt_obj.get("price")));
                int opt_price = price + plus_price;
                option_obj.put(opt_name, opt_price);
            }
            option_obj.put("default_price", price);
        } catch (Exception ex) {
            option_obj = null;
        }
        if (option_obj == null) {
            try {
                option_obj = new JSONObject();
                ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
                ArrayList option_standards = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionStandards");
                JSONObject option_standards_obj = new JSONObject();
                for (int k = 0; k < option_standards.size(); k++) {
                    Map opt_obj = (Map) option_standards.get(k);
                    option_standards_obj.put((String) opt_obj.get("optionName"), (String) opt_obj.get("optionGroupName"));
                }
                for (int a = 0; a < option_array.size(); a++) {
                    Map opt_obj = (Map) option_array.get(a);
                    String opt_name = "";
                    for (int b = 1; b < 100; b++) {
                        if (opt_obj.containsKey("optionName" + b)) {
                            opt_name += (option_standards_obj.get(opt_obj.get("optionName" + b)) + ": " + opt_obj.get("optionName" + b));
                            opt_name += " / ";
                        } else {
                            break;
                        }
                    }
                    if (!opt_name.equals("")) {
                        opt_name = opt_name.substring(0, opt_name.length() - 3);
                    }
                    int plus_price = Integer.parseInt(String.valueOf((Long) opt_obj.get("price")));
                    int opt_price = price + plus_price;
                    option_obj.put(opt_name, opt_price);
                }
                option_obj.put("default_price", price);
            } catch (Exception ex) {
                option_obj = null;
            }
        }
        if(option_obj == null) {
            option_obj = new JSONObject();
            option_obj.put("default_price", price);
        }
        try {
            meta_keywords = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[name=\"keywords\"]')[0].getAttribute('content')");
        } catch (Exception e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        Long thumbnail_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3TvRO_uxKp').length");
        thumb_arr = new ArrayList<>();
        if (thumbnail_leng > 0) {
            for (int t = 0; t < thumbnail_leng; t++) {
                try {
                    String thumb_src = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3TvRO_uxKp')[" + t + "].children[0].children[0].src");
                    thumb_src = thumb_src.split("type=f40")[0] + "type=m510";
                    thumb_arr.add(thumb_src);
                }catch(Exception e1) {
                    System.out.println("360도 썸네일 스킵");
                }
            }
        }
        try {
            ArrayList delivery_prop_list = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.productDeliveryLeadTimes");
            for (int p = 0; p < delivery_prop_list.size(); p++) {
                Map obj = (Map) delivery_prop_list.get(p);
                Long leadTimeCount = (Long) ((Map) delivery_prop_list.get(p)).get("leadTimeCount");
                total_props += Long.valueOf((String) obj.get("rangeNumberText")) * leadTimeCount;
                total_cnt += leadTimeCount;
            }
            prob_double_this = total_props * 1.0 / total_cnt * 1.0;

        } catch (Exception e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        try {
            String starst = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2pgHN-ntx6')[1].textContent");
            starst = starst.split("/")[0];
            star_this = Double.parseDouble(starst);
        } catch (NoSuchMethodError e1) {
            e1.printStackTrace();
        } catch(Exception e2){
            e2.printStackTrace();
        }
        try {
            String total_reviews_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('q9fRhG-eTG')[0].textContent");
            total_reviews_cnt = Format.parse(total_reviews_str).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            title = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2v8ltQQncP')[0].textContent");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            jsons = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('script')[0].textContent");
            product_obj = (JSONObject) parser.parse(jsons);
        } catch (Exception e) {}
        try {
            jsons = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('script')[1].textContent");
            product_obj = (JSONObject) parser.parse(jsons);
        } catch (Exception e) {}
        try {
            category = (String) product_obj.get("category");
            try {
                String[] categories = category.split(">");
                cat_full = category;
                cat_big = categories[0];
                cat_middle = categories[1];
                cat_small = categories[2];
                if (categories.length == 4) {
                    cat_product = categories[3];
                }
            } catch (NoSuchMethodError er) {

            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        try {
            image_url = (String) product_obj.get("image");
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            qna = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3HJHJjSrNK')[1].textContent");
        } catch (NoSuchMethodError e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            String business_names = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1hBeKq0WZK')[0].textContent");
            business_name = business_names.split("고객센터")[0];
        } catch (NoSuchMethodError e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            store_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('title')[0].textContent.split(\" : \")[1]");
        } catch (NoSuchMethodError e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            Long overseas_check = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2bKM7srDEo').length");
            if (overseas_check > 0) {
                overseas = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            send_place = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('-SPZfRz-75')[0].children[2].children[2].textContent.split(\"보내실 곳\")[1].trim()");
        } catch (Exception e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        try {
            seller_grade = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3CfLtIh1fI')[0].textContent");
        } catch (NoSuchMethodError e1) {
            //e1.printStackTrace();
        } catch (Exception e2) {
            //e2.printStackTrace();
        }
        try {
            try {
                register_number = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('IA34ue2o2i')[0].children[2].children[1].children[1].textContent");
            } catch (NoSuchMethodError ee) { }
            try {
                tongsin_number = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('IA34ue2o2i')[0].children[2].children[1].children[3].textContent");
            } catch (NoSuchMethodError ee) { }
            try {
                address = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('IA34ue2o2i')[0].children[2].children[2].children[1].textContent");
                phone_number = address.split("고객센터: ")[1].split("인증")[0].trim();
                email = address.split("메일:")[1].split("\\)")[0].trim();
            } catch (NoSuchMethodError e1) { }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        try {
            Long trs = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children.length");
            for (int a = 0; a < trs; a++) {
                try {
                    String tr_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[0].textContent");
                    String tr_value = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[1].textContent");
                    if (tr_name.equals("제조국") || tr_name.equals("원산지")) {
                        make_country = tr_value;
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                } catch (NoSuchMethodError e2) {
                    //e2.printStackTrace();
                }
                try {
                    String tr_name2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[2].textContent");
                    String tr_value2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[3].textContent");
                    if (tr_name2.equals("제조국") || tr_name2.equals("원산지")) {
                        make_country = tr_value2;
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                } catch (NoSuchMethodError e2) {
                    //e2.printStackTrace();
                }

            }
        } catch (NullPointerException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodError e2) {
            //e2.printStackTrace();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        String product_no ="";
        try {
            String url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            product_no = url.split("/products/")[1].split("\\?")[0];
        }catch(Exception e){
            e.printStackTrace();
        }

        basic_infos.put("product_no", product_no);
        basic_infos.put("option_obj", option_obj);
        basic_infos.put("six_month_salecount", six_month_salecount);
        basic_infos.put("meta_keywords", meta_keywords);
        basic_infos.put("prob_double_this", prob_double_this);
        basic_infos.put("star_this", star_this);
        basic_infos.put("total_reviews_cnt", total_reviews_cnt);
        basic_infos.put("price", price);
        basic_infos.put("category", category);
        basic_infos.put("image_url", image_url);
        basic_infos.put("cat_full", cat_full);
        basic_infos.put("cat_big", cat_big);
        basic_infos.put("cat_middle", cat_middle);
        basic_infos.put("cat_small", cat_small);
        basic_infos.put("cat_product", cat_product);
        basic_infos.put("cumulationSaleCount", cumulationSaleCount);
        basic_infos.put("recentSaleCount", recentSaleCount);
        basic_infos.put("thumb_arr", thumb_arr);
        basic_infos.put("title", title);
        basic_infos.put("qna", qna);
        basic_infos.put("store_name", store_name);
        basic_infos.put("make_country", make_country);
        basic_infos.put("overseas", overseas);
        basic_infos.put("send_place", send_place);
        basic_infos.put("seller_grade", seller_grade);
        basic_infos.put("register_number", register_number);
        basic_infos.put("tongsin_number", tongsin_number);
        basic_infos.put("address", address);
        basic_infos.put("phone_number", phone_number);
        basic_infos.put("email", email);
        basic_infos.put("business_name", business_name);
        basic_infos.put("product_url", product_url);
        basic_infos.put("seller_name", "");

        try {
            String current_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            String seller_name = current_url.split("\\/products\\/")[0].split("smartstore.naver.com\\/")[1];
            basic_infos.put("seller_name", seller_name);

        }catch(Exception ex) {
            ex.printStackTrace();
        }


        return basic_infos;
    }


    /**
     * 상품 매출 추정 및 전체 정보 수집
     *
     * @param url 수집할 상품 URL
     * @param realtime_flag Deprecated (과거 실시간 로직에서 DB 이중화 구조로 인한 delay 문제 해결 위한 플래그 였으나 현재 삭제됨. null 입력할 것)
     * @param enemy_flag 경쟁상품 수집 여부 플래그 (enemy : 새로 수집함, enemy-ifnotexist : 기존 정보 없을때 수집함, enemy-extends : 수집안하고 기존 정보 상속받음, enemy-rank: Deprecated)
     * @param driverquit chromedriver 종료여부 (quit : 프로세스종료, not-quit : 프로세스유지)
     */
    public JSONObject executeProcess(String url, String realtime_flag, String enemy_flag, String driverquit) {

        JSONObject result_final = new JSONObject();
        String update_type = "";

        if(enemy_flag.equals("enemy") || enemy_flag.equals("enemy-ifnotexist")){
            update_type = "상품(Full)";
        }
        else {
            update_type = "상품(Partial)";
        }

        if (url.split("naver").length == 1) {
            result_final.put("valid_url", "스마트스토어 URL이 아닙니다");
            return result_final;
        }
        JSONObject result = new JSONObject();
        JSONObject data_obj_exist = new JSONObject();
        String insert_time = null;
        String cat_big = null;
        String cat_middle = null;
        String cat_small = null;
        String cat_product = null;
        String cat_full = null;
        String meta_keywords = null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -30);
        Date month_ago = cal.getTime();
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, -7);
        week_ago = c1.getTime();
        int category_rank = -1;
        int enemy_rank = -1;
        int price = -1;
        int avg_price = -1;
        int compare = 1;
        try {
            result_final.put("valid_url", "unvalid");
            if (driver == null) {
                driver = DriverControl.getGeneralDriver(isHeadless);
            }
            DriverControl.removePopupGetDriver(driver, url);
            Thread.sleep(3000);
            try {
                url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                if(url.equals("https://smartstore.naver.com/main/no-product")) {
                    result_final.put("valid_url", "deleted");
                    if(driverquit.equals("quit")) {
                        DriverControl.quitDriver(driver);
                    }
                    return result_final;
                }

            } catch (NoSuchMethodError e) {
                Thread.sleep(3000);
                DriverControl.alertClick(driver);
                DriverControl.quitDriver(driver);
                result_final.put("valid_url", "스마트스토어 URL이 아닙니다");
                driver = DriverControl.getGeneralDriver(isHeadless);
            }

            if (url.split("smartstore.naver.com").length > 1 || url.split("shopping.naver.com").length > 1 || url.split("brand.naver.com").length > 1  ) {
                result_final.put("valid_url", "valid");
                Boolean invalid_result = DriverControl.invalidCheck(driver);

                if (invalid_result == false) {
                    if (url.split("/products/").length == 2) {
                        String product_no = url.split("products/")[1].split("\\?")[0];
                        Connection con = mysql.initConnect(DB_IP);
                        mysql.updateDatalabPlusNotSell(con, "update datalab_plus set notsell=? where product_no=?", 1, product_no);
                        con.close();
                        result_final.put("valid_url", "invalid");
                        return result;
                    }
                }
                if (url.split("/products/").length == 2) {
                    String product_no = url.split("/products/")[1].split("\\?")[0];
                    if (product_no.equals("")) {
                        product_no = url.split("/products/")[1];
                    }
                    Connection select_conn = mysql.initConnect(Conf.NAVER_DB_IP);
                    ArrayList<HashMap<String, String>> datalab_plus_exist = mysql.selectDatalabPlus(select_conn, "select * from datalab_plus where product_no='" + product_no + "'");
                    if (datalab_plus_exist.size() > 0) {
                        data_obj_exist = (JSONObject) parser.parse(datalab_plus_exist.get(0).get("data"));
                        String reviews_exist = (String) datalab_plus_exist.get(0).get("reviews");
                        result.put("review_exist", reviews_exist);
                        insert_time = (String) datalab_plus_exist.get(0).get("insert_time");
                        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date inserted_date = dtFormat.parse(insert_time);
                        int compare2 = week_ago.compareTo(inserted_date);
                        compare = week_ago.compareTo(inserted_date);
                        if (realtime_flag != null && compare2 <= 0) {
                            result_final = data_obj_exist;
                        }
                    }
                    select_conn.close();

                    try {
                        String title_checker = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2v8ltQQncP')[0].textContent");
                    } catch (Exception e) {
                        result_final.put("valid_url", "invalid");

                        if (driverquit.equals("quit")) {
                            try {
                                DriverControl.quitDriver(driver);
                                driver = null;
                            } catch (NullPointerException ex) {
                                //ex.printStackTrace();
                            }
                            //driver = null;
                        }

                        return result_final;
                    }

                    rec.insertUpdateList(update_type, product_no, "상품 기본 정보 수집중...",0,1);

                    JSONObject basicInfos = getBasicProductDetailInfo(driver, url);

                    price = (int) basicInfos.get("price");
                    JSONObject option_obj = (JSONObject) basicInfos.get("option_obj");
                    Long six_month_salecount = (long) basicInfos.get("six_month_salecount");
                    meta_keywords = (String) basicInfos.get("meta_keywords");
                    ArrayList<String> thumb_arr = (ArrayList<String>) basicInfos.get("thumb_arr");
                    double prob_double_this = (double) basicInfos.get("prob_double_this");
                    prob_double_this = Math.round(prob_double_this * 100) / 100.0;
                    Double star_this = (double) basicInfos.get("star_this");
                    int total_reviews_cnt = (int) basicInfos.get("total_reviews_cnt");
                    String title = (String) basicInfos.get("title");
                    JSONObject product_obj = (JSONObject) basicInfos.get("product_obj");
                    String category = (String) basicInfos.get("category");
                    String image_url = (String) basicInfos.get("image_url");
                    cat_full = (String) basicInfos.get("cat_full");
                    cat_big = (String) basicInfos.get("cat_big");
                    cat_middle = (String) basicInfos.get("cat_middle");
                    cat_small = (String) basicInfos.get("cat_small");
                    cat_product = (String) basicInfos.get("cat_product");
                    String qna = (String) basicInfos.get("qna");
                    int overseas =  (int) basicInfos.get("overseas");
                    String make_country= (String) basicInfos.get("make_country");
                    String send_place= (String) basicInfos.get("send_place");
                    String register_number= (String) basicInfos.get("register_number");
                    String tongsin_number= (String) basicInfos.get("tongsin_number");
                    String address= (String) basicInfos.get("address");
                    String phone_number= (String) basicInfos.get("phone_number");
                    String email= (String) basicInfos.get("email");
                    String store_name= (String) basicInfos.get("store_name");
                    String seller_grade =  (String) basicInfos.get("seller_grade");
                    String productUrl = (String) basicInfos.get("product_url");
                    String seller_name = (String) basicInfos.get("seller_name");
                    String business_name = (String) basicInfos.get("business_name");


                    if(seller_name.equals("")) {
                        seller_name = productUrl.split("naver.com/")[1].split("/")[0];
                    }

                    result_final.put("사업자명", business_name);
                    result_final.put("스토어명", store_name);
                    result_final.put("실판매량(6달)", basicInfos.get("cumulationSaleCount"));
                    result_final.put("실판매량(3일)", basicInfos.get("recentSaleCount"));
                    try {
                        result_final.put("옵션객체", basicInfos.get("option_obj").toString());
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }

                    HashMap<String, String> productMap = new HashMap();
                    productMap.put("product_no", product_no);
                    productMap.put("product_title", title);
                    productMap.put("product_image", image_url);
                    productMap.put("product_category", category);
                    productMap.put("cat_big", cat_big);
                    productMap.put("cat_middle", cat_middle);
                    productMap.put("cat_small", cat_small);
                    productMap.put("cat_product", cat_product);
                    productMap.put("product_keyword", "");
                    productMap.put("product_url", url);
                    productMap.put("price", String.valueOf(price));

                    rec.insertUpdateList(update_type, product_no, "상품 기본 정보 수집완료. 리뷰 분석 준비중...",0,1);

                    JSONObject result2 = nrs.getReviewInfo(driver, productMap, result, total_reviews_cnt, option_obj);

                    if (result2.get("valid_url").equals("리뷰에러가 발생하였습니다")) {
                        System.out.println("프로덕트넘버:" + product_no);
                        result_final.put("valid_url", "리뷰에러가 발생하였습니다");
                        return result2;
                    }

                    rec.insertUpdateList(update_type, product_no, "리뷰 수집 완료. 구매옵션 분석 중...",0,1);

                    HashMap<String, Double> total_ratio = Calculator.printReviewOptionRatio((ArrayList) result2.get("review_array_total"));

                    rec.insertUpdateList(update_type, product_no, "구매 옵션 분석 완료. 기초 데이터 취합중...",0,1);

                    String review_total = ((ArrayList) result2.get("review_array_total")).toString();
                    result_final.put("오늘리뷰", result2.get("review_array_today"));
                    result_final.put("봄", result2.get("spring"));
                    result_final.put("여름", result2.get("summer"));
                    result_final.put("가을", result2.get("fall"));
                    result_final.put("겨울", result2.get("winter"));
                    JSONObject thisprod = new JSONObject();
                    thisprod.put("가격", price);
                    thisprod.put("리뷰", total_reviews_cnt);
                    thisprod.put("배송", prob_double_this);
                    thisprod.put("평점", star_this);
                    try {
                        thisprod.put("구매옵션", option_obj.size() - 1);
                    }catch(Exception ex){
                        thisprod.put("구매옵션", 0);
                    }
                    thisprod.put("평균가격", result2.get("avg_price"));
                    result_final.put("본상품_특성", thisprod);
                    if (six_month_salecount != -1L) {
                        int six_review_size = ((ArrayList) result2.get("review_array_sixmonth")).size();
                        double rat = six_month_salecount * 1.0 / six_review_size * 1.0;
                        if ((six_month_salecount >= 50) && (rat < 4)) {
                            e = rat;
                        }
                    }
                    for (int ks = 0; ks < 12; ks++) {
                        ArrayList arr = (ArrayList<Integer>) result2.get("month");
                        ((ArrayList) result2.get("month")).set(ks, (Integer) arr.get(ks));

                        ArrayList arr2 = (ArrayList<Integer>) result2.get("month_advance");
                        ((ArrayList) result2.get("month_advance")).set(ks, Math.round((Integer) arr2.get(ks) * e));
                    }
                    result_final.put("월별매출그래프", result2.get("month_advance"));
                    result_final.put("월별리뷰수그래프", result2.get("month"));
                    double revenue = -1;
                    double revenue3m = -1;
                    double revenue6m =-1;
                    double revenue12m = -1;
                    double revenue_pm =-1;
                    double revenue_6pm = -1;
                    double revenue_12pm = -1;
                    double revenue0d = -1;
                    double revenue1d = -1;
                    double revenue7d = -1;
                    double revenue1m = -1;
                    int total_review_count = -1;
                    int three_review_count = -1;
                    int six_review_count = -1;
                    int twelve_review_count = -1;
                    int nine_review_count = -1;
                    if (option_obj == null) {
                        total_review_count = (int)result2.get("review_count_original");//((ArrayList) result2.get("review_array_total")).size();
                        three_review_count = ((ArrayList) result2.get("review_array_threemonth")).size();
                        six_review_count = ((ArrayList) result2.get("review_array_sixmonth")).size();
                        twelve_review_count = ((ArrayList) result2.get("review_array_twelvemonth")).size();
                        int eleven_review_count = ((ArrayList) result2.get("review_array_elevenmonth")).size();
                        nine_review_count = ((ArrayList) result2.get("review_array_ninemonth")).size();
                        int yesterday_review_count = ((ArrayList) result2.get("review_array_yesterday")).size();
                        int today_review_count = ((ArrayList) result2.get("review_array_today")).size();
                        int thisweek_review_count = ((ArrayList) result2.get("review_array_thisweek")).size();
                        int one_review_count = ((ArrayList) result2.get("review_array_onemonth")).size();
                        revenue = Math.round(total_review_count * price * e);
                        try {
                            revenue += Math.round((double) result2.get("보정치") * (int) result2.get("avg_price") * e);
                        }catch(Exception e){
                            //e.printStackTrace();
                        }
                        revenue3m = Math.round(three_review_count * price * e);
                        revenue6m = Math.round(six_review_count * price * e);
                        revenue12m = Math.round(twelve_review_count * price * e);
                        revenue_pm = Math.round((total_review_count - three_review_count) * price * e);
                        revenue_6pm = Math.round((total_review_count - six_review_count) * price * e);
                        revenue_12pm = Math.round((total_review_count - twelve_review_count) * price * e);
                        revenue0d = yesterday_review_count * price * e;
                        revenue1d = today_review_count * price * e;
                        revenue7d = thisweek_review_count * price * e;
                        revenue1m = one_review_count * price * e;
                    } else {

                        total_review_count = (int)result2.get("review_count_original"); //((ArrayList) result2.get("review_array_total")).size() + String.valueOf(double) result2.get("보정치");
                        three_review_count = ((ArrayList) result2.get("review_array_threemonth")).size();
                        six_review_count = ((ArrayList) result2.get("review_array_sixmonth")).size();
                        twelve_review_count = ((ArrayList) result2.get("review_array_twelvemonth")).size();
                        int eleven_review_count = ((ArrayList) result2.get("review_array_elevenmonth")).size();
                        nine_review_count = ((ArrayList) result2.get("review_array_ninemonth")).size();
                        revenue = Math.round((long) result2.get("option_base_revenue") * e);
                        try {
                            revenue += Math.round((double) result2.get("보정치") * (int) result2.get("avg_price") * e);
                        }catch(Exception ex){

                        }
                        revenue3m = Math.round((long) result2.get("option_base_revenue3m") * e);
                        revenue6m = Math.round((long) result2.get("option_base_revenue6m") * e);
                        revenue12m = Math.round((long) result2.get("option_base_revenue12m") * e);
                        revenue_pm = Math.round(((long) result2.get("option_base_revenue") - (long) result2.get("option_base_revenue3m")) * e);
                        revenue_6pm = Math.round(((long) result2.get("option_base_revenue") - (long) result2.get("option_base_revenue6m")) * e);
                        revenue_12pm = Math.round(((long) result2.get("option_base_revenue") - (long) result2.get("option_base_revenue12m")) * e);
                        revenue0d = (long) result2.get("option_base_revenue0d") * e;
                        revenue1d = (long) result2.get("option_base_revenue1d") * e;
                        revenue7d = (long) result2.get("option_base_revenue7d") * e;
                        revenue1m = (long) result2.get("option_base_revenue1m") * e;
                    }
                    JSONObject years_revenue_obj = (JSONObject) result2.get("year_based_revenue");
                    for(Iterator iterator = years_revenue_obj.keySet().iterator(); iterator.hasNext();) {
                        int year = (Integer) iterator.next();
                        long[] month_arr = (long[]) years_revenue_obj.get(year);
                        ArrayList<Integer> month_arr2 = new ArrayList<Integer>();
                        for (int m = 0; m < month_arr.length; m++) {
                            month_arr2.add(m , Long.valueOf(Math.round(month_arr[m] * e)).intValue());
                        }
                        years_revenue_obj.put(year, month_arr2);
                    }

                    double growth = Calculator.getGrowthRateTotal(years_revenue_obj);
                    double growth_3m = Calculator.getGrowthRate(years_revenue_obj, 1, 3);
                    double growth_6m = Calculator.getGrowthRate(years_revenue_obj, 1, 6);
                    double growth_12m = Calculator.getGrowthRate(years_revenue_obj, 1, 12);
                    double growth_year = Calculator.getGrowthRateYear(years_revenue_obj);
                    double growth_past = Calculator.getGrowthRate(years_revenue_obj, 13, 3);
                    double growth_past_6m = Calculator.getGrowthRate(years_revenue_obj, 13, 6);
                    double growth_past_12m = Calculator.getGrowthRate(years_revenue_obj, 13, 12);
                    try {
                        Long trs = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children.length");
                        for (int a = 0; a < trs; a++) {

                            try {
                                String tr_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[0].textContent");
                                String tr_value = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[1].textContent");
                                result_final.put(tr_name, tr_value);
                                if (tr_name.equals("제조국") || tr_name.equals("원산지")) {
                                    make_country = tr_value;
                                }
                            } catch (NoSuchMethodError ex1) {
                                //ex1.printStackTrace();
                            } catch (Exception ex2) {
                                //ex2.printStackTrace();
                            }

                            try {
                                String tr_name2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[2].textContent");
                                String tr_value2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1_UiXWHt__')[0].children[2].children[" + a + "].children[3].textContent");
                                result_final.put(tr_name2, tr_value2);
                                if (tr_name2.equals("제조국") || tr_name2.equals("원산지")) {
                                    make_country = tr_value2;
                                }
                            } catch (NoSuchMethodError ex1) {
                                //ex1.printStackTrace();
                            } catch (Exception ex2) {
                                //ex2.printStackTrace();
                            }
                        }
                    } catch (NullPointerException e1) {
                        e1.printStackTrace();
                    } catch (NoSuchMethodError e2) {
                        e2.printStackTrace();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }

                    if (enemy_flag.equals("enemy-rank")) {
                        //키워드 해시태그 등록시키기
                        ArrayList<HashMap> firstpage_keyword_infos = insertHashtagByTitle(compare, driver);
                        result_final.put("1페이지", firstpage_keyword_infos);
                        long my_pricexcount = price * total_review_count;
                        Connection conn_keyword = mysql.initConnect(Conf.NAVER_DB_IP_SERVICE);
                        String enemy_total_query = "select *  from keyword_hashtag where keyword in (select keyword from keyword_hashtag where product_no='" + product_no + "') ORDER BY pricexcount_review DESC;";
                        String enemy_query = "select count(*) as count from keyword_hashtag where keyword in (select keyword from keyword_hashtag where product_no='" + product_no + "') and pricexcount_review > " + my_pricexcount;
                        int count2 = mysql.selectCount(conn_keyword, enemy_query);
                        ArrayList<HashMap<String, String>> datas2 = mysql.selectKeywordHashtag(conn_keyword, enemy_total_query);
                        enemy_rank = count2 + 1;
                        conn_keyword.close();
                        double c_rank = -1; //Math.round((category_rank * 1.0 / category_total * 1.0) * 100.0);
                        double e_rank = Math.round((enemy_rank * 1.0 / datas2.size()) * 100.0);
                        ArrayList<HashMap<String, String>> enemys = datas2;//mysql.selectKeywordHashtag(conn, ar);
                        ArrayList<HashMap<String, String>> enemy_dup = new ArrayList<>();
                        HashMap<String, Integer> dup = new HashMap<>();
                        int rrr = 1;
                        int enemy_leng = enemys.size();
                        if (enemy_leng > 100) {
                            enemy_leng = 100;
                        }
                        Connection conn_2 = mysql.initConnect(DB_IP);
                        for (int j = 0; j < enemy_leng; j++) {
                            HashMap hs = enemys.get(j);
                            if (!dup.containsKey(hs.get("title"))) {
                                try {
                                    int enemy_price = Integer.parseInt((String) hs.get("price"));
                                    long enemy_pricecount_review = Long.parseLong((String) hs.get("pricexcount_review"));
                                    double enemy_revenue = Math.round(enemy_pricecount_review * e);
                                    String enemy_image_url = (String) hs.get("product_img");
                                    hs.put("price", enemy_price);
                                    hs.put("revenue", enemy_revenue);
                                    hs.put("enemy_rank", rrr);
                                    String product_n = (String) hs.get("product_no");
                                    String q = "select JSON_EXTRACT(data, '$.image_url') as image_url, url, product_no from datalab_plus where product_no='" + product_n + "'";
                                    ArrayList<HashMap<String, String>> aa = mysql.selectDatalabPlusImageUrl(conn_2, q);
                                    if (aa.size() == 1) {
                                        enemy_image_url = aa.get(0).get("image_url");
                                        enemy_image_url = enemy_image_url.substring(1, enemy_image_url.length() - 1);
                                        enemy_image_url = enemy_image_url.split("type=")[0] + "type=f140";
                                    } else {
                                        continue;
                                    }
                                    hs.put("image_url", enemy_image_url);
                                    rrr++;
                                    enemy_dup.add(hs);
                                    dup.put((String) hs.get("title"), 1);
                                } catch (NumberFormatException ex) {
                                    continue;
                                }
                            }
                        }
                        conn_2.close();
                        if (compare > 0) {
                            int e_cnt = 1;
                            for (int e = 0; e < 50; e++) {
                                try {
                                    DriverControl.removePopupGetDriver(driver, enemy_dup.get(e).get("product_url"));
                                    String prob = null;
                                    double prob_double = 60.0;
                                    String current_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                                    if (current_url.split("search.shopping.naver.com").length > 1) {
                                        String new_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.linkCompare_compare_wrap__3aPZK > a')[0].href");
                                        DriverControl.removePopupGetDriver(driver, new_url);
                                    }
                                    Boolean check_result = DriverControl.invalidCheck(driver);
                                    if (check_result == false) {
                                        continue;
                                    }
                                    try {
                                        prob = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2wxgzdBSi-')[0].textContent");
                                        String[] probs = prob.split("\\(");
                                        if (probs.length > 1) {
                                            prob = prob.split("\\)")[0].split("\\(")[1];
                                        }
                                        prob = prob.substring(0, prob.length() - 1);
                                        prob_double = Double.parseDouble(prob);

                                    } catch (NoSuchMethodError e1) {
                                        //e1.printStackTrace();
                                    } catch (Exception e2) {
                                        //e2.printStackTrace();
                                    }
                                    try {
                                        if (prob == null) {
                                            prob = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_218f7rvTTy')[0].children[2].textContent");
                                            String[] probs = prob.split("\\(");
                                            if (probs.length > 1) {
                                                prob = prob.split("\\)")[0].split("\\(")[1];
                                            }
                                            prob = prob.substring(0, prob.length() - 1);
                                            prob_double = Double.parseDouble(prob);
                                        }
                                    } catch (NoSuchMethodError e1) {
                                        //e1.printStackTrace();
                                    } catch (Exception e2) {
                                        //e2.printStackTrace();
                                    }
                                    Double star = 4.0;
                                    try {
                                        String starst = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2pgHN-ntx6')[1].textContent");
                                        starst = starst.split("/")[0];
                                        star = Double.parseDouble(starst);
                                    } catch (NoSuchMethodError e1) {
                                        //e1.printStackTrace();
                                    } catch (Exception e2) {
                                        continue;
                                    }
                                    int buyopt_leng = 1;
                                    Long optsleng = 0L;
                                    try {

                                        if (option_obj == null) {
                                            optsleng = (Long) ((JavascriptExecutor) driver).executeScript("return  document.querySelectorAll('._3R5zw_nN-K').length");
                                            optsleng = optsleng / 2;
                                            buyopt_leng = Math.round(optsleng);
                                            for (int t = 0; t < optsleng; t++) {
                                                try {
                                                    ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('._3R5zw_nN-K')[" + t + "].click()");
                                                    Thread.sleep(1000);
                                                    Long option_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('ul[role=\"listbox\"]')[0].children.length");
                                                    buyopt_leng = buyopt_leng * option_leng.intValue();
                                                    int pass = 0;
                                                    for (int jj = 0; jj < option_leng; jj++) {
                                                        try {
                                                            ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('ul[role=\"listbox\"]')[0].children[" + jj + "].children[0].click()");
                                                            pass = 1;
                                                            break;
                                                        } catch (Exception ex) {
                                                            pass = 0;
                                                            ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('._3R5zw_nN-K')[" + t + "].click()");
                                                            Thread.sleep(1000);
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    System.out.println("품절 옵션 클릭 하였음");
                                                }
                                            }
                                        } else {
                                            buyopt_leng = option_obj.keySet().size();
                                        }

                                    } catch (NullPointerException ex) {
                                        //ex.printStackTrace();
                                    }

                                    JSONObject ob = new JSONObject();
                                    ob.put("배송", prob_double);
                                    ob.put("구매옵션", buyopt_leng);
                                    ob.put("구매옵션(갯수만)", optsleng);
                                    ob.put("평점", star);
                                    ob.put("타이틀", enemy_dup.get(e).get("title"));
                                    ob.put("리뷰", enemy_dup.get(e).get("count_review"));
                                    ob.put("가격", enemy_dup.get(e).get("price"));
                                    result_final.put("경쟁자특성" + e_cnt, ob);
                                    e_cnt++;

                                } catch (IndexOutOfBoundsException e1) {
                                    //e1.printStackTrace();
                                } catch (NoSuchMethodError e2) {
                                    //e2.printStackTrace();
                                } catch (Exception e3) {
                                    System.out.println("품절입니다");
                                }

                            }
                        } else {
                            for (int e = 0; e < 30; e++) {
                                if (data_obj_exist.containsKey("경쟁자특성" + e)) {
                                    result_final.put("경쟁자특성" + e, data_obj_exist.get("경쟁자특성" + e));
                                }
                            }
                        }
                        ArrayList rel_items = new ArrayList();
                        if (compare > 0) {
                            Connection conn_snu = mysql.initConnect(DB_IP);
                            ArrayList<HashMap<String, String>> users = new ArrayList<>();
                            String user_id_query = "select distinct(user_id) from naver_users where product_id='" + product_no + "'";
                            ArrayList<HashMap<String, String>> user_ids = mysql.selectNaverUsersOnlyUserid(conn_snu, user_id_query);
                            for (int id = 0; id < user_ids.size(); id++) {
                                String user_id = user_ids.get(id).get("user_id");
                                String user_query = "select * , count(*) as count from naver_users where user_id='" + user_id + "'";
                                ArrayList<HashMap<String, String>> user_query_single = mysql.selectNaverUsers(conn_snu, user_query);
                                users.add(user_query_single.get(0));
                            }
                            conn_snu.close();
                            if (users.size() > 0) {
                                int sum_users = 0;
                                for (int us = 0; us < users.size(); us++) {
                                    sum_users += Integer.parseInt(users.get(us).get("count"));
                                }
                                double rest = 100.0;
                                for (int uss = 0; uss < users.size(); uss++) {
                                    HashMap<String, String> item = new HashMap<>();
                                    double percent = Integer.parseInt(users.get(uss).get("count")) * 1.0 / sum_users * 1.0;
                                    percent = percent * 100;
                                    percent = Math.round(percent * 100) / 100.0;
                                    String percent_str = String.valueOf(percent);
                                    String product_url = users.get(uss).get("product_url");
                                    String product_tile = users.get(uss).get("product_title");
                                    String product_image = users.get(uss).get("product_image");
                                    String product_price = users.get(uss).get("product_price");
                                    String product_category = users.get(uss).get("product_category");

                                    item.put("percent", percent_str);
                                    item.put("product_url", product_url);
                                    item.put("product_title", product_tile);
                                    item.put("product_image", product_image);
                                    item.put("product_price", product_price);
                                    item.put("product_category", product_category);
                                    rest = rest - percent;
                                    rel_items.add(item);
                                }
                            }
                        } else {
                            if (data_obj_exist.containsKey("연관상품")) {
                                rel_items = (ArrayList) data_obj_exist.get("연관상품");
                            }
                        }

                        c_rank = Math.round(c_rank * 100) / 100.0;
                        e_rank = Math.round(e_rank * 100) / 100.0;
                        result_final.put("분야매출순위", c_rank);
                        result_final.put("경쟁제품간매출순위", e_rank);
                        result_final.put("경쟁제품상세분석", enemy_dup);


                    }

                    ArrayList enemy_group = new ArrayList();
                    HashMap<String,Integer> isSameProduct = new HashMap<String,Integer>();
                    if(enemy_flag.equals("enemy-extends")){
                        if(data_obj_exist.containsKey("경쟁그룹") && data_obj_exist.get("경쟁그룹") !=  null){
                            result_final.put("경쟁그룹", data_obj_exist.get("경쟁그룹"));
                        }
                        else{
                            enemy_flag = "not-enemy";
                        }

                    }
                    if(enemy_flag.equals("enemy-ifnotexist")){
                        if(data_obj_exist.containsKey("경쟁그룹") && data_obj_exist.get("경쟁그룹") !=  null){
                            if(((ArrayList)data_obj_exist.get("경쟁그룹")).size() > 0) {
                                result_final.put("경쟁그룹", data_obj_exist.get("경쟁그룹"));
                            }
                            else {
                                enemy_flag = "enemy";
                            }
                        }
                        else{
                            enemy_flag = "enemy";
                        }
                    }

                    if(enemy_flag.equals("enemy")) {

                        rec.insertUpdateList(update_type, product_no, "경쟁 상품 취합 중...",0,1);
                        SellerAnalysis rp = new SellerAnalysis();
                        ArrayList<String> thisurl = new ArrayList();
                        thisurl.add(url);
                        ArrayList<HashMap> enemy_nos = rp.getRelevantProductNos(driver, thisurl, cat_small, store_name);
                        rec.insertUpdateList(update_type, product_no, "경쟁 상품 분석을 시작 합니다",0,enemy_nos.size());
                        for(int e=0; e < enemy_nos.size(); e++){
                            HashMap enemy_map =  enemy_nos.get(e);
                            String enemy_no = (String)enemy_map.get("product_no");
                            if(isSameProduct.containsKey(enemy_no)) {
                                continue;
                            } else {
                                isSameProduct.put(enemy_no,1);
                            }
                            int isWeekOverData = 1;
                            Connection conn_enemy = mysql.initConnect(DB_IP);
                            ArrayList<HashMap<String, String>> dplus_basic = mysql.selectDplusBasic(conn_enemy, "select * from datalab_plus_basic where product_no='" + enemy_no + "'");
                            conn_enemy.close();
                            try {
                                if (dplus_basic.size() > 0) {
                                    Date inserted_date = yyyymmddFormat.parse((String) dplus_basic.get(0).get("insert_time"));
                                    isWeekOverData = month_ago.compareTo(inserted_date);
                                } else{
                                    isWeekOverData = 999;
                                }
                            } catch (Exception ex) {

                            }
                            JSONObject data_obj = new JSONObject();
                            if (isWeekOverData > 0) {
                                String enemy_url = (String)enemy_map.get("product_url");
                                if(enemy_url.split("smartstore.naver.com").length > 1) {
                                    data_obj = getBasicProductDetailInfo(driver, (String) enemy_map.get("product_url"));
                                    Connection conn_enemy2 = mysql.initConnect(DB_IP);
                                    mysql.insertDatalabPlusBasic(conn_enemy2, "insert into datalab_plus_basic(product_no,data) values(?,?)", enemy_no, data_obj.toJSONString());
                                    conn_enemy2.close();
                                }
                            } else {
                                data_obj = (JSONObject) parser.parse((String) ((HashMap) dplus_basic.get(0)).get("data"));
                            }

                            if(data_obj.get("product_no") == null){
                                continue;
                            }
                            if(data_obj.get("product_no").equals("")) {
                                continue;
                            }

                            JSONObject ob = new JSONObject();
                            ob.put("배송", data_obj.get("prob_double_this"));
                            if(data_obj.get("option_obj") != null) {
                                ob.put("구매옵션", ((JSONObject) data_obj.get("option_obj")).size() - 1);
                            }else {
                                ob.put("구매옵션", 0);
                            }
                            ob.put("product_no", data_obj.get("product_no"));
                            ob.put("평점", data_obj.get("star_this"));
                            ob.put("타이틀", data_obj.get("title"));
                            ob.put("리뷰", data_obj.get("total_reviews_cnt"));
                            ob.put("가격", data_obj.get("price"));
                            ob.put("스토어명", data_obj.get("store_name"));
                            ob.put("image_url", data_obj.get("image_url"));
                            ob.put("seller_name", data_obj.get("seller_name"));
                            ob.put("category", data_obj.get("category"));
                            if (data_obj.get("seller_name") == null) {
                                ob.put("seller_name", enemy_map.get("seller_name"));
                            }
                            ob.put("keyword_type", enemy_map.get("keyword_type"));
                            ob.put("keyword", enemy_map.get("keyword"));
                            String rstr = data_obj.get("total_reviews_cnt").toString();
                            if(Long.parseLong(rstr) != 0) {
                                enemy_group.add(ob);
                                System.out.println("경쟁품 체크:" + data_obj.get("title"));
                            }

                            rec.selectAndPlucCountUpdateList(product_no, "경쟁상품(" + ob.get("타이틀") + ") 분석 완료");
                        }

                        result_final.put("경쟁그룹", enemy_group);

                    }

                    rec.insertUpdateList(update_type, product_no, "경쟁 상품 분석 완료. 데이터 최종 취합 중",0,1);
                    String price_comma = NumberFormat.getInstance().format(price);
                    String total_review_count_comma = NumberFormat.getInstance().format(total_review_count);
                    String revenue_comma = NumberFormat.getInstance().format(Math.round(revenue));
                    String revenue_pm_comma = NumberFormat.getInstance().format(Math.round(revenue_pm));
                    String revenue_pm_comma6 = NumberFormat.getInstance().format(Math.round(revenue_6pm));
                    String revenue_pm_comma12 = NumberFormat.getInstance().format(Math.round(revenue_12pm));
                    String revenue3m_comma = NumberFormat.getInstance().format(Math.round(revenue3m));
                    String revenue6m_comma = NumberFormat.getInstance().format(Math.round(revenue6m));
                    String revenue12m_comma = NumberFormat.getInstance().format(Math.round(revenue12m));
                    String revenue0d_comma = NumberFormat.getInstance().format(Math.round(revenue0d));
                    String revenue1d_comma = NumberFormat.getInstance().format(Math.round(revenue1d));
                    String revenue7d_comma = NumberFormat.getInstance().format(Math.round(revenue7d));
                    String revenue1m_comma = NumberFormat.getInstance().format(Math.round(revenue1m));

                    result_final.put("리뷰수", total_review_count_comma);
                    result_final.put("누적매출액", revenue_comma);
                    result_final.put("누적매출액3개월", revenue3m_comma);
                    result_final.put("누적매출액6개월", revenue6m_comma);
                    result_final.put("누적매출액12개월", revenue12m_comma);
                    result_final.put("revenue", revenue);
                    result_final.put("revenue3m", revenue3m);
                    result_final.put("revenue6m", revenue6m);
                    result_final.put("revenue12m", revenue12m);
                    result_final.put("누적매출액어제", revenue0d_comma);
                    result_final.put("누적매출액오늘", revenue1d_comma);
                    result_final.put("누적매출액7일", revenue7d_comma);
                    result_final.put("누적매출액1개월", revenue1m_comma);
                    result_final.put("누적매출액증감", revenue_pm_comma);
                    result_final.put("누적매출액증감12", revenue_pm_comma12);
                    result_final.put("누적매출액증감6", revenue_pm_comma6);
                    result_final.put("월평균성장률", growth);
                    result_final.put("월평균성장률3개월", growth_3m);
                    result_final.put("월평균성장률6개월", growth_6m);
                    result_final.put("월평균성장률12개월", growth_12m);
                    result_final.put("월평균성장률(과거1년전3개월)", growth_past);
                    result_final.put("월평균성장률(과거1년전6개월)", growth_past_6m);
                    result_final.put("월평균성장률(과거1년전12개월)", growth_past_12m);
                    result_final.put("연평균성장률", growth_year);
                    result_final.put("연도별매출액", years_revenue_obj.toJSONString());
                    result_final.put("image_url", image_url);
                    result_final.put("category", category);
                    result_final.put("price", price_comma);
                    result_final.put("구매옵션", total_ratio);
                    result_final.put("별점", star_this);
                    result_final.put("질문", qna);
                    result_final.put("타이틀", title);

                    double revenue_ss = Math.round((double) result2.get("spring") + (double) result2.get("summer"));
                    double revenue_fw = Math.round((double) result2.get("fall") + (double) result2.get("winter"));

                    result_final.put("해외여부", overseas);
                    result_final.put("보내실곳", send_place);
                    result_final.put("사업자등록번호 ", register_number);
                    result_final.put("통신판매업번호", tongsin_number);
                    result_final.put("사업장소재지", address);
                    result_final.put("고객센터", phone_number);
                    result_final.put("메일", email);
                    result_final.put("썸네일", thumb_arr.toString());
                    result_final.put("원산지", make_country);
                    result_final.put("메타키워드", meta_keywords);
                    result_final.put("product_url", productUrl);
                    result_final.put("seller_name", seller_name);
                    Long product_value = getProductValue(growth_past, growth);
                    result_final.put("점수", product_value);
                    result_final.put("cat_full", cat_full);
                    result_final.put("valid_url", "valid");


                    long test = Long.parseLong(product_no);
                    Connection conn_last = mysql.initConnect(DB_IP);
                    ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> coms = mysql.selectNaverLightComparisonKeyword(conn_last, "select category_comparison, firstpage_keyword from naver_light where product_no='" + product_no + "'");
                    int category_comparison = -1;
                    String firstpage_keyword = null;
                    if (coms.size() == 1) {
                        category_comparison = coms.get(0).category_comparison;
                        firstpage_keyword = coms.get(0).firstpage_keyword;
                    }
                    conn_last.close();
                    Connection conn_last1 = mysql.initConnect(DB_IP);
                    mysql.insertDatalabPlus(conn_last1, "insert into datalab_plus (product_no,title,growth,price,revenue,revenue3m,revenue6m,revenue12m,data,make_country,url, overseas, send_place, phone_number, email, address, revenue_ss, revenue_fw, reviews, store_name, seller_grade, card_value, cat_full, cat_big, cat_middle,cat_small, cat_product, brand, make_company, category_comparison, firstpage_keyword) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", test, title, growth_3m, price, revenue, revenue3m, revenue6m, revenue12m, result_final.toString(), make_country, url, overseas, send_place, phone_number, email, address, revenue_ss, revenue_fw, review_total, store_name, seller_grade, product_value, cat_full, cat_big, cat_middle, cat_small, cat_product, (String) result_final.get("브랜드"), (String) result_final.get("제조사"), category_comparison, firstpage_keyword);
                    conn_last1.close();
                    result_final.put("valid", true);

                    rec.insertUpdateList(update_type, product_no, "분석완료",1,1);
                    rec.finishUpdateList(product_no);
                }
            }
            else {
                result_final.put("valid_url", "스마트스토어 URL이 아닙니다");
            }

        } catch(TimeoutException ex1){
            System.out.println("타임아웃 발생");
            DriverControl.quitDriver(driver);;
            driver = null;
            result_final.put("retry", true);
            result_final.put("valid", false);
        } catch(NoSuchMethodError ex2){
            result_final.put("valid_url", "스마트스토어 URL이 아닙니다");
            result_final.put("valid", false);
        } catch(Exception ex3) {
            result_final.put("valid_url", "스마트스토어 URL이 아닙니다");
            result_final.put("valid", false);
        }

        if (driverquit.equals("quit")) {
            try {
                DriverControl.quitDriver(driver);;
                driver = null;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return result_final;
    }


    /**
     * 상품 키워드(타이틀,메타태그)로 네이버쇼핑 검색 후 naver_store,keyword_hashtag,naver_light 테이블에 INSERT
     *
     * @param insert_flag DB 저장 여부(>0 : 저장, 이외 : 저장안함)
     * @param driver 크롬드라이버
     */
    public ArrayList<HashMap> insertHashtagByTitle ( int insert_flag, ChromeDriver driver){

        ArrayList<HashMap> keywords_array = new ArrayList<>();

        try {

            Thread.sleep(1000);

            String this_title = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2v8ltQQncP')[0].textContent");
            String location_href = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            String this_product_no = location_href.split("products/")[1];
            ArrayList<String> reg_keywords = new ArrayList<>();
            Long reg_keyword_check = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('._3Vox1DKZiA').length");
            if (reg_keyword_check == 1) {
                Long reg_keyword_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('._3Vox1DKZiA')[0].children.length");
                for (int r = 0; r < reg_keyword_leng; r++) {
                    String reg_keyword = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('._3Vox1DKZiA')[0].children[" + r + "].textContent");
                    reg_keyword = reg_keyword.substring(1, reg_keyword.length());
                    reg_keywords.add(reg_keyword);
                }
            }
            String meta_keywords = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[name=\"keywords\"]')[0].getAttribute('content')");
            ArrayList<String> meta_keywords_arr = new ArrayList<>();
            String[] metas = meta_keywords.split(",");
            for (int m = 0; m < metas.length; m++) {
                meta_keywords_arr.add(metas[m]);
            }
            ArrayList<String> title_candidates = new ArrayList<>();
            String[] title_split = this_title.split(" ");
            String temp = "";
            for (int t1 = 0; t1 < title_split.length; t1++) {
                if (!temp.equals("")) {
                    title_candidates.add(temp);
                }
                for (int t2 = t1 + 1; t2 < title_split.length; t2++) {
                    temp = title_split[t1];
                    temp += " ";
                    temp += title_split[t2];
                    if (!temp.equals("")) {
                        title_candidates.add(temp);
                    }
                }
                temp = "";
            }
            title_candidates.addAll(reg_keywords);
            title_candidates.addAll(meta_keywords_arr);
            JSONObject DUP = new JSONObject();
            for (int ttt = 0; ttt < title_candidates.size(); ttt++) {
                DUP.put(title_candidates.get(ttt), 1);
            }

            Iterator<String> keys = DUP.keySet().iterator();

            while (keys.hasNext()) {
                String keyword = keys.next();
                driver.get("https://search.shopping.naver.com/search/all?query=" + keyword);
                String content_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('__NEXT_DATA__').textContent");
                String total_cnt_str = "";
                try {
                    total_cnt_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('subFilter_num__2x0jq')[0].textContent");
                    int total_cnt = Format.parse(total_cnt_str).intValue();
                    total_cnt_str = String.valueOf(total_cnt);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                String isAd = "0";
                try {
                    JSONObject content_obj = (JSONObject) parser.parse(content_str);
                    JSONObject props = (JSONObject) content_obj.get("props");
                    JSONObject pageProps = (JSONObject) props.get("pageProps");
                    JSONObject initialState = (JSONObject) pageProps.get("initialState");
                    JSONObject products = (JSONObject) initialState.get("products");
                    ArrayList list = (ArrayList) products.get("list");
                    int pricexcount = 0;
                    for (int t = 0; t < list.size(); t++) {
                        int rank = t + 1;
                        JSONObject obj_temp = (JSONObject) list.get(t);
                        JSONObject content = (JSONObject) obj_temp.get("item");
                        String product_no = (String) content.get("mallProductId");
                        if (product_no == null) {
                            product_no = "";
                        }

                        String ad_id = (String) content.get("adid");
                        if (ad_id != null) {
                            isAd = "1";
                        }
                        String title = (String) content.get("productName");
                        String store_name = (String) content.get("mallName");
                        String count_review_str = "";
                        try {
                            count_review_str = (String) content.get("reviewCount");
                        } catch (Exception eee) {
                            Long count_review_long = (Long) content.get("reviewCount");
                            count_review_str = String.valueOf(count_review_long);

                        }
                        int count_review = Integer.valueOf(count_review_str);
                        String price_str = (String) content.get("price");
                        int price = Integer.parseInt(price_str);
                        String cat_big = (String) content.get("category1Name");
                        String cat_middle = (String) content.get("category2Name");
                        String cat_small = (String) content.get("category3Name");
                        String cat_product = (String) content.get("category4Name");
                        String product_img = (String) content.get("imageUrl");
                        String product_url = (String) content.get("mallProductUrl");
                        String is_brand = "";
                        try {
                            is_brand = (String) content.get("isBrandStore");
                        } catch (Exception e) {
                            long is_brand_long = (Long) content.get("isBrandStore");
                            is_brand = String.valueOf(is_brand_long);
                        }

                        String is_hotdeal = "";
                        try {
                            is_hotdeal = (String) content.get("isHotDeal");
                        } catch (Exception e) {
                            long is_hotdeal_long = (Long) content.get("isHotDeal");
                            is_hotdeal = String.valueOf(is_hotdeal_long);
                        }
                        String isExceptedBest100 = "";
                        try {
                            isExceptedBest100 = (String) content.get("isExceptedBest100");
                        } catch (Exception e) {
                            long isExceptedBest100_long = (Long) content.get("isExceptedBest100");
                            isExceptedBest100 = String.valueOf(isExceptedBest100_long);
                        }

                        String search_keyword = (String) content.get("searchKeyword");
                        String open_date = (String) content.get("openDate");
                        String docId = (String) content.get("docid");
                        String parentId = (String) content.get("parentId");
                        String newNormHit2 = "";
                        try {
                            newNormHit2 = String.valueOf((Double) content.get("newNormHit2"));
                        } catch (Exception e) {
                            newNormHit2 = String.valueOf((Long) content.get("newNormHit2"));
                        }
                        String simillarImageCnt = (String) content.get("mblSimImgSgntCnt");
                        String keepCnt = "";
                        try {
                            keepCnt = String.valueOf((Long) content.get("keepCnt"));
                        } catch (Exception e) {
                            keepCnt = (String) content.get("keepCnt");
                        }
                        pricexcount += price * count_review;
                        String register_date = open_date.substring(0, 8);
                        java.util.Date reg_date = new java.text.SimpleDateFormat("yyyyMMdd").parse(register_date);
                        int count_buy = -1;
                        String count_buy_str = "";
                        try {
                            count_buy_str = String.valueOf((Long) content.get("purchaseCnt"));
                        } catch (Exception e) {
                            count_buy_str = (String) content.get("purchaseCnt");
                        }
                        try {
                            count_buy = Integer.parseInt(count_buy_str);
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        Long insert_timestamp = System.currentTimeMillis() / 1000;
                        Long register_timestamp = reg_date.getTime() / 1000;
                        String store_id = (String) content.get("mallId");
                        String store_url = (String) content.get("mallPcUrl");
                        String store_url_name = "";
                        try {
                            store_url_name = store_url.split("/products/")[0].split("smartstore\\.naver\\.com/")[1];
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        String store_grade = "가격비교";
                        String goodservice = "";
                        String store_desc = "";
                        String register_number = "";
                        String tongsin_number = "";
                        int product_count = -1;
                        String address = "";
                        String logo_image = "";
                        String talk_id = "";
                        int revenue = -1;
                        int review_count = -1;
                        int visit_count = -1;
                        int zzim_count = -1;
                        String best10_image = null;
                        try {
                            JSONObject mallInfoCache = (JSONObject) content.get("mallInfoCache");
                            String grade_code = (String) mallInfoCache.get("mallGrade");

                            if (grade_code != null) {
                                if (grade_code.equals("M44006")) {
                                    store_grade = "씨앗";
                                }
                                if (grade_code.equals("M44005")) {
                                    store_grade = "새싹";
                                }
                                if (grade_code.equals("M44004")) {
                                    store_grade = "파워";
                                }
                                if (grade_code.equals("M44003")) {
                                    store_grade = "빅파워";
                                }
                                if (grade_code.equals("M44002")) {
                                    store_grade = "프리미엄";
                                }
                                if (grade_code.equals("M44001")) {
                                    store_grade = "플래티넘";
                                }
                            }
                            Boolean goodservice_bool = (Boolean) mallInfoCache.get("goodService");
                            goodservice = goodservice_bool.toString();
                            store_desc = (String) mallInfoCache.get("mallIntroduction");
                            register_number = (String) mallInfoCache.get("businessNo");
                            tongsin_number = (String) mallInfoCache.get("onmktRegisterNo");
                            talk_id = (String) mallInfoCache.get("talkAccountId");
                            product_count = Integer.parseInt((String) mallInfoCache.get("prodCnt"));
                            address = (String) mallInfoCache.get("bizplBaseAddr");
                            logo_image = (String) ((JSONObject) mallInfoCache.get("mallLogos")).get("FORYOU");
                        } catch (Exception ex) {

                        }
                        int category_comparison = 0;
                        if (product_no.equals("")) {
                            category_comparison = 1;
                            product_url = (String) content.get("crUrl");
                            ArrayList low_list = (ArrayList) content.get("lowMallList");
                            try {
                                for (int j = 0; j < low_list.size(); j++) {
                                    String pid = (String) ((JSONObject) low_list.get(j)).get("mallPid");
                                    if (pid.equals(this_product_no)) {
                                        HashMap<String, String> product_map = new HashMap<>();
                                        product_map.put("keyword", keyword);
                                        product_map.put("rank", String.valueOf(rank));
                                        product_map.put("total_cnt", total_cnt_str);
                                        product_map.put("is_brand", is_brand);
                                        product_map.put("is_hotdeal", is_hotdeal);
                                        product_map.put("category_comparison", String.valueOf(category_comparison));
                                        product_map.put("isAd", isAd);
                                        keywords_array.add(product_map);
                                        break;
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        if (category_comparison == 0) {
                            if (product_no.equals(this_product_no)) {
                                HashMap<String, String> product_map = new HashMap<>();
                                product_map.put("keyword", keyword);
                                product_map.put("rank", String.valueOf(rank));
                                product_map.put("total_cnt", total_cnt_str);
                                product_map.put("is_brand", is_brand);
                                product_map.put("is_hotdeal", is_hotdeal);
                                product_map.put("category_comparison", String.valueOf(category_comparison));
                                product_map.put("isAd", isAd);
                                keywords_array.add(product_map);
                            }
                        }
                        Boolean isSmartStore = false;
                        try {
                            long pd = Long.parseLong(product_no);
                            isSmartStore = true;
                        } catch (Exception ex) {
                            isSmartStore = false;
                        }
                        try {
                            if (isSmartStore) {
                                if (insert_flag > 0) {
                                    Connection conn_ks = mysql.initConnect(DB_IP);
                                    if (!store_url_name.equals("")) {
                                        JSONObject store_obj = new JSONObject();
                                        store_obj.put("store_id", store_id);
                                        store_obj.put("store_name", store_name);
                                        store_obj.put("store_url_name", store_url_name);
                                        store_obj.put("store_desc", store_desc);
                                        store_obj.put("store_grade", store_grade);
                                        store_obj.put("talk_id", talk_id);
                                        store_obj.put("address", address);
                                        store_obj.put("register_number", register_number);
                                        store_obj.put("tongsin_number", tongsin_number);
                                        store_obj.put("logo_image", logo_image);
                                        store_obj.put("goodservice", goodservice);
                                        store_obj.put("product_count", product_count);
                                        store_obj.put("store_url", store_url);
                                        store_obj.put("revenue", revenue);
                                        store_obj.put("review_count", review_count);
                                        store_obj.put("visit_count", visit_count);
                                        store_obj.put("zzim_count", zzim_count);
                                        store_obj.put("best10_image", best10_image);
                                        String insert_query0 = "insert into naver_store(store_id, store_name, store_url_name, store_desc, store_grade, talk_id, address, register_number, tongsin_number, logo_image, goodservice, product_count, store_url, revenue, review_count, visit_count, zzim_count, best10_image, data) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        mysql.insertNaverStore(conn_ks, insert_query0, store_obj);
                                    }
                                    String insert_query2 = "insert into keyword_hashtag(keyword_product_no, keyword, product_no, title, product_url, store_name, count_review, price, pricexcount_review, cat_big, cat_middle, cat_small, cat_product, product_img, ranking, total_cnt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                    String keyword_product_no = keyword + "," + product_no;
                                    if (!product_no.equals("")) {
                                        mysql.insertKeywordHashtag(conn_ks, insert_query2, keyword_product_no, keyword, product_no, title, product_url, store_name, count_review, price, price * count_review, cat_big, cat_middle, cat_small, cat_product, product_img, rank, Integer.parseInt(total_cnt_str));
                                    }
                                    if (product_url != null) {
                                        String insert_query = "insert into naver_light(product_no,product_url, product_img, price, cat_full, cat_big, cat_middle, cat_small, cat_product,count_review, count_buy, count_zzim, insert_timestamp, register_date, register_timestamp, title, store_name, store_grade, star, category_comparison,docid,parentid,normhit,similar_image_cnt, keep_cnt,isExceptedBest100, is_brand,is_hotdeal,search_keyword,open_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        mysql.insertProductInfoOfNaverLight(conn_ks, insert_query, product_no, product_url, product_img, price, cat_big + ">" + cat_middle + ">" + cat_small + ">" + cat_product, cat_big, cat_middle, cat_small, cat_product, count_review, count_buy, zzim_count, insert_timestamp, register_date, register_timestamp, title, store_name, store_grade, 0.0, category_comparison, docId, parentId, newNormHit2, simillarImageCnt, keepCnt, isExceptedBest100, is_brand, is_hotdeal, search_keyword, open_date);
                                    }
                                    conn_ks.close();
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }


                    for (int k = 0; k < keywords_array.size(); k++) {
                        if (keywords_array.get(k).get("keyword").equals(keyword)) {
                            keywords_array.get(k).put("pricexcount_review", String.valueOf(pricexcount));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Collections.sort(keywords_array, new Comparator<HashMap>() {
                    @Override
                    public int compare(HashMap o1, HashMap o2) {
                        String v1 = (String) o1.get("pricexcount_review");
                        String v3 = (String) o2.get("pricexcount_review");
                        return v1.compareTo(v3);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return keywords_array;
    }


    /**
     * 키워드-상품번호 매칭 후 keyword_hashtag 테이블에 INSERT. 경쟁관계 파악용
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할개수
     */
    public void baseProcess (String part_number, String total_numbers){

        try {
            driver = DriverControl.getGeneralDriver();
            driver.get("https://shopping.naver.com");
            Thread.sleep(5000);
            ArrayList<String[]> categories = new ArrayList();
            JSONParser parser = new JSONParser();
            Connection conn_select = mysql.initConnect(DB_IP);
            ArrayList<HashMap<String, String>> arr = mysql.selectDatalab(conn_select, "select * from datalab_insight");
            conn_select.close();
            for (int d = 0; d < arr.size(); d++) {

                HashMap<String, String> hs = arr.get(d);
                String keyword_total_str = (String) hs.get("keyword_total");
                JSONArray keyword_total = (JSONArray) parser.parse(keyword_total_str);
                for (int k = 0; k < keyword_total.size(); k++) {
                    String keyword = (String) keyword_total.get(k);

                    String shop_url = "https://search.shopping.naver.com/search/all.nhn?query=" + keyword;
                    String[] strs = new String[6];
                    strs[4] = shop_url;
                    strs[5] = keyword;
                    categories.add(strs);

                }
            }
            System.out.println("키워드정보 수집 완료!");
            int slice_counts = Math.round(categories.size() / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point - slice_counts;
            if(categories.size()-end_point < slice_counts) {
                end_point = categories.size();
            }
            for (int p = starting_point; p < end_point; p++) {
                try {
                    String list_url = categories.get(p)[4];
                    driver.get(list_url);
                } catch (NoSuchMethodError eee) {
                    continue;
                }
                HashMap<String, JSONObject> list_data = nsls.getListData(driver);
                for (String product_url : list_data.keySet()) {

                    String product_no = (String) list_data.get(product_url).get("product_no");
                    String title = (String) list_data.get(product_url).get("title");
                    String store_name = (String) list_data.get(product_url).get("store_name");
                    int count_review = (Integer) list_data.get(product_url).get("count_review");
                    int price = (Integer) list_data.get(product_url).get("price");
                    String cat_big = (String) list_data.get(product_url).get("cat_big");
                    String cat_middle = (String) list_data.get(product_url).get("cat_middle");
                    String cat_small = (String) list_data.get(product_url).get("cat_small");
                    String cat_product = (String) list_data.get(product_url).get("cat_product");
                    String product_img = (String) list_data.get(product_url).get("product_img");
                    int rank = (Integer) list_data.get(product_url).get("rank");
                    int total_cnt = Integer.parseInt((String) list_data.get(product_url).get("total_cnt"));
                    String insert_query2 = "replace into keyword_hashtag(keyword_product_no, keyword, product_no, title, product_url, store_name, count_review, price, pricexcount_review, cat_big, cat_middle, cat_small, cat_product, product_img, ranking, total_cnt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    String keyword_product_no = categories.get(p)[5] + "," + product_no;
                    Connection conn_ks = mysql.initConnect(DB_IP);
                    mysql.insertKeywordHashtag(conn_ks, insert_query2, keyword_product_no, categories.get(p)[5], product_no, title, product_url, store_name, count_review, price, price * count_review, cat_big, cat_middle, cat_small, cat_product, product_img, rank, total_cnt);
                    conn_ks.close();
                }
                Thread.sleep(3000);
            }
        } catch (TimeoutException timeout) {
            System.out.println(timeout.getMessage());
            driver.navigate().refresh();
        } catch (org.json.simple.parser.ParseException e1) {
            //e1.printStackTrace();
        } catch (InterruptedException e2) {
            //e2.printStackTrace();
        } catch (SQLException e3) {
            //e3.printStackTrace();
        }
        DriverControl.quitDriver(driver);;
    }

    public void updateGrowthQue() {

        try {
            final int array_num = 100;
            final int thread_num = 4;
            UpdateGrowthObject rankObject = new UpdateGrowthObject(array_num);
            ArrayList<Thread> array_threads = new ArrayList<>();
            Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
            String query_max = "select max(id) as rownum from datalab_plus";
            int rownum = mysql.selectRowNumber(conn, query_max);
            conn.close();
            for(int t=0; t < thread_num; t++){
                Thread thread = new QueUpdateGrowthThread(rankObject);
                thread.setName("Thread-" + (t+1));
                array_threads.add(thread);

            }
            for(int j =0; j < array_threads.size(); j++) {
                array_threads.get(j).start();
            }
            for(int j =0; j < array_threads.size(); j++) {
                array_threads.get(j).join();
            }
            System.out.println("Finish");
            insertGrowthData(rankObject);

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    public void updateGrowth() {
        try {
            final int array_num = 10000;
            final int thread_num = 10;
            UpdateGrowthObject rankObject = new UpdateGrowthObject(array_num);
            ArrayList<Thread> array_threads = new ArrayList<>();
            Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
            String query_max = "select max(id) as rownum from datalab_plus";
            int rownum = mysql.selectRowNumber(conn, query_max);
            conn.close();
            int slice_counts = Math.round(rownum / thread_num);

            for(int part_number=1; part_number <= thread_num; part_number++) {

                int end_point = slice_counts * part_number;
                int starting_point = end_point - slice_counts;
                if (rownum - end_point < slice_counts) {
                    end_point = rownum;
                }
                Thread thread = new UpdateGrowthThread(starting_point, end_point, rankObject);
                thread.setName("Thread-" + part_number);
                array_threads.add(thread);

            }
            for(int j =0; j < array_threads.size(); j++) {
                array_threads.get(j).start();
            }
            for(int j =0; j < array_threads.size(); j++) {
                array_threads.get(j).join();
            }
            System.out.println("Finish");
            insertGrowthData(rankObject);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    public void insertGrowth() {

        try {
            System.out.println("디비를 조회하고 있습니다...");
            Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
            String key = "$.\"월평균성장률12개월\"";
            String dplus_query = "select product_no,title,cat_full,url,revenue,revenue3m,revenue6m,revenue12m,growth,store_name,category_comparison,seller_grade,overseas,insert_time, data from datalab_plus where JSON_EXTRACT(data, '"+ key + "') <9999  and revenue12m > 10000000 and cat_big != '패션' and insert_time >= '2022-02-01'  order by JSON_EXTRACT(data, '"+ key + "') desc limit 10000";
            JSONArray array = mysql.selectDatalabPlusForGrowth(conn, dplus_query);
            conn.close();

            JSONObject data = new JSONObject();
            double max = -1f;
            double min = 999999999999f;
            for(int j=0; j < array.size(); j++) {

                try {
                    JSONObject obj = (JSONObject) array.get(j);
                    data = (JSONObject) parser.parse((String) obj.get("data"));
                    double revenue12m = (double) data.get("revenue12m");
                    if (revenue12m > max) {
                        max = revenue12m;
                    }
                    if (revenue12m < min) {
                        min = revenue12m;
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            for(int j=0; j < array.size(); j++) {

                try {
                     JSONObject obj2 = (JSONObject) array.get(j);
                    String revenue_class_name = Calculator.getHistNameSimple(max, min, Double.parseDouble((String) obj2.get("revenue12m")), array.size());
                    obj2.put("revenue12m_classname", revenue_class_name);

                    data = (JSONObject) parser.parse((String) obj2.get("data"));
                    try {
                        obj2.put("growth_3m", (double) data.get("월평균성장률3개월"));
                    }catch(Exception ex){
                        ex.printStackTrace();
                        obj2.put("growth_3m", -999.0);
                    }
                    try {
                        obj2.put("growth_6m", (double) data.get("월평균성장률6개월"));
                    }catch(Exception ex){
                        ex.printStackTrace();
                        obj2.put("growth_6m", -999.0);
                    }
                    try {
                        obj2.put("growth_12m", (double) data.get("월평균성장률12개월"));
                    }catch(Exception ex){
                        ex.printStackTrace();
                        obj2.put("growth_12m", -999.0);
                    }
                    try {
                        obj2.put("growth", (double) data.get("월평균성장률"));
                    }catch(Exception ex){
                        ex.printStackTrace();
                        obj2.put("growth", -999.0);
                    }
                    obj2.put("data", null);

                }catch(Exception ex2) {
                    ex2.printStackTrace();
                }
            }

            Connection conn2 = mysql.initConnect(Conf.NAVER_DB_IP);
            mysql.insertGrowth(conn2, "insert into growth(data) values(?)", array.toJSONString());
            conn2.close();
            System.out.println("growth 인서트 완료");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void insertGrowthData(UpdateGrowthObject rankObject) {
        try{
            JSONArray array = new JSONArray();
            JSONObject obj = new JSONObject();
            String growth = "";
            String growth6m = "";
            String growth12m ="";
            double revenue12m = 0L;
            JSONObject data = new JSONObject();
            double max = -1f;
            double min = 999999999999f;

            Connection cons = mysql.initConnect(Conf.NAVER_DB_IP);
            for(int k=0; k < rankObject.getArrayList().size(); k++) {
                JSONArray result = mysql.selectDatalabPlusForGrowth(cons, "select product_no,title,cat_full,url,revenue,revenue3m,revenue6m,revenue12m,growth,store_name,category_comparison,seller_grade,overseas,insert_time, data from datalab_plus where product_no='" + rankObject.getArrayList().get(k).getProductNo() + "'");
                obj = (JSONObject)result.get(0);

                data = (JSONObject) parser.parse((String)obj.get("data"));
                growth = String.valueOf((double) data.get("월평균성장률"));
                growth6m = String.valueOf((double) data.get("월평균성장률6개월"));
                growth12m = String.valueOf((double) data.get("월평균성장률12개월"));
                revenue12m =(double) data.get("revenue12m");

                obj.put("growth", growth);
                obj.put("growth6m", growth6m);
                obj.put("growth12m", growth12m);

                if(revenue12m > max) {
                    max = revenue12m;
                }
                if(revenue12m < min) {
                    min = revenue12m;
                }


                array.add(obj);
            }
            cons.close();
            for(int j=0; j < array.size(); j++) {
                JSONObject obj2 = (JSONObject) array.get(j);
                String revenue_class_name = Calculator.getHistNameSimple(max, min, Double.parseDouble((String)obj2.get("revenue12m")), array.size());
                obj2.put("revenue12m_classname", revenue_class_name);
            }

            Connection conn2 = mysql.initConnect(Conf.NAVER_DB_IP);
            mysql.insertGrowth(conn2, "insert into growth(data) values(?)", array.toJSONString());
            conn2.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}

