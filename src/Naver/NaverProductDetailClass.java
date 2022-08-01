package Naver;

public class NaverProductDetailClass <T,I,D,L,TS> {

    public T product_no;
    public T product_url;
    public T cat_big;
    public T cat_middle;
    public T cat_small;
    public T cat_product;
    public T title;
    public T title_sub;
    public T info_flag;
    public I limit_time;
    public I discount;
    public I price;
    public I purchase;
    public I store_zzim;
    public I toktok_friends;
    public T product_img;
    public D star_avg;
    public D star_5;
    public D star_4;
    public D star_3;
    public D star_2;
    public D star_1;
    public I count_review;
    public I count_like;
    public I count_qna;
    public T store_name;
    public T store_company;
    public T store_email;
    public T store_address;
    public T store_phone;
    public T store_url;
    public D store_star;
    public T made_country;
    public T halbu;
    public T insert_time;
    public T user_update_timestamp;
    public TS datalab_update_timestamp;
    public I photo_review;
    public T register_date;
    public T cat_full;
    public L insert_timestamp;
    public L register_timestamp;
    public T data_review;
    public I category_comparison;
    public T firstpage_keyword;
    public I deletes;
    public I id;
    public NaverProductDetailClass(){};

    public void setInsertTIme(T inserttime){
        this.insert_time = inserttime;
    }

    public void setProductInfo(
            T product_no,
            T product_url,
            T cat_big,
            T cat_middle,
            T cat_small,
            T cat_product,
            T title,
            T title_sub,
            T info_flag,
            I limit_time,
            I discount,
            I price,
            I purchase, I store_zzim, I toktok_friends) {
        this.product_no = product_no;
        this.product_url = product_url;
        this.cat_big = cat_big;
        this.cat_middle= cat_middle;
        this.cat_small =cat_small;
        this.cat_product =cat_product;
        this.title= title;
        this.title_sub= title_sub;
        this.info_flag = info_flag;
        this.limit_time = limit_time;
        this.discount =discount;
        this.price= price;
        this.purchase = purchase;
        this.store_zzim = store_zzim;
        this.toktok_friends = toktok_friends;
    }

    public void setProductInfo2(
            T product_no,
            T product_url,
            T product_img,
            T cat_big,
            T cat_middle,
            T cat_small,
            T cat_product,
            T title,
            T title_sub,
            T info_flag,
            I discount,
            I price,
            T register_date) {
        this.product_no = product_no;
        this.product_url = product_url;
        this.product_img = product_img;
        this.cat_big = cat_big;
        this.cat_middle= cat_middle;
        this.cat_small =cat_small;
        this.cat_product =cat_product;
        this.title= title;
        this.title_sub= title_sub;
        this.info_flag = info_flag;
        this.discount =discount;
        this.price= price;
        this.register_date = register_date;
    }
    public void setSellerAndDetailInfo(
            D star_avg,
            D star_5,
            D star_4,
            D star_3,
            D star_2,
            D star_1,
            I count_review,
            I photo_review,
            I count_like,
            I count_qna,
            I store_zzim,
            I toktok_friends,
            T store_name,
            T store_company,
            T store_email,
            T store_address,
            T store_phone,
            T store_url,
            D store_star,
            T made_country,
            T halbu)
    {
        this.star_avg = star_avg;
        this.star_5 = star_5;
        this.star_4 = star_4;
        this.star_3=star_3;
        this.star_2=star_2;
        this.star_1=star_1;
        this.count_review = count_review;
        this.count_like = count_like;
        this.count_qna = count_qna;
        this.store_zzim = store_zzim;
        this.toktok_friends = toktok_friends;
        this.store_name = store_name;
        this.store_company= store_company;
        this.store_email = store_email;
        this.store_address= store_address;
        this.store_phone = store_phone;
        this.store_url = store_url;
        this.store_star = store_star;
        this.made_country = made_country;
        this.halbu = halbu;
        this.photo_review = photo_review;
    }

    public void setCatFull(T cat_full){
        this.cat_full = cat_full;
    }

    public void setTimestamp(L insert_timestamp, L register_timestamp){
        this.insert_timestamp = insert_timestamp;
        this.register_timestamp = register_timestamp;
    }
}

