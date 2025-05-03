package com.example.ExploreIt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class OpenStreetMapClient {

    // Overpass API  //https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_API_by_Example
    private static final String OVERPASS_API_URL = "https://overpass-api.de/api/interpreter";

//https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_API_by_Example
    public static String findNearbyPOIs(double lat, double lon, int radius, String poiType) throws IOException {// create an Overpass QL query
        String query = "[out:json];" +
                "node" +
                "[\"amenity\"=\"" + poiType + "\"]" +
                "(around:" + radius + "," + lat + "," + lon + ");" +
                "out body;";

//multiple POI types
        if (poiType.equals("all")) {
            query = "[out:json];" +
                    "(" +
                    "  node[\"amenity\"](around:" + radius + "," + lat + "," + lon + ");" +
                    "  node[\"tourism\"](around:" + radius + "," + lat + "," + lon + ");" +
                    "  node[\"shop\"](around:" + radius + "," + lat + "," + lon + ");" +
                    "  node[\"leisure\"](around:" + radius + "," + lat + "," + lon + ");" +
                    ");" +
                    "out body;";
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

        URL url = new URL(OVERPASS_API_URL + "?data=" + encodedQuery);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }
    public static String findNearbyPOIsByAddress(String address, int radius, String poiType) throws IOException {
        String nominatimUrl = "https://nominatim.openstreetmap.org/search?format=json&q=" +
                URLEncoder.encode(address, StandardCharsets.UTF_8.toString());

        URL url = new URL(nominatimUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "ExploreIt-App");

        String responseStr = getResponse(connection);

        int latStart = responseStr.indexOf("\"lat\":\"") + 7;
        int latEnd = responseStr.indexOf("\"", latStart);
        int lonStart = responseStr.indexOf("\"lon\":\"") + 7;
        int lonEnd = responseStr.indexOf("\"", lonStart);

        if (latStart > 7 && lonStart > 7) {
         double lat = Double.parseDouble(responseStr.substring(latStart, latEnd));
            double lon = Double.parseDouble(responseStr.substring(lonStart, lonEnd));

            // Now find POIs near these coordinates
            return findNearbyPOIs(lat, lon, radius, poiType);
        } else {
            return "{\"error\": \"Could not geocode address\"}";
        }
    }

//get response method
  private static String getResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }
}