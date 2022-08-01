package DatalabPlus;

import Connection.MySQLConnector;
import DatalabPlus.SellerAnalysis;
import DatalabPlus.UpdateGrowthObject;
import Naver.NaverShoppingLightScrapper;
import Util.Conf;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class UpdateGrowthThread extends Thread {

    ProductAnalysis rm = new ProductAnalysis(Conf.NAVER_DB_IP);
    private UpdateGrowthObject rankingOjbect;
    public MySQLConnector mysql = new MySQLConnector();
    public JSONObject data = new JSONObject();
    public Timestamp ts = null;
    public Date current_date = null;
    public JSONObject original_data = new JSONObject();
    JSONParser parser = new JSONParser();
    Calendar c1 = Calendar.getInstance();
    public int starting_point = 0;
    public int end_point = 0;
    public Connection conn;
    public Statement st;
    public ResultSet rs;
    public Date three_monthago;
    public String query_max = "select max(id) as rownum from datalab_plus";
    public int rownum;

    UpdateGrowthThread(int starting_point, int end_point, UpdateGrowthObject rankingOjbect) {
        this.starting_point = starting_point;
        this.end_point = end_point;
        this.rankingOjbect = rankingOjbect;
    }

    public void createConnectionMaterials() {
        try {
            conn = mysql.initConnect(Conf.NAVER_DB_IP);
            st = conn.createStatement();
            rs = null;
            c1.add(Calendar.MONTH, -1 * 3);
            three_monthago = c1.getTime();
            rownum= mysql.selectRowNumber(conn, query_max);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        createConnectionMaterials();

        for (int id = starting_point; id <= end_point; id++) {
            System.out.println("[" + Thread.currentThread().getName() + "] " +  id);
            try {
                if (conn.isClosed()) {
                    conn = mysql.initConnect(Conf.NAVER_DB_IP);
                    st = conn.createStatement();
                    rs = null;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

            original_data = mysql.selectDatalabPlusTotal3(conn, st, rs, "select id,product_no,data,cat_big,notsell,insert_time from datalab_plus where id=" + id);
             if (original_data.size() == 0) {
                continue;
            }
            try {
                if ((int) original_data.get("notsell") == 1) {
                    continue;
                }
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            try {
                ts = (Timestamp) original_data.get("insert_time");
                current_date = new Date(ts.getTime());
                data = (JSONObject) parser.parse((String) original_data.get("data"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            try {
                if ((Double) data.get("revenue12m") > 10000000 && (Double) data.get("월평균성장률12개월") < 999 && (Double) data.get("월평균성장률12개월") > 0 && three_monthago.compareTo(current_date) < 0 && !((String) original_data.get("cat_big")).equals("패션")) {
                    synchronized (this) {
                        rankingOjbect.updateProductNosAndGrowthList(data);
                        if((id % rankingOjbect.array_num == 0) && (rankingOjbect.getArrayListLength() == rankingOjbect.array_num) ) {
                            rm.insertGrowthData(rankingOjbect);
                        }
                    }

                }
            } catch (Exception ex) {

            }

        }
        System.out.println(Thread.currentThread().getName() + " 업무 종료^^");
        interrupt();
    }
}
