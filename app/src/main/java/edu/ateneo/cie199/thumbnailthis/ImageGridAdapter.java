package edu.ateneo.cie199.thumbnailthis;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by francis on 4/21/17.
 */

/*
 *  The ImageGridAdapter is a customized adapter that can be used
 *  with a GridView to display a simple gallery of images.
 *
 *  To use this, simply create a new instance of ImageGridAdapter
 *  and then set the list of image file names that it should load
 *  from a Thumbnail Server accessible over the network.
 *  e.g.
 *
 *      ArrayList<String> mImageList = new ArrayList<>();
 *      mAdapter = new ImageGridAdapter(this);
 *      ...
 *      mAdapter.setData( mImageList );
 *
 */
public class ImageGridAdapter extends BaseAdapter {
    private ArrayList<String> mImageList = null;
    private Context mContext = null;

    public ImageGridAdapter(Context c) {
        mContext = c;
        return;
    }

    /*
     *  Sets the list of image file names to be downloaded from the
     *  server and loaded by this ImageGridAdapter into the GridView
     */
    public void setData(ArrayList<String> imgList) {
        mImageList = imgList;
        return;
    }

    /*
     *  Used by Android to determine what View should be loaded into a
     *  specific GridView element and what it should look like. In this
     *  case, it simply loads an image into an ImageView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /* Check if the list of image names has been set and is non-empty */
        if (mImageList == null) {
            return convertView;
        }

        if (mImageList.size() <= 0){
            return convertView;
        }

        /* Check if the 'position' parameter corresponds to a valid index
         *  for a data source in our list of image file names */
        String imgFileName = getItem(position);
        if (imgFileName.equals("")) {
            return convertView;
        }

        /* Load a layout from one of our XML files into where this GridView
         *  element should appear */
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_view_item, parent, false);
        }

        /* From the loaded layout, get the ImageView that we want to load
         *  an image from the Thumbnail server into */
        ImageView imgItem = (ImageView) convertView.findViewById(R.id.img_item);

        /* Get the full url where we can retrieve files from the thumbnail server */
        ThumbnailThisApp app = (ThumbnailThisApp) ((Activity) mContext).getApplication();
        String serverFilesUrl = app.getServerFilesUrl();

        /* To simplify the task of loading image files from the Thumbnail
         *  server, an external library/module, Picasso, is used.
         *
         *  For more info on this module, check out:
         *  https://square.github.io/picasso/
         */
        Picasso.with(mContext)
                .load(serverFilesUrl + imgFileName)
                .into(imgItem);

        return convertView;
    }

    /*
     *  Gets the current number of elements being displayed by
     *  this adapter in the GridView
     **/
    @Override
    public int getCount() {
        if (mImageList != null) {
            return mImageList.size();
        }
        return 0;
    }

    /*
     *  Gets the String value of the item at the specified position
     *  in this adapter's source data
     **/
    @Override
    public String getItem(int position) {
        if (mImageList != null) {
            return mImageList.get(position);
        }
        return "";
    }

    /*
     *  Gets the integer ID of the item at the specified position
     *  in this adapter's source data. Possibly redundant.
     **/
    @Override
    public long getItemId(int position) {
        if (mImageList != null) {
            if ((position > mImageList.size()) ||
                    (position < 0)) {
                return 0;
            }

            return position;
        }
        return 0;
    }

}
