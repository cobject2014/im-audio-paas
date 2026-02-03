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
import AudioPlayer from '../components/AudioPlayer';

const DemoPage = () => {
    const [text, setText] = useState('Hello, this is a test of the TTS Gateway system.');
    const [voiceId, setVoiceId] = useState('aliyun');
    const [model, setModel] = useState('default');
    const [extraBodyStr, setExtraBodyStr] = useState('{\n  "emotion": "happy"\n}');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [audioBlob, setAudioBlob] = useState<Blob | null>(null);

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
            setLoading(false);
            return;
        }

        try {
            const blob = await generateSpeech({
                input: text,
                voice: voiceId,
                model: model,
                extra_body: extraBody
            });
            setAudioBlob(blob);
        } catch (err: any) {
            console.error('TTS Failed', err);
            setError('Generation failed: ' + (err.response?.statusText || err.message));
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
                            <AudioPlayer audioBlob={audioBlob} />
                        </Grid>
                    )}
                </Grid>
            </Paper>
        </Container>
    );
};

export default DemoPage;
