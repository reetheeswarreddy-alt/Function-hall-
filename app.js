document.addEventListener("DOMContentLoaded", () => {
    const bookingForm = document.getElementById("bookingForm");
    const paymentForm = document.getElementById("paymentForm");
    const hallSelect = document.getElementById("hallId");
    const hallCards = document.getElementById("hallCards");
    const foodSection = document.getElementById("foodSection");
    const message = document.getElementById("message");
    const paymentSection = document.getElementById("payment");
    const paymentMessage = document.getElementById("paymentMessage");
    const confirmationSection = document.getElementById("confirmation");
    const confirmationText = document.getElementById("confirmationText");
    const bookingButton = document.getElementById("bookingButton");
    const paymentButton = document.getElementById("paymentButton");
    const newBooking = document.getElementById("newBooking");

    const state = { booking: null };

    const money = value =>
        new Intl.NumberFormat("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(Number(value) || 0);

    function setMessage(element, text, type = "") {
        element.textContent = text || "";
        element.className = `message ${type}`.trim();
    }

    async function readApiResponse(response) {
        const raw = await response.text();

        if (!raw.trim()) {
            throw new Error(`Server returned an empty response (HTTP ${response.status}).`);
        }

        try {
            const data = JSON.parse(raw);

            if (!response.ok) {
                throw new Error(
                    data.message || data.error || `Request failed (HTTP ${response.status}).`
                );
            }

            return data;
        } catch (error) {
            if (error instanceof SyntaxError) {
                console.error("Non-JSON server response:", raw);
                throw new Error(
                    `Server returned invalid JSON (HTTP ${response.status}). Check Tomcat logs and database settings.`
                );
            }
            throw error;
        }
    }

    async function loadHalls() {
        try {
            const response = await fetch("api/halls", {
                headers: { Accept: "application/json" }
            });

            const halls = await readApiResponse(response);

            if (!Array.isArray(halls)) {
                throw new Error(halls.message || "Unexpected halls response.");
            }

            renderHalls(halls);
        } catch (error) {
            console.error(error);
            hallCards.innerHTML = `
                <div class="hall-card">
                    <h3>Unable to load halls</h3>
                    <p>${escapeHtml(error.message)}</p>
                    <p class="help">Make sure MySQL is running and schema.sql has been executed.</p>
                </div>
            `;
        }
    }

    function renderHalls(halls) {
        if (halls.length === 0) {
            hallCards.innerHTML = '<div class="loading-card">No halls are available.</div>';
            return;
        }

        hallCards.innerHTML = halls.map(hall => `
            <article class="hall-card">
                <h3>${escapeHtml(hall.name)}</h3>
                <p><strong>Location:</strong> ${escapeHtml(hall.location)}</p>
                <p><strong>Capacity:</strong> ${hall.capacity} guests</p>
                <p class="hall-price">₹${money(hall.pricePerDay)} / day</p>
            </article>
        `).join("");

        hallSelect.innerHTML =
            '<option value="">Select a hall</option>' +
            halls.map(hall =>
                `<option value="${hall.id}">${escapeHtml(hall.name)} - ₹${money(hall.pricePerDay)}</option>`
            ).join("");
    }

    function updateFoodVisibility() {
        const selected =
            document.querySelector('input[name="foodRequired"]:checked')?.value === "true";

        foodSection.classList.toggle("hidden", !selected);

        if (!selected) {
            document.querySelectorAll('input[name="foodItems"]').forEach(input => {
                input.checked = false;
            });
        }
    }

    document.querySelectorAll('input[name="foodRequired"]').forEach(input => {
        input.addEventListener("change", updateFoodVisibility);
    });

    bookingForm.addEventListener("submit", async event => {
        event.preventDefault();
        setMessage(message, "");

        if (!bookingForm.reportValidity()) {
            return;
        }

        bookingButton.disabled = true;
        bookingButton.textContent = "Submitting...";

        try {
            const formData = new FormData(bookingForm);

            const response = await fetch("api/bookings", {
                method: "POST",
                body: formData,
                headers: { Accept: "application/json" }
            });

            const data = await readApiResponse(response);
            state.booking = data;

            document.getElementById("paymentBookingId").textContent = data.bookingId;
            document.getElementById("paymentHall").textContent = data.hallName;
            document.getElementById("paymentGuests").textContent = data.guests;
            document.getElementById("paymentDecoration").textContent = data.decoration;
            document.getElementById("paymentFood").textContent =
                data.foodRequired ? data.foodItems : "Without Food";
            document.getElementById("paymentAmount").textContent = money(data.amount);

            setMessage(message, data.message, "success");
            paymentSection.classList.remove("hidden");
            paymentSection.scrollIntoView({ behavior: "smooth", block: "start" });
        } catch (error) {
            console.error(error);
            setMessage(message, error.message, "error");
        } finally {
            bookingButton.disabled = false;
            bookingButton.textContent = "Confirm Booking";
        }
    });

    paymentForm.addEventListener("submit", async event => {
        event.preventDefault();
        setMessage(paymentMessage, "");

        if (!state.booking) {
            setMessage(paymentMessage, "Please complete the booking first.", "error");
            return;
        }

        paymentButton.disabled = true;
        paymentButton.textContent = "Processing...";

        try {
            const formData = new FormData(paymentForm);
            formData.append("bookingId", state.booking.bookingId);
            formData.append("amount", state.booking.amount);

            const response = await fetch("api/payments", {
                method: "POST",
                body: formData,
                headers: { Accept: "application/json" }
            });

            const data = await readApiResponse(response);

            confirmationText.textContent =
                `Booking #${data.bookingId} is confirmed. Payment reference: ${data.reference}.`;

            paymentSection.classList.add("hidden");
            confirmationSection.classList.remove("hidden");
            confirmationSection.scrollIntoView({ behavior: "smooth", block: "start" });
        } catch (error) {
            console.error(error);
            setMessage(paymentMessage, error.message, "error");
        } finally {
            paymentButton.disabled = false;
            paymentButton.textContent = "Pay & Confirm";
        }
    });

    newBooking.addEventListener("click", () => {
        bookingForm.reset();
        paymentForm.reset();
        state.booking = null;
        updateFoodVisibility();
        setMessage(message, "");
        setMessage(paymentMessage, "");
        paymentSection.classList.add("hidden");
        confirmationSection.classList.add("hidden");
    });

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    updateFoodVisibility();
    loadHalls();
});
