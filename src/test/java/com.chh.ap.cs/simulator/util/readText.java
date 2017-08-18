package com.chh.ap.cs.simulator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by 申卓 on 2017/8/18.
 */
public class readText {
    private static final String filePath = "src/test/log4data.log";

    private static List<String> list = new LinkedList<String>();

    public static void main(String[] args) {
        readTxtFile(filePath);
    }

    public static List<String> readTxtFile(String filePath) {
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(deal(lineTxt));
//                    System.out.println(deal(lineTxt));
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
            return list;
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return null;
    }

    public static String deal(String str) {
        int n = str.length();
        int index = str.indexOf("data");
        if (index < 0) {
            return null;
        }
        index += 7;
        return str.substring(index, n - 2);
    }
}
