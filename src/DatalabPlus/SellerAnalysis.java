package DatalabPlus;

import Naver.*;
import Util.*;
import Connection.MySQLConnector2;
import Connection.MySQLConnector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URLDecoder;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SellerAnalysis {

    MySQLConnector mysql = new MySQLConnector();
    MySQLConnector2 mysql2 = new MySQLConnector2();
    Connection conn = null;
    JSONParser parser = new JSONParser();
    ProductAnalysis pa = new ProductAnalysis(Conf.NAVER_DB_IP);
    NaverShoppingLightScrapper nl = new NaverShoppingLightScrapper(Conf.NAVER_DB_IP);
    SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
    Recorder rec = new Recorder();
    String update_name = "";
    String update_type = "";
    int min =1;
    int max = 9900;

    /**
     * 관련 상품 프로덕트 넘버 구하기
     *
     */
    public ArrayList getRelevantProductNos(ChromeDriver driver, ArrayList<String> product_urls, String target_category_small, String target_storename){

        ArrayList<ArrayList>  keyword_groups = KeywordGroupMakerWithReason(driver,product_urls);
        ArrayList<String> keyword_group = keyword_groups.get(0);
        ArrayList<String> keyword_reasons = keyword_groups.get(1);
        ArrayList<HashMap> nos = new ArrayList<>();
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, -7);
        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, -30);
        Date month_ago = c1.getTime();
        int isWeekOverData = 1;
        JSONParser parser = new JSONParser();

        try {
            conn = mysql.initConnect(Conf.NAVER_DB_IP);
            for (int i = 0; i < keyword_group.size(); i++) {
                String keyword = (String) keyword_group.get(i);
                if(keyword.equals("")){
                    continue;
                }
                String keyword_reason = (String) keyword_reasons.get(i);
                ArrayList<HashMap<String, String>> keyword_cache = mysql.selectKeywordCache(conn, "select * from keyword_cache where keyword='" + keyword + "'");

                try {
                    if (keyword_cache.size() > 0) {
                        Date inserted_date = yyyymmddFormat.parse(keyword_cache.get(0).get("insert_time"));
                        isWeekOverData = month_ago.compareTo(inserted_date);
                    } else{
                        isWeekOverData = 999;
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

                try {
                    ArrayList<HashMap<String, String>> array = new ArrayList<>();
                    if (isWeekOverData > 0) {
                        String query_url = "https://search.shopping.naver.com/search/all?where=all&frm=NVSCTAB&query=" + keyword;
                        array = nl.getListDataFast(driver,keyword, query_url);
                        mysql.insertKeywordCache(conn, "insert into keyword_cache(keyword,data) values(?,?)", keyword, Calculator.convertArrayListToJSONArrayString(array));
                        if(array.size() ==0) {
                            continue;
                        }

                    } else {
                        JSONArray cache_data = (JSONArray) parser.parse((String) ((HashMap) keyword_cache.get(0)).get("data"));
                        for (int c = 0; c < cache_data.size(); c++) {
                            JSONObject dataset =(JSONObject) cache_data.get(c);
                            String product_url = (String)dataset.get("product_url");
                            if(product_url.split("smartstore.naver.com").length > 1) {
                                array.add(Calculator.convertJSONObjectToHashmap((JSONObject) cache_data.get(c)));
                            }
                        }
                    }


                    for (int j = 0; j < array.size(); j++) {

                        String product_no = array.get(j).get("product_no");
                        String cat_small = array.get(j).get("cat_small");
                        String store_name = array.get(j).get("store_name");
                        String product_url = array.get(j).get("product_url");


                        if(store_name == null){
                            continue;
                        }

                        if (cat_small.equals(target_category_small) && !store_name.equals(target_storename) && !store_name.equals("")) {

                            HashMap<String,String> hm = new HashMap<>();
                            hm.put("product_no", product_no);
                            hm.put("cat_small", cat_small);
                            hm.put("store_name", store_name);
                            hm.put("product_url", product_url);
                            hm.put("keyword", keyword);
                            hm.put("선정근거", keyword_reason);
                            System.out.println("타겟경쟁상품후보군:" + product_no);
                            nos.add(hm);
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            conn.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return nos;
    }

    /**
     * 키워드 ngram 분해 + 메타태그 + 데이터랩 키워드 리스트
     *
     */
    public ArrayList<?> KeywordGroupMaker(ChromeDriver driver, ArrayList<String> product_urls) {


        ArrayList keyword_group = new ArrayList();
        String cat_small = "";
        HashMap<String,Integer> hasKeyword = new HashMap<>();

        if(driver == null) {
            driver = DriverControl.getGeneralDriver();
        }


        for(int i=0; i < product_urls.size(); i++) {

            JSONObject basicInfos = pa.getBasicProductDetailInfo(driver, product_urls.get(i));


            String title = (String) basicInfos.get("title");
            String meta_keywords = (String) basicInfos.get("meta_keywords");
            cat_small = (String) basicInfos.get("cat_small");
            String[] metas = meta_keywords.split(",");
            for (int m = 0; m < metas.length; m++) {
                if (!hasKeyword.containsKey(metas[m])) {
                    keyword_group.add(metas[m]);
                    hasKeyword.put(metas[m], 1);
                }
            }
            String[] title_split = title.split(" ");
            String temp = "";
            for (int t1 = 0; t1 < title_split.length; t1++) {
                if (!hasKeyword.containsKey(temp)) {
                    keyword_group.add(temp);
                    hasKeyword.put(temp, 1);
                }
            }

        }

        try {
            ArrayList<HashMap<String, String>> datalab_data = new ArrayList<>();
            Connection conn_select = mysql.initConnect(Conf.NAVER_DB_IP);
            datalab_data = mysql.selectDatalab(conn_select, "select * from datalab_insight where small_cat='" + cat_small + "'");
            conn_select.close();
            for (int d = 0; d < datalab_data.size(); d++) {
                HashMap<String, String> hs = datalab_data.get(d);
                String keyword_total_str = (String) hs.get("keyword_total");
                JSONArray keyword_total = (JSONArray) parser.parse(keyword_total_str);
                int keyword_limit = 30;
                if(keyword_limit > keyword_total.size() ) {
                    keyword_limit = keyword_total.size();
                }
                for (int k = 0; k < keyword_limit; k++) {
                    String keyword = (String) keyword_total.get(k);
                    if (!hasKeyword.containsKey(keyword)) {
                        keyword_group.add(keyword);
                        hasKeyword.put(keyword, 1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return keyword_group;

    }
    /**
     * 키워드 ngram 분해 + 메타태그 + 데이터랩 키워드 +근거 리스트
     *
     */
    public ArrayList<ArrayList> KeywordGroupMakerWithReason(ChromeDriver driver, ArrayList<String> product_urls) {


        ArrayList<String> keyword_group = new ArrayList();
        ArrayList<String> keyword_reason = new ArrayList();
        String cat_small = "";
        HashMap<String,Integer> hasKeyword = new HashMap<String,Integer>();

        if(driver == null) {
            driver = DriverControl.getGeneralDriver();
        }


        for(int i=0; i < product_urls.size(); i++) {
            JSONObject basicInfos = pa.getBasicProductDetailInfo(driver, product_urls.get(i));
            String title = (String) basicInfos.get("title");
            String meta_keywords = (String) basicInfos.get("meta_keywords");
            String cat_middle = (String) basicInfos.get("cat_middle");
            cat_small = (String) basicInfos.get("cat_small");
            String[] metas = meta_keywords.split(",");
            for (int m = 0; m < metas.length; m++) {
                if (!hasKeyword.containsKey(metas[m])) {
                    keyword_group.add(metas[m]);
                    keyword_reason.add("메타태그");
                    hasKeyword.put(metas[m], 1);
                }
            }
            String[] title_split = title.split(" ");
            String temp = "";
            for (int t1 = 0; t1 < title_split.length; t1++) {
                if (!hasKeyword.containsKey(temp)) {
                    keyword_group.add(temp);
                    keyword_reason.add("타이틀");
                    hasKeyword.put(temp, 1);
                }

            }

        }
        ArrayList result_array = new ArrayList();
        result_array.add(keyword_group);
        result_array.add(keyword_reason);

        return result_array;

    }
    /**
     * 샵의 모든 상품 url 수집
     *
     */
    public HashMap<String,HashMap> getShoppingmallGroup(ChromeDriver driver, ArrayList keyword_group) {


        HashMap<String,HashMap> shop_list = new HashMap<>();

        int review_count_sum = 0;

        if(driver == null){
            driver =DriverControl.getGeneralDriver();
        }

        for(int i =0; i < keyword_group.size(); i++) {
            String keyword = (String) keyword_group.get(i);
            String query_url = "https://search.shopping.naver.com/search/all?where=all&frm=NVSCTAB&query=" + keyword;
            ArrayList<HashMap<String,String>> array = new ArrayList<>();
            try {
                array = nl.getListDataFast(driver,keyword, query_url);
                Thread.sleep(2000);
            }catch(Exception ex){
                continue;
            }
            for(int t=0; t < array.size(); t++){
                String mall_no = array.get(t).get("mall_no");
                String product_no = array.get(t).get("product_no");
                String product_url = array.get(t).get("product_url");
                String store_name = array.get(t).get("store_name");
                String review_count_str = array.get(t).get("review_count");
                review_count_sum += Integer.parseInt(review_count_str);
                if(mall_no != null && product_url != null) {
                    shop_list.put(mall_no, array.get(t));
                }
            }
            try {
                Thread.sleep(1500);
            }catch(Exception ex){

            }
        }

        System.out.println("fin" + review_count_sum);

        return shop_list;



    }
    /**
     * 샵의 모든 상품 url 수집
     *
     */
    public ArrayList<String> getAllProductsUrlFromShop(ChromeDriver driver, String url) {

        conn = mysql.initConnect(Conf.NAVER_DB_IP);

        Long review_sum = 0l;

        HttpConn httpconn = new HttpConn();
        JSONArray result_array = new JSONArray();
        String store_name = "";
        String store_name_kor = "";
        ArrayList<String> product_urls = new ArrayList<>();
        ArrayList<String> product_url_list = new ArrayList<String>();

        try {
            url = URLDecoder.decode(url, "UTF-8");
            int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
            int smartstore_leng = url.split("smartstore.naver.com").length;
            int brand_leng = url.split("brand.naver.com").length;
            if(smartstore_leng > 1 || brand_leng > 1) {

                if (url.split("\\?NaPm").length > 1) {
                    url = url.split("\\?NaPm")[0];
                }

                if (smartstore_leng > 1) {
                    String[] suburl_list = url.split("smartstore.naver.com")[1].split("/");
                    if (suburl_list.length > 1) {
                        store_name = suburl_list[1];
                        url = "https://smartstore.naver.com/" + store_name + "/category/ALL?cp=1";
                    }
                }

                if (brand_leng > 1) {
                    String[] suburl_list = url.split("brand.naver.com")[1].split("/");
                    if (suburl_list.length > 1) {
                        store_name = suburl_list[1];
                        url = "https://brand.naver.com/" + store_name + "/category/ALL?cp=1";
                    }
                }
                while(true) {
                    try {
                        if(store_name.equals("")) {
                            break;
                        }
                        driver.get(url);
                        int page_num = 1;
                        int product_check_num = 0;
                        Long prc_leng = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split('productCount\":').length");
                        String product_count_str = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split('productCount\":')[" + String.valueOf(prc_leng-1) + "].split(',')[0]");
                        int product_count = 0;
                        try {
                            product_count = Integer.parseInt(product_count_str);

                        }catch(Exception ex){
                            product_count_str = (String) ((JavascriptExecutor) driver).executeScript("return  document.getElementsByClassName('_1EmaQAfujj')[0].textContent");
                            product_count = Integer.parseInt(product_count_str);

                        }
                        while (true) {
                            String newurl = url + "&page=" + page_num;
                            driver.get(newurl);;


                            ArrayList<String> pr_list = new ArrayList<>();
                            ArrayList list1 = (ArrayList)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('-qHwcFXhj0')");
                            ArrayList list2 = (ArrayList)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3S7Ho5J2Ql')");
                            ArrayList list3 = (ArrayList)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1RT6khg3UQ')");
                            ArrayList list4 = (ArrayList)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3zhPg5BV4U')");
                            ArrayList list5 = (ArrayList)((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2txbrXXlXM')");

                            if(list1.size() > 0) {
                                for(int p=0; p < list1.size(); p++) {
                                    String pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('-qHwcFXhj0')[" + p + "].children[0].href");
                                    pr_list.add(pr_href);
                                    product_url_list.add(pr_href);
                                    product_check_num++;


                                    try {
                                        String review_cnt = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('-qHwcFXhj0')[" + p + "].children[1].children[1].textContent");
                                        review_cnt = review_cnt.replaceAll(",", "");
                                        review_sum += Long.parseLong(review_cnt);
                                    }catch(Exception ex){
                                        int s = 0;
                                    }
                                }
                            }
                            if(list2.size() > 0) {
                                for(int p=0; p < list2.size(); p++) {
                                    String pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3S7Ho5J2Ql')[" + p + "].children[0].href");
                                    if(pr_href == null) {
                                        pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3S7Ho5J2Ql')[" + p + "].children[1].href");

                                    }
                                    pr_list.add(pr_href);
                                    product_url_list.add(pr_href);
                                    product_check_num++;
                                }
                            }
                            if(list3.size() > 0) {
                                for(int p=0; p < list3.size(); p++) {
                                    String pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_1RT6khg3UQ')[" + p + "].children[0].href");
                                    pr_list.add(pr_href);
                                    product_url_list.add(pr_href);
                                    product_check_num++;
                                }
                            }
                            if(list4.size() > 0) {
                                for(int p=0; p < list4.size(); p++) {
                                    String pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_3zhPg5BV4U')[" + p + "].children[0].href");
                                    pr_list.add(pr_href);
                                    product_url_list.add(pr_href);
                                    product_check_num++;
                                }
                            }
                            if(list5.size() > 0) {
                                for(int p=0; p < list5.size(); p++) {
                                    String pr_href = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_2txbrXXlXM')[" + p + "].children[0].href");
                                    pr_list.add(pr_href);
                                    product_url_list.add(pr_href);
                                    product_check_num++;
                                }
                            }
                            if (product_check_num >= product_count) {
                                break;
                            }


                            if (pr_list.size() > 0) {
                                page_num++;
                            }

                            if (pr_list.size() == 0) {
                                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                                Thread.sleep(2000);
                            }

                        }





                        System.out.println("완료");
                        break;
                    }catch(Exception ex){
                        ex.printStackTrace();
                        System.out.println("에러2");
                    }



                }


            } else if (url.split("ttps://shopping.naver.com").length > 1) {
                System.out.println("스마트스토어 상세페이지 접속 후 다시 클릭해주세요 (쇼핑윈도는 지원하지 않음)");

            } else {
                System.out.println("스마트스토어 상세페이지 접속 후 다시 클릭해주세요");
            }

            conn.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }


        System.out.println(review_sum);
        return product_url_list;
    }
    public ArrayList<String> getAllProductsUrlFromShop(String url) {

        conn = mysql.initConnect(Conf.NAVER_DB_IP);

        HttpConn httpconn = new HttpConn();
        JSONArray result_array = new JSONArray();
        String store_name = "";
        String store_name_kor = "";
        ArrayList<String> product_urls = new ArrayList<>();

        try {
            url = URLDecoder.decode(url, "UTF-8");
            int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
            int smartstore_leng = url.split("smartstore.naver.com").length;
            int brand_leng = url.split("brand.naver.com").length;
            if(smartstore_leng > 1 || brand_leng > 1) {

                if (url.split("\\?NaPm").length > 1) {
                    url = url.split("\\?NaPm")[0];
                }

                if (smartstore_leng > 1) {
                    String[] suburl_list = url.split("smartstore.naver.com")[1].split("/");
                    if (suburl_list.length > 1) {
                        store_name = suburl_list[1];
                        url = "https://smartstore.naver.com/" + store_name + "/category/ALL?cp=1";
                    } else {

                    }
                }

                if (brand_leng > 1) {
                    String[] suburl_list = url.split("brand.naver.com")[1].split("/");
                    if (suburl_list.length > 1) {
                        store_name = suburl_list[1];
                        url = "https://brand.naver.com/" + store_name + "/category/ALL?cp=1";
                    } else {

                    }
                }

                ArrayList<String> product_url_list = new ArrayList<String>();
                while(true) {
                    try {
                        int page_num = 1;
                        int product_check_num = 0;
                        random_int = (int)Math.floor(Math.random()*(max-min+1)+min);
                        String ua = mysql.selectUserAgent(conn, "select user_agent from useragent where rownum >='" + random_int + "' limit 1");
                        String body = httpconn.sendGet(url, ua);
                        String[] product_count_split = body.split("productCount\":");
                        String product_count_str = body.split("productCount\":")[product_count_split.length - 1].split(",")[0];
                        int product_count = Integer.parseInt(product_count_str);

                        while (true) {
                            String newurl = url + "&page=" + page_num;
                            String web_data = httpconn.sendGet(newurl, ua);
                            Document doc = Jsoup.parse(web_data);

                            ArrayList<Element> pr_list = new ArrayList<>();
                            Elements li_list = doc.getElementsByTag("li");
                            for (Element elm : li_list) {
                                if (elm.hasClass("-qHwcFXhj0") || elm.hasClass("_3S7Ho5J2Ql") || elm.hasClass("_1RT6khg3UQ")) {
                                    pr_list.add(elm);
                                }
                            }

                            for (int i = 0; i < pr_list.size(); i++) {
                                String pr_url = ((Elements) ((Element) pr_list.get(i)).getElementsByTag("a")).first().attr("href");
                                product_url_list.add("https://smartstore.naver.com" + pr_url);
                                product_check_num++;
                            }

                            if (product_check_num >= product_count) {
                                break;
                            }
                            if (pr_list.size() > 0) {
                                page_num++;
                            }
                            if (pr_list.size() == 0) {
                                random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                                ua = mysql.selectUserAgent(conn, "select user_agent from useragent where rownum >='" + random_int + "' limit 1");
                            }

                        }
                        for (int j = 0; j < product_url_list.size(); j++) {
                            while (true) {
                                try {

                                    String productUrl = product_url_list.get(j);

                                    product_urls.add(productUrl);

                                    break;

                                } catch (Exception ex) {
                                    System.out.println(ex.toString());
                                    random_int = (int) Math.floor(Math.random() * (max - min + 1) + min);
                                }
                            }
                        }
                        System.out.println("완료");
                        break;
                    }catch(Exception ex){
                        ex.printStackTrace();
                        System.out.println("에러2");
                    }



                }
            } else if (url.split("ttps://shopping.naver.com").length > 1) {
                System.out.println("스마트스토어 상세페이지 접속 후 다시 클릭해주세요 (쇼핑윈도는 지원하지 않음)");

            } else {
                System.out.println("스마트스토어 상세페이지 접속 후 다시 클릭해주세요");
            }

            conn.close();
        }catch(Exception e){
            System.out.println(e.toString());
        }

        return product_urls;
    }

    /**
     * 내 상품 및 경쟁사 데이터 수집해서 report 테이블에 저장
     *
     */
    public void makeReport(String part_number, String total_numbers, String product_url) {


        ChromeDriver driver = DriverControl.getGeneralDriver();
        ArrayList<String> pr_list = getAllProductsUrlFromShop(driver, product_url);
        ArrayList keyword_group = KeywordGroupMaker(driver, pr_list);

        int slice_counts = Math.round(keyword_group.size() / Integer.parseInt(total_numbers));
        int end_point = slice_counts * Integer.parseInt(part_number);
        int starting_point = end_point - slice_counts;

        ArrayList keyword_group_part = new ArrayList();
        for (int p = starting_point; p < end_point; p++) {
            keyword_group_part.add(keyword_group.get(p));
        }

        HashMap<String, HashMap> enemy_shops = getShoppingmallGroup(driver, keyword_group_part);

        for (String key : enemy_shops.keySet()) {
            String url = "https://smartstore.naver.com/main/products/" + enemy_shops.get(key).get("product_no");
            pa.executeProcess(url, null, "false","not-quit");

            try {
                conn = mysql2.initConnect(Conf.NAVER_DB_IP);
                mysql2.insertReport(conn, "insert into report(product_no, shop_no, store_name) values(?,?,?)", key, (String) enemy_shops.get(key).get("mall_no"), (String) enemy_shops.get(key).get("store_name"));
                conn.close();
                System.out.println("입력완료:" + enemy_shops.get(key).get("product_no"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }


    /**
     * 샵에 있는 모든 상품 데이터 정보 merge
     *
     */
    public void mergeAllProductsInShop(String product_url) {
        ChromeDriver driver = DriverControl.getGeneralDriver();
        Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);

        ArrayList<String> pr_list = getAllProductsUrlFromShop(driver, product_url);

        JSONObject years_revenue_obj = new JSONObject();
        double growth = 0.0;
        double growth_3m = 0.0;
        double growth_6m = 0.0;
        double growth_12m = 0.0;

        double growth_enemy = 0.0;
        double growth_enemy_3m = 0.0;
        double growth_enemy_6m = 0.0;
        double growth_enemy_12m = 0.0;


        Double revenue = 0.0;
        Double revenue_3m = 0.0;
        Double revenue_6m = 0.0;
        Double revenue_12m = 0.0;


        Double revenue_enemy = 0.0;
        Double revenue_enemy_3m = 0.0;
        Double revenue_enemy_6m = 0.0;
        Double revenue_enemy_12m = 0.0;


        Double category_growth_recent12m = -999.0;

        JSONObject categories = new JSONObject();
        JSONObject categories_revenue = new JSONObject();
        JSONObject enemies = new JSONObject();
        JSONObject revenue_sort = new JSONObject();
        JSONObject enemies_filtered = new JSONObject();
        JSONObject enemies_revenue = new JSONObject();
        JSONObject enemies_revenue3m = new JSONObject();
        JSONObject enemies_revenue6m = new JSONObject();
        JSONObject enemies_revenue12m = new JSONObject();
        JSONObject enemies_revenue_graph = new JSONObject();
        JSONObject enemy_revenue_graph_total = new JSONObject();

        JSONObject product_revenue = new JSONObject();


        JSONObject enemies_product = new JSONObject();

        JSONArray monthly_revenues = new JSONArray();
        JSONObject category_click_graph = new JSONObject();
        JSONObject category_recent12m_growth_graph = new JSONObject();


        long review_count = 0;
        String seller_name_this = "";
        String store_name_this = "";
        JSONObject years_revenue_obj_enemy = new JSONObject();


        String thumbnail_img = "https://img-shop.pstatic.net/storefarm/front/common/noimg/no_img_seller_60x60.jpg";
        String description = "";

        for (int h = 0; h < 12; h++) {
            monthly_revenues.add(0);
        }

        try {
            try {
                String[] suburl_list = product_url.split("smartstore.naver.com")[1].split("/");
                if (suburl_list.length > 1) {
                    String st = suburl_list[1];
                    driver.get("https://smartstore.naver.com/" + st + "/category/ALL?cp=1");
                }
                thumbnail_img = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[property=\"og:image\"]')[0].content");
                description = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[name=\"description\"]')[0].content");
            } catch (Exception ex) {

            }

            for (int i = 0; i < pr_list.size(); i++) {
                String this_url = pr_list.get(i);
                driver.get(this_url);
                String url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                String product_no = url.split("products/")[1];

                ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");

                try {
                    JSONObject data_obj = new JSONObject();

                    if (arr.size() == 0) {
                        data_obj = pa.executeProcess(this_url, null, "enemy", "not-quit");
                        arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "'");
                    }


                    HashMap hm = arr.get(0);
                    String data_str = (String) hm.get("data");
                    data_obj = (JSONObject) parser.parse(data_str);
                    String category = (String) hm.get("cat_full");
                    if (categories.containsKey(category)) {
                        categories.put(category, (int) categories.get(category) + 1);
                    } else {
                        categories.put(category, 1);
                    }

                    if (categories_revenue.containsKey(category)) {
                        categories_revenue.put(category, (Double) categories_revenue.get(category) + Double.parseDouble((String) hm.get("revenue")));
                    } else {
                        categories_revenue.put(category, Double.parseDouble((String) hm.get("revenue")));
                    }

                    if (product_revenue.containsKey(product_no)) {
                        JSONObject product_obj = (JSONObject) product_revenue.get(product_no);
                        product_obj.put("매출", (double) product_obj.get("매출") + Double.parseDouble((String) hm.get("revenue")));
                        product_revenue.put(product_no, product_obj);
                    } else {
                        JSONObject obj = new JSONObject();
                        obj.put("매출", Double.parseDouble((String) hm.get("revenue")));
                        obj.put("썸네일", data_obj.get("image_url"));
                        obj.put("타이틀", data_obj.get("타이틀"));
                        product_revenue.put(product_no, obj);
                    }

                    seller_name_this = (String) data_obj.get("seller_name");
                    if (seller_name_this.equals("")) {
                        seller_name_this = product_url.split("naver.com/")[1];
                    }
                    store_name_this = (String) data_obj.get("스토어명");
                    String review_str = (String) data_obj.get("리뷰수");
                    review_count += (Long) Long.parseLong(review_str.replaceAll(",", ""));

                    String year_str = (String) data_obj.get("연도별매출액");
                    JSONObject data_year_obj = (JSONObject) parser.parse(year_str);
                    years_revenue_obj = Calculator.sumYearRevenue(years_revenue_obj, data_year_obj);


                    revenue += Double.valueOf((String) hm.get("revenue"));
                    revenue_3m += Double.valueOf((String) hm.get("revenue3m"));
                    revenue_6m += Double.valueOf((String) hm.get("revenue6m"));
                    revenue_12m += Double.valueOf((String) hm.get("revenue12m"));
                    JSONArray month_array = (JSONArray) data_obj.get("월별매출그래프");
                    for (int m = 0; m < month_array.size(); m++) {
                        long month_revenue = (Long) Long.parseLong(String.valueOf(monthly_revenues.get(m)));
                        monthly_revenues.set(m, (long) Long.parseLong(String.valueOf(month_array.get(m))) + month_revenue);
                    }
                    ArrayList enemy_group = (ArrayList) data_obj.get("경쟁그룹");

                    if (enemy_group == null) {
                        JSONObject data_obj2 = pa.executeProcess(this_url, null, "enemy", "not-quit");
                        enemy_group = (ArrayList) data_obj2.get("경쟁그룹");
                    }


                    for (int k = 0; k < enemy_group.size(); k++) {
                        JSONObject enemy_obj = (JSONObject) enemy_group.get(k);
                        String seller_name = (String) enemy_obj.get("seller_name");
                        Long price = (Long) Long.parseLong(String.valueOf(enemy_obj.get("가격")));
                        Long reviews_cnt = (Long) Long.valueOf(String.valueOf(enemy_obj.get("리뷰")));
                        String keyword = (String) enemy_obj.get("keyword");
                        String reason = keyword + "(" + product_no + ")";

                        if (!enemies.containsKey(seller_name)) {
                            JSONArray e_arr = new JSONArray();
                            e_arr.add(reason);
                            enemies.put(seller_name, e_arr);
                            revenue_sort.put(seller_name, price * reviews_cnt);

                        } else {
                            JSONArray ee_arr = (JSONArray) enemies.get(seller_name);
                            ee_arr.add(reason);
                            enemies.put(seller_name, ee_arr);
                            revenue_sort.put(seller_name, (Long) revenue_sort.get(seller_name) + price * reviews_cnt);

                        }

                    }


                    System.out.println("전체:" + pr_list.size() + "  완료:" + (i + 1) + "/" + (pr_list.size()));


                } catch (Exception ex) {
                    JSONObject data = pa.executeProcess(this_url, null, "enemy", "not-quit");
                }
            }


            growth = Calculator.getGrowthRateTotal(years_revenue_obj);
            growth_3m = Calculator.getGrowthRate2(years_revenue_obj, 1, 3);
            growth_6m = Calculator.getGrowthRate2(years_revenue_obj, 1, 6);
            growth_12m = Calculator.getGrowthRate2(years_revenue_obj, 1, 12);
            String most_revenue_category = getMostRevenueCategory(categories_revenue);
            ArrayList<String> enemy_list = enemyFilter(revenue_sort);
            for (int j = 0; j < enemy_list.size(); j++) {
                String enemy_name = enemy_list.get(j);
                enemies_filtered.put(enemy_name, enemies.get(enemy_name));
            }
            JSONArray enemy_dataset = new JSONArray();
            long customer_transaction_enemy_sum = 0;
            for (Iterator iterator = enemies_filtered.keySet().iterator(); iterator.hasNext(); ) {
                String seller_name = (String) iterator.next();
                JSONArray enemy_reasons = (JSONArray) enemies_filtered.get(seller_name);
                if (seller_name.equals("")) {
                    continue;
                }
                ArrayList<JSONObject> objs = mysql.selectDatalabPlusMall(conn, "select * from datalab_plus_mall where seller_id='" + seller_name + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                if (objs.size() > 0 && ((String) objs.get(0).get("data") != null)) {
                    JSONObject obj = (JSONObject) parser.parse((String) ((JSONObject) objs.get(0)).get("data"));
                    String most_cat = (String) obj.get("최다매출카테고리");
                    String my_middle = most_revenue_category.split(">")[1];
                    String enemy_middle = most_cat.split(">")[1];
                    if (my_middle.equals(enemy_middle)) {
                        Long revenues = (Long) (obj.get("revenue"));
                        enemies_revenue.put(seller_name, revenues);
                        Long revenue3m = (Long) (obj.get("revenue_3m"));
                        enemies_revenue3m.put(seller_name, revenue3m);
                        Long revenue6m = (Long) (obj.get("revenue_6m"));
                        enemies_revenue6m.put(seller_name, revenue6m);
                        Long revenue12m = (Long) (obj.get("revenue_12m"));
                        enemies_revenue12m.put(seller_name, revenue12m);
                        revenue_enemy += revenues;
                        revenue_enemy_3m += revenue3m;
                        revenue_enemy_6m += revenue6m;
                        revenue_enemy_12m += revenue12m;
                        JSONObject year_revenues = (JSONObject) parser.parse((String) (obj.get("연도별매출액")));
                        years_revenue_obj_enemy = Calculator.sumYearRevenue(years_revenue_obj_enemy, year_revenues);
                        enemies_revenue_graph.put(seller_name, year_revenues.toJSONString());
                        customer_transaction_enemy_sum += (Long) obj.get("객단가");
                        JSONObject light_obj = new JSONObject();
                        light_obj.put("seller_name", obj.get("seller_name"));
                        light_obj.put("seller_id", obj.get("seller_id"));
                        light_obj.put("썸네일", obj.get("썸네일"));
                        light_obj.put("최다매출카테고리", obj.get("최다매출카테고리"));
                        light_obj.put("revenue", obj.get("revenue"));
                        light_obj.put("revenue_12m", obj.get("revenue_12m"));
                        light_obj.put("revenue_6m", obj.get("revenue_6m"));
                        light_obj.put("revenue_3m", obj.get("revenue_3m"));
                        light_obj.put("growth", obj.get("growth"));
                        light_obj.put("객단가", obj.get("객단가"));
                        light_obj.put("선정근거", enemy_reasons.toJSONString());

                        enemy_dataset.add(light_obj);
                    }

                }
                else {

                    ArrayList<String> urls = getAllProductsUrlFromShop(driver, "https://smartstore.naver.com/" + seller_name + "/");
                    String thumbnail_this = "https://img-shop.pstatic.net/storefarm/front/common/noimg/no_img_seller_60x60.jpg";
                    String description_this = "";

                    try {
                        driver.get("https://smartstore.naver.com/" + seller_name + "/");
                        thumbnail_this = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[property=\"og:image\"]')[0].content");
                        description_this = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('meta[name=\"description\"]')[0].content");
                    } catch (Exception ex) {

                    }

                    long revenue12m_this = 0;
                    long revenue3m_this = 0;
                    long revenue6m_this = 0;
                    long revenue_total = 0;
                    JSONObject years_revenue_this = new JSONObject();
                    JSONObject categories_this = new JSONObject();
                    JSONObject categories_revenue_this = new JSONObject();
                    String store_name = "";
                    long review_count_this = 0;
                    JSONObject product_revenue_this = new JSONObject();
                    for (int i = 0; i < urls.size(); i++) {

                        String url = urls.get(i);
                        String product_no = null;
                        JSONObject enemy_obj = new JSONObject();


                        try {
                            product_no = url.split("martstore.naver.com/" + seller_name + "/products/")[1];
                            ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");

                            if (arr.size() == 1) {
                                enemy_obj = (JSONObject) parser.parse(((String) (arr.get(0)).get("data")));
                            }

                            if (arr.size() == 0) {
                                enemy_obj = pa.executeProcess(urls.get(i), null, "enemy-extends", "not-quit");
                            }

                            Double revenue3m = (Double) Double.parseDouble(String.valueOf(enemy_obj.get("revenue3m")));
                            revenue3m_this += revenue3m;
                            Double revenue6m = (Double) Double.parseDouble(String.valueOf(enemy_obj.get("revenue6m")));
                            revenue6m_this += revenue6m;
                            Double revenue12m = (Double) Double.parseDouble(String.valueOf(enemy_obj.get("revenue12m")));
                            revenue12m_this += revenue12m;
                            Double revenues = (Double) Double.parseDouble(String.valueOf(enemy_obj.get("revenue")));
                            revenue_total += revenues;
                            String review_str = (String) enemy_obj.get("리뷰수");
                            review_count_this += (Long) Long.parseLong(review_str.replaceAll(",", ""));
                            JSONObject year_revenues = (JSONObject) parser.parse((String) (enemy_obj.get("연도별매출액")));
                            years_revenue_this = Calculator.sumYearRevenue(years_revenue_this, year_revenues);
                            years_revenue_obj_enemy = Calculator.sumYearRevenue(years_revenue_obj_enemy, year_revenues);
                            store_name = (String) enemy_obj.get("스토어명");


                            revenue_enemy += revenues;
                            revenue_enemy_3m += revenue3m;
                            revenue_enemy_6m += revenue6m;
                            revenue_enemy_12m += revenue12m;

                            HashMap hm = arr.get(0);
                            String category = (String) hm.get("cat_full");
                            if (categories_this.containsKey(category)) {
                                categories_this.put(category, (int) categories_this.get(category) + 1);
                            } else {
                                categories_this.put(category, 1);
                            }
                            if (categories_revenue_this.containsKey(category)) {
                                categories_revenue_this.put(category, (Double) categories_revenue_this.get(category) + revenues);
                            } else {
                                categories_revenue_this.put(category, revenues);
                            }

                            if (product_revenue_this.containsKey(product_no)) {
                                JSONObject product_obj = (JSONObject) product_revenue_this.get(product_no);
                                product_obj.put("매출", (double) product_obj.get("매출") + revenues);
                                product_revenue_this.put(product_no, product_obj);

                            } else {
                                JSONObject obj = new JSONObject();
                                obj.put("매출", revenues);
                                obj.put("썸네일", enemy_obj.get("image_url"));
                                obj.put("타이틀", enemy_obj.get("타이틀"));
                                product_revenue_this.put(product_no, obj);
                            }


                        } catch (Exception ex) {

                        }

                    }


                    String most_revenue_category_enemy = getMostRevenueCategory(categories_revenue_this);
                    String my_middle = most_revenue_category.split(">")[1];
                    String enemy_middle = most_revenue_category_enemy.split(">")[1];

                    if (my_middle.equals(enemy_middle)) {

                        revenue_enemy += revenue_total;
                        revenue_enemy_3m += revenue3m_this;
                        revenue_enemy_6m += revenue6m_this;
                        revenue_enemy_12m += revenue12m_this;

                        long customer_transaction_enemy = Math.round(revenue_total / (review_count_this * 3.5) * 1.0);
                        customer_transaction_enemy_sum += customer_transaction_enemy;
                        enemies_revenue3m.put(seller_name, revenue3m_this);
                        enemies_revenue6m.put(seller_name, revenue6m_this);
                        enemies_revenue12m.put(seller_name, revenue12m_this);
                        enemies_revenue.put(seller_name, revenue_total);
                        enemies_revenue_graph.put(seller_name, years_revenue_this.toJSONString());
                        double growth_this = Calculator.getGrowthRateTotal(years_revenue_this);
                        double growth_this_3m = Calculator.getGrowthRate2(years_revenue_this, 1, 3);
                        double growth_this_6m = Calculator.getGrowthRate2(years_revenue_this, 1, 6);
                        double growth_this_12m = Calculator.getGrowthRate2(years_revenue_this, 1, 12);
                        JSONObject obj = new JSONObject();
                        obj.put("썸네일", thumbnail_this);
                        obj.put("description", description_this);
                        obj.put("seller_id", seller_name);
                        obj.put("seller_name", store_name);
                        obj.put("월별누적매출그래프", Calculator.makeMonthlyRevenue(years_revenue_this));
                        obj.put("시즌별매출분포", Calculator.makeSeasonRevenueRatio(years_revenue_this));
                        obj.put("예상매출회귀분석", Calculator.makeRegressionGraph(years_revenue_this));
                        obj.put("객단가", customer_transaction_enemy);
                        obj.put("객단가콤마", NumberFormat.getInstance().format(Math.round(customer_transaction_enemy)));
                        obj.put("revenue", revenue_total);
                        obj.put("revenue_3m", revenue3m_this);
                        obj.put("revenue_6m", revenue6m_this);
                        obj.put("revenue_12m", revenue12m_this);
                        obj.put("revenue_comma", NumberFormat.getInstance().format(Math.round(revenue_total)));
                        obj.put("revenue_3m_comma", NumberFormat.getInstance().format(Math.round(revenue3m_this)));
                        obj.put("revenue_6m_comma", NumberFormat.getInstance().format(Math.round(revenue6m_this)));
                        obj.put("revenue_12m_comma", NumberFormat.getInstance().format(Math.round(revenue12m_this)));
                        obj.put("연도별매출액", years_revenue_this.toJSONString());
                        obj.put("growth", growth_this);
                        obj.put("growth_3m", growth_this_3m);
                        obj.put("growth_6m", growth_this_6m);
                        obj.put("growth_12m", growth_this_12m);
                        obj.put("카테고리빈도수", categories_this.toJSONString());
                        obj.put("카테고리빈도수매출", categories_revenue_this.toJSONString());
                        obj.put("상품별매출", product_revenue_this.toJSONString());
                        obj.put("최다매출카테고리", most_revenue_category_enemy);
                        JSONObject light_obj = new JSONObject();
                        light_obj.put("seller_name", obj.get("seller_name"));
                        light_obj.put("seller_id", obj.get("seller_id"));
                        light_obj.put("썸네일", obj.get("썸네일"));
                        light_obj.put("최다매출카테고리", obj.get("최다매출카테고리"));
                        light_obj.put("revenue", obj.get("revenue"));
                        light_obj.put("revenue_12m", obj.get("revenue_12m"));
                        light_obj.put("revenue_6m", obj.get("revenue_6m"));
                        light_obj.put("revenue_3m", obj.get("revenue_3m"));
                        light_obj.put("growth", obj.get("growth"));
                        light_obj.put("객단가", obj.get("객단가"));
                        light_obj.put("선정근거", enemy_reasons.toJSONString());
                        enemy_dataset.add(light_obj);
                        mysql.insertDatalabPlusMall2(conn, "insert into datalab_plus_mall(seller_id,seller_name,data) values(?,?,?)", seller_name, store_name, obj.toJSONString());
                    }


                }
            }

            growth_enemy = Calculator.getGrowthRateTotal(years_revenue_obj_enemy);
            growth_enemy_3m = Calculator.getGrowthRate2(years_revenue_obj_enemy, 1, 3);
            growth_enemy_6m = Calculator.getGrowthRate2(years_revenue_obj_enemy, 1, 6);
            growth_enemy_12m = Calculator.getGrowthRate2(years_revenue_obj_enemy, 1, 12);
            JSONObject enemy12m = enemies_revenue12m;
            enemy12m.put(seller_name_this, revenue_12m);
            HashMap<String, Long> cr3hhi_12m = Calculator.getCR3andHHI(enemy12m);
            long customer_transaction = Math.round(revenue / (review_count * 3.5) * 1.0);
            most_revenue_category = getMostRevenueCategory(categories_revenue);
            long customer_transaction_enemy = Math.round(customer_transaction_enemy_sum * 1.0 / enemies_filtered.size() * 1.0);
            JSONObject result_final = new JSONObject();
            JSONObject result_final2 = new JSONObject();
            result_final.put("썸네일", thumbnail_img);
            result_final.put("description", description);
            result_final.put("seller_id", seller_name_this);
            result_final.put("seller_name", store_name_this);
            result_final.put("revenue", revenue);
            result_final.put("revenue_3m", revenue_3m);
            result_final.put("revenue_6m", revenue_6m);
            result_final.put("revenue_12m", revenue_12m);
            result_final.put("revenue_comma", NumberFormat.getInstance().format(Math.round(revenue)));
            result_final.put("revenue_3m_comma", NumberFormat.getInstance().format(Math.round(revenue_3m)));
            result_final.put("revenue_6m_comma", NumberFormat.getInstance().format(Math.round(revenue_6m)));
            result_final.put("revenue_12m_comma", NumberFormat.getInstance().format(Math.round(revenue_12m)));
            result_final.put("연도별매출액", years_revenue_obj.toJSONString());
            result_final.put("growth", growth);
            result_final.put("growth_3m", growth_3m);
            result_final.put("growth_6m", growth_6m);
            result_final.put("growth_12m", growth_12m);
            result_final.put("객단가", customer_transaction);
            result_final.put("객단가콤마", NumberFormat.getInstance().format(Math.round(customer_transaction)));
            result_final.put("카테고리빈도수", categories.toJSONString());
            result_final.put("카테고리빈도수매출", categories_revenue.toJSONString());
            result_final.put("최다매출카테고리", most_revenue_category);
            result_final.put("상품별매출", product_revenue.toJSONString());
            JSONArray month_revenue_obj = Calculator.makeMonthlyRevenue(years_revenue_obj);
            result_final.put("월별누적매출그래프", month_revenue_obj);
            JSONObject season_obj = Calculator.makeSeasonRevenueRatio(years_revenue_obj);
            result_final.put("시즌별매출분포", season_obj);
            JSONArray regression_obj = Calculator.makeRegressionGraph(years_revenue_obj);
            result_final.put("예상매출회귀분석", regression_obj);
            result_final2.put("revenue_enemy", revenue_enemy);
            result_final2.put("revenue_enemy_3m", revenue_enemy_3m);
            result_final2.put("revenue_enemy_6m", revenue_enemy_6m);
            result_final2.put("revenue_enemy_12m", revenue_enemy_12m);
            result_final2.put("revenue_enemy_comma", NumberFormat.getInstance().format(Math.round(revenue_enemy)));
            result_final2.put("revenue_enemy_3m_comma", NumberFormat.getInstance().format(Math.round(revenue_enemy_3m)));
            result_final2.put("revenue_enemy_6m_comma", NumberFormat.getInstance().format(Math.round(revenue_enemy_6m)));
            result_final2.put("revenue_enemy_12m_comma", NumberFormat.getInstance().format(Math.round(revenue_enemy_12m)));
            result_final2.put("경쟁사별매출전체", enemies_revenue.toJSONString());
            result_final2.put("경쟁사별매출12개월", enemies_revenue12m.toJSONString());
            result_final2.put("경쟁사별연도별매출액", enemies_revenue_graph.toJSONString());
            result_final2.put("경쟁사연도별매출액토탈", years_revenue_obj_enemy.toJSONString());
            result_final2.put("경쟁사성장률", growth_enemy);
            result_final2.put("경쟁사성장률3개월", growth_enemy_3m);
            result_final2.put("경쟁사성장률6개월", growth_enemy_6m);
            result_final2.put("경쟁사성장률12개월", growth_enemy_12m);
            result_final2.put("CR312개월", cr3hhi_12m.get("cr3"));
            result_final2.put("HHI12개월", cr3hhi_12m.get("hhi"));
            result_final2.put("객단가경쟁사", customer_transaction_enemy);
            result_final2.put("CR312개월_콤마", NumberFormat.getInstance().format(Math.round(cr3hhi_12m.get("cr3"))));
            result_final2.put("HHI12개월_콤마", NumberFormat.getInstance().format(Math.round(cr3hhi_12m.get("hhi"))));
            result_final2.put("객단가경쟁사_콤마", NumberFormat.getInstance().format(Math.round(customer_transaction_enemy)));
            result_final2.put("경쟁사리스트", enemy_dataset.toJSONString());

            Connection new_conn = mysql.initConnect(Conf.NAVER_DB_IP);
            mysql.insertDatalabPlusMall(new_conn, "insert into datalab_plus_mall(seller_id,seller_name,data,enemy) values(?,?,?,?)", seller_name_this, store_name_this, result_final.toJSONString(), result_final2.toJSONString());
            new_conn.close();
            conn.close();
            DriverControl.quitDriver(driver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 상품 데이터 매출 합산해서 가장 많이 차지하는 카테고리 구하기
     *
     */
    public String getMostRevenueCategory(JSONObject category_obj) {

        double max = -1;
        String most = "";


        for (Iterator iterator = category_obj.keySet().iterator(); iterator.hasNext(); ) {
            String category_name = (String) iterator.next();
            double value = Double.parseDouble(String.valueOf(category_obj.get(category_name)));
            if(value >= max){
                max = value;
                most = category_name;
            }
        }

        return most;
    }

    /**
     * 샵에 있는 모든 상품 데이터 정보 update
     *
     */
    public void updateAllProductsInShop(String part_number, String total_numbers, String product_url) {
        ChromeDriver driver = DriverControl.getGeneralDriver();
        ArrayList<String> pr_list = getAllProductsUrlFromShop(driver, product_url);
        int slice_counts = Math.round(pr_list.size() / Integer.parseInt(total_numbers));
        int end_point = slice_counts * Integer.parseInt(part_number);
        int starting_point = end_point - slice_counts;
        if(pr_list.size()-end_point < slice_counts) {
            end_point = pr_list.size();
        }
        rec.insertUpdateList(this.update_type, this.update_name,"타겟몰 전체 업데이트를 시작합니다",0, end_point-starting_point);

        for(int i=starting_point; i< end_point; i++){
            String this_url = pr_list.get(i);
            driver.get(this_url);
            String url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            String product_no = url.split("products/")[1];
            ArrayList arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -1 DAY)");

            if(arr.size() == 0) {
                pa.executeProcess(this_url, null, "enemy-ifnotexist", "not-quit");
                rec.selectAndPlucCountUpdateList(this.update_name, "타겟몰 상품 데이터 (" + this_url + ") 분석 완료");
            }
            System.out.println("전체:" +  pr_list.size() + "  완료:" +  (i+1) + "/" + (end_point));
        }
        DriverControl.quitDriver(driver);

    }


    /**
     * 경쟁사 리스트 구하기
     *
     */
    public ArrayList<String> getEnemyListFromUrl(ArrayList<String> urls) {

        ArrayList<String> enemy_urls = new ArrayList<>();
        HashMap<String,Integer> enemy_checker = new HashMap<>();
        JSONObject revenue_sort = new JSONObject();
        try {
            Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
            for (int t = 0; t < urls.size(); t++) {
                String url = urls.get(t);
                String product_no = url.split("products/")[1];
                JSONObject data_obj = new JSONObject();
                ArrayList enemy_group = new ArrayList();
                ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");

                if(arr.size() == 0) {
                    data_obj = pa.executeProcess(url,null, "enemy-ifnotexist","quit");
                }
                if (arr.size() > 0) {
                    try {
                        HashMap hm = arr.get(0);
                        String data_str = (String) hm.get("data");
                        data_obj = (JSONObject) parser.parse(data_str);
                        try {
                            enemy_group = (ArrayList) data_obj.get("경쟁그룹");
                            if (enemy_group.size() == 0) {
                                data_obj = pa.executeProcess(url, null, "enemy-ifnotexist", "quit");
                                enemy_group = (ArrayList) data_obj.get("경쟁그룹");
                            }
                        }catch(Exception ex){
                            data_obj = pa.executeProcess(url, null, "enemy-ifnotexist", "quit");
                            enemy_group = (ArrayList) data_obj.get("경쟁그룹");
                        }

                    } catch (Exception ex) {

                    }
                }
                try {
                    for (int k = 0; k < enemy_group.size(); k++) {

                        JSONObject enemy_obj = (JSONObject) enemy_group.get(k);
                        String seller_name = (String) enemy_obj.get("seller_name");
                        Long price = (Long) Long.parseLong(String.valueOf(enemy_obj.get("가격")));
                        Long reviews_cnt = (Long) Long.valueOf(String.valueOf(enemy_obj.get("리뷰")));
                        String keyword = (String) enemy_obj.get("keyword");


                        if (!enemy_checker.containsKey(seller_name)) {
                            enemy_checker.put(seller_name, 1);
                            revenue_sort.put(seller_name, price * reviews_cnt);

                        } else {
                            revenue_sort.put(seller_name, (Long) revenue_sort.get(seller_name) + price * reviews_cnt);
                        }

                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
            ArrayList<String> filtered_enemy_list = enemyFilter(revenue_sort);
            ChromeDriver dr = DriverControl.getGeneralDriver();
            for(int j=0; j < filtered_enemy_list.size(); j++) {
                String enemy_name = filtered_enemy_list.get(j);
                ArrayList<String> enemy_urls_all = getAllProductsUrlFromShop(dr,"https://smartstore.naver.com/" + enemy_name);
                enemy_urls.addAll(enemy_urls_all);
            }
            dr.quit();
            conn.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return enemy_urls;

    }


    /**
     * 경쟁사 리스트 업데이트
     *
     */
    public void updateDplusEnemyList(ArrayList url_list) {

        SellerAnalysis rm = new SellerAnalysis();
        ArrayList enemy_urls = rm.getEnemyListFromUrl(url_list);
        for(int i=0; i< enemy_urls.size(); i++){
            try {
                String enemy_url = (String) enemy_urls.get(i);
                String product_no = (String) enemy_url.split("products/")[1];
                ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                if(arr.size() > 0) {
                    continue;
                }

            }catch(Exception ex){

            }
            pa.executeProcess((String)enemy_urls.get(i), null, "enemy-extends","quit");
        }
    }


    public void makeKeywordReport(String product_url) {

        KeywordExposed dp = new KeywordExposed();
        ChromeDriver driver = DriverControl.getGeneralDriver();
        ArrayList<String> pr_list = getAllProductsUrlFromShop(driver, product_url);
        ArrayList keyword_group = KeywordGroupMaker(driver, pr_list);
        dp.getKeywordAdEffeciencyManyKeywords(keyword_group);

    }
    public void mergeProductToMallWithMultiThread(String seller_id, String update_type){


        MultiExecutors multi_enemy = new MultiExecutors();
        MultiExecutors multi_myurl = new MultiExecutors();
        Recorder rec = new Recorder();
        int split_number =  15; //Runtime.getRuntime().availableProcessors();
        int my_url_threds = 3;
        this.update_name = seller_id;
        this.update_type = update_type;
        String target_url = "https://smartstore.naver.com/" + seller_id;
        ChromeDriver driver =DriverControl.getGeneralDriver();
        ArrayList my_urls = getAllProductsUrlFromShop(driver, target_url);
        DriverControl.quitDriver(driver);
        rec.insertUpdateList(update_type, seller_id,"내 쇼핑몰 업데이트 준비 중...",0,my_urls.size());
        for(int i=1; i <=split_number; i++){
            multi_myurl.executeProcessMultiThread(my_urls, String.valueOf(i),String.valueOf(my_url_threds),"enemy-ifnotexist", i);
        }


        multi_myurl.setCallback(new MultiExecutors.CallBack() {

            @Override
            public void onFinish(){
                multi_myurl.setFinishFlag();
                multi_myurl.finish();
                rec.updateMessage(update_name, "전체 쇼핑몰 업데이트 완료. 이제 경쟁사 URL을 수집중...");
                ArrayList enemy_urls = getEnemyListFromUrl(my_urls);
                rec.insertUpdateList(update_type, seller_id,"타겟몰 경쟁사 정보 불러오기중...",0,enemy_urls.size());
                for(int i=1; i <=split_number; i++){
                    multi_enemy.executeProcessMultiThread(enemy_urls, String.valueOf(i),String.valueOf(split_number),"enemy-extends", i);
                }
                multi_enemy.setCallback(new MultiExecutors.CallBack() {

                    @Override
                    public void onFinish(){
                        multi_enemy.setFinishFlag();
                        multi_enemy.finish();
                        rec.updateMessage(seller_id, "[완료] 모든 경쟁상품 분석완료. 몰단위 머징 작업 시작중...");
                        mergeAllProductsInShop("https://smartstore.naver.com/" + seller_id + "/");
                        rec.updateMessage(seller_id, "[최종완료] 머징 작업 완료");
                        rec.finishUpdateList(seller_id);
                        rec.notifyToServer(Conf.SERVICE_SERVER_IP + "/update/complete", update_name);
                    }
                    @Override
                    public void onGetMessage(MultiExecutors multi) {
                        String enemy_product_no = multi.result_str;
                        int total_checked_count = multi.total_count;
                        JSONArray arr = rec.updateAndSelect(seller_id, "[분석완료] 경쟁사상품번호 : " +enemy_product_no, total_checked_count);

                        if(arr.size() > 0) {
                            JSONObject obj = (JSONObject) arr.get(0);
                            int total_count = (int) obj.get("total_count");
                            int success_count = (int) obj.get("success_count");
                            success_count++;


                        }

                    }
                });

            }

            @Override
            public void onGetMessage(MultiExecutors multi) {
                String my_product_no = multi.result_str;
                int total_checked_count = multi.total_count;
                JSONArray arr = rec.updateAndSelect(seller_id, "[분석완료] 상품번호 : " +my_product_no, total_checked_count);

            }
        });

    }
    public ArrayList<String> enemyFilter(JSONObject revenue_sort) {

        double sum = 0.0;
        double avg_rev = 0.0;
        ArrayList revenue_sort_array = new ArrayList();
        ArrayList<String> enemies_filtered = new ArrayList();

        for (Iterator iterator = revenue_sort.keySet().iterator(); iterator.hasNext(); ) {
            JSONObject tempobj = new JSONObject();
            String seller_name = (String) iterator.next();
            tempobj.put("seller_name", seller_name);
            tempobj.put("displayed_revenue", revenue_sort.get(seller_name));
            revenue_sort_array.add(tempobj);
            sum += (Long) revenue_sort.get(seller_name) * 1.0;
        }
        avg_rev = sum / revenue_sort_array.size() * 1.0;
        try {
            Collections.sort(revenue_sort_array, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    Long v1 = (Long) o1.get("displayed_revenue");
                    Long v3 = (Long) o2.get("displayed_revenue");
                    return v3.compareTo(v1);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(revenue_sort_array);
        for (int f = 0; f < revenue_sort_array.size(); f++) {
            JSONObject obj = (JSONObject) revenue_sort_array.get(f);
            String selected_seller_name = (String) obj.get("seller_name");
            Long selected_seller_revenue = (Long) obj.get("displayed_revenue");

            if (selected_seller_revenue >= avg_rev) {
                enemies_filtered.add(selected_seller_name);
            }

        }

        return enemies_filtered;
    }














}