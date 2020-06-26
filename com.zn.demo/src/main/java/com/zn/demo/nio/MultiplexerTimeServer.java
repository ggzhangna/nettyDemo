package com.zn.demo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * MultiplexerTimeServer
 *
 * @author ggzhangna
 * @date 20/6/26
 */
public class MultiplexerTimeServer implements Runnable{

    private Selector selector;

    private ServerSocketChannel servChannel;

    private volatile boolean stop;

    public MultiplexerTimeServer(int port) {
        try {
            // 创建一个serverSocketChannel，配置并绑定在selector上
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();
            servChannel.configureBlocking(false);
            servChannel.socket().bind(new InetSocketAddress(port),1024);
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("the time server is start in port: " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(){
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e){
                        if (key != null){
                            key.cancel();
                            if (key.channel() != null){
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 多路复用器关闭后，所有注册在上面的channel pipe等资源都会自动注册关闭
        if (selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()){
            if (key.isAcceptable()){
                // 也就是说，ServerSocketChannel并不是创建出来的是，是从SelectionKey中获取的
                // SelectionKey又是从Selector轮询获取的，Selector中有需要的一切吗？哈哈哈
                // 哦，不对，这个ServerSocketChannel是绑定在selector中的
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                try {
                    // SocketChannel又是从ServerSocketChannel中获取的，我晕了，这是什么关系？
                    // 相当于完成TCP三次握手
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (key.isReadable()){
                // 由此可以理解Channel是双向的，既可以获取ServerSocketChannel，又可以获取SocketChannel
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // 因为前面设置成了非阻塞，这里的read的也是非阻塞的
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0){
                    // flip作用：将缓冲区当前的limit设置为position,position设置为0，用于后续的读取操作
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes,"UTF-8");
                    System.out.println("The time server receive order:" + body);
                } else if (readBytes < 0){
                    key.cancel();
                    sc.close();
                } else {
                    // 没有读取到字节，属于正常场景，忽略
                    ;
                }
            }
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0){
            byte[] bytes = response.getBytes();
            // 开辟新的缓冲区
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            // socketChannel是异步非阻塞的，并不保证一次把所有的字节数组发送完
            // TODO 处理"半写包"
            // TODO 需要注册写操作，轮训selector将没有发送完的ByteBuffer发送完毕，并可以通过ByteBuffer.hasRemain()判断消息是否发送完成
            channel.write(writeBuffer);
        }
    }
}
