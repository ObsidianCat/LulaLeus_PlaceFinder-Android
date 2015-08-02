package android.lulius.com.placefinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class GoogleAccess {
    public final static String SEARCH_API = "https://maps.googleapis.com/maps/api/place/";
    public final static String RESPONSE_TYPE = "json";
    public static final String API_KEY = "AIzaSyBvd8fv2efVLO4QGCxeJtBTKhw34m_ReKs";

    public static String searchPlace(String q, String searchType) {

        // query string:
        // (use a decoder for the query)
        String queryString = "";
        queryString += "?sensor=false";
        queryString += "&key="+API_KEY;
        try {
            queryString += "&query=" + URLEncoder.encode(q, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return getUrlResults(searchType, queryString);
    }


    //second signature of the method
    public static String searchPlace(String searchType, String currentLat, String currentLong, String keyword, String radius) {

        // query string:
        // (use a decoder for the query)
        String queryString = "";
        queryString += "?sensor=false";
        queryString += "&key="+API_KEY;
        queryString += "&location=" + currentLat +","+currentLong;
        queryString += "&radius="+radius;
        if(!keyword.equals("")){
            try {
                queryString += "&keyword=" + URLEncoder.encode(keyword, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return getUrlResults(searchType, queryString);
    }

    private static String getUrlResults(String searchType, String queryString) {
        BufferedReader input = null;
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();

        try {


            // prepare a URL object :

            //Log.d(TAG, SEARCH_API + queryString);
            URL url = new URL(SEARCH_API + searchType + RESPONSE_TYPE + queryString);

            // open a connection
            connection = (HttpURLConnection) url.openConnection();

            // check the result status of the conection:
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // not good.
                return null;
            }


            // get the input stream from the connection
            // and make it into a buffered char stream.
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // read from the buffered stream line by line:
            String line = "";
            while ((line = input.readLine()) != null) {
                // append to the string builder:
                response.append(line + "\n");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {

            // close the stream if it exists:
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // close the connection if it exists:
            if (connection != null) {
                connection.disconnect();
            }
        }

        // get the string from the string builder:
        return response.toString();
    }


}
