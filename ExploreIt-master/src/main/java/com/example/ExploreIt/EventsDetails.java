package com.example.ExploreIt;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*") // Allow requests from any origin for testing
public class EventsDetails {

    private static final String EVENTS_JSON_PATH = "D:/Downloads/NTU Assignments/SOCT/ExploreIt/src/main/resources/data/events.json";

    //to display all events
    @GetMapping
    public String getAllEvents() {
        try {

            return new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
        } catch (IOException e) {
         return "[]";
        }
    }


    // Get request by event ID
    @GetMapping("/{id}")
    public String getEventById(@PathVariable String id) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
            JSONArray events = new JSONArray(content);
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                if (event.getString("eventId").equals(id)) {
                    return event.toString();
                }
            }// if event is not found show not foudn message
            return "{\"error\": \"Event not found\"}";
        } catch (IOException e) {
            return "{\"error\": \"Error reading events file\"}";
        }
    }


 // search events functions using filters or keywords
    @GetMapping("/search")
    public String searchEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        try {
            String content = new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
            JSONArray allEvents = new JSONArray(content);
            JSONArray matchingEvents = new JSONArray();
    LocalDate searchDate = null;
    if (date != null && !date.isEmpty()) {
    searchDate = LocalDate.parse(date);
            }

            // Filter for events
            for (int i = 0; i < allEvents.length(); i++) {
         JSONObject event = allEvents.getJSONObject(i);
         boolean typeMatch = type == null || type.isEmpty() || event.getString("type").equalsIgnoreCase(type);

         boolean cityMatch = city == null || city.isEmpty() || event.getString("city").equalsIgnoreCase(city);

         boolean dateMatch = true;
                if (searchDate != null) {
                    LocalDate eventDate = LocalDate.parse(event.getString("date"));
                    dateMatch = eventDate.equals(searchDate);
                }

                // price range filter
         boolean priceMatch = true;
                double price = event.getDouble("ticketPrice");
                if (minPrice != null) {
                    priceMatch = price >= minPrice;
                }
                if (priceMatch && maxPrice != null) {
                    priceMatch = price <= maxPrice;
                }
                if (typeMatch && cityMatch && dateMatch && priceMatch) {
                    matchingEvents.put(event);
                }
            }

            return matchingEvents.toString();
        } catch (IOException e) {
            return "{\"error\": \"Error reading events file\"}";
        }
    }

    // Post method to add a new event
    @PostMapping
    public String addEvent(@RequestBody String eventJson) {
        try {
            JSONObject newEvent = new JSONObject(eventJson);

            // Generate event ID if not provided
            if (!newEvent.has("eventId") || newEvent.getString("eventId").isEmpty()) { // to generate id for event
                newEvent.put("eventId", "E" + UUID.randomUUID().toString().substring(0, 8));
            }
            if (!newEvent.has("imageUrl")) {
                newEvent.put("imageUrl", "");
            }
            JSONArray events; // events
            try {
                String content = new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
                events = new JSONArray(content);
            } catch (IOException e) {
                // incase file not found create new array.
                events = new JSONArray();
            }

            events.put(newEvent);
            Files.write(Paths.get(EVENTS_JSON_PATH), events.toString(4).getBytes());

            return newEvent.toString();
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}