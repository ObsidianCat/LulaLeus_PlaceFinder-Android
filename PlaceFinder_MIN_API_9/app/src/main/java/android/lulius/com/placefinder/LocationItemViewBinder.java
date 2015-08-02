package android.lulius.com.placefinder;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class LocationItemViewBinder implements android.support.v4.widget.SimpleCursorAdapter.ViewBinder {
    public static final int DISTANCE_INDEX = 4;
    public static final double MILE_TO_KILOMETER_RATIO = 0.62137;
    private final SharedPreferences prefs;
    private float currentLat  ;
    private float currentLong ;
    DecimalFormat df = new DecimalFormat("#.00");

    public LocationItemViewBinder(String currentLat, String currentLong,  SharedPreferences sharedPref) {
        if (currentLat != null) {
            this.currentLat = Float.parseFloat(currentLat);
        }
        if (currentLong != null) {
            this.currentLong = Float.parseFloat(currentLong);
        }
        this.prefs = sharedPref;
    }

    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }

    /**
     * Binds the Cursor column defined by the specified index to the specified view.
     * <p/>
     * When binding is handled by this ViewBinder, this method must return true.
     * If this method returns false, SimpleCursorAdapter will attempts to handle
     * the binding on its own.
     *
     * @param view        the view to bind the data to
     * @param cursor      the cursor to get the data from
     * @param columnIndex the column at which the data can be found in the cursor
     * @return true if the data was bound to the view, false otherwise
     */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(columnIndex == DISTANCE_INDEX){ // let's suppose that the column 0 is the date
            setDistance((TextView) view, cursor);
            return true;
        }
        else if (columnIndex == 5) {
            String url = cursor.getString(5);
            ImageView placeIcon = (ImageView) view;
//            urlToImage(placeIcon, url);
            new DownloadImageTask(placeIcon).execute(url);

        }
        else if (columnIndex == 0) {
            TextView v = (TextView) view;
            v.setText(Integer.toString(cursor.getInt(0)));
        }
        else if (columnIndex == 6) {
            ImageView star = (ImageView) view;
            int isFav = cursor.getInt(6);
            if(isFav==0){
                star.setVisibility(View.GONE);
            }
            else{
                star.setVisibility(View.VISIBLE);
            }
            return true;


        }
        return false;
    }

    private void setDistance(TextView view, Cursor cursor) {
        float lng = Float.parseFloat(cursor.getString(cursor.getColumnIndex(PlacesContract.Places.LNG)));
        float lat = Float.parseFloat(cursor.getString(cursor.getColumnIndex(PlacesContract.Places.LAT)));
        String kmOrMile = this.prefs.getString("distanceUnits", "kilometers");
        double distance = haversine(this.currentLat, this.currentLong, lat, lng);

        if (kmOrMile.equals("miles")) {
            distance /= MILE_TO_KILOMETER_RATIO;
        }
        String formattedDistance = df.format(distance);
        if (distance < 1) {
            formattedDistance = "0"+formattedDistance;
        }
        view.setText(formattedDistance + kmOrMile);
    }


//    -- DownloadImageTask:
    class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        private ImageView imageView;
        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //in the background:

            //get the address from the params:
            String address = params[0];
            HttpURLConnection connection = null;
            InputStream stream = null;
            ByteArrayOutputStream outputStream = null;

            //the bitmap will go here:
            Bitmap b = null;

            try {
                // build the URL:
                URL url = new URL(address);
                // open a connection:
                connection = (HttpURLConnection) url.openConnection();

                // check the connection response code:
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    // not good..
                    return null;
                }

                // the input stream:
                stream = connection.getInputStream();

                // get the length:
                int length = connection.getContentLength();

                // a stream to hold the read bytes.
                // (like the StringBuilder we used before)
                outputStream = new ByteArrayOutputStream();

                // a byte buffer for reading the stream in 1024 bytes chunks:
                byte[] buffer = new byte[1024];

                int totalBytesRead = 0;
                int bytesRead = 0;

                //read the bytes from the stream
                while ((bytesRead = stream.read(buffer, 0, buffer.length)) != -1) {
                    totalBytesRead += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);
                }

                // flush the output stream - write all the pending bytes in its
                // internal buffer.
                outputStream.flush();

                // get a byte array out of the outputStream
                // theses are the bitmap bytes
                byte[] imageBytes = outputStream.toByteArray();

                // use the BitmapFactory to convert it to a bitmap
                b = BitmapFactory.decodeByteArray(imageBytes, 0, length);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    // close connection:
                    connection.disconnect();
                }
                if (outputStream != null) {
                    try {
                        // close output stream:
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return b;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        };
    }


}
