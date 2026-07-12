/**
 * UI Utilities - Các hàm dùng chung cho giao diện StarCinema
 */
const UIUtils = {
    /**
     * Định dạng tiền tệ VND
     */
    formatVND(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    },

    /**
     * Định dạng ngày tháng năm
     */
    formatDateTime(dateStr) {
        if (!dateStr) return 'N/A';
        const date = new Date(dateStr);
        return date.toLocaleString('vi-VN');
    },

    /**
     * Hiển thị thông báo Toast (Sử dụng Bootstrap Toast)
     */
    showToast(message, type = 'success') {
        // Tự động tạo container nếu chưa có
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }

        const toastId = 'toast-' + Date.now();
        const icon = type === 'success' ? 'check-circle' : 'exclamation-triangle';
        const color = type === 'success' ? 'success' : 'danger';

        const toastHtml = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${color} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="bi bi-${icon} me-2"></i> ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', toastHtml);
        const toastEl = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
        toast.show();
        toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
    },

    /**
     * Ẩn/Hiện phần tử dựa trên Role
     * Dùng: <button data-role="ADMIN,MANAGER">Xóa</button>
     */
    applyRoleVisibility() {
        const currentRole = localStorage.getItem('cinemaRole');
        document.querySelectorAll('[data-role]').forEach(el => {
            const allowedRoles = el.getAttribute('data-role').split(',');
            if (allowedRoles.includes(currentRole)) {
                // Hiển thị lại nếu khớp quyền (xóa inline style display:none)
                if (el.style.display === 'none') {
                    el.style.display = ''; 
                }
            } else {
                el.style.display = 'none';
            }
        });
    }
};

// Tự động áp dụng phân quyền cho các phần tử UI khi trang tải xong
document.addEventListener('DOMContentLoaded', () => UIUtils.applyRoleVisibility());
