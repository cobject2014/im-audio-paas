import React, { useState } from 'react';
import { 
    Box, 
    Button, 
    Container, 
    TextField, 
    Typography, 
    Paper, 
    Grid,
    CircularProgress,
    Alert
} from '@mui/material';
import { generateSpeech } from '../api/ttsClient';
import WaveformPlayer from '../components/WaveformPlayer'; // Updated import
import LogWindow, { LogEntry } from '../components/LogWindow'; // Added import

const DemoPage = () => {
    const [text, setText] = useState('Hello, this is a test of the TTS Gateway system.');
    const [voiceId, setVoiceId] = useState('aliyun');
    const [model, setModel] = useState('default');
    const [extraBodyStr, setExtraBodyStr] = useState('{\n  "emotion": "happy"\n}');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [audioBlob, setAudioBlob] = useState<Blob | null>(null);
    const [logs, setLogs] = useState<LogEntry[]>([]); // Log state

    const addLog = (method: string, url: string, status?: number, duration?: number, error?: boolean, response?: any) => {
        const timestamp = new Date().toLocaleTimeString();
        setLogs(prev => [...prev, { timestamp, method, url, status, duration, error, response }]);
    };

    const handleGenerate = async () => {
        setLoading(true);
        setError(null);
        setAudioBlob(null);

        let extraBody = {};
        try {
            if (extraBodyStr.trim()) {
                extraBody = JSON.parse(extraBodyStr);
            }
        } catch (e) {
            setError('Invalid JSON in Extra Body');
            return;
        }

        const startTime = Date.now();
        const url = '/v1/audio/speech';
        addLog('POST', url + ' (Started)', undefined, undefined, false, { 
            input: text, 
            voice: voiceId, 
            model: model, 
            extraParams: extraBody 
        });

        try {
            const blob = await generateSpeech({
                input: text,
                voice: voiceId,
                model: model,
                extra_body: extraBody
            });
            const duration = Date.now() - startTime;
            addLog('POST', url, 200, duration, false, `Success. Received Audio Blob (${blob.size} bytes, type: ${blob.type})`);
            setAudioBlob(blob);
        } catch (err: any) {
            const duration = Date.now() - startTime;
            console.error('TTS Failed', err);
            let errMsg = err.message || 'Unknown Error';
            let status = err.response?.status || 500;
            let detail = null;
            
            // Handle Blob error response
            if (err.response && err.response.data instanceof Blob) {
                 try {
                     const text = await err.response.data.text();
                     const json = JSON.parse(text);
                     if (json.message) errMsg = json.message;
                     detail = json;
                 } catch (parseError) {
                     // Not JSON, stick to statusText or message
                     if (err.response.statusText) errMsg = err.response.statusText;
                     try {
                        detail = await err.response.data.text(); 
                     } catch (e) {}
                 }
            } else if (err.response?.data?.message) {
                errMsg = err.response.data.message;
                detail = err.response.data;
            } else if (err.response?.statusText) {
                errMsg = err.response.statusText;
            }

            addLog('POST', url, status, duration, true, detail || errMsg);
            setError('Generation failed: ' + errMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom>
                    TTS Interactive Demo
                </Typography>
                
                <Grid container spacing={3}>
                    <Grid item xs={12}>
                        <TextField
                            label="Input Text"
                            multiline
                            rows={3}
                            fullWidth
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                        />
                    </Grid>
                    
                    <Grid item xs={12} sm={6}>
                        <TextField
                            label="Voice ID"
                            fullWidth
                            value={voiceId}
                            helperText="e.g., aliyun, xiaoyun, Joanna, qwen-voice-1"
                            onChange={(e) => setVoiceId(e.target.value)}
                        />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <TextField
                            label="Model ID"
                            fullWidth
                            value={model}
                            helperText="provider/model-name (optional)"
                            onChange={(e) => setModel(e.target.value)}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <TextField
                            label="Extra Params (JSON)"
                            multiline
                            rows={3}
                            fullWidth
                            value={extraBodyStr}
                            onChange={(e) => setExtraBodyStr(e.target.value)}
                            InputProps={{
                                sx: { fontFamily: 'monospace' }
                            }}
                        />
                    </Grid>

                    <Grid item xs={12}>
                        <Button 
                            variant="contained" 
                            size="large" 
                            onClick={handleGenerate} 
                            disabled={loading || !text || !voiceId}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Synthesize Speech'}
                        </Button>
                    </Grid>

                    {error && (
                        <Grid item xs={12}>
                            <Alert severity="error">{error}</Alert>
                        </Grid>
                    )}

                    {audioBlob && (
                        <Grid item xs={12}>
                            <WaveformPlayer audioBlob={audioBlob} />
                        </Grid>
                    )}
                </Grid>
            </Paper>

            <LogWindow logs={logs} />
        </Container>
    );
};

export default DemoPage;
