package com.zn.demo.nio;

/**
 * TimeClient
 *
 * @author ggzhangna
 * @date 20/7/5
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0){
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                System.out.println("use default port.");
            }
        }

        new Thread(new TimeClientHandler("127.0.0.1",port),"timeClient-001").start();
    }
}
