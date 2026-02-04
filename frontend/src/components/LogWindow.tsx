import React, { useEffect, useRef, useState } from 'react';
import { Box, Typography, Paper, Collapse, IconButton } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

export interface LogEntry {
  timestamp: string;
  method: string;
  url: string;
  status?: number;
  duration?: number;
  error?: boolean;
  response?: any;
}

interface LogWindowProps {
  logs: LogEntry[];
}

const LogRow: React.FC<{ log: LogEntry }> = ({ log }) => {
    const [open, setOpen] = useState(false);

    return (
        <React.Fragment>
            <Box sx={{ mb: 1, display: 'flex', gap: 2, alignItems: 'center' }}>
                <IconButton size="small" onClick={() => setOpen(!open)} sx={{ p: 0, color: '#888' }}>
                    {open ? <KeyboardArrowUpIcon fontSize="small" /> : <KeyboardArrowDownIcon fontSize="small" />}
                </IconButton>
                <span style={{ color: '#888' }}>[{log.timestamp}]</span>
                <span style={{ color: '#569cd6', fontWeight: 'bold' }}>{log.method}</span>
                <span style={{ flex: 1, wordBreak: 'break-all' }}>{log.url}</span>
                {log.status && (
                    <span style={{ 
                        color: log.error || log.status >= 400 ? '#f48771' : '#4ec9b0', 
                        fontWeight: 'bold' 
                    }}>
                        {log.status}
                    </span>
                )}
                {log.duration && (
                    <span style={{ color: '#b5cea8' }}>{log.duration}ms</span>
                )}
            </Box>
            <Collapse in={open} timeout="auto" unmountOnExit>
                <Box sx={{ ml: 6, mb: 1, p: 1, bgcolor: '#00000030', borderRadius: 1, overflowX: 'auto' }}>
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-all', color: '#ce9178' }}>
                        {typeof log.response === 'string' 
                            ? log.response 
                            : JSON.stringify(log.response, null, 2)}
                    </pre>
                </Box>
            </Collapse>
        </React.Fragment>
    );
};

const LogWindow: React.FC<LogWindowProps> = ({ logs }) => {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [logs]);

  if (logs.length === 0) return null;

  return (
    <Paper sx={{ mt: 4, p: 0, bgcolor: '#1e1e1e', color: '#d4d4d4', overflow: 'hidden', borderRadius: 2 }}>
      <Box sx={{ p: 1, borderBottom: '1px solid #333', bgcolor: '#252526' }}>
        <Typography variant="subtitle2" sx={{ fontFamily: 'monospace' }}>
          Network Logs
        </Typography>
      </Box>
      <Box sx={{ p: 2, height: 200, overflowY: 'auto', fontFamily: 'monospace', fontSize: '0.85rem' }}>
        {logs.map((log, index) => (
            <LogRow key={index} log={log} />
        ))}
        <div ref={bottomRef} />
      </Box>
    </Paper>
  );
};

export default LogWindow;
