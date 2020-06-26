package com.zn.aio.demo;

import com.zn.bio.demo.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TimeServer 伪异步io
 *
 * @author ggzhangna
 * @date 20/6/25
 */
public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0){
            try{
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e){
                System.out.println("use default port.");
            }
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            Socket socket = null;

            TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50,10000);


            while (true){
                // 运行main时，程序会阻塞在这里
                // 可以通过idea的Run工具栏中的照相机看到程序的Dump信息，观察阻塞
                socket = server.accept();
                singleExecutor.execute(new TimeServerHandler(socket));
            }
        }finally {
            if (server != null){
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}
