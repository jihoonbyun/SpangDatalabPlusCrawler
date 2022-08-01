package Naver;

import Util.DriverControl;
import Util.Mcode;
import Connection.MySQLConnector;
import Util.Conf;
import Util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.Connection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class KeywordExposed  {
    ;
    MySQLConnector mysql = new MySQLConnector();
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    Mcode mc = new Mcode();
    int KEYWORD_START_NUMBER = 0;
    int KEYWORD_NUMBERS = 10;
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
    String today_date = format1.format(cal.getTime());
    ChromeDriver driver = null;


    /**
     * 데이터랩 전체 키워드 광고 효율 계산
     *
     * @param part 시작지점
     * @param total 전체분할개수
     */
    public void getKeywordAdEffeciency(int part, int total) {

        JSONObject obj = null;
        JSONArray arr2 = null;
        JSONParser parser = new JSONParser();
        NaverReviewScrapper nr = new NaverReviewScrapper(Conf.NAVER_DB_IP);
        NaverShoppingLightScrapper nsls = new NaverShoppingLightScrapper(Conf.NAVER_DB_IP);
        KeywordToolPage ktp = new KeywordToolPage();
        Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
        driver = DriverControl.getGeneralDriver();
        ktp.accessKeywordToolPage(driver);
        ArrayList<HashMap<String, String>> datalab_list = mysql.selectDatalab(conn, "select * from datalab_insight");
        HashMap<String, String> keyword_map = new HashMap<>();
        ArrayList<String> keyword_list = new ArrayList<>();
        HashMap<String, ArrayList<String>> sk_id_keywords = new HashMap<>();

        for (int i = 0; i < datalab_list.size(); i++) {
            HashMap<String, String> data = datalab_list.get(i);
            arr2 = null;
            try {
                obj = (JSONObject) parser.parse(data.get("data_total"));
                arr2 = (JSONArray) obj.get("keyword");
            } catch (ClassCastException e1) {
                e1.printStackTrace();
            } catch (org.json.simple.parser.ParseException e2) {
                e2.printStackTrace();
            }
            if (arr2 == null) {
                try {
                    arr2 = (JSONArray) parser.parse(data.get("keyword_total"));
                } catch (NullPointerException e1) {
                    e1.printStackTrace();
                } catch (org.json.simple.parser.ParseException e2) {
                    e2.printStackTrace();
                }
            }
            sk_id_keywords.put(data.get("sk_id"), new ArrayList<>());

            int check = 0;
            for (int j = KEYWORD_START_NUMBER; j < KEYWORD_NUMBERS; j++) {
                try {
                    check = j;
                    String keyword = (String) arr2.get(j);
                    sk_id_keywords.get(data.get("sk_id")).add(keyword);
                    if (!keyword_map.containsKey(keyword)) {
                        keyword_map.put(keyword, data.get("sk_id"));
                        keyword_list.add(keyword);
                    }
                } catch (IndexOutOfBoundsException error) {
                    error.printStackTrace();
                    System.out.println("잘못된데이터 아이디:" + data.get("sk_id"));
                    continue;
                }
            }
        }

        int slice_counts = Math.round(keyword_list.size() / total);
        int end_point = slice_counts * part;
        int starting_point = end_point - slice_counts;

        if(keyword_list.size()-end_point < slice_counts) {
            end_point = keyword_list.size();
        }

        ArrayList<String> temp_arr = new ArrayList<>();
        for (int j = starting_point; j < end_point; j++) {
            String keyword = (String) keyword_list.get(j);
            temp_arr.add(keyword);
        }

        Utils.ThreadSleep(Thread.currentThread(), 10 * 1000);
        HashMap<String, int[]> hm = ktp.keywordDashboardCheck(driver, temp_arr, "300");
        keyword_loop:
        for (int j = starting_point; j < end_point; j++) {
            String keyword = (String) keyword_list.get(j);
            System.out.println("키워드:" + keyword);
            String q = "select * from keyword_1page where keyword='" + keyword + "' and insert_time <= DATE_ADD(now(), INTERVAL -15 DAY)";
            ArrayList<HashMap<String, String>> hs = mysql.selectKeywordDup(conn,q);

            if(hs.size() == 0){
                continue;
            }

            driver.get("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
            nsls.scrapDetailByUrl("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
            mc.initNaverList(0);
            Long product_cnt = (Long) ((JavascriptExecutor) driver).executeScript(mc.product_list_length);

            int review_cnt = 0;
            int revenue_sum = 0;
            int naver_cnt = 0;
            ArrayList<Integer> prices = new ArrayList<>();
            ArrayList<Integer> rev_list = new ArrayList<>();
            ArrayList<Double> revenue_list = new ArrayList<>();
            double price_avg = 0.0;
            int cnt = 0;
            String seller_data_str = "";
            int seller_count_before = 0;
            JSONArray seller_arrs;
            try {
                seller_data_str = (String) hs.get(j).get("seller_data");
                seller_count_before = Integer.parseInt(hs.get(j).get("seller_count"));
            } catch(Exception e){

            }

            try {
                seller_arrs = (JSONArray) parser.parse(seller_data_str);
            }catch (Exception e){
                seller_arrs = new JSONArray();
            }
            JSONArray today_seller_arr = new JSONArray();
            HashMap<String,Integer> seller_counter = new HashMap<>();
            int seller_count = 0;
            product_loop:
            for (int k = 0; k < product_cnt; k++) {
                try {
                    mc.initNaverList(k);
                    int count_review = -1;
                    int ranking = -1;
                    try {
                        String count_review_str = (String) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                        count_review = Format.parse(count_review_str).intValue();
                    }catch(ClassCastException ex) {
                        Long count_review_long = (Long) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                        count_review = Format.parse(String.valueOf(count_review_long)).intValue();
                    }

                    String product_title = (String) ((JavascriptExecutor) driver).executeScript(mc.product_title);
                    String product_price = (String) ((JavascriptExecutor) driver).executeScript(mc.product_price);
                    String product_url = (String) ((JavascriptExecutor) driver).executeScript(mc.product_url);
                    String product_no = (String) ((JavascriptExecutor) driver).executeScript(mc.product_no);
                    String seller_name = (String) ((JavascriptExecutor) driver).executeScript(mc.seller_name);
                    String seller_url = (String) ((JavascriptExecutor) driver).executeScript(mc.seller_url);
                    String ad_url =(String) ((JavascriptExecutor) driver).executeScript(mc.ad_url);
                    ranking = k+1;
                    int price = Format.parse(product_price).intValue();
                    price_avg += price;
                    cnt++;
                    prices.add(price);
                    revenue_sum += count_review * price;
                    if(product_url == null){
                        product_url = ad_url;
                    }
                    JSONObject seller_obj = new JSONObject();
                    seller_obj.put("seller_name", seller_name);
                    seller_obj.put("seller_url", seller_url);
                    seller_obj.put("product_name", product_title);
                    seller_obj.put("product_no", product_no);
                    seller_obj.put("product_url",product_url);
                    seller_obj.put("product_price", price);
                    seller_obj.put("product_review",count_review);
                    seller_obj.put("keyword", keyword);
                    seller_obj.put("ranking",ranking);
                    seller_obj.put("date",today_date);
                    try {
                        JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 1);
                        String latest_date = (String) ((JSONObject) before_arr.get(0)).get("date");
                        if (!today_date.equals(latest_date)) {
                            today_seller_arr.add(seller_obj);
                        }
                    }catch(ArrayIndexOutOfBoundsException e){
                        today_seller_arr.add(seller_obj);
                    }
                    if(!seller_counter.containsKey(seller_name)){
                        seller_count++;
                        seller_counter.put(seller_name,1);
                    }
                    review_cnt += count_review;
                    ArrayList<JSONObject> review_list = null;
                    long reviewBasedRevenue = (long) nr.getOnlyReviewInfoBasedRevenue(product_url, 3);
                    revenue_sum += (reviewBasedRevenue * 3.5);
                    if (product_url.split("smartstore.naver.com").length > 1) {
                        naver_cnt++;
                        System.out.println("진행중:" + keyword + " " + product_title);
                    }
                } catch (ParseException erer) {
                    continue product_loop;

                } catch (NoSuchMethodError erererer) {
                    continue product_loop;
                }

            }


            seller_arrs.add(today_seller_arr);
            double seller_inflow_tangent = 0.0;
            double delta = seller_count - seller_count_before;
            double diffDays = 1;
            try {
                JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 2);
                String date_bef = (String) ((JSONObject) before_arr.get(0)).get("date");
                Date date_before = format1.parse(date_bef);
                Date date_today = format1.parse(today_date);
                double diff = date_today.getTime() - date_before.getTime();
                diffDays = diff / (24 * 60 * 60 * 1000);

            }catch(ParseException pe){
                System.out.println("이전 데이터가 없다.");
            }catch(Exception e){
                System.out.println("이전 데이터가 없다.");
            }
            Double radian = Math.atan2(delta,diffDays);
            seller_inflow_tangent = (Double) (57.295779513082323 * radian);
            double revenue_avg = revenue_sum / product_cnt;
            try {
                int total_cost = hm.get(keyword)[0]  * hm.get(keyword)[1];
                double ctr = hm.get(keyword)[0] *1.0 /  hm.get(keyword)[2]*1.0;
                if(hm.get(keyword)[2] == 0) {
                    ctr = 0;
                }
                double efficiency =  revenue_avg * 1.0 /  total_cost * 1.0;
                if(total_cost == 0){
                    efficiency = 0;
                }
                price_avg = price_avg * 1.0 / product_cnt*1.0;
                double sums = 0.0;
                double sums_for_skew = 0.0;
                double sums_for_kurto = 0.0;
                for (int s = 0; s < revenue_list.size(); s++) {
                    sums += ((revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg));
                    sums_for_skew += (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg);
                    sums_for_kurto += (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg);
                }
                double std = Math.round(Math.sqrt(sums / revenue_list.size()));
                double cv = std / revenue_avg;
                double skewness = sums_for_skew / ((std * std * std) * revenue_list.size());
                double kurtosis = sums_for_kurto / ((std * std * std * std) * revenue_list.size()) - 3;
                efficiency = Math.round(efficiency * 100) / 100.0;
                cv = Math.round(cv * 100) / 100.0;
                skewness = Math.round(skewness * 100) / 100.0;
                kurtosis = Math.round(kurtosis * 100) / 100.0;
                double price_median = getMedian(prices);
                mysql.insertKeywordOnepage(conn, "insert into keyword_1page(keyword, review_3m, revenue_avg_3m, adcost,revenue_adcost,std,cv,skewness,kurtosis, ctr, expect_exposed, expect_click, expect_clickcost,price_avg, price_median,revenue_sum, seller_data, seller_count, seller_inflow_tangent) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", keyword, review_cnt, revenue_avg, total_cost, efficiency, std, cv, skewness, kurtosis,ctr,hm.get(keyword)[2],hm.get(keyword)[0],hm.get(keyword)[1],price_avg,price_median,revenue_sum, seller_arrs.toJSONString(), seller_count, seller_inflow_tangent);
                Long catalog_size = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_model_list').length");
                String category = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[0].children[1].children[2].textContent.trim().replace(/ /g, \"\").replace(/\\n/gi, \"\")");
                mysql.update1pageCatalog(conn, "update keyword_1page set catalog=?, category=? where keyword=?", catalog_size, category, keyword);

            } catch (Exception er) { ;
                continue keyword_loop;
            } catch (NoSuchMethodError er2) {
                continue keyword_loop;
            }


        }

    }

    /**
     * 데이터랩 전체 키워드 광고 효율 계산 (특정 키워드 리스트)
     *
     * @param keyword_list 키워드리스트
     */
    public void getKeywordAdEffeciencyManyKeywords(ArrayList<String> keyword_list) {

        JSONObject obj = null;
        JSONArray arr2 = null;
        JSONParser parser = new JSONParser();
        Conf mv = new Conf();
        NaverReviewScrapper nr = new NaverReviewScrapper(mv.NAVER_DB_IP);
        NaverShoppingLightScrapper nsls = new NaverShoppingLightScrapper(mv.NAVER_DB_IP);
        KeywordToolPage ktp = new KeywordToolPage();

        Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
        driver = DriverControl.getGeneralDriver();
        ktp.accessKeywordToolPage(driver);
        try {
            ktp.accessKeywordToolPage(driver);
        }catch(Exception er){

        }
        HashMap<String, int[]> hm = ktp.keywordDashboardCheck(driver, keyword_list, "300");
        keyword_loop:
        for (int j = 0; j < keyword_list.size(); j++) {

            String keyword = (String) keyword_list.get(j);
            System.out.println("키워드:" + keyword);
            String q = "select * from keyword_1page where keyword='" + keyword + "' and insert_time <= DATE_ADD(now(), INTERVAL -15 DAY)";
            ArrayList<HashMap<String, String>> hs = mysql.selectKeywordDup(conn,q);
            if(hs.size() == 0){
                continue;
            }
            driver.get("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
            mc.initNaverList(0);
            Long product_cnt = (Long) ((JavascriptExecutor) driver).executeScript(mc.product_list_length);

            int review_cnt = 0;
            int revenue_sum = 0;
            int naver_cnt = 0;
            ArrayList<Integer> prices = new ArrayList<>();
            ArrayList<Integer> rev_list = new ArrayList<>();
            ArrayList<Double> revenue_list = new ArrayList<>();
            double price_avg = 0.0;
            int cnt = 0;
            String seller_data_str = "";
            int seller_count_before = 0;
            JSONArray seller_arrs;
            try {
                seller_data_str = (String) hs.get(j).get("seller_data");
                seller_count_before = Integer.parseInt(hs.get(j).get("seller_count"));
            } catch(Exception e){

            }
            try {
                seller_arrs = (JSONArray) parser.parse(seller_data_str);
            }catch (Exception e){
                seller_arrs = new JSONArray();
            }
            JSONArray today_seller_arr = new JSONArray();
            HashMap<String,Integer> seller_counter = new HashMap<>();
            int seller_count = 0;
            product_loop:
            for (int k = 0; k < product_cnt; k++) {
                try {
                    mc.initNaverList(k);
                    int count_review = -1;
                    int ranking = -1;
                    try {
                        String count_review_str = (String) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                        count_review = Format.parse(count_review_str).intValue();
                    }catch(ClassCastException ex) {
                        Long count_review_long = (Long) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                        count_review = Format.parse(String.valueOf(count_review_long)).intValue();
                    }

                    String product_title = (String) ((JavascriptExecutor) driver).executeScript(mc.product_title);
                    String product_price = (String) ((JavascriptExecutor) driver).executeScript(mc.product_price);
                    String product_url = (String) ((JavascriptExecutor) driver).executeScript(mc.product_url);
                    String product_no = (String) ((JavascriptExecutor) driver).executeScript(mc.product_no);
                    String seller_name = (String) ((JavascriptExecutor) driver).executeScript(mc.seller_name);
                    String seller_url = (String) ((JavascriptExecutor) driver).executeScript(mc.seller_url);
                    String ad_url =(String) ((JavascriptExecutor) driver).executeScript(mc.ad_url);
                    ranking = k+1;
                    int price = Format.parse(product_price).intValue();
                    price_avg += price;
                    cnt++;
                    prices.add(price);
                    revenue_sum += count_review * price;
                    if(product_url == null){
                        product_url = ad_url;
                    }

                    JSONObject seller_obj = new JSONObject();
                    seller_obj.put("seller_name", seller_name);
                    seller_obj.put("seller_url", seller_url);
                    seller_obj.put("product_name", product_title);
                    seller_obj.put("product_no", product_no);
                    seller_obj.put("product_url",product_url);
                    seller_obj.put("product_price", price);
                    seller_obj.put("product_review",count_review);
                    seller_obj.put("keyword", keyword);
                    seller_obj.put("ranking",ranking);
                    seller_obj.put("date",today_date);

                    try {
                        JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 1);
                        String latest_date = (String) ((JSONObject) before_arr.get(0)).get("date");
                        if (!today_date.equals(latest_date)) {
                            today_seller_arr.add(seller_obj);
                        }
                    }catch(ArrayIndexOutOfBoundsException e){
                        today_seller_arr.add(seller_obj);
                    }
                    if(!seller_counter.containsKey(seller_name)){
                        seller_count++;
                        seller_counter.put(seller_name,1);
                    }
                    review_cnt += count_review;
                    ArrayList<JSONObject> review_list = null;
                    int reviewBasedRevenue = (int) nr.getOnlyReviewInfoBasedRevenue(product_url, 3);
                    revenue_sum += (reviewBasedRevenue * 3.5);
                    if (product_url.split("smartstore.naver.com").length > 1) {
                        naver_cnt++;
                        System.out.println("진행중:" + keyword + " " + product_title);
                    }
                } catch (ParseException erer) {
                    continue product_loop;

                } catch (NoSuchMethodError erererer) {
                    continue product_loop;
                }

            }

            seller_arrs.add(today_seller_arr);
            double seller_inflow_tangent = 0.0;
            double delta = seller_count - seller_count_before;
            double diffDays = 1;
            try {
                JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 2);
                String date_bef = (String) ((JSONObject) before_arr.get(0)).get("date");
                Date date_before = format1.parse(date_bef);
                Date date_today = format1.parse(today_date);
                double diff = date_today.getTime() - date_before.getTime();
                diffDays = diff / (24 * 60 * 60 * 1000);

            }catch(ParseException pe){
                System.out.println("이전 데이터가 없다.");
            }catch(Exception e){
                System.out.println("이전 데이터가 없다.");
            }
            Double radian = Math.atan2(delta,diffDays);
            seller_inflow_tangent = (Double) (57.295779513082323 * radian);
            double revenue_avg = revenue_sum / product_cnt;
            try {
                int total_cost = hm.get(keyword)[0]  * hm.get(keyword)[1];
                double ctr = hm.get(keyword)[0] *1.0 /  hm.get(keyword)[2]*1.0;
                if(hm.get(keyword)[2] == 0) {
                    ctr = 0;
                }
                double efficiency =  revenue_avg * 1.0 /  total_cost * 1.0;
                if(total_cost == 0){
                    efficiency = 0;
                }
                price_avg = price_avg * 1.0 / product_cnt*1.0;
                double sums = 0.0;
                double sums_for_skew = 0.0;
                double sums_for_kurto = 0.0;
                for (int s = 0; s < revenue_list.size(); s++) {
                    sums += ((revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg));
                    sums_for_skew += (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg);
                    sums_for_kurto += (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg) * (revenue_list.get(s) - revenue_avg);
                }
                double std = Math.round(Math.sqrt(sums / revenue_list.size()));
                double cv = std / revenue_avg;
                double skewness = sums_for_skew / ((std * std * std) * revenue_list.size());
                double kurtosis = sums_for_kurto / ((std * std * std * std) * revenue_list.size()) - 3;
                efficiency = Math.round(efficiency * 100) / 100.0;
                cv = Math.round(cv * 100) / 100.0;
                skewness = Math.round(skewness * 100) / 100.0;
                kurtosis = Math.round(kurtosis * 100) / 100.0;
                double price_median = getMedian(prices);

                mysql.insertKeywordOnepage(conn, "insert into keyword_1page(keyword, review_3m, revenue_avg_3m, adcost,revenue_adcost,std,cv,skewness,kurtosis, ctr, expect_exposed, expect_click, expect_clickcost,price_avg, price_median,revenue_sum, seller_data, seller_count, seller_inflow_tangent) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", keyword, review_cnt, revenue_avg, total_cost, efficiency, std, cv, skewness, kurtosis,ctr,hm.get(keyword)[2],hm.get(keyword)[0],hm.get(keyword)[1],price_avg,price_median,revenue_sum, seller_arrs.toJSONString(), seller_count, seller_inflow_tangent);
                mysql.insertKeywordOnepage(conn, "insert into keyword_1page_report(keyword, review_3m, revenue_avg_3m, adcost,revenue_adcost,std,cv,skewness,kurtosis, ctr, expect_exposed, expect_click, expect_clickcost,price_avg, price_median,revenue_sum, seller_data, seller_count, seller_inflow_tangent) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", keyword, review_cnt, revenue_avg, total_cost, efficiency, std, cv, skewness, kurtosis,ctr,hm.get(keyword)[2],hm.get(keyword)[0],hm.get(keyword)[1],price_avg,price_median,revenue_sum, seller_arrs.toJSONString(), seller_count, seller_inflow_tangent);
                Long catalog_size = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_model_list').length");
                String category = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[0].children[1].children[2].textContent.trim().replace(/ /g, \"\").replace(/\\n/gi, \"\")");
                mysql.update1pageCatalog(conn, "update keyword_1page set catalog=?, category=? where keyword=?", catalog_size, category, keyword);
                mysql.update1pageCatalog(conn, "update keyword_1page_report set catalog=?, category=? where keyword=?", catalog_size, category, keyword);

            } catch (Exception er) { ;
                continue keyword_loop;
            } catch (NoSuchMethodError er2) {
                continue keyword_loop;
            }


        }

    }

    /**
     * 데이터랩 전체 키워드 광고 효율 계산 (1 키워드)
     *
     * @param keyword 키워드
     */
    public void getKeywordAdEffeciencyOneKeyword(String keyword) {

        ChromeDriver driver = null;

        JSONObject obj = null;
        JSONArray arr2 = null;
        JSONParser parser = new JSONParser();


        Conf mv = new Conf();
        NaverReviewScrapper nr = new NaverReviewScrapper(mv.NAVER_DB_IP);
        NaverShoppingLightScrapper nsls = new NaverShoppingLightScrapper(mv.NAVER_DB_IP);
        Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
        driver = DriverControl.getGeneralDriver();
        HashMap<String, String> keyword_map = new HashMap<>();
        ArrayList<String> keyword_list = new ArrayList<>();
        HashMap<String, ArrayList<String>> sk_id_keywords = new HashMap<>();
        ArrayList<String> temp_arr = new ArrayList<>();
        temp_arr.add(keyword);
        try {
            Thread.sleep(10 * 1000);
        }catch(Exception er){

        }
        String q = "select * from keyword_1page where keyword='" + keyword + "' and insert_time <= DATE_ADD(now(), INTERVAL -15 DAY)";
        ArrayList<HashMap<String, String>> hs = mysql.selectKeywordDup(conn, q);
        driver.get("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
        Long leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection').length");
        nsls.scrapDetailByUrl("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
        int review_cnt = 0;
        int revenue_sum = 0;
        int naver_cnt = 0;
        ArrayList<Integer> prices = new ArrayList<>();
        ArrayList<Integer> rev_list = new ArrayList<>();
        double price_avg = 0.0;
        int cnt = 0;
        String seller_data_str = "";
        int seller_count_before = 0;
        JSONArray seller_arrs;
        try {
            seller_data_str = (String) hs.get(0).get("seller_data");
            seller_count_before = Integer.parseInt(hs.get(0).get("seller_count"));
        } catch(Exception e){

        }

        try {
            seller_arrs = (JSONArray) parser.parse(seller_data_str);
        }catch (Exception e){
            seller_arrs = new JSONArray();
        }
        JSONArray today_seller_arr = new JSONArray();
        HashMap<String,Integer> seller_counter = new HashMap<>();
        int seller_count = 0;
        for (int k = 0; k < leng; k++) {
            String product_title = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[" + k + "].children[1].children[0].textContent.trim()");
            try {
                String product_price = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_price_reload')[" + k + "].textContent.trim()");
                int price = Format.parse(product_price).intValue();
                price_avg += price;
                cnt++;
                prices.add(price);
                int count_review = -1;
                Long span_list = (Long) ((JavascriptExecutor) driver).executeScript("return $('.goods_list > li').eq(" + k + ").children('.info').children('.etc').children().length");
                for (int span = 0; span < span_list; span++) {
                    String span_text = (String) ((JavascriptExecutor) driver).executeScript("return $('.goods_list > li').eq(" + k + ").children('.info').children('.etc').children().eq(" + span + ").text()");
                    String[] review_split = span_text.split("뷰");
                    String[] buy_split = span_text.split("건수");

                    if (review_split.length > 1) {
                        count_review = Format.parse(review_split[1]).intValue();
                    }
                }
                revenue_sum += count_review * price;
                revenue_sum = 0;

                String mall_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall_txt')[" + k + "].querySelector('a').href");
                String product_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.info')[" + k + "].querySelector('a').href");
                String product_no = "";
                try {
                    product_no = (String) ((JavascriptExecutor) driver).executeScript("return $('.goods_list > li').eq(" + k + ").attr('data-mall-pid')");
                } catch (NoSuchMethodError er) {

                }
                String seller_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[" + k + "].children[2].children[0].children[0].textContent");
                String seller_url = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[" + k + "].children[2].children[0].children[0].href");
                JSONObject seller_obj = new JSONObject();
                seller_obj.put("seller_name", seller_name);
                seller_obj.put("seller_url", seller_url);
                seller_obj.put("product_name", product_title);
                seller_obj.put("product_no", product_no);
                seller_obj.put("product_url",product_url);
                seller_obj.put("product_price", price);
                seller_obj.put("product_review",count_review);
                seller_obj.put("keyword", keyword);
                seller_obj.put("ranking",k);
                seller_obj.put("date",today_date);
                try {
                    JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 1);
                    String latest_date = (String) ((JSONObject) before_arr.get(0)).get("date");
                    if (!today_date.equals(latest_date)) {
                        today_seller_arr.add(seller_obj);
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    today_seller_arr.add(seller_obj);
                }
                if(!seller_counter.containsKey(seller_name)){
                    seller_count++;
                    seller_counter.put(seller_name,1);
                }
                if (mall_url.split("smartstore.naver.com").length > 1) {
                    mysql.updateNaverLightFirstPage(conn,"update naver_light set firstpage_keyword=? where product_url=?", keyword, product_url);
                    ArrayList<JSONObject> review_list = null;
                    review_list = nr.getOnlyReviewInfo(product_url, 3);
                    if (review_list.size() != 0) {

                        int revenue = review_list.size() * price;
                        revenue_sum += revenue;
                        review_cnt += review_list.size();
                        rev_list.add(revenue);
                        naver_cnt++;
                    }

                }
            } catch (ParseException erer) {
                continue;
            } catch (NoSuchMethodError erererer) {
                continue;
            }
            System.out.println("진행중:" + keyword + " " + product_title);

        }


        seller_arrs.add(today_seller_arr);
        double seller_inflow_tangent = 0.0;
        double delta = seller_count - seller_count_before;
        double diffDays = 1;
        try {
            JSONArray before_arr = (JSONArray) seller_arrs.get(seller_arrs.size() - 2);
            String date_bef = (String) ((JSONObject) before_arr.get(0)).get("date");
            Date date_before = format1.parse(date_bef);
            Date date_today = format1.parse(today_date);
            double diff = date_today.getTime() - date_before.getTime();
            diffDays = diff / (24 * 60 * 60 * 1000);

        }catch(ParseException pe){
            System.out.println("이전 데이터가 없다.");
        }catch(Exception e){
            System.out.println("이전 데이터가 없다.");
        }
        Double radian = Math.atan2(delta,diffDays);
        seller_inflow_tangent = (Double) (57.295779513082323 * radian);
        double revenue_avg = revenue_sum / naver_cnt;
        try {

            double total_cost = 0;
            double ctr = 0;
            double efficiency = revenue_avg / total_cost;
            price_avg = price_avg / cnt;
            double sums = 0.0;
            double sums_for_skew = 0.0;
            double sums_for_kurto = 0.0;

            for (int s = 0; s < rev_list.size(); s++) {
                sums += ((rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg));
                sums_for_skew += (rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg);
                sums_for_kurto += (rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg) * (rev_list.get(s) - revenue_avg);
            }
            double std = Math.round(Math.sqrt(sums / rev_list.size()));
            double cv = std / revenue_avg;
            double skewness = sums_for_skew / ((std * std * std) * rev_list.size());
            double kurtosis = sums_for_kurto / ((std * std * std * std) * rev_list.size()) - 3;
            efficiency = Math.round(efficiency * 100) / 100.0;
            cv = Math.round(cv * 100) / 100.0;
            skewness = Math.round(skewness * 100) / 100.0;
            kurtosis = Math.round(kurtosis * 100) / 100.0;
            double price_median = getMedian(prices);
            mysql.insertKeywordOnepage(conn, "insert into keyword_1page(keyword, review_3m, revenue_avg_3m, adcost,revenue_adcost,std,cv,skewness,kurtosis, ctr, expect_exposed, expect_click, expect_clickcost,price_avg, price_median,revenue_sum, seller_data, seller_count, seller_inflow_tangent) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", keyword, review_cnt, revenue_avg, total_cost, efficiency, std, cv, skewness, kurtosis,ctr,-1,-1,-1,price_avg,price_median,revenue_sum, seller_arrs.toJSONString(), seller_count, seller_inflow_tangent);
            Long catalog_size = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_model_list').length");
            String category = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[0].children[1].children[2].textContent.trim().replace(/ /g, \"\").replace(/\\n/gi, \"\")");
            mysql.update1pageCatalog(conn, "update keyword_1page set catalog=?, category=? where keyword=?", catalog_size, category, keyword);


        } catch (Exception er) {

        } catch (NoSuchMethodError er2) {

        }

        DriverControl.quitDriver(driver);

    }


    /**
     * 중앙값 구하기
     *
     * @param array 정수 배열
     */

    public static double getMedian(ArrayList<Integer> array) {
        if (array.size() == 0) return Double.NaN;
        int center = array.size() / 2;

        if (array.size() % 2 == 1) {
            return array.get(center);
        } else {
            return (array.get(center - 1) + array.get(center)) / 2.0;
        }
    }






}
