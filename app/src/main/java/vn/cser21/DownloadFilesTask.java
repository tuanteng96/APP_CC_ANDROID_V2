package vn.cser21;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import vn.cser21.App21;

public class DownloadFilesTask extends AsyncTask<String, String, String> {
    public App21 app21;
    private static final String shareNamme = "downloaded";
    private String getCache(String url) {
        MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        return s.getString(url, null);

    }
    private void clearCache(String key) {
        MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        SharedPreferences.Editor editor = s.edit();
        if (key == null) editor.clear();
        else
            editor.remove(key);
        editor.apply();

    }
    private void setCache(String url, String localPath) {
       MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        SharedPreferences.Editor editor = s.edit();
        editor.putString(url, localPath);
        editor.apply();

    }
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }
    protected void onPostExecute(String localPath) {
        //showDialog("Downloaded " + result + " bytes");
    }
    public void clear(final String file, final Runnable callback) {
        final boolean delete_all = file == null || "".equals(file);
        String _fname = getCache(file); // file: có thể là url
        if (_fname == null) _fname = file;
        else {
            clearCache(file);
        }
        final String fname = _fname;
        new Runnable() {
            @Override
            public void run() {

                ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
                File directory = cw.getDir("profile", Context.MODE_PRIVATE);
                if (!directory.exists()) {
                    callback.run();
                    return;
                }
                File[] fs = directory.listFiles();
                for (File f : fs) {
                    try {
                        String[] Segs = fname.split("/");
                        String last = Segs[Segs.length - 1];
                        if (delete_all || f.getName() == last) {
                            boolean deleted = f.delete();
                        }
                    } catch (Exception ex) {

                    }
                }
                if (delete_all) clearCache(null);
                callback.run();
            }
        }.run();

    }
    @Override
    protected String doInBackground(String... addresses) {
        InputStream in = null;
        String localPath = null;
        try {
            for (String address : addresses) {

               // localPath = getCache(address);
               // if (localPath != null && !"".equals(localPath)) break;
                ;

                // 1. Declare a URL Connection
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 2. Open InputStream to connection
                conn.connect();
                in = conn.getInputStream();

                String nohash = address.split("\\#")[0];

                String[] segs = nohash.split("\\?")[0].split("/");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
                Date date = new Date(System.currentTimeMillis());
                String fname = formatter.format(date) + "-" + segs[segs.length - 1];
                ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
                File directory = cw.getDir("profile", Context.MODE_PRIVATE);
                if (!directory.exists()) {
                    directory.mkdir();
                }


                // 3. Download and decode the bitmap using BitmapFactory
                File file = new File(directory, fname);
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                    localPath = file.getAbsolutePath();
                    //setCache(address, localPath);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }


    public File getFile(String path) {
        String[] segs = path.split("\\?")[0].split("/");
        String fname = segs[segs.length - 1];
        ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return new File(directory, fname);
    }

    public Bitmap getBitmap(String path) {
        File f = getFile(path);
        return BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    public String getExt(String path) {
        String[] segs = path.split("\\?")[0].split("/");
        String fname = segs[segs.length - 1];
        String[] segs2 = fname.split("\\.");
        String ext = segs2[segs2.length - 1];
        return ext.toLowerCase();
    }

    public List<FileInfo> getlist() {

        String[] lst = new String[]{};
        ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        List<FileInfo> arr = new ArrayList<FileInfo>();
        if (directory.exists()) {

            for (File f : directory.listFiles()) {
                FileInfo x = new FileInfo();
                x.name = f.getName();
                x.len = f.length();
                x.abspath = f.getAbsolutePath();
                try {
                    BasicFileAttributes attr = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        attr = Files.readAttributes(Paths.get(x.abspath), BasicFileAttributes.class);
                        FileTime c = attr.creationTime();
                        FileTime l = attr.lastAccessTime();
                        x.create = new Date(c.toMillis());
                        x.last = new Date(l.toMillis());
                    } else {
                        File file = new File(x.abspath);
                        long lastModified = file.lastModified();
                        x.create = new Date(lastModified);
                        x.last = new Date(lastModified);
                    }


                } catch (IOException ex) {
                    // handle exception
                }
                arr.add(x);
            }
        }


        return arr;
    }

    public static String strBase64(String input){
        try{
            byte[] data = input.getBytes("UTF-8");
            return android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
            //return  Base64.getUrlEncoder().encodeToString(data);
        }catch (Exception ex){
            return "";
        }
    }

    public String toBase64(String localPath){

        try{
            File file = getFile(localPath);
            InputStream finput = new FileInputStream(file);
            byte[] fileBytes = new byte[(int)file.length()];
            finput.read(fileBytes, 0, fileBytes.length);
            finput.close();
            String imageStr = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imageStr = Base64.getUrlEncoder().encodeToString(fileBytes);
            }
            return  imageStr;
        }catch (Exception ex){
            return  null;
        }
    }

    public String GET_TEXT(String localPath) throws IOException {
        File file = getFile(localPath);
        InputStream finput = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        finput.read(fileBytes, 0, fileBytes.length);
        finput.close();

        String str = new String(fileBytes, "UTF-8");
        return str;
    }

    public static  String tryDecodeUrl(String input){
        String encode =  "encode:";
        if(input.startsWith(encode))
        {
            return  URLDecoder.decode(input.substring(encode.length()));
        }else{
            return  input;
        }
    }

    public String filenameFrom(String suffix) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date();
        String fname = formatter.format(date) + "-" + suffix;
        ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);

        File file = new File(directory, fname);
        return file.getAbsolutePath();
    }

    public  String saveUri(Uri sourceuri, String suffix)
    {
        String sourceFilename= sourceuri.getPath();
        String destinationFilename = filenameFrom(suffix);

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceuri.toString()));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  destinationFilename;
    }
     String saveVideoFileOnActivityResult(Intent videoIntent, String suff){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        File file = null;
        String rs ="";
        try {
            rs = filenameFrom(suff);
            file = new File(rs);
            AssetFileDescriptor videoAsset = app21.mContext.getContentResolver().openAssetFileDescriptor(videoIntent.getData(), "r");
            fis = videoAsset.createInputStream();
            //File videoFile = new File(Environment.getExternalStorageDirectory(),"<VideoFileName>.mp4");
            fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            //fis.close();
            //fos.close();
        } catch (IOException e) {
            // TODO: handle error
            e.printStackTrace();
        }finally{
            try {
                if(fis!=null)
                    fis.close();
                if(fos!=null)
                    fos.close();
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        return rs;
    }
    class FileInfo {

        public String name;

        public Date create;

        public Date last;
        public long len;
        public String abspath;
    }


}
