package ru.nsu.sokolova;

import static ru.nsu.sokolova.Consts.*;

public class Main
{
    public static void main(String[] args)
    {
        int serverPort = Integer.valueOf(args[PORT_INDEX]);
        Server server = new Server(serverPort);
        server.startWorking();
    }
}
