package vn.cser21

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class GetORCached {
    var items: List<GetORCachedItem>? = null;
    var app21: App21? = null;
    var root: String? = null;

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPath(url: String): String {
        val segs = url.split('\\', '/', ':').filter { x -> x != "" };
        val cw = ContextWrapper(app21?.mContext?.applicationContext)

        if (root == null) {
            val directory = cw.getDir("profile", Context.MODE_PRIVATE);
            root = directory.path + "/getorcached";
            val appDirectory = File("$root")
            if (!appDirectory.exists()) {
                appDirectory.mkdir()
            }
        }
        var i: Int = 0;
        var path: String = "";
        root?.let {
            path = it;
        }
        while (i < segs.count() - 1) {
            path = "${path}/${segs[i]}";
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdir()
            }
            i++;
        }
        return path + "/" + segs[segs.count() - 1];
    }

    fun down(urlStr: String?, savePath: String, result: Result, app21: App21, returnType: Int) {


        // localPath = getCache(address);
        // if (localPath != null && !"".equals(localPath)) break;
        // 1. Declare a URL Connection
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection

        // 2. Open InputStream to connection
        conn.connect()
        val ins = conn.inputStream;
        // 3. Download and decode the bitmap using BitmapFactory
        val file: File = File(savePath)
        FileOutputStream(file).use { output ->
            val buffer =
                ByteArray(4 * 1024) // or other buffer size
            var read: Int

            while ((ins.read(buffer).also { read = it }) != -1) {
                output.write(buffer, 0, read)
            }

            output.flush()


            response(file,returnType, result, app21);
        }
    }

    private fun response(file: File, returnType: Int, result: Result, app21: App21) {
        //result.data = "file:/${file.path}"
        when (returnType) {
            0 -> {
                result.data = "file:/${file.path}"
            }

            1 -> {
                result.data = file.inputStream().readBytes().toString(Charsets.UTF_8)
            }

            else -> {
                result.data = "file://${file.path}"
            }
        }
        result.success = true;
        app21.App21Result(result);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handle(url: String?, type: Int, returnType: Int, result: Result, app21: App21) {
        if (this.app21 == null) {
            this.app21 = app21;
        }
        val path = url?.let { getPath(it) }
        val file = path?.let { File(it) };

        when (type) {
            0 -> {
                //get and cached
                path?.let {
                    down(url, it, result, app21, returnType);
                }

            }

            1 -> {
                //cached if not  get
                if (file?.exists() == true) {

                    response(file,returnType,result, app21);
                } else {
                    path?.let {
                        down(url, it, result, app21, returnType);
                    }
                }
            }

            2 -> {
                //only get
                path?.let {
                    down(url, it, result, app21, returnType);
                }
            }

            3 -> {
                //only cached
                if (file?.exists() == true) {
                    response(file,returnType,result, app21)
                } else {
                    result.data = "FILE_NOT_FOUND";
                    result.success = false;
                    app21.App21Result(result);
                }
            }

            4 -> {
                //delete cached
                if (file?.exists() == true) {
                    file.delete();
                    result.data = "deleted"
                    result.success = true;
                    app21.App21Result(result);
                } else {
                    result.data = "FILE_NOT_FOUND";
                    result.success = false;
                    app21.App21Result(result);
                }
            }




            else -> {
                result.data = "KHONG_HO_TRO_$type";
                app21.App21Result(result);
            }
        }
    }
}

class GetORCachedItem {
    var url: String? = null;
    var result: Result? = null;
}