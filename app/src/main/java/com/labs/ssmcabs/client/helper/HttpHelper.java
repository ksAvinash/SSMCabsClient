package com.labs.ssmcabs.client.helper;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpHelper {


    public static class FetchMapDirectionsTask extends AsyncTask<Object, String, String>{
        private final String TAG = "MAPS_API_FETCH";
        Context mContext;

        public FetchMapDirectionsTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected String doInBackground(Object... objects) {
            LatLng source = (LatLng) objects[0];
            LatLng destination = (LatLng) objects[1];
            String maps_api_url = "https://maps.googleapis.com/maps/api/directions/json?origin="+
                    source.latitude+","+source.longitude+"&destination="
                    +destination.latitude+","+destination.longitude+"&key=AIzaSyBD2iC-g6uE7oTo7br8gcAQiuVHmA-8ub4";
            Log.d(TAG, maps_api_url);
            try{
                URL url = new URL(maps_api_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader data = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder builder = new StringBuilder();

                String line = "";
                while ((line = data.readLine()) != null) {
                    builder.append(line);
                }

                Log.d(TAG, "RESPONSE : "+builder);
                conn.disconnect();
                return builder.toString();
            }catch (Exception e){
                Log.d(TAG, "Error fetching team stats");
                Log.d(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            if(str == null)
                return;
            ParserMapDirectionsTask parserMapDirectionsTask = new ParserMapDirectionsTask(mContext);
            parserMapDirectionsTask.execute(str);
        }
    }

    private static class ParserMapDirectionsTask extends AsyncTask<String, String, List<List<HashMap<String, String>>>> {
        List<List<HashMap<String, String>>> routes;
        List<DistanceAndDurationAdapter> distanceAndDurationAdapter;

        JSONObject jObject;
        String directionMode = "driving";
        private final String TAG = "MAPS_API_PARSE";
        private final String TAG2 = "MAPS_API_POLYLINE";
        PolyLineTaskLoadedCallback taskCallback;

        private ParserMapDirectionsTask(Context mContext){
            this.taskCallback = (PolyLineTaskLoadedCallback) mContext;
        }


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            String api_response = strings[0];
            try {
                jObject = new JSONObject(api_response);
                routes = new ArrayList<>();
                distanceAndDurationAdapter = new ArrayList<>();

                JSONArray jRoutes;
                JSONArray jLegs;
                JSONArray jSteps;

                jRoutes = jObject.getJSONArray("routes");
                //Traversing all routes
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();
                    //Traversing all legs
                    for (int j = 0; j < jLegs.length(); j++) {
                                JSONObject distance =  ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                                JSONObject duration =  ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                                distanceAndDurationAdapter.add(new DistanceAndDurationAdapter(distance.getString("text"), duration.getString("text")));
                                jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        // Traversing all steps
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            // Traversing all points
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
                Log.d(TAG, "Executing routes");
                Log.d(TAG, routes.toString());
            } catch (Exception e) {
                Log.d(TAG, "parsing exception"+e.toString());
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);

            if(result == null)
                return;

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                if (directionMode.equalsIgnoreCase("walking")) {
                    lineOptions.width(10);
                    lineOptions.color(Color.MAGENTA);
                } else {
                    lineOptions.width(15);
                    lineOptions.color(Color.parseColor("#FF9800"));
                }
                Log.d(TAG2, "onPostExecute line options decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                //mMap.addPolyline(lineOptions);
                taskCallback.onTaskDone(lineOptions, distanceAndDurationAdapter);

            } else {
                Log.d(TAG2, "without Polylines drawn");
            }
        }

        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }

    }
}
