/**
 * Sidebar Generator - Tự động hiển thị menu theo vai trò người dùng
 * Đảm bảo tất cả trang quản lý dùng chung một cấu trúc và style.
 */
const Sidebar = {
    init() {
        this.injectStyles();
        this.render();
    },

    injectStyles() {
        if (!document.querySelector('link[href*="sidebar.css"]')) {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = '/css/sidebar.css';
            document.head.appendChild(link);
        }
    },

    render() {
        const role = localStorage.getItem('cinemaRole');
        const username = localStorage.getItem('cinemaUsername') || 'User';
        let sidebarEl = document.querySelector('.sidebar');
        
        if (!sidebarEl) {
            sidebarEl = document.createElement('aside');
            sidebarEl.className = 'sidebar';
            document.body.prepend(sidebarEl);
        }

        const currentPath = window.location.pathname;

        let menuHtml = `
            <div class="sidebar-header">
                <a href="/index.html" class="brand">STAR<span>CINEMA</span></a>
            </div>
            <ul class="nav-menu list-unstyled">
        `;

        // MENU CHUNG (DASHBOARD)
        if (['ADMIN', 'MANAGER'].includes(role)) {
            menuHtml += `
                <li class="nav-section-title">Tổng quan</li>
                <li class="nav-item"><a href="/dashboard/executive-dashboard.html" class="nav-link-admin ${currentPath.includes('executive-dashboard') ? 'active' : ''}"><i class="bi bi-speedometer2"></i> Dashboard Chiến lược</a></li>
            `;
        }

        // MENU MANAGER (Nghiệp vụ rạp)
        if (role === 'MANAGER') {
            menuHtml += `
                <li class="nav-section-title">Báo cáo & Thống kê</li>
                <li class="nav-item"><a href="/dashboard/tickets-report.html" class="nav-link-admin ${currentPath.includes('tickets-report') ? 'active' : ''}"><i class="bi bi-ticket-perforated"></i> Báo cáo Vé</a></li>
                <li class="nav-item"><a href="/dashboard/customers-report.html" class="nav-link-admin ${currentPath.includes('customers-report') ? 'active' : ''}"><i class="bi bi-people"></i> Báo cáo Khách hàng</a></li>
                <li class="nav-item"><a href="/dashboard/fnb-report.html" class="nav-link-admin ${currentPath.includes('fnb-report') ? 'active' : ''}"><i class="bi bi-cup-hot"></i> Báo cáo F&B</a></li>
                <li class="nav-item"><a href="/dashboard/promotions-report.html" class="nav-link-admin ${currentPath.includes('promotions-report') ? 'active' : ''}"><i class="bi bi-tags"></i> Báo cáo Khuyến mãi</a></li>
                <li class="nav-item"><a href="/dashboard/staff-report.html" class="nav-link-admin ${currentPath.includes('staff-report') ? 'active' : ''}"><i class="bi bi-person-badge"></i> Báo cáo Nhân sự</a></li>

                <li class="nav-section-title">Quản lý Nghiệp vụ</li>
                <li class="nav-item"><a href="/manage-movies.html" class="nav-link-admin ${currentPath.includes('manage-movies') ? 'active' : ''}"><i class="bi bi-film"></i> Quản lý phim</a></li>
                <li class="nav-item"><a href="/manage-showtimes.html" class="nav-link-admin ${currentPath.includes('manage-showtimes') ? 'active' : ''}"><i class="bi bi-calendar-event"></i> Suất chiếu</a></li>
                <li class="nav-item"><a href="/manage-rooms.html" class="nav-link-admin ${currentPath.includes('manage-rooms') ? 'active' : ''}"><i class="bi bi-door-open"></i> Phòng chiếu</a></li>
                <li class="nav-item"><a href="/manage-pricing.html" class="nav-link-admin ${currentPath.includes('manage-pricing') ? 'active' : ''}"><i class="bi bi-currency-dollar"></i> Cấu hình giá</a></li>
                <li class="nav-item"><a href="/manage-combos.html" class="nav-link-admin ${currentPath.includes('manage-combos') ? 'active' : ''}"><i class="bi bi-bag-check"></i> Combo & Bắp nước</a></li>
                <li class="nav-item"><a href="/manage-promotions.html" class="nav-link-admin ${currentPath.includes('manage-promotions') ? 'active' : ''}"><i class="bi bi-tags"></i> Khuyến mãi</a></li>
                <li class="nav-item"><a href="/manage-membership.html" class="nav-link-admin ${currentPath.includes('manage-membership') ? 'active' : ''}"><i class="bi bi-card-checklist"></i> Hạng thành viên</a></li>
                <li class="nav-item"><a href="/manage-audit.html" class="nav-link-admin ${currentPath.includes('manage-audit') ? 'active' : ''}"><i class="bi bi-shield-check"></i> Nhật ký hệ thống</a></li>
            `;
        }

        // MENU ADMIN (Quản lý User)
        if (role === 'ADMIN') {
            menuHtml += `
                <li class="nav-section-title">Hệ thống</li>
                <li class="nav-item"><a href="/manage-users.html" class="nav-link-admin ${currentPath.includes('manage-users') ? 'active' : ''}"><i class="bi bi-people"></i> Quản lý người dùng</a></li>
            `;
        }

        // MENU STAFF (Bán vé)
        if (role === 'STAFF') {
            menuHtml += `
                <li class="nav-section-title">Nghiệp vụ quầy</li>
                <li class="nav-item"><a href="/pos.html" class="nav-link-admin ${currentPath.includes('pos') ? 'active' : ''}"><i class="bi bi-pc-display"></i> Bán vé tại quầy</a></li>
            `;
        }

        menuHtml += `
            </ul>
            <div class="user-panel">
                <div class="admin-profile">
                    <div class="admin-avatar">${username.charAt(0).toUpperCase()}</div>
                    <div>
                        <div style="font-size:0.9rem;font-weight:600;color:#fff">${username}</div>
                        <div style="font-size:0.75rem;color:var(--text-muted)">${this.getRoleName(role)}</div>
                    </div>
                </div>
                <button class="btn btn-sm btn-outline-danger w-100 mt-3" onclick="AuthGuard.logout()">
                    <i class="bi bi-box-arrow-right me-2"></i> Đăng xuất
                </button>
            </div>
        `;

        sidebarEl.innerHTML = menuHtml;
    },

    getRoleName(role) {
        const roles = {
            'ADMIN': 'Quản trị viên',
            'MANAGER': 'Quản lý rạp',
            'STAFF': 'Nhân viên',
            'CUSTOMER': 'Khách hàng'
        };
        return roles[role] || role;
    }
};

document.addEventListener('DOMContentLoaded', () => Sidebar.init());
