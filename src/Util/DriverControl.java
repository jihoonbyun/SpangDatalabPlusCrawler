package Util;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Set;

public class DriverControl  {

    public static  void removePopupGetDriver (ChromeDriver driver, String url){
        String currentHandle = ((ChromeDriver) driver).getWindowHandle();
        try {
            driver.get(url);
        } catch (Exception e) {
            try {
                driver.switchTo().alert().dismiss();
                System.out.println("클릭");
            } catch (Exception ex) {

            }
        }

        Set<String> handles = null;
        handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(currentHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(currentHandle);

    }
    public static void quitDriver(ChromeDriver driver){
        if(driver != null) {
           driver.quit();
        }
    }
    public static void alertClick(ChromeDriver driver) {

        int counter = 0;

        while (true) {
            try {
                driver.switchTo().alert().dismiss();
                System.out.println("클릭");
                break;
            } catch (NoSuchMethodError e) {

                counter++;
                if (counter == 10) {
                    return;
                }
            } catch (NoClassDefFoundError e2) {

                counter++;
                if (counter == 10) {
                    return;
                }
            } catch (NoSuchSessionException ex) {
                return;
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }
        }
    }
    public static Boolean invalidCheck (ChromeDriver driver) {
        Boolean rs = true;
        try {
            Long notSell = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"운영이 중지되었습니다\").length");
            Long notSell2 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매금지 된 상품입니다.\").length");
            Long notSell3 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 현재 판매중지 된 상품입니다.\").length");
            Long notSell4 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"상품이 존재하지 않습니다.\").length");
            Long notSell5 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"이 상품은 판매할 수 없습니다\").length");
            Long notSell6 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"연령 확인이 필요한 서비스\").length");
            Long notSell7 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"현재 서비스 접속\").length");
            Long notSell8 = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('body')[0].textContent.split(\"현재 운영되고\").length");

            if (notSell7 > 1) {
                try {
                    Thread.sleep(30000);
                    ((JavascriptExecutor) driver).executeScript("location.reload()");

                } catch (Exception ex) {

                }
            }


            if (notSell > 1 || notSell2 > 1 || notSell3 > 1 || notSell4 > 1 || notSell5 > 1 || notSell6 > 1 || notSell8 > 1) {
                rs = false;
            } else {
                rs = true;
            }
        } catch (Exception ex) {
            //
        }
        return rs;
    }
    public static void tooManyRequest(ChromeDriver driver){
        try {
            Long check_screen = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall_txt').length");
            Thread.sleep(5000);
        }catch(NoSuchMethodError e) {
            //요청횟수가 너무 많습니다.
            while(true){
                try {
                    Thread.sleep(10000);
                    driver.executeScript("location.reload()");
                    Long check_screen = (Long) ((JavascriptExecutor) driver).executeScript("return document.querySelectorAll('.mall_txt').length");
                    System.out.println("요청횟수 지옥 탈출!");
                    break;
                }catch(Exception et){

                }catch(NoSuchMethodError et2){

                }
            }
        } catch(InterruptedException ex2) {
            ex2.printStackTrace();
        }
    }
    public static int tooManyRequestOld(ChromeDriver driver){
        //요청이 너무 많습니다 or 검색결과가 없습니다 체크
        int continue_flag = 0;
        while(true) {
            try {

                //검색결과를 제공하지 않습니다.
                Long tester3_long = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('result_txt').length");
                if(tester3_long > 0) {
                    continue_flag = 1;
                    break;
                }
                //검색결과가 없습니다.
                Long tester2_long = (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('search_none').length");
                if(tester2_long > 0){
                    continue_flag =1;
                    break;
                }
                //연령확인이 필요합니다.
                Long tester4_long =  (Long) ((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('title').length");
                if(tester4_long > 0){
                    continue_flag =1;
                    break;
                }

                //요청이 너무 많습니다
                String tester = (String) ((JavascriptExecutor) driver).executeScript("return $('#_result_paging').children('strong').text().split('현재 페이지')[1].trim()");
                break;

            } catch (NoSuchMethodError toomanyrequest) {
                //요청이 너무 많습니다
                try {
                    Thread.sleep(30 * 1000);
                }   catch (InterruptedException e){

                }
                ((JavascriptExecutor) driver).executeScript("return location.reload()");
            }
        }

        return continue_flag;
    }
    public static void popupFocus(final WebDriver driver, String correct_url_part) {
        try {
            final String mainWindowHandle = driver.getWindowHandle();
            Thread.sleep(5000);
            for (String activeHandle : driver.getWindowHandles()) {
                if (!activeHandle.equals(mainWindowHandle)) {
                    driver.switchTo().window(activeHandle);
                    //url체크
                    if(correct_url_part != null) {
                        String current_url = (String) ((JavascriptExecutor) driver).executeScript("return location.href");
                        if (current_url.split(correct_url_part).length > 1) {
                            return;
                        }
                    }

                }
            }
        }catch(Exception er){

        }
    }
    public static void removeAllPages(final WebDriver driver) {
        try {
            final String mainWindowHandle = driver.getWindowHandle();
            Thread.sleep(5000);
            for (String activeHandle : driver.getWindowHandles()) {

                driver.switchTo().window(activeHandle);
                DriverControl.quitDriver((ChromeDriver)driver);



            }
        }catch(Exception er){

        }
    }
    public static void manipulatePopUp(final WebDriver driver) {
        try {
            final String mainWindowHandle = driver.getWindowHandle();
            ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName('btn2')[5].click()");
            Thread.sleep(5000);
            for (String activeHandle : driver.getWindowHandles()) {
                if (!activeHandle.equals(mainWindowHandle)) {
                    driver.switchTo().window(activeHandle);
                }
            }
        }catch(Exception er){
            er.printStackTrace();
        }
    }
    public static void quitDriverAll(final WebDriver driver) {
        try {
            final String mainWindowHandle = driver.getWindowHandle();
            Thread.sleep(5000);
            for (String activeHandle : driver.getWindowHandles()) {

                driver.switchTo().window(activeHandle);
                DriverControl.quitDriver((ChromeDriver)driver);

            }
        }catch(Exception er){
            er.printStackTrace();
        }
    }
    public static ChromeDriver getGeneralDriver (Boolean isHeadless) {
        System.setProperty("webdriver.chrome.driver", Conf.CHROMEDRIVER_DIR);
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-popup-blocking");
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--disable-gpu");
        options.addArguments("disable-gpu");
        options.addArguments("enable-features=NetworkServiceInProcess");
        options.addArguments("disable-features=NetworkService");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--aggressive-cache-discard");
        options.addArguments("--disable-cache");
        options.addArguments("--disable-application-cache");
        options.addArguments("--disable-offline-load-stale-cache");
        options.addArguments("--disk-cache-size=0");
        options.addArguments("--dns-prefetch-disable");
        options.addArguments("--no-proxy-server");
        options.addArguments("--force-device-scale-factor=1");
        options.addArguments("--log-level=3"); options.addArguments("--silent"); options.addArguments("--disable-browser-side-navigation"); options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        if(isHeadless) {
            options.addArguments("headless");
        }

        ChromeDriver driver = new ChromeDriver(options);
        System.out.println("Opening Chrome");
        return driver;
    }
    public static ChromeDriver getGeneralDriverDownload(String download_dir) {
        System.setProperty("webdriver.chrome.driver", Conf.CHROMEDRIVER_DIR);
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", download_dir);
        chromePrefs.put("safebrowsing.enabled", "false");
        chromePrefs.put("download.prompt_for_download", "false");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        ChromeDriver driver;
        driver = new ChromeDriver(options);
        System.out.println("Opening Chrome");

        return driver;
    }
    public static ChromeDriver getGeneralDriver () {

        System.setProperty("webdriver.chrome.driver", Conf.CHROMEDRIVER_DIR);
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-popup-blocking");
        options.setExperimentalOption("prefs", chromePrefs);
        ChromeDriver driver = new ChromeDriver(options);
        System.out.println("Opening Chrome");
        return driver;
    }
    public static void checkAlert(ChromeDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 2);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            //exception handling
        }
    }
}
