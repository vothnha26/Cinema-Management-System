/**
 * Auth Guard - Bảo vệ các trang dựa trên Role.
 * Tự động kiểm tra quyền khi trang tải lên.
 */
const AuthGuard = {
    // Cấu hình quyền truy cập dựa trên tên file (URL)
    // Nếu không có trong danh sách này -> Cần Login
    // Nếu có -> Chỉ những Role được liệt kê mới được vào
    permissions: {
        'dashboard.html': ['ADMIN', 'MANAGER'],
        'manage-branches.html': ['ADMIN'],
        'manage-users.html': ['ADMIN'],
        'manage-master-data.html': ['ADMIN'],
        'manage-movies.html': ['ADMIN', 'MANAGER'],
        'manage-pricing.html': ['ADMIN', 'MANAGER'],
        'manage-promotions.html': ['ADMIN', 'MANAGER'],
        'manage-membership.html': ['ADMIN', 'MANAGER'],
        'manage-combos.html': ['ADMIN', 'MANAGER'],
        'manage-audit.html': ['ADMIN'],
        'manage-showtimes.html': ['ADMIN', 'MANAGER'],
        'manage-rooms.html': ['ADMIN', 'MANAGER'],
        'manage-bookings.html': ['ADMIN', 'MANAGER', 'STAFF'],
        'pos.html': ['ADMIN', 'MANAGER', 'STAFF'],
        'history.html': ['ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER'],
        'profile.html': ['ADMIN', 'MANAGER', 'STAFF', 'CUSTOMER'],
    },

    getToken() { return localStorage.getItem('cinemaToken'); },
    getRole() { return localStorage.getItem('cinemaRole'); },
    getUsername() { return localStorage.getItem('cinemaUsername'); },
    isLoggedIn() { return !!this.getToken(); },

    /**
     * Tự động kiểm tra quyền dựa trên URL hiện tại
     */
    init() {
        const path = window.location.pathname.split('/').pop();
        if (!path || path === 'index.html' || path === 'auth.html' || path === 'reset-password.html' || path === 'logout.html') {
            return; // Trang công khai
        }

        if (!this.isLoggedIn()) {
            window.location.href = '/auth.html';
            return;
        }

        const allowedRoles = this.permissions[path];
        if (allowedRoles) {
            const currentRole = this.getRole();
            if (!allowedRoles.includes(currentRole)) {
                alert('Bạn không có quyền truy cập trang này.');
                window.location.href = '/index.html';
            }
        }
    },

    logout() {
        window.location.href = '/logout.html';
    }
};

// Tự động kích hoạt khi tải trang
AuthGuard.init();
