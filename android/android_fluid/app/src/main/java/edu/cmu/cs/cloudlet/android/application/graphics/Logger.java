package edu.cmu.cs.cloudlet.android.application.graphics;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.System.exit;

/**
 * Created by molguin on 2017-12-22.
 */

public class Logger implements Runnable {

    private class Log {
        long timestamp;
        int seq_id;
        int size;
    }

    ;

    private String prefix;
    private Queue<Log> q;
    private final Object mtx = new Object();
    private Thread t;
    private boolean running;
    FileWriter file;


    public Logger(String prefix) throws IOException {
        File extStore = Environment.getExternalStorageDirectory();
        String filename = extStore.getAbsolutePath() + "/"
                + prefix + "_" + System.currentTimeMillis()
                + ".csv";
        File f = new File(filename);
        file = new FileWriter(f);
        android.util.Log.i("Logger", "Logging to " + filename);
        file.append("# Logs for Fluid Client\n");
        file.append("# LOGS START\n");

        q = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        running = true;
        t = new Thread(this);
        t.start();
    }

    private void writeLogToFile(Log l) {
        StringBuilder s = new StringBuilder();
        s.append(l.timestamp);
        s.append(",");
        s.append(l.seq_id);
        s.append(",");
        s.append(l.size);
        s.append("\n");
        try {
            file.write(s.toString());
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public void stop() throws InterruptedException, IOException {
        synchronized (mtx) {
            running = false;
        }
        t.join(100);
        Object logs[] = q.toArray();
        for (Object l : logs)
            this.writeLogToFile((Log) l);
        file.write("# LOGS END\n");
        file.close();
    }

    @Override
    public void run() {

        while (true) {
            synchronized (mtx) {
                if (!running) break;
            }

            if (q.isEmpty()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    exit(-1);
                }
                continue;
            }
            this.writeLogToFile(q.poll());
        }

    }


    public void logMessage(int seq, int size) {
        Log l = new Log();
        l.timestamp = System.currentTimeMillis();
        l.seq_id = seq;
        l.size = size;

        q.offer(l);
    }
}
