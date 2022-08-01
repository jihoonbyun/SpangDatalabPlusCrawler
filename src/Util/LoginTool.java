package Util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Scanner;

public class LoginTool {

    public void loginWait(ChromeDriver driver){
        while(true) {
            Scanner scn = new Scanner(System.in);
            System.out.println("로그인이 필요합니다. 브라우저 화면에서 로그인 후 엔터를 눌러주세요");
            scn.nextLine();

            String current_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
            if(current_url.split("nid.naver.com/nidlogin").length ==1) {
                System.out.println("로그인이 완료되었습니다");
                break;
            }

        }

    }


}
