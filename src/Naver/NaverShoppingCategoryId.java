package Naver;

import Connection.MySQLConnector;
import Util.DriverControl;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NaverShoppingCategoryId {

    MySQLConnector mysql = new MySQLConnector();
    Connection conn = null;
    ChromeDriver driver;

    public NaverShoppingCategoryId(String DB_IP) {
        conn = mysql.initConnect(DB_IP);
    }

    public void initDriver(){
        driver= DriverControl.getGeneralDriver();
        driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * 네이버 카테고리 아이디 수집 후 저장 (Deprecated)
     *
     */
    public void detail() {

        String category_code = "";
        try {
            driver = DriverControl.getGeneralDriver();
            driver.get("https://shopping.naver.com");
            Thread.sleep(5000);
            ArrayList<String[]> categories = new ArrayList();

            Thread.sleep(5000);

            Long help_menu_length = (Long) ((JavascriptExecutor) driver).executeScript("return $('ul.co_category_list:Eq(0) > li').length");
            ArrayList<String> help_menu_urls = new ArrayList<>();
            for (int i = 0; i < help_menu_length; i++) {
                String help_menu_url = (String) ((JavascriptExecutor) driver).executeScript("return $('ul.co_category_list:Eq(0) > li').eq(" + i + ").children('.co_ly_inner').children('.co_help').children('a:Eq(0)').attr('href')");
                help_menu_urls.add(help_menu_url);
            }

            for (int j = 0; j < help_menu_urls.size(); j++) {
                driver.get(help_menu_urls.get(j));
                String cat_id = help_menu_urls.get(j).split("cat_id=")[1];
                String big_cat_name = "";
                try {
                    big_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[0].textContent");
                } catch (NoSuchMethodError er) {
                    Thread.sleep(1000 * 60);
                    continue;
                }

                Long category_col = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children.length");
                for (int k = 0; k < category_col; k++) {

                    try {
                        String middle_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[0].children[0].textContent");
                        Long category_list = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children.length");
                        for (int p = 0; p < category_list; p++) {
                            String small_cat_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children[" + p + "].children[0].textContent");
                            String small_cat_url = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children[" + p + "].children[0].href");

                            String product_cat_name = null;
                            String product_cat_url = null;
                            Long product_cat_numbers = 0L;
                            try {

                                product_cat_numbers = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children[" + p + "].children[1].children.length");

                                if (product_cat_numbers != 0) {
                                    for (int tt = 0; tt < product_cat_numbers; tt++) {
                                        String pr_name = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children[" + p + "].children[1].children[" + tt + "].textContent");
                                        String pr_url = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('header_header__26kBA')[0].nextElementSibling.children[1].children[" + k + "].children[0].children[1].children[" + p + "].children[1].children[" + tt + "].children[0].href");
                                        String[] strs = new String[5];
                                        strs[0] = big_cat_name;
                                        strs[1] = middle_cat_name;
                                        strs[2] = small_cat_name;
                                        strs[3] = pr_name;

                                        try {
                                            pr_url = pr_url.split("catId=")[1];
                                        } catch (Exception erer) {

                                        }
                                        mysql.insertCateogryId(conn, "insert into category_id(big_name,middle_name,small_name, product_name,category_code) values(?,?,?,?,?)", big_cat_name, middle_cat_name, small_cat_name, pr_name, pr_url);

                                    }
                                }


                            } catch (NoSuchMethodError ERER) {

                            }

                            if (product_cat_name == null || product_cat_url == null) {
                                String[] strs = new String[5];
                                strs[0] = big_cat_name;
                                strs[1] = middle_cat_name;
                                strs[2] = small_cat_name;
                                strs[3] = null;
                                try {
                                    String pr_url = small_cat_url.split("catId=")[1];
                                    mysql.insertCateogryId(conn, "insert into category_id(big_name,middle_name,small_name, product_name,category_code) values(?,?,?,?,?)", big_cat_name, middle_cat_name, small_cat_name, "", pr_url);

                                } catch (Exception er) {
                                    er.printStackTrace();
                                }
                            }

                        }
                        System.out.println("카테고리 수집 완료!");

                    } catch (NoSuchMethodError er) {

                    }
                }


            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
