import React, { useEffect, useRef } from 'react';
import { Box, Typography } from '@mui/material';

interface AudioPlayerProps {
    audioBlob: Blob | null;
}

const AudioPlayer: React.FC<AudioPlayerProps> = ({ audioBlob }) => {
    const audioRef = useRef<HTMLAudioElement>(null);
    const [audioUrl, setAudioUrl] = React.useState<string | null>(null);

    useEffect(() => {
        if (audioBlob) {
            const url = URL.createObjectURL(audioBlob);
            setAudioUrl(url);
            
            // Auto play
            if (audioRef.current) {
                audioRef.current.load();
                audioRef.current.play().catch(e => console.log("Auto-play prevented", e));
            }

            return () => {
                URL.revokeObjectURL(url);
            };
        } else {
            setAudioUrl(null);
        }
    }, [audioBlob]);

    if (!audioBlob) {
        return null;
    }

    return (
        <Box sx={{ mt: 2, p: 2, border: '1px solid #ccc', borderRadius: 1 }}>
            <Typography variant="subtitle1" gutterBottom>
                Generated Audio:
            </Typography>
            <audio ref={audioRef} controls src={audioUrl || undefined} style={{ width: '100%' }}>
                Your browser does not support the audio element.
            </audio>
        </Box>
    );
};

export default AudioPlayer;
