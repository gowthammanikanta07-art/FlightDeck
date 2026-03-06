 



CREATE DATABASE IF NOT EXISTS flightdeck;

-- DB for integration testing cycle only
CREATE DATABASE IF NOT EXISTS flightdeck_test;


USE flightdeck;

CREATE TABLE IF NOT EXISTS flights (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number    VARCHAR(20)  NOT NULL UNIQUE,
    airline_name     VARCHAR(100) NOT NULL,
    origin           VARCHAR(100) NOT NULL,
    destination      VARCHAR(100) NOT NULL,
    departure_time   DATETIME     NOT NULL,
    arrival_time     DATETIME     NOT NULL,
    price            DOUBLE       NOT NULL,
    available_seats  INT          NOT NULL
);


CREATE TABLE IF NOT EXISTS bookings (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    passenger_name    VARCHAR(100) NOT NULL,
    passenger_email   VARCHAR(100) NOT NULL,
    flight_id         BIGINT       NOT NULL,
    booking_reference VARCHAR(50)  NOT NULL UNIQUE,
    FOREIGN KEY (flight_id) REFERENCES flights(id)
);


CREATE TABLE IF NOT EXISTS coupons (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    discount_percent DOUBLE       NOT NULL,
    expiry_date      DATE         NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- =============================================
-- SAMPLE DATA — FLIGHTS
-- =============================================

INSERT INTO flights
    (flight_number, airline_name, origin, destination,
     departure_time, arrival_time, price, available_seats)
VALUES
    ('AI101', 'Air India',    'Delhi',   'Mumbai',
     '2025-06-01 08:00:00', '2025-06-01 10:00:00', 2500.00, 120),

    ('AI102', 'Air India',    'Mumbai',  'Delhi',
     '2025-06-01 12:00:00', '2025-06-01 14:00:00', 2500.00, 110),

    ('6E201', 'IndiGo',       'Delhi',   'Bangalore',
     '2025-06-01 09:00:00', '2025-06-01 11:30:00', 3200.00, 150),

    ('6E202', 'IndiGo',       'Bangalore','Delhi',
     '2025-06-01 14:00:00', '2025-06-01 16:30:00', 3200.00, 140),

    ('SG301', 'SpiceJet',     'Mumbai',  'Chennai',
     '2025-06-01 10:00:00', '2025-06-01 12:00:00', 2800.00, 100),

    ('SG302', 'SpiceJet',     'Chennai', 'Mumbai',
     '2025-06-01 15:00:00', '2025-06-01 17:00:00', 2800.00, 95),

    ('UK401', 'Vistara',      'Delhi',   'Hyderabad',
     '2025-06-01 07:00:00', '2025-06-01 09:30:00', 3500.00, 130),

    ('UK402', 'Vistara',      'Hyderabad','Delhi',
     '2025-06-01 16:00:00', '2025-06-01 18:30:00', 3500.00, 125);

INSERT INTO coupons
    (code, discount_percent, expiry_date, active)
VALUES
    ('SAVE10',     10.0, '2026-12-31', TRUE),   -- Valid 10% off
    ('SAVE20',     20.0, '2026-12-31', TRUE),   -- Valid 20% off
    ('SAVE30',     30.0, '2026-12-31', TRUE),   -- Valid 30% off
    ('EXPIRED20',  20.0, '2024-01-01', TRUE),   -- Expired coupon
    ('INACTIVE30', 30.0, '2026-12-31', FALSE);  -- Inactive coupon
