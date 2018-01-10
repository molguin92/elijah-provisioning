package edu.cmu.cs.cloudlet.android.application.graphics;

/**
 * Created by molguin on 2017-12-21.
 */

import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.*;

public class Synchronizer implements Runnable {
    private Thread t;
    private boolean running;

    private String host;
    private int port;
    private final Object mutex = new Object();

    public Synchronizer(String host, int port) throws Exception {
        running = true;
        this.host = host;
        this.port = port;
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        // set up connection to server
        try {
            Socket socket = new Socket(host, port);
            DataInputStream incoming = new DataInputStream(socket.getInputStream());
            DataOutputStream outgoing = new DataOutputStream(socket.getOutputStream());

            long time;

            while (true) {
                synchronized (mutex) {
                    if (!running) break;
                }

                incoming.readLong(); // wait for ping
                //time = System.currentTimeMillis();
                time = (new Date()).getTime();
                outgoing.writeLong(time);
                // Thread.sleep(25);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() throws InterruptedException {
        synchronized (mutex) {
            running = false;
        }

        t.join(100);
    }
}
