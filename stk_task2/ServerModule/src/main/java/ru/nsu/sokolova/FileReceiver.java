package ru.nsu.sokolova;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileReceiver
{
    private DataInputStream socketInput_;
    private DataOutputStream socketOutput_;
    private int clientID_;
    public FileReceiver(Socket socket)
    {
        try
        {
            socketInput_ = new DataInputStream(socket.getInputStream());
            socketOutput_ = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void receiveFile(int clientID)
    {
        clientID_ = clientID;
        try
        {
            int digestSize = socketInput_.readInt();
            byte[] receivedDigest = new byte[digestSize];
            int receivedDigestSize = socketInput_.read(receivedDigest,0, digestSize);
            if(receivedDigestSize != digestSize)
            {
                System.out.println("#" + clientID_ +": Error while receiving file digest.");
                finishWork();
                return;
            }
            int fileSize = (int)socketInput_.readLong();
            String fileName = socketInput_.readUTF();
            String file = Consts.clientsFilesFolderPath + fileName;
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            long leftBytes = fileSize;
            byte[] buffer = new byte[Consts.PACK_SIZE];
            int receivedBytes = 0;
            long startTime = System.currentTimeMillis();
            long currentDif = 0;
            double instantSpeed = 0;
            double averageSpeed = 0;
            long previousTime = startTime;
            long globalDif = 0;
            while(leftBytes > 0)
            {
                long nextPackSize = Math.min(leftBytes, buffer.length);

                int count = socketInput_.read(buffer, 0, (int)nextPackSize);
                long currentTime = System.currentTimeMillis();
                currentDif = currentTime - previousTime;
                globalDif = currentTime - startTime;
                if (currentDif >= Consts.CHECK_TIME)
                {
                    instantSpeed = (double)count*1000/(currentDif);
                    averageSpeed = (double)receivedBytes*1000/(globalDif);
                    System.out.printf("#" + clientID_ +": Instant speed = %f b/sec | " +
                            "Average speed = %f b/sec | %f%% received.\n", instantSpeed, averageSpeed, (double)receivedBytes*100/fileSize);
                    previousTime = currentTime;
                }
                if (count <= 0)
                {
                    System.out.println("#" + clientID_ +": Something went wrong!");
                    finishWork();
                }

                fileOutputStream.write(buffer, 0, count);
                leftBytes -= count;
                receivedBytes += count;
            }
            long finishTime = System.currentTimeMillis();
            currentDif = finishTime - startTime;
            averageSpeed = (double)receivedBytes*1000/(currentDif);
            System.out.printf("#" +clientID_ + ": File was received in %f sec. Average speed = %f b/sec\n", (double)currentDif/1000, averageSpeed);

            boolean checkRes = checkFileDigest(file, receivedDigest);
            socketOutput_.writeLong((long)receivedBytes);
            socketOutput_.writeBoolean(checkRes);
            socketOutput_.flush();
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finishWork();
    }
    private boolean checkFileDigest(String file, byte[] receivedDigest)
    {
        boolean checkRes = false;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(Paths.get(file)));
            byte[] digest = md.digest();

            System.out.print("#" + clientID_ + ": Check showed that ");
            if(Arrays.equals(receivedDigest, digest))
            {
                System.out.print("file was received successfully.\n");
                checkRes =  true;
            }
            else
            {
                System.out.print("file was not received successfully.\n");
                checkRes =  false;
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return checkRes;
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
