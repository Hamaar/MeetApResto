package com.hamaar.meetapresto.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import id.zelory.compressor.Compressor;

import static com.hamaar.meetapresto.Utils.GlobalVars.BASE_DIR;
import static com.hamaar.meetapresto.Utils.GlobalVars.EXTERNAL_DIR_FILES;
import static com.hamaar.meetapresto.Utils.GlobalVars.PICTURES_DIR_FILES;
import static com.hamaar.meetapresto.Utils.GlobalVars.imagesPath;

/**
 * Created by hilmi on 22/12/2018.
 */

public class GlobalHelper {


    public static File compressFoto(Context context, File actualImage) {

        final String path = imagesPath;

        File compressedImage = new Compressor.Builder(context)
                .setMaxWidth(1280)
                .setMaxHeight(1024)
                .setQuality(85)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(path)
                .build()
                .compressToFile(actualImage);

        deleteRecursive(actualImage);

        return compressedImage;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static Uri convertFileToContentUri(Context context, File file) throws Exception {
        //Uri localImageUri = Uri.fromFile(localImageFile); // Not suitable as it's not a content Uri
        ContentResolver cr = context.getContentResolver();
        String imagePath = file.getAbsolutePath();
        String imageName = null;
        String imageDescription = null;
        String uriString = MediaStore.Images.Media.insertImage(cr, imagePath, imageName, imageDescription);
        System.out.println(uriString);
        Log.e("Uri ", uriString);
        return Uri.parse(uriString);
    }

    public static String getMimeTypeFromUri(Context context, Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(uri));

        return type;
    }

    public static String encodeFileBase64(String filePath) {
        File file = new File(filePath);  //file Path
        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            for (int j = 0; j < b.length; j++) {
                System.out.print((char) b[j]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            //System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }

        byte[] byteFileArray = new byte[0];
        try {
            byteFileArray = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String base64String = "";
        if (byteFileArray.length > 0) {
            base64String = Base64.encodeToString(byteFileArray, Base64.NO_WRAP);
            //Log.i("File Base64 string", "IMAGE PARSE ==>" + base64String);
        }

        return base64String;
    }

    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }

    public static void createFolder() {
        File folder = new File(BASE_DIR + EXTERNAL_DIR_FILES + "/db");
        File pictures = new File(BASE_DIR + PICTURES_DIR_FILES);
        File imageFolder = new File(imagesPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        if (!imageFolder.exists()) {
            imageFolder.mkdir();
        }

        if (!pictures.exists()) {
            pictures.mkdir();
        }
    }
}
