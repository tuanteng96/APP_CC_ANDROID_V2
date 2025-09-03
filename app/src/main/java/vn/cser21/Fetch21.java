package vn.cser21;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import vn.cser21.Callback21;

public class Fetch21 {
    public void fetch(final String url,final Callback21 callback21){
        final AsyncTask execute = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                String _url = "";
                String step = "";
                try {
                    _url = url;

                    InputStream in = null;
                    URL url = new URL(_url);
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) url.openConnection();
                    step += "->conn";
                    // 2. Open InputStream to connection
                    conn.connect();
                    in = conn.getInputStream();
                    step += "->getInputStream";


                    try{
                        byte[] bytes = IOUtils.toByteArray(in);
                        String str = new String(bytes, "UTF-8");
                        if (callback21 != null)
                        {
                            callback21.lastResult = str;
                            callback21.ok();
                        }
                    }catch (Exception ex){
                        if (callback21 != null)
                        {
                            callback21.lastExp = ex;
                            callback21.no();
                        }
                    }


                } catch (NetworkOnMainThreadException netError) {
                    Log.i("NetThreadException:", step);
                    netError.printStackTrace();
                } catch (IOException e) {


                    e.printStackTrace();
                    if (callback21 != null) {
                        callback21.lastExp = e;
                        callback21.no();
                    }
                }
                return null;
            }
        };

        execute.execute();
    }
}
