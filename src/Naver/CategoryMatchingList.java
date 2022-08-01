package Naver;

import DatalabPlus.ProductAnalysis;
import Connection.MySQLConnector;
import Util.Conf;
import Util.DriverControl;
import Util.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class CategoryMatchingList {

    MySQLConnector mysql = new MySQLConnector();
    ChromeDriver driver;
    ChromeDriver driver2;
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    int KEYWORD_START_NUMBER = 0;
    int KEYWORD_NUMBERS = 10;
    ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);


    public void findLowestPricelist(int part, int total){

        Conf mv = new Conf();
        ArrayList<String> lowlist_urls = new ArrayList<>();
        ArrayList<String> serviceid_list = new ArrayList<>();
        HashMap<String,Integer> duptest = new HashMap<>();

        try {
            driver = DriverControl.getGeneralDriver();
            driver.get("https://shopping.naver.com");
            Thread.sleep(5000);
            Long help_menu_length = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.co_cel a').length");
            ArrayList<String> help_menu_urls = new ArrayList<>();
            for (int i = 0; i < help_menu_length; i++) {
                String help_menu_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.co_cel a')[" + i + "].href");
                lowlist_urls.add(help_menu_url);
            }

        }catch(Exception er){

        }
        int slice_counts = Math.round(lowlist_urls.size() / total);
        int end_point = slice_counts * part;
        int starting_point = end_point- slice_counts;
        if(lowlist_urls.size()-end_point < slice_counts) {
            end_point = lowlist_urls.size();
        }

        for(int i=starting_point; i < end_point; i++) {

            try {
                String search_result = lowlist_urls.get(i);
                driver.get(search_result);
                String current_list_url = search_result;
                int pagenum = 1;
                try {
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('subFilter_filter__3Y-uy')[1].click()");
                    Thread.sleep(5000);
                }catch(NoSuchMethodError e) {


                }
                int bread = 1;
                while(true) {

                    try {
                        int current_scroll = 0;
                        HashMap<String,String> product_urls = new HashMap<>();
                        for(int scroll =0; scroll < 100; scroll++) {
                            ((JavascriptExecutor) driver).executeScript("window.scrollBy(" + current_scroll + "," + (current_scroll + 50) + ")");
                            current_scroll += 50;
                        }
                        Long product_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.basicList_mall_title__3MWFY a').length");
                        for (int pp = 0; pp < product_leng; pp++) {
                            String product_url = "";
                            try {
                                product_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.basicList_mall_title__3MWFY a')[" + pp + "].href");
                                if(product_url.split("cr.shopping.naver").length > 1) {
                                    product_urls.put(product_url, product_url);
                                }
                            } catch (NoSuchMethodError er) {
                                continue;
                            }
                        }
                        for(String key : product_urls.keySet()){
                            savePriceComparisonUrl(key, driver);
                        }


                    }catch(NoSuchMethodError e){
                        System.out.println("해당분야는 최저가가 없다");
                    }

                    try {
                        try {
                            driver.get(current_list_url);
                            for(int pa =0; pa < 10; pa++){
                                String pagenum_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_btn_page__FuJaU ')[" + pa + "].className");
                                if(pagenum_str.split("activ").length > 1){
                                    pagenum = (pa +1);
                                    break;
                                }
                            }

                            if (pagenum == 51) {
                                break;
                            }

                        } catch (NoSuchMethodError er) {

                            break;
                        }

                        Long next_text_check = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_next__1ITTf').length");
                        if (next_text_check == 1) {
                            ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('pagination_next__1ITTf')[0].click()");
                            Thread.sleep(3000);
                            current_list_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                        } else {
                            break;
                        }

                    } catch (NoSuchMethodError er) {
                        break;
                    }

                    driver.get(current_list_url);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 데이터랩 키워드 검색후 1페이지 상품 가격비교에서 찾기
     * (과거에는 지금처럼 가격비교 탭이 따로 없었음)
     *
     * @param part 시작지점
     * @param total 전체분할개수
     *
     */
    public void keywordFirstPageExposed(int part, int total) {
        JSONObject obj = null;
        JSONArray arr2 = null;
        int check =0;
        JSONParser parser = new JSONParser();
        driver = DriverControl.getGeneralDriver();
        ArrayList<HashMap<String, String>> datalab_list = null;
        try {
            Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
            datalab_list = mysql.selectDatalab(conn, "select * from datalab_insight");
            conn.close();
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        HashMap<String, String> keyword_map = new HashMap<>();
        ArrayList<String> keyword_list = new ArrayList<>();
        HashMap<String,ArrayList<String>> sk_id_keywords = new HashMap<>();

        for(int i =0; i < datalab_list.size(); i++) {
            HashMap<String,String> data = datalab_list.get(i);
            arr2 = null;
            try {
                obj = (JSONObject) parser.parse(data.get("data_total"));
                arr2 = (JSONArray) obj.get("keyword");
            } catch (java.lang.ClassCastException e1) {
                //e1.printStackTrace();
            } catch(org.json.simple.parser.ParseException e2){
                //e2.printStackTrace();
            }
            if (arr2 == null) {
                try {
                    arr2 = (JSONArray) parser.parse(data.get("keyword_total"));
                } catch (NullPointerException er) {
                    System.out.println("what?");
                } catch(org.json.simple.parser.ParseException er){
                    System.out.println("what?");
                }
            }

            sk_id_keywords.put(data.get("sk_id"), new ArrayList<>());

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
        int slice_counts = Math.round(keyword_list.size() / total);
        int end_point = slice_counts * part;
        int starting_point = end_point- slice_counts;
        if(keyword_list.size()-end_point < slice_counts) {
            end_point = keyword_list.size();
        }
        for(int j=starting_point; j < end_point; j++){
            String keyword = Utils.StringReplace((String)keyword_list.get(j));
            driver.get("https://search.shopping.naver.com/search/all.nhn?query=" + keyword ) ;

            DriverControl.tooManyRequest(driver);

            try {

                String current_list_url = "https://search.shopping.naver.com/search/all.nhn?query=" + keyword;
                int pagenum = 1;
                try {
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('subFilter_filter__3Y-uy')[1].click()");
                    Thread.sleep(5000);
                }catch(NoSuchMethodError e) {
                    //e.printStackTrace();
                }

                while(true) {
                    try {
                        int current_scroll = 0;
                        HashMap<String,String> product_urls = new HashMap<>();
                        for(int scroll =0; scroll < 100; scroll++) {
                            ((JavascriptExecutor) driver).executeScript("window.scrollBy(" + current_scroll + "," + (current_scroll + 50) + ")");
                            current_scroll += 50;
                        }

                        Long product_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.basicList_mall_title__3MWFY a').length");

                        for (int pp = 0; pp < product_leng; pp++) {

                            String product_url = "";
                            try {
                                product_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.basicList_mall_title__3MWFY a')[" + pp + "].href");
                                if(product_url.split("cr.shopping.naver").length > 1) {
                                    product_urls.put(product_url, product_url);
                                }
                            } catch (NoSuchMethodError er) {
                                continue;
                            }
                        }

                        for(String key : product_urls.keySet()){
                            savePriceComparisonUrl(key, driver);
                        }


                    }catch(NoSuchMethodError e){
                        System.out.println("해당분야는 최저가가 없다");
                    }
                    try {
                        try {
                            driver.get(current_list_url);
                            for(int pa =0; pa < 10; pa++){
                                String pagenum_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_btn_page__FuJaU ')[" + pa + "].className");
                                if(pagenum_str.split("activ").length > 1){
                                    pagenum = (pa +1);
                                    break;
                                }
                            }

                            if (pagenum == 51) {
                                break;
                            }

                        } catch (NoSuchMethodError er) {
                            break;
                        }

                        Long next_text_check = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_next__1ITTf').length");
                        if (next_text_check == 1) {
                            ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('pagination_next__1ITTf')[0].click()");
                            Thread.sleep(3000);
                            current_list_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                        } else {
                            break;
                        }

                    } catch (NoSuchMethodError er) {
                        break;
                    }

                    driver.get(current_list_url);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 구글 이미지 검색하여 가격 찾기 (Deprecated)
     *
     * @param part 시작지점
     * @param total 전체분할개수
     *
     */
    public void findPriceAndGooglelist(int part, int total){

        Conf mv = new Conf();
        driver = DriverControl.getGeneralDriver();
        ArrayList<HashMap<String, String>> cc_list = null;
        try {
            Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
            cc_list = (ArrayList<HashMap<String, String>>) mysql.selectCategoryComparsionList(conn, "select * from category_comparison_list where lowest_price is null or insert_time <= DATE_ADD(now(), INTERVAL -15 DAY);");
            conn.close();
        }catch (SQLException SC){
            SC.printStackTrace();
        }
        int slice_counts = Math.round(cc_list.size() / total);
        int end_point = slice_counts * part;
        int starting_point = end_point- slice_counts;

        if(cc_list.size()-end_point < slice_counts) {
            end_point = cc_list.size();
        }

        for(int i=starting_point; i < end_point; i++){

            try {
                HashMap hs = cc_list.get(i);

                if( hs.get("service_id") == "null"){
                    continue;
                }

                String comp_url = "https://search.shopping.naver.com/detail/detail.nhn?nv_mid=" + hs.get("service_id");

                driver.get(comp_url);
                int lowest_price = 0;
                int reviews = 0;

                try {
                    String lowest_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('low_price')[0].children[1].textContent");
                    lowest_price = Format.parse(lowest_price_str).intValue();
                }catch(ParseException er2){
                    String lowest_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('low_price')[0].children[2].textContent");
                    lowest_price = Format.parse(lowest_price_str).intValue();

                }

                try {
                    String reviews_str = "";
                    Long tabs = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab').length");
                    for(int t =0; t < tabs; t++){
                        String isReview = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab')[" + t +"].getAttribute('data-tab-name')");
                        if(isReview.equals("review")){
                            reviews_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab')[" + t+"].children[1].textContent");
                            reviews = Format.parse(reviews_str).intValue();
                        }
                    }



                }catch(NoSuchMethodError er){

                }


                String image_src = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('viewImage').src");
                driver.get("https://www.google.co.kr/imghp");
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('BwoPOe')[0].click()");
                ((JavascriptExecutor) driver).executeScript("document.getElementById('qbui').value='" + image_src + "'");
                Thread.sleep(1000);
                ((JavascriptExecutor) driver).executeScript("document.getElementById('qbbtc').children[0].click()");
                Thread.sleep(500);


                String google_image_yn = "n";
                String china_url = "";
                try {
                    String yn_check = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('not-first')[0].children[0].textContent");
                    if (yn_check.equals("일치하는 이미지를 포함하는 페이지")) {
                        google_image_yn = "y";
                            Long list_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('iUh30').length");
                            for (int g = 0; g < list_leng; g++) {
                                String url = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('iUh30')[" + g + "].textContent");
                                if (url.split("taobao").length > 1 || url.split("1688").length > 1) {
                                    china_url = url;
                                }
                            }

                    }
                }catch(NoSuchMethodError er){

                }

                String google_image_search = (String)((JavascriptExecutor) driver).executeScript("return location.href");

                try {
                    Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
                    mysql.updateCategoryComparisonList(conn, "update category_comparison_list set lowest_price=?, reviews=?,google_image_yn=?,google_image_search=?, main_image=?, china_url=? where service_id=?", lowest_price, reviews, google_image_yn, google_image_search, image_src, china_url, (String) hs.get("service_id"));
                    conn.close();
                }catch(SQLException SC){
                    SC.printStackTrace();
                }

                System.out.println( hs.get("service_id"));
            }catch(NoSuchMethodError er){
                continue;
            }catch(Exception er2){
                continue;
            }



        }


    }
    /**
     * 카테고리매칭(가격비교) 상품들을 DB에 저장한다 단, 특정된 카테고리에 한하여
     *
     * @param part 시작지점
     * @param total 전체분할개수
     * @param big_cat 특정카테고리(대분류)
     *
     */
    public void gotodbByCatName(int part, int total, String big_cat) {
        try {

            String category = "";
            int cnt = 1;
            int break_point =11;
            int lowest_price = 0;
            int reviews = 0;

            Conf mv = new Conf();
            Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
            ArrayList<String> ids = new ArrayList<>();
            driver2 = DriverControl.getGeneralDriver();
            focusCategorySelectMenuPopup();
            Thread.sleep(10000);

                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('placeholdersjs')[0].value ='" + big_cat + "'");
                Thread.sleep(2000);

                try {
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_srchBtn_search_commonSearcher').click()");
                    Thread.sleep(3000);

                }catch(NoSuchMethodError e){


                    DriverControl.quitDriver(driver);
                    driver = DriverControl.getGeneralDriver();
                    driver.get("");
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('storefarmlogin').click()");
                    final String mainWindowHandle2 = driver.getWindowHandle();
                    DriverControl.popupFocus(driver, null);
                    WebElement id = driver.findElementsByTagName("input").get(0);
                    WebElement pass = driver.findElementsByTagName("input").get(1);
                    id.sendKeys(Conf.NAVER_ID);
                    pass.sendKeys(Conf.NAVER_PASSWORD);//https://adcenter.shopping.naver.com/member/login/form.nhn?targetUrl=/&mode=logout
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByTagName('button')[0].click()");
                    Thread.sleep(5000);
                    driver.switchTo().window(mainWindowHandle2);
                    driver.get("https://adcenter.shopping.naver.com/product/manage/service/list.nhn?status=CATEGORY_MATCHED&catParam=50000008-1-true");
                    Thread.sleep(10000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn2')[5].click()");
                    Thread.sleep(5000);
                    DriverControl.popupFocus(driver,"adcenter.shopping.naver.com/product/model/predict/");
                    Thread.sleep(3000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_popupBtn_search_modelSrchPop_searcher').click()");
                    DriverControl.popupFocus(driver,null);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('placeholdersjs')[0].value ='" + big_cat + "'");
                    Thread.sleep(2000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_srchBtn_search_commonSearcher').click()");
                    Thread.sleep(3000);

                }
                String category_name = "";
                String currentHandle = "";
                for (String activeHandle : driver.getWindowHandles()) {
                    currentHandle = activeHandle;
                }
                driver.switchTo().window(currentHandle);

                try {
                    category_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('_showRslt_search_modelSrchPop_searcher').textContent");
                    category_name = category_name.split("\\(")[0];
                }catch(NoSuchMethodError er){
                    DriverControl.alertClick(driver);
                }catch(NoClassDefFoundError er2){
                    driver.switchTo().alert().dismiss();
                }


                looper:
                while (true) {

                    try {
                        Long product_list_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementById('reqModelList').children[0].children[2].children[0].children[2].children.length");

                        for (int i = 0; i < product_list_leng; i++) {
                            String image_src = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('reqModelList').children[0].children[2].children[0].children[2].children[" + i + "].children[5].children[0].src");
                            String service_now = "Y";
                            String service_name = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.goods')[" + i + "].textContent");
                            String service_id = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.cmp_service_id')[" + i + "].textContent");
                            String manu_brand = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.brand_name')[" + i + "].innerHTML");
                            String service_manufacturer = "";
                            String service_brand = "";
                            if(!manu_brand.equals("<br>")) {
                                String[] manu_brands = manu_brand.split("<br>");
                                service_manufacturer = manu_brands[0];
                                try {
                                    service_brand = manu_brands[1];
                                }catch(ArrayIndexOutOfBoundsException e){
                                    System.out.println(e.getMessage());

                                }

                            }
                            driver2.get("https://search.shopping.naver.com/detail/detail.nhn?nvMid=" + service_id);
                            DriverControl.tooManyRequest(driver2);
                            Long product_list_lengs = (Long) ((JavascriptExecutor) driver2).executeScript("return document.querySelectorAll('.mall a').length");
                            try {
                                String lowest_price_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('lowestPrice_num__3AlQ-')[0].textContent");
                                lowest_price = Format.parse(lowest_price_str).intValue();
                            } catch (ParseException er2) {
                                String lowest_price_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('low_price')[0].children[2].textContent");
                                lowest_price = Format.parse(lowest_price_str).intValue();
                            }
                            try {
                                String reviews_str = "";
                                Long tabs = (Long) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab').length");
                                for (int t = 0; t < tabs; t++) {
                                    String isReview = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].getAttribute('data-tab-name')");
                                    if (isReview.equals("review")) {
                                        reviews_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].children[1].textContent");
                                        reviews = Format.parse(reviews_str).intValue();
                                    }
                                }
                            } catch (NoSuchMethodError er) {

                            }
                            Connection conn_temps = mysql.initConnect(mv.NAVER_DB_IP_SERVICE);
                            mysql.insertCategoryMatching(conn_temps, "insert into category_comparison_list(service_image,service_yn,service_name, service_id,service_manufacturer,service_brand,category, category_name, lowest_price, reviews) values(?,?,?,?,?,?,?,?,?,?)", image_src, service_now, service_name, service_id, service_manufacturer, service_brand, category,category_name, lowest_price, reviews);
                            conn_temps.close();

                            for(int tt =0; tt < product_list_lengs; tt++ ){

                                String product_list_url = (String) ((JavascriptExecutor) driver2).executeScript("return document.querySelectorAll('.mall a')[" + tt +"].href");
                                JSONObject res = dp.executeProcess(product_list_url, null, "false","not-quit");
                                if(res.get("valid_url").equals("valid")) {
                                    Connection conn2 = mysql.initConnect(new Conf().NAVER_DB_IP);
                                    mysql.updateDatalabPlusCateogryComparsion(conn2, "update datalab_plus set category_comparison=1 where product_no=?", (String) res.get("product_no"));
                                    conn2.close();
                                }
                            }


                        }

                        Thread.sleep(1000);
                        ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('paginate_regular')[0].children[" + cnt + "].click()");
                        cnt++;

                        if (cnt == break_point) {
                            cnt = 2;
                            break_point = 13;
                        }
                    } catch (NoSuchMethodError er) {
                        DriverControl.removeAllPages(driver);
                        DriverControl.removeAllPages(driver2);
                        focusCategorySelectMenuPopup();
                        break looper;
                    }
                }






        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    /**
     * 카테고리매칭(가격비교) 상품들을 DB에 저장한다
     *
     * @param part 시작지점
     * @param total 전체분할개수
     */
    public void saveCategoryMathcingList(int part, int total) {
        try {

            String category = "";

            int cnt = 1;
            int break_point =11;

            int lowest_price = 0;
            int reviews = 0;

            Conf mv = new Conf();
            Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
            ArrayList<String> ids = new ArrayList<>();

            try {
                ArrayList<HashMap<String,String>>  category_ids = (ArrayList<HashMap<String,String>> ) mysql.selectCategoryIds(conn, "select * from category_id");
                for(int k=0; k < category_ids.size(); k++){
                    HashMap<String,String> hs = category_ids.get(k);
                    String id = hs.get("category_code");
                    ids.add(id);

                }
            }catch(Exception er){

            }
            int slice_counts = Math.round(ids.size() / total);
            int end_point = slice_counts * part;
            int starting_point = end_point- slice_counts;

            if(ids.size()-end_point < slice_counts) {
                end_point = ids.size();
            }
            focusCategorySelectMenuPopup();

            for(int j =starting_point; j < end_point; j++) {

                String cat_id = ids.get(j);
                category = cat_id;
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('placeholdersjs')[0].value ='" + cat_id + "'");
                Thread.sleep(2000);

                try {
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_srchBtn_search_commonSearcher').click()");
                    Thread.sleep(3000);
                }catch(NoSuchMethodError e){

                    DriverControl.quitDriver(driver);
                    driver = DriverControl.getGeneralDriver();
                    driver.get("");
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('storefarmlogin').click()");
                    final String mainWindowHandle2 = driver.getWindowHandle();
                    DriverControl.popupFocus(driver, null);
                    WebElement id2 = driver.findElementsByTagName("input").get(0);
                    WebElement pass2 = driver.findElementsByTagName("input").get(1);
                    id2.sendKeys(mv.NAVER_ID);
                    pass2.sendKeys(mv.NAVER_PASSWORD);https://adcenter.shopping.naver.com/member/login/form.nhn?targetUrl=/&mode=logout
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByTagName('button')[0].click()");
                    Thread.sleep(5000);
                    driver.switchTo().window(mainWindowHandle2);
                    driver.get("https://adcenter.shopping.naver.com/product/manage/service/list.nhn?status=CATEGORY_MATCHED&catParam=50000008-1-true");
                    Thread.sleep(10000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn2')[5].click()");
                    Thread.sleep(5000);
                    DriverControl.popupFocus(driver,"adcenter.shopping.naver.com/product/model/predict/");
                    Thread.sleep(3000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_popupBtn_search_modelSrchPop_searcher').click()");
                    DriverControl.popupFocus(driver,null);
                    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('placeholdersjs')[0].value ='" + cat_id + "'");
                    Thread.sleep(2000);
                    ((JavascriptExecutor) driver).executeScript("document.getElementById('_srchBtn_search_commonSearcher').click()");
                    Thread.sleep(3000);

                }
                String category_name = "";
                String currentHandle = "";
                for (String activeHandle : driver.getWindowHandles()) {
                    currentHandle = activeHandle;
                }
                driver.switchTo().window(currentHandle);
                try {
                    category_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('_showRslt_search_modelSrchPop_searcher').textContent");
                    category_name = category_name.split("\\(")[0];
                }catch(NoSuchMethodError er){
                    DriverControl.alertClick(driver);
                }catch(NoClassDefFoundError er2){
                    driver.switchTo().alert().dismiss();
                    continue;
                }


                looper:
                while (true) {

                    try {
                        Long product_list_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementById('reqModelList').children[0].children[2].children[0].children[2].children.length");

                        for (int i = 0; i < product_list_leng; i++) {
                            String image_src = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('reqModelList').children[0].children[2].children[0].children[2].children[" + i + "].children[5].children[0].src");
                            String service_now = "Y";
                            String service_name = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.goods')[" + i + "].textContent");
                            String service_id = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.cmp_service_id')[" + i + "].textContent");
                            String manu_brand = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.brand_name')[" + i + "].innerHTML");
                            String service_manufacturer = "";
                            String service_brand = "";
                            if(!manu_brand.equals("<br>")) {
                                String[] manu_brands = manu_brand.split("<br>");
                                service_manufacturer = manu_brands[0];
                                try {
                                    service_brand = manu_brands[1];
                                }catch(ArrayIndexOutOfBoundsException e){
                                    System.out.println(e.getMessage());

                                }

                            }
                            driver2.get("https://search.shopping.naver.com/detail/detail.nhn?nvMid=" + service_id);
                            Long product_list_lengs = (Long) ((JavascriptExecutor) driver2).executeScript("return document.querySelectorAll('.mall a').length");
                            try {
                                String lowest_price_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('low_price')[0].children[1].textContent");
                                lowest_price = Format.parse(lowest_price_str).intValue();
                            } catch (ParseException er2) {
                                String lowest_price_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('low_price')[0].children[2].textContent");
                                lowest_price = Format.parse(lowest_price_str).intValue();

                            }
                            try {
                                String reviews_str = "";
                                Long tabs = (Long) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab').length");
                                for (int t = 0; t < tabs; t++) {
                                    String isReview = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].getAttribute('data-tab-name')");
                                    if (isReview.equals("review")) {
                                        reviews_str = (String) ((JavascriptExecutor) driver2).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].children[1].textContent");
                                        reviews = Format.parse(reviews_str).intValue();
                                    }
                                }
                            } catch (NoSuchMethodError er) {

                            }


                            Connection conn_temps = mysql.initConnect(mv.NAVER_DB_IP);
                            mysql.insertCategoryMatching(conn_temps, "insert into category_comparison_list(service_image,service_yn,service_name, service_id,service_manufacturer,service_brand,category, category_name, lowest_price, reviews) values(?,?,?,?,?,?,?,?,?,?)", image_src, service_now, service_name, service_id, service_manufacturer, service_brand, category,category_name, lowest_price, reviews);
                            conn_temps.close();

                            for(int tt =0; tt < product_list_lengs; tt++ ){

                                String product_list_url = (String) ((JavascriptExecutor) driver2).executeScript("return document.querySelectorAll('.mall a')[" + tt +"].href");
                                JSONObject res = dp.executeProcess(product_list_url, null, "false","not-quit");
                                if(res.get("valid_url").equals("valid")) {
                                    Connection conn2 = mysql.initConnect(new Conf().NAVER_DB_IP);
                                    mysql.updateDatalabPlusCateogryComparsion(conn2, "update datalab_plus set category_comparison=1 where product_no=?", (String) res.get("product_no"));
                                    conn2.close();
                                }
                            }


                        }

                        Thread.sleep(1000);
                        ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('paginate_regular')[0].children[" + cnt + "].click()");
                        cnt++;

                        if (cnt == break_point) {
                            cnt = 2;
                            break_point = 13;
                        }
                    } catch (NoSuchMethodError er) {
                        DriverControl.removeAllPages(driver);
                        DriverControl.removeAllPages(driver2);
                        focusCategorySelectMenuPopup();
                        break looper;
                    }
                }
            }





        } catch (Exception er) {
            er.printStackTrace();
        }
    }

    /**
     * 카테고리 선택 메뉴(팝업) 포커스
     *
     */
    private void focusCategorySelectMenuPopup(){

        try {
            driver = DriverControl.getGeneralDriver();
            driver.get("https://adcenter.shopping.naver.com/member/login/form.nhn?targetUrl=/&mode=logout");
            ((JavascriptExecutor) driver).executeScript("document.getElementById('storefarmlogin').click()");
            final String mainWindowHandle = driver.getWindowHandle();
            DriverControl.popupFocus(driver, "sell.smartstore.naver.com/#/loginSpAdCenter");
            for(int i = 60; i > 0; i--) {
                System.out.println(i + "초 남았습니다");
                Thread.sleep(1*1000);
            }
            Thread.sleep(2000);
            driver.get("https://adcenter.shopping.naver.com/product/manage/service/list.nhn?status=CATEGORY_MATCHED");
            driver.switchTo().window(driver.getWindowHandle());
            Thread.sleep(2000);
            try {
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn2')[5].click()");
            }catch(NoSuchMethodError er){
                driver.get("https://adcenter.shopping.naver.com/product/manage/service/list.nhn?status=CATEGORY_MATCHED");
                Thread.sleep(10000);
                ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn2')[5].click()");
            }
            Thread.sleep(2000);
            DriverControl.popupFocus(driver, "adcenter.shopping.naver.com/product/model/predict/");
            Thread.sleep(2000);
            ((JavascriptExecutor) driver).executeScript("document.getElementById('_popupBtn_search_modelSrchPop_searcher').click()");
            Thread.sleep(2000);
            DriverControl.popupFocus(driver, null);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    private void savePriceComparisonUrl(String url, ChromeDriver driver){

        int lowest_price = 0;
        int reviews = 0;

        String service_now = "y";
        String service_name = "";
        String service_id = "";
        String service_manufacturer= "";
        String service_brand ="";
        String category = "";
        String category_name = "";
        driver.get(url);
        DriverControl.tooManyRequest(driver);
        String comp_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");

        try {
            if(comp_url.split("/search.shopping.naver.com/detail/lite.nhn").length > 1) {

                try {
                    service_id = (String) ((JavascriptExecutor) driver).executeScript("return location.href.split('nvMid=')[1].split('&')[0]");
                } catch (Exception er2) {
                    return;
                }

                try {
                    service_name = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.h_area h2')[0].textContent.trim()");
                } catch (Exception er2) { }

                try {
                    Long desc_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.info_inner span').length");
                    for(int d =0; d < desc_leng; d++) {
                        String desc_str = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.info_inner span')[" + d + "].textContent");
                        if(desc_str.split("브랜드").length > 1) {
                            service_brand = desc_str.split("브랜드")[1];
                        }
                        if(desc_str.split("제조사").length > 1) {
                            service_manufacturer = desc_str.split("제조사")[1];
                        }
                    }
                } catch (Exception er2) { }


                try {
                    category = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.search_breadcrumb span a')[2].href.split('cat_id=')[1]");
                } catch (Exception er2) { }

                try {
                    category_name = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.search_breadcrumb')[0].textContent.trim().replaceAll(\" \",\"\").replaceAll(\"\\n\",\"\")");
                } catch (Exception er2) { }


                try {
                    String lowest_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('low_price')[0].children[1].textContent");
                    lowest_price = Format.parse(lowest_price_str).intValue();
                } catch (ParseException er2) {
                    String lowest_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('low_price')[0].children[2].textContent");
                    lowest_price = Format.parse(lowest_price_str).intValue();

                }

                try {
                    String reviews_str = "";
                    Long tabs = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab').length");
                    for (int t = 0; t < tabs; t++) {
                        String isReview = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].getAttribute('data-tab-name')");
                        if (isReview.equals("review")) {
                            reviews_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_contentTab')[" + t + "].children[1].textContent");
                            reviews = Format.parse(reviews_str).intValue();
                        }
                    }


                } catch (NoSuchMethodError er) {

                }

                String google_image_yn = "no";
                String google_image_search = "no";
                String image_src = "no";
                String china_url = "no";
                Connection conn = mysql.initConnect(new Conf().NAVER_DB_IP);
                mysql.insertCategoryMatching(conn, "insert into category_comparison_list(service_image,service_yn,service_name, service_id,service_manufacturer,service_brand, category, category_name, lowest_price, reviews) values(?,?,?,?,?,?,?,?,?,?)", image_src, service_now, service_name, service_id, service_manufacturer, service_brand, category, category_name, lowest_price, reviews);
                mysql.closeConnect();
                Long product_list_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall a').length");
                for(int tt =0; tt < product_list_leng; tt++ ){
                    String product_list_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall a')[" + tt +"].href");
                    JSONObject res = dp.executeProcess(product_list_url, null, "false","not-quit");
                    if(res.get("valid_url").equals("valid")) {
                        Connection conn2 = mysql.initConnect(new Conf().NAVER_DB_IP);
                        mysql.updateDatalabPlusCateogryComparsion(conn2, "update datalab_plus set category_comparison=1 where product_no=?", (String) res.get("product_no"));
                        conn2.close();
                    }
                }


                System.out.println(comp_url);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
