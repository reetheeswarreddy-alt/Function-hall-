package com.functionhall.servlet;

import com.functionhall.dao.PaymentDAO;
import com.functionhall.model.Payment;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/payments")
public class PaymentServlet extends HttpServlet {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int bookingId = parsePositiveInt(request, "bookingId");
            double amount = Double.parseDouble(required(request, "amount"));
            String method = required(request, "paymentMethod").toUpperCase();

            if (!Double.isFinite(amount) || amount <= 0) {
                throw new IllegalArgumentException("Invalid payment amount.");
            }

            if (!method.equals("UPI") && !method.equals("CARD") && !method.equals("CASH")) {
                throw new IllegalArgumentException("Invalid payment method.");
            }

            String reference = method.equals("CASH")
                    ? "CASH-" + bookingId + "-" + System.currentTimeMillis()
                    : "DEMO-" + System.currentTimeMillis();

            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setAmount(amount);
            payment.setPaymentMethod(method);
            payment.setTransactionReference(reference);

            paymentDAO.save(payment);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().printf(
                    "{\"success\":true,\"bookingId\":%d,\"reference\":\"%s\",\"message\":\"Payment recorded successfully.\"}",
                    bookingId, escape(reference));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().printf(
                    "{\"success\":false,\"message\":\"%s\"}", escape(e.getMessage()));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(
                    "{\"success\":false,\"message\":\"Unable to record payment. Check the server and database.\"}");
            e.printStackTrace();
        }
    }

    private int parsePositiveInt(HttpServletRequest request, String name) {
        String value = required(request, name);
        try {
            int number = Integer.parseInt(value);
            if (number <= 0) throw new NumberFormatException();
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

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
