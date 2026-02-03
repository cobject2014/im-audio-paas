import axios from 'axios';

export interface ProviderConfig {
    id: string;
    name: string;
    providerType: 'ALIYUN' | 'AWS' | 'TENCENT';
    baseUrl?: string;
    accessKey?: string;
    secretKey?: string;
    isActive?: boolean;
}

const adminClient = axios.create({
    baseURL: '/api/admin', // Assuming we proxy /api to backend, or use CORS. For now let's assume /admin or localhost:8080
});

// Configure baseURL to point to backend. 
// Ideally via env var. For dev MVP:
adminClient.defaults.baseURL = 'http://localhost:8080/admin';

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
