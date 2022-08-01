package Naver;

import Util.DriverControl;
import Util.LoginTool;
import Connection.MySQLConnector;
import Util.Conf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

public class KeywordToolPageClass<T,I,J> {
    J trend_obj_1m;
    J trend_obj_3m;
    J trend_obj_6m;
    J trend_obj_12m;
    J trend_obj_24m;
    T date_1m;
    T date_3m;
    T date_6m;
    T date_12m;
    T date_24m;
    T click_1m;
    T click_3m;
    T click_6m;
    T click_12m;
    T click_24m;
    I delta_1m;
    I delta_3m;
    I delta_6m;
    I delta_12m;
    I delta_24m;
    T delta1m;
    T delta3m;
    T delta6m;
    T delta12m;
    T delta24m;
}
