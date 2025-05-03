package com.example.ExploreIt;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MapBoxClient {
    private static final String ACCESS_TOKEN = "pk.eyJ1IjoiY2hyaXN0aW5hMTAwNiIsImEiOiJjbThhbHJ5NXowZmY0MmxyMGZnNGRhYWxzIn0.2fRuBt3LhuR72zTsDxfduw";
    private static final String BASE_URL = "https://api.mapbox.com/";

 public static String geocode(String address) throws Exception {
        String url = BASE_URL + "geocoding/v5/mapbox.places/" + URLEncoder.encode(address, StandardCharsets.UTF_8) + ".json?access_token=" + ACCESS_TOKEN;
        return fetchData(url);
    }

  public static String getDirections(String origin, String destination, String profile) throws Exception {
  String originCoords = extractCoordinates(geocode(origin));
  String destCoords = extractCoordinates(geocode(destination));
   if (originCoords == null || destCoords == null) return "Error: Unable to fetch coordinates";
       String url = BASE_URL + "directions/v5/mapbox/" + profile + "/" + originCoords + ";" + destCoords + "?access_token=" + ACCESS_TOKEN;
        return fetchData(url);
    }

    private static String fetchData(String urlString) throws Exception {
       HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
       conn.setRequestMethod("GET");
      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
        while ((line = reader.readLine()) != null) response.append(line);
             reader.close();
        return response.toString();
    }

    private static String extractCoordinates(String response) {
     int index = response.indexOf("\"coordinates\":[");
      if (index == -1) return null;
      return response.substring(index + 14, response.indexOf("]", index));
    }
}