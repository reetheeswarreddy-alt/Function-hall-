package com.functionhall.servlet;

import com.functionhall.dao.BookingDAO;
import com.functionhall.model.Booking;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

@WebServlet("/api/bookings")
public class BookingServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Booking booking = new Booking();

            booking.setCustomerName(required(request, "customerName"));
            booking.setEmail(required(request, "email"));
            booking.setPhone(required(request, "phone"));
            booking.setHallId(parsePositiveInt(request, "hallId"));
            booking.setEventDate(required(request, "eventDate"));
            booking.setGuests(parsePositiveInt(request, "guests"));
            booking.setDecoration(required(request, "decoration"));

            boolean foodRequired =
                    "true".equalsIgnoreCase(request.getParameter("foodRequired"));
            booking.setFoodRequired(foodRequired);

            String[] selectedFood = request.getParameterValues("foodItems");
            booking.setFoodItems(foodRequired && selectedFood != null
                    ? Arrays.stream(selectedFood)
                        .filter(s -> s != null && !s.isBlank())
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.joining(", "))
                    : "");

            booking.setSpecialRequest(trimToNull(request.getParameter("specialRequest")));

            validate(booking);

            BigDecimal amount = bookingDAO.calculateAmount(booking);
            String hallName = bookingDAO.getHallName(booking.getHallId());
            int bookingId = bookingDAO.save(booking);

            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response,
                    "{\"success\":true,\"bookingId\":" + bookingId +
                    ",\"hallName\":\"" + escape(hallName) +
                    "\",\"guests\":" + booking.getGuests() +
                    ",\"decoration\":\"" + escape(booking.getDecoration()) +
                    "\",\"foodRequired\":" + booking.isFoodRequired() +
                    ",\"foodItems\":\"" + escape(booking.getFoodItems()) +
                    "\",\"amount\":" + amount.toPlainString() +
                    ",\"message\":\"Booking submitted successfully. Please complete payment.\"}");

        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response,
                    "{\"success\":false,\"message\":\"Please enter a valid event date.\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response,
                    "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response,
                    "{\"success\":false,\"message\":\"Server/database error. Check MySQL settings and Tomcat logs.\"}");
            e.printStackTrace();
        }
    }

    private void validate(Booking booking) {
        if (booking.getGuests() > 5000) {
            throw new IllegalArgumentException("Guests cannot exceed 5000.");
        }

        LocalDate eventDate = LocalDate.parse(booking.getEventDate());
        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past.");
        }

        if (!booking.isFoodRequired()) {
            booking.setFoodItems("");
        } else if (booking.getFoodItems() == null || booking.getFoodItems().isBlank()) {
            throw new IllegalArgumentException("Please select at least one food item.");
        }

        if (!"With Decoration".equals(booking.getDecoration())
                && !"Without Decoration".equals(booking.getDecoration())) {
            throw new IllegalArgumentException("Please select a valid decoration option.");
        }

        if (!booking.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (!booking.getPhone().matches("^[0-9+()\\-\\s]{7,20}$")) {
            throw new IllegalArgumentException("Please enter a valid phone number.");
        }
    }

    private int parsePositiveInt(HttpServletRequest request, String name) {
        String value = required(request, name);
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a positive number.");
        }
    }

    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        response.getWriter().print(json);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
