import React, { useEffect, useRef, useState } from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import PauseIcon from '@mui/icons-material/Pause';
import WaveSurfer from 'wavesurfer.js';

interface WaveformPlayerProps {
    audioBlob: Blob | null;
}

const WaveformPlayer: React.FC<WaveformPlayerProps> = ({ audioBlob }) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const wavesurfer = useRef<WaveSurfer | null>(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [currentTime, setCurrentTime] = useState('00:00');
    const [totalTime, setTotalTime] = useState('00:00');

    // Helper to format time
    const formatTime = (seconds: number) => {
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins < 10 ? '0' : ''}${mins}:${secs < 10 ? '0' : ''}${secs}`;
    };

    useEffect(() => {
        if (!containerRef.current || !audioBlob) return;

        // Initialize WaveSurfer
        wavesurfer.current = WaveSurfer.create({
            container: containerRef.current,
            waveColor: '#4a90e2',
            progressColor: '#1976d2',
            cursorColor: '#1976d2',
            barWidth: 2,
            barGap: 3,
            height: 80,
            normalize: true,
        });

        // Load audio
        const url = URL.createObjectURL(audioBlob);
        wavesurfer.current.load(url);

        // Events
        wavesurfer.current.on('ready', () => {
            setTotalTime(formatTime(wavesurfer.current?.getDuration() || 0));
            wavesurfer.current?.play();
            setIsPlaying(true);
        });

        wavesurfer.current.on('audioprocess', () => {
            setCurrentTime(formatTime(wavesurfer.current?.getCurrentTime() || 0));
        });

        wavesurfer.current.on('finish', () => {
            setIsPlaying(false);
        });

        wavesurfer.current.on('play', () => setIsPlaying(true));
        wavesurfer.current.on('pause', () => setIsPlaying(false));

        // Cleanup
        return () => {
             if (wavesurfer.current) {
                 wavesurfer.current.destroy();
             }
             URL.revokeObjectURL(url);
        };
    }, [audioBlob]);

    const handlePlayPause = () => {
        if (wavesurfer.current) {
            wavesurfer.current.playPause();
        }
    };

    if (!audioBlob) return null;

    return (
        <Box sx={{ mt: 3, p: 2, border: '1px solid #e0e0e0', borderRadius: 2, bgcolor: '#fafafa' }}>
            <Typography variant="subtitle1" gutterBottom fontWeight="bold">
                Generated Audio
            </Typography>
            
            <Box display="flex" alignItems="center" gap={2}>
                <IconButton onClick={handlePlayPause} color="primary" sx={{ border: '2px solid', p: 1 }}>
                    {isPlaying ? <PauseIcon fontSize="large" /> : <PlayArrowIcon fontSize="large" />}
                </IconButton>
                
                <Box flex={1} ref={containerRef} />
            </Box>
            
            <Box display="flex" justifyContent="space-between" mt={1} px={7}>
                <Typography variant="caption" color="text.secondary">{currentTime}</Typography>
                <Typography variant="caption" color="text.secondary">{totalTime}</Typography>
            </Box>
        </Box>
    );
};

export default WaveformPlayer;
