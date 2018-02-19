package edu.cmu.cs.cloudlet.android.application.graphics;

import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

    private class Trace {
        long timestamp;
        int size;
        int seq_id;
        byte[] data;
    }

    private String prefix;
    private Queue<Log> log_q;
    private Queue<Trace> trace_q;
    private final Object mtx = new Object();
    private Thread log_t;
    private boolean running;
    FileWriter log_file;
    DataOutputStream trace_file;

    long previous_trace_ts;


    public Logger(String prefix) throws IOException {
        File extStore = Environment.getExternalStorageDirectory();
        long current_time = System.currentTimeMillis();

        String log_filename = extStore.getAbsolutePath() + "/"
                + prefix + "_" + current_time + ".csv";
        String trace_filename = extStore.getAbsolutePath() + "/"
                + prefix + "_" + current_time + "_trace";

        File f_log = new File(log_filename);
        File f_trace = new File(trace_filename);

        log_file = new FileWriter(f_log);
        trace_file = new DataOutputStream(new FileOutputStream(f_trace));

        android.util.Log.i("Logger", "Logging to " + log_filename);
        log_file.append("# Logs for Fluid Client\n");
        log_file.append("# LOGS START\n");

        log_q = new ConcurrentLinkedQueue<>();
        trace_q = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        previous_trace_ts = -1;
        running = true;
        log_t = new Thread(this);
        log_t.start();
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
            log_file.write(s.toString());
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    private void writeTraceToFile(Trace t) {
        int dt;

        if (previous_trace_ts == -1)
            dt = 0;
        else
            dt = (int) (t.timestamp - previous_trace_ts);

        previous_trace_ts = t.timestamp;

        try {
            trace_file.writeInt(dt);
            trace_file.writeInt(t.seq_id);
            trace_file.writeInt(t.size);
            trace_file.write(t.data);
        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public void stop() throws InterruptedException, IOException {
        synchronized (mtx) {
            running = false;
        }
        log_t.join(100);

        Object logs[] = log_q.toArray();
        for (Object l : logs)
            this.writeLogToFile((Log) l);
        log_file.write("# LOGS END\n");
        log_file.close();

        Object traces[] = trace_q.toArray();
        for (Object l : logs)
            this.writeTraceToFile((Trace) l);
        trace_file.close();
    }

    @Override
    public void run() {

        while (true) {
            synchronized (mtx) {
                if (!running) break;
            }

            if (log_q.isEmpty() && trace_q.isEmpty()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    exit(-1);
                }
                continue;
            }

            if (!log_q.isEmpty())
                this.writeLogToFile(log_q.poll());
            if (!trace_q.isEmpty())
                this.writeTraceToFile(trace_q.poll());

        }

    }


    public void logMessage(int seq, int size) {
        Log l = new Log();
        l.timestamp = System.currentTimeMillis();
        l.seq_id = seq;
        l.size = size;

        log_q.offer(l);
    }

    public void logTrace(int seq, int size, byte[] data) {

        long timestamp = System.currentTimeMillis();

        Log l = new Log();
        l.timestamp = timestamp;
        l.seq_id = seq;
        l.size = size;
        log_q.offer(l);

        Trace t = new Trace();
        t.timestamp = timestamp;
        t.size = size;
        t.data = data;
        t.seq_id = seq;
        trace_q.offer(t);
    }
}
