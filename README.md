# VServ — Vehicle Service Management System

**System Architecture & Developer Reference · v2.0 Backend · v3.0 Frontend**

---

## 1. Overview

VServ is a full-stack vehicle service management platform designed for automotive service centres. It provides role-based workflows for Admins, Service Advisors, and Customers — covering the complete lifecycle from booking a service to invoice payment and customer feedback.

The system is composed of a Spring Boot REST API backend and a Vite + React frontend. Both are independently deployable, communicating via HTTP/JSON over a defined API contract.

---

## 2. Technology Stack

| Layer | Technology | Notes |
|---|---|---|
| Backend Runtime | Java 17 | Spring Boot 3.5.13, Maven build |
| Web Framework | Spring MVC | REST API — no server-side rendering |
| Security | Spring Security + JJWT 0.11.5 | JWT access + refresh token auth, BCrypt password hashing |
| ORM / DB Access | Spring Data JPA (Hibernate) | Entity-based repository pattern |
| Database | MySQL 8+ | InnoDB, generated columns, triggers, partial unique index |
| Payment Gateway | Razorpay Java SDK 1.4.8 | Order creation + HMAC-SHA256 signature verification |
| SMS | Twilio SDK 10.9.2 | Transactional SMS notifications |
| Email | Resend API (HTTP) | OTP delivery and service notifications |
| Frontend Runtime | Node.js + Vite 5.3 | Module bundler — builds to static assets |
| UI Framework | React 18.3 | Component-based SPA |
| State Management | Redux Toolkit 2.3 + React-Redux 9.1 | Auth slice; most feature state is local |
| HTTP Client | Axios 1.7 | Centralised instance with JWT interceptor |
| Routing | React Router DOM 6.26 | Client-side routing with AuthGuard / GuestGuard |
| Charts | Recharts 2.12 | Admin dashboard analytics |
| PDF Generation | jsPDF 4.2 + html2canvas | Client-side invoice PDF export |
| Styling | Plain CSS | Per-feature stylesheets; three reference CSS themes (admin, advisor, customer) |

---

## 3. Component-Based Architecture

### 3.1 Backend Architecture

The backend follows a vertical-slice feature architecture under the `com.vserv` package. Each feature owns its Controller → Service → Repository stack, keeping concerns isolated.

#### Package Structure

```
com.vserv
├── core/                  ← Cross-cutting infrastructure
│   ├── config/            ← CORS, JWT filter, Security config, external service props
│   ├── exception/         ← GlobalExceptionHandler, custom exception types
│   ├── pagination/        ← PagedResponse<T>, PaginationUtils
│   ├── status/            ← StatusToggleGuard (safe deactivation checks)
│   └── util/              ← SecurityUtils, SlotTimeUtils, ValidationPatterns
├── entity/                ← JPA entities (DB tables)
└── features/              ← One sub-package per domain feature
    ├── auth/              ← Login, register, OTP, refresh, logout
    ├── advisor/           ← Advisor CRUD, load management
    ├── audit/             ← Booking history / audit log
    ├── availability/      ← Slot management, startup seeding
    ├── booking/           ← Booking lifecycle (create/confirm/cancel/reschedule)
    ├── catalog/           ← Service catalog CRUD
    ├── dashboard/         ← Aggregated stats for admin dashboard
    ├── feedback/          ← Post-service customer ratings
    ├── invoice/           ← Invoice view, payment recording
    ├── notification/      ← In-app notifications
    ├── paymentgateway/    ← Razorpay order + verify, Twilio SMS
    ├── profile/           ← Profile update, password change
    ├── servicerecord/     ← Service execution, items, completion
    ├── user/              ← User CRUD (admin)
    ├── vehicle/           ← Vehicle CRUD
    └── workitem/          ← Work-item catalog CRUD
```

#### Layer Responsibilities

Each feature slice contains the following layers:

- **Controller:** Handles HTTP request mapping, input validation, and role-based `@PreAuthorize` guards. Delegates all business logic downward.
- **Service / ServiceImpl:** Enforces business rules, orchestrates cross-feature calls (e.g., `BookingService` calls `VehicleService` and `CatalogService`). All transactional boundaries live here.
- **Repository:** Spring Data JPA interfaces. Custom JPQL/native queries for complex filtering (status checks, advisor load, unsettled invoices).
- **DTO:** Request and response shapes decoupled from JPA entities. Validated with `jakarta.validation` annotations.
- **Mapper:** Static utility methods that translate between entities and DTOs.

---

### 3.2 Frontend Architecture

The frontend mirrors the backend's feature-slice structure. Each feature is self-contained with its own components, services, and styles.

#### Package Structure

```
src/
├── app/           ← Store, root reducer, routes, document meta
├── assets/        ← Fonts, icons, images
├── components/    ← Shared reusable components
│   ├── common/    ← PageElements (breadcrumbs, section titles)
│   └── ui/        ← ActionIconButton, Badge, Modal, PaginationControls,
│                     TimeSlotGrid, BookingStepper, VehicleCard, Toast, etc.
├── config/        ← App-wide constants
├── features/      ← One folder per domain feature
│   ├── auth/      ← LoginPage, RegisterPage, ForgotPasswordPage, AuthGuard, authSlice
│   ├── advisors/  ← AdvisorsPage
│   ├── audit-logs/← AuditLogsPage
│   ├── availability/ ← AvailabilityPage
│   ├── bookings/  ← CustomerBookServicePage, CustomerBookingsPage,
│                     BookingsAdminPage, AdminBookServicePage, OverdueBookingsPage
│   ├── catalog/   ← CatalogPage
│   ├── dashboard/ ← AdminDashboardPage, AdvisorDashboardPage, CustomerDashboardPage
│   ├── invoices/  ← InvoicesAdminPage, CustomerInvoicesPage, PaymentPage
│   ├── notifications/ ← NotificationsPage, NotifBadge, useNotifications
│   ├── profile/   ← AdminProfilePage, AdvisorProfilePage, CustomerProfilePage
│   ├── public/    ← HomePage, ServicesPage, AboutPage, ContactPage, ErrorPage
│   ├── service-records/ ← ManageServicePage, CompleteServicePage, ServiceDetailsPage
│   ├── users/     ← UsersPage
│   ├── vehicles/  ← MyVehiclesPage, VehiclesAdminPage
│   └── work-items/ ← WorkItemsPage
├── hooks/         ← useNavigationGuard
├── layouts/       ← AppShell, Topbar, AppFooter
├── lib/           ← Axios singleton with JWT interceptor
├── services/      ← pagination helper
├── styles/        ← globals.css, per-feature CSS, theme reference sheets
└── utils/         ← formatters, validationPatterns, adminValidation, authValidation
```

#### State & Data Flow

- **Redux store:** Manages auth state only (user object, roles). Feature data is fetched fresh per component mount.
- **Axios interceptor:** Attaches `Authorization: Bearer <token>` to every request automatically.
- **AuthGuard:** Wraps protected routes; redirects to `/login` if unauthenticated or role mismatch.
- **GuestGuard:** Redirects already-authenticated users away from `/login` and `/register`.

---

### 3.3 Role-Based Access Summary

| Feature | ADMIN | ADVISOR | CUSTOMER |
|---|---|---|---|
| **Dashboard** | Full stats | Own workload | Own summary |
| **Users** | CRUD all users | – | – |
| **Vehicles** | View all | – | Own vehicles CRUD |
| **Service Catalog** | CRUD | Read | Read |
| **Work Items** | CRUD | Read | – |
| **Availability Slots** | CRUD | – | Read (for booking) |
| **Bookings** | All + confirm/cancel/reassign | Assigned bookings | Own bookings + create |
| **Service Records** | All + complete | Assigned + complete | – |
| **Invoices** | All | – | Own invoices |
| **Payments** | Record manually | – | Pay via Razorpay |
| **Feedback** | View all | – | Submit after service |
| **Notifications** | – | – | Own notifications |
| **Advisors** | CRUD | Own profile | – |
| **Audit Logs** | Full history | – | – |
| **Profile** | Own profile | Own profile | Own profile |

---

## 4. Database Details

**Database:** `vehicle_service_wb` (MySQL 8+, InnoDB engine)

### 4.1 Table Reference

| Table | Key Columns | Purpose |
|---|---|---|
| **role** | role_id, role_name (ENUM), description | Seed-only. Defines the three system roles: ADMIN, ADVISOR, CUSTOMER. |
| **user** | user_id, full_name, email, password, phone, gender, role_id, status, is_deleted, loyalty_badge, created_at, last_login | Central identity table. Soft-delete via `is_deleted` + `deleted_at`. ACTIVE/INACTIVE status. `loyalty_badge` flag for repeat customers. |
| **vehicle** | vehicle_id, user_id, car_type, brand, model, registration_number, manufacture_year, mileage, last_service_date, next_service_due, service_interval_km, is_deleted, is_active | Customer vehicles. `registration_number` is UNIQUE. Soft-deleted. `is_active` mirrors `is_deleted` for query convenience. |
| **service_catalog** | catalog_id, service_name, service_type, description, base_price, booking_charge, car_type, duration_hours, is_active | Admin-managed list of offered services. `booking_charge` is the advance charged at booking time (default ₹299). |
| **work_item_catalog** | work_item_id, item_name, item_type (PART/LABOR/CONSUMABLE), unit_price, car_type, description, is_active | Granular billable items added to a service record by advisors. |
| **service_availability** | availability_id, service_date, time_slot, max_bookings, current_bookings, is_available | Defines bookable date+slot combinations. UNIQUE(service_date, time_slot). Auto-seeded at startup. Past slots are bulk-marked unavailable on startup. |
| **service_booking** | booking_id, vehicle_id, catalog_id, service_date, time_slot, booking_status, booking_notes, archived_vehicle_info, archived_owner_id, active_slot (VIRTUAL) | Core booking record. `booking_status`: PENDING → CONFIRMED → COMPLETED (or CANCELLED/RESCHEDULED). `active_slot` is a generated column used in the partial unique index to prevent double-booking active slots. |
| **booking_history** | history_id, booking_id, action_type, old/new_service_date, old/new_time_slot, reason, action_by, action_date | Full audit trail. Written on every booking state change. |
| **service_advisor** | advisor_id (FK→user), specialization, overtime_rate, availability_status, current_load, last_assigned_at | Extends the user table for advisor-specific attributes. `current_load` tracks active assignments. |
| **service_record** | service_id, booking_id (UNIQUE), advisor_id, service_start_date, service_end_date, status (PENDING/IN_PROGRESS/COMPLETED), estimated_hours, actual_hours, remarks | One-to-one with `service_booking` after confirmation. Tracks the execution timeline. |
| **service_item** | item_id, service_id, work_item_id, quantity, unit_price, total_price (GENERATED STORED) | Line items on a service record. `total_price` is a stored generated column (quantity × unit_price). |
| **invoice** | invoice_id, service_id (UNIQUE), total_amount, items_total, base_service_price, booking_charge, overtime_charge, advance_paid, advance_amount, invoice_date, payment_status | Created when a `service_record` is completed. `payment_status`: PENDING → PARTIALLY_PAID → PAID. `advance_amount` tracks pre-paid booking charge deducted from final. |
| **payment** | payment_id, invoice_id, payment_method (UPI/CARD/NET_BANKING/CASH), transaction_reference, amount, payment_date, payment_status, payment_purpose (BOOKING_CHARGE/FINAL_INVOICE) | Individual payment transactions. Supports partial payments via `payment_purpose`. |
| **service_feedback** | feedback_id, service_id (UNIQUE), customer_id, rating (1–5), feedback_text, submitted_at | Post-service rating. One per service record, submitted by the customer. |
| **notification** | notification_id, user_id, notification_type, title, message, related_booking_id, is_read, sent_at | In-app notifications for customers. Linked to bookings with SET NULL on booking delete. |
| **password_reset_token** | id, user_id, token, otp_hash, otp_verified, expires_at, used, created_at | Two-step password reset: OTP sent via email, verified to issue a reset token, then used once for reset. |
| **revoked_token** | id, token (UNIQUE TEXT), revoked_at | JWT denylist for logout. Both access and refresh tokens are stored on logout. |

---

### 4.2 Unique Constraints

#### Standard UNIQUE Indexes

- **user.email:** No two accounts may share an email address.
- **vehicle.registration_number:** Each vehicle plate is globally unique (even across soft-deleted vehicles).
- **service_availability (service_date, time_slot):** Composite unique — one slot record per date+time pair.
- **service_record.booking_id:** One-to-one relationship — a booking can only have one service record.
- **invoice.service_id:** One invoice per service record.
- **service_feedback.service_id:** One feedback submission per completed service.
- **revoked_token.token:** Prevents duplicate revocation inserts.

#### Partial Unique Index — Preventing Double-Bookings

The most complex constraint prevents the same vehicle from having two active bookings on the same date+slot, while still allowing cancelled/completed bookings to be re-used:

```sql
-- Virtual generated column (active only for open booking states)
active_slot VARCHAR(20) GENERATED ALWAYS AS (
  CASE WHEN booking_status IN ('PENDING','CONFIRMED','RESCHEDULED')
       THEN time_slot ELSE NULL END) VIRTUAL

-- Unique index on (vehicle_id, service_date, active_slot)
CREATE UNIQUE INDEX uq_active_vehicle_datetime
ON service_booking (vehicle_id, service_date, active_slot);
```

Because NULL values are never considered equal in SQL unique indexes, cancelled or completed bookings produce a NULL `active_slot` and are excluded from the uniqueness check. This allows the same vehicle to be re-booked after a cancellation on the same date+slot.

---

### 4.3 Database Triggers

#### `trg_invoice_date`
BEFORE INSERT on `invoice`. If `invoice_date` is NULL (e.g., not supplied by the application), automatically sets it to `CURDATE()`. Ensures no invoice is ever stored without a date.

#### `before_vehicle_delete`
BEFORE UPDATE on `vehicle`. When a vehicle is soft-deleted (`is_deleted` changes from FALSE to TRUE), the trigger:
- Copies the vehicle's brand, model, and registration_number into `archived_vehicle_info` on all linked `service_booking` rows.
- Copies the vehicle owner's `user_id` into `archived_owner_id` so historical bookings remain queryable even after the vehicle record is logically removed.
- Stamps `deleted_at` with the current timestamp.

#### `before_user_soft_delete`
BEFORE UPDATE on `user`. When a user's `is_deleted` flag is set to TRUE, automatically stamps `deleted_at = CURRENT_TIMESTAMP`.

---

### 4.4 Entity Relationship Summary

- **user 1→N vehicle:** A customer may own many vehicles.
- **vehicle 1→N service_booking:** Multiple bookings per vehicle over time.
- **service_booking 1→1 service_record:** One record per confirmed booking.
- **service_record 1→N service_item:** Multiple line items per service job.
- **service_record 1→1 invoice:** Invoice created on service completion.
- **invoice 1→N payment:** Supports instalment / partial payments.
- **service_record 1→1 service_feedback:** Customer rates each completed service once.
- **user (ADVISOR) 1→1 service_advisor:** Extends user with advisor metadata.
- **service_booking 1→N booking_history:** Full audit trail of every booking action.

---

## 5. Complete REST API Reference

### Authentication — `/api/auth`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| POST | `/api/auth/login` | Authenticate user; returns JWT access + refresh tokens | Public | – | `LoginRequest {email, password}` |
| POST | `/api/auth/register` | Customer self-registration | Public | – | `RegisterRequest {fullName, email, password, phone, gender}` |
| POST | `/api/auth/logout` | Revoke access + refresh tokens (adds to denylist) | Authenticated | – | Authorization header + optional `RefreshTokenRequest` |
| GET | `/api/auth/me` | Return the currently authenticated user's profile | Authenticated | – | – |
| POST | `/api/auth/refresh` | Issue new access token using refresh token | Public (refresh token) | – | `RefreshTokenRequest {refreshToken}` |
| POST | `/api/auth/forgot-password/request` | Send OTP to registered email for password reset | Public | – | `ForgotPasswordRequestDto {email}` |
| POST | `/api/auth/forgot-password/verify` | Verify OTP; returns signed reset token | Public | – | `VerifyOtpRequest {email, otp}` |
| POST | `/api/auth/forgot-password/reset` | Apply new password using reset token | Public (reset token) | – | `ResetPasswordRequest {resetToken, newPassword}` |

### Users — `/api/users` (ADMIN only)

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/users` | List all non-deleted users with toggle-guard and loyalty data | ADMIN | q, role, status, sort, page, size | – |
| GET | `/api/users/{id}` | Get single user by ID | ADMIN | – | – |
| POST | `/api/users` | Create a new user (any role, including ADVISOR) | ADMIN | – | `CreateUserRequest {fullName, email, password, phone, gender, roleName, specialization?, availabilityStatus?, overtimeRate?}` |
| PUT | `/api/users/{id}` | Update user details | ADMIN | – | `UpdateUserRequest` |
| PATCH | `/api/users/{id}/status` | Toggle user ACTIVE ↔ INACTIVE (guard-checked) | ADMIN | – | – |

### Vehicles — `/api/vehicles`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/vehicles` | List all vehicles (all owners) | ADMIN | q, carType, serviceStatus, sort, page, size | – |
| GET | `/api/vehicles/{id}` | Get single vehicle by ID | Authenticated | – | – |
| GET | `/api/vehicles/by-user/{userId}` | List vehicles belonging to a specific user | Self or ADMIN | q, carType, sort, page, size | – |
| POST | `/api/vehicles` | Add a new vehicle | Authenticated | – | `CreateVehicleRequest {userId, brand, model, registrationNumber, manufactureYear, carType, mileage, serviceIntervalKm}` |
| PUT | `/api/vehicles/{id}` | Update vehicle details | Owner or ADMIN | – | `UpdateVehicleRequest` |
| PATCH | `/api/vehicles/{id}/status` | Toggle vehicle active/inactive (guard-checked) | Owner or ADMIN | – | – |

### Service Catalog — `/api/catalog`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/catalog` | List all services (optionally filtered) | Public | activeOnly, q, serviceType, carType, statusFilter, sort, page, size | – |
| GET | `/api/catalog/{id}` | Get single catalog service | Public | – | – |
| POST | `/api/catalog` | Create a new service | ADMIN | – | `CreateCatalogRequest {serviceName, serviceType, description, basePrice, carType, durationHours}` |
| PUT | `/api/catalog/{id}` | Update a catalog service | ADMIN | – | `UpdateCatalogRequest` |
| PATCH | `/api/catalog/{id}/toggle` | Toggle service active/inactive | ADMIN | – | – |

### Work Items — `/api/work-items`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/work-items` | List all work items | ADMIN / ADVISOR | activeOnly, q, itemType, carType, statusFilter, sort, page, size | – |
| GET | `/api/work-items/{id}` | Get single work item | ADMIN / ADVISOR | – | – |
| POST | `/api/work-items` | Create work item | ADMIN | – | `CreateWorkItemRequest {itemName, itemType, carType, unitPrice, description}` |
| PUT | `/api/work-items/{id}` | Update work item | ADMIN | – | `UpdateWorkItemRequest` |
| PATCH | `/api/work-items/{id}/toggle` | Toggle work item active/inactive | ADMIN | – | – |

### Availability — `/api/availability`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/availability` | List all slots in a date range (defaults to current month) | ADMIN | from (date), to (date) | – |
| GET | `/api/availability/bookable` | List bookable slots in date range (defaults to next 30 days) | Public | from (date), to (date) | – |
| POST | `/api/availability` | Create a single availability slot | ADMIN | – | `CreateAvailabilityRequest {serviceDate, timeSlot, maxBookings?}` |
| PUT | `/api/availability/{id}` | Update slot max bookings or availability flag | ADMIN | – | `UpdateAvailabilityRequest {maxBookings?, isAvailable?}` |
| POST | `/api/availability/bulk` | Bulk-create slots across a date range | ADMIN | – | `BulkSlotRequest {from, to, slots[], maxBookings}` |
| PATCH | `/api/availability/{id}/toggle` | Toggle slot available/unavailable | ADMIN | – | – |
| DELETE | `/api/availability/{id}` | Mark an empty future slot as unavailable (soft-delete) | ADMIN | – | – |

### Bookings — `/api/bookings`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/bookings` | List bookings (role-scoped: ADMIN=all, ADVISOR=assigned, CUSTOMER=own) | Authenticated | status, q, dateFrom, sort, unassignedOnly, page, size | – |
| GET | `/api/bookings/{id}` | Get single booking by ID | Scoped | – | – |
| GET | `/api/bookings/overdue` | List CONFIRMED bookings with past service_date | ADMIN | – | – |
| GET | `/api/bookings/{id}/history` | Full booking audit trail | Scoped | – | – |
| POST | `/api/bookings` | Create a new booking (collects booking charge payment info) | Authenticated | – | `CreateBookingRequest {vehicleId, catalogId, serviceDate, timeSlot, notes?, paymentMethod?, transactionRef?, waiveBookingCharge?}` |
| PATCH | `/api/bookings/{id}/confirm` | Confirm booking + optionally assign advisor | ADMIN | – | `ConfirmBookingRequest {advisorId?}` |
| PATCH | `/api/bookings/{id}/cancel` | Cancel booking (releases slot capacity) | Scoped | – | `CancelBookingRequest {reason}` |
| PATCH | `/api/bookings/{id}/reschedule` | Reschedule to a new date+slot | Scoped | – | `RescheduleRequest {newDate, newSlot, reason}` |
| PATCH | `/api/bookings/{id}/reassign-advisor` | Reassign booking to different advisor | ADMIN | – | `ReassignAdvisorRequest {advisorId}` |

### Service Records — `/api/service-records`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/service-records` | List service records (ADMIN=all, ADVISOR=assigned) | ADMIN / ADVISOR | status, q, sort, page, size | – |
| GET | `/api/service-records/{id}` | Get single service record | ADMIN / ADVISOR | – | – |
| GET | `/api/service-records/{id}/items` | List work items on a service record | ADMIN / ADVISOR | – | – |
| PATCH | `/api/service-records/{id}/start` | Mark record IN_PROGRESS; stamps start time | ADMIN / ADVISOR | – | – |
| PUT | `/api/service-records/{id}/items` | Replace service item list | ADMIN / ADVISOR | – | `SaveItemsRequest {items[]: {workItemId, quantity, unitPrice}}` |
| PATCH | `/api/service-records/{id}/remarks` | Update remarks field | ADMIN / ADVISOR | – | `UpdateRemarksRequest {remarks}` |
| PATCH | `/api/service-records/{id}/complete` | Complete service + generate invoice | ADMIN / ADVISOR | – | `CompleteServiceRequest {actualHours, remarks?, items[]?}` |

### Invoices — `/api/invoices`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/invoices` | List invoices (ADMIN=all, CUSTOMER=own) | ADMIN / CUSTOMER | status, q, paymentMethod, sort, page, size | – |
| GET | `/api/invoices/{id}` | Get invoice detail (only for COMPLETED services) | Scoped | – | – |
| POST | `/api/invoices/{id}/pay` | Record a CASH payment against an invoice | ADMIN / CUSTOMER | – | `PaymentRequest {paymentMethod, amount, transactionReference?}` |
| POST | `/api/invoices/{id}/checkout/order` | Create Razorpay order for digital invoice payment | ADMIN / CUSTOMER | – | `CheckoutOrderRequest {amount, paymentMethod, description?}` |
| POST | `/api/invoices/{id}/checkout/verify` | Verify Razorpay signature and record payment | ADMIN / CUSTOMER | – | `VerifyPaymentRequest {razorpayOrderId, razorpayPaymentId, razorpaySignature, paymentMethod, amount}` |

### Payment Gateway — `/api/payments` (booking charge)

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| POST | `/api/payments/booking-charge/order` | Create Razorpay order for booking advance charge | ADMIN / CUSTOMER | – | `CheckoutOrderRequest {amount, paymentMethod (no CASH), description?}` |
| POST | `/api/payments/booking-charge/verify` | Verify booking charge payment signature | ADMIN / CUSTOMER | – | `VerifyPaymentRequest {razorpayOrderId, razorpayPaymentId, razorpaySignature, paymentMethod, amount}` |

### Service Feedback — `/api/service-feedback`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/service-feedback` | List all feedback entries | ADMIN | – | – |
| GET | `/api/service-feedback/service/{serviceId}` | Get feedback status for a service record | ADMIN / CUSTOMER | – | – |
| POST | `/api/service-feedback/service/{serviceId}` | Submit feedback for a completed service | CUSTOMER | – | `CreateServiceFeedbackRequest {rating (1–5), feedbackText?}` |

### Notifications — `/api/notifications`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/notifications` | List notifications for current user | Authenticated | filter (all\|unread\|type), page, size | – |
| PATCH | `/api/notifications/{id}/read` | Mark a single notification as read | Authenticated | – | – |
| PATCH | `/api/notifications/read-all` | Mark all notifications as read | Authenticated | – | – |
| DELETE | `/api/notifications/{id}` | Delete a single notification | Authenticated | – | – |

### Advisors — `/api/advisors`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/advisors` | List all advisors with toggle-guard data | ADMIN | q, availabilityStatus, specialization, sort, page, size | – |
| GET | `/api/advisors/available` | List advisors with AVAILABLE status | ADMIN / ADVISOR | – | – |
| GET | `/api/advisors/{id}` | Get single advisor by ID | ADMIN | – | – |
| PUT | `/api/advisors/{id}` | Update advisor specialization / overtime rate | ADMIN | – | `UpdateAdvisorRequest {specialization?, overtimeRate?, availabilityStatus?}` |
| PATCH | `/api/advisors/{id}/status` | Toggle advisor user account ACTIVE ↔ INACTIVE | ADMIN | – | – |

### Profile — `/api/profile`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/profile` | Get current user's full profile | Authenticated | – | – |
| PUT | `/api/profile` | Update name and phone | Authenticated | – | `UpdateProfileRequest {fullName, phone}` |
| POST | `/api/profile/change-password/request-otp` | Send OTP to own email for password change | Authenticated | – | – |
| POST | `/api/profile/change-password/verify-otp` | Verify OTP; returns reset token | Authenticated | – | `VerifyProfileOtpRequest {otp}` |
| POST | `/api/profile/change-password` | Apply new password using reset token | Authenticated | – | `CompletePasswordChangeRequest {resetToken, newPassword}` |

### Dashboard — `/api/dashboard`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/dashboard` | Aggregated KPI stats (bookings, revenue, advisors, feedback) | ADMIN | – | – |

### Audit Logs — `/api/audit-logs`

| Method | Path | Purpose | Auth / Role | Query Params | Body / Notes |
|---|---|---|---|---|---|
| GET | `/api/audit-logs` | Full booking action history | ADMIN | actionType, bookingQuery, dateFrom (date), dateTo (date), sort, page, size | – |

---

## 6. Backend–Frontend–Database Mapping

| Endpoint | Purpose | Frontend Component | DB Tables |
|---|---|---|---|
| POST /api/auth/login | Login, issue JWT | LoginPage.jsx | – |
| POST /api/auth/register | Customer self-registration | RegisterPage.jsx | user |
| POST /api/auth/logout | Revoke tokens | authSlice (logout) | revoked_token |
| GET /api/auth/me | Fetch current user profile | authSlice, Topbar | user |
| POST /api/auth/refresh | Renew access token | axios interceptor | revoked_token |
| POST /api/auth/forgot-password/request | Send OTP to email | ForgotPasswordPage.jsx | password_reset_token |
| POST /api/auth/forgot-password/verify | Verify OTP → reset token | ForgotPasswordPage.jsx | password_reset_token |
| POST /api/auth/forgot-password/reset | Apply new password | ForgotPasswordPage.jsx | user |
| GET /api/users | List all users (admin) | UsersPage.jsx | user, role |
| GET /api/users/{id} | Get user by ID | UsersPage.jsx | user |
| POST /api/users | Create user (admin) | UsersPage.jsx | user |
| PUT /api/users/{id} | Update user | UsersPage.jsx | user |
| PATCH /api/users/{id}/status | Toggle user active/inactive | UsersPage.jsx | user |
| GET /api/vehicles | List vehicles (admin) | VehiclesAdminPage | vehicle |
| GET /api/vehicles/{id} | Get vehicle by ID | MyVehiclesPage, VehiclesAdminPage | vehicle |
| GET /api/vehicles/by-user/{userId} | List customer's own vehicles | MyVehiclesPage.jsx | vehicle |
| POST /api/vehicles | Add vehicle | MyVehiclesPage.jsx | vehicle |
| PUT /api/vehicles/{id} | Edit vehicle | MyVehiclesPage, VehiclesAdminPage | vehicle |
| PATCH /api/vehicles/{id}/status | Toggle vehicle active | VehiclesAdminPage.jsx | vehicle |
| GET /api/catalog | List services | CatalogPage, BookServicePage | service_catalog |
| GET /api/catalog/{id} | Get catalog service by ID | BookServicePage | service_catalog |
| POST /api/catalog | Create service (admin) | CatalogPage.jsx | service_catalog |
| PUT /api/catalog/{id} | Update service | CatalogPage.jsx | service_catalog |
| PATCH /api/catalog/{id}/toggle | Toggle service active | CatalogPage.jsx | service_catalog |
| GET /api/work-items | List work items | WorkItemsPage, ManageServicePage | work_item_catalog |
| GET /api/work-items/{id} | Get work item by ID | WorkItemsPage | work_item_catalog |
| POST /api/work-items | Create work item (admin) | WorkItemsPage.jsx | work_item_catalog |
| PUT /api/work-items/{id} | Update work item | WorkItemsPage.jsx | work_item_catalog |
| PATCH /api/work-items/{id}/toggle | Toggle work item active | WorkItemsPage.jsx | work_item_catalog |
| GET /api/availability | List slots (admin) | AvailabilityPage | service_availability |
| GET /api/availability/bookable | List bookable slots | BookServicePage (date picker) | service_availability |
| POST /api/availability | Create slot | AvailabilityPage.jsx | service_availability |
| PUT /api/availability/{id} | Update slot | AvailabilityPage.jsx | service_availability |
| POST /api/availability/bulk | Bulk-create slots | AvailabilityPage.jsx | service_availability |
| PATCH /api/availability/{id}/toggle | Toggle slot availability | AvailabilityPage.jsx | service_availability |
| DELETE /api/availability/{id} | Soft-delete empty future slot | AvailabilityPage.jsx | service_availability |
| GET /api/bookings | List bookings (role-scoped) | BookingsAdminPage, CustomerBookingsPage | service_booking |
| GET /api/bookings/{id} | Get booking by ID | BookingsAdminPage, CustomerBookingsPage | service_booking |
| GET /api/bookings/overdue | Overdue confirmed bookings (admin) | OverdueBookingsPage.jsx | service_booking |
| GET /api/bookings/{id}/history | Booking audit trail | BookingsAdminPage.jsx | booking_history |
| POST /api/bookings | Create booking | CustomerBookServicePage, AdminBookServicePage | service_booking, service_availability, payment |
| PATCH /api/bookings/{id}/confirm | Confirm + assign advisor | BookingsAdminPage.jsx | service_booking, service_record, service_advisor |
| PATCH /api/bookings/{id}/cancel | Cancel booking | BookingsAdminPage, CustomerBookingsPage | service_booking, service_availability |
| PATCH /api/bookings/{id}/reschedule | Reschedule | CustomerBookingsPage.jsx | service_booking, service_availability |
| PATCH /api/bookings/{id}/reassign-advisor | Reassign advisor (admin) | BookingsAdminPage.jsx | service_booking, service_advisor |
| GET /api/service-records | List records (advisor/admin) | ManageServicePage.jsx, AdvisorDashboard | service_record |
| GET /api/service-records/{id} | Get record by ID | ManageServicePage, ServiceDetailsPage | service_record |
| GET /api/service-records/{id}/items | Get items on a record | ManageServicePage.jsx | service_item |
| PATCH /api/service-records/{id}/start | Start service | ManageServicePage.jsx | service_record |
| PUT /api/service-records/{id}/items | Save work items | ManageServicePage.jsx | service_item |
| PATCH /api/service-records/{id}/remarks | Update remarks | ManageServicePage.jsx | service_record |
| PATCH /api/service-records/{id}/complete | Complete + generate invoice | CompleteServicePage.jsx | service_record, invoice |
| GET /api/invoices | List invoices (role-scoped) | InvoicesAdminPage, CustomerInvoicesPage | invoice, payment |
| GET /api/invoices/{id} | Invoice detail | CustomerInvoicesPage, PaymentPage | invoice |
| POST /api/invoices/{id}/pay | Record manual payment (cash) | PaymentPage.jsx | payment |
| POST /api/invoices/{id}/checkout/order | Create Razorpay order for invoice | PaymentPage.jsx (digital) | payment (via Razorpay) |
| POST /api/invoices/{id}/checkout/verify | Verify Razorpay invoice payment | PaymentPage.jsx | payment |
| POST /api/payments/booking-charge/order | Razorpay order for booking advance | CustomerBookServicePage | payment |
| POST /api/payments/booking-charge/verify | Verify booking charge payment | CustomerBookServicePage | payment |
| GET /api/service-feedback | List all feedback (admin) | InvoicesAdminPage context | service_feedback |
| GET /api/service-feedback/service/{serviceId} | Get feedback status for service | CustomerInvoicesPage | service_feedback |
| POST /api/service-feedback/service/{serviceId} | Submit feedback | CustomerInvoicesPage.jsx | service_feedback |
| GET /api/notifications | Get notifications (current user) | NotificationsPage, NotifBadge | notification |
| PATCH /api/notifications/{id}/read | Mark notification read | NotificationsPage.jsx | notification |
| PATCH /api/notifications/read-all | Mark all read | NotificationsPage.jsx | notification |
| DELETE /api/notifications/{id} | Delete notification | NotificationsPage.jsx | notification |
| GET /api/advisors | List advisors (admin) | AdvisorsPage, BookingsAdminPage | service_advisor, user |
| GET /api/advisors/available | List available advisors | BookingsAdminPage (assign) | service_advisor |
| GET /api/advisors/{id} | Get advisor by ID | AdvisorsPage | service_advisor |
| PUT /api/advisors/{id} | Update advisor profile | AdvisorsPage, AdvisorProfilePage | service_advisor |
| PATCH /api/advisors/{id}/status | Toggle advisor availability | AdvisorsPage.jsx | service_advisor, user |
| GET /api/profile | Get own profile | AdminProfilePage, AdvisorProfilePage, CustomerProfilePage | user, service_advisor |
| PUT /api/profile | Update name & phone | AdminProfilePage, AdvisorProfilePage, CustomerProfilePage | user |
| POST /api/profile/change-password/request-otp | Send OTP for password change | ProfilePage (all roles) | password_reset_token |
| POST /api/profile/change-password/verify-otp | Verify OTP for password change | ProfilePage (all roles) | password_reset_token |
| POST /api/profile/change-password | Apply password change | ProfilePage (all roles) | user |
| GET /api/dashboard | Admin KPIs | AdminDashboardPage.jsx | Multiple tables aggregate |
| GET /api/audit-logs | Full audit log (admin) | AuditLogsPage.jsx | booking_history |

---

## 7. Prerequisites

### 7.1 Required Software

- **Java 17+:** JDK required to build and run the Spring Boot backend.
- **Maven 3.9+:** Used by the Spring Boot Maven plugin (or use the Maven wrapper if included).
- **MySQL 8.0+:** InnoDB required. The schema uses generated columns and trigger DDL that are MySQL 8 features.
- **Node.js 18+ + npm:** Required to install frontend dependencies and run the Vite dev server.
- **Razorpay account:** Needed for payment gateway features. Obtain Key ID and Key Secret from the Razorpay dashboard.
- **Resend account:** Email delivery for OTP. Get API key from resend.com. Free tier covers development.
- **Twilio account (optional):** SMS notifications. If `TWILIO_ENABLED=false` the feature is disabled gracefully.

### 7.2 Backend Setup

#### Step 1 — Database

```bash
mysql -u root -p < schema.sql    # Run the full DDL script
```

#### Step 2 — Environment

Copy `.env.example` to `.env` and fill in all values:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/vehicle_service_wb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=<your_password>
SERVER_PORT=8080
JWT_SECRET=<32+ char random string>
VSERV_CORS_ALLOWED_ORIGINS=http://localhost:5173
RESEND_API_KEY=<resend_key>
RESEND_SENDER_EMAIL=onboarding@resend.dev
RAZORPAY_ENABLED=true
RAZORPAY_KEY_ID=<rzp_key_id>
RAZORPAY_KEY_SECRET=<rzp_secret>
TWILIO_ENABLED=true
TWILIO_ACCOUNT_SID=<sid>
TWILIO_AUTH_TOKEN=<token>
TWILIO_PHONE_NUMBER=<e164_number>
```

#### Step 3 — Seed Roles

The `ApplicationStartupRunner` inserts the three roles on first boot if they are absent. No manual seed is required.

#### Step 4 — Run

```bash
./mvnw spring-boot:run          # Dev mode with DevTools hot-reload
./mvnw package -DskipTests      # Build JAR
java -jar target/vserv-backend-2.0.0.jar  # Run JAR
```

### 7.3 Frontend Setup

#### Step 1 — Install

```bash
cd frontend && npm install
```

#### Step 2 — Environment

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_DEV_PORT=5173
VITE_RAZORPAY_KEY_ID=<rzp_key_id>
```

#### Step 3 — Run

```bash
npm run dev       # Vite dev server at http://localhost:5173
npm run build     # Production build → dist/
npm run preview   # Serve the dist/ build locally
```

The built `dist/` folder is purely static — serve it from Nginx, a CDN, or any static host. The backend must be reachable at `VITE_API_BASE_URL`.

---

## 8. Edge Cases Handled

### 8.1 Authentication & Security

- **JWT denylist on logout:** Both the access token and refresh token are stored in the `revoked_token` table on logout. The `JwtAuthenticationFilter` checks this table before accepting any token — preventing reuse of stolen tokens even within their validity window.
- **Refresh token rotation:** Refresh returns a new access token. Presenting a revoked refresh token is rejected immediately.
- **Email already registered:** Registration returns a generic 409 Conflict without revealing which field is taken, preventing user enumeration.
- **OTP expiry:** Password reset OTPs are time-limited (stored `expires_at`). Expired OTPs are rejected. The `used` flag prevents replay once a token is consumed.
- **Concurrent password reset:** Only one valid (unused, unexpired) OTP per user is active at a time.

### 8.2 Booking System

- **Double-booking prevention (vehicle level):** The partial unique index `uq_active_vehicle_datetime` on `(vehicle_id, service_date, active_slot)` prevents the same vehicle from holding two PENDING/CONFIRMED/RESCHEDULED bookings in the same date+slot. CANCELLED or COMPLETED bookings produce a NULL `active_slot` and are excluded.
- **Slot capacity enforcement:** `service_availability.current_bookings` is incremented on booking and decremented on cancellation. Bookings are rejected when `current_bookings >= max_bookings` or `is_available = FALSE`.
- **Past date bookings:** The booking service rejects `service_date` values in the past.
- **Overdue confirmed bookings:** `GET /api/bookings/overdue` surfaces bookings that are CONFIRMED but whose `service_date` has passed, allowing the admin to take action.
- **Rescheduling availability:** Before accepting a reschedule, the system checks the target slot has capacity and is available. The original slot's booking count is decremented only on success.
- **Slot release on cancel:** Cancelling a booking decrements `current_bookings` on the linked `service_availability` row to return capacity.
- **Advisor re-assignment:** Admin can reassign a booking's advisor. The old advisor's `current_load` is decremented and the new advisor's incremented atomically.

### 8.3 Vehicle & User Deactivation Guards

- **StatusToggleGuard on users:** Before deactivating a CUSTOMER, the system checks for open bookings (PENDING/CONFIRMED/RESCHEDULED) and unsettled invoices (PENDING/PARTIALLY_PAID). If either exists, deactivation is blocked with a descriptive error.
- **StatusToggleGuard on advisors:** Before deactivating an ADVISOR, the system checks for open service record assignments (IN_PROGRESS or PENDING) and non-zero `current_load`. Blocked with a reason if found.
- **StatusToggleGuard on vehicles:** Deactivating a vehicle checks for open bookings and unsettled invoices linked to that vehicle.
- **Reactivating a vehicle with inactive owner:** The guard prevents a vehicle from being re-activated if its owner's account is currently INACTIVE.

### 8.4 Vehicle Soft-Delete & Archiving

- **Archived vehicle info on soft-delete:** When a vehicle is soft-deleted, the database trigger `before_vehicle_delete` copies brand, model, and registration_number into `archived_vehicle_info` on all linked `service_booking` rows, and the owner ID into `archived_owner_id`. Historical booking records remain fully queryable even after the vehicle is logically removed.
- **Registration number remains unique:** Even soft-deleted vehicles retain their unique `registration_number` constraint, preventing a new vehicle from re-using a deleted plate.

### 8.5 Invoice & Payment

- **Invoice only visible post-completion:** `GET /api/invoices/{id}` returns a BusinessException if the linked service record is not yet COMPLETED. Customers cannot see or pay a draft invoice.
- **Booking charge advance deduction:** When a customer pays the booking charge via Razorpay, the amount is stored in `invoice.advance_amount`. On final payment, this is deducted from `total_amount` so the customer pays only the balance.
- **Partial payment tracking:** `invoice.payment_status` moves from PENDING to PARTIALLY_PAID to PAID based on cumulative payments recorded in the `payment` table. Paying in instalments is supported.
- **Razorpay signature verification:** Before recording any digital payment, the backend verifies the HMAC-SHA256 Razorpay signature on `orderId + paymentId`. Tampered or replayed payment verifications are rejected.
- **Booking charge requires digital method:** CASH is explicitly disallowed for booking charge payments (must be UPI, CARD, or NET_BANKING), enforced both in the gateway controller and service layer.
- **Invoice date auto-stamped:** The `trg_invoice_date` trigger ensures `invoice_date` is always `CURDATE()` if the application omits it.

### 8.6 Service Availability — Startup Seeding

- **Automatic slot generation:** `SlotMaintenanceService` runs at every application startup (via `ApplicationStartupRunner`). It seeds today's and tomorrow's time slots (09:00–19:00 in 2-hour bands, 5 slots per day) if they don't already exist.
- **Past slot cleanup:** A single bulk UPDATE marks all `service_availability` rows with `service_date < today` as `is_available = FALSE`, preventing bookings on stale slots without deleting historical data.
- **Idempotent seeding:** The seed check uses `existsByServiceDate`, so repeated restarts do not create duplicate slot rows.

### 8.7 Access Control

- **Booking access isolation:** Customers can only read and modify their own bookings. Advisors can only read bookings assigned to them. Admins have unrestricted access.
- **Invoice access isolation:** Customers only see invoices for their own vehicles. The backend checks the invoice's linked vehicle owner matches the requesting user.
- **Service record access:** Advisors can only access records assigned to them. Admins can access all.
- **Customer cannot book other vehicles:** When creating a booking, the system verifies the requested vehicle's owner ID matches the authenticated customer.
- **Frontend route guards:** `AuthGuard` wraps every protected route and checks both authentication status and role. A customer navigating to `/admin/dashboard` is redirected to `/login`.

### 8.8 Data Integrity

- **Enum validation:** All ENUM columns (car_type, service_type, booking_status, etc.) are constrained at both the DB schema level and the Java entity/DTO level with `@Valid` annotations.
- **Generated total_price:** `service_item.total_price` is a STORED generated column (quantity × unit_price), preventing application-layer calculation drift.
- **Cascading deletes:** `user → vehicle` (ON DELETE CASCADE); `service_record → service_item` (ON DELETE CASCADE); `user → notification` (ON DELETE CASCADE). Booking to notification uses SET NULL to preserve the notification text even if the booking is deleted.
- **Pagination:** All list endpoints support optional `page` and `size` query parameters. When absent, the full list is returned. `PaginationUtils.isPaged()` guards this gracefully.
- **Search:** Booking, service record, and invoice list endpoints support a `q` (full-text substring) query parameter that searches across multiple fields (owner name, vehicle info, service name, advisor name, etc.) in-memory after DB fetch.

---

## 9. Key Business Flows

### 9.1 Booking Lifecycle

1. **Customer creates booking:** Selects vehicle → catalog service → date → slot → pays booking charge via Razorpay (or waived by admin). Booking status = PENDING.
2. **Admin confirms booking:** Optionally assigns an advisor. Booking status = CONFIRMED. Service record created (status = PENDING). Advisor `current_load` incremented.
3. **Advisor starts service:** Marks service record as IN_PROGRESS. Service start time stamped.
4. **Advisor adds work items:** Saves parts, labour, and consumables against the service record.
5. **Advisor completes service:** Submits actual hours and final item list. Service record status = COMPLETED. Invoice auto-generated. Booking status = COMPLETED.
6. **Customer pays invoice:** Via Razorpay (digital) or admin records cash. Advance booking charge deducted.
7. **Customer submits feedback:** One-time 1–5 star rating with optional text attached to the service record.

### 9.2 Password Reset Flow

1. **Request OTP:** Customer submits email. If found, a hashed 6-digit OTP is stored in `password_reset_token` with a 15-minute TTL. OTP sent via Resend API.
2. **Verify OTP:** Customer submits email + OTP. Hash compared. On success, `otp_verified = TRUE` and a signed reset token is returned.
3. **Reset password:** Customer submits reset token + new password. Token checked for validity and `used = FALSE`. Password BCrypt-hashed and updated. Token marked `used = TRUE`.

### 9.3 In-Profile Password Change Flow

1. **Request OTP:** Authenticated user calls `POST /api/profile/change-password/request-otp`. OTP sent to own email.
2. **Verify OTP:** User submits OTP via `POST /api/profile/change-password/verify-otp`. Receives reset token.
3. **Change password:** User submits reset token + new password via `POST /api/profile/change-password`. Token consumed.

### 9.4 Notification Flow

Notifications are stored in the `notification` table and surfaced via `GET /api/notifications` (current-user-scoped). The `NotifBadge` component polls periodically to show unread count. Notifications are created server-side on booking confirmation, status changes, and service completion. Individual notifications can be marked read or deleted by the owner.

---

## 10. Deployment Notes

- **CORS:** Set `VSERV_CORS_ALLOWED_ORIGINS` to the exact origin(s) of your deployed frontend (e.g., `https://vserv.example.com`). Multiple origins are comma-separated.
- **JWT Secret:** Use at least 32 random bytes. Rotate if compromised; all existing tokens will be invalidated.
- **Razorpay mode:** Set `RAZORPAY_ENABLED=false` to disable payment gateway in environments without Razorpay credentials. Payment-related endpoints will return errors; keep disabled only in development without payment testing.
- **Twilio:** Set `TWILIO_ENABLED=false` to silence SMS without errors.
- **Static frontend:** Run `npm run build`. Serve the `dist/` folder via Nginx or a CDN. Point `VITE_API_BASE_URL` at the backend host.
- **Database migrations:** The project does not use Flyway or Liquibase. Manage schema changes manually via DDL scripts.
- **Slot seeding:** `SlotMaintenanceService` runs at every startup. In production, ensure the application is restarted daily (or schedule a CRON endpoint) so tomorrow's slots are always seeded.

---

*Confidential — Internal Developer Reference*
