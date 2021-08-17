package ru.nsu.sokolova;

import java.net.Socket;

public class Connection
{
    private Socket clientSocket_;
    private Thread threadListener_;
    private FileReceiver msgHandler_;
    private int id_;
    public Connection(Socket clientSocket, int id)
    {
        clientSocket_ = clientSocket;
        id_ = id;
        msgHandler_ = new FileReceiver(clientSocket);
        threadListener_ = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!clientSocket_.isClosed() && !threadListener_.isInterrupted())
                {
                    //System.out.println(Consts.INFO_DELIMITER);
                    //System.out.println("data from "+ id_ + " client:");
                    msgHandler_.receiveFile(id_);
                    //System.out.println(Consts.INFO_DELIMITER + Consts.LINES_DELIMITER);
                }
            }
        });
    }
    public void startConversation()
    {
        threadListener_.start();
    }
}
