import axios from 'axios';

export interface ProviderConfig {
    id: string;
    name: string;
    providerType: 'ALIYUN' | 'AWS' | 'TENCENT' | 'VIBEVOICE' | 'QWEN';
    baseUrl?: string;
    accessKey?: string;
    secretKey?: string;
    isActive?: boolean;
    metadata?: string; // JSON string
}

const adminClient = axios.create({
    baseURL: '/api/admin', // Assuming we proxy /api to backend, or use CORS. For now let's assume /admin or localhost:8080
});

// Configure baseURL to point to backend. 
// Ideally via env var. For dev MVP:
adminClient.defaults.baseURL = 'http://localhost:8080/admin';

// Initialize token from localStorage if available
const storedToken = localStorage.getItem('adminAuth');
if (storedToken) {
    adminClient.defaults.headers.common['Authorization'] = `Basic ${storedToken}`;
}

// Interceptor to handle 401 Unauthorized globally
adminClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Clear invalid token
            localStorage.removeItem('adminAuth');
            // Redirect to login
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export const setAuthToken = (token: string) => {
    // For Basic Auth, token is base64(user:pass)
    if (token) {
        adminClient.defaults.headers.common['Authorization'] = `Basic ${token}`;
    } else {
        delete adminClient.defaults.headers.common['Authorization'];
    }
};

export const getProviders = async () => {
    const response = await adminClient.get<ProviderConfig[]>('/providers');
    return response.data;
};

export const getProvider = async (id: string) => {
    const response = await adminClient.get<ProviderConfig>(`/providers/${id}`);
    return response.data;
};

export const createProvider = async (provider: Omit<ProviderConfig, 'id'>) => {
    const response = await adminClient.post<ProviderConfig>('/providers', provider);
    return response.data;
};

export const updateProvider = async (id: string, provider: Partial<ProviderConfig>) => {
    const response = await adminClient.put<ProviderConfig>(`/providers/${id}`, provider);
    return response.data;
};

export const deleteProvider = async (id: string) => {
    await adminClient.delete(`/providers/${id}`);
};

export interface StatisticsDto {
    providerName: string;
    totalRequests: number;
    successCount: number;
    failureCount: number;
    successRate: number;
    avgLatencyMs: number;
}

export const getStatistics = async () => {
    const response = await adminClient.get<StatisticsDto[]>('/statistics');
    return response.data;
};
