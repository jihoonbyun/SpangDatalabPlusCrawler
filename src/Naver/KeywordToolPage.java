package Naver;

import Util.DriverControl;
import Util.LoginTool;
import Connection.MySQLConnector;
import Util.Conf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

public class KeywordToolPage extends KeywordToolPageClass<String,Integer,JSONObject> {

    MySQLConnector mysql = new MySQLConnector();
    LoginTool lt = new LoginTool();
    ChromeDriver driver;
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    int KEYWORD_START_NUMBER = 0;
    int KEYWORD_NUMBERS = 10;


    /**
     * 데이터랩 카테고리 클릭수 성장률 모멘텀 계산후 CSV 저장
     *
     */
    public void getDatalabMomentumCSV() {
        try {

            Connection conn = mysql.initConnect(Conf.DATALAB_IP);
            ArrayList<HashMap<String, String>> datalab_list = mysql.selectDatalab(conn, "(select * from datalab_insight where big_cat='생활/건강' and female > 70 and age30=100  and click_300_avg is not null)");
            conn.close();
            int error_count =0;
            String csvFileName = Conf.CSV_FILE_NAME;
            String csvFileNameError = Conf.CSV_FILE_NAME_ERROR;
            BufferedWriter writer = null;
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(csvFileNameError)), "MS949"));

            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(csvFileName)), "MS949"));
                writer.write("Category,24m,12m,6m,3m,1m,positive,ss,fw,clicks,price,clicks_md,price_md,click_1st,price_1st,click_3rd,price_3rd");
                writer.write("\r\n");
            }catch(IOException er){
                er.printStackTrace();
            }

            for(int i=0; i < datalab_list.size(); i++){
                HashMap<String,String> data = datalab_list.get(i);
                JSONObject obj = null;
                JSONArray arr = null;
                JSONArray arr2 = null;
                JSONParser parser = new JSONParser();

                try {
                    obj = (JSONObject) parser.parse(data.get("data_total"));
                    arr = (JSONArray)obj.get("trend");
                    arr2 = (JSONArray)obj.get("keyword");
                }catch(java.lang.ClassCastException ER){
                    arr = (JSONArray) parser.parse(data.get("data_total"));
                }
                if(arr2 == null){
                    try {
                        arr2 = (JSONArray) parser.parse(data.get("keyword_total"));
                    }catch(NullPointerException er){
                        er.printStackTrace();
                    }
                }
                if(arr.size() == 0 && arr2 == null) {
                    error_count++;
                    writer2.write(data.get("sk_id"));
                    writer2.write("\r\n");
                }
                else {

                    String ss_avg = "";
                    String fw_avg = "";
                    int ss = 0;
                    int fw =0;
                    int ss_count = 0;
                    int fw_count = 0;
                    double ss_avg_100 = 0.0;
                    double fw_avg_100 = 0.0;
                    for(int k=0; k <arr.size();k++){
                        JSONObject trend_obj = (JSONObject) arr.get(k);
                        String date = (String) trend_obj.get("date");
                        String click = (String) trend_obj.get("click");
                        try {
                            String[] dates = date.split("-");
                            int month = Integer.parseInt(dates[1]);
                            if(month == 3 || month == 4 || month == 5 || month==6 || month==7 || month ==8){
                                ss += Integer.parseInt(click);
                                ss_count++;
                            }
                            else {
                                fw += Integer.parseInt(click);
                                fw_count++;
                            }
                        }catch (NullPointerException er){
                            er.printStackTrace();
                        }
                    }

                    try {
                        ss_avg_100 = ss / ss_count;
                    }catch(ArithmeticException er){
                        er.printStackTrace();
                    }


                    try {
                        fw_avg_100 = fw / fw_count;
                    }catch(ArithmeticException er){
                        er.printStackTrace();
                    }

                     ss_avg = String.valueOf(ss_avg_100 / (ss_avg_100 + fw_avg_100));
                     fw_avg = String.valueOf(fw_avg_100 / (ss_avg_100 + fw_avg_100));
                    int predict_clicks_avg = 0;
                    int predict_price_avg = 0;
                    JSONObject trend_obj = (JSONObject) arr.get(arr.size() - 1);
                    String date_now = (String) trend_obj.get("date");
                    String click_now = (String) trend_obj.get("click");

                    try {
                        trend_obj_1m = (JSONObject) arr.get(arr.size() - 2);
                        date_1m = (String) trend_obj_1m.get("date");
                        click_1m = (String) trend_obj_1m.get("click");
                        delta_1m = Integer.parseInt(click_now)- Integer.parseInt(click_1m);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                    try {
                        trend_obj_3m = (JSONObject) arr.get(arr.size() - 4);
                        date_3m = (String) trend_obj_3m.get("date");
                        click_3m = (String) trend_obj_3m.get("click");
                        delta_3m = Integer.parseInt(click_now)- Integer.parseInt(click_3m);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                    try {
                        trend_obj_6m = (JSONObject) arr.get(arr.size() - 7);
                        date_6m = (String) trend_obj_6m.get("date");
                        click_6m = (String) trend_obj_6m.get("click");
                        delta_6m = Integer.parseInt(click_now)-Integer.parseInt(click_6m);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                    try {
                        trend_obj_12m = (JSONObject) arr.get(arr.size() - 13);
                        date_12m = (String) trend_obj_12m.get("date");
                        click_12m = (String) trend_obj_12m.get("click");
                        delta_12m = Integer.parseInt(click_now) - Integer.parseInt(click_12m);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                    try {
                        trend_obj_24m = (JSONObject) arr.get(0);
                        date_24m = (String) trend_obj_24m.get("date");
                        click_24m = (String) trend_obj_24m.get("click");
                        delta_24m = Integer.parseInt(click_now) - Integer.parseInt(click_24m);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }

                    if (delta_1m != -1) {
                        delta1m = String.valueOf(delta_1m);
                    }
                    if (delta_3m != -1) {
                        delta3m = String.valueOf(delta_3m);
                    }
                    if (delta_6m != -1) {
                        delta6m = String.valueOf(delta_6m);
                    }
                    if (delta_12m != -1) {
                        delta12m = String.valueOf(delta_12m);
                    }
                    if (delta_24m != -1) {
                        delta24m = String.valueOf(delta_24m);
                    }

                    int positive_count = 0;
                    if(delta_1m >0) { positive_count++; }
                    if(delta_3m >0) { positive_count++; }
                    if(delta_6m >0) { positive_count++; }
                    if(delta_12m >0) { positive_count++; }
                    if(delta_24m >0) { positive_count++; }


                    String cat_name = data.get("sk_id").replaceAll(",", "-");

                    String predict_clicks_avg_str = "";
                    String predict_price_avg_str = "";
                    if(predict_clicks_avg != 0){
                        predict_clicks_avg_str = String.valueOf(predict_clicks_avg);
                    }
                    if(predict_price_avg != 0){
                        predict_price_avg_str = String.valueOf(predict_price_avg);
                    }

                    Connection conn3 = mysql.initConnect(Conf.DATALAB_IP);
                    String cat_full = cat_name.replaceAll("-",">");
                    ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> datas = mysql.selectNaverLight(conn3, "select * from naver_light where cat_full='" + cat_full + "'");
                    conn3.close();
                    int review_sum = 0;
                    int price_sum = 0;
                    int product_sum = 0;
                    int price_avg = 0;
                    int count = 0;
                    for(int n=0; n < datas.size(); n++) {
                        try {
                            JSONArray datar = (JSONArray) parser.parse(datas.get(n).data_review);
                            int price = datas.get(n).price;
                            int review_count = datar.size();
                            price_sum += price;
                            review_sum += review_count;
                            product_sum++;
                            count++;
                        }catch (NullPointerException er){
                            continue;
                        }
                    }
                    if(count > 0) {
                        price_avg = Math.round(price_sum / count);
                    }

                    String csv_line = cat_name + "," + delta24m + "," + delta12m + "," + delta6m + "," + delta3m + "," + delta1m + "," + String.valueOf(positive_count) + "," + ss_avg + "," + fw_avg + "," + data.get("click_300_avg") + "," + data.get("price_300_avg") + "," + data.get("click_300_median") + "," + data.get("price_300_median")+ "," + data.get("click_300_1st") + "," + data.get("price_300_1st") + "," + data.get("click_300_3rd") + "," + data.get("price_300_3rd") + "," + product_sum + "," + review_sum + "," + price_avg;

                    try {
                        writer.write(csv_line);
                        writer.write("\r\n");
                    } catch (IOException er) {
                        er.printStackTrace();
                    }

                    System.out.println("완료 [" + i + "/" + datalab_list.size() + "]");
                }

            }
            writer.close();
            writer2.close();
            conn.close();
            System.out.println("모두 완료");
            System.out.println("에러수:" + String.valueOf(error_count));


        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    /**
     * 300원 기준 평균 광고비단가 계산
     *
     */
    public void updateDatalabClickAvgPrice(){

        JSONObject obj = null;
        JSONArray arr2 = null;
        JSONParser parser = new JSONParser();

        try {
            Connection conn = mysql.initConnect(Conf.DATALAB_IP);

            ArrayList<HashMap<String, String>> datalab_list = mysql.selectDatalab(conn, "(select * from datalab_insight where age30=100 and female > 70) union (select * from datalab_insight where age20=100 and female > 70)");
            conn.close();

        HashMap<String, String> keyword_map = new HashMap<>();
        ArrayList<String> keyword_list = new ArrayList<>();
        HashMap<String,ArrayList<String>> sk_id_keywords = new HashMap<>();

        for(int i =0; i < datalab_list.size(); i++) {
            HashMap<String,String> data = datalab_list.get(i);
            arr2 = null;
            try {
                obj = (JSONObject) parser.parse(data.get("data_total"));
                arr2 = (JSONArray) obj.get("keyword");
            } catch (java.lang.ClassCastException ER) {

            } catch(ParseException er){

            }
            if (arr2 == null) {
                try {
                    arr2 = (JSONArray) parser.parse(data.get("keyword_total"));
                } catch (NullPointerException er) {
                    System.out.println("what?");
                } catch(ParseException er){
                    System.out.println("what?");
                }
            }
            sk_id_keywords.put(data.get("sk_id"), new ArrayList<>());

            int check =0;
            for(int j=KEYWORD_START_NUMBER; j < KEYWORD_NUMBERS; j++){
                try {
                    check = j;
                    String keyword = (String) arr2.get(j);
                    sk_id_keywords.get(data.get("sk_id")).add(keyword);
                    if (!keyword_map.containsKey(keyword)) {
                        keyword_map.put(keyword, data.get("sk_id"));
                        keyword_list.add(keyword);
                    }
                }catch(IndexOutOfBoundsException er){
                    System.out.println(data.get("sk_id"));
                    continue;
                }
            }
        }

        if(arr2 != null){
            driver= DriverControl.getGeneralDriver();
            driver.get("https://manage.searchad.naver.com/customers/1561953/tool/keyword-planner");
            HashMap<String, int[]> rs = keywordDashboardCheck(driver, keyword_list, "300");
            System.out.println(rs.size());
            for( HashMap.Entry<String, ArrayList<String>> elem : sk_id_keywords.entrySet() ){
                String sk_id = elem.getKey();
                ArrayList<String> keywords = elem.getValue();
                int count = 0;
                int click_sum = 0;
                int price_sum = 0;
                int median_num = Math.round(keywords.size() / 2);
                int click_median = -1;
                int price_median = -1;
                int click_1st = -1;
                int price_1st = -1;
                int click_3rd = -1;
                int price_3rd = -1;

                for(int k=0; k < keywords.size(); k++){
                    try {
                        int[] data = rs.get(keywords.get(k).toUpperCase());
                        int click = data[0];
                        int price = data[1];
                        click_sum += click;
                        price_sum += price;
                        count++;
                        if(k == median_num){
                            click_median = data[0];
                            price_median = data[1];
                        }
                        if(k == 0){
                            click_1st = data[0];
                            price_1st = data[1];
                        }
                        if(k == 2){
                            click_3rd = data[0];
                            price_3rd = data[1];
                        }

                    }catch(Exception er){
                        System.out.println("제외 키워드");
                        System.out.println(er.getMessage());
                    }
                }
                try {
                    int click_avg = Math.round(click_sum / count);
                    int price_avg = Math.round(price_sum / count);

                    Connection conn2 = mysql.initConnect(Conf.DATALAB_IP);
                    mysql.updateDatalabClickPriceAvg(conn2, "update datalab_insight set click_300_avg=?, price_300_avg=?, click_300_median=?, price_300_median=?, click_300_1st=?, price_300_1st=?, click_300_3rd=?, price_300_3rd=? where sk_id=?", click_avg, price_avg, click_median, price_median,click_1st, price_1st, click_3rd, price_3rd, sk_id);
                    conn2.close();

                }catch(Exception er){

                }

            }


        }

    }catch(Exception er){
            er.printStackTrace();
        return;
    }

    }

    /**
     * 키워드툴 웹페이지 접근
     * @param driver 크롬드라이버
     */
    public void accessKeywordToolPage(ChromeDriver driver){

        try {

            driver.get("https://manage.searchad.naver.com/customers/1561953/tool/keyword-planner");
           Thread.sleep(5000);
            String current_url = (String)((JavascriptExecutor) driver).executeScript("return location.href");
            if(current_url.split("searchad.naver.com").length > 1) {

                driver.get("https://searchad.naver.com/login?returnUrl=https:%2F%2Fmanage.searchad.naver.com&returnMethod=get");
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('naver_login_btn')[0].click()");
                //아이디.비번 입력
                lt.loginWait(driver);
            }

            driver.get("https://manage.searchad.naver.com/customers/1561953/tool/keyword-planner");


        }catch(Exception er){
            er.printStackTrace();
        }

    }

    /**
     * 키워드별 클릭비용 리턴
     *
     * @param driver 크롬드라이버
     * @param keyword_list 키워드리스트
     * @price beting_price 입찰가
     */
    public  HashMap<String,int[]> keywordDashboardCheck(ChromeDriver driver, ArrayList keyword_list,String beting_price){

        HashMap<String,int[]> keyword_click_price = new HashMap<>();

        try {
            ArrayList<String> keywords_100_dummy = new ArrayList<>();
            double round = Math.ceil(keyword_list.size() / 100) + 1;
            if(round < 1) { round = 1;}
            for (int kw = 0; kw < round; kw++) {
                String keywords = "";
                for (int k = kw * 100; k < (kw * 100) + 100; k++) {
                    if (k < keyword_list.size()) {
                        String keyword = (String) keyword_list.get(k);
                        keyword= keyword.replaceAll(" ", "");
                        if(keyword.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
                            keywords += keyword;
                            keywords += "\\r";
                        }
                    }
                }
                keywords_100_dummy.add(keywords);
            }



            for (int j = 0; j < keywords_100_dummy.size(); j++) {
                try{
                    long howmany = (Long)((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('naver_login_btn').length");
                    if(howmany > 0){
                        lt.loginWait(driver);
                        driver.get("https://manage.searchad.naver.com/customers/1561953/tool/keyword-planner");
                    }
                }catch(NoSuchMethodError er){

                }catch(Exception erer){

                }
                String keywords = "";
                try {
                    Thread.sleep(5000);
                    keywords = keywords_100_dummy.get(j);
                    String str = "document.querySelectorAll('textarea')[1].value =\"" + keywords + "\"";
                    Thread.sleep(1000);
                    ((JavascriptExecutor) driver).executeScript(str);
                    Thread.sleep(2000);
                    WebElement textarea = (WebElement) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('textarea')[1]");
                    textarea.sendKeys(Keys.RETURN);
                    Thread.sleep(1000);
                }catch(NoSuchMethodError EEE){
                    System.out.println(keywords);
                }

                Thread.sleep(3000);
                ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('.btn-primary')[1].click()");
                Thread.sleep(1000);
                try {
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                }catch(NoAlertPresentException er){

                }

                try {
                    Thread.sleep(2000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('bidamt-area')[0].children[0].value=''");
                }catch (NoSuchMethodError er){

                    ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('.btn-primary')[1].click()");
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('bidamt-area')[0].children[0].value=''");
                }
                WebElement input_wrapper = (WebElement) driver.findElementsByClassName("bidamt-area").get(0);
                WebElement input_field = input_wrapper.findElement(By.xpath(".//input"));
                input_field.sendKeys(beting_price);
                input_field.sendKeys(Keys.ENTER);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn-primary')[0].click()");

                Thread.sleep(3000);
                Long row_length = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.data-table-tbody')[0].children.length");
                for (int r = 0; r < row_length; r++) {

                    int[] data = new int[3];
                    String keyword = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('data-table-tbody')[0].children[" + r + "].children[1].textContent");
                    String clicks = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('data-table-tbody')[0].children[" + r + "].children[4].textContent");
                    int clicks_int = Format.parse(clicks).intValue();
                    data[0] = clicks_int;
                    String price = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('data-table-tbody')[0].children[" + r + "].children[5].textContent");
                    //price = price.replace(price.substring(price.length() - 1), "");
                    int price_int = Format.parse(price).intValue();
                    data[1] = price_int;
                    String exposed = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('data-table-tbody')[0].children[" + r + "].children[3].textContent");
                    int exposed_int = Format.parse(exposed).intValue();
                    data[2] = exposed_int;
                    HashMap<String, int[]> hs = new HashMap<>();
                    keyword_click_price.put(keyword, data);

                }
                ((JavascriptExecutor) driver).executeScript("window.history.go(-1)");



            }

        }catch(Exception er){
            er.printStackTrace();
        }

        return keyword_click_price;
    }








}
