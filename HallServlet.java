package com.functionhall.servlet;

import com.functionhall.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/halls")
public class HallServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sql = "SELECT id, name, location, capacity, price_per_day FROM halls ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) {
                    json.append(',');
                }
                first = false;

                json.append('{')
                    .append("\"id\":").append(rs.getInt("id")).append(',')
                    .append("\"name\":\"").append(escape(rs.getString("name"))).append("\",")
                    .append("\"location\":\"").append(escape(rs.getString("location"))).append("\",")
                    .append("\"capacity\":").append(rs.getInt("capacity")).append(',')
                    .append("\"pricePerDay\":").append(rs.getBigDecimal("price_per_day"))
                    .append('}');
            }

            json.append(']');
            out.print(json);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(
                    "{\"success\":false,\"message\":\"Unable to load halls. Check database connection.\"}");
            e.printStackTrace();
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
