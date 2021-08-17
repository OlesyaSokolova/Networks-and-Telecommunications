package ru.nsu.sokolova;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server
{
    ServerSocket serverSocket_;
    public Server(int port)
    {
        try
        {
            serverSocket_= new ServerSocket(port);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void startWorking()
    {
        System.out.println("Hello, I'm a server!\nI started working correctly and I'm waiting for files from clients.");
        int id = 0;
        while(!serverSocket_.isClosed())
        {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket_.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Connection newConn = new Connection(clientSocket, id);
            id++;
            newConn.startConversation();
        }
    }
}
