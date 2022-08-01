
import Datalab.CategoryScrapper;
import DatalabPlus.ProductAnalysis;
import DatalabPlus.SellerAnalysis;
import Naver.*;
import Util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.ArrayList;

/**
 * TODO:프로그램 실행전, Conf에서 먼저 DB IP 및 윈도우 디렉토리 경로를 수정
 *
 * 데이터랩플러스 - 스마트스토어 상품 데이터 수집 및 분석
 *
 * @author 변지훈 (info@thumbtics.com)
 * @version 1.0
 *
 */
public class Main {

    public static ProcessBuilder builder = new ProcessBuilder("C:/Windows/System32/cmd.exe");

    public static void main(String args[]) {

        String arg1 = args[0];
        String arg2 = args[1];
        String arg3 = args[2];


        /* 데이터랩 플러스 */
        /**
         * 상품 데이터 수집 및 업데이트 (데이터랩플러스)
         * NOTE.단일 특정 상품
         *
         * 1단계 : 입력한 URL 디비에서 조회해보고 없으면 직접 브라우저 띄어서 리뷰 긁어오기
         * 2단계 : 리뷰작업 이후 디비에서 경쟁자정보, 랭킹 정보 조회
         *
         * @param arg1 명령어
         * @param arg2 경쟁사수집여부 (ex. enemy-extends)
         * @param arg3 크롬드라이버 종료여부 (ex. not-quit)
         */
        if(arg1.equals("product-execute")) {
            ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
            dp.executeProcess(arg2, null, arg2, arg3);
        }

        /**
         * 데이터랩에서 키워드 긁어와서 조회한후 1~3페이지 상품 정보 긁어와서 네이버 리뷰 라이트에 저장하고 키워드 해시태그 태이블에 입력(상호 경쟁제품을 알기 위함)
         * 1단계
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg2 전체분할개수
         */
          if (arg1.equals("product-base")) {
            ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
            while(true) {
                dp.baseProcess(arg2, arg3);
            }
        }

        /**
         * 데이터랩플러스 테이블 업데이트
         * 2단계
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg2 전체분할개수
         */
        if(arg1.equals("product-update")) {
            ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
            dp.isHeadless = true;
            while(true) {
                dp.updateProcessDatalabPlus(arg2, arg3);
            }
        }



        /**
         * 네이버라이트 기반 데이터랩플러스 테이블 업데이트
         * 3단계
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg3 전체분할개수
         */
        if(arg1.equals("product-update-naverlight")) {
            ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
            dp.isHeadless = true;
            String special_query = args[3];
            while(true) {
                dp.updateProcessDatalabPlusFromNaverLight(arg2, arg3, special_query);
            }
        }

        /**
         * 데이터랩 키워드 기반 노출 상품 분석 실시
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg3 전체분할개수
         */
        if(arg1.equals("product-update-keywords")) {
            ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
            dp.isHeadless = false;

            while(true) {
                dp.updateProcessDatalabPlusFromDatalabKeywords(arg2,arg3);
            }
        }


        /**
         * 네이버라이트 기반 상품 데이터 업데이트 실시 (멀티프로세싱).
         * @param arg1 명령어
         * @param arg2 시작
         * @param arg3 끝
         * @param arg4 전체분할개수
         *
         * ex. dplus-multi-naverlight 10 20 50
         * 전체 naver-light를 50개 그룹으로 분리 후 10~20 그룹에 해당하는 데이터 업데이트 실시. 총 10개의 프로세스 생성됨
         */
        if(arg1.equals("product-multi-naverlight")) {
            int arg_4 =  Integer.parseInt(args[3]); //Runtime.getRuntime().availableProcessors()-10;
            for(int arg_2=Integer.parseInt(arg2); arg_2 < Integer.parseInt(arg3); arg_2++){
                try {
                    Process p = builder.start();
                    Utils.execCommandSingle("start /high java -jar " + Conf.JAR_PATH + " product-update-naverlight " + arg_2 + " " + arg_4, p, 1000);
                    Utils.execCommandSingle("exit", p, 100);
                    p.destroy();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }


        /**
         * 데이터랩플러스 상품 데이터 분석 실시 (멀티프로세싱)
         * NOTE. 편의상 전체 테이블 데이터가 아닌 키워드 기반으로 업데이트로 변경함
         *
         * @param arg1 명령어
         * @param arg2 시작
         * @param arg3 끝
         * @param arg4 전체분할개수
         *
         * ex. dplus-multi-datalabplus 10 20 50
         * 전체 검색 대상 키워드를 50개 그룹으로 분리 후 10~20 그룹에 해당하는 데이터 업데이트 실시. 총 10개의 프로세스 생성됨
         */
        if(arg1.equals("product-multi-datalabplus")) {
            int arg_4 =  Integer.parseInt(args[3]); //Runtime.getRuntime().availableProcessors()-10;
            for(int arg_2=Integer.parseInt(arg2); arg_2 <=Integer.parseInt(arg3); arg_2++){

                try {
                    Process p = builder.start();
                    //Utils.execCommandSingle("start /high java -jar " + Conf.JAR_PATH + " dplus-update " + arg_2 + " " + arg_4, p, 1000);
                    Utils.execCommandSingle("start /high java -jar " + Conf.JAR_PATH + " product-update-keywords " + arg_2 + " " + arg_4, p, 1000);
                    Utils.execCommandSingle("exit", p, 100);
                    Thread.sleep(60*1000);
                    p.destroy();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }


        /**
         * 데이터랩 테이블 업데이트
         *
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg2 전체분할개수
         */
        if(arg1.equals("product-update-datalab")) {
            try {
                CategoryScrapper c = new CategoryScrapper(Conf.DATALAB_IP);
                while(true) {
                    c.datalabTotal(arg2, arg3);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }



        /**
         * 셀러(쇼핑몰)에 있는 모든 상품 정보 업데이트
         *
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg3 전체분할개수
         * @param arg4 특정 셀러 url
         */
        if(arg1.equals("seller-update")) {
            SellerAnalysis rm = new SellerAnalysis();
            rm.updateAllProductsInShop(arg2,arg3,args[3]);
        }


        /**
         * 셀러(쇼핑몰)에 있는 모든 상품 정보 합산 및 통계
         * 특정 스마트스토어 URL
         * @param arg1 명령어
         * @param arg2 스토어 URL
         */
        if(arg1.equals("seller-merge")) {
            SellerAnalysis sa = new SellerAnalysis();
            sa.mergeAllProductsInShop(arg2);
        }


        /**
         * 특정 셀러(쇼핑몰)에 있는 모든 상품 정보 합산 및 통계 멀티쓰레드 실시
         * 특정 스마트스토어 URL
         * @param arg1 명령어
         * @param arg2 스토어ID (ex. likalika)
         * @param arg3 업데이트타입 (기본 "셀러")
         */
        if(arg1.equals("seller-multi")) {
            SellerAnalysis sa = new SellerAnalysis();
            sa.mergeProductToMallWithMultiThread(arg2,arg3);
        }


        /**
         * 서버측 분석 요청 모니터링. DB를 계속 SELECT
         * @param arg1 명령어
         */
        if(arg1.equals("update-scheduler")) {

            MultiExecutors mt = new MultiExecutors();
            SellerAnalysis rp = new SellerAnalysis();
            Recorder rec = new Recorder();
            JSONArray array = new JSONArray();
            System.out.println("스케쥴러가 시작되었습니다. 5초 간격으로 요청을 모니터링 합니다");
            while(true) {

                try {
                    array = rec.selectUpdateListAllManual();

                    for (int i = 0; i < array.size(); i++) {
                        JSONObject obj = (JSONObject) array.get(i);
                        String update_name = (String) obj.get("update_name");
                        String update_type = (String) obj.get("update_type");

                        if (update_type.equals("상품(Partial)")) {
                            rec.updateManual("수동접수완료", update_name);
                            String url ="https://smartstore.naver.com/main/products/" + update_name;
                            mt.executeThread(url,"enemy-extends");
                        }
                        if (update_type.equals("상품(Full)")) {
                            rec.updateManual("수동접수완료", update_name);
                            String url ="https://smartstore.naver.com/main/products/" + update_name;
                            mt.executeThread(url,"enemy");
                        }

                        if (update_type.equals("셀러")) {
                            rec.updateManual("수동접수완료", update_name);
                            rp.mergeProductToMallWithMultiThread(update_name, "셀러");
                        }
                    }
                    Thread.sleep(1000* 5);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }


        /**
         * 특정 셀러(쇼핑몰)의 경쟁상품 업데이트
         * 특정 스마트스토어 URL
         * @param arg1 명령어
         * @param arg2 시작지점
         * @param arg3 전체분할개수
         * @param arg4 셀러 URL
         */
        if(arg1.equals("seller-update-enemy")) {
            SellerAnalysis rm = new SellerAnalysis();

            //내사이트 전체 URL을 구한다
            ChromeDriver driver =DriverControl.getGeneralDriver();
            ArrayList my_urls = rm.getAllProductsUrlFromShop(driver, args[3]);
            DriverControl.quitDriver(driver);

            //경쟁사 URL을 구한다(중복이제거되있음)
            ArrayList enemy_urls = rm.getEnemyListFromUrl(my_urls);

            //경쟁사 url을 분할한다
            int slice_counts = Math.round(enemy_urls.size() / Integer.parseInt(arg3));
            int end_point = slice_counts * Integer.parseInt(arg2);
            int starting_point = end_point - slice_counts;
            if(enemy_urls.size()-end_point < slice_counts) {
                end_point = enemy_urls.size();
            }

            ArrayList splits_urls = new ArrayList();
            for(int i=starting_point; i < end_point; i++) {
                splits_urls.add(enemy_urls.get(i));
            }

            //업데이트를 시작한다
            rm.updateDplusEnemyList(splits_urls);
        }


        /**
         * 상품 성장률 업데이트
         */
        if(arg1.equals("update-growth")) {
            while(true) {
                ProductAnalysis rm = new ProductAnalysis(Conf.NAVER_DB_IP);
                rm.updateGrowth();
            }

        }
        /**
         * 상품 성장률 업데이트 (멀티쓰레드)
         */
        if(arg1.equals("update-growth-que")) {
            while(true) {
                ProductAnalysis rm = new ProductAnalysis(Conf.NAVER_DB_IP);
                rm.updateGrowthQue();
            }

        }
        /**
         * 상품 성장률 인서트
         */
        if(arg1.equals("update-growth-fromdb")) {
            while(true) {
                ProductAnalysis rm = new ProductAnalysis(Conf.NAVER_DB_IP);
                rm.insertGrowth();
            }

        }

        /**
         * 데이터랩 성장률 업데이트
         */
        if(arg1.equals("update-growth-datalab")) {

            try {
                CategoryScrapper cap = new CategoryScrapper(Conf.NAVER_DB_IP);
                cap.getGrowth();
                Thread.sleep(60*1000*60*24);
            }catch(Exception ex){

            }

        }


        /**
         * 추천 상품 추천 타입-1 데이터 캐싱
         *
         * 추천타입1 : 총매출 1억이상 or 3개월매출 3천만이상
         * 전체 datalab_plus 테이블 에서 해당 조건 상품 뽑아서 datalab_plus_cache1 테이블에 저장
         *
         * 슬레이브만 수행함
         */
        if(arg1.equals("cache-cache1")) {
            while(true) {
                Cache cache = new Cache();
                cache.cache1();
            }
        }

        /**
         * datalab_plus 테이블 타이틀 캐싱
         *
         * 스핑크스에서 타이틀 캐싱을 위해 미리 datalab_plus에서 타이틀만 뽑아 놓음
         * (참고: 스핑크스에서 타이틀 캐싱은 앱내 검색 속도 향상을 위해서 수행합니다)
         * 슬레이브만 수행함
         */
        if(arg1.equals("cache-title")) {
            while(true) {
                Cache cache = new Cache();
                cache.cache_title();
            }
        }

        /**
         * 마스터디비-슬레이브디비간 키워드 해시태그(keyword_hashtag)
         * 슬레이브 ->마스터 데이터를 복제
         * 사전단계: 슬레이브에서 keyword_hashtag 빈 테이블을 복제하고 keyword_hashtag2 로 이름 변경
         */
        if(arg1.equals("recovery-hashtag-fromslave")) {
            Recovery rec = new Recovery();
            rec.hashtagFromSlave();
        }

        /**
         * 마스터디비-슬레이브디비간 키워드 해시태그(keyword_hashtag)
         * 마스터 ->슬레이브 데이터를 복제
         */
        if(arg1.equals("recovery-hashtag-frommaster")) {
            Recovery rec = new Recovery();
            rec.hashtagFromMaster(arg2,arg3);
        }

        /**
         * 마스터디비-슬레이브디비간 데이터랩플러스(datalab_plus)
         *
         * 마스터 ->슬레이브 데이터를 복제
         * 사전단계: 슬레이브에서 datalab_plus 빈 테이블을 복제하고 datalab_plus_backup 로 이름 변경
         * 데이터가 많아 분산처리를 위해 part, total을 받아 전체 row 갯수를 분산해서 복구합니다
         *
         * @param  part  수행하는 인스턴스 넘버
         * @param  total  수행하는 인스턴스 전체 갯수
         */

        if(arg1.equals("recovery-datalabplus")) {
            Recovery recovery = new Recovery();
            recovery.datalabPlus(arg2,arg3);

        }
        /**
         * 마스터디비-슬레이브디비간 네이버라이트(naver_light)
         *
         * 마스터 ->슬레이브 데이터를 복제
         * 사전단계: 슬레이브에서 naver_light 빈 테이블을 복제하고 naver_light2 로 이름 변경
         * 주의 : 초기에 data_review를 수집하였으나, 이를 복제하면서 OOM(Out Of Memory)을 일으키므로 복제하지 않음
         */
        if(arg1.equals("recovery-naverlight")) {
            Recovery recovery = new Recovery();
            recovery.naverLight();

        }
        /**
         * 마스터디비-슬레이브디비간 네이버스토어(naver_store)
         *
         * 마스터 ->슬레이브 데이터를 복제
         * 사전단계: 슬레이브에서 naver_store 빈 테이블을 복제하고 naver_store2 로 이름 변경
         */
        if(arg1.equals("recovery-naverstore")) {
            Recovery recovery = new Recovery();
            recovery.naverStore();

        }
        /**
         * 마스터디비-슬레이브디비간 네이버유저(naver_users)
         *
         * 마스터 ->슬레이브 데이터를 복제
         * 사전단계: 슬레이브에서 naver_users 빈 테이블을 복제하고 naver_users2 로 이름 변경
         */
        if(arg1.equals("recovery-naverusers")) {
            Recovery recovery = new Recovery();
            recovery.naverUsers();

        }

  

        /**
         * 1페이지 키워드 데이터 수집
         *
         * 네이버 광고 대시보드 접속후 키워드 데이터 수집/저장 (keyword_1page), 해당 키워드로 상품 검색 추가 데이터 수집
         * 이 과정에서, 상품 리스트를 훑으면서 naver_light도 수집함
         *
         * [주의]
         * 처음에 수동 로그인 해야함
         *
         * @param part 시작지점
         * @param total 전체분할개수
         */
        if(arg1.equals("naver-keyword")) {

            while(true) {
                KeywordExposed dp = new KeywordExposed();
                dp.getKeywordAdEffeciency(Integer.parseInt(arg2), Integer.parseInt(arg3));
                FirstPageHasCatalog fpc = new FirstPageHasCatalog();
                fpc.check();
            }
        }

        if(arg1.equals("naver-keyword-revenue-adcost")){
            KeywordExposed dp = new KeywordExposed();
            dp.getKeywordAdEffeciency(Integer.parseInt(arg2), Integer.parseInt(arg3));
        }

        //특정키워드만 검색해서 업데이트함(시간관계상 광고 관련 수집안함)
        if(arg1.equals("naver-keyword-revenue-adcost-keyword")){
            KeywordExposed dp = new KeywordExposed();
            dp.getKeywordAdEffeciencyOneKeyword(arg2);
        }


        if(arg1.equals("naver-keyword-catalog")){
            FirstPageHasCatalog fpc = new FirstPageHasCatalog();
            fpc.check();
        }

        //최저가 토탈 프로세스
        if(arg1.equals("naver-categorymatching-total")){

            //카테고리 리스트에서 최저가
            CategoryMatchingList cm = new CategoryMatchingList();
            cm.findLowestPricelist(Integer.parseInt(arg2), Integer.parseInt(arg3));

            //키워드 최저가
            CategoryMatchingList cm2 = new CategoryMatchingList();
            cm2.keywordFirstPageExposed(Integer.parseInt(arg2), Integer.parseInt(arg3));

            //대시보드 최저가
            cm2.saveCategoryMathcingList(Integer.parseInt(arg2), Integer.parseInt(arg3));

        }




        if(arg1.equals("naver-reviewcount")){
            NaverReviewScrapper dp = new NaverReviewScrapper(Conf.NAVER_DB_IP);
            dp.printReviewOptionRatio(arg2, Integer.parseInt(arg3));
        }



        //데이터랩 모멘텀 계산해서 CSV 추출하기.(경로설정필)
        if(arg1.equals("practice-momentum")){
            KeywordToolPage dp = new KeywordToolPage();
            dp.getDatalabMomentumCSV();
        }

        if(arg1.equals("practice-keyword")){
            KeywordToolPage dp = new KeywordToolPage();
            dp.updateDatalabClickAvgPrice();
        }


        /* 네이버 "라이트" 스크랩 */

        if(arg1.equals("scrap-naver-light")){
            while(true) {
                NaverShoppingLightScrapper scrp = new NaverShoppingLightScrapper(Conf.NAVER_DB_IP);
                scrp.scrapDetail(arg2, arg3, args[3], args[4]);
            }
        }


        if(arg1.equals("scrap-naver-light-auto")){
            try {
                for(int i=1; i <= Integer.parseInt(arg3); i++) {

                    Process p = builder.start();
                    Utils.execCommandSingle("start /high java -jar " + Conf.JAR_PATH + " scrap-naver-light " +  i + " " + arg3 + " " + args[3], p, 1000);
                    Utils.execCommandSingle("exit", p, 100);
                    p.destroy();
                    Thread.sleep(30 * 1000);
                }
            }catch(Exception er){
                System.out.println(er.getMessage());
            }
        }



        /* 네이버 "리뷰" 스크랩 */
        /* 유저정보 수집 */
        //리뷰만 수집하는데 리뷰수집하면서 유저정보까지 수집함
        if(arg1.equals("scrap-naver-review-users")) {
            NaverReviewScrapper ss= new NaverReviewScrapper(Conf.NAVER_DB_IP);
            ss.getOnlyReviewInfo(args[1],Integer.parseInt(args[2]));
            //https://smartstore.naver.com/kssinesp/products/5619069773
        }


        if(arg1.equals("scrap-naver-review-users-all")) {
            NaverReviewScrapper ss= new NaverReviewScrapper(Conf.NAVER_DB_IP);
            while(true) {
                ss.getReviewAndUsersInfo(arg2, arg3);
            }
        }

        if(arg1.equals("scrap-naver-review")){
            while(true) {
                NaverReviewScrapper scrp = new NaverReviewScrapper(Conf.NAVER_DB_IP);
                scrp.updateNaverReview(arg2, arg3, null);
            }
        }
        if(arg1.equals("scrap-naver-review-auto")){
            try {
                for(int i=1; i <= Integer.parseInt(arg3); i++) {

                    Process p = builder.start();
                    //util.execCommandSingle("start /high "+(String) list.get("command") +"execute"+ list.get("ad_network_name") + " " + list.get("device_name"), p, 1000);
                    Utils.execCommandSingle("start /high java -jar " + Conf.JAR_PATH + " scrap-naver-review " +  i + " " + arg3, p, 1000);
                    Utils.execCommandSingle("exit", p, 100);
                    p.destroy();
                    Thread.sleep(3* 60 * 1000);
                }
            }catch(Exception er){
                System.out.println(er.getMessage());
            }
        }


        if(arg1.equals("scrap-datalab")){

            Datalab.CategoryScrapper dc = new Datalab.CategoryScrapper(Conf.DATALAB_IP);
            try {
                dc.scrapDetail(arg2, arg3);
            }catch (Exception er){
                er.printStackTrace();
            }

        }




    }


}
