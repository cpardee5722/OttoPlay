package com.example.ottoplay.ui.login;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DatabaseConnector {
    private int port;
    private String hostName;
    private Socket socket;

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

    public DatabaseConnector() {
        port = 8889;
        hostName = "ottoplay.hopto.org";
    }

    public ArrayList<ArrayList<String>> requestData(String queryData) {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        String queryResult;

        try {
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

            int numRead = 0;
            while (numRead < count) {
                numRead += input.read(buf);
            }

            queryResult = new String(buf, "UTF-8");
            results = processQueryResult(queryResult);

            socket.close();
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

