package Datalab;

import Connection.MySQLConnector;
import Util.Conf;
import Util.DriverControl;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.*;
import java.sql.Connection;
import java.util.*;
import static java.lang.Thread.sleep;

public class CategorySelectScrapper {
    MySQLConnector mysql = new MySQLConnector();
    ChromeDriver driver;
    String download_dir = Conf.DATALAB_DOWNLOAD_DIR;
    String DBIP = Conf.NAVER_DB_IP;

    public CategorySelectScrapper(String DB_IP) {
        new File(download_dir).mkdirs();
        DBIP = DB_IP;
    }

    public void initDriver(){
        driver= DriverControl.getGeneralDriver();
    }


    public void scrapDetail(String special_category) throws  Exception{

        initDriver();
        driver.get("https://datalab.naver.com/shoppingInsight/sCategory.naver");
        Connection conn = mysql.initConnect(DBIP);
        ArrayList<HashMap<String, String>> datalab_list = new ArrayList<>();
        if(special_category == "null") {
            datalab_list = mysql.selectDatalab(conn, "select * from datalab_insight where data_total is null");
        }
        else {
            datalab_list = mysql.selectDatalab(conn, "SELECT * FROM datalab_insight WHERE sk_id LIKE '%" + special_category + "%'");
        }
        conn.close();

        ArrayList<String> arr = new ArrayList<>();
        String[] splits = special_category.split(",");
        for(int j=0; j < splits.length; j++){
            arr.add(splits[j]);
        }
        ArrayList<String> rs = categorySelect(arr);

        //카테고리 제대로 눌렀는지 체크
        Boolean pressedWell = categoryPressCheck(rs.get(0), rs.get(1), rs.get(2), rs.get(3));
        if(pressedWell == true) {
            scrap(driver, rs.get(0), rs.get(1), rs.get(2), rs.get(3));
        }
        else {
            System.out.println("오류발생");
            Thread.sleep(1000 * 60 * 1);
        }


        DriverControl.quitDriver(driver);
        return;



    }

    public boolean categoryPressCheck(String select1, String select2, String select3,String select4){

        Boolean rs = false;
        String press1 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[0].firstElementChild.textContent");
        String press2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[1].firstElementChild.textContent");
        String press3 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[2].firstElementChild.textContent");
        if(select4 != null) {
            String press4 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[3].firstElementChild.textContent");

            if (press1.equals(select1) && press2.equals(select2) && press3.equals(select3) && press4.equals(select4)) {
                rs = true;
            }
        }
        if(select4 == null){
            if (press1.equals(select1) && press2.equals(select2) && press3.equals(select3)) {
                rs = true;
            }
        }
        return rs;
    }


    public ArrayList<String> categorySelect(ArrayList<String> list) throws Exception {


        String select1 = (String)list.get(0);
        String select2 = (String)list.get(1);
        String select3 = (String)list.get(2);
        String select4 = null;

        if(list.size() >= 3){


            Long count1 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].childElementCount");
            for(int i=0; i < count1; i++){
                String name1 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].children[" + i + "].textContent");
                if(name1.equals(select1)){
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('scroll_cst')[0].children[" + i + "].firstElementChild.click()");
                    sleep(1000);
                    break;
                }
            }

            Long count2 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].childElementCount");
            for(int i=0; i < count2; i++){
                String name2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].children[" + i + "].textContent");
                if(name2.equals(select2)){
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('scroll_cst')[1].children[" + i + "].firstElementChild.click()");
                    sleep(1500);
                    break;
                }
            }

            Long count3 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].childElementCount");
            for(int i=0; i < count3; i++){
                String name3 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].children[" + i + "].textContent");
                if(name3.equals(select3)){
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('scroll_cst')[2].children[" + i + "].firstElementChild.click()");
                    sleep(2000);
                    break;
                }
            }

        }
        if(list.size() == 4){

            select4 = (String)list.get(3);

            Long count4 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].childElementCount");
            for(int i=0; i < count4; i++){
                String name4 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].children[" + i + "].textContent");
                if(name4.equals(select4)){
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('scroll_cst')[3].children[" + i + "].firstElementChild.click()");
                    sleep(2000);
                    break;
                }
            }
        }

        ArrayList<String> rs = new ArrayList<>();
        rs.add(select1);
        rs.add(select2);
        rs.add(select3);
        rs.add(select4);

        return rs;

    }



    public void scrap(ChromeDriver driver, String big_cat_name, String middle_cat_name, String small_cat_name, String product_cat_name){

        ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w4')[0].children[1].children[2].children[0].click()");


        long list = (Long)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('w2')[0].children[1].children.length");


        JSONArray year_str_array = new JSONArray();
        JSONArray trend_array = new JSONArray();
        ArrayList<Double> ages = new ArrayList<>();
        JSONArray keywords_total = new JSONArray();
        Double mobile_percent = 0.0;
        Double pc_percent = 0.0;
        Double male_percent = 0.0;
        Double female_percent = 0.0;


        for(int j =1; j < list; j++ ) {


            try {


                ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('label[for=\"8_set_period3\"]')[0].click()");
                Thread.sleep(1000);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[0].children[1].children[1].children[0].click()");
                Thread.sleep(500);
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[1].children[1].children[" + (list-2) + "].children[0].click()");
                Thread.sleep(500);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[0].children[1].children[" + j + "].children[0].click()");
                Thread.sleep(500);
                String year_str = (String) ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[0].children[1].children[" + j + "].children[0].textContent");
                year_str_array.put(year_str);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w3')[0].children[1].children[0].children[0].click()");
                Thread.sleep(500);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[1].children[1].children[0].children[0].click()");
                Thread.sleep(500);

                Long last_mon = (Long)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('w3')[2].children[1].children.length");
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w3')[2].children[1].children["+ (last_mon-1) + "].children[0].click()");
                Thread.sleep(500);

                ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('btn_submit')[0].click()");
                sleep(1000);



                for (int kk = 0; kk < 20; kk++) {
                    //document.getElementsByClassName('rank_top1000_list')[0].children[1].firstElementChild.firstElementChild.nextSibling.textContent.trim()
                    try {
                        Thread.sleep(20);
                        String keyword = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('rank_top1000_list')[0].children[" + kk + "].firstElementChild.firstElementChild.nextSibling.textContent.trim()");
                        keywords_total.put(keyword);
                    } catch (NoSuchMethodError erer) {
                        continue;
                    } catch(Exception ex){

                    }
                }

                try {
                    String mobile = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.bb-arcs-mo')[0].nextElementSibling.textContent");
                    String male = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.bb-arcs-m')[0].nextElementSibling.textContent");
                    Long items = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.list_item').length");
                    for (int tt = 0; tt < items; tt++) {
                        try {
                            String age = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.list_item')[" + tt + "].children[0].children[0].style.height");
                            Double age_percent = Double.parseDouble(age.replace(age.substring(age.length() - 1), ""));
                            ages.add(age_percent);
                        } catch (NoSuchMethodError er) {
                            System.out.println(er.getMessage());
                        } catch(Exception ex){

                        }
                    }

                    mobile_percent = Double.parseDouble(mobile.replace(mobile.substring(mobile.length() - 1), ""));
                    pc_percent = 100 - mobile_percent;
                    male_percent = Double.parseDouble(male.replace(male.substring(male.length() - 1), ""));
                    female_percent = 100 - male_percent;


                } catch (NoSuchMethodError er) {

                } catch(Exception ex){

                }


                try {
                    FileUtils.cleanDirectory(new File(download_dir));
                    sleep(3000);
                } catch (Exception er) {

                }


                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn_submit')[0].click()");


                JSONArray trends_all = getTrends();

                trend_array.put(trends_all);


            } catch (Exception er) {
                er.printStackTrace();
            }
        }

        //스케일링
        for(int k=0; k < trend_array.length(); k++){

            try {
                JSONArray current_year = (JSONArray) trend_array.get(k);
                if(k != (trend_array.length()-1)) {
                    JSONArray next_year = (JSONArray) trend_array.get(k+1);

                    JSONObject last_day_of_current = (JSONObject) current_year.get(current_year.length()-1);
                    JSONObject first_day_of_next = (JSONObject) next_year.get(0);
                    double last_day = Double.parseDouble((String)last_day_of_current.get("click"));
                    double first_day= Double.parseDouble((String)first_day_of_next.get("click"));
                    double scale_ratio = last_day /first_day;
                    for(int t=0; t< next_year.length(); t++){
                        JSONObject obj = (JSONObject) next_year.get(t);
                        long click = Long.parseLong((String)obj.get("click"));
                        long new_click = (Math.round(click * scale_ratio));
                        JSONObject new_obj = new JSONObject();
                        new_obj.put("date", (String)obj.get("date"));
                        new_obj.put("click",String.valueOf(new_click));
                        next_year.put(t, new_obj);
                    }
                    trend_array.put(k+1, next_year);

                }

            }catch(Exception ex){
                ex.printStackTrace();
            }

        }



        try {
            if (trend_array.length() > 0) {
                String notfoundtext = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('blank_space')[0].style.display");
                if (notfoundtext.equals("none")) {
                    Connection conn2 = mysql.initConnect(DBIP);
                    mysql.insertDatalabTrend(conn2, "insert into datalab_insight(sk_id, big_cat, middle_cat, small_cat, product_cat, mobile,pc,male,female,age10,age20,age30,age40,age50,age60,data_1m,data_3m, data_total,keyword_1m,keyword_3m, keyword_total) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", big_cat_name, middle_cat_name, small_cat_name, product_cat_name, mobile_percent, pc_percent, male_percent, female_percent, ages.get(0), ages.get(1), ages.get(2), ages.get(3), ages.get(4), ages.get(5), null, null, trend_array.toString(), null, null, keywords_total.toString());
                    conn2.close();
                } else {
                    Thread.sleep(1000 * 60 * 1);
                    System.out.println("에러 잠시휴식");
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }



    public JSONArray getTrends(){

        JSONArray trends_array = new JSONArray();

        try {
            ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn_document_down')[0].click()");
            sleep(3000);
            final File folder = new File(download_dir);
            String filename = listFilesForFolder(folder);
            sleep(3000);
            File csvFile = new File(download_dir + "\\" + filename);
            BufferedReader br = null;
            String line = "";
            int cnt = 0;
            try {

                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {
                    cnt++;
                    if (cnt >= 10) {
                        System.out.println(line);
                        // use comma as separator
                        String[] data = line.split(",");
                        JSONObject obj = new JSONObject();
                        if (data.length == 2) {
                            obj.put("date", data[0]);
                            obj.put("click", data[1]);
                        }
                        trends_array.put(obj);

                    }

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                        FileUtils.cleanDirectory(new File(download_dir));
                        sleep(3000);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }catch(Exception er){

        }

        return trends_array;
    }

    public JSONArray getKeywords(){
        JSONArray keywords = new JSONArray();

        for (int kk = 0; kk < 500; kk++) {
           try {
                String keyword = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('rank_top1000_list')[0].children[" + kk + "].firstElementChild.firstElementChild.nextSibling.textContent.trim()");
                keywords.put(keyword);
            } catch (NoSuchMethodError erer) {
                continue;
            }
        }
        return keywords;
    }


    public void capture(String filepath) throws InterruptedException, IOException {
        File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        File screenshotLocation = new File(filepath);
        FileUtils.copyFile(screenshot, screenshotLocation);
    }

    public String listFilesForFolder(final File folder) {
        String rs = "";
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                rs = fileEntry.getName();
                break;
            }
        }
        return rs;
    }

}
