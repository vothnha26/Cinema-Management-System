const API_BASE_URL = '/api';

const api = {
    getHeaders() {
        const headers = { 'Content-Type': 'application/json' };
        const token = localStorage.getItem('cinemaToken');
        if (token && token !== 'null') headers['Authorization'] = `Bearer ${token}`;
        return headers;
    },

    async handleResponse(response) {
        try {
            const data = await response.json();
            if (!response.ok) return { success: false, message: data.message || `Lỗi ${response.status}`, status: response.status };
            return data;
        } catch (e) {
            return response.ok ? { success: true, data: null } : { success: false, message: "Lỗi kết nối máy chủ" };
        }
    },

    async request(endpoint, options = {}) {
        const path = endpoint.startsWith('/') ? endpoint : '/' + endpoint;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 30000); // 30s timeout

        try {
            const response = await fetch(`${API_BASE_URL}${path}`, {
                ...options,
                headers: options.headers || this.getHeaders(),
                signal: controller.signal
            });
            clearTimeout(timeoutId);
            return await this.handleResponse(response);
        } catch (err) {
            clearTimeout(timeoutId);
            if (err.name === 'AbortError') return { success: false, message: "Yêu cầu quá hạn" };
            return { success: false, message: "Không thể kết nối tới máy chủ" };
        }
    },

    get(endpoint) { return this.request(endpoint, { method: 'GET' }); },
    post(endpoint, data) { return this.request(endpoint, { method: 'POST', body: JSON.stringify(data) }); },
    put(endpoint, data) { return this.request(endpoint, { method: 'PUT', body: JSON.stringify(data) }); },
    delete(endpoint) { return this.request(endpoint, { method: 'DELETE' }); },
    patch(endpoint, data) { return this.request(endpoint, { method: 'PATCH', body: JSON.stringify(data) }); },
    
    postMultipart(endpoint, formData) {
        return this.request(endpoint, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('cinemaToken')}` },
            body: formData
        });
    },

    putMultipart(endpoint, formData) {
        return this.request(endpoint, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('cinemaToken')}` },
            body: formData
        });
    }
};
