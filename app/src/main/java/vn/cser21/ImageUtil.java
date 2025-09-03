package vn.cser21;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;

import vn.cser21.Result;

public class ImageUtil extends AsyncTask<Result, Result, Result> {

    public DownloadFilesTask downloadFilesTask;


    @Override
    protected Result doInBackground(Result... results) {
        Result rs = null;

        for (Result result : results) {
            rs = result.copy();
            try {
                ImageUtilInfo imageUtilInfo = new Gson().fromJson(result.params, ImageUtilInfo.class);

                String imgPath = "";
                File file = downloadFilesTask.getFile(imageUtilInfo.path);
                Bitmap bitmap = downloadFilesTask.getBitmap(imageUtilInfo.path);
                // ExifInterface exif = new ExifInterface(imgPath);


                Matrix matrix = new Matrix();
            /*
             int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
            */
                matrix.postRotate(imageUtilInfo.degrees);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                FileOutputStream out = new FileOutputStream(file.getAbsoluteFile());

                String ext = downloadFilesTask.getExt(imageUtilInfo.path);
                switch (ext) {
                    case "png":
                        rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        break;
                    default://jpg
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                        break;
                }

                rs.success = true;

            } catch (Exception ex) {
                rs.success = false;
                rs.error = ex.getMessage();
            }
        }


        return rs;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxWidth, float maxHeight,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxWidth / realImage.getWidth(),
                (float) maxHeight / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}

class ImageUtilInfo {
    public String path;
    public float degrees;
}
