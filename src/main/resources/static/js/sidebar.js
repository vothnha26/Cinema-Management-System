/**
 * Sidebar Generator - Tách biệt hoàn toàn menu ADMIN và MANAGER
 */
const Sidebar = {
    init() {
        this.injectStyles();
        this.render();
    },

    injectStyles() {
        if (document.getElementById('sidebar-styles')) return;
        const link = document.createElement('link');
        link.id = 'sidebar-styles';
        link.rel = 'stylesheet';
        link.href = '/css/sidebar.css';
        document.head.appendChild(link);
    },

    render() {
        const sidebarEl = document.querySelector('.sidebar');
        if (!sidebarEl) return;

        const role = localStorage.getItem('cinemaRole');
        const username = localStorage.getItem('cinemaUsername') || 'Người dùng';
        const currentPath = window.location.pathname;

        let menuHtml = `
            <div class="brand">
                <i class="bi bi-stars"></i>
                <span>STAR<span>CINEMA</span></span>
            </div>
            
            <div class="user-profile-sm">
                <div class="avatar">${username.charAt(0).toUpperCase()}</div>
                <div class="info">
                    <div class="name">${username}</div>
                    <div class="role-badge">${this.getRoleName(role)}</div>
                </div>
            </div>

            <ul class="nav-menu">
        `;

        if (role === 'ADMIN') {
            menuHtml += `
                <li class="nav-section-title">QUẢN TRỊ HỆ THỐNG</li>
                <li class="nav-item"><a href="/dashboard.html" class="nav-link-admin ${currentPath.includes('dashboard') ? 'active' : ''}"><i class="bi bi-speedometer2"></i> Tổng quan</a></li>
                <li class="nav-item"><a href="/manage-branches.html" class="nav-link-admin ${currentPath.includes('manage-branches') ? 'active' : ''}"><i class="bi bi-building"></i> Chi nhánh</a></li>
                <li class="nav-item"><a href="/manage-users.html" class="nav-link-admin ${currentPath.includes('manage-users') ? 'active' : ''}"><i class="bi bi-people"></i> Người dùng</a></li>
                <li class="nav-item"><a href="/manage-master-data.html" class="nav-link-admin ${currentPath.includes('manage-master-data') ? 'active' : ''}"><i class="bi bi-database-gear"></i> Dữ liệu gốc</a></li>

                <li class="nav-section-title">QUẢN LÝ NỘI DUNG</li>
                <li class="nav-item"><a href="/manage-movies.html" class="nav-link-admin ${currentPath.includes('manage-movies') ? 'active' : ''}"><i class="bi bi-film"></i> Kho Phim hệ thống</a></li>
                <li class="nav-item"><a href="/manage-pricing.html" class="nav-link-admin ${currentPath.includes('manage-pricing') ? 'active' : ''}"><i class="bi bi-currency-dollar"></i> Cấu hình giá</a></li>
                <li class="nav-item"><a href="/manage-promotions.html" class="nav-link-admin ${currentPath.includes('manage-promotions') ? 'active' : ''}"><i class="bi bi-tags"></i> Khuyến mãi</a></li>
                <li class="nav-item"><a href="/manage-membership.html" class="nav-link-admin ${currentPath.includes('manage-membership') ? 'active' : ''}"><i class="bi bi-card-checklist"></i> Hạng thành viên</a></li>
                <li class="nav-item"><a href="/manage-combos.html" class="nav-link-admin ${currentPath.includes('manage-combos') ? 'active' : ''}"><i class="bi bi-bag-check"></i> Combo & Bắp nước</a></li>
                <li class="nav-item"><a href="/manage-audit.html" class="nav-link-admin ${currentPath.includes('manage-audit') ? 'active' : ''}"><i class="bi bi-shield-check"></i> Nhật ký hệ thống</a></li>
            `;
        } else if (role === 'MANAGER') {
            menuHtml += `
                <li class="nav-section-title">VẬN HÀNH RẠP</li>
                <li class="nav-item"><a href="/dashboard.html" class="nav-link-admin ${currentPath.includes('dashboard') ? 'active' : ''}"><i class="bi bi-speedometer2"></i> Dashboard Rạp</a></li>
                <li class="nav-item"><a href="/manage-showtimes.html" class="nav-link-admin ${currentPath.includes('manage-showtimes') ? 'active' : ''}"><i class="bi bi-calendar-event"></i> Lịch chiếu</a></li>
                <li class="nav-item"><a href="/manage-rooms.html" class="nav-link-admin ${currentPath.includes('manage-rooms') ? 'active' : ''}"><i class="bi bi-door-open"></i> Phòng chiếu</a></li>
                <li class="nav-item"><a href="/manage-movies.html" class="nav-link-admin ${currentPath.includes('manage-movies') ? 'active' : ''}"><i class="bi bi-collection-play"></i> Danh sách phim</a></li>

                <li class="nav-section-title">KINH DOANH</li>
                <li class="nav-item"><a href="/manage-pricing.html" class="nav-link-admin ${currentPath.includes('manage-pricing') ? 'active' : ''}"><i class="bi bi-currency-dollar"></i> Cấu hình giá</a></li>
                <li class="nav-item"><a href="/manage-promotions.html" class="nav-link-admin ${currentPath.includes('manage-promotions') ? 'active' : ''}"><i class="bi bi-tags"></i> Khuyến mãi</a></li>
                <li class="nav-item"><a href="/manage-membership.html" class="nav-link-admin ${currentPath.includes('manage-membership') ? 'active' : ''}"><i class="bi bi-card-checklist"></i> Thành viên</a></li>

                <li class="nav-section-title">BÁO CÁO THỐNG KÊ</li>
                <li class="nav-item"><a href="/dashboard/tickets-report.html" class="nav-link-admin ${currentPath.includes('tickets-report') ? 'active' : ''}"><i class="bi bi-ticket-perforated"></i> Báo cáo Vé</a></li>
                <li class="nav-item"><a href="/dashboard/fnb-report.html" class="nav-link-admin ${currentPath.includes('fnb-report') ? 'active' : ''}"><i class="bi bi-cup-hot"></i> Báo cáo F&B</a></li>
            `;
        } else if (role === 'STAFF') {
            menuHtml += `
                <li class="nav-section-title">NHÂN VIÊN</li>
                <li class="nav-item"><a href="/pos.html" class="nav-link-admin"><i class="bi bi-cart3"></i> Bán vé (POS)</a></li>
                <li class="nav-item"><a href="/manage-bookings.html" class="nav-link-admin"><i class="bi bi-ticket-detailed"></i> Tra cứu vé</a></li>
            `;
        }

        menuHtml += `
            </ul>
            <div class="sidebar-footer">
                <a href="#" onclick="AuthGuard.logout()" class="logout-btn">
                    <i class="bi bi-box-arrow-left"></i> <span>Đăng xuất</span>
                </a>
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
