package com.functionhall.dao;

import com.functionhall.config.DatabaseConnection;
import com.functionhall.model.Booking;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingDAO {

    public int save(Booking booking) throws SQLException {
        if (!isAvailable(booking.getHallId(), booking.getEventDate())) {
            throw new IllegalArgumentException(
                    "This hall is already booked for the selected date.");
        }

        String sql = """
                INSERT INTO bookings
                (customer_name, email, phone, hall_id, event_date, guests,
                 decoration, food_required, food_items, special_request)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, booking.getCustomerName());
            ps.setString(2, booking.getEmail());
            ps.setString(3, booking.getPhone());
            ps.setInt(4, booking.getHallId());
            ps.setDate(5, Date.valueOf(booking.getEventDate()));
            ps.setInt(6, booking.getGuests());
            ps.setString(7, booking.getDecoration());
            ps.setBoolean(8, booking.isFoodRequired());
            ps.setString(9, booking.getFoodItems());
            ps.setString(10, booking.getSpecialRequest());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Booking was saved but no booking ID was returned.");
    }

    public boolean isAvailable(int hallId, String eventDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE hall_id = ? AND event_date = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setDate(2, Date.valueOf(eventDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    public BigDecimal getHallPrice(int hallId) throws SQLException {
        String sql = "SELECT price_per_day FROM halls WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        throw new IllegalArgumentException("Selected hall does not exist.");
    }

    public String getHallName(int hallId) throws SQLException {
        String sql = "SELECT name FROM halls WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        throw new IllegalArgumentException("Selected hall does not exist.");
    }

    public BigDecimal calculateAmount(Booking booking) throws SQLException {
        BigDecimal amount = getHallPrice(booking.getHallId());

        if ("With Decoration".equalsIgnoreCase(booking.getDecoration())) {
            amount = amount.add(new BigDecimal("5000.00"));
        }

        if (booking.isFoodRequired()) {
            amount = amount.add(
                    new BigDecimal("450.00")
                            .multiply(BigDecimal.valueOf(booking.getGuests())));
        }

        return amount;
    }
}
