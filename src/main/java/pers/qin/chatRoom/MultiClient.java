package pers.qin.chatRoom;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
/*
  *  kkboy
 */
class ClientReadServer implements Runnable{
    //创建socket对象
    private Socket socket;
    public ClientReadServer(Socket socket){//构造方法
        this.socket = socket;
    }
    @Override
    //覆写run方法
    public void run(){
        try{
            //获取服务端发送的消息
            Scanner scanner = new Scanner(socket.getInputStream());
            //在客户端显示消息
            while(scanner.hasNextLine()){
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

class ClientSendServer implements Runnable{
    private Socket socket;
    public ClientSendServer(Socket socket){
    this.socket = socket;
    }
    @Override
    public void run() {
        try{
            //获取用户的socket对象的输出流
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            while(true){
                String msg = null;
                if(scanner.hasNextLine()){
                    msg = scanner.nextLine();
                    printStream.println(msg);
                }
                if(msg.equals("exit")){
                    scanner.close();
                    printStream.close();
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
public class MultiClient {
    public static void main(String[] args) throws IOException{
        Socket socket = new Socket("127.0.0.1",6666);
        Thread read = new Thread(new ClientReadServer(socket));
        Thread send = new Thread(new ClientSendServer(socket));
        //启动线程
        read.start();
        send.start();
    }
}

