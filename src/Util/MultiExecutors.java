package Util;

import DatalabPlus.ProductAnalysis;
import Naver.NaverProductDetailClass;
import DatalabPlus.SellerAnalysis;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Connection.MySQLConnector2;

public class MultiExecutors {

    private final ExecutorService executorService;
    private CallBack callback;
    public String result_str = "";
    public int total_count = 0;
    public int naver_id = 0;
    public int sum = 0;
    public int finish_flag = 0;
    public int next_id = 0;
    public int max_id = 0;
    public int start_count = 0;
    ArrayList<Integer> que = new ArrayList<>();
    ArrayList<ProductAnalysis> DATALAB_ARRAY = new ArrayList<ProductAnalysis>();

    public MultiExecutors(){
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void setFinishFlag(){
        finish_flag = 1;
    }

    public void setId(int id) { next_id = id;}

    public int getId() { return next_id;}

    public interface CallBack{
        public void onGetMessage(MultiExecutors multi);
        public void onFinish();
    }
    public void setCallback(CallBack callback) {
        this.callback = callback;
    }


    public int ThreadNum = 0;
    public int current_thread_id = 0;

    public void setThreadNum(int thrednum){
        this.ThreadNum = thrednum;
    }

    public void quitAllDriver(){
        for(int i=0; i < DATALAB_ARRAY.size(); i++){
            ProductAnalysis dp = DATALAB_ARRAY.get(i);
            DriverControl.quitDriver(dp.driver);
        }
    }
    public void executeRoutineProcessMultiByThreadNum(int threads) {

        Recorder rec = new Recorder();

        //디비연결
        MySQLConnector2 mysql = new MySQLConnector2();
        Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);

        //코어갯수만큼 datalabplus 객체 생성
        DATALAB_ARRAY = new ArrayList<ProductAnalysis>();
        for (int i = 0; i < threads; i++) {
            DATALAB_ARRAY.add(new ProductAnalysis(Conf.NAVER_DB_IP));
            que.add(0);
        }

        //맥스아이디
        String query = "select max(id) as id from naver_light";
        ArrayList<NaverProductDetailClass<String,Integer,Double,Long, Timestamp>> ids = mysql.selectNaverLightUrlId(conn, query);
        int max_id = ids.get(0).id;
        int naver_id = 0;



        while(true) {


            try {

                //모든 URL을 분석했을떄
                if (naver_id > max_id) {
                    //초기화
                    naver_id = 0;
                }

                //URL 남아있을때
                else {

                    //큐에 빈방을 찾는다
                    int freeroom_number = returnFreeRoom(que);

                    //큐가 가득 찼을떄
                    if (freeroom_number == -1) {
                        Thread.sleep(1000);
                    }

                    //빈 큐 있음
                    else {

                        //큐 사용중
                        que.set(freeroom_number, 1);

                        //큐 전용 dp
                        ProductAnalysis dp = DATALAB_ARRAY.get(freeroom_number);

                        //url
                        ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> urls = mysql.selectNaverLightUrl(conn, "select distinct(product_url), product_no, insert_time, category_comparison, datalab_update_timestamp, deletes from naver_light where id=" + naver_id);


                        if(urls.size() > 0) {

                            int start_count = naver_id;

                            String product_no = urls.get(0).product_no;

                            //쓰레드 실행
                            Runnable task = new Runnable() {

                                @Override
                                public void run() {

                                    try {

                                        Connection conn2 = mysql.initConnect(Conf.NAVER_DB_IP);
                                        ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn2, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                                        conn2.close();

                                        if (arr.size() == 0) {
                                            if (!product_no.equals("")) {
                                                JSONObject res = dp.executeProcess("https://smartstore.naver.com/main/products/" + product_no, null, "enemy-extends", "not-quit");
                                                if (res.get("valid_url").equals("스마트스토어 URL이 아닙니다")) {
                                                    mysql.updateNaver(conn, "update naver_light set deletes=? where id=?", 1, String.valueOf(start_count));
                                                } else if (res.get("valid_url").equals("리뷰에러가 발생하였습니다")) {
                                                    System.out.println("리뷰수집에 문제가 있음!! 점검바람");
                                                } else if (res.get("valid_url").equals("deleted")) {
                                                    mysql.updateNaver(conn, "update naver_light set deletes=? where id=?", 1, String.valueOf(start_count));
                                                }
                                            }

                                        }

                                        //작업완료. 방해제
                                        que.set(freeroom_number, 0);

                                        //메시지 콜백
                                        total_count++;


                                    } catch (Exception ex) {

                                    }
                                }
                            };

                            executorService.submit(task);


                        }


                        naver_id++;

                    }

                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }





    }
    public void executeThreadByThreadNum(int threadNum){

        setThreadNum(threadNum);

        ArrayList<ProductAnalysis> DATALAB_ARRAY = new ArrayList<ProductAnalysis>();
        for(int i=0; i<threadNum; i++){
            DATALAB_ARRAY.add(new ProductAnalysis(Conf.NAVER_DB_IP));
        }


        MySQLConnector2 mysql = new MySQLConnector2();
        Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);
        Conf mv = new Conf();

        //맥스아이디
        String query = "select max(id) as id from naver_light";
        ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> ids = mysql.selectNaverLightUrlId(conn, query);
        int max_id = ids.get(0).id;

        while(true) {

            ArrayList<NaverProductDetailClass<String,Integer,Double,Long,Timestamp>> urls = mysql.selectNaverLightUrl(conn, "select distinct(product_url), product_no, insert_time, category_comparison, datalab_update_timestamp, deletes from naver_light where id=" + naver_id);
            naver_id++;
            System.out.println("네이버아이디:" + naver_id);

            if (naver_id >= max_id) {
                naver_id = 0;
            }

            try {

                if(urls.size() > 0) {

                    String product_no = urls.get(0).product_no;


                    while(true) {

                        if (current_thread_id == threadNum) {
                            Thread.sleep(1000);
                        }


                        if (current_thread_id < threadNum) {

                            ProductAnalysis dp = DATALAB_ARRAY.get(current_thread_id);
                            current_thread_id++;

                            Runnable task = new Runnable() {

                                @Override
                                public void run() {

                                    try {

                                        Connection conn2 = mysql.initConnect(mv.NAVER_DB_IP);
                                        ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn2, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                                        conn2.close();

                                        if (arr.size() == 0) {
                                            if (!product_no.equals("")) {
                                                JSONObject res = dp.executeProcess("https://smartstore.naver.com/main/products/" + product_no, null, "enemy-extends", "not-quit");
                                                if (res.get("valid_url").equals("스마트스토어 URL이 아닙니다")) {
                                                    mysql.updateNaver(conn, "update naver_light set deletes=? where id=?", 1, String.valueOf(naver_id));
                                                } else if (res.get("valid_url").equals("리뷰에러가 발생하였습니다")) {
                                                    System.out.println("리뷰수집에 문제가 있음!! 점검바람");

                                                } else if (res.get("valid_url").equals("deleted")) {
                                                    mysql.updateNaver(conn, "update naver_light set deletes=? where id=?", 0, String.valueOf(naver_id));
                                                }
                                            }

                                        }

                                        current_thread_id--;



                                    } catch (Exception ex) {

                                    }
                                }
                            };

                            executorService.execute(task);

                            break;


                        }
                    }



                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    public void executeThread(String target_url, String enemy_type){

        ProductAnalysis dp =new ProductAnalysis(Conf.NAVER_DB_IP);
        MySQLConnector2 mysql = new MySQLConnector2();

            try {

                Runnable task = new Runnable() {

                    @Override
                    public void run() {

                        try {


                            JSONObject res = dp.executeProcess(target_url, null, enemy_type, "quit");

                            if((Boolean)res.get("valid") == true) {
                                Recorder rec = new Recorder();
                                rec.notifyToServer(Conf.SERVICE_SERVER_IP + "/update/complete", (String) res.get("상품번호"));
                            } else {
                                Recorder rec = new Recorder();
                                rec.notifyToServer(Conf.SERVICE_SERVER_IP + "/update/error", (String) res.get("상품번호"));
                            }



                        } catch (Exception ex) {

                        }
                    }
                };

                executorService.execute(task);



                //executorService.submit(runnable);
            } catch (Exception wrong_url) {

            }



    }


    public int returnFreeRoom(ArrayList<Integer> que) {
        int res = -1;
        for(int i=0; i < que.size(); i++){
            if(que.get(i) == 0) {
                res = i;
                break;
            }
        }
        return res;
    }


    public void executeProcessMultiByThreadNum(ArrayList inserted_urls, String update_name, String enemy_type, int threads) {


        int original_size = inserted_urls.size();

        int count = 0;

        //디비연결
        MySQLConnector2 mysql = new MySQLConnector2();
        Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);

        //코어갯수만큼 datalabplus 객체 생성
        DATALAB_ARRAY = new ArrayList<ProductAnalysis>();
        for (int i = 0; i < threads; i++) {
            DATALAB_ARRAY.add(new ProductAnalysis(Conf.NAVER_DB_IP));
            que.add(0);
        }

        Recorder rec = new Recorder();



        while(true) {

            try {

                //모든 URL을 분석했을떄
                if(total_count == original_size){

                    //초기화
                    for (int i = 0; i < threads; i++) {
                        que.set(i, 0);
                    }

                    rec.updateAndSelect(update_name, "Multi Threads works successfully", 0);
                    break;


                }

                //URL 남아있을때
                else {

                    //큐에 빈방을 찾는다
                    int freeroom_number = returnFreeRoom(que);

                    //풀방
                    if (freeroom_number == -1) {
                        Thread.sleep(1000);
                    }

                    //빈방있음
                    else {


                        //방 사용중
                        que.set(freeroom_number, 1);

                        //방 전용 dp
                        ProductAnalysis dp = DATALAB_ARRAY.get(freeroom_number);

                        //url 할당
                        try {
                            String target_url = (String) inserted_urls.get(count);

                        count++;
                        String product_no = target_url.split("products/")[1];
                        result_str = product_no;
                        ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");

                        //쓰레드 실행
                        Runnable task = new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    if(arr.size() == 0) {
                                        dp.executeProcess(target_url, null, enemy_type, "not-quit");
                                    } else {
                                        HashMap mydata = arr.get(0);
                                        if(!mydata.containsKey("경쟁그룹")) {
                                            if(enemy_type.equals("enemy") || enemy_type.equals("enemy-ifnotexist")) {
                                                dp.executeProcess(target_url, null, enemy_type, "not-quit");
                                            }
                                        }
                                    }

                                    //작업완료. 방해제
                                    que.set(freeroom_number, 0);

                                    //메시지 콜백
                                    total_count++;
                                    rec.updateAndSelect(update_name, "[분석완료] 상품번호 : " +product_no, total_count);

                                } catch (Exception ex) {
                                    //ex.printStackTrace();
                                }
                            }
                        };

                        executorService.submit(task);


                        }catch(Exception ex){
                            break;
                        }


                    }

                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }





    }



    public void executeProcessMultiThread(ArrayList inserted_urls, String part, String total, String enemy_type, int threadid){
        Runnable task = new Runnable() {


            @Override
            public void run() {

                ArrayList splits_urls = new ArrayList();

                try {

                    SellerAnalysis rm = new SellerAnalysis();
                    ProductAnalysis dp = new ProductAnalysis(Conf.NAVER_DB_IP);
                    MySQLConnector2 mysql = new MySQLConnector2();
                    Connection conn = mysql.initConnect(Conf.NAVER_DB_IP);


                    //경쟁사 url을 분할한다
                    int slice_counts = Math.round(inserted_urls.size() / Integer.parseInt(total));
                    int end_point = slice_counts * Integer.parseInt(part);
                    int starting_point = end_point - slice_counts;
                    if(inserted_urls.size()-end_point < slice_counts) {
                        end_point = inserted_urls.size();
                    }

                    //분할한 url 그룹
                    for(int i=starting_point; i < end_point; i++) {
                        splits_urls.add(inserted_urls.get(i));
                    }




                    //분할한 URL 그룹을 순서대로 executeProcess 해준다
                    for(int i=0; i< splits_urls.size(); i++){

                        String target_url = "";

                        try {
                            target_url = (String) splits_urls.get(i);
                            String product_no = (String) target_url.split("products/")[1];
                            result_str = product_no;
                            ArrayList<HashMap<String, String>> arr = mysql.selectDatalabPlus(conn, "select * from datalab_plus where product_no='" + product_no + "' AND insert_time > DATE_ADD(now(), INTERVAL -15 DAY)");
                            if(arr.size() > 0) {

                                HashMap mydata = arr.get(0);
                                if(mydata.containsKey("경쟁그룹")) {
                                    //이미있는경우, 메시지 전송
                                    total_count++;
                                    callback.onGetMessage(MultiExecutors.this);
                                }

                                //데이터는 있으나, 경쟁그룹이 없는 경우
                                else {
                                    if(enemy_type.equals("enemy") || enemy_type.equals("enemy-ifnotexist")) {
                                        dp.executeProcess(target_url, null, enemy_type, "not-quit!!");
                                    }

                                    //완료후 메시지 전송
                                    total_count++;
                                    callback.onGetMessage(MultiExecutors.this);
                                }

                            } else {
                                dp.executeProcess(target_url, null, enemy_type, "not-quit!!");

                                //완료후 메시지 전송
                                total_count++;
                                callback.onGetMessage(MultiExecutors.this);
                            }

                        }catch(Exception ex){
                            total_count++;
                            //ex.printStackTrace();
                            //callback.onGetMessage(MultiExecutors.this);

                        }

                    }

                   DriverControl.quitDriver(dp.driver);

                }catch(NumberFormatException e){
                    //e.printStackTrace();
                    //callback.onGetMessage(MultiExecutors.this);
                } catch(Exception ex){
                    //ex.printStackTrace();
                }
                System.out.println("check" + String.valueOf(threadid) + " : " + splits_urls.size());
                sum +=  splits_urls.size();
                if(sum >= inserted_urls.size()){
                    callback.onFinish();
                }

            }
        }; executorService.submit(task);
    }


    public void finish(){
        sum = 0;
        total_count = 0;
        executorService.shutdown(); // 스레드풀 종료

    }



}
