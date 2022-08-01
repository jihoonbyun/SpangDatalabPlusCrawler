package Naver;

import Util.DriverControl;
import Util.Recorder;
import Util.StringMatching;
import Connection.MySQLConnector;
import Util.Utils;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.lang.Thread.sleep;

public class NaverReviewScrapper extends NaverProductDetailClass<String,Integer,Double,Long,Timestamp> {

    MySQLConnector mysql = new MySQLConnector();
    ChromeDriver driver;
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    StringMatching lcs = new StringMatching();
    String DB_IP = null;
    Recorder rec =  new Recorder();

    public NaverReviewScrapper(String DB_IP_STR) {
        DB_IP = DB_IP_STR;
    }

    public void initDriver(){
        driver= DriverControl.getGeneralDriver();
        driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * 리뷰 데이터 수집
     *
     * @param driver 크롬드라이버
     * @param productMap 상품정보
     * @param result 리턴객체
     * @param total_reviews_cnt 전체리뷰수
     * @param opt_obj 옵션객체
     * @return 리뷰객체
     */
    public org.json.simple.JSONObject getReviewInfo(ChromeDriver driver, HashMap<String,String> productMap, org.json.simple.JSONObject result, int total_reviews_cnt, org.json.simple.JSONObject opt_obj){

        String product_num = productMap.get("product_no");
        long option_base_revenue = 0;
        long option_base_revenue3m = 0;
        long option_base_revenue6m = 0;
        long option_base_revenue12m = 0;
        long option_base_revenue0d = 0;
        long option_base_revenue1d = 0;
        long option_base_revenue7d = 0;
        long option_base_revenue1m = 0;
        int spring = 0;
        int summer = 0;
        int fall = 0;
        int winter = 0;
        int avg_price =0;


        ArrayList<Integer> month = new ArrayList<Integer>();
        ArrayList<Integer>month_3 =  new ArrayList<Integer>();
        for(int kk =0; kk < 12 ; kk++){
            month.add(0);
            month_3.add(0);
        }

        ArrayList<Integer> month_advance = new ArrayList<Integer>();
        ArrayList<Integer>month_3_advance =  new ArrayList<Integer>();
        for(int kk =0; kk < 12 ; kk++){
            month_advance.add(0);
            month_3_advance.add(0);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date da = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(da);
        cal.add(Calendar.MONTH, -1 * 3);
        Date three_monthago = cal.getTime();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(da);
        cal2.add(Calendar.MONTH, -1 * 6);
        Date six_monthago = cal2.getTime();

        Calendar cal3 = Calendar.getInstance();
        cal3.setTime(da);
        cal3.add(Calendar.MONTH, -1 * 12);
        Date twelve_monthago = cal3.getTime();

        Calendar cal4 = Calendar.getInstance();
        cal4.setTime(da);
        cal4.add(Calendar.MONTH, -1 * 1);
        Date one_monthago = cal4.getTime();

        Calendar cal5 = Calendar.getInstance();
        cal5.setTime(da);
        cal5.add(Calendar.MONTH, -1 * 2);
        Date two_monthago = cal5.getTime();

        Calendar cal6 = Calendar.getInstance();
        cal6.setTime(da);
        cal6.add(Calendar.HOUR, -1 * 24*7);
        Date thisweek = cal6.getTime();

        Calendar cal7 = Calendar.getInstance();
        cal7.setTime(da);
        cal7.add(Calendar.HOUR, -1 * 24);
        Date today = cal7.getTime();

        Calendar cal8 = Calendar.getInstance();
        cal8.setTime(da);
        cal8.add(Calendar.HOUR, -2 * 24);
        Date yesterday = cal8.getTime();

        Calendar cal11 = Calendar.getInstance();
        cal11.setTime(da);
        cal11.add(Calendar.MONTH, -1 * 11);
        Date eleven_monthago = cal11.getTime();

        Calendar cal12 = Calendar.getInstance();
        cal12.setTime(da);
        cal12.add(Calendar.MONTH, -1 * 9);
        Date nine_monthago = cal12.getTime();
        Calendar calendar = Calendar.getInstance();

        String product_url = (String)productMap.get("product_url");
        int gijonIsBigger = 0;
        org.json.simple.JSONObject obj_gijon = new org.json.simple.JSONObject();
        org.json.JSONArray exist_arr = null;
        try {
            String exist_str = (String)result.get("review_exist");
            if(exist_str != null && !exist_str.equals("[]")) {
                exist_arr = new org.json.JSONArray((String)result.get("review_exist"));

            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        String product_name =  (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3oDjSvLwq9')[0].textContent");
        ArrayList<org.json.JSONObject> review_array_elevenmonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_ninemonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_yesterday = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_today = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_thisweek = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_onemonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_twomonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_threemonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_sixmonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_twelvemonth = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_total = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_elevenmonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_ninemonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_yesterday2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_today2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_thisweek2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_onemonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_twomonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_threemonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_sixmonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_twelvemonth2 = new ArrayList<>();
        ArrayList<org.json.JSONObject> review_array_total2 = new ArrayList<>();

        org.json.simple.JSONObject year_based_revenue = new org.json.simple.JSONObject();

        int review_count = 0;
        try {

            driver.get(product_url);

            Boolean error = false;
            Long notSell = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"운영이 중지되었습니다\").length");
            Long notSell2 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매금지 된 상품입니다.\").length");
            Long notSell3 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매중지 된 상품입니다.\").length");
            Long notSell4 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"상품이 존재하지 않습니다.\").length");
            Long notSell5 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 판매할 수 없습니다\").length");
            Long notSell6 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"연령 확인이 필요한 서비스\").length");
            Long notSell7 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"현재 서비스 접속\").length");


            if(notSell > 1 || notSell2 >1 || notSell3 > 1 || notSell4 >1 || notSell5 > 1 || notSell6 > 1 || notSell7 > 1) {
                error = true;
                if (product_url.split("/products/").length == 2) {
                    String product_no = product_url.split("products/")[1].split("\\?")[0];
                    Connection con = mysql.initConnect(DB_IP);
                    mysql.updateDatalabPlusNotSell(con, "update datalab_plus set notsell=? where product_no=?", 1, product_no);
                    con.close();
                    result.put("valid_url", "invalid");
                    return result;
                }
            }


            try {
                sleep(3000);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }catch(NoSuchMethodError er){
                driver.get(product_url);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }
            try {
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_1K9yWX-Lpq')[1].children[0].click()");
                sleep(5000);
            } catch (NoSuchMethodError er) {
                System.out.println("상품이 삭제되었음");
                error =true;
            } catch(Exception ex) {
                System.out.println("리뷰가 0");
            }
            int product_counts = 0;
            int pagenums = 0;
            try {
                if (exist_arr != null) {
                    for (int j = 0; j < exist_arr.length(); j++) {
                        org.json.JSONObject obj = (org.json.JSONObject) exist_arr.get(j);
                        try {
                            String user_id = (String) obj.get("user_id");
                            String review_date = (String) obj.get("review_date");
                            String option = null;
                            try {
                                option = (String) obj.get("option");
                            } catch (Exception ex) {
                                //ex.printStackTrace();
                            }
                            String review_text = (String) obj.get("review_text");
                            String review_image = null;
                            if (obj.has("review_image")) {
                                review_image = (String) obj.get("review_image");
                            }
                            String star = (String) obj.get("star");
                            String check_key = user_id+review_date+option+review_text+star;
                            if(obj_gijon.containsKey(check_key)) {
                                obj_gijon.put(check_key, (Integer)obj_gijon.get(check_key)+1);
                            } else {
                                obj_gijon.put(check_key, 1);
                            }

                            Date review_date_date = dateFormat.parse(review_date);
                            obj.put("user_id", user_id);
                            obj.put("review_date", review_date);
                            obj.put("option", option);
                            obj.put("review_text", review_text);
                            obj.put("review_image", review_image);
                            obj.put("star", star);
                            obj.put("review_time", review_date_date.getTime());
                            int year_num = Integer.parseInt(review_date.split("-")[0]);
                            int month_num = Integer.parseInt(review_date.split("-")[1]);
                            int d = month.get(month_num - 1);
                            month.set(month_num - 1, d + 1);

                            if(option != null) {
                                int lcs_base_option_price = 0;
                                int max = 0;
                                int cnt = 0;
                                int avg_price_this = 0;
                                String lcs_key = "";
                                for (Object key : opt_obj.keySet()) {
                                    cnt++;
                                    avg_price_this += (int) opt_obj.get(key);
                                    int lcs_point = lcs.wordMatching((String) key, option);
                                    if (lcs_point > max) {
                                        max = lcs_point;
                                        lcs_base_option_price = (int) opt_obj.get(key);
                                        lcs_key = (String) key;
                                    }
                                }
                                avg_price = Integer.parseInt(String.valueOf(Math.round(avg_price_this * 1.0 / cnt * 1.0)));
                                if (opt_obj != null) {
                                    if (opt_obj.containsKey(option)) {
                                        int opt_price = (int) opt_obj.get(option);
                                        obj.put("price", opt_price);
                                        option_base_revenue += opt_price;
                                    }
                                    else {
                                        if (lcs_base_option_price == 0) {
                                            obj.put("price", avg_price);
                                            option_base_revenue += avg_price;
                                        } else {
                                            obj.put("price", lcs_base_option_price);
                                            option_base_revenue += lcs_base_option_price;
                                        }
                                    }
                                }
                            } else {
                                obj.put("price", opt_obj.get("default_price"));
                                option_base_revenue += (int) opt_obj.get("default_price");
                            }
                            if(year_based_revenue.containsKey(year_num)) {
                                long[] month_arr = (long[])year_based_revenue.get(year_num);
                                month_arr[month_num-1] = Long.valueOf(String.valueOf((month_arr[month_num-1] + (int) obj.get("price"))));
                                year_based_revenue.put(year_num, month_arr);
                            } else {
                                long[] month_arr = new long[12];
                                month_arr[month_num - 1] = Long.valueOf(String.valueOf((int) obj.get("price")));
                                year_based_revenue.put(year_num, month_arr);
                            }
                            int this_month_num = Integer.parseInt(review_date.split("-")[1])- 1;
                            int month_review_count = month.get(this_month_num);
                            month.set(this_month_num, month_review_count + 1);
                            int price = (int) obj.get("price");
                            int this_month_revenue = month_advance.get(this_month_num);
                            month_advance.set(this_month_num, this_month_revenue + price);
                            review_array_total.add(obj);
                            product_counts++;
                            if (review_date_date.getTime() >= eleven_monthago.getTime()) {
                                review_array_elevenmonth.add(obj);
                            }
                            if (review_date_date.getTime() >= nine_monthago.getTime()) {
                                review_array_ninemonth.add(obj);
                            }

                            if (review_date_date.getTime() >= yesterday.getTime()) {
                                review_array_yesterday.add(obj);
                                option_base_revenue0d += (int)obj.get("price");
                            }
                            if (review_date_date.getTime() >= today.getTime()) {
                                review_array_today.add(obj);
                                option_base_revenue1d += (int) obj.get("price");
                            }
                            if (review_date_date.getTime() > thisweek.getTime()) {
                                review_array_thisweek.add(obj);
                                option_base_revenue7d += (int) obj.get("price");
                            }
                            if (review_date_date.getTime() > one_monthago.getTime()) {
                                review_array_onemonth.add(obj);
                                option_base_revenue1m += (int) obj.get("price");
                            }
                            if (review_date_date.getTime() > two_monthago.getTime()) {
                                review_array_twomonth.add(obj);
                            }
                            if (review_date_date.getTime() >= three_monthago.getTime()) {
                                review_array_threemonth.add(obj);
                                option_base_revenue3m += (int) obj.get("price");
                            }
                            if (review_date_date.getTime() > six_monthago.getTime()) {
                                review_array_sixmonth.add(obj);
                                option_base_revenue6m += (int) obj.get("price");
                            }
                            if (review_date_date.getTime() > twelve_monthago.getTime()) {
                                review_array_twelvemonth.add(obj);
                                option_base_revenue12m += (int) obj.get("price");
                            }
                            calendar.setTime(review_date_date);
                            int rm = calendar.get(Calendar.MONTH);
                            if (rm == 3 || rm == 4 || rm == 5) {
                                spring++;
                            }
                            if (rm == 6 || rm == 7 || rm == 8) {
                                summer++;
                            }
                            if (rm == 9 || rm == 10 || rm == 11) {
                                fall++;
                            }
                            if (rm == 12 || rm == 1 || rm == 2) {
                                winter++;
                            }

                        } catch (NoSuchMethodError er) {
                            continue;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            continue;
                        }

                    }


                }
            } catch (NoSuchMethodError e) {
                e.printStackTrace();
            }

            int total_delta = Math.abs(total_reviews_cnt - review_array_total.size());
            gijonIsBigger = total_reviews_cnt - review_array_total.size();
            if(total_delta > 0) {
                loop_whie:
                while (true) {

                    try {
                        Thread.sleep(1000);
                        Long review_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56').length");
                        rec.updateMessage(product_num, "리뷰 수집중...");
                        for (int j = 0; j < review_leng; j++) {
                            org.json.JSONObject obj = new org.json.JSONObject();

                            String star = "";
                            try {

                                star = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[0].children[1].textContent");
                                String userid = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[1].children[0].textContent");
                                String review_date = "";
                                String review_date_yymmdd = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[1].children[1].textContent");
                                String[] review_dates = review_date_yymmdd.split("\\.");
                                review_date = ("20" + review_dates[0]) + "-" + (review_dates[1]) + "-" + (review_dates[2]);
                                Date review_date_date = dateFormat.parse(review_date);

                                String option = null;
                                try {
                                    option = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[2].textContent");
                                } catch (NoSuchMethodError er) {

                                } catch(Exception ex){

                                }

                                String review_text = null;
                                review_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[1].textContent");
                                System.out.println(review_text);

                                String review_image = null;
                                try {
                                    review_image = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2389dRohZq')[" + j + "].children[0].children[0].children[1].children[0].children[0].children[1].children[0].children[0].src");
                                } catch (NoSuchMethodError er) {
                                    //er.printStackTrace();
                                } catch (Exception ex) {
                                    //ex.printStackTrace();
                                }

                                int find_new = 0;
                                String check_key = userid+review_date+option+review_text+star;
                                if(obj_gijon.containsKey(check_key)) {
                                    int howmany = (Integer)obj_gijon.get(check_key);
                                    int res = howmany-1;
                                    if(res < 0) {
                                        find_new = 1;
                                    }
                                    obj_gijon.put(check_key, res);

                                } else {
                                    find_new = 1;
                                }

                                if(find_new == 1) {
                                    obj.put("user_id", userid);
                                    obj.put("review_date", review_date);
                                    obj.put("option", option);
                                    obj.put("review_text", review_text);
                                    obj.put("review_image", review_image);
                                    obj.put("star", star);
                                    obj.put("review_time", review_date_date.getTime());
                                    int year_num = Integer.parseInt(review_date.split("-")[0]);
                                    int month_num = Integer.parseInt(review_date.split("-")[1]);
                                    int d = month.get(month_num - 1);
                                    month.set(month_num - 1, d + 1);
                                    if(option != null) {
                                        int lcs_base_option_price = 0;
                                        int max = 0;
                                        int min = 99999;
                                        int cnt = 0;
                                        String lcs_key = "";
                                        int avg_price_this = 0;
                                        for (Object key : opt_obj.keySet()) {
                                            cnt++;
                                            avg_price_this += (int) opt_obj.get(key);
                                            int lcs_point = lcs.wordMatching((String) key, option);

                                            if (lcs_point > max) {
                                                max = lcs_point;
                                                lcs_base_option_price = (int) opt_obj.get(key);
                                                lcs_key = (String) key;
                                            }
                                        }
                                        avg_price = Integer.parseInt(String.valueOf(Math.round(avg_price_this * 1.0 / cnt * 1.0)));
                                        System.out.println("유사옵션 : " + lcs_key + " vs " + option);
                                        if(lcs_key.equals("")){
                                            lcs_base_option_price = avg_price;
                                        }
                                        if (opt_obj != null) {
                                            if (opt_obj.containsKey(option)) {
                                                int opt_price = (int) opt_obj.get(option);
                                                obj.put("price", opt_price);
                                                option_base_revenue += opt_price;
                                            }
                                            else {
                                                if (lcs_base_option_price == 0) {
                                                    obj.put("price", avg_price);
                                                    option_base_revenue += avg_price;
                                                } else {
                                                    obj.put("price", lcs_base_option_price);
                                                    option_base_revenue += lcs_base_option_price;
                                                }
                                            }
                                        }
                                    } else {
                                        obj.put("price", opt_obj.get("default_price"));
                                        option_base_revenue += (int) opt_obj.get("default_price");
                                    }
                                    if(year_based_revenue.containsKey(year_num)) {
                                        long[] month_arr = (long[])year_based_revenue.get(year_num);
                                        month_arr[month_num-1] = month_arr[month_num-1] + (int) obj.get("price");
                                        year_based_revenue.put(year_num, month_arr);
                                    } else {
                                        long[] month_arr = new long[12];
                                        month_arr[month_num-1] = (int)obj.get("price");
                                        year_based_revenue.put(year_num, month_arr);
                                    }
                                    int this_month_num = Integer.parseInt(review_date.split("-")[1])- 1;
                                    int month_review_count = month.get(this_month_num);
                                    month.set(this_month_num, month_review_count + 1);
                                    int price = (int) obj.get("price");
                                    int this_month_revenue = month_advance.get(this_month_num);
                                    month_advance.set(this_month_num, this_month_revenue + price);
                                    review_array_total2.add(obj);
                                    product_counts++;

                                    if (review_date_date.getTime() >= yesterday.getTime()) {
                                        review_array_yesterday2.add(obj);
                                        option_base_revenue0d += (int) obj.get("price");
                                    }
                                    if (review_date_date.getTime() >= today.getTime()) {
                                        review_array_today2.add(obj);
                                        option_base_revenue1d += (int) obj.get("price");
                                    }

                                    if (review_date_date.getTime() > thisweek.getTime()) {
                                        option_base_revenue7d += (int) obj.get("price");
                                    }

                                    if (review_date_date.getTime() > one_monthago.getTime()) {
                                        review_array_onemonth2.add(obj);
                                        option_base_revenue1m += (int) obj.get("price");
                                    }
                                    if (review_date_date.getTime() > two_monthago.getTime()) {
                                        review_array_twomonth2.add(obj);

                                    }
                                    if (review_date_date.getTime() > three_monthago.getTime()) {
                                        review_array_threemonth2.add(obj);
                                        option_base_revenue3m += (int) obj.get("price");

                                    }
                                    if (review_date_date.getTime() > six_monthago.getTime()) {
                                        review_array_sixmonth2.add(obj);
                                        option_base_revenue6m += (int) obj.get("price");
                                    }

                                    if (review_date_date.getTime() > twelve_monthago.getTime()) {
                                        review_array_twelvemonth2.add(obj);
                                        option_base_revenue12m += (int) obj.get("price");

                                    }

                                    if (review_date_date.getTime() > eleven_monthago.getTime()) {
                                        review_array_elevenmonth2.add(obj);
                                    }
                                    if (review_date_date.getTime() > nine_monthago.getTime()) {
                                        review_array_ninemonth2.add(obj);
                                    }

                                    calendar.setTime(review_date_date);
                                    int rm = calendar.get(Calendar.MONTH);

                                    if (rm == 3 || rm == 4 || rm == 5) {
                                        spring++;
                                    }

                                    if (rm == 6 || rm == 7 || rm == 8) {
                                        summer++;
                                    }

                                    if (rm == 9 || rm == 10 || rm == 11) {
                                        fall++;
                                    }

                                    if (rm == 12 || rm == 1 || rm == 2) {
                                        winter++;
                                    }
                                }


                                if(review_array_total2.size() == total_delta) {
                                    break loop_whie;
                                }


                            } catch (NoSuchMethodError er) {
                                continue;
                            }

                        }
                    } catch (NoSuchMethodError e) {
                        break;
                    }


                    pagenums++;
                    sleep(500);
                    try {
                        Long leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children.length");

                        for (int t = 1; t < leng; t++) {
                            String is_current = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + t + "].getAttribute('aria-current')");
                            if (is_current.equals("true")) {
                                int next_comb = t + 1;

                                String is_next = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].textContent");
                                if (is_next.equals("다음")) {
                                    String aria_hidden = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].getAttribute('aria-hidden')");
                                    if (aria_hidden.equals("true")) {
                                        break loop_whie;
                                    }
                                }

                                ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].click()");
                                break;
                            }
                        }


                    } catch (NoSuchMethodError er) {
                                              review_count = 0;
                        result.put("valid_url", "이 상품은 정보가 없습니다");
                        break;

                    } catch(JavascriptException ex) {
                        result.put("valid_url", "조건에 맞는 리뷰가 없습니다"); break;
                    }
                }
            }




        } catch(NumberFormatException er){
            System.out.println("리뷰없음");
            result.put("valid_url","이 상품은 정보가 없습니다");

        } catch(Exception er){
            er.printStackTrace();

        }

        if(gijonIsBigger < 0) {

            if(total_reviews_cnt != (review_array_total.size() + review_array_total2.size())) {
               Iterator<String> keys = obj_gijon.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    int count = (Integer) obj_gijon.get(key);
                    if (count > 0) {

                        ArrayList forDelete = new ArrayList();
                        forDelete.add(review_array_yesterday);
                        forDelete.add(review_array_today);
                        forDelete.add(review_array_thisweek);
                        forDelete.add(review_array_onemonth);
                        forDelete.add(review_array_twomonth);
                        forDelete.add(review_array_threemonth);
                        forDelete.add(review_array_sixmonth);
                        forDelete.add(review_array_twelvemonth);
                        forDelete.add(review_array_elevenmonth);
                        forDelete.add(review_array_ninemonth);
                        forDelete.add(review_array_total);

                        removeGijonReview(key, forDelete);
                    }
                }
            }

        }

        int total_reviews_count = total_reviews_cnt;
        int checked_reviews_count = review_array_total2.size() + review_array_total.size();
        result.put("review_count_original", total_reviews_count);
        double plus_delta = (total_reviews_count - checked_reviews_count)*1.0 ;
        if(plus_delta > 0) {
            result.put("보정치", plus_delta);
        } else {
            result.put("보정치", 0.0);
        }
        int season = spring+summer+fall+winter;
        double spring_av = (spring*1.0 / season*1.0) * 100;
        double summer_av = (summer*1.0 / season*1.0) * 100;
        double fall_av = (fall*1.0 / season*1.0) * 100;
        double winter_av = (winter*1.0 / season*1.0) * 100;
        result.put("spring", spring_av);
        result.put("summer", summer_av);
        result.put("fall", fall_av);
        result.put("winter", winter_av);
        result.put("month",month);
        result.put("month_3",month_3);
        result.put("month_advance", month_advance);
        result.put("option_base_revenue",option_base_revenue );
        result.put("option_base_revenue3m",option_base_revenue3m  );
        result.put("option_base_revenue6m",option_base_revenue6m );
        result.put("option_base_revenue12m",option_base_revenue12m );
        result.put("option_base_revenue0d",option_base_revenue0d );
        result.put("option_base_revenue1d",option_base_revenue1d );
        result.put("option_base_revenue7d",option_base_revenue7d );
        result.put("option_base_revenue1m",option_base_revenue1m );

        review_array_yesterday2.addAll(review_array_yesterday);
        review_array_today2.addAll(review_array_today);
        review_array_thisweek2.addAll(review_array_thisweek);
        review_array_onemonth2.addAll(review_array_onemonth);
        review_array_twomonth2.addAll(review_array_twomonth);
        review_array_threemonth2.addAll(review_array_threemonth);
        review_array_sixmonth2.addAll(review_array_sixmonth);
        review_array_twelvemonth2.addAll(review_array_twelvemonth);
        review_array_elevenmonth2.addAll(review_array_elevenmonth);
        review_array_ninemonth2.addAll(review_array_ninemonth);
        review_array_total2.addAll(review_array_total);

        review_array_yesterday2 = reviewtimeSorter(review_array_yesterday2);
        review_array_today2 = reviewtimeSorter(review_array_today2);
        review_array_thisweek2= reviewtimeSorter(review_array_thisweek2);
        review_array_onemonth2= reviewtimeSorter(review_array_onemonth2);
        review_array_twomonth2 = reviewtimeSorter(review_array_twomonth2);
        review_array_threemonth2=reviewtimeSorter(review_array_threemonth2);
        review_array_sixmonth2=reviewtimeSorter(review_array_sixmonth2);
        review_array_twelvemonth2=reviewtimeSorter(review_array_twelvemonth2);
        review_array_elevenmonth2=reviewtimeSorter(review_array_elevenmonth2);
        review_array_ninemonth2=reviewtimeSorter(review_array_ninemonth2);
        review_array_total2=reviewtimeSorter(review_array_total2);

        result.put("valid_url", true);
        result.put("review_array_yesterday", review_array_yesterday2);
        result.put("review_array_today", review_array_today2);
        result.put("review_array_thisweek", review_array_thisweek2);
        result.put("review_array_onemonth", review_array_onemonth2);
        result.put("review_array_twomonth", review_array_twomonth2);
        result.put("review_array_threemonth", review_array_threemonth2);
        result.put("review_array_sixmonth", review_array_sixmonth2);
        result.put("review_array_twelvemonth", review_array_twelvemonth2);
        result.put("review_array_elevenmonth", review_array_elevenmonth2);
        result.put("review_array_ninemonth", review_array_ninemonth2);
        result.put("review_array_total", review_array_total2);
        result.put("year_based_revenue",year_based_revenue);
        result.put("avg_price",avg_price);

        System.out.println("상품명:" + product_name);
        if(product_name == null) {
            result.put("valid_url","리뷰에러가 발생하였습니다");
        }
        else if(product_name.length() == 0) {
            result.put("valid_url","리뷰에러가 발생하였습니다");
        }

        return result;
    }

    /**
     * 리뷰 시간으로 소팅
     *
     * @param review_array 리뷰객체어레이
     * @return 리뷰객체어레이
     */
    public ArrayList reviewtimeSorter(ArrayList review_array){


        Collections.sort(review_array, new Comparator<org.json.JSONObject>() {
            @Override
            public int compare(org.json.JSONObject o1, org.json.JSONObject o2) {
                Long v1=-1L;
                Long v3=-1L;
                try {
                    v1 = (Long) o1.get("review_time");
                    v3 = (Long) o2.get("review_time");

                }catch(Exception ex){
                    ex.printStackTrace();
                }

                long del = v1-v3;
                int res = 1;
                if(del > 0) {
                    res = -1;
                }
                if(del < 0) {
                    res = 1;
                }
                if(del == 0){
                    res = 0;
                }

                return res;
            }
        });

        return review_array;

    }
    /**
     * 기존 리뷰 삭제
     *
     * @param key 검색할리뷰키
     * @param arrs 리뷰객체어레이
     */
    public void removeGijonReview(String key, ArrayList<ArrayList> arrs){
        for(int i=0; i < arrs.size(); i++){
            for(int j=0; j < arrs.get(i).size(); j++){

                try {
                    org.json.JSONObject obj = (org.json.JSONObject) arrs.get(i).get(j);
                    if(obj.has("option")) {
                        String check_key = (String) obj.get("user_id") + (String) obj.get("review_date") + (String) obj.get("option") + (String) obj.get("review_text") + (String) obj.get("star");
                        if (check_key.equals(key)) {
                            arrs.get(i).remove(j);
                        }
                    }
                    else  {
                        String check_key = (String) obj.get("user_id") + (String) obj.get("review_date") + null + (String) obj.get("review_text") + (String) obj.get("star");
                        if (check_key.equals(key)) {
                            arrs.get(i).remove(j);
                        }
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * 리뷰 옵션 비율 계산
     *
     * @param url URL
     * @param month 월
     */
    public Boolean printReviewOptionRatio(String url, Integer month){

        ArrayList<JSONObject> arr = getOnlyReviewInfo(url,month);
        HashMap<String,Integer> option_map = new HashMap<>();

        for(int i=0; i < arr.size(); i++){
            try {
                String option_key = (String) arr.get(i).get("option");
                if(option_map.containsKey(option_key)){
                    option_map.put(option_key, option_map.get(option_key)+1);
                }
                else{
                    option_map.put(option_key,1);
                }

            }catch(Exception er){
                String option_key = "무옵션";
                if(option_map.containsKey(option_key)){
                    option_map.put(option_key, option_map.get(option_key)+1);
                }
                else{
                    option_map.put(option_key,1);
                }
            }
        }
        option_map = Utils.sort(option_map);
        Iterator<String> keys = option_map.keySet().iterator();

        System.out.println("최근 " + month + "개월 이내 판매량 비율 분석");
        System.out.println("-----------------");
        System.out.println("총 리뷰수 : " + arr.size());
        while( keys.hasNext() ){
            String key = keys.next();
            double d = option_map.get(key) * 1.0/arr.size() * 1.0;
            d = Math.round(d*100)/100.0;
            System.out.println("[" + key + "] : " + option_map.get(key) + "개 : " + String.valueOf(d));
        }
        System.out.println("-----------------");
        return false;


    }

    /**
     * 리뷰 데이터 업데이트
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할개수
     * @param product_nos 상품넘버
     */
    public void updateNaverReview(String part_number, String total_numbers,String product_nos){

        ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> naver_product_list = null;
        try {

            if(product_nos == null) {
                Connection conn = mysql.initConnect(DB_IP);
                naver_product_list = mysql.selectNaverLight(conn, "select product_url,product_no,product_img,title,cat_big,cat_middle,cat_small,cat_product,register_date,register_timestamp from naver_light where (deletes=0 and data_review is null) or insert_timestamp <= DATE_ADD(now(), INTERVAL -168 HOUR)");
                conn.close();
            }
            else {
                Connection conn = mysql.initConnect(DB_IP);
                naver_product_list = mysql.selectNaverLight(conn, "select product_url,product_no,product_img,title,cat_big,cat_middle,cat_small,cat_product,register_date,register_timestamp from naver_light where product_no='" + product_nos + "'");
                conn.close();
            }
        }catch(SQLException sc){
            sc.printStackTrace();
        }

        driver = DriverControl.getGeneralDriver();

        int slice_counts = Math.round(naver_product_list.size() / Integer.parseInt(total_numbers));
        int end_point = slice_counts * Integer.parseInt(part_number);
        int starting_point = end_point- slice_counts;

        if(naver_product_list.size()-end_point < slice_counts) {
            end_point = naver_product_list.size();
        }

        for(int k=starting_point; k < end_point; k++) {
            try {

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("[" + sdf.format(cal.getTime()) + "] " + k + "/" + end_point);
                NaverProductDetailClass<String,Integer,Double,Long,Timestamp> naver_list = naver_product_list.get(k);

                driver.get(naver_list.product_url);
                Boolean error = false;

                try {
                    String title_error = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('title_error')[0].textContent");
                    if(title_error.split("불가합니다").length > 1){
                        Thread.sleep(60 * 1000);
                        driver.navigate().refresh();
                    }
                    if(title_error.split("존재하지").length > 1){
                        //driver.navigate().refresh();
                        error = true;
                    }
                }catch(NoSuchMethodError e2){

                }
                try {
                    String url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                    if(url.split("nid.naver.com").length > 1){
                        error = true;
                    }
                }catch(NoSuchMethodError E1){

                }
                try {
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                } catch (NoAlertPresentException e) {
                    //e.printStackTrace();
                } catch(NoSuchMethodError er){

                } catch(Exception erer){

                }



                if(error == true){
                    continue;
                }

                ArrayList<JSONObject> review_array = new ArrayList<>();
                int review_count = 0;
                try {
                    Long rleng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_count').length");
                    if (rleng == 2) {
                        String review_count_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_count')[1].innerText");
                        review_count_str = review_count_str.replaceAll(",", "");
                        review_count = Integer.parseInt(review_count_str);
                    }
                    if (rleng == 1) {
                        String review_count_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_count')[0].innerText");
                        review_count_str = review_count_str.replaceAll(",", "");
                        review_count = Integer.parseInt(review_count_str);
                    }

                    try {
                        ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_REVIEW_CREATE_DATE_DESC')[0].click()");
                        sleep(3000);
                    } catch (NoSuchMethodError er) {
                        Connection conn = mysql.initConnect(DB_IP);
                        mysql.updateNaver(conn, "update naver set deletes=? where product_no=?", 1, naver_list.product_no);
                        conn.close();
                        continue;
                    }
                    String title = naver_list.title;
                    String cat1 = naver_list.cat_big;
                    String cat2 = naver_list.cat_big + ">" + naver_list.cat_middle;
                    String cat3 = naver_list.cat_big + ">" + naver_list.cat_middle + ">" + naver_list.cat_small;
                    String cat4 = null;
                    if (naver_list.cat_product != null && !naver_list.cat_product.equals("")) {
                        cat4 = naver_list.cat_big + ">" + naver_list.cat_middle + ">" + naver_list.cat_small + ">" + naver_list.cat_product;
                    } else {
                        cat4 = cat3;
                    }

                    String product_no = naver_list.product_no;
                    String register_date = naver_list.register_date;
                    Long register_timestamp = naver_list.register_timestamp;
                    String product_image = naver_list.product_img;
                    int product_counts = 0;
                    int bread = 0;

                    while (true) {
                        String review_id = null;
                        String review_user_id = null;
                        Long review_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children.length");
                        for (int j = 0; j < review_leng; j++) {
                            JSONObject obj = new JSONObject();
                            String star = "";
                            try {
                                star = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[0].children[1].children[0].children[2].innerText");
                                String userid = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[0].children[1].children[1].children[0].children[0].innerText");
                                String review_date = "";
                                String review_date_yymmdd = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[0].children[1].children[1].children[0].children[1].innerText");
                                String[] review_dates = review_date_yymmdd.split("\\.");
                                review_date = ("20" + review_dates[0]) + "-" + (review_dates[1]) + "-" + (review_dates[2]);
                                String option = null;
                                try {
                                    option = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[0].children[1].children[1].children[0].getElementsByTagName('p')[0].innerText");
                                } catch (NoSuchMethodError er) {

                                }
                                String review_text = null;
                                review_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[0].children[1].children[2].innerText");
                                String review_image = null;
                                try {
                                    review_image = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].children[0].children[1].getElementsByTagName('img')[0].src");
                                } catch (NoSuchMethodError er) {

                                }
                                try {
                                    String review_ids = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('detail_list_review')[0].firstElementChild.children[" + j + "].id");
                                    review_id = review_ids.substring(2,review_ids.length());
                                    review_user_id = review_id.substring(0,5);
                                } catch (NoSuchMethodError er) {

                                }
                                obj.put("star", star);
                                obj.put("user_id", userid);
                                obj.put("review_date", review_date);
                                obj.put("option", option);
                                obj.put("review_text", review_text);
                                obj.put("review_image", review_image);
                                obj.put("review_id", review_id);
                                obj.put("review_user_id", review_user_id);
                                review_array.add(obj);
                                product_counts++;
                                Connection conn = mysql.initConnect(DB_IP);
                                ArrayList<HashMap<String,String>>hashtags = mysql.selectKeywordHashtag(conn,"select * from keyword_hashtag where product_no=" + product_no + "");
                                conn.close();

                                String keyword = hashtags.get(0).get("keyword");
                                HashMap<String,String> usermap = new HashMap();
                                usermap.put("nickname",userid);
                                usermap.put("review_date", review_date);
                                usermap.put("review_image",review_image);
                                usermap.put("review_text",review_text);
                                usermap.put("review_option",option);
                                usermap.put("review_star",star);
                                usermap.put("product_id",product_no);
                                usermap.put("product_title",title);
                                usermap.put("product_image", product_image);
                                usermap.put("product_category", cat4);
                                usermap.put("cat_big", naver_list.cat_big);
                                usermap.put("cat_middle", naver_list.cat_middle);
                                usermap.put("cat_small", naver_list.cat_small);
                                usermap.put("cat_product", naver_list.cat_product);
                                usermap.put("product_keyword", keyword);
                                usermap.put("product_url", naver_list.product_url);

                                mysql.insertUserInfo(conn,"insert into naver_users(review_id,user_id,nickname,review_date,review_image,review_text,review_option,review_star,product_id,product_title,product_image,product_category,cat_big,cat_middle,cat_small,cat_product,product_keyword,product_price,product_url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", usermap);

                            } catch (NoSuchMethodError er) {
                                continue;
                            }

                        }


                        bread++;
                        sleep(500);
                        try {
                            Long breadcrumb = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children.length");
                            String last2_bread_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children[" + (breadcrumb - 2) + "].innerText");
                            String last1_bread_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children[" + (breadcrumb - 1) + "].innerText");
                            if (last2_bread_text.split("다").length > 1 || last1_bread_text.split("다").length > 1) {
                                String next_bread_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children[" + bread + "].innerText");
                                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_review_list_page')[0].children[" + bread + "].click()");
                                if (next_bread_text.split("다").length > 1) {
                                    //다음 버튼을 누르면 반드시 다음페이지에는 맨앞,이전이 있고 디폴트로 첫번째 페이지로가 가기때문에 다음순번은 3이되어야한다. 앞에서 한번++해주기 떄문에 2 셋팅
                                    bread = 2;
                                }
                            }
                            else {
                                Long breadcrumb_now = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children.length");
                                String current_bread_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children[" + (bread - 1) + "].innerText");
                                String last_bread_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_review_list_page')[0].children[" + (breadcrumb_now - 1) + "].innerText");
                                if (current_bread_text.equals(last_bread_text)) {
                                    break;
                                }
                                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_review_list_page')[0].children[" + bread + "].click()");

                            }
                        } catch (NoSuchMethodError er) {
                            review_count = 0;
                            break;

                        }
                    }
                    String data_review = review_array.toString();
                    Connection conn = mysql.initConnect(DB_IP);
                    mysql.updateNaverReview(conn, "update naver_light set data_review=?,count_review=? where product_no=?", data_review, review_count,product_no);
                    conn.close();

                } catch(NumberFormatException er){
                    System.out.println("리뷰없음");
                } catch(NoSuchMethodError ee){
                    System.out.println("리뷰없음");
                }

            }catch(Exception er){
                //er.printStackTrace();
                System.out.println("예상치 못한 에러");
                System.out.println(er.getMessage());
                continue;
            }


        }

        DriverControl.quitDriver(driver);





    }

    /**
     * 리뷰 및 유저정보 계산
     *
     * @param part_number 시작지점
     * @param total_numbers 전체분할개수
     */
    public void getReviewAndUsersInfo(String part_number, String total_numbers){
        ArrayList<HashMap<String, String>> nlights = null;
        try {
            Connection conn = mysql.initConnect(DB_IP);
            nlights = mysql.selectKeywordHashtag(conn, "select * from keyword_hashtag");
            conn.close();
        }catch(SQLException SC){
            SC.printStackTrace();
        }

        int slice_counts = Math.round(nlights.size() / Integer.parseInt(total_numbers));
        int end_point = slice_counts * Integer.parseInt(part_number);
        int starting_point = end_point- slice_counts;

        if(nlights.size()-end_point < slice_counts) {
            end_point = nlights.size();
        }

        for(int i=starting_point; i < end_point; i++){
            getOnlyReviewInfo(nlights.get(i).get("product_url"), -1);
        }
    }

    /**
     * 리뷰정보만 계산
     *
     * @param product_url 상품URL
     * @param monthago 월
     */
    public ArrayList getOnlyReviewInfo(String product_url, int monthago){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date da = new Date();
        Calendar cal = Calendar.getInstance();
        org.json.simple.JSONObject option_obj = new org.json.simple.JSONObject();
        cal.setTime(da);
        cal.add(Calendar.MONTH, -1 * monthago);
        Date x_monthago = cal.getTime();
        ArrayList<JSONObject> review_array = new ArrayList<>();
        String product_name = "";
        String product_no = null;
        long option_base_revenue = 0;
        int product_counts = 0;
        int bread = 0;
        int price = 0;


        ChromeDriver driver =DriverControl.getGeneralDriver();


        try {

            driver.get(product_url);
            Boolean error = false;
            Long notSell = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"운영이 중지되었습니다\").length");
            Long notSell2 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매금지 된 상품입니다.\").length");
            Long notSell3 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매중지 된 상품입니다.\").length");
            Long notSell4 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"상품이 존재하지 않습니다.\").length");
            Long notSell5 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 판매할 수 없습니다\").length");
            Long notSell6 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"연령 확인이 필요한 서비스\").length");
            Long notSell7 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"현재 서비스 접속\").length");


            if(notSell > 1 || notSell2 >1 || notSell3 > 1 || notSell4 >1 || notSell5 > 1 || notSell6 > 1 || notSell7 > 1) {
                error = true;
                if (product_url.split("/products/").length == 2) {
                    String product_no2 = product_url.split("products/")[1].split("\\?")[0];
                    Connection con = mysql.initConnect(DB_IP);
                    mysql.updateDatalabPlusNotSell(con, "update datalab_plus set notsell=? where product_no=?", 1, product_no2);
                    con.close();
                }
            }


            try {
                sleep(3000);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }catch(NoSuchMethodError er){
                driver.get(product_url);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }
            try {
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_1K9yWX-Lpq')[1].children[0].click()");
                sleep(5000);
            } catch (NoSuchMethodError er) {
                error =true;
            } catch(Exception ex) {
            }

            if(error){
                return review_array;
            }
            try {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[1].textContent");
                price = Format.parse(price_str).intValue();
            } catch(NoSuchMethodError nopr) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            } catch(Exception ee) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            }
            try {
                ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
                ArrayList option_groups = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.combinationOptions");
                for(int a =0; a < option_array.size(); a++) {
                    Map opt_obj = (Map) option_array.get(a);
                    String opt_name = "";
                    for(int b=1; b < 100; b++) {
                        if(opt_obj.containsKey("optionName" + b)) {

                            String group_name =  (String) ((Map)option_groups.get(b-1)).get("groupName");
                            if(group_name == null) {
                                group_name = "옵션" + b;
                            }
                            opt_name += (group_name+": " + opt_obj.get("optionName" + b));
                            opt_name += " / ";
                        }
                        else {
                            break;
                        }
                    }
                    if(!opt_name.equals("")) {
                        opt_name = opt_name.substring(0, opt_name.length()-3);
                    }

                    int plus_price = Integer.parseInt(String.valueOf((Long)opt_obj.get("price")));
                    int opt_price = price + plus_price;
                    option_obj.put(opt_name, opt_price);

                }
                option_obj.put("default_price", price);
            }catch(Exception ex) {
                option_obj = null;
            }
            if(option_obj == null) {
                try {

                    option_obj = new org.json.simple.JSONObject();
                    ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
                    ArrayList option_standards = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionStandards");
                    org.json.simple.JSONObject option_standards_obj = new org.json.simple.JSONObject();
                    for(int k=0; k < option_standards.size(); k++) {
                        Map opt_obj = (Map) option_standards.get(k);

                        String group_name =   (String) opt_obj.get("optionGroupName");
                        if(group_name == null) {
                            group_name = "옵션" + (k+1);
                        }
                        option_standards_obj.put((String)opt_obj.get("optionName"), group_name);
                    }

                    for(int a =0; a < option_array.size(); a++) {
                        Map opt_obj = (Map) option_array.get(a);
                        String opt_name = "";
                        for(int b=1; b < 100; b++) {
                            if(opt_obj.containsKey("optionName" + b)) {

                                String group_name = (String) option_standards_obj.get(opt_obj.get("optionName" + b));
                                if(group_name == null){
                                    group_name ="옵션" + (b+1);
                                }
                                opt_name += (group_name+": " + opt_obj.get("optionName" + b));
                                opt_name += " / ";
                            }
                            else {
                                break;
                            }
                        }
                        if(!opt_name.equals("")) {
                            opt_name = opt_name.substring(0, opt_name.length()-3);
                        }

                        int plus_price = Integer.parseInt(String.valueOf((Long)opt_obj.get("price")));
                        int opt_price = price + plus_price;
                        option_obj.put(opt_name, opt_price);
                    }
                    option_obj.put("default_price", price);
                }catch(Exception ex) {
                    option_obj = null;
                }
            }




            loop_whie:
            while (true) {
                try {
                    Thread.sleep(1000);
                    Long review_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56').length");

                    for (int j = 0; j < review_leng; j++) {

                        org.json.JSONObject obj = new org.json.JSONObject();


                        try {
                            String review_date = "";
                            String review_date_yymmdd = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[1].children[1].textContent");
                            String[] review_dates = review_date_yymmdd.split("\\.");
                            review_date = ("20" + review_dates[0]) + "-" + (review_dates[1]) + "-" + (review_dates[2]);
                            Date review_date_date = dateFormat.parse(review_date);
                            if (review_date_date.getTime() >= x_monthago.getTime()) {
                                String star = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[0].children[1].textContent");
                                String userid = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[1].children[0].textContent");
                                String option = null;
                                try {
                                    option = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[2].textContent");
                                } catch (NoSuchMethodError er) {

                                } catch (Exception ex) {

                                }
                                String review_text = null;
                                review_text = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[1].textContent");
                                System.out.println(review_text);
                                String review_image = null;
                                try {
                                    review_image = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2389dRohZq')[" + j + "].children[0].children[0].children[1].children[0].children[0].children[1].children[0].children[0].src");
                                } catch (NoSuchMethodError er) {

                                } catch (Exception ex) {

                                }

                                obj.put("user_id", userid);
                                obj.put("review_date", review_date);
                                obj.put("option", option);
                                obj.put("review_text", review_text);
                                obj.put("review_image", review_image);
                                obj.put("star", star);
                                obj.put("review_time", review_date_date.getTime());
                                int lcs_base_option_price = 0 ;
                                int max = 0;
                                int cnt = 0;
                                String lcs_key = "";
                                int avg_price = 0;
                                for (Object key : option_obj.keySet())
                                {
                                    cnt++;
                                    avg_price += (int) option_obj.get(key);
                                    int lcs_point = lcs.wordMatching((String)key, option);
                                    if(lcs_point > max) {
                                        max = lcs_point;
                                        lcs_base_option_price = (int) option_obj.get(key);
                                        lcs_key = (String)key;
                                    }
                                }
                                avg_price = Integer.parseInt(String.valueOf(Math.round(avg_price*1.0 / cnt*1.0)));
                                if (option_obj != null) {
                                    if (option_obj.containsKey(option)) {
                                        int opt_price = (int) option_obj.get(option);
                                        obj.put("price", opt_price);
                                        option_base_revenue += opt_price;
                                    }
                                    else {
                                        if(lcs_base_option_price == 0) {
                                            obj.put("price", avg_price);
                                            option_base_revenue += avg_price;
                                        }
                                        else {
                                            obj.put("price", lcs_base_option_price);
                                            option_base_revenue += lcs_base_option_price;
                                        }
                                    }
                                }
                            }

                            else {
                                break loop_whie;
                            }

                        } catch (NoSuchMethodError er) {
                            continue;
                        }

                    }
                } catch (NoSuchMethodError e) {
                    break;
                }


                bread++;
                sleep(500);
                try {
                    Long leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children.length");
                    for (int t = 1; t < leng; t++) {
                        String is_current = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + t + "].getAttribute('aria-current')");
                        if (is_current.equals("true")) {
                            int next_comb = t + 1;

                            String is_next = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].textContent");
                            if (is_next.equals("다음")) {
                                String aria_hidden = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].getAttribute('aria-hidden')");
                                if (aria_hidden.equals("true")) {
                                    break loop_whie;
                                }
                            }

                            ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].click()");
                            break;
                        }
                    }


                } catch (NoSuchMethodError er) {
                    break;

                }
            }




        } catch(NumberFormatException er){
            System.out.println("리뷰없음");
        } catch(Exception er){
            System.out.println(er.getMessage());
        }

        DriverControl.quitDriver(driver);
        System.out.println("상품명:" + product_name);
        return review_array;
    }

    /**
     * 리뷰정보만 바탕으로 매출 추정
     *
     * @param product_url 상품URL
     * @param monthago 월
     */
    public long getOnlyReviewInfoBasedRevenue(String product_url, int monthago){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date da = new Date();
        Calendar cal = Calendar.getInstance();
        org.json.simple.JSONObject option_obj = new org.json.simple.JSONObject();
        cal.setTime(da);
        cal.add(Calendar.MONTH, -1 * monthago);
        Date x_monthago = cal.getTime();

        String product_name = "";
        String product_no = null;
        long option_base_revenue = 0;
        int product_counts = 0;
        int bread = 0;
        int price = 0;


        ChromeDriver driver =DriverControl.getGeneralDriver();


        try {

            driver.get(product_url);
            Boolean error = false;
            Long notSell = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"운영이 중지되었습니다\").length");
            Long notSell2 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매금지 된 상품입니다.\").length");
            Long notSell3 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매중지 된 상품입니다.\").length");
            Long notSell4 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"상품이 존재하지 않습니다.\").length");
            Long notSell5 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 판매할 수 없습니다\").length");
            Long notSell6 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"연령 확인이 필요한 서비스\").length");
            Long notSell7 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"현재 서비스 접속\").length");


            if(notSell > 1 || notSell2 >1 || notSell3 > 1 || notSell4 >1 || notSell5 > 1 || notSell6 > 1 || notSell7 > 1) {
                error = true;
                if (product_url.split("/products/").length == 2) {
                    String product_no2 = product_url.split("products/")[1].split("\\?")[0];
                    Connection con = mysql.initConnect(DB_IP);
                    mysql.updateDatalabPlusNotSell(con, "update datalab_plus set notsell=? where product_no=?", 1, product_no2);
                    con.close();
                }
            }


            try {
                sleep(3000);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }catch(NoSuchMethodError er){
                driver.get(product_url);
                ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1k5R-niA93')[1].children[0].click()");
            }
            try {
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('_1K9yWX-Lpq')[1].children[0].click()");
                sleep(5000);
            } catch (NoSuchMethodError er) {
                error =true;
            } catch(Exception ex) {
            }

            if(error){
                return 0;
            }
            try {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[1].textContent");
                price = Format.parse(price_str).intValue();

            } catch(NoSuchMethodError nopr) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            } catch(Exception ee) {
                String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1LY7DqCnwR')[0].textContent");
                price = Format.parse(price_str).intValue();
            }
            try {
                ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
                ArrayList option_groups = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.combinationOptions");
                for(int a =0; a < option_array.size(); a++) {
                    Map opt_obj = (Map) option_array.get(a);
                    String opt_name = "";
                    for(int b=1; b < 100; b++) {
                        if(opt_obj.containsKey("optionName" + b)) {

                            String group_name =  (String) ((Map)option_groups.get(b-1)).get("groupName");
                            if(group_name == null) {
                                group_name = "옵션" + b;
                            }
                            opt_name += (group_name+": " + opt_obj.get("optionName" + b));
                            opt_name += " / ";
                        }
                        else {
                            break;
                        }
                    }
                    if(!opt_name.equals("")) {
                        opt_name = opt_name.substring(0, opt_name.length()-3);
                    }

                    int plus_price = Integer.parseInt(String.valueOf((Long)opt_obj.get("price")));
                    int opt_price = price + plus_price;
                    option_obj.put(opt_name, opt_price);

                }
                option_obj.put("default_price", price);
            }catch(Exception ex) {
                option_obj = null;
            }
            if(option_obj == null) {
                try {
                    option_obj = new org.json.simple.JSONObject();
                    ArrayList option_array = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionCombinations");
                    ArrayList option_standards = (ArrayList) ((JavascriptExecutor) driver).executeScript("return window.__PRELOADED_STATE__.product.A.optionStandards");
                    org.json.simple.JSONObject option_standards_obj = new org.json.simple.JSONObject();
                    for(int k=0; k < option_standards.size(); k++) {
                        Map opt_obj = (Map) option_standards.get(k);

                        String group_name =   (String) opt_obj.get("optionGroupName");
                        if(group_name == null) {
                            group_name = "옵션" + (k+1);
                        }
                        option_standards_obj.put((String)opt_obj.get("optionName"), group_name);
                    }



                    for(int a =0; a < option_array.size(); a++) {
                        Map opt_obj = (Map) option_array.get(a);
                        String opt_name = "";
                        for(int b=1; b < 100; b++) {
                            if(opt_obj.containsKey("optionName" + b)) {

                                String group_name = (String) option_standards_obj.get(opt_obj.get("optionName" + b));
                                if(group_name == null){
                                    group_name ="옵션" + (b+1);
                                }
                                opt_name += (group_name+": " + opt_obj.get("optionName" + b));
                                opt_name += " / ";
                            }
                            else {
                                break;
                            }
                        }
                        if(!opt_name.equals("")) {
                            opt_name = opt_name.substring(0, opt_name.length()-3);
                        }

                        int plus_price = Integer.parseInt(String.valueOf((Long)opt_obj.get("price")));
                        int opt_price = price + plus_price;
                        option_obj.put(opt_name, opt_price);
                    }
                    option_obj.put("default_price", price);
                }catch(Exception ex) {
                    option_obj = null;
                }
            }


            loop_whie:
            while (true) {
                try {
                    Thread.sleep(1000);
                    Long review_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56').length");

                    for (int j = 0; j < review_leng; j++) {


                        JSONObject obj =new JSONObject();


                        try {
                            String review_date = "";
                            String review_date_yymmdd = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[1].children[1].textContent");
                            String[] review_dates = review_date_yymmdd.split("\\.");
                            review_date = ("20" + review_dates[0]) + "-" + (review_dates[1]) + "-" + (review_dates[2]);
                            Date review_date_date = dateFormat.parse(review_date);
                            if (review_date_date.getTime() >= x_monthago.getTime()) {
                                String option = null;
                                try {
                                    option = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1YShY6EQ56')[" + j + "].children[0].children[1].children[2].textContent");
                                } catch (NoSuchMethodError er) {

                                } catch (Exception ex) {

                                }

                                obj.put("review_date", review_date);
                                obj.put("option", option);
                                obj.put("review_time", review_date_date.getTime());

                                int lcs_base_option_price = 0 ;
                                int max = 0;
                                int cnt = 0;
                                String lcs_key = "";
                                int avg_price = 0;
                                for (Object key : option_obj.keySet())
                                {
                                    cnt++;
                                    avg_price += (int) option_obj.get(key);
                                    int lcs_point = lcs.wordMatching((String)key, option);
                                    if(lcs_point > max) {
                                        max = lcs_point;
                                        lcs_base_option_price = (int) option_obj.get(key);
                                        lcs_key = (String)key;
                                    }
                                }
                                avg_price = Integer.parseInt(String.valueOf(Math.round(avg_price*1.0 / cnt*1.0)));
                                if (option_obj != null) {
                                    if (option_obj.containsKey(option)) {
                                        int opt_price = (int) option_obj.get(option);
                                        obj.put("price", opt_price);
                                        option_base_revenue += opt_price;
                                    }
                                    else {
                                        if(lcs_base_option_price == 0) {
                                            obj.put("price", avg_price);
                                            option_base_revenue += avg_price;
                                        }
                                        else {
                                            obj.put("price", lcs_base_option_price);
                                            option_base_revenue += lcs_base_option_price;
                                        }
                                    }
                                }
                            }

                            else {
                                break loop_whie;
                            }

                        } catch (NoSuchMethodError er) {
                            continue;
                        }

                    }
                } catch (NoSuchMethodError e) {
                    break;
                }


                bread++;
                sleep(500);
                try {
                    Long leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children.length");

                    for (int t = 1; t < leng; t++) {
                        String is_current = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + t + "].getAttribute('aria-current')");
                        if (is_current.equals("true")) {
                            int next_comb = t + 1;

                            String is_next = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].textContent");
                            if (is_next.equals("다음")) {
                                String aria_hidden = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].getAttribute('aria-hidden')");
                                if (aria_hidden.equals("true")) {
                                    break loop_whie;
                                }
                            }

                            ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3YOQJDR2-0')[0].children[0].children[" + (next_comb) + "].click()");
                            break;
                        }
                    }


                } catch (NoSuchMethodError er) {
                    break;

                }
            }




        } catch(NumberFormatException er){
            System.out.println("리뷰없음");
        } catch(Exception er){
            System.out.println(er.getMessage());
        }

        DriverControl.quitDriver(driver);
        System.out.println("상품명:" + product_name);
        return option_base_revenue;
    }






}
