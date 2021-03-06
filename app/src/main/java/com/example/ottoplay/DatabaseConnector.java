package com.example.ottoplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;


public class DatabaseConnector {
    private int port;
    private String hostName;
    private Socket socket;
    private ReentrantLock lock;

    private ArrayList<ArrayList<String>> processQueryResult(String queryResult) {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        String[] rows = queryResult.split(":");
        for (String r : rows) {
            ArrayList<String> row = new ArrayList<String>();
            String[] cols = r.split(",");
            for (String c : cols) {
                row.add(c);
            }
            results.add(row);
        }

        return results;
    }

    public DatabaseConnector(ReentrantLock lock) {
        this.lock = lock;
        port = 8889;
        hostName = "ottoplay.hopto.org";
    }

    public DatabaseConnector() {
        lock = null;
        port = 8889;
        hostName = "ottoplay.hopto.org";
    }

    public ArrayList<ArrayList<String>> requestData(String queryData) {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        String queryResult;

        try {
            if (lock != null) lock.lock();

            //make connection and get output stream
            socket = new Socket(hostName, port);
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);

            //first send length of query data
            int len = queryData.length();
            dos.writeInt(len);
            dos.write(queryData.getBytes(), 0, len);

            //receive db information
            InputStream in = socket.getInputStream();
            DataInputStream input = new DataInputStream(in);
            int count = input.readInt();
            byte[] buf = new byte[count];
            input.readFully(buf);
            queryResult = new String(buf, "UTF-8");
            socket.close();
            if (lock != null) lock.unlock();

            if (count > 0) results = processQueryResult(queryResult);

        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}

