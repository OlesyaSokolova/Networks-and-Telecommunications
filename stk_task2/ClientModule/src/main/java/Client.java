import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client
{
    private Socket serverSocket_;
    DataInputStream socketInput_;
    DataOutputStream socketOutput_;
    public Client(int serverPort, String serverIPaddress)
    {
        try {
            serverSocket_ = new Socket(serverIPaddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendFile(String filePath)
    {
        File fileToSend = new File(filePath);
        try
        {
            socketInput_ = new DataInputStream(serverSocket_.getInputStream());
            socketOutput_ = new DataOutputStream(serverSocket_.getOutputStream());

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(filePath)));
            byte[] digest = md.digest();
            socketOutput_.writeInt(digest.length);
            socketOutput_.write(digest, 0, digest.length);
            socketOutput_.writeLong(fileToSend.length());
            socketOutput_.writeUTF(fileToSend.getName());
            byte[] buffer = new byte[Consts.MAX_FILE_SIZE];
            int count;
            FileInputStream fis = new FileInputStream(fileToSend);
            System.out.println("Hello, world! I successfully connected to server.\n"+
                    "I'm going to send file: " + filePath);
            System.out.println("File size is: " + fileToSend.length() + " bytes");
            System.out.println("...");
            while((count = fis.read(buffer)) != -1)
            {
                socketOutput_.write(buffer, 0, count);
            }
            long checkFileSize = socketInput_.readLong();
            boolean checkRes = socketInput_.readBoolean();
            if(checkFileSize == fileToSend.length() && checkRes == true)
            {
                System.out.println(Consts.SUCCESS);
            }
            else
            {
                System.out.println(Consts.FAILURE);
            }
            socketOutput_.flush();
            fis.close();
            finishWork();
        } catch (IOException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    private void finishWork()
    {
        try
        {
            socketInput_.close();
            socketOutput_.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
