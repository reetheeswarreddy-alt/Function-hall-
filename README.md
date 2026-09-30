# HallBook - Function Hall Booking Web Application

A beginner-friendly Java web application using HTML, CSS, JavaScript, Jakarta Servlets, JDBC and MySQL.

## Features
- Loads function halls from MySQL.
- Responsive browser-based booking form.
- With/without decoration.
- With/without food.
- Multiple food item selection.
- Special requests.
- Server-side validation.
- Prevents double-booking of the same hall on the same date.
- Calculates the demo booking amount on the server.
- Shows payment after a successful booking.
- Demo UPI, card and cash payment recording.
- Every API returns JSON, avoiding the "Unexpected end of JSON input" problem.
- Supports environment variables for cloud deployment.

## Structure
```text
function-hall-booking/
├── pom.xml
├── README.md
├── Dockerfile
├── render.yaml
├── database/
│   └── schema.sql
└── src/main/
    ├── java/com/functionhall/
    │   ├── config/DatabaseConnection.java
    │   ├── model/Booking.java
    │   ├── model/Payment.java
    │   ├── dao/BookingDAO.java
    │   ├── dao/PaymentDAO.java
    │   └── servlet/
    │       ├── HallServlet.java
    │       ├── BookingServlet.java
    │       └── PaymentServlet.java
    └── webapp/
        ├── index.html
        ├── css/style.css
        └── js/app.js
```

## Local setup
1. Install JDK 17, Maven, MySQL and Tomcat 10.1+.
2. Run `database/schema.sql` in MySQL.
3. Default local DB:
   - URL: `jdbc:mysql://localhost:3306/function_hall_db`
   - User: `root`
   - Password: `root`
4. Change `DatabaseConnection.java` if your MySQL credentials are different.
5. Build:
```bash
mvn clean package
```
6. Deploy `target/function-hall-booking.war` to Tomcat 10.1+.
7. Open:
```text
http://localhost:8080/function-hall-booking/
```

## Render
The Dockerfile runs the WAR on Tomcat. Set:
```text
DB_URL=jdbc:mysql://YOUR-MYSQL-HOST:3306/function_hall_db?useSSL=false&serverTimezone=UTC
DB_USER=YOUR_DATABASE_USER
DB_PASSWORD=YOUR_DATABASE_PASSWORD
```
The cloud database must be reachable from Render. A MySQL server running only on your laptop cannot be used by Render.

## Payment
This is a learning/demo payment flow. It records the selected method and a demo reference in MySQL; it does not move real money. A real gateway needs a separate secure integration and server-side verification.

## The previous JSON error
The fixed Java servlets always send a JSON body on success and errors. The fixed `app.js` first reads the response as text, checks for an empty/non-JSON response, and shows a useful error instead of blindly calling `response.json()`.
