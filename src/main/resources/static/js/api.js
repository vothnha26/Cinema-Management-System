const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    async get(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        return await response.json();
    },

    async post(endpoint, data, isMultipart = false) {
        const headers = {};
        if (!isMultipart) {
            headers['Content-Type'] = 'application/json';
        }
        
        const token = localStorage.getItem('token');
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: headers,
            body: isMultipart ? data : JSON.stringify(data)
        });
        return await response.json();
    },

    async put(endpoint, data, isMultipart = false) {
        const headers = {};
        if (!isMultipart) {
            headers['Content-Type'] = 'application/json';
        }
        
        const token = localStorage.getItem('token');
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: headers,
            body: isMultipart ? data : JSON.stringify(data)
        });
        return await response.json();
    },

    async delete(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        return await response.json();
    },

    async patch(endpoint, data) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    }
};
