package com.chh.ap.cs.simulator.device;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by niow on 2017/8/14.
 */
public abstract class Device {

    protected String serverIp = "127.0.0.1";

    protected int serverPort = 3088;

    protected Socket socket;

    public Device() {

    }

    public void connect() throws IOException {
        socket = new Socket(serverIp, serverPort);
    }

    protected void sendData(int[] data) throws IOException {
        byte[] msg =  new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            msg[i] = (byte)data[i];
        }
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(msg);
    }

    public abstract void sendLogin() throws IOException;



    public void close() throws IOException {
        socket.getOutputStream().close();
        socket.getInputStream().close();
        socket.close();
    }
}
