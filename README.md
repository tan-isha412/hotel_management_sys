# 🏨 SnoreZz Hotel Management System

A desktop hotel management application built with **JavaFX 21**, **Maven**, and **SceneBuilder**. Handles the full guest lifecycle — room inventory, bookings, check-in/checkout, billing with PDF export, complaints, and room service — all persisted locally via Java object serialization. No database required.

---

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [OOP Design](#oop-design)
- [Project Structure](#project-structure)
- [Room Types & Pricing](#room-types--pricing)
- [Promo Codes](#promo-codes)
- [Data Persistence](#data-persistence)
- [Prerequisites](#prerequisites)
- [Running the App](#running-the-app)
- [Building a Fat JAR](#building-a-fat-jar)
- [Module Overview](#module-overview)
- [Notifications](#notifications)

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21.0.2 |
| Layout Tool | SceneBuilder (FXML) |
| Build Tool | Maven 3.x |
| PDF Generation | iTextPDF 5.5.13 |
| Persistence | Java Object Serialization (`.dat` files) |
| IDE | IntelliJ IDEA |

---

## Features

### Room Management
- Add and configure rooms (Standard / Deluxe / Luxury Suite)
- View all rooms with live availability status (AVAILABLE / BOOKED / MAINTENANCE)
- Filter rooms by type or availability

### Booking
- Look up customer by phone number before booking
- Filter available rooms by type and budget range
- Date picker with past-date prevention
- Add-on services: Extra Breakfast (₹350/night/guest), Airport Transfer (₹800 flat), Spa (₹1200/night)
- Promo code support with live discount preview
- Birthday discount auto-detection (checks customer DOB against today)
- Smart room recommendation based on guest count and budget
- Live price recalculation on every form change
- Simulated payment overlay with progress bar (multithreaded via `ExecutorService`)

### Check-In / Check-Out
- One-click check-in from confirmed bookings
- Checkout flow shows full bill summary before confirmation
- Room status automatically updates to AVAILABLE on checkout
- PDF invoice generated on checkout via iTextPDF

### Billing
- Itemised bill: room charges, service charges, promo discount, birthday discount
- GST included in Luxury Suite tariff calculation
- PDF bill saved to user's home directory

### Complaints
- Log complaints linked to a booking and room
- Auto-priority assignment: Electrical/Plumbing → HIGH, Carpentry → MEDIUM, others → LOW
- Track status: PENDING → IN_PROGRESS → RESOLVED
- Resolution time calculated and displayed (e.g. "2 hr 10 min")

### Room Service
- Log in-room service requests against active bookings
- Track service status and associated costs

### Dashboard
- Live stats: total rooms, available/booked count, total customers, active bookings, total revenue
- Occupancy % and average booking value
- Live clock updated every second via JavaFX `Timeline`

### Customers
- Register customers with name, phone, email, ID proof (Aadhaar / Passport / DL), DOB, nationality
- View all registered customers

---

## OOP Design

The project was built to demonstrate core OOP principles:

**Abstraction & Inheritance** — `Room` is an abstract base class. `StandardRoom`, `DeluxeRoom`, and `LuxuryRoom` extend it and each override `calculateTariff(int days)` differently.

**Polymorphism** — `calculateTariff` is called polymorphically across all room types:
- `StandardRoom` → flat rate (`basePrice × days`)
- `DeluxeRoom` → adds 10% service charge on top
- `LuxuryRoom` → adds per-night butler fee (₹1500/night) + 18% GST

**Encapsulation** — all model fields are private with getters/setters. `DataStore` exposes only `Collections.unmodifiableList(...)` to the outside.

**Interface** — `Amenities` interface defines `getAmenities()`, `getAmenitiesCharge()`, `hasWifi()`, `hasBreakfast()`, `hasPool()`. All room types implement it.

**Singleton** — `DataStore` uses a thread-safe `synchronized` singleton pattern to ensure a single shared in-memory state across all controllers.

**Multithreading** — `BookingService` uses `Executors.newFixedThreadPool(3)` to simulate async payment processing without freezing the UI thread.

---

## Project Structure

```
OSDL/
├── pom.xml
└── src/
    └── main/
        ├── java/com/hotel/
        │   ├── ui/
        │   │   └── MainApp.java              ← JavaFX entry point
        │   ├── controller/
        │   │   ├── MainController.java       ← sidebar navigation
        │   │   ├── DashboardController.java  ← live stats + clock
        │   │   ├── BookingController.java    ← full booking flow
        │   │   ├── CheckoutController.java   ← check-in/out
        │   │   ├── BillingController.java    ← bill view + PDF
        │   │   ├── AddRoomController.java    ← add/manage rooms
        │   │   ├── RoomsController.java      ← room list view
        │   │   ├── CustomersController.java  ← customer registry
        │   │   ├── ComplaintController.java  ← complaints tracker
        │   │   └── RoomServiceController.java
        │   ├── model/
        │   │   ├── Room.java                 ← abstract base class
        │   │   ├── StandardRoom.java
        │   │   ├── DeluxeRoom.java
        │   │   ├── LuxuryRoom.java
        │   │   ├── Amenities.java            ← interface
        │   │   ├── Booking.java
        │   │   ├── Customer.java
        │   │   ├── Complaint.java
        │   │   └── RoomService.java
        │   ├── service/
        │   │   ├── DataStore.java            ← singleton, serialization
        │   │   ├── BookingService.java       ← promo/discount/booking logic
        │   │   ├── BillingService.java       ← bill generation
        │   │   └── NotificationService.java  ← simulated email/SMS
        │   └── util/
        │       ├── AlertUtil.java            ← styled JavaFX dialogs
        │       ├── NavUtil.java              ← FXML navigation helper
        │       └── Validator.java            ← input validation
        └── resources/com/hotel/
            ├── fxml/
            │   ├── MainLayout.fxml
            │   ├── Dashboard.fxml
            │   ├── Booking.fxml
            │   ├── Checkout.fxml
            │   ├── Billing.fxml
            │   ├── AddRoom.fxml
            │   ├── Rooms.fxml
            │   ├── Customers.fxml
            │   ├── Complaint.fxml
            │   └── RoomService.fxml
            └── css/
                └── main.css                  ← dark theme stylesheet
```

---

## Room Types & Pricing

| Type | Base Price | Capacity | Tariff Logic |
|---|---|---|---|
| Standard | ₹2,500 / night | 2 guests | Flat: `basePrice × days` |
| Deluxe | ₹5,500 / night | 3 guests | +10% service charge |
| Luxury Suite | ₹12,000 / night | 4 guests | +₹1,500/night butler fee + 18% GST |

### Add-on Services

| Service | Cost |
|---|---|
| Extra Breakfast | ₹350 / night / guest |
| Airport Transfer | ₹800 (flat, one-time) |
| Spa Service | ₹1,200 / night |

---

## Promo Codes

| Code | Discount |
|---|---|
| `WELCOME5` | 5% off |
| `APRIL10` | 10% off |
| `HOTEL15` | 15% off |
| `SUMMER20` | 20% off |
| `VIP25` | 25% off |

A birthday discount is also applied automatically when the customer's registered date of birth matches today's date.

---

## Data Persistence

All data is saved as serialized `.dat` files in the user's home directory:

```
~/.hotel_management/
├── rooms.dat
├── customers.dat
├── bookings.dat
├── complaints.dat
└── services.dat
```

Data is loaded automatically on startup. If no `rooms.dat` exists, the app seeds a default room inventory. Generated PDF bills are also saved here.

> There is no database. All persistence is handled through Java's built-in `ObjectInputStream` / `ObjectOutputStream`. All model classes implement `Serializable`.

---

## Prerequisites

- **JDK 17** or higher
- **Maven 3.6+**
- JavaFX is pulled in automatically by Maven — no separate JavaFX SDK installation needed

> On macOS, make sure `JAVA_HOME` points to JDK 17+:
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 17)
> ```

---

## Running the App

### Via Maven (recommended)

```bash
# Clone or unzip the project
cd OSDL

# Run directly
mvn javafx:run
```

### Via IntelliJ IDEA

1. Open the `OSDL` folder as a Maven project
2. Let IntelliJ import dependencies
3. Run `com.hotel.ui.MainApp` as the main class

> If IntelliJ shows module errors, go to **File → Project Structure → Modules** and ensure the SDK is set to Java 17.

---

## Building a Fat JAR

The project uses `maven-shade-plugin` to bundle all dependencies (including JavaFX) into a single executable JAR:

```bash
mvn clean package
```

The shaded JAR will be at:

```
target/HotelManagementSystem-1.0-SNAPSHOT.jar
```

Run it with:

```bash
java -jar target/HotelManagementSystem-1.0-SNAPSHOT.jar
```

> Note: On some systems, running a shaded JavaFX JAR requires `--add-modules javafx.controls,javafx.fxml` depending on your JDK distribution. If you get module errors, use `mvn javafx:run` instead.

---

## Module Overview

| Controller | FXML | Responsibility |
|---|---|---|
| `MainController` | `MainLayout.fxml` | Sidebar nav, content pane switching |
| `DashboardController` | `Dashboard.fxml` | Live stats, occupancy %, live clock |
| `BookingController` | `Booking.fxml` | Full booking flow with pricing, promos, add-ons |
| `CheckoutController` | `Checkout.fxml` | Check-in / checkout, room status update |
| `BillingController` | `Billing.fxml` | Bill view, PDF export |
| `AddRoomController` | `AddRoom.fxml` | Add Standard / Deluxe / Luxury rooms |
| `RoomsController` | `Rooms.fxml` | Room inventory table, filter by type |
| `CustomersController` | `Customers.fxml` | Register and browse customers |
| `ComplaintController` | `Complaint.fxml` | Log, prioritise, resolve complaints |
| `RoomServiceController` | `RoomService.fxml` | In-room service requests |

---

## Notifications

The `NotificationService` simulates email and SMS confirmations — no real APIs are called. All notifications are printed to the console and surfaced in UI popups via `AlertUtil`.

To wire up real notifications later, replace the method bodies in `NotificationService.java`:
- **Email** → JavaMail / SendGrid SDK
- **SMS** → Twilio Java SDK

---

*SnoreZz Hotel Management System — JavaFX Desktop Project*
