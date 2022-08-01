package Datalab;

import Connection.MySQLConnector;
import Util.Conf;
import Util.DriverControl;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.lang.Thread.sleep;

public class CategoryScrapper {
    MySQLConnector mysql = new MySQLConnector();
    Connection conn = null;
    ChromeDriver driver;
    String download_dir = Conf.DATALAB_DOWNLOAD_DIR;
    String DBIP;
    public CategoryScrapper(String DB_IP) {
        new File(download_dir).mkdirs();
        conn = mysql.initConnect(DB_IP);
        DBIP = DB_IP;
    }

    public void initDriver(){

        if (driver == null) {
            driver= DriverControl.getGeneralDriverDownload(download_dir);
        }
    }

    public String monthago (String standard_date, int i) {
        String monthago = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(sdf.parse(standard_date));
            cal1.add(Calendar.MONTH, -1 * i);
            monthago = sdf.format(cal1.getTime()) + "-01";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return monthago;
    }

    public void getGrowth(){

        JSONParser parser = new JSONParser();
        double growth_3m = -999;
        double growth_6m = -999;
        double growth_12m = -999;
        double growth = -999;
        String first_date = "";
        String last_date = "";
        org.json.simple.JSONArray result_array = new org.json.simple.JSONArray();

        try {
            Connection conn2 = mysql.initConnect(DBIP);
            ArrayList<HashMap<String, String>> datalab_data = mysql.selectDatalabAll(conn2, "select * from datalab_insight where insert_time > '2022-01-01'");
            conn2.close();

            for(int i =0; i < datalab_data.size(); i++) {

                HashMap data = datalab_data.get(i);
                String sk_id = (String) data.get("sk_id");
                HashMap<String,Integer> date_click = new HashMap<String,Integer>();
                org.json.simple.JSONArray data_totals = (org.json.simple.JSONArray) parser.parse((String)data.get("data_total"));
                org.json.simple.JSONArray data_total_array = new org.json.simple.JSONArray();
                for(int t=0; t < data_totals.size(); t++) {
                    try {
                        org.json.simple.JSONArray array = (org.json.simple.JSONArray) data_totals.get(t);
                        for (int k = 0; k < array.size(); k++) {
                            org.json.simple.JSONObject obj = (org.json.simple.JSONObject) array.get(k);
                            if (obj.containsKey("date") && obj.containsKey("click")) {
                                data_total_array.add(obj);
                            }
                        }
                    }catch(Exception ex){
                        org.json.simple.JSONObject obj = (org.json.simple.JSONObject) data_totals.get(t);
                        if (obj.containsKey("date") && obj.containsKey("click")) {
                            data_total_array.add(obj);
                        }
                    }

                }



                try {
                    first_date = (String) ((org.json.simple.JSONObject) data_total_array.get(0)).get("date");
                    first_date = first_date.replaceAll("\"", "");
                }catch(Exception ex){
                    ex.printStackTrace();
                    continue;
                }



                for(int j=0;  j < data_total_array.size(); j++) {

                    try {
                        org.json.simple.JSONObject obj = (org.json.simple.JSONObject) data_total_array.get(j);
                        String date = (String) obj.get("date");
                        date = date.replaceAll("\"", "");
                        String clicks = (String) obj.get("click");
                        date_click.put(date, Integer.parseInt(clicks));

                        //마지막 날
                        if (j == (data_total_array.size() - 1)) {
                            last_date = date;
                        }
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }

                }

                String three_monthago = monthago(last_date,3);
                String six_monthago = monthago(last_date,6);
                String twelve_monthago = monthago(last_date,12);


                try {
                    double parent = date_click.get(three_monthago) * 1.0;
                    if(parent == 0) {
                        parent = 1.0;
                    }
                    growth_3m = Math.pow(date_click.get(last_date) * 1.0 / parent , 1.0 / 3 * 1.0);
                }catch(Exception ex) {

                }


                try {
                    double parent = date_click.get(six_monthago) * 1.0;
                    if(parent == 0) {
                        parent = 1.0;
                    }
                    growth_6m = Math.pow(date_click.get(last_date) * 1.0 / parent, 1.0 / 6 * 1.0);
                }catch(Exception ex) {

                }

                try {
                    double parent = date_click.get(twelve_monthago) * 1.0;
                    if(parent == 0) {
                        parent = 1.0;
                    }
                    growth_12m = Math.pow(date_click.get(last_date) * 1.0 / parent, 1.0 / 12 * 1.0);
                }catch(Exception ex) {

                }
                //전체
                try {
                    double parent = date_click.get(first_date) * 1.0;
                    if(parent == 0) {
                        parent = 1.0;
                    }
                    growth = Math.pow(date_click.get(last_date) * 1.0 / parent, 1.0 / (date_click.size()-1) * 1.0);
                }catch(Exception ex) {

                }

                org.json.simple.JSONObject result = new org.json.simple.JSONObject();
                result.put("sk_id",sk_id);
                result.put("big_cat",data.get("big_cat"));
                result.put("middle_cat",data.get("middle_cat"));
                result.put("small_cat",data.get("small_cat"));
                result.put("product_cat",data.get("product_cat"));
                result.put("mobile",data.get("mobile"));
                result.put("pc",data.get("pc"));
                result.put("male",data.get("male"));
                result.put("female",data.get("female"));
                result.put("age10",data.get("age10"));
                result.put("age20",data.get("age20"));
                result.put("age30",data.get("age30"));
                result.put("age40",data.get("age40"));
                result.put("age50",data.get("age50"));
                result.put("age60",data.get("age60"));
                result.put("growth_3m", growth_3m);
                result.put("growth_6m", growth_6m);
                result.put("growth_12m", growth_12m);
                result.put("growth", growth);
                result.put("first_date",first_date);
                result.put("last_date",last_date);
                result_array.add(result);
            }

            Collections.sort(result_array, new Comparator<org.json.simple.JSONObject>() {
                @Override
                public int compare(org.json.simple.JSONObject o1, org.json.simple.JSONObject o2) {
                    Double v1 = 0.0;
                    Double v3= 0.0;
                    try {
                        v1 = (Double) o1.get("growth_3m");
                        v3 = (Double) o2.get("growth_3m");

                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    return v3.compareTo(v1);
                }
            });

            for(int p=0; p < result_array.size(); p++){
                org.json.simple.JSONObject obj = (org.json.simple.JSONObject)result_array.get(p);
                obj.put("ranking", p+1);
            }

            mysql.insertGrowthDatalab(conn, "insert into growth_datalab(data) values(?)", result_array.toJSONString());
            conn.close();

        }catch(Exception ex) {
            ex.printStackTrace();
        }



    }


    public void scrapDetail(String part , String total) throws  Exception{

        initDriver();
        driver.get("https://datalab.naver.com/shoppingInsight/sCategory.naver");
        Long big_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].childElementCount");
        mainloop:
        for (int i = 0; i < big_cat_numbers; i++) {
            String big_cat_name = null;
            Long middle_cat_numbers = 0L;
            try {
                //2단계
                big_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].children[" + i + "].firstElementChild.text");
                ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].children[" + i + "].firstElementChild.click()");
                sleep(1000);
                middle_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].childElementCount");
            }catch (NoSuchMethodError er){
                driver.navigate().refresh();
                big_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].children[" + i + "].firstElementChild.text");
                ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[0].children[" + i + "].firstElementChild.click()");
                sleep(2000);
                middle_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].childElementCount");

            }
            for (int j = 0; j < middle_cat_numbers; j++) {
                String middle_cat_name = null;
                Long small_cat_numbers = 0L;
                try {
                    //3단계
                    middle_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].children[" + j + "].firstElementChild.text");
                    ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].children[" + j + "].firstElementChild.click()");
                    sleep(1000);
                    small_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].childElementCount");
                }catch(NoSuchMethodError er){
                    driver.navigate().refresh();
                    middle_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].children[" + j + "].firstElementChild.text");
                    ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[1].children[" + j + "].firstElementChild.click()");
                    sleep(2000);
                    small_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].childElementCount");
                }

                for (int k = 0; k < small_cat_numbers; k++) {
                    //4단계 있는지 체크
                    String small_cat_name = null;
                    String product_cat_exist = null;
                    String product_cat_name = null;
                    try{
                        small_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].children[" + k + "].firstElementChild.text");
                        ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].children[" + k + "].firstElementChild.click()");
                        sleep(500);
                        product_cat_exist = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].children[0].firstElementChild.getAttribute('data-cid')");
                    } catch(NoSuchMethodError er) {
                        driver.navigate().refresh();
                        sleep(2000);
                        small_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].children[" + k + "].firstElementChild.text");
                        ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[2].children[" + k + "].firstElementChild.click()");
                        product_cat_name = null;
                        product_cat_exist = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].children[0].firstElementChild.getAttribute('data-cid')");
                    } catch(JavascriptException EX){
                        Thread.sleep(1000*60 *10);
                        driver.get("https://datalab.naver.com/shoppingInsight/sCategory.naver");
                        continue mainloop;
                    }
                    Long product_cat_numbers = 0L;
                    if (product_cat_exist != null) {
                        product_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].childElementCount");
                        for (int p = 0; p < product_cat_numbers; p++) {
                            product_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].children[" + p + "].firstElementChild.text");
                            ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('scroll_cst')[3].children[" + p + "].firstElementChild.click()");
                            sleep(500);
                            Boolean pressWell = categoryPressCheck(big_cat_name, middle_cat_name, small_cat_name, product_cat_name);
                            if(pressWell == true) {
                                Connection conn2 = mysql.initConnect(DBIP);
                                ArrayList<HashMap<String, String>> check = mysql.selectDatalab(conn2, "select * from datalab_insight where big_cat='" + big_cat_name + "' and middle_cat='" + middle_cat_name + "' and small_cat='" + small_cat_name + "' and product_cat='" + product_cat_name + "' and insert_time > DATE_ADD(now(), INTERVAL -7 DAY)");
                                conn2.close();
                                if(check.size() == 0) {
                                    Thread.sleep(1000*5);
                                    scrap(driver, big_cat_name, middle_cat_name, small_cat_name, product_cat_name);
                                    Thread.sleep(1000*60 * 2);
                                }
                            }
                            else {
                                System.out.println("IP를 바꿉니다.5분간 휴식 --");
                                Thread.sleep(1000 * 60 * 1);
                            }
                        }
                    }

                    else {
                        Boolean pressWell = categoryPressCheck(big_cat_name, middle_cat_name, small_cat_name, product_cat_name);
                        if(pressWell == true) {
                            Connection conn2 = mysql.initConnect(DBIP);
                            ArrayList<HashMap<String, String>> check = mysql.selectDatalab(conn2, "select * from datalab_insight where big_cat='" + big_cat_name + "' and middle_cat='" + middle_cat_name + "' and small_cat='" + small_cat_name + "' and insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                            conn2.close();
                            if(check.size() == 0) {
                                Thread.sleep(1000*5);
                                scrap(driver, big_cat_name, middle_cat_name, small_cat_name, product_cat_name);
                                Thread.sleep(1000*60 * 5);
                            }
                        }
                        else {
                            System.out.println("IP를 바꿉니다.5분간 휴식 --");
                            Thread.sleep(1000 * 60 * 1);
                        }
                    }

                }

            }

        }
    }

    public void datalabTotal(String part, String total) throws  Exception {
        scrapDetail(part,total);
        getGrowth();
    }


    public boolean categoryPressCheck(String select1, String select2, String select3, String select4){

        Boolean rs = false;
        String press1 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[0].firstElementChild.textContent");
        String press2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[1].firstElementChild.textContent");
        String press3 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[2].firstElementChild.textContent");

        if(select4 == null) {
            if (!press1.equals("1분류") && !press2.equals("2분류") && !press3.equals("3분류")) {
                rs = true;
            }
        }
        if(select4 != null){
            String press4 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('select')[3].firstElementChild.textContent");
            if (!press1.equals("1분류") && !press2.equals("2분류") && !press3.equals("3분류") && !press4.equals("4분류")) {
                rs = true;
            }
        }

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
                Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[1].children[1].children[" + (list-2) + "].children[0].click()");
                Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[0].children[1].children[" + j + "].children[0].click()");
                Thread.sleep(1000);
                String year_str = (String) ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[0].children[1].children[" + j + "].children[0].textContent");
                year_str_array.put(year_str);
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w3')[0].children[1].children[0].children[0].click()");
                Thread.sleep(1000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w2')[1].children[1].children[0].children[0].click()");
                    Thread.sleep(1000);
                    Long last_mon = (Long)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('w3')[2].children[1].children.length");
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('w3')[2].children[1].children["+ (last_mon-1) + "].children[0].click()");
                    Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('btn_submit')[0].click()");
                sleep(1000);
                if(j ==(list-1) || j== (list-2)) {
                    for (int tt = 0; tt < 20; tt++) {

                        for (int kk = 0; kk < 20; kk++) {
                            //document.getElementsByClassName('rank_top1000_list')[0].children[1].firstElementChild.firstElementChild.nextSibling.textContent.trim()
                            try {
                                Thread.sleep(20);
                                String keyword = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('rank_top1000_list')[0].children[" + kk + "].firstElementChild.firstElementChild.nextSibling.textContent.trim()");
                                keywords_total.put(keyword);
                            } catch (NoSuchMethodError erer) {
                                continue;
                            } catch (Exception ex) {

                            }
                        }
                        ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn_page_next')[0].click()");
                        sleep(1000);
                        //document.getElementsByClassName('btn_page_next')[0].click()
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
            //er.printStackTrace();
        }

        return trends_array;
    }

    public JSONArray getKeywords(){
        JSONArray keywords = new JSONArray();

        for (int kk = 0; kk < 500; kk++) {
            //document.getElementsByClassName('rank_top1000_list')[0].children[1].firstElementChild.firstElementChild.nextSibling.textContent.trim()
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
