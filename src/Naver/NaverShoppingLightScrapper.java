package Naver;

import Util.DriverControl;
import Util.Mcode;
import Util.Utils;
import Connection.MySQLConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.sql.Connection;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
public class NaverShoppingLightScrapper {

    MySQLConnector mysql = new MySQLConnector();
    NumberFormat Format = NumberFormat.getNumberInstance(Locale.UK);
    Mcode mc = new Mcode();
    JSONParser parser = new JSONParser();
    String current_list_url = "";
    ChromeDriver driver = null;
    String DB_IP = null;
    int pagenum = 1;
    Boolean is_save  = true;

    public NaverShoppingLightScrapper(String DB_IP_STR) {
        DB_IP = DB_IP_STR;
    }

    public void setIsSave(boolean is_save) {
        this.is_save = is_save;
    }


    /**
     * 네이버 쇼핑 데이터 수집후 DB 저장
     *
     * @param part_number 시작지점
     * @param total_numbers  전체분할수
     * @param method 수집방식(category, keyword)
     * @param special 중분류 특정 카테고리 지정 (기본 null로 할 것)
     */
    public void scrapDetail(String part_number, String total_numbers, String method, String special){

        driver = DriverControl.getGeneralDriver();
        driver.get("https://shopping.naver.com");

        try {

            Thread.sleep(5000);
            ArrayList<String[]> categories = new ArrayList();

            String cat_product = null;
            String cat_big = null;
            String cat_middle = null;
            String cat_small = null;

            if(method.equals("category")) {

                Thread.sleep(5000);

                Connection conn = mysql.initConnect(DB_IP);
                ArrayList<HashMap<String, String>> category_ids = new ArrayList<>();
                if(special != "null") {
                    category_ids = (ArrayList<HashMap<String, String>>) mysql.selectCategoryIds(conn, "select * from category_id where middle_name LIKE '%" + special + "'");
                } else {
                    category_ids = (ArrayList<HashMap<String, String>>) mysql.selectCategoryIds(conn, "select * from category_id");
                }
                for(int t=0; t < category_ids.size(); t++) {
                    HashMap<String,String> hm = category_ids.get(t);
                    String[] strs = new String[5];
                    strs[0] = hm.get("big_name");
                    strs[1] = hm.get("middle_name");
                    strs[2] = hm.get("small_name");
                    strs[3] = hm.get("product_name");
                    strs[4] = "https://search.shopping.naver.com/search/category?catId=" + hm.get("category_code");
                    categories.add(strs);
                }

            }

            if(method.equals("keyword")){
                JSONParser parser = new JSONParser();
                Connection conn = mysql.initConnect(DB_IP);
                ArrayList<HashMap<String,String>> arr = new ArrayList<>();
                if(!special.equals("null")) {
                    arr = mysql.selectDatalab(conn,"select * from datalab_insight where keyword_total LIKE '%" + special + "'");
                }
                else {
                    arr = mysql.selectDatalab(conn,"select * from datalab_insight");
                }

                conn.close();
                for(int d=0; d < arr.size(); d++){
                    HashMap<String,String> hs = arr.get(d);
                    String big_cat_name = (String) hs.get("big_cat");
                    String middle_cat_name = (String) hs.get("middle_cat");
                    String small_cat_name = (String) hs.get("small_cat");
                    String product_cat_name =  (String)hs.get("product_cat");
                    String keyword_total_str = (String) hs.get("keyword_total");
                    JSONArray keyword_total = (JSONArray)parser.parse(keyword_total_str);
                    for(int k =0; k < keyword_total.size(); k++){
                        String keyword =(String) keyword_total.get(k);

                        keyword = Utils.StringReplace(keyword);

                        String shop_url = "https://search.shopping.naver.com/search/all?query=" + keyword;
                        String[] strs = new String[5];
                        strs[0] = big_cat_name;
                        strs[1] = middle_cat_name;
                        strs[2] = small_cat_name;
                        strs[3] = product_cat_name;
                        strs[4] = shop_url;
                        categories.add(strs);

                    }
                }
                System.out.println("키워드정보 수집 완료!");



            }

            Utils.ThreadSleep(Thread.currentThread(), 1000);
            int slice_counts = Math.round(categories.size() / Integer.parseInt(total_numbers));
            int end_point = slice_counts * Integer.parseInt(part_number);
            int starting_point = end_point- slice_counts;
            if(categories.size()-end_point < slice_counts) {
                end_point = categories.size();
            }

            for(int p=starting_point; p < end_point; p++) {
                String list_url = "";
                try {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    System.out.println("[" + sdf.format(cal.getTime()) + "] " + p + "/" + end_point);


                    if(method.equals("keyword")){
                        list_url = categories.get(p)[4];

                        try {
                            driver.get(list_url);
                        } catch(NoSuchMethodError er){
                            er.printStackTrace();
                            Thread.sleep(1000 * 60);
                            continue;
                        }

                        cat_big = categories.get(p)[0];
                        cat_middle = categories.get(p)[1];
                        cat_small = categories.get(p)[2];
                        cat_product = categories.get(p)[3];
                    }


                    if(method.equals("category")) {
                        try {
                            list_url = categories.get(p)[4];
                            driver.get(list_url);
                            current_list_url = list_url;

                        } catch (NoSuchMethodError er) {
                            System.out.println(er.getMessage());
                            continue;
                        }
                    }


                    while (true) {
                        getListDataFast(driver,null, list_url);
                        try {
                            try {
                                for(int pa =0; pa < 10; pa++){
                                    String pagenum_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_btn_page__FuJaU ')[" + pa + "].className");
                                    if(pagenum_str.split("activ").length > 1){
                                        String current_page = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagination_btn_page__FuJaU ')[" + pa + "].textContent.split(\"현재 페이지\")[1]");
                                        pagenum = Integer.parseInt(current_page);
                                        break;
                                    }
                                }
                                if (pagenum == 50) {
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
                        Thread.sleep(3000);


                    }
                }catch(TimeoutException timeout){
                    System.out.println(timeout.getMessage());
                    driver.navigate().refresh();
                    Thread.sleep(5000);
                }

            }
        }catch(Exception er) {
            er.printStackTrace();
            driver.navigate().refresh();
        }

        DriverControl.quitDriver(driver);

    }

    /**
     * 네이버 쇼핑 현재화면 검색결과 상품 데이터 수집
     *
     * @param drivers 크롬드라이버
     * @return total_numbers  전체분할수
     */
    public HashMap<String,JSONObject> getListData(ChromeDriver drivers) {

        String first_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");
        HashMap<String,JSONObject> product_info_list = new HashMap<>();
        int current_scroll = 0;
        for(int scroll =0; scroll < 50; scroll++) {
            ((JavascriptExecutor) drivers).executeScript("window.scrollBy(" + current_scroll + "," + (current_scroll + 50) + ")");
            current_scroll += 100;
            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                //
            }
        }

        Long total_items = (Long) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81').length");
        for (int pp = 0; pp < total_items; pp++) {

            String product_url = "";
            try {
                product_url = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[0].children[0].children[0].href");
                String check_url = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_mall_title__3MWFY')[" + pp + "].children[0].href");
                if(check_url.split("smartstore.naver.com").length == 1) {
                    continue;
                }

            } catch (NoSuchMethodError er) {
                continue;
            }


            String cat_product = "";
            String cat_big = "";
            String cat_middle = "";
            String cat_small = "";
            try {
                cat_big = (String) ((JavascriptExecutor) drivers).executeScript("return  document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[2].children[0].textContent");
                cat_middle = (String) ((JavascriptExecutor) drivers).executeScript("return  document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[2].children[1].textContent");
                cat_small = (String) ((JavascriptExecutor) drivers).executeScript("return  document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[2].children[2].textContent");
                cat_product = (String) ((JavascriptExecutor) drivers).executeScript("return  document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[2].children[3].textContent");

            } catch (NoSuchMethodError e1) {
                //e1.printStackTrace();
            } catch(Exception e2){
                //e2.printStackTrace();
            }


            String price_str = "";
            int price = 0;
            try {
                price_str = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[1].textContent");
                if (price_str.substring(0, 2).equals("최저") || price_str.substring(0, 2).equals("광고")) {
                    price_str = price_str.substring(2, price_str.length()-1);
                }
                price = Format.parse(price_str).intValue();

            } catch (NoSuchMethodError e1) {
                //e1.printStackTrace();
            } catch(ParseException e2){
                //e2.printStackTrace();
            } catch(Exception e3){
                //e3.printStackTrace();
            }

            String product_no = ""; //변경됨

            String product_img = "";
            try {
                product_img = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[0].children[0].children[0].children[0].src");
            } catch (NoSuchMethodError e1) {
                //e1.printStackTrace();
            } catch(Exception e2){
                //e2.printStackTrace();
            }


            String store_name = null;
            int category_comparison = 0;
            try {
                store_name = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[2].children[0].children[0].textContent");
                if(store_name.equals("쇼핑몰별 최저가")) {
                    category_comparison = 1;
                    store_name = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[2].children[1].children[0].children[0].children[0].children[1].textContent");
                }
            } catch (NoSuchMethodError e1) {
                //e1.printStackTrace();
            } catch(Exception e2){
                //e2.printStackTrace();
            }
            String store_grade = "씨앗";
            try {
                if(category_comparison == 1) {
                    store_grade = "최저가";
                }
                else {
                    store_grade = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[2].children[1].children[0].textContent");
                }
            } catch (NoSuchMethodError e1) {
                //e1.printStackTrace();
            } catch(Exception e2){
                //e2.printStackTrace();
            }


            String title = null;
            try {
                title = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[0].textContent");
            } catch (NoSuchMethodError e) {
                //e.printStackTrace();
            }

            String total_cnt_str = "";
            try {
                total_cnt_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('subFilter_num__2x0jq')[0].textContent");
            }catch(Exception e) {
                e.printStackTrace();
            }



            String cat_full = cat_big + ">" + cat_middle + ">" + cat_small;
            if (cat_product != null && cat_product.equals("null") && !cat_product.equals("")) {
                cat_full += ">";
                cat_full += cat_product;
            }


            int count_zzim = -1;
            int count_review = -1;
            int count_buy = -1;
            String register_date = null;
            long register_timestamp = -1;
            Double count_star = -1.0;

            try {
                Long sub_datas = (Long) ((JavascriptExecutor) drivers).executeScript(" return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[4].children.length");

                for (int sd = 0; sd < sub_datas; sd++) {
                    String sub_data = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[4].children[" + sd + "].textContent");
                    String sub_check = sub_data.substring(0, 2);
                    if (sub_check.equals("리뷰")) {

                        try {
                            if (sub_data.substring(0, 3).equals("리뷰별")) {
                                String review_str = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[4].children[" + sd + "].children[1].textContent");
                                count_review = Format.parse(review_str).intValue();
                                String star = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('basicList_item__2XT81')[" + pp + "].children[0].children[1].children[4].children[" + sd + "].children[0].textContent");
                                count_star = Format.parse(star).doubleValue();
                            } else {
                                count_review = Format.parse(sub_data.substring(2, sub_data.length())).intValue();
                            }
                        } catch (NoSuchMethodError e1) {
                            //e1.printStackTrace();
                        } catch (ParseException e2) {
                            //e2.printStackTrace();
                        } catch(IndexOutOfBoundsException e3){
                            e3.printStackTrace();
                        }


                    }
                    if (sub_check.equals("등록")) {
                        try {
                            register_date = sub_data.substring(4, sub_data.length());
                            if (register_date != null && !register_date.equals("")) {
                                register_date += "01";
                                register_date = register_date.replaceAll("\\.", "-");
                                register_date = register_date.trim();

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Month.Day.Year
                                Date d = formatter.parse(register_date);
                                register_timestamp = d.getTime();
                            }
                        } catch (NoSuchMethodError e1) {
                            //e1.printStackTrace();
                        } catch (ParseException e2) {
                            //e2.printStackTrace();
                        }
                    }
                    if (sub_check.equals("찜하")) {
                        try {
                            count_zzim = Format.parse(sub_data.substring(3, sub_data.length())).intValue();
                        } catch (NoSuchMethodError e1) {
                            //e1.printStackTrace();
                        } catch (ParseException e2) {
                            //e2.printStackTrace();
                        }
                    }
                    if (sub_check.equals("구매")) {
                        try {
                            count_buy = Format.parse(sub_data.substring(4, sub_data.length())).intValue();
                        } catch (NoSuchMethodError e1) {
                            //e1.printStackTrace();
                        } catch (ParseException e2) {
                            //e2.printStackTrace();
                        }
                    }

                }

                Long insert_timestamp = System.currentTimeMillis() / 1000;
                register_timestamp = register_timestamp / 1000;
                JSONObject obj = new JSONObject();
                obj.put("product_no", product_no);
                obj.put("product_url", product_url);
                obj.put("product_img", product_img);
                obj.put("price", price);
                obj.put("cat_full", cat_full);
                obj.put("cat_big", cat_big);
                obj.put("cat_middle", cat_middle);
                obj.put("cat_small", cat_small);
                obj.put("cat_product", cat_product);
                obj.put("count_review", count_review);
                obj.put("count_buy", count_buy);
                obj.put("count_zzim", count_zzim);
                obj.put("cat_product", cat_product);
                obj.put("insert_timestamp", insert_timestamp);
                obj.put("register_date", register_date);
                obj.put("register_timestamp", register_timestamp);
                obj.put("title", title);
                obj.put("store_name", store_name);
                obj.put("store_grade", store_grade);
                obj.put("star", count_star);
                obj.put("category_comparison", category_comparison);
                obj.put("rank", (pp+1));
                obj.put("total_cnt", total_cnt_str);

                product_info_list.put(product_url, obj);
            }catch(NoSuchMethodError er){
                //
            }
        }


        for( String mall_url : product_info_list.keySet() ){

            try {
                drivers.get(mall_url);
                Boolean invalid = DriverControl.invalidCheck(drivers);
                if(invalid == false) {
                    continue;
                }
                String current_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");
                if(current_url.split("shopping.naver.com").length > 1){
                    try {
                        String lowest_url = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('low_price')[0].nextElementSibling.href");
                        drivers.get(lowest_url);
                        current_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }

                }

                if (current_url.split("smartstore.naver.com").length  == 1) {
                    continue;
                }


                String product_no = current_url.split("\\?NaPm")[0].split("products/")[1];
                product_info_list.get(mall_url).put("product_no", product_no);

                try {
                    String store_names = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('store_name')[0].textContent.trim()");
                    String store_name2 = store_names.split("고객센터")[0];
                    product_info_list.get(mall_url).put("store_name", store_name2);
                    Double star_this = -1.0;
                    String starst = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('wrap_estimation')[0].children[1].children[1].textContent");
                    star_this = Double.parseDouble(starst);
                    product_info_list.get(mall_url).put("star", star_this);
                    String seller_grade ="씨앗";
                    seller_grade = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('grade')[0].textContent");
                    product_info_list.get(mall_url).put("store_grade", seller_grade);

                } catch (NoSuchMethodError e1) {
                    //e1.printStackTrace();
                } catch(Exception e2){
                    //e2.printStackTrace();
                }

                JSONObject obj = product_info_list.get(mall_url);
                String insert_query = "insert into naver_light(product_no,product_url, product_img, price, cat_full, cat_big, cat_middle, cat_small, cat_product,count_review, count_buy, count_zzim, insert_timestamp, register_date, register_timestamp, title, store_name, store_grade, star, category_comparison,docid,parentid,normhit,similar_image_cnt, keep_cnt,isExceptedBest100, is_brand,is_hotdeal,search_keyword,open_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                Connection conn = mysql.initConnect(DB_IP);
                mysql.insertProductInfoOfNaverLight(conn,
                        insert_query,
                        (String)obj.get("product_no"),
                        current_url,
                        (String)obj.get("product_img"),
                        (int)obj.get("price"),
                        (String)obj.get("cat_full"),
                        (String)obj.get("cat_big"),
                        (String)obj.get("cat_middle"),
                        (String) obj.get("cat_small"),
                        (String) obj.get("cat_product"),
                        (int)obj.get("count_review"),
                        (int)obj.get("count_buy"),
                        (int) obj.get("count_zzim"),
                        (Long) obj.get("insert_timestamp"),
                        (String) obj.get("register_date"),
                        (Long) obj.get("register_timestamp"),
                        (String)obj.get("title"),
                        (String) obj.get("store_name"),(String) obj.get("store_grade"),(Double) obj.get("star"), (Integer) obj.get("category_comparison"), "", "","","","", "","", "","","" );
                conn.close();

            } catch (Exception e1) {
                e1.printStackTrace();
                continue;
            } catch (NoSuchMethodError e2){
                continue;
            }

        }
        try {
            drivers.get(first_url);
        }catch(NoSuchMethodError er){
            drivers = DriverControl.getGeneralDriver();
            drivers.get(first_url);
        }
        return product_info_list;

    }
    /**
     * 네이버 쇼핑 검색결과 상품 데이터 수집
     *
     * @param driver 크롬드라이버
     * @param passing_keyword 검색키워드
     * @param query_url 쿼리URL
     */
    public ArrayList<HashMap<String, String>> getListDataFast(ChromeDriver driver, String passing_keyword, String query_url) {


        boolean skip = false;
        String content_str = "";
        while(true) {
            try {
                driver.get(query_url);
                content_str= (String) ((JavascriptExecutor) driver).executeScript("return document.getElementById('__NEXT_DATA__').textContent");
                String location_href = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                if(!location_href.contains("naver")) {
                    skip = true;
                    break;
                }
                else {
                    break;
                }
            }catch(Exception ex){
                try {
                    skip =true;
                    break;

                }catch(Exception EX2){

                }
            }
        }

        if(skip) {
            return new ArrayList<>();
        }

        long startTime = System.currentTimeMillis();

        ArrayList<HashMap<String,String>> arr = new ArrayList<>();


        try {
            String total_cnt_str = "";
            total_cnt_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('subFilter_num__2x0jq')[0].textContent");
        }catch(Exception ex) {

        }

        String isAd = "0";


        try {
            JSONObject content_obj = (JSONObject) parser.parse(content_str);
            JSONObject props = (JSONObject) content_obj.get("props");
            JSONObject pageProps = (JSONObject)props.get("pageProps");
            JSONObject initialState =(JSONObject)pageProps.get("initialState");
            JSONObject products = (JSONObject)initialState.get("products");
            ArrayList list = (ArrayList) products.get("list");

            for (int t =1; t < list.size(); t++) {

                int rank = t;
                JSONObject obj_temp = (JSONObject)list.get(t);
                JSONObject content = (JSONObject)obj_temp.get("item");
                String product_no = (String) content.get("mallProductId");

                String ad_id = (String) content.get("adid");
                if(ad_id != null) {
                    isAd = "1";
                }
                String title = (String)content.get("productName");
                String store_name =  (String)content.get("mallName");

                String mall_no = (String) content.get("mallNo");
                String mall_url = "";
                try {
                    mall_url = (String) content.get("mallPcUrl");
                }catch(Exception ex){

                }

                String count_review_str ="";
                try {
                    count_review_str = String.valueOf((Long) content.get("reviewCount"));
                }catch(Exception ee) {
                    count_review_str = (String)content.get("reviewCount");
                }
                int count_review = Integer.valueOf(count_review_str);
                String price_str = (String)content.get("price");
                int price = Integer.parseInt(price_str);
                String cat_big = (String)content.get("category1Name");
                String cat_middle = (String)content.get("category2Name");
                String cat_small = (String)content.get("category3Name");
                String cat_product = (String)content.get("category4Name");
                String product_img =(String)content.get("imageUrl");
                String product_url = (String)content.get("mallProductUrl");
                if(product_url == null) {
                    product_url = (String)content.get("adcrUrl");
                }
                String is_brand = "";
                try {
                    is_brand = (String) content.get("isBrandStore");
                }catch(Exception EX) {
                    is_brand = String.valueOf((Long) content.get("isBrandStore"));
                }
                String search_keyword = (String)content.get("searchKeyword");
                String is_hotdeal ="";
                try {
                    is_hotdeal = (String) content.get("isHotDeal");
                }catch(Exception ee){
                    is_hotdeal = String.valueOf((Long) content.get("isHotDeal"));
                }
                String open_date = (String)content.get("openDate");
                String docId =  (String)content.get("docid");
                String parentId =  (String)content.get("parentId");
                String newNormHit2 ="";
                try {
                    newNormHit2 = (String) content.get("newNormHit2");
                }catch(Exception ex) {
                    try {
                        newNormHit2 = String.valueOf((Double) content.get("newNormHit2"));
                    }catch(Exception e22x) {
                        newNormHit2 = String.valueOf((Long) content.get("newNormHit2"));
                    }
                }
                String simillarImageCnt =  (String)content.get("mblSimImgSgntCnt");
                String isExceptedBest100 = (String) content.get("isExceptedBest100");
                String keepCnt = "";
                try {
                    keepCnt = (String) content.get("keepCnt");
                }catch(Exception EEE){
                    keepCnt = String.valueOf((Long) content.get("keepCnt"));
                }



                String register_date = open_date.substring(0, 8);
                java.util.Date reg_date = new java.text.SimpleDateFormat("yyyyMMdd").parse(register_date);
                int count_buy = -1;
                String count_buy_str = "";


                try {
                    count_buy_str = (String) content.get("purchaseCnt");
                }catch(Exception ex){
                    count_buy_str = String.valueOf((Long) content.get("purchaseCnt"));
                }


                count_buy = Integer.parseInt(count_buy_str);
                Long insert_timestamp = System.currentTimeMillis() / 1000;
                Long register_timestamp = reg_date.getTime() / 1000;


                String store_grade = "가격비교";
                try {
                    JSONObject mallInfoCache = (JSONObject) content.get("mallInfoCache");
                    String grade_code = (String)mallInfoCache.get("mallGrade");

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
                }catch(Exception ex){

                }

                int category_comparison = 0;
                if(product_no == null){
                    category_comparison = 1;
                    product_url = (String)content.get("adcrUrl");
                }

                else if(product_no.equals("")) {
                    category_comparison = 1;
                    product_url = (String)content.get("crUrl");
                }


                Boolean isSmartStore = false;
                try {
                    long pd = Long.parseLong(product_no);
                    isSmartStore = true;
                } catch (Exception ex) {
                    isSmartStore = false;
                }

                HashMap<String,String> hm = new HashMap<>();
                hm.put("product_no", product_no);
                hm.put("category_comparison",String.valueOf(category_comparison));
                hm.put("product_url", product_url);
                hm.put("store_grade", store_grade);
                hm.put("store_name", store_name);
                hm.put("title", title);
                hm.put("price",price_str);
                hm.put("cat_small", cat_small);
                hm.put("cat_big",cat_big);
                hm.put("cat_middle", cat_middle);
                hm.put("cat_product", cat_product);
                hm.put("mall_no",mall_no);
                hm.put("product_img",product_img);
                hm.put("isSmartStore",isSmartStore.toString());
                hm.put("review_count",count_review_str);
                hm.put("is_brand",is_brand);
                hm.put("count_buy",count_buy_str);
                hm.put("open_date",open_date);
                hm.put("search_keyword",search_keyword);
                hm.put("isAd", isAd.toString());
                hm.put("keyword",passing_keyword);
                hm.put("mall_url", mall_url);
                arr.add(hm);
                try {

                    if (isSmartStore) {

                        if(product_url != null) {



                            if(is_save) {

                                String insert_query = "insert into naver_light(product_no,product_url, product_img, price, cat_full, cat_big, cat_middle, cat_small, cat_product,count_review, count_buy, count_zzim, insert_timestamp, register_date, register_timestamp, title, store_name, store_grade, star, category_comparison,docid,parentid,normhit,similar_image_cnt, keep_cnt,isExceptedBest100, is_brand,is_hotdeal,search_keyword,open_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                Connection conn = mysql.initConnect(DB_IP);
                                mysql.insertProductInfoOfNaverLight(conn,
                                        insert_query,
                                        product_no,
                                        product_url,
                                        product_img,
                                        price,
                                        cat_big + ">" + cat_middle + ">" + cat_small + ">" + cat_product,
                                        cat_big,
                                        cat_middle,
                                        cat_small,
                                        cat_product,
                                        count_review,
                                        count_buy,
                                        0,
                                        insert_timestamp,
                                        register_date,
                                        register_timestamp,
                                        title,
                                        store_name, store_grade, 0.0, category_comparison, docId, parentId, newNormHit2, simillarImageCnt, keepCnt, isExceptedBest100, is_brand, is_hotdeal, search_keyword, open_date);
                                conn.close();
                            }
                        }

                    }


                } catch (Exception ex) {

                }



            }

        } catch (Exception ee) {

            ee.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        long delta = endTime-startTime;

        if(delta < 2000){
            try {
                Thread.sleep(delta);
            }catch(Exception ex){

            }
        }


        return arr;

    }


    /**
     * 상세페이지 데이터 수집 및 저장
     *
     * @param url URL
     */
    public void scrapDetailByUrl(String url) {

        ChromeDriver driver = null;
        try {

            driver = DriverControl.getGeneralDriver();

            try {

                getListDataFast(driver,null, url);
                Thread.sleep(3000);
                DriverControl.quitDriver(driver);

            } catch (Exception er) {
                driver.navigate().refresh();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        DriverControl.quitDriver(driver);
    }



    public HashMap<String,JSONObject> getListDataNew(ChromeDriver drivers) {

        String first_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");
        HashMap<String,JSONObject> product_info_list = new HashMap<>();
        int current_scroll = 0;
        for(int scroll =0; scroll < 50; scroll++) {
            ((JavascriptExecutor) drivers).executeScript("window.scrollBy(" + current_scroll + "," + (current_scroll + 50) + ")");
            current_scroll += 100;
            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        mc.initNaverList(0);
        Long total_items = (Long) ((JavascriptExecutor) drivers).executeScript(mc.product_list_length);

        for (int pp = 0; pp < total_items; pp++) {

            try {
                mc.initNaverList(pp);
                String product_url = "";
                String product_img = "";
                String product_no = "";
                String cat_product = "";
                String cat_big = "";
                String cat_middle = "";
                String cat_small = "";
                String price_str = "";
                int price = 0;
                String store_grade = "씨앗";
                String store_name = "";
                String title = null;
                int count_zzim = -1;
                int count_review = -1;
                int count_buy = -1;
                String register_date = null;
                long register_timestamp = -1;
                Double count_star = -1.0;
                int category_comparison = -1;

                try {
                    product_url = (String) ((JavascriptExecutor) drivers).executeScript(mc.product_url);
                    String check_url = (String) ((JavascriptExecutor) drivers).executeScript(mc.seller_url);
                    if (check_url.split("smartstore.naver.com").length == 1) {
                        continue;
                    }

                } catch (NoSuchMethodError er) {
                    continue;
                } catch (Exception ex) {
                    continue;
                }


                try {
                    title = (String) ((JavascriptExecutor) drivers).executeScript(mc.product_name);
                    product_no = (String) ((JavascriptExecutor) drivers).executeScript(mc.product_no);
                    product_img = (String) ((JavascriptExecutor) drivers).executeScript(mc.product_img);
                    cat_big = (String) ((JavascriptExecutor) drivers).executeScript(mc.cat_big);
                    cat_middle = (String) ((JavascriptExecutor) drivers).executeScript(mc.cat_middle);
                    cat_small = (String) ((JavascriptExecutor) drivers).executeScript(mc.cat_small);
                    cat_product = (String) ((JavascriptExecutor) drivers).executeScript(mc.cat_product);
                    price_str = (String) ((JavascriptExecutor) drivers).executeScript(mc.product_price);
                    price = Format.parse(price_str).intValue();
                    store_name = (String) ((JavascriptExecutor) drivers).executeScript(mc.seller_name);
                    String count_buy_str = (String)((JavascriptExecutor) drivers).executeScript(mc.product_buy_count);
                    count_buy = Format.parse(count_buy_str).intValue();
                    String register_date_str = (String)((JavascriptExecutor) drivers).executeScript(mc.register_date);
                    register_date = register_date_str.substring(0,4) + "-" + register_date_str.substring(4,6) + "-" + register_date.substring(6,8);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Month.Day.Year
                    Date d = formatter.parse(register_date);
                    register_timestamp = d.getTime();

                } catch (NoSuchMethodError er) {
                    System.out.println(er.toString());
                } catch (Exception er2) {
                    System.out.println(er2.toString());
                }

                try {
                    String count_review_str = (String) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                    count_review = Format.parse(count_review_str).intValue();
                } catch (ClassCastException ex) {
                    Long count_review_long = (Long) ((JavascriptExecutor) driver).executeScript(mc.product_review_count);
                    count_review = Format.parse(String.valueOf(count_review_long)).intValue();
                }


                try {
                    String count_star_str = (String) ((JavascriptExecutor) driver).executeScript(mc.product_star);
                    count_star = Format.parse(count_star_str).doubleValue();
                } catch (ClassCastException | ParseException ex) {

                }

                try {
                    ArrayList isComparison = (ArrayList) ((JavascriptExecutor) drivers).executeScript(mc.product_comparison_list);
                    if (isComparison.size() > 0) {
                        store_grade = "최저가";
                        category_comparison = 1;
                    } else {
                        store_grade = mc.getSellerGrade((String) ((JavascriptExecutor) drivers).executeScript(mc.seller_grade_code));
                    }
                } catch (NoSuchMethodError e1) {

                } catch (Exception e2) {

                }


                String total_cnt_str = "";
                try {
                    total_cnt_str = (String) ((JavascriptExecutor) driver).executeScript(mc.total_products_count);
                } catch (Exception e) {

                }
                String cat_full = cat_big + ">" + cat_middle + ">" + cat_small;
                if (cat_product != null && cat_product.equals("null") && !cat_product.equals("")) {
                    cat_full += ">";
                    cat_full += cat_product;
                }

                try {
                    Long insert_timestamp = System.currentTimeMillis() / 1000;
                    register_timestamp = register_timestamp / 1000;

                    JSONObject obj = new JSONObject();
                    obj.put("product_no", product_no);
                    obj.put("product_url", product_url);
                    obj.put("product_img", product_img);
                    obj.put("price", price);
                    obj.put("cat_full", cat_full);
                    obj.put("cat_big", cat_big);
                    obj.put("cat_middle", cat_middle);
                    obj.put("cat_small", cat_small);
                    obj.put("cat_product", cat_product);
                    obj.put("count_review", count_review);
                    obj.put("count_buy", count_buy);
                    obj.put("count_zzim", count_zzim);
                    obj.put("cat_product", cat_product);
                    obj.put("insert_timestamp", insert_timestamp);
                    obj.put("register_date", register_date);
                    obj.put("register_timestamp", register_timestamp);
                    obj.put("title", title);
                    obj.put("store_name", store_name);
                    obj.put("store_grade", store_grade);
                    obj.put("star", count_star);
                    obj.put("category_comparison", category_comparison);
                    obj.put("rank", (pp + 1));
                    obj.put("total_cnt", total_cnt_str);

                    product_info_list.put(product_url, obj);
                } catch (NoSuchMethodError er) {
                    //
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }
        for( String mall_url : product_info_list.keySet() ){


            try {
                drivers.get(mall_url);
                Boolean invalid = DriverControl.invalidCheck(drivers);
                if(invalid == false) {
                    continue;
                }
                String current_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");

                if(current_url.split("shopping.naver.com").length > 1){
                    try {
                        String lowest_url = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('low_price')[0].nextElementSibling.href");
                        drivers.get(lowest_url);
                        current_url = (String) ((JavascriptExecutor) drivers).executeScript("return location.href");
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }

                }

                if (current_url.split("smartstore.naver.com").length  == 1) {
                    continue;
                }

                String product_no = current_url.split("\\?NaPm")[0].split("products/")[1];

                product_info_list.get(mall_url).put("product_no", product_no);

                try {
                    String store_names = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('store_name')[0].textContent.trim()");
                    String store_name2 = store_names.split("고객센터")[0];
                    product_info_list.get(mall_url).put("store_name", store_name2);
                    Double star_this = -1.0;
                    String starst = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('wrap_estimation')[0].children[1].children[1].textContent");
                    star_this = Double.parseDouble(starst);
                    product_info_list.get(mall_url).put("star", star_this);
                    String seller_grade ="씨앗";
                    seller_grade = (String) ((JavascriptExecutor) drivers).executeScript("return document.getElementsByClassName('grade')[0].textContent");
                    product_info_list.get(mall_url).put("store_grade", seller_grade);

                } catch (NoSuchMethodError e1) {

                } catch(Exception e2){

                }

                JSONObject obj = product_info_list.get(mall_url);

                String insert_query = "insert into naver_light(product_no,product_url, product_img, price, cat_full, cat_big, cat_middle, cat_small, cat_product,count_review, count_buy, count_zzim, insert_timestamp, register_date, register_timestamp, title, store_name, store_grade, star, category_comparison,docid,parentid,normhit,similar_image_cnt, keep_cnt,isExceptedBest100, is_brand,is_hotdeal,search_keyword,open_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                Connection conn = mysql.initConnect(DB_IP);
                mysql.insertProductInfoOfNaverLight(conn,
                        insert_query,
                        (String)obj.get("product_no"),
                        current_url,
                        (String)obj.get("product_img"),
                        (int)obj.get("price"),
                        (String)obj.get("cat_full"),
                        (String)obj.get("cat_big"),
                        (String)obj.get("cat_middle"),
                        (String) obj.get("cat_small"),
                        (String) obj.get("cat_product"),
                        (int)obj.get("count_review"),
                        (int)obj.get("count_buy"),
                        (int) obj.get("count_zzim"),
                        (Long) obj.get("insert_timestamp"),
                        (String) obj.get("register_date"),
                        (Long) obj.get("register_timestamp"),
                        (String)obj.get("title"),
                        (String) obj.get("store_name"),(String) obj.get("store_grade"),(Double) obj.get("star"), (Integer) obj.get("category_comparison"), "", "","","","", "","", "","","" );
                conn.close();




            } catch (Exception er) {
                er.printStackTrace();
                continue;
            } catch (NoSuchMethodError E){
                continue;
            }

        }


        try {
            drivers.get(first_url);
        }catch(NoSuchMethodError er){
            drivers = DriverControl.getGeneralDriver();
            drivers.get(first_url);
        }
        return product_info_list;

    }


}

