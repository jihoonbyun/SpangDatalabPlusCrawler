package Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.util.*;

public class Utils {

    public static String StringReplace(String str){
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        str =str.replaceAll(match, "");
        return str;
    }


    public static HashMap sort (HashMap map){

        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                int comparision = (o1.getValue() - o2.getValue()) * -1;
                return comparision == 0 ? o1.getKey().compareTo(o2.getKey()) : comparision;
            }
        });

        HashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Iterator<Map.Entry<String, Integer>> iter = list.iterator(); iter.hasNext(); ) {
            Map.Entry<String, Integer> entry = iter.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void ThreadSleep(Thread _thread, int miliseconds) {
        try {
            _thread.sleep(miliseconds);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public static void execCommandSingle(String cmd, Process p, int sleeptime){

        BufferedWriter p_stdin = null;
        BufferedReader reader = null;

        try {
            p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(),  "euc-kr"));
            reader = new BufferedReader(new InputStreamReader(p.getInputStream(),  "euc-kr"));
            p_stdin.write(cmd);
            p_stdin.newLine();
            p_stdin.flush();
            Thread.sleep(sleeptime);
            while (reader.ready()) {
                CharBuffer cbuff = CharBuffer.allocate(1024);
                reader.read(cbuff);
                cbuff.flip();
                String msg = cbuff.toString();
                //System.out.println(msg);
                if (msg == null) {
                    //reader.close();
                    break;
                }
            }


        } catch (Exception e) {
            System.out.println("err in execCommandSingle");
            System.out.println(e);

        } finally {
            System.out.println("exec command!");
        }
    }

}
