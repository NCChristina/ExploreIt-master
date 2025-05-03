package com.example.ExploreIt;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

//references
//https://www.kindsonthegenius.com/spring-boot/09-spring-boot-write-get-methods
// https://www.kindsonthegenius.com/spring-boot/09-spring-boot-write-get-methods

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingDetails {

    private static final String BOOKINGS_JSON_PATH = "./bookings.json";
    private static final String EVENTS_JSON_PATH = "src/main/resources/data/events.json";

    @GetMapping
    public String getAllBookings() {
        try {
            return new String(Files.readAllBytes(Paths.get(BOOKINGS_JSON_PATH)));
        } catch (IOException e) {
            return "[]";
        }
    }


// Create a new booking
    @PostMapping
    public String createBooking(@RequestBody String bookingJson) {
        try {
            JSONObject bookingRequest = new JSONObject(bookingJson);

            String eventId = bookingRequest.getString("eventId");
            String customerName = bookingRequest.getString("customerName");
            String customerEmail = bookingRequest.getString("customerEmail");
            int ticketCount = bookingRequest.getInt("ticketCount");
            String currency = bookingRequest.optString("currency", "GBP");

            boolean eventUpdated = updateEventTickets(eventId, ticketCount);
            if (!eventUpdated) {
                return "{\"error\": \"Unfortunately Event Tickets have been SOLD OUT \"}";
            }

            double ticketPrice = getEventTicketPrice(eventId);
            double totalPrice = ticketPrice * ticketCount;

            // Create booking object
            JSONObject newBooking = new JSONObject();
            newBooking.put("bookingId", "B" + UUID.randomUUID().toString().substring(0, 8));
            newBooking.put("eventId", eventId);
            newBooking.put("customerName", customerName);
            newBooking.put("customerEmail", customerEmail);
            newBooking.put("ticketCount", ticketCount);
            newBooking.put("totalPrice", totalPrice);
            newBooking.put("currency", currency);
            newBooking.put("bookingTime", LocalDateTime.now().toString());

            JSONArray bookings;
            try {
                String content = new String(Files.readAllBytes(Paths.get(BOOKINGS_JSON_PATH)));
                bookings = new JSONArray(content);
            } catch (IOException e) {
                // Create new array if file doesn't exist
                bookings = new JSONArray();
            }

            bookings.put(newBooking);

   // Save back to file
   Files.createDirectories(Paths.get(BOOKINGS_JSON_PATH).getParent());
   Files.write(Paths.get(BOOKINGS_JSON_PATH), bookings.toString(4).getBytes());

            return newBooking.toString();
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

 //  update available tickets for an event
    private boolean updateEventTickets(String eventId, int ticketsToBook) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
            JSONArray events = new JSONArray(content);
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                if (event.getString("eventId").equals(eventId)) {
                    int availableTickets = event.getInt("availableTickets");

                    if (availableTickets >= ticketsToBook) {
                        event.put("availableTickets", availableTickets - ticketsToBook);

                        // Save events to file
                        Files.write(Paths.get(EVENTS_JSON_PATH), events.toString(4).getBytes());
                        return true;
                    } else {
                        return false; // Not enough tickets
                    }
                }
            }

            return false; // Event not found
        } catch (IOException e) {
            return false;
        }
    }

    //  method to get ticket price for an event
    private double getEventTicketPrice(String eventId) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(EVENTS_JSON_PATH)));
            JSONArray events = new JSONArray(content);

            for (int i = 0; i < events.length(); i++) {
         JSONObject event = events.getJSONObject(i);
          if (event.getString("eventId").equals(eventId)) {
            return event.getDouble("ticketPrice");
                }
            }

            return 0.0; // Event not found
        } catch (IOException e) {
            return 0.0;
        }
    }
}