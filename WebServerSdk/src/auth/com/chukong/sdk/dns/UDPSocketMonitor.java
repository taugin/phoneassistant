package com.chukong.sdk.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

import com.chukong.sdk.common.Log;


/**
 * Authored by EagleDNS<a href="http://www.unlogic.se/projects/eagledns">
 * http://www.unlogic.se/projects/eagledns</a>
 *
 * @author yihua.huang@dianping.com
 * @date 2012-12-17
 */
public class UDPSocketMonitor extends Thread {

    private InetAddress addr;
    private int port;
    private static final short udpLength = 512;
    private DatagramSocket socket;

    private QueryProcesser queryProcesser;

    private ThreadPools threadPools;

    public UDPSocketMonitor(String host, int port) {
        super();
        try {
            this.addr = Inet4Address.getByName(host);
            this.port = port;
            queryProcesser = new QueryProcesser();
            socket = new DatagramSocket(port, addr);
        } catch (IOException e) {
            System.err.println("Startup fail, 53 port is taken or has no privilege. Check if you are running in root, or another DNS server is running.");
            System.err.println("Startup fail, 53 port is taken or has no privilege = " + e);
            System.exit(-1);
        }

        //this.setDaemon(true);
    }

    @Override
    public void run() {
        threadPools = new ThreadPools();
        ExecutorService executorService = threadPools.getMainProcessExecutor();
        Log.d(Log.TAG, "Starting UDP socket monitor on address "
                + this.getAddressAndPort());

        while (true) {
            try {

                byte[] in = new byte[udpLength];
                DatagramPacket indp = new DatagramPacket(in, in.length);
                indp.setLength(in.length);
                socket.receive(indp);
                executorService.execute(new UDPConnectionWorker(indp, queryProcesser, new UDPConnectionResponser(socket, indp)));
            } catch (SocketException e) {

                // This is usally thrown on shutdown
                Log.d(Log.TAG, "SocketException thrown from UDP socket on address "
                        + this.getAddressAndPort() + ", " + e);
                break;
            } catch (IOException e) {

                Log.d(Log.TAG, "IOException thrown by UDP socket on address "
                        + this.getAddressAndPort() + ", " + e);
            }
        }
        Log.d(Log.TAG, "UDP socket monitor on address " + getAddressAndPort()
                + " shutdown");
    }

    public String getAddressAndPort() {

        return addr.getHostAddress() + ":" + port;
    }
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch(Exception e) {
            
        }
    }
}
