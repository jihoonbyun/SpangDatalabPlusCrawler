package Util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Connection.MySQLConnector;
import java.sql.Connection;

public class Recorder {

    Conf mv = new Conf();
    MySQLConnector mysql = new MySQLConnector();
    //Connection conn_slave = mysql.initConnect(mv.NAVER_DB_IP_SERVICE);
    //TODO: 향후 서비스 DB로 바꿀 것
    String DB_URL = mv.NAVER_DB_IP;
    Connection conn_slave = mysql.initConnect(DB_URL);

    Boolean offoption = false;

    public void setOff(){
        this.offoption = true;
    }

    public void close() {
        try {
            conn_slave.close();
        }catch(Exception ex) {
        }
    }

    public JSONArray selectUpdateListAllManual() {
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        JSONArray arr = mysql.selectUpdateList(conn_slave,"select * from update_list where register_type='수동' order by insert_time asc");
        return arr;
    }
    public void updateManual(String register_type, String update_name) {
        mysql.updateUpdateListRegisterType(conn_slave,"update update_list set register_type=? where update_name=?", register_type, update_name);
    }

    public JSONArray selectUpdateListAll() {
        JSONArray arr = mysql.selectUpdateList(conn_slave,"select * from update_list order by insert_time asc");
        return arr;
    }


    public void insertUpdateList(String update_type, String update_name, String data, int part, int total){

        if(offoption) {
            return;
        }

        //mysql.startTransaction(conn_slave);
        String query = "insert into update_list(update_type, update_name,message,success_count,total_count) values(?,?,?,?,?)";
        if(data.length() > 30) {
            data = data.substring(0,30) + "...";
        }
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        mysql.insertUpdateList(conn_slave, query ,update_type, update_name, data,part,total);
        //mysql.commit(conn_slave);

    }

    public JSONArray updateAndSelect(String update_name, String msg,  int  success_count){
        updateSuccessCountMessage(update_name,msg, success_count);
        if(msg.length() > 30) {
            msg = msg.substring(0,30) + "...";
        }
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        JSONArray arr = mysql.selectUpdateList(conn_slave,"select * from update_list where update_name='" + update_name + "'");
        return arr;
    }


    public void updateMessage(String update_name, String message) {

        if(offoption) {
            return;
        }
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String query ="update update_list set message=? where update_name=?";
        if(message.length() > 30) {
            message = message.substring(0,30) + "...";
        }
        mysql.updateMessage(conn_slave, query, message, update_name);
    }

    public void updateSuccessCountMessage(String update_name, String message, int count) {

        if(offoption) {
            return;
        }

        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String query ="update update_list set message=?, success_count=? where update_name=?";
        if(message.length() > 30) {
            message = message.substring(0,30) + "...";
        }
        mysql.updateSuccessCountMessage(conn_slave, query, message, count, update_name);
    }

    public JSONArray selectAndPlucCountUpdateList(String update_name, String msg){
        //mysql.startTransaction(conn_slave);
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        JSONArray arr = mysql.selectUpdateList(conn_slave,"select * from update_list where update_name='" + update_name + "' for update");
        if(arr.size() > 0) {
            JSONObject obj = (JSONObject) arr.get(0);
            int total_count = (int) obj.get("total_count");
            int success_count = (int) obj.get("success_count");
            success_count++;
            updateSuccessCountMessage(update_name, msg, success_count);
        }
        //mysql.commit(conn_slave);
        return arr;
    }

    public void finishUpdateList(String update_name) {

        if(offoption) {
            return;
        }
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String query ="update update_list set register_type=? where update_name=?";
        mysql.updateUpdateListRegisterType(conn_slave,query, "분석완료", update_name);


    }

    public void notifyToServer(String target_url, String update_name) {

        if(offoption) {
            return;
        }
        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }

            HttpConn httpconn = new HttpConn();
            int  random_int = (int)Math.floor(Math.random()*(100-1+1)+1);
            String ua = mysql.selectUserAgent(conn_slave, "select user_agent from useragent where rownum >='" + random_int + "' limit 1");
            //httpconn.sendPost(target_url + "?update_name=" + update_name,"", ua);
            httpconn.sendGet(target_url + "?update_name=" + update_name, ua);
            System.out.println("이메일 전송 요청 완료");

        }catch(Exception ex){
            ex.printStackTrace();
        }


    }


    public void deleteUpdateList(String update_name) {

        if(offoption) {
            return;
        }

        try {
            if (conn_slave.isClosed()) {
                conn_slave = mysql.initConnect(DB_URL);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        String query = "delete from update_list where update_name='" + update_name + "'";
        mysql.deleteUpdateList(conn_slave, query);
    }

}
