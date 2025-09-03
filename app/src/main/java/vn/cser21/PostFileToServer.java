package vn.cser21;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import vn.cser21.App21;
import vn.cser21.Result;

public class PostFileToServer extends AsyncTask<Result, Result, Result> {
    public App21 app21;

    class PostInfo {
        public String server;
        public String path;
        public String token;

    }

    public byte[] fileToBytes(File file) {
        //File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    protected Result doInBackground(Result... results) {
        // String rs = "{\"success\": true,\"data\":\"https://cser.vn/app/image/3M.jpg?t=1\"}";

        Result rs = new Result();

        for (Result result : results) {
            PostInfo postInfo = new Gson().fromJson(result.params, PostInfo.class);
            rs = result.copy();
            try {
                URL url = new URL(postInfo.server);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // InputStream in = connection.getInputStream();
                String auth = "Bearer " + postInfo.token;


                String boundary = UUID.randomUUID().toString();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                DataOutputStream request = new DataOutputStream(connection.getOutputStream());
                request.writeBytes("--" + boundary + "\r\n");
                request.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
                request.writeBytes("--" + boundary + "\r\n");


                String[] segs = postInfo.path.split("\\?")[0].split("/");
                String fname = segs[segs.length - 1];
                ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
                File directory = cw.getDir("profile", Context.MODE_PRIVATE);
                File file = new File(directory, fname);
                request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fname + "\"\r\n\r\n");
                request.write(fileToBytes(file));
                request.writeBytes("\r\n");
                request.writeBytes("--" + boundary + "--\r\n");
                request.flush();
                int respCode = connection.getResponseCode();

                switch (respCode) {
                    case 200:
                        rs.success = true;
                        rs.data = InputStreamToString(connection.getInputStream());
                        //all went ok - read response
                        break;
                    case 301:
                    case 302:
                    case 307:
                    default:
                        rs.success = false;
                        rs.error = "" + respCode;
                        ;
                        //do something sensible
                }

            } catch (Exception ex) {

            }
        }


        return rs;
    }

    public String InputStreamToString(InputStream inputStream) {
        try {
            InputStreamReader isReader = new InputStreamReader(inputStream);
            //Creating a BufferedReader object
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}


