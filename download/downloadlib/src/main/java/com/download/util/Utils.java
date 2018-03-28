package com.download.util;

/**
 * Created by peiboning on 2018/3/15.
 */

public class Utils {
    public static String formatByte(int byteNum){
        if(byteNum<=0){
            return "0 KB/S";
        }
        if(byteNum < 1024){
            return byteNum + " b/s";
        }else{
            byteNum = byteNum / 1024;
        }
        if(byteNum < 1024){
            return byteNum + " KB/s";
        }else{
            byteNum = byteNum/1024;
        }

        if(byteNum  < 1024){
            return byteNum + " MB/s";
        }else{
            byteNum = byteNum / 1024;
        }
        return byteNum + " GB/s";

    }
}
