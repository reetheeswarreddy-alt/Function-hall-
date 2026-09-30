package com.functionhall.dao;

import com.functionhall.config.DatabaseConnection;
import com.functionhall.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PaymentDAO {

    public void save(Payment payment) throws Exception {
        String sql = """
                INSERT INTO payments
                (booking_id, amount, payment_method, payment_status, transaction_reference)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, payment.getBookingId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, "PAID");
            ps.setString(5, payment.getTransactionReference());
            ps.executeUpdate();
        }
    }
}
