package Naver;

import Connection.MySQLConnector;
import Util.DriverControl;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class NaverShoppingScrapper {

    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
    MySQLConnector mysql = new MySQLConnector();
    Connection conn = null;
    ChromeDriver driver;
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    String current_list_url = "";

    public NaverShoppingScrapper(String DB_IP) {
        conn = mysql.initConnect(DB_IP);
    }

    public void initDriver(){
        driver= DriverControl.getGeneralDriver();
        driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
    }


    public void scrapDetail(String part_number, String total_numbers){

        try {
            driver = DriverControl.getGeneralDriver();
            driver.get("https://shopping.naver.com");
            Thread.sleep(5000);
            ArrayList<String[]> categories = new ArrayList();
            Thread.sleep(1000);
            Long help_menu_length = (Long) ((JavascriptExecutor) driver).executeScript("return $('ul.co_category_list:Eq(0) > li').length");
            ArrayList<String> help_menu_urls = new ArrayList<>();
            for(int i=0; i < help_menu_length; i++){
                String help_menu_url= (String) ((JavascriptExecutor) driver).executeScript("return $('ul.co_category_list:Eq(0) > li').eq(" +i + ").children('.co_ly_inner').children('.co_help').children('a:Eq(0)').attr('href')");
                help_menu_urls.add(help_menu_url);
            }

            for(int j=0; j < help_menu_urls.size(); j++){
                driver.get(help_menu_urls.get(j));
                String big_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_tit').text()");

                Long category_col = (Long) ((JavascriptExecutor) driver).executeScript("return $('.category_col').length");
                for(int k=0; k < category_col; k++){
                    String middle_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k+ ").children('.category_cell').children('h3').children('a').children('strong').text()");
                    Long category_list = (Long) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').length");
                    for(int p=0; p < category_list; p++){
                        String small_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('a').text()");
                        String small_cat_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('a').attr('href')");

                        String product_cat_name = null;
                        String product_cat_url = null;
                        try {
                            product_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('ul').children('li').children('a').text()");
                            product_cat_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('ul').children('li').children('a').attr('href')");

                            if(!product_cat_name.equals("")){
                                Long product_cat_names_leng = (Long) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('ul').children('li').children('a').length");
                                for(int tt= 0; tt < product_cat_names_leng; tt++){
                                    String pr_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('ul').children('li').children('a').eq(" + tt + ").text()");
                                    String pr_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('ul').children('li').children('a').eq(" + tt + ").attr('href')");
                                    String[] strs = new String[5];
                                    strs[0] = big_cat_name;
                                    strs[1] = middle_cat_name;
                                    strs[2] = small_cat_name;
                                    strs[3] = pr_name;
                                    strs[4] = "https://search.shopping.naver.com" +pr_url;
                                    categories.add(strs);
                                }
                            }

                            if(product_cat_name.equals("")) {
                                product_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('.ly_category_list').children('ul').children('li').children('a').text()");
                                product_cat_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('.ly_category_list').children('ul').children('li').children('a').attr('href')");

                                if(!product_cat_name.equals("")) {
                                Long product_cat_names_leng = (Long) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k +").children('.category_cell').children('.category_list').children('li').eq("+ p + ").children('.ly_category_list').children('ul').children('li').children('a').length");
                                for(int tt= 0; tt < product_cat_names_leng; tt++) {
                                    String pr_name = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k + ").children('.category_cell').children('.category_list').children('li').eq(" + p + ").children('.ly_category_list').children('ul').children('li').children('a').eq(" + tt + ").text()");
                                    String pr_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.category_col').eq(" + k + ").children('.category_cell').children('.category_list').children('li').eq(" + p + ").children('.ly_category_list').children('ul').children('li').children('a').eq(" + tt + ").attr('href')");
                                    String[] strs = new String[5];
                                    strs[0] = big_cat_name;
                                    strs[1] = middle_cat_name;
                                    strs[2] = small_cat_name;
                                    strs[3] = pr_name;
                                    strs[4] = "https://search.shopping.naver.com" +pr_url;
                                    categories.add(strs);
                                }
                                }

                            }

                        }catch(Exception er){

                        } catch(NoSuchMethodError ERER){

                        }

                        if(product_cat_name == null || product_cat_url == null){
                            String[] strs = new String[5];
                            strs[0] = big_cat_name;
                            strs[1] = middle_cat_name;
                            strs[2] = small_cat_name;
                            strs[3] = null;
                            strs[4] = "https://search.shopping.naver.com" + small_cat_url;
                            categories.add(strs);
                        } else{



                        }


                    }
                }


            }

            System.out.println("카테고리 수집 완료!");
            Thread.sleep(1000);
            int slice_counts = Math.round(categories.size() / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point- slice_counts;


            if(categories.size()-end_point < slice_counts) {
                end_point = categories.size();
            }

            for(int p=starting_point; p < end_point; p++) {

                String cat_product;
                String cat_big;
                String cat_middle;
                String cat_small;

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("[" + sdf.format(cal.getTime()) + "] " + p + "/" + end_point);

                try {
                    String list_url = categories.get(p)[4];

                    cat_big = categories.get(p)[0];
                    cat_middle = categories.get(p)[1];
                    cat_small = categories.get(p)[2];
                    cat_product =categories.get(p)[3];
                    driver.get(list_url);
                    current_list_url = list_url;
                }catch(NoSuchMethodError er){
                    System.out.println(er.getMessage());
                    continue;
                }
                    int dup_count = 0;
                    Long list_number;
                    while (true) {
                        try {
                            list_number = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall_txt').length");
                        } catch (NoSuchMethodError er) {
                            break;
                        }
                        for (int pp = 0; pp < list_number - 1; pp++) {
                            try {
                                String pagenum = (String) ((JavascriptExecutor) driver).executeScript("return $('#_result_paging').children('strong').text().split('현재 페이지')[1].trim()");
                                if (Integer.parseInt(pagenum) == 11) {
                                    break;
                                }

                            }catch (NoSuchMethodError er){
                                break;
                            }
                            try {
                                String mall_url = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall_txt')[" + pp + "].querySelector('a').href");
                                if (mall_url.split("smartstore.naver.com").length == 1) {
                                    continue;
                                }
                            } catch (NoSuchMethodError er) {
                                try {
                                    ((JavascriptExecutor) driver).executeScript("window.history.go(-1)");
                                } catch (UnhandledAlertException f) {
                                    try {
                                        Alert alert = driver.switchTo().alert();
                                        String alertText = alert.getText();
                                        System.out.println("Alert data: " + alertText);
                                        alert.accept();
                                        ((JavascriptExecutor) driver).executeScript("window.history.go(-1)");
                                    } catch (NoAlertPresentException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchMethodError erer) {

                                    }
                                }


                                try {
                                    Alert alert = driver.switchTo().alert();
                                    String alertText = alert.getText();
                                    System.out.println("Alert data: " + alertText);
                                    alert.accept();
                                    ((JavascriptExecutor) driver).executeScript("window.history.go(-1)");
                                } catch (NoAlertPresentException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodError erer) {

                                }

                                //}
                                continue;
                            }


                            String product_url = "";
                            try {
                                product_url = (String) ((JavascriptExecutor) driver).executeScript("return $('.goods_list > li').eq(" + pp + ").find('a.tit').attr('href')");
                            } catch (NoSuchMethodError er) {
                                continue;
                            }
                            String register_date = "";
                            try {
                                //$('.date').eq(0).text()
                                register_date = (String) ((JavascriptExecutor) driver).executeScript("return $('.goods_list > li').eq(" + pp + ").children('.info').children('.etc').children('.date').text()");
                            }catch(NoSuchMethodError er3){

                            }

                            if(register_date != null){
                                register_date += "01";
                                register_date = register_date.replaceAll("\\.","-");
                                register_date = register_date.split("등록일")[1];
                                register_date = register_date.trim();
                            }

                            String res = saveProductData(product_url, cat_big, cat_middle, cat_small, cat_product, register_date);

                            if (res.equals("duplicate")) {
                                dup_count++;
                            }
                            if (res.equals("noduplicate")) {
                                dup_count = 0;
                            }
                            if (!res.equals("quit")) {
                                try {
                                    Thread.sleep(1000);
                                    //((JavascriptExecutor) driver).executeScript("window.history.go(-1)");
                                    driver.get(current_list_url);
                                } catch (NoSuchMethodError er) {
                                    er.printStackTrace();
                                }
                            }

                        }
                        try {
                            ((JavascriptExecutor) driver).executeScript("$('#_result_paging').children().last().click()");
                            Thread.sleep(3000);
                            current_list_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                        } catch (NoSuchMethodError er) {
                            break;
                        }
                        Thread.sleep(3000);


                    }

            }

            DriverControl.quitDriver(driver);




        }catch(Exception er){
            //er.printStackTrace();
            System.out.println(er.getMessage());
        }

    }


    public String saveProductData(String product_url, String cat_big, String cat_middle, String cat_small, String cat_product, String register_date){


        try {
            driver.get(product_url);
        }catch(NoSuchMethodError er){

            Alert alert = driver.switchTo().alert();
            alert.accept();
        }

        try {
            sleep(1000);
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (NoAlertPresentException e) {
            //e.printStackTrace();
        } catch(NoSuchMethodError er){

        } catch(Exception erer){

        }



        NaverProductDetailClass product = new NaverProductDetailClass<String,Integer,Double,Long,Timestamp>();

        Double star_avg = -1.0;
        int count_review = -1;
        int photo_review = -1;
        int count_like = -1;
        int count_qna = -1;
        int toktok_friends = -1;
        int store_zzim = -1;

        String store_name = null;
        String store_company = null;
        String store_email = null;
        String store_address = null;
        String store_phone = null;
        String store_url = null;
        Double store_star = null;
        String made_country = null;
        String halbu = null;

        int price = -1;
        int discount = -1;

        Double star5 = -1.0;
        Double star4 = -1.0;
        Double star3 = -1.0;
        Double star2 = -1.0;
        Double star1 = -1.0;

        String title = null;
        String product_number = null;
        try {
            try {
                product_number = (String) ((JavascriptExecutor) driver).executeScript("return  document.querySelectorAll('input[name$=\"productId\"]')[0].value");
            }catch(NoSuchMethodError er){
                return "nodata";
            }
            try {
                title = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('prd_name')[1].textContent.trim()");

            }catch(NoSuchMethodError erer){

                try {
                    sleep(1000);
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                    title = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('prd_name')[1].textContent.trim()");
                }catch (NoSuchMethodError ererer){

                }
                try {
                    driver.navigate().refresh();
                } catch(NoSuchMethodError ererererer){

                }
            }
            String title_sub = null;
            try {
                Long price_str_num = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale').length");
                if (price_str_num == 3) {
                    try {
                        String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale')[2].querySelectorAll('.thm')[0].textContent");
                        price = Format.parse(price_str).intValue();
                        discount = 0;
                        String original_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale')[1].querySelectorAll('.thm')[0].textContent");
                        int original_price = Format.parse(original_price_str).intValue();
                        double discounts = price * 1.0 / original_price * 1.0;
                        discount = (int) ((1 - discounts) * 100);

                    } catch (Exception noOriginalPrice) {

                    }
                }
                else if (price_str_num == 2) {
                    try {
                        String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale')[1].querySelectorAll('.thm')[0].textContent");
                        price = Format.parse(price_str).intValue();
                        discount = 0;
                        String original_price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale')[0].querySelectorAll('.thm')[0].textContent");
                        int original_price = Format.parse(original_price_str).intValue();
                        double discounts = price * 1.0 / original_price * 1.0;
                        discount = (int) ((1 - discounts) * 100);

                    } catch (Exception noOriginalPrice) {

                    }

                } else {
                    try {
                        String price_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sale')[0].querySelectorAll('.thm')[0].textContent");
                        price = Format.parse(price_str).intValue();
                        discount = 0;
                    } catch (NoSuchMethodError er) {
                        return  "nodata";
                    } catch(ParseException er){
                        return  "nodata";
                    }
                }
            }catch(NoSuchMethodError er){
                return  "nodata";
            }
            String info_flag = "";
            try {
                info_flag = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('benefit')[0].querySelectorAll('.first')[0].textContent.trim()");
            }catch(NoSuchMethodError er){

            }

            Long product_img_length = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_image_box').length");
            String product_img = "";
            for (int rr = 0; rr < product_img_length; rr++) {
                String img_url = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_image_box')[" + rr + "].querySelectorAll('img')[0].src");
                product_img += img_url;
                if (rr != (product_img_length - 1)) {
                    product_img += ",";
                }
            }
            try {
                Long star_str_exist = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sns_section')[0].querySelectorAll('.wrap_label').length");
                if(star_str_exist > 1) {
                    String star_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sns_section')[0].querySelectorAll('.wrap_label')[1].querySelectorAll('strong')[0].textContent");
                    star_avg = Double.parseDouble(star_str);
                }

            } catch (NoSuchMethodError star_err) {
                //System.out.println("star err : " + star_err.getMessage());
            }
            try {
                Long existStars = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction').length");
                String star_5 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction')[0].querySelectorAll('.value')[0].textContent.split(\"명\")[0]");
                String star_4 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction')[0].querySelectorAll('.value')[1].textContent.split(\"명\")[0]");
                String star_3 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction')[0].querySelectorAll('.value')[2].textContent.split(\"명\")[0]");
                String star_2 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction')[0].querySelectorAll('.value')[3].textContent.split(\"명\")[0]");
                String star_1 = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('graph_satisfaction')[0].querySelectorAll('.value')[4].textContent.split(\"명\")[0]");

                if(existStars > 0){
                    try {
                        star5 = Double.parseDouble(star_5);
                        star4 = Double.parseDouble(star_4);
                        star3 = Double.parseDouble(star_3);
                        star2 = Double.parseDouble(star_2);
                        star1 = Double.parseDouble(star_1);
                        Double sum = star1 + star2 + star3 + star4 + star5;

                        star5 = (star5 / sum) * 100;
                        star4 = (star4 / sum) * 100;
                        star3 = (star3 / sum) * 100;
                        star2 = (star2 / sum) * 100;
                        star1 = (star1 / sum) * 100;

                    }catch (NumberFormatException er){

                        try {
                            star5 = Format.parse(star_5).doubleValue();
                            star4 = Format.parse(star_4).doubleValue();
                            star3 = Format.parse(star_3).doubleValue();
                            star2 = Format.parse(star_2).doubleValue();
                            star1 = Format.parse(star_1).doubleValue();

                            Double sum = star1 + star2 + star3 + star4 + star5;

                            star5 = (star5 / sum) * 100;
                            star4 = (star4 / sum) * 100;
                            star3 = (star3 / sum) * 100;
                            star2 = (star2 / sum) * 100;
                            star1 = (star1 / sum) * 100;
                        } catch(ParseException er2){
                            star5 = -1.0;
                            star4 = -1.0;
                            star3 = -1.0;
                            star2 = -1.0;
                            star1 = -1.0;
                        }
                    }


                }

            } catch (NoSuchMethodError unknown_err) {

                star5 = -1.0;
                star4 = -1.0;
                star3 = -1.0;
                star2 = -1.0;
                star1 = -1.0;
            }
            try {
                String review_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('sns_section')[0].querySelectorAll('a')[0].querySelectorAll('strong')[0].textContent");
                count_review = Format.parse(review_str).intValue();
            } catch (NoSuchMethodError err) {
                //err.printStackTrace();
                count_review =-1;
            } catch (ParseException er){
                count_review = -1;
            }
            try {
                Thread.sleep(1000);
                String photo_review_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('fc_point thm')[2].textContent.split(\"(\")[1].split(\")\")[0].split(\"건\")[0]");
                photo_review = Format.parse(photo_review_str).intValue();
            } catch (NoSuchMethodError err) {
                //err.printStackTrace();
                photo_review = 0;
            }  catch (ParseException er){
                photo_review = 0;
            } catch(InterruptedException er2){

            }
            try {
                String toktok_friends_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_talk_friend_count')[0].textContent");
                toktok_friends = Format.parse(toktok_friends_str).intValue();
            } catch (NoSuchMethodError err) {
                //err.printStackTrace();
                toktok_friends = 0;
            } catch (ParseException er){
                toktok_friends = 0;
            }
            try {
                String store_zzim_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_subscribe_count')[0].textContent");
                store_zzim = Format.parse(store_zzim_str).intValue();
            } catch (NoSuchMethodError err) {
                //err.printStackTrace();
                store_zzim = 0;
            } catch (ParseException er){
                store_zzim = 0;
            }
            try {
                store_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('N=a:lid.home')[0].textContent");
                store_company = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('seller')[0].textContent.split(\"상호명 :\")[1].split(\"|\")[0].trim()");
                store_email = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('seller')[0].textContent.split(\"메일 :\")[1].split(\")\")[0].trim()");
                store_address = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('seller')[0].textContent.split(\"사업장소재지:\")[1].trim()");
                store_phone = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('seller')[0].textContent.split(\"대표전화 :\")[1].split(\",\")[0].trim()");

            } catch (NoSuchMethodError store_err) {
                //store_err.printStackTrace();
            }


            made_country = "미수집";
            halbu = "미수집";
            store_star = -1.0;


            try {
                Long th_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('th').length");
                for(int th=0; th < th_leng; th++){
                    String th_string = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('th')[" + th + "].textContent");
                    if(th_string.equals("원산지")){
                        made_country = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('th')[" + th + "].nextElementSibling.textContent");
                        break;
                    }
                }
            } catch (NoSuchMethodError err) {
                //err.printStackTrace();
                toktok_friends = 0;
            }


            try {
                product_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            }catch(NoSuchMethodError pageout){
                try {
                    Alert alert = driver.switchTo().alert();
                    String alertText = alert.getText();
                    System.out.println("Alert data: " + alertText);
                    alert.accept();
                    ((JavascriptExecutor) driver).executeScript("window.history.go(-1)");
                } catch (NoAlertPresentException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodError erer) {

                }

                try {
                    driver.navigate().refresh();
                }catch(Exception erer2){

                }
            }

            product_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");

            product.setProductInfo2(product_number, product_url, product_img, cat_big, cat_middle, cat_small, cat_product, title, title_sub, info_flag, discount, price, register_date);
            product.setSellerAndDetailInfo(star_avg, star5, star4, star3, star2, star1, count_review, photo_review, count_like, count_qna, store_zzim, toktok_friends, store_name, store_company, store_email, store_address, store_phone, store_url, store_star, made_country, halbu);
            String cat_full = cat_big + ">" + cat_middle + ">" + cat_small;
            if(cat_product != null && cat_product != "") {
                cat_full += ">";
                cat_full += cat_small;
            }
            product.setCatFull(cat_full);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Month.Day.Year
            Date d = formatter.parse(register_date);
            long register_timestamp = d.getTime();
            product.setTimestamp(System.currentTimeMillis() / 1000, register_timestamp / 1000);

            String insert_query = "insert into naver(sk_id,product_no,product_url,product_img, cat_big,cat_middle,cat_small,cat_product,title,title_sub,star_avg,star_5,star_4,star_3,star_2,star_1,count_review, count_photoreview, count_like,count_qna,store_name,store_company,store_email,store_address,store_phone,store_url,store_star,made_country,halbu,info_flag,discount,price, store_zzim, toktok_friends, register_date, cat_full, insert_timestamp, register_timestamp) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            Boolean res = mysql.insertProductInfoOfNaver(conn, insert_query, product);
            System.out.println("수집완료:" + title);

            String r = "noduplicate";
            if(res == false){
                r = "duplicate";
            }

            return r;


        }catch(Exception erer){
            System.out.println("판매중 상품 아님");
            DriverControl.quitDriver(driver);
            return  "quit";
        }

    }


}
