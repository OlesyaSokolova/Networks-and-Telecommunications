import java.io.IOException;
import java.net.*;
import java.security.KeyStore;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    static DatagramPacket datagramToSend_;
    static MulticastSocket sender_, receiver_;
    static InetAddress group_;
    static long startTime_;
    static Map<String, Long> connections_ = new ConcurrentHashMap<String, Long>();
    public static void main(String [] args)
    {
        if(args.length != Consts.REQUIRED_ARGUMENTS_NUMBER)
        {
            System.out.println(Consts.INCORRECT_INPUT);
            return;
        }
        final String inetAddressName = args[Consts.FIRST_ARG_INDEX];
        try
        {
            group_ = InetAddress.getByName(inetAddressName);
            sender_ = new MulticastSocket();
            receiver_ = new MulticastSocket(Consts.PORT);
            receiver_.joinGroup(group_);
            sender_.joinGroup(group_);
            datagramToSend_ = new DatagramPacket(Consts.MESSAGE_TO_SEND.getBytes(), Consts.MESSAGE_TO_SEND.length(),
                    group_, Consts.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startTime_ = System.currentTimeMillis();
        Runnable recvRunnable = new Runnable() {
            public void run() {
                try {
                    while(true)
                    {
                        byte[] buf = new byte[Consts.BUF_SIZE];
                        DatagramPacket datagramToRecv = new DatagramPacket(buf, buf.length);
                        receiver_.receive(datagramToRecv);
                        connections_.put(datagramToRecv.getAddress().toString().substring(1) +":"+ datagramToRecv.getPort(), System.currentTimeMillis());
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };

        Runnable senderRunnable = new Runnable() {
            public void run() {
                try {
                    while(true)
                    {
                        sender_.send(datagramToSend_);
                        Thread.sleep(Consts.DELAY_TIME/2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable cleanerRunnable = new Runnable() {
            public void run()
            {
                while(true)
                {
                    printConnectionsInfo();
                    connections_.clear();
                    try {
                        Thread.sleep(Consts.DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        Thread senderThread = new Thread(senderRunnable);
        senderThread.start();
        Thread cleanerThread = new Thread(cleanerRunnable);
        cleanerThread.start();
        Thread receiverThread = new Thread(recvRunnable);
        receiverThread.start();
    }
    static void printConnectionsInfo()
    {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println(time.format(formatter));
        int counter = 0;
        try {
            ArrayList<String> myIPs = new ArrayList<String>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    myIPs.add(addr.getHostAddress());
                }
            }
            for(Map.Entry<String, Long> connection: connections_.entrySet())
            {
                for(int i = 0; i < myIPs.size(); i++)
                {
                    if(connection.getKey().contains(myIPs.get(i)))
                    {
                        if((Long)connection.getValue() - startTime_ > Consts.TTL)
                        {
                            connections_.remove(connection.getKey());
                        }
                        counter++;
                    }
                }
                System.out.println(connection.getKey());
            }
            System.out.println(counter + " copies of myself was detected.");
            System.out.println(Consts.INFO_DELIMITER+Consts.LINE_DELIMITER);
            } catch (SocketException e) {
                e.printStackTrace();
            }
    }
}
