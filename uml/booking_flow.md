# 🎭 Sequence Diagram - Luồng Đặt Vé Tích Hợp Pricing Engine

Đây là bản thiết kế cho **Step 1.1** thuộc [CONDUCTOR.md](file:///d:/Fullit/projects/NNLTTT/Final/CINEMA_MANAGEMENT_SYSTEM/CONDUCTOR.md). Sơ đồ mô tả luồng đặt vé online/quầy đã được refactor để tuân thủ SOLID:
1. Xác định khách hàng thông qua Query Method (không dùng `findAll().stream()`).
2. Gọi `PricingService` để tính giá vé động cho từng ghế (áp dụng VIP surcharge, weekend rule, member discount) thay vì nhân cứng 95.000đ.

---

## 1. Sequence Diagram: Luồng Đặt Vé Online (CustomerBookingFacade)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách hàng
    participant Controller as BookingController
    participant Facade as CustomerBookingFacade
    participant CustRepo as CustomerRepository
    participant ShowtimeRepo as ShowtimeRepository
    participant SeatRepo as SeatRepository
    participant Pricing as PricingService
    participant ComboRepo as ComboRepository
    participant BookRepo as BookingRepository
    participant PayRepo as PaymentRepository
    participant Event as ApplicationEventPublisher

    Customer->>Controller: POST /api/bookings/online (OnlineBookingRequest)
    Controller->>Facade: processOnlineBooking(request, username)
    
    alt username != null (Thành viên đăng nhập)
        Facade->>CustRepo: findByUserUsername(username)
        CustRepo-->>Facade: Customer (hoặc null)
    else Khách vãng lai (Guest)
        Facade->>Facade: Validate guest info (Tên, SĐT, Email)
    end

    Facade->>ShowtimeRepo: findById(request.showtimeId)
    ShowtimeRepo-->>Facade: Showtime

    loop Với mỗi seatId trong request.seatIds
        Facade->>SeatRepo: findById(seatId)
        SeatRepo-->>Facade: Seat
        Facade->>Pricing: calculateTicketPrice(showtime, seat, customer)
        Pricing-->>Facade: PriceCalculationResult (finalPrice, appliedRules)
        Note over Facade, Pricing: Tính giá vé động dựa trên loại phòng, loại ghế và hạng thành viên
        Facade->>Facade: Tích lũy totalPrice += finalPrice
    end

    opt Có chọn combos bắp nước
        loop Với mỗi comboId, quantity
            Facade->>ComboRepo: findById(comboId)
            ComboRepo-->>Facade: Combo
            Facade->>Facade: Tích lũy totalPrice += combo.price * quantity
        end
    end

    Facade->>BookRepo: save(Booking)
    BookRepo-->>Facade: Booking (bookingCode)

    Facade->>PayRepo: save(Payment)
    PayRepo-->>Facade: Payment

    Facade->>Event: publishEvent(TicketBookedEvent)
    Note over Event: Observer: Gửi Email xác nhận đặt vé thành công

    Facade-->>Controller: BookingResponse
    Controller-->>Customer: 200 OK (BookingResponse)
```

---

## 2. Sequence Diagram: Luồng Bán Vé Tại Quầy (StaffPosFacade)

```mermaid
sequenceDiagram
    autonumber
    actor Staff as Nhân viên quầy
    participant Controller as POSController
    participant Facade as StaffPosFacade
    participant CustRepo as CustomerRepository
    participant ShowtimeRepo as ShowtimeRepository
    participant SeatRepo as SeatRepository
    participant Pricing as PricingService
    participant ComboRepo as ComboRepository
    participant BookRepo as BookingRepository
    participant PayRepo as PaymentRepository
    participant Event as ApplicationEventPublisher

    Staff->>Controller: POST /api/staff/bookings/pos (PosBookingRequest)
    Controller->>Facade: processDirectBooking(request)
    
    alt Có nhập Số điện thoại khách hàng
        Facade->>CustRepo: findByPhone(phone)
        CustRepo-->>Facade: Customer (Nếu có)
    end

    Facade->>ShowtimeRepo: findById(request.showtimeId)
    ShowtimeRepo-->>Facade: Showtime

    loop Với mỗi seatId trong request.seatIds
        Facade->>SeatRepo: findById(seatId)
        SeatRepo-->>Facade: Seat
        Facade->>Pricing: calculateTicketPrice(showtime, seat, customer)
        Pricing-->>Facade: PriceCalculationResult (finalPrice)
        Facade->>Facade: Tích lũy totalPrice += finalPrice
    end

    opt Có chọn combos bắp nước
        loop Với mỗi comboId, quantity
            Facade->>ComboRepo: findById(comboId)
            ComboRepo-->>Facade: Combo
            Facade->>Facade: Tích lũy totalPrice += combo.price * quantity
        end
    end

    Facade->>BookRepo: save(Booking)
    BookRepo-->>Facade: Booking (bookingCode)

    Facade->>PayRepo: save(Payment)
    PayRepo-->>Facade: Payment (SUCCESS)

    opt Khách hàng có email liên kết
        Facade->>Event: publishEvent(TicketBookedEvent)
    end

    Facade-->>Controller: BookingResponse
    Controller-->>Staff: 200 OK (BookingResponse)
```
