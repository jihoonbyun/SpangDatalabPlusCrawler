package Naver;

import Connection.MySQLConnector;
import Util.Conf;
import Util.DriverControl;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

public class FirstPageHasCatalog {

    MySQLConnector mysql = new MySQLConnector();
    ChromeDriver driver;


    /**
     * keyword_1page 디비에서 검색 후 업데이트 (deprecated)
     *
     */
    public void check() {

        Conf mv = new Conf();
        Connection conn = mysql.initConnect(mv.NAVER_DB_IP);
        driver = DriverControl.getGeneralDriver();
        int stop_flag = 0;
        String q = "select * from keyword_1page where category is null";
        ArrayList<HashMap<String,String>> keyword_list = mysql.selectKeywordDup(conn, q);
        for (int j = 0; j < keyword_list.size(); j++) {

            try {
                if(stop_flag == 1){
                    j--;
                }
                String keyword = (String) keyword_list.get(j).get("keyword");
                driver.get("https://search.shopping.naver.com/search/all.nhn?query=" + keyword);
                Long catalog_size = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_model_list').length");
                String category = (String) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('_itemSection')[0].children[1].children[2].textContent.trim().replace(/ /g, \"\").replace(/\\n/gi, \"\")");
                mysql.update1pageCatalog(conn, "update keyword_1page set catalog=?, category=? where keyword=?", catalog_size, category, keyword);
                System.out.println( j+ "[카탈로그 업데이트]" + keyword + " : " + catalog_size);
                Thread.sleep(3000);
                stop_flag =0;

            }catch(NoSuchMethodError e){
                try {
                    Thread.sleep(10000);
                    stop_flag =1;
                }catch(Exception e2) {
                    e2.printStackTrace();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
