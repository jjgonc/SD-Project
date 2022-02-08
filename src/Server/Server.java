package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Model.Info;

public class Server {

    ReentrantLock l = new ReentrantLock();
    Condition c = l.newCondition();




    public void wakeup() {

        this.l.lock();
        try{
            this.c.signalAll();
        }
        finally {
            this.l.unlock();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {


        Server s = new Server();

        ServerSocket serverSocket = new ServerSocket(8888);
        Info info = new Info(s);


        while (info.isOnline()) {

            try{
                serverSocket.setSoTimeout(10000);
                Socket socket = serverSocket.accept();
                new Thread(new ServerConnection(new TaggedConnection(socket), info)).start();
            }catch (Exception ignored){

            }

        }

     s.l.lock();

     try{
         while(info.getUsersLogged() != 0){
             System.out.println("awaiting");
             s.c.await();
         }
     }

     finally {
         s.l.unlock();
     }


        info.saveData("saves");


        System.out.println("Servidor Encerrado");

    }

}
