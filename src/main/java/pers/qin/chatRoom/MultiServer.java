package pers.qin.chatRoom;


import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
  *  kkboy
 */
class Server implements Runnable{
    private Socket socket;
    //建立ConcurrentHashMap数据结构，用来保存用户名和socket对象
    private static Map<String , Socket> map = new ConcurrentHashMap<>();
    public Server(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run(){
        try {
            //获取客户端的输入
            Scanner scanner = new Scanner(socket.getInputStream());
            String msg =null ;
            while(true){
                //判断输入是否为空，不为空取出信息
                if(scanner.hasNextLine()) {
                    msg = scanner.nextLine();
                    //构建正则表达式
                    Pattern pattern = Pattern.compile("\r");
                    //分割字符
                    Matcher matcher = pattern.matcher(msg);
                    //msg.replaceAll("");
                    //判断信息开始字符
                    if(msg.startsWith("userName")){
                        //分割字符，并取后面的字符
                        String userName = msg.split("\\:")[1];
                        //调用注册函数
                        userRegist(userName,socket);
                        continue;
                    }else if(msg.startsWith("exit")){
                        //验证判断用户是否注册
                         firstStep(socket);
                         //调用退出函数
                         userExit(socket);
                         continue;
                     }else if(msg.startsWith("G:")){
                        firstStep(socket);
                        String str = msg.split("\\:")[1];
                        groupChat(socket , str);
                        continue;
                    }else if(msg.startsWith("P:")&&msg.contains("-")){
                        firstStep(socket);
                        String userName = msg.split("\\:")[1].split("-")[0];
                        String str = msg.split("\\:")[1].split("-")[1];
                        privateChat(socket , userName , str);
                        continue;
                    }else {
                        PrintStream printStream = new PrintStream(socket.getOutputStream());
                        printStream.println("输入格式错误!请按照以下格式输入!");
                        printStream.println("注册用户格式:[userName:用户名]");
                        printStream.println("群聊格式:[G:群聊信息]");
                        printStream.println("私聊格式:[P:userName-私聊信息]");
                        printStream.println("用户退出格式[包含exit即可]");
                        continue;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /*
    检测用户是否已经注册
     */
    public void firstStep(Socket socket) throws IOException{
        //将Map集合转变成Set集合
        Set<Map.Entry<String , Socket>> set = map.entrySet();
        for(Map.Entry<String , Socket> entry : set){
            if(entry.getValue().equals(socket) && entry.getKey() == null){
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.println("请首先注册用户##############################");
                printStream.println("注册格式为：[userName:用户名]");
            }
        }
    }
    /*
  用户注册
   */
    public void userRegist(String userName , Socket socket){
        try {
            //将用户名、socket对象放到Map集合中
            map.put(userName, socket);
            System.out.println("[用户名为" + userName + "][客户端为]" + socket + "上线了！");
            //将Map集合转变成Set集合
            Set<Map.Entry<String, Socket>> set = map.entrySet();
            //遍历集合
            for (Map.Entry<String, Socket> entry : set) {
                //获取socket
                Socket client = entry.getValue();
                PrintStream printStream = new PrintStream(client.getOutputStream());
                if(entry.getKey() != userName) {
                     printStream.println("[用户名为" + userName + "][客户端为]" + socket + "上线了！");
                }
                printStream.println("当前在线人数为:"+map.size()+"人");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /*
    用户私聊
     */
    public void privateChat(Socket socket , String userName , String msg) throws IOException{
        String curUser = null ;
        Set<Map.Entry<String , Socket >> set = map.entrySet();
        for(Map.Entry<String , Socket> entry : set){
            //获取发送消息的对象用户名
            if(entry.getValue().equals(socket)){
                curUser = entry.getKey();
                break;
            }
        }
        //获取私发给指定用户的socket
        Socket client = map.get(userName);
        //获取指定用户的socket的输出流
        PrintStream printStream = new PrintStream(client.getOutputStream());
        printStream.println(curUser+"私聊说："+msg);
    }
    /*
    用户群聊
     */
    public void groupChat(Socket socket , String msg) throws IOException{
        String curUser = null ;
        Set<Map.Entry<String , Socket>> set = map.entrySet();
        for(Map.Entry<String , Socket> entry : set){
            if(entry.getValue().equals(socket)){
                curUser = entry.getKey();
                break;
            }
        }
        for(Map.Entry<String , Socket> entry : set) {
            //给除了该用户之外的其他用户发送消息
            if (curUser != entry.getKey()) {
                Socket client = entry.getValue();
                PrintStream printStream = new PrintStream(client.getOutputStream());
                printStream.println(curUser + "群聊说：" + msg);
            }
        }
    }
    /*
    用户退出
     */
    public void userExit(Socket socket){
        String curUser = null ;
        //获取socket对应的用户名
        for(String key : map.keySet()){
            if(map.get(key).equals(socket)){
                curUser = key;
                break;
            }
        }
        //使用Iterator.remove()移除指定用户，若直接使用Map.remove()的方法将报错。
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            if(curUser == iterator.next()){
                iterator.remove();
                System.out.println("用户："+curUser+"退出聊天室！！！");
            }
        }
    }
}


public class MultiServer {
    public static void main(String[] args){
        try{
            //实例化服务器，设置服务器的端口
            ServerSocket serverSocket = new ServerSocket(6666);
            //建立地址池，设置地址池大小
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            for(int i = 0; i < 20 ; i++){
                System.out.println("欢迎来到聊天室###########################");
                //监听有无客户端的连接
                Socket socket = serverSocket.accept();
                System.out.println("有新朋友加入###########################");
                //将新线程发放到地址池中
                executorService.execute(new Server(socket));
            }
            //关闭地址池和服务器
            executorService.shutdown();
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}


