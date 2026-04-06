# Plan: Implement Statistics Backend for Cinema Management System

## Objective
Implement comprehensive backend API for dashboard reports (Executive, Customers, F&B, Promotions, Staff, Tickets) as required by the frontend.

## Key Files & Context
- `StatisticsController.java`: Endpoints for each report.
- `StatisticsService.java`: Interface defining report retrieval methods.
- `StatisticsServiceImpl.java`: Implementation using various repositories.
- `BookingRepository.java`, `CustomerRepository.java`, `PromotionRepository.java`, `ComboRepository.java`, `UserRepository.java`: Data sources.
- `com.example.cinema.model.dto.response.statistics.*`: New response DTOs.

## Implementation Steps

### 1. Define DTOs
Create specific response DTOs for each report under `com.example.cinema.model.dto.response.statistics`:
- `CustomerStatisticsResponse`: KPI stats, tier counts, distribution maps, and top customer list.
- `FnBStatisticsResponse`: KPI stats, product list, category revenue, daily trend, stock alerts.
- `PromotionStatisticsResponse`: KPI stats, campaign list, usage distribution, daily trend.
- `StaffStatisticsResponse`: KPI stats, staff performance list, attendance, shift distribution.
- `TicketStatisticsResponse`: KPI stats, channel split, seat type stats, daily trend, recent tickets.

### 2. Update Repositories
Add complex aggregation queries using JPQL in existing repositories:
- `BookingRepository`: Aggregates for tickets, seat types, revenue by channel, etc.
- `CustomerRepository`: Aggregates for membership tiers, return rates, avg spend.
- `PromotionRepository`: Usage and ROI calculations.
- `ComboRepository`: F&B sales and categories.
- `UserRepository`: Staff performance and attendance (mock for now if no attendance table exists).

### 3. Implement Service Logic
Refactor `StatisticsServiceImpl` to implement new methods.
- Use **Strategy Pattern** or **Facade Pattern** if necessary to avoid a massive service file.
- For this phase, I will add methods to `StatisticsService` and implementation in `StatisticsServiceImpl`, grouping logic internally to maintain readability.

### 4. Update Controller
Add endpoints:
- `GET /api/statistics/customers`
- `GET /api/statistics/fnb`
- `GET /api/statistics/promotions`
- `GET /api/statistics/staff`
- `GET /api/statistics/tickets`
- `GET /api/statistics/overview` (Refine existing)

## Verification & Testing
- Use **MockMvc** to test each API endpoint with various date ranges.
- Validate the structure of returned JSON matches the requirements of the frontend.

## Migration & Rollback
- No database schema changes required as we are only adding queries.
- Rollback: Revert `StatisticsServiceImpl` and `StatisticsController`.
