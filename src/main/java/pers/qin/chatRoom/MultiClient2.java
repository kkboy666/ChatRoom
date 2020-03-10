package pers.qin.chatRoom;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

//1.客户端读取服务器端信息的线程
class ClientReadServer1 implements Runnable{
    private Socket socket;
    public ClientReadServer1(Socket socket){
        this.socket=socket;
    }
    @Override
    public void run() {
        //1.获取服务器端输入流
        try {
            Scanner scanner=new Scanner(socket.getInputStream());
            while(scanner.hasNextLine()){
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//2.客户端向服务器端发送信息的线程
class ClientSendServer1 implements Runnable{
    private Socket socket;
    public ClientSendServer1(Socket socket){
        this.socket=socket;
    }
    @Override
    public void run() {
        try {
            //1.获取服务器端的输出流
            PrintStream printStream=new PrintStream(socket.getOutputStream());
            //2.从键盘中输入信息
            Scanner scanner=new Scanner(System.in);
            while(true){
                String msg=null;
                if(scanner.hasNextLine()){
                    msg=scanner.nextLine();
                    printStream.println(msg);
                }
                if(msg.equals("exit")){
                    scanner.close();
                    printStream.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class MultiClient2 {
    public static void main(String[] args) throws IOException{
        //1.客户端连接服务器端,返回套接字Socket对象
        Socket socket=new Socket("127.0.0.1",6666);
        //2.创建读取服务器端信息的线程和发送服务器端信息的线程
        Thread read=new Thread(new ClientReadServer(socket));
        Thread send=new Thread(new ClientSendServer(socket));
        //3.启动线程
        read.start();
        send.start();
    }
}

