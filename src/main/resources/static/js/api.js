const API_BASE_URL = '/api';

const api = {
    getHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };
        const token = localStorage.getItem('cinemaToken');
        if (token && token !== 'null' && token !== 'undefined') {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    },

    async handleResponse(response) {
        try {
            const data = await response.json();
            if (!response.ok) {
                // Nếu server trả về lỗi nghiệp vụ (400, 401...) với format ApiResponse
                return {
                    success: false,
                    message: data.message || `Lỗi hệ thống (${response.status})`,
                    status: response.status
                };
            }
            return data;
        } catch (e) {
            // Trường hợp response không phải JSON
            if (!response.ok) {
                return { success: false, message: `Lỗi kết nối server (${response.status})` };
            }
            return { success: true, data: null };
        }
    },

    async get(endpoint) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                headers: this.getHeaders()
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async post(endpoint, data) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async postMultipart(endpoint, formData) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('cinemaToken')}`
                },
                body: formData
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async put(endpoint, data) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'PUT',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async putMultipart(endpoint, formData) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('cinemaToken')}`
                },
                body: formData
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async delete(endpoint) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'DELETE',
                headers: this.getHeaders()
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    async patch(endpoint, data) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                method: 'PATCH',
                headers: this.getHeaders(),
                body: data ? JSON.stringify(data) : null
            });
            return await this.handleResponse(response);
        } catch (e) {
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    }
};
