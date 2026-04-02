/**
 * Auth Guard - Bảo vệ các trang cần đăng nhập.
 * Include file này ở đầu các trang admin/staff/manager.
 *
 * Cách dùng:
 * <script src="/js/auth-guard.js"></script>
 * <script>
 *   AuthGuard.requireRole(['ADMIN']);           // Chỉ cho ADMIN vào
 *   AuthGuard.requireRole(['STAFF', 'ADMIN']);  // STAFF hoặc ADMIN
 *   AuthGuard.requireLogin();                   // Chỉ cần đăng nhập
 * </script>
 */
const AuthGuard = {
    getToken() {
        return localStorage.getItem('token');
    },

    getRole() {
        return localStorage.getItem('role');
    },

    getUsername() {
        return localStorage.getItem('username');
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    /**
     * Yêu cầu đăng nhập. Nếu chưa login -> redirect về auth.html.
     */
    requireLogin() {
        if (!this.isLoggedIn()) {
            window.location.href = '/auth.html';
            return false;
        }
        return true;
    },

    /**
     * Yêu cầu role cụ thể. Nếu không đúng role -> redirect về trang chủ.
     * @param {string[]} allowedRoles - Danh sách role được phép, vd: ['ADMIN', 'STAFF']
     */
    requireRole(allowedRoles) {
        if (!this.requireLogin()) return false;

        const currentRole = this.getRole();
        if (!allowedRoles.includes(currentRole)) {
            alert('Bạn không có quyền truy cập trang này.');
            window.location.href = '/index.html';
            return false;
        }
        return true;
    },

    /**
     * Đăng xuất: xóa token và redirect về trang đăng nhập.
     */
    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('username');
        window.location.href = '/auth.html';
    }
};
