package Util;

import org.json.simple.JSONObject;

public class Mcode {

    public String product_list_length;
    public String product_title;
    public String product_price;
    public String product_url;
    public String product_no;
    public String product_review_count;
    public String product_buy_count;
    public String product_img;
    public String cat_big;
    public String cat_middle;
    public String cat_small;
    public String cat_product;
    public String seller_name;
    public String seller_url;
    public String product_star;
    public String seller_grade_code;
    public String product_comparison_list;
    public String ad_url;
    public String rank;
    public String product_name;
    public String register_date;
    public String total_products_count;


    public void initNaverList(int k){
        product_list_length = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list.length";
        product_title = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.productTitle";
        product_price = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.price";
        product_url = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.mallProductUrl";
        product_no = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.mallProductId";
        product_review_count = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.reviewCount";
        product_buy_count = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.purchaseCnt";
        seller_name = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.mallName";
        seller_url = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.mallPcUrl";
        product_star = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.scoreInfo";
        seller_grade_code = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.mallInfoCache.mallGrade";
        product_comparison_list = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.lowMallList";
        ad_url = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.adcrUrl";
        rank = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.rank";
        cat_big = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.category1Name";
        cat_middle = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.category2Name";
        cat_small= "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.category3Name";;
        cat_product="return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.category4Name";
        product_img = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.imageUrl";
        product_name = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.productName";
        register_date = "return window.__NEXT_DATA__.props.pageProps.initialState.products.list[" + k + "].item.openDate";
        total_products_count = "return document.getElementsByClassName('subFilter_num__2x0jq')[0].textContent";
    }



    public String getSellerGrade(String grade_code){

        String store_grade = null;
        if(grade_code.equals("M44006")) { store_grade = "씨앗"; }
        if(grade_code.equals("M44005")) { store_grade = "새싹"; }
        if(grade_code.equals("M44004")) { store_grade = "파워"; }
        if(grade_code.equals("M44003")) { store_grade = "빅파워"; }
        if(grade_code.equals("M44002")) { store_grade = "프리미엄"; }
        if(grade_code.equals("M44001")) { store_grade = "플래티넘"; }

        return store_grade;
    }


}
