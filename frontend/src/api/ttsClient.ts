import axios from 'axios';

export interface SpeechRequest {
    model?: string;
    input: string;
    voice: string;
    speed?: number;
    response_format?: string;
    extra_body?: Record<string, any>;
}

const ttsClient = axios.create({
    baseURL: 'http://localhost:8080/v1/audio',
    responseType: 'blob', // Important for audio
});

// Request interceptor to add auth token
ttsClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('adminAuth');
    if (token) {
        config.headers.Authorization = `Basic ${token}`;
    }
    return config;
});

export const generateSpeech = async (request: SpeechRequest): Promise<Blob> => {
    // Default model if not provided, though backend might require it or not.
    // Spec says 'model' is required in CreateSpeechRequest schema.
    const payload = {
        model: request.model || 'default', 
        ...request,
    };
    
    const response = await ttsClient.post('/speech', payload);
    return response.data;
};
