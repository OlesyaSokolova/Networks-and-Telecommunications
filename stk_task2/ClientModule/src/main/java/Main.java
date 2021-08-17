public class Main
{
    public static void main(String[] args)
    {
        String filepath = args[0];
        int serverPort = Integer.valueOf(args[1]);
        String serverIPaddress = args[2];
        Client client = new Client(serverPort, serverIPaddress);
        client.sendFile(filepath);
    }
}
