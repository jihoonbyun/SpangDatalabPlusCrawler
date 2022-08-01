package DatalabPlus;

import Util.Utils;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.text.NumberFormat;
import java.util.*;

public class Calculator {

    /**
     * 리뷰옵션 비율 계산
     *
     * @param arr 리뷰 배열
     */
    public static HashMap<String, Double> printReviewOptionRatio(ArrayList< JSONObject > arr) {
        HashMap<String, Integer> option_map = new HashMap<>();
        for (int i = 0; i < arr.size(); i++) {
            try {
                String option_key = (String) arr.get(i).get("option");
                if (option_map.containsKey(option_key)) {
                    option_map.put(option_key, option_map.get(option_key) + 1);
                } else {
                    option_map.put(option_key, 1);
                }

            } catch (Exception er) {
                String option_key = "무옵션(기타)";
                if (option_map.containsKey(option_key)) {
                    option_map.put(option_key, option_map.get(option_key) + 1);
                } else {
                    option_map.put(option_key, 1);
                }
            }
        }
        option_map = Utils.sort(option_map);
        Iterator<String> keys = option_map.keySet().iterator();
        System.out.println("-----------------");
        System.out.println("총 리뷰수 : " + arr.size());
        HashMap hs = new HashMap<String, Double>();
        while (keys.hasNext()) {
            String key = keys.next();
            double d = option_map.get(key) * 1.0 / arr.size() * 1.0;
            d = Math.round(d * 100) / 100.0;
            System.out.println("[" + key + "] : " + option_map.get(key) + "개 : " + String.valueOf(d));
            hs.put(key, d);
        }
        System.out.println("-----------------");
        return hs;
    }

    /**
     * 한달전 연.월 구하기
     *
     * @param monthago 지금으로부터 n월전
     */

    public static HashMap getMonthAgo(int monthago){

        HashMap<String,Integer> hm = new HashMap<>();
        Calendar c = Calendar.getInstance();
        java.util.Date da = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(da);
        cal.add(Calendar.MONTH, -1 * monthago);
        Date ago = cal.getTime();
        int month = ago.getMonth()+1;
        int year = ago.getYear() + +1900;
        hm.put("year", year);
        hm.put("month", month);

        return hm;
    }
    /**
     * 특정 지점 성장률 구하기
     *
     * @param years_revenue_obj {연도 : [월별매출]} 형태 JSON 객체
     * @param starting_monthago 시작지점.지금으로부터 n월
     * @param range 시작지점으로부터 과거 n월
     */
    public static double   getGrowthRate(org.json.simple.JSONObject years_revenue_obj, int starting_monthago, int range) {
        double growth_temp = -999;

        HashMap target_ago = getMonthAgo(range+starting_monthago);
        int targetago_year = (int) target_ago.get("year");
        int targetago_month =(int) target_ago.get("month");

        HashMap month_ago = getMonthAgo(starting_monthago);
        int year_1m = (int) month_ago.get("year");
        int month_1m =(int) month_ago.get("month");



        try {
            long starting_point_temp = (long) Long.parseLong(String.valueOf(((ArrayList) years_revenue_obj.get(targetago_year)).get(targetago_month - 1)));
            long final_point = (long) Long.valueOf(String.valueOf(((ArrayList) years_revenue_obj.get(year_1m)).get(month_1m - 1)));
            if (starting_point_temp > 0) {
                growth_temp = Math.pow(final_point * 1.0 / starting_point_temp, 1.0 / range * 1.0);
                growth_temp = Math.round(growth_temp * 100) / 100.0;
            }

        }catch(Exception ex){
                //ex.printStackTrace();
        }
        return growth_temp;
    }
    /**
     * 특정 지점 성장률 구하기
     *
     * @param years_revenue_obj {연도 : [월별매출]} 형태 JSON 객체
     * @param starting_monthago 시작지점.지금으로부터 n월
     * @param range 시작지점으로부터 과거 n월
     */
    public static double   getGrowthRate2(org.json.simple.JSONObject years_revenue_obj, int starting_monthago, int range) {
        double growth_temp = -999;

        HashMap target_ago = getMonthAgo(range+starting_monthago);
        int targetago_year = (int) target_ago.get("year");
        int targetago_month =(int) target_ago.get("month");

        HashMap month_ago = getMonthAgo(starting_monthago);
        int year_1m = (int) month_ago.get("year");
        int month_1m =(int) month_ago.get("month");

        try {
            String str = String.valueOf(targetago_year);
            String str2= String.valueOf(year_1m);
            long starting_point_temp = (long) Long.parseLong(String.valueOf(((ArrayList) years_revenue_obj.get(str)).get(targetago_month - 1)));
            long final_point = (long) Long.valueOf(String.valueOf(((ArrayList) years_revenue_obj.get(str2)).get(month_1m - 1)));
            if (starting_point_temp > 0) {
                growth_temp = Math.pow(final_point * 1.0 / starting_point_temp, 1.0 / range * 1.0);
                growth_temp = Math.round(growth_temp * 100) / 100.0;
            }

        }catch(Exception ex){
            //ex.printStackTrace();
        }
        return growth_temp;
    }
    /**
     * 전체기간 성장률 구하기
     *
     * @param years_revenue_obj {연도 : [월별매출]} 형태 JSON 객체
     */
    public static double getGrowthRateTotal(org.json.simple.JSONObject years_revenue_obj) {

        double growth_temp = -999;
        ArrayList<Long> revenues_all = new ArrayList<>();


        try {
            HashMap current_date = getMonthAgo(0);
            int min_year = 99999;
            int max_year = -1;
            Iterator i = years_revenue_obj.keySet().iterator();
            while (i.hasNext()) {
                String str = i.next().toString();
                int key = Integer.parseInt(str);
                if (min_year > key) {
                    min_year = key;
                }
                if(max_year < key){
                    max_year = key;
                }
            }
            for(int year=min_year; year<=max_year; year++){

                try {
                    ArrayList month_list = (ArrayList) years_revenue_obj.get(String.valueOf(year));
                    for (int month = 0; month < 12; month++) {
                        long value = Long.parseLong(String.valueOf(month_list.get(month)));
                        revenues_all.add(value);
                    }
                }catch(Exception ex){
                    ArrayList month_list = (ArrayList) years_revenue_obj.get(year);
                    for (int month = 0; month < 12; month++) {
                        long value = Long.parseLong(String.valueOf(month_list.get(month)));
                        revenues_all.add(value);
                    }
                }
            }

            int first_index = 0;
            long first_value = 0;
            for(int first=0; first<revenues_all.size(); first++){
                if(revenues_all.get(first) >0 ) {
                    first_index = first;
                    first_value = revenues_all.get(first);
                    break;
                }
            }

            int last_index = revenues_all.size()-1;
            long last_value = 0;
            for(int last=revenues_all.size()-1; last >=0; last--){
                if(revenues_all.get(last) >0 ) {
                    last_index = last;
                    last_value = revenues_all.get(last);
                    break;
                }
            }
            int count = last_index- first_index;
            try {
                if (first_value > 0) {
                    growth_temp = Math.pow(last_value * 1.0 / first_value, 1.0 / count * 1.0);
                    growth_temp = Math.round(growth_temp * 100) / 100.0;
                }
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }catch(Exception ex){

            ex.printStackTrace();
        }
        return growth_temp;
    }
    /**
     * 연간 성장률 구하기
     *
     * @param years_revenue_obj {연도 : [월별매출]} 형태 JSON 객체
     */
    public static double   getGrowthRateYear(org.json.simple.JSONObject years_revenue_obj) {
        double growth_temp = -999;
        Boolean start_flag = false;
        int count = 0;
        ArrayList<Integer> data_new_list = new ArrayList<>();
        int min_year = 99999;
        int max_year =-1;

        Iterator i = years_revenue_obj.keySet().iterator();
        while(i.hasNext())
        {
            int key = (int) i.next();
            if(min_year > key) {
                min_year= key;
            }

            if(max_year < key){
                max_year = key;
            }
        }

        for(int t=(min_year+1); t<=(max_year-1); t++) {
            ArrayList values = (ArrayList)years_revenue_obj.get(t);
            if(values != null) {
                for (int k = 0; k < values.size(); k++) {
                    int value = (int) values.get(k);
                    if (value != 0) {
                        start_flag = true;
                    }
                    if (start_flag) {
                        count++;
                        data_new_list.add(value);
                    }
                }
            }
            else{
                System.out.println("중간에 어떤 년도에 아예 매출 없는 경우");
                for(int f=0; f < 12; f++){
                    data_new_list.add(0);
                    count++;
                }
            }
        }
        if(data_new_list.size() >= 24) {
            try {
                double d = data_new_list.size() / 12;
                int group_size = (int) Math.floor(d);
                int first_sum = 0;
                int last_sum = 0;
                for (int s = data_new_list.size() - 1; s >= data_new_list.size() - 12; s--) {
                    last_sum += data_new_list.get(s);
                }
                for (int t = 0; t < 12; t++) {
                    first_sum += data_new_list.get(t);
                }
                count = max_year - min_year;
                growth_temp = Math.pow(last_sum * 1.0 / first_sum, 1.0 / (group_size - 1) * 1.0);
                growth_temp = Math.round(growth_temp * 100) / 100.0;
            }catch(Exception ex){

            }
        }
        return growth_temp;
    }
    /**
     * CR3,HHI 구하기
     *
     * @param objects {셀러 : 매출} 형태 JSON 객체
     */
    public static HashMap<String,Long> getCR3andHHI(org.json.simple.JSONObject objects) {

        long cr3 = 0;
        long hhi = 0;

        ArrayList array = new ArrayList();
        Long sum = 0L;
        for(Iterator iterator = objects.keySet().iterator(); iterator.hasNext();) {
            String seller_id = (String) iterator.next();
            sum += Math.round(Double.parseDouble(String.valueOf(objects.get(seller_id))));
        }
        for(Iterator iterator = objects.keySet().iterator(); iterator.hasNext();) {
            String seller_id = (String) iterator.next();
            double ratio = (Double.parseDouble(String.valueOf(objects.get(seller_id))) * 1.0 / sum * 1.0 )*100;
            hhi += Math.round(ratio * ratio);
            array.add(ratio);
        }

        Collections.sort(array, Collections.reverseOrder());

        double no1 = (double)array.get(0);
        double no2 = (double)array.get(1);
        double no3 = (double)array.get(2);
        double no4 = (double)array.get(3);

        cr3= Math.round(no1 + no2 + no3);

        HashMap hm = new HashMap();
        hm.put("cr3", cr3);
        hm.put("hhi", hhi);

        return hm;

    }
    /**
     * 계급나누기. 계급이름 구하기
     *
     * @param max 최대값
     * @param min 최소값
     * @param value 기준값
     * @param array_num 데이터수
     */
    public static String getHistNameSimple(double max, double min, double value, int array_num){

        String result_str = "";
        long max_limit = Math.round(max / 10000000);
        long value_round = Math.round(value/10000000);
        for(int j=1; j < max_limit+1; j++){
            if(value_round == j) {
                result_str = j + "천만원";
            }
        }
        return result_str;
    }

    /**
     * 계급나누기. 계급이름 구하기
     *
     * @param max 최대값
     * @param min 최소값
     * @param value 기준값
     * @param array_num 데이터수
     */
    public static String getHistName(double max, double min, double value, int array_num){

        String result_str = "";
        double class_num = Math.round(1 + 3.3 * Math.log(array_num * 1.0));
        double class_value = Math.round((max-min)  / (1.0 *class_num));
        for(int k=0; k < class_num; k++){
            double class_min = min + class_value * k;
            double class_max = min + class_value *(k+1);
            if(value >= class_min && value < class_max) {
                String min_comma = NumberFormat.getInstance().format(Math.round(class_min/10000000) * 10000000);
                String max_comma = NumberFormat.getInstance().format(Math.round(class_max/10000000) * 10000000);
                result_str  = min_comma + "~" + max_comma;
                break;
            }
        }
        return result_str;

    }

    /**
     * 프로덕트 매출 합산하기
     *
     * @param obj 매출 객체1
     * @param obj2 매출 객체2
     */
    public static org.json.simple.JSONObject sumYearRevenue(org.json.simple.JSONObject obj, org.json.simple.JSONObject obj2){

        Iterator<String> obj2_keys = obj2.keySet().iterator();

        while (obj2_keys.hasNext()) {
            String year = obj2_keys.next();
            if(obj.containsKey(year)) {

                ArrayList month_array1 = (ArrayList)obj.get(year);
                ArrayList month_array2 = (ArrayList)obj2.get(year);
                ArrayList<Long> month_arr = new ArrayList();

                for(int j=0; j < 12; j++){
                    month_arr.add(0L);
                }

                for(int i=0; i < 12; i++){
                    try {
                        long month_data = (long) Long.parseLong(String.valueOf(month_array1.get(i)));
                        long month_data2 = (long) Long.parseLong(String.valueOf(month_array2.get(i)));
                        long total_data = month_data + month_data2;
                        month_arr.set(i, total_data);
                    }catch(Exception ex){
                        month_arr.set(i,(long) Long.parseLong(String.valueOf(month_array2.get(i))));
                    }
                }
                obj.put(year, month_arr);
            }
            else {
                obj.put(year, obj2.get(year));
            }
        }

        return obj;
    }

    /**
     * ArrayList 변환
     *
     * @param arr 어레이리스트
     */
    public static String convertArrayListToJSONArrayString(ArrayList arr) {

        JSONArray new_array = new JSONArray();

        for(int i=0;i<arr.size();i++){

            org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
            HashMap hm = (HashMap) arr.get(i);
            Iterator<String> keys = hm.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value= (String) hm.get(key);
                obj.put(key,value);
            }
            new_array.add(obj);
        }

        return new_array.toJSONString();
    }
    /**
     * JSONObject 변환
     *
     * @param obj 제이슨오브젝트
     */
    public static HashMap convertJSONObjectToHashmap(org.json.simple.JSONObject obj) {
        HashMap<String,String> hm = new HashMap<>();
        Iterator<String> keys = obj.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            hm.put(key, (String)obj.get(key));
        }

        return hm;
    }
    public  static JSONArray makeRegressionGraph (org.json.simple.JSONObject year_obj) {

        JSONArray dataset = new JSONArray();
        for (Iterator iterator = year_obj.keySet().iterator(); iterator.hasNext(); ) {
            String year = (String) iterator.next();
            ArrayList arrays = (ArrayList) year_obj.get(year);
            for (int i = 0; i < 12; i++) {
                JSONArray array = new JSONArray();
                Long value = Long.parseLong(String.valueOf(arrays.get(i)));
                array.add(i+1);
                array.add(value);
                dataset.add(array);
            }
        }

        return dataset;
    }
    public static org.json.simple.JSONObject makeSeasonRevenueRatio(org.json.simple.JSONObject year_obj) {

        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        double spring = 0;
        double summer = 0;
        double fall = 0;
        double winter = 0;
        double sum = 0;
        for(Iterator iterator = year_obj.keySet().iterator(); iterator.hasNext();) {
            String year = (String) iterator.next();
            ArrayList arrays = (ArrayList)year_obj.get(year);
            for(int i=0; i < 12; i++){

                Long value = Long.parseLong(String.valueOf(arrays.get(i)));

                sum += value;

                if(i==2 || i==3 || i==4) {
                    spring += value;
                }
                if(i==5 || i==6 || i==7) {
                    summer += value;
                }
                if(i==8 || i==9 || i==10) {
                    fall += value;
                }
                if(i==11 || i==0 || i==1) {
                    winter += value;
                }
            }
        }

        spring = spring / sum;
        summer = summer / sum;
        fall = fall/sum;
        winter = winter / sum;

        obj.put("봄", spring);
        obj.put("여름", summer);
        obj.put("가을", fall);
        obj.put("겨울", winter);


        return obj;

    }
    public static JSONArray makeMonthlyRevenue(org.json.simple.JSONObject year_obj) {
        JSONArray array = new JSONArray();
        for(long t=0; t < 12; t++){
            array.add(t);
        }
        for(Iterator iterator = year_obj.keySet().iterator(); iterator.hasNext();) {
            String year = (String) iterator.next();
            ArrayList arrays = (ArrayList)year_obj.get(year);
            for(int i=0; i < 12; i++){
                Long value = Long.parseLong(String.valueOf(arrays.get(i)));
                Long value2 = (Long)array.get(i);
                array.set(i, value+value2);
            }

        }

        return array;
    }
}
