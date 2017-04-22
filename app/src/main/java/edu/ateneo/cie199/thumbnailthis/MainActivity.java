package edu.ateneo.cie199.thumbnailthis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int MENU_OPTION_CAPTURE = 0;
    private final int MENU_OPTION_SYNC_NOW = 1;
    private final int MENU_OPTION_SET_SERVER = 2;

    private static final int REQUEST_IMAGE_CAPTURE = 7770; // ARBITRARY

    private ImageGridAdapter mAdapter = null;
    private SyncWithServerTask mSyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* A custom adapter is used so that we can work with the
         *  GridView which shows items in a gallery-like format
         *  ideal for displaying things like images.
         *
         * See ImageGridAdapter.java for more info */
        mAdapter = new ImageGridAdapter(this);

        /* Bind the adapter to the GridView in our layout */
        GridView gridView = (GridView) findViewById(R.id.grid_images);
        gridView.setAdapter(mAdapter);

        /* Finally, request to synchronize the list of stored files
         *  with the Thumbnail server */
        requestServerSync();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        /* The following lines add items to the menu. The only
         *  important things here are:
         *      the 2nd argument (item id),
         *      the 3rd argument (order in menu), and
         *      the last argument (menu item text) */
        menu.add(0, MENU_OPTION_CAPTURE, 0, "Capture Thumbnail");
        menu.add(0, MENU_OPTION_SYNC_NOW, 0, "Sync with Server");
        menu.add(0, MENU_OPTION_SET_SERVER, 1, "Set Thumbnail Server");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_OPTION_CAPTURE:
                /* Send an Intent to the main Android system requesting for
                 * image capture functionality. Here, startActivityForResult(...)
                 * is used since we want to get the final result of that action
                 * which is the 'image' from an 'Image Capture' app */
                Intent launchCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(launchCameraIntent, REQUEST_IMAGE_CAPTURE);
                break;
            case MENU_OPTION_SET_SERVER:
                displaySetServerDialog();
                break;
            case MENU_OPTION_SYNC_NOW:
                requestServerSync();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     *  This function is used in with startActivityForResult(...)
     *  to process the results of the launched activity.
     *
     *  In this case, the launched activity is the Android phone's
     *  camera application.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Check if the result is from the Image Capture that we requested
         *  earlier from the main Android system */
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            /* Retrieve the Bitmap object from the data intent. This
             *  Bitmap contains a small picture that was taken with
             *  the Android phone's camera app */
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            /* Call on the application object to save this Bitmap object
             *  to a PNG format image file */
            ThumbnailThisApp app = (ThumbnailThisApp) this.getApplication();
            String imgFilePath = app.saveImage("test_image.png", imageBitmap);

            /* After saving the image, send it to the Thumbnail server as well */
            Toast.makeText(MainActivity.this, "Sending image...", Toast.LENGTH_SHORT).show();
            SendImageTask sendImgTask = new SendImageTask();
            sendImgTask.execute(imgFilePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
        return;
    }

    /*
     *  Creates and executes a request to synchronize data with the server
     */
    private void requestServerSync() {
        if (mSyncTask == null) {
            Toast.makeText(this, "Synchronizing with server...", Toast.LENGTH_SHORT).show();
            mSyncTask = new SyncWithServerTask();
            mSyncTask.execute();
        } else {
            Toast.makeText(this, "Sync ongoing...", Toast.LENGTH_SHORT).show();
        }
        return;
    }

    /* Builds a dialog box for setting the chat server from scratch and
     *  then displays it to the user. The result is saved in the
     *  ThumbnailThisApp object as the new thumbnail server URL */
    private void displaySetServerDialog() {
        /* Prepare an Editable Text Field from scratch. This text
         *  field will contain the user's choice of chat server URL */
        final ThumbnailThisApp app = (ThumbnailThisApp) MainActivity.this.getApplication();
        final EditText edtServer = new EditText(this);
        edtServer.setText(app.getServerUrl()); // Displays the existing chat server

        /* Instantiate an Alert Dialog Box builder. This is how most
         *  Android Dialog Boxes are created on-the-fly */
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

        /* Use the builder to set the different parameters of your dialog
         *  box. What we're using here are:
         *      Title   - the text at the top of the dialog box
         *      Message - text inside the dialog box
         *      View    - a "View" object inside the dialog box; in this
          *               case, we set it to the Editable Text Field we
          *               created earlier
          *     Pos Btn - Just a button really; you can "listen" for when
          *               the user clicks it so that the app can react
          *               appropriately
          *     Neg Btn - Same as Pos Btn
          */
        dlgBuilder.setTitle("Set Server")
                .setMessage("Enter your thumbnail server URL:")
                .setView(edtServer)
                .setPositiveButton("OK",
                        /* Dialog Buttons can have click listeners attached like this one */
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String server = edtServer.getText().toString();
                                if (server.equals("") == false) {
                                    /* Propagate the new server URL to the
                                     *  ThumbnailThisApp object */
                                    app.setServer(server);
                                }

                                dialog.cancel(); // Closes the dialog box
                                return;
                            }
                        })
                .setNegativeButton("Cancel",
                        /* Dialog Buttons can have click listeners attached like this one */
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel(); // Closes the dialog box
                                return;
                            }
                        });

        /* Call the show() function on the builder to display the dialog
         *  box to the user. THIS IS VERY IMPORTANT. */
        dlgBuilder.show();
        return;
    }

    /*
     *  An AsyncTask for sending an image to the server through an Http
     *  POST request. The path to an image file on the user's mobile is
     *  provided to this AsyncTask during execution.
     *  e.g.
     *      String path = "/path/to/file/image.png";
     *
     *      SendImageTask sendImgTask = new SendImageTask();
     *      sendImgTask.execute(path);
     */
    private class SendImageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String imgFilePath = params[0];

            /* Call the sendImage(...) function on the app object to
             * send the image file whose path is given by 'imgFilePath'
             */
            ThumbnailThisApp app = (ThumbnailThisApp) MainActivity.this.getApplication();
            app.sendImage(imgFilePath);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MainActivity.this, "File sent!",Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
            return;
        }
    }

    /*
     *  An AsyncTask for synchronizing the list of available image files
     *  on the Thumbnail server
     */
    private class SyncWithServerTask extends AsyncTask<Void, Void, Void> {
        ArrayList<String> imgFileList = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {
            /* Send a request to the Thumbnail server for a list of image files
             *  that it is currently stored in it*/
            ThumbnailThisApp app = (ThumbnailThisApp) MainActivity.this.getApplication();
            String response = app.sendServerSyncRequest();

            /* Check if we received an invalid response from the server */
            if (response.equals("")) {
                /* If the received response is invalid, attempt to load an
                 *  old response saved through SharedPrefs */
                response = loadOldServerResponse();
            } else {
                /* IF this is a valid response, save it to SharedPrefs as
                 *  well in case we need to use it again in the future when
                 *  the server is not available */
                saveServerResponse(response);
            }

            /* Parse the response into a list of image file names (hence why we
             *  store these as Strings) that we need to load into our GridView
             *  gallery */
            String results[] = response.split(",");
            for (String s : results) {
                imgFileList.add(s);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /* If the new list of image file names isn't empty,
             *  then we proceed with loading that new set of
             *  data into our adapter. */
            if (imgFileList.size() > 0) {
                Toast.makeText(MainActivity.this,
                        "Sync finished. Downloading images...",
                        Toast.LENGTH_SHORT).show();

                mAdapter.setData(imgFileList);
                mAdapter.notifyDataSetChanged();
            }

            mSyncTask = null;

            super.onPostExecute(aVoid);
            return;
        }

        /*
         * A utility function for saving the response received from
         * the server into this app's SharedPrefs file. This might
         * be useful if ever the server becomes unavailable and we
         * have to load the list of images from memory instead
         */
        private void saveServerResponse(String response) {
            SharedPreferences prefs =
                    getSharedPreferences("edu.ateneo.cie199.thumbnailthis", MODE_PRIVATE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("LastSyncString", response);
            editor.commit();

            return;
        }

        /*
         * A utility function for loading back an old response
         * from the server in case it isn't available. This ensures
         * that we can reload the old images even if we don't have
         * any server connection
         */
        private String loadOldServerResponse() {
            SharedPreferences prefs =
                getSharedPreferences("edu.ateneo.cie199.thumbnailthis", MODE_PRIVATE);
            return prefs.getString("LastSyncString", "");
        }
    }
}
