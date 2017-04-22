package edu.ateneo.cie199.thumbnailthis;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by francis on 4/20/17.
 */

public class ThumbnailThisApp extends Application {
    private HttpClient mHttpClient = new DefaultHttpClient();
    private String mServer = "http://192.168.1.14:8000/"; /* TODO Set default server URL here */

    /*
     *  Returns the current thumbnail server URL
     */
    public String getServerUrl() {
        return mServer;
    }

    /*
     *  Returns the current thumbnail server's files URL
     */
    public String getServerFilesUrl() {
        return mServer + "files/";
    }

    /*
     *  Sets the thumbnail server URL to something else
     */
    public boolean setServer(String serverUrl) {
        mServer = serverUrl;
        Log.i("ThumbnailThisApp", "Server: " + serverUrl);
        return true;
    }

    /*
     *  Sends an image to the thumbnail server via Http POST through
     *  the "upload" resource path. Note that we cannot simply send
     *  images as is: we need to create a Multipart POST to contain
     *  the image before we are able to do so
     */
    public boolean sendImage(String imageFilePath) {
        try {
            /* Create the base HTTP POST request with the URL of the chat
             *  server plus the resource path where we can send messages */
            HttpPost httpPost = new HttpPost(mServer + "upload");

            /* Check first if our image file path points to a valid file */
            File imgFile = new File(imageFilePath);
            if (imgFile.exists() == false) {
                return false;
            }

            /* To send over images, we must use Multipart POST requests which
             *  contain both string data and binary (image file) data */

            /* Create the string part first */
            StringPart fileIdPart = new StringPart("file_id", imgFile.getName());

            /* Create the image file part of our request using the provided
             *  image file path */
            FilePart imgFilePart = new FilePart("file", imgFile);

            /* Combine the parts together inside the Http POST entity */
            Part parts[] = { fileIdPart, imgFilePart };
            HttpEntity multipartEntity = new MultipartEntity(parts);
            httpPost.setEntity(multipartEntity);

            /* Execute our HTTP POST request */
            HttpResponse response = mHttpClient.execute(httpPost);

            /* We can interpret the response here, but chose not to since
             *  it isn't that important to use now */
        } catch (Exception e) {
            Log.e("ThumbnailThisApp", "Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     *  Sends an Http GET request to the server requesting a list of files
     *  that is currently stored in the Thumbnail Server
     */
    public String sendServerSyncRequest() {
        String contents = "";

        /* Create the base HTTP GET request with the URL of the thumbnail
         *  server plus the resource path where we can retrieve a list
         *  of files that have been previously sent to the server */
        HttpGet request = new HttpGet(mServer + "list");

        try {
            /* Execute our HTTP GET request */
            HttpResponse response = mHttpClient.execute(request);

            /* Use the convenience class EntityUtils to turn the contents
             *  of the chat server's response to our HTTP GET request into
             *  a string */
            contents = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            Log.e("ThumbnailThisApp", "Exception occurred: " + e.getMessage());
            contents = "";
        }

        /* Pass the chat server's HTTP response contents to another function
         *  for further processing/parsing */
        return contents;
    }

    /*
     *  Saves a Bitmap object into a PNG format image file on the user's
     *  mobile phone given the file name specified as a parameter
     */
    public String saveImage(String fileName, Bitmap imageBmp) {
        /* Get storage path */
        String savePath = getStoragePath();

        /* Check if path exists */
        File pathStorage = new File(savePath);
        if (pathStorage.exists() == false) {
            /* Create the directory */
            pathStorage.mkdirs();
        }

        /* Check if the file exists */
        File saveFile = new File(savePath, fileName);
        if (saveFile.exists() == false) {
            try {
                saveFile.createNewFile();
            } catch (Exception e) {
                Log.e("FriendFaceApp", "Exception occurred: " + e.getMessage());
                return "";
            }
        }

        try {
            /* Get file output stream */
            FileOutputStream fos = new FileOutputStream(saveFile, false);

            /* Write the Bitmap to a file */
            imageBmp.compress(Bitmap.CompressFormat.PNG, 90, fos);

            /* close the output stream */
            fos.close();
        } catch (Exception e) {
            Log.e("ThumbnailThisApp", "Exception occurred: " + e.getMessage());
            return "";
        }

        /* Return the image path to the calling function */
        return saveFile.getAbsolutePath();
    }

    /*
     *  Returns the storage path configured for this app.
     *
     *  Note that you can manually set it to a folder on your mobile
     *  phone's SD Card so that images are automatically saved there
     *  instead.
     */
    private String getStoragePath() {
        //String storagePath = getFilesDir().toString();
        String storagePath = "/sdcard/Download/";
        return storagePath;
    }
}
