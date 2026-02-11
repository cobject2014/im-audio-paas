import React, { useEffect, useState } from 'react';
import { 
    Box, Button, Container, Paper, Table, TableBody, TableCell, 
    TableContainer, TableHead, TableRow, Typography, Alert, CircularProgress 
} from '@mui/material';
import { getStatistics, StatisticsDto } from '../api/adminClient';

const StatisticsPage: React.FC = () => {
    const [stats, setStats] = useState<StatisticsDto[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadStatistics();
    }, []);

    const loadStatistics = async () => {
        try {
            setLoading(true);
            const data = await getStatistics();
            setStats(data);
            setError(null);
        } catch (err) {
            setError('Failed to load statistics');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
             <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h4" component="h1">
                    Provider Statistics
                </Typography>
                <Button variant="contained" onClick={loadStatistics} disabled={loading}>
                    Refresh
                </Button>
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            
            {loading ? (
                <Box display="flex" justifyContent="center">
                    <CircularProgress />
                </Box>
            ) : (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Provider Name</TableCell>
                                <TableCell align="right">Total Requests</TableCell>
                                <TableCell align="right">Success Count</TableCell>
                                <TableCell align="right">Failure Count</TableCell>
                                <TableCell align="right">Success Rate</TableCell>
                                <TableCell align="right">Avg Latency (ms)</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {stats.map((stat) => (
                                <TableRow key={stat.providerName}>
                                    <TableCell component="th" scope="row">
                                        {stat.providerName}
                                    </TableCell>
                                    <TableCell align="right">{stat.totalRequests}</TableCell>
                                    <TableCell align="right">{stat.successCount}</TableCell>
                                    <TableCell align="right">{stat.failureCount}</TableCell>
                                    <TableCell align="right">
                                        {(stat.successRate * 100).toFixed(2)}%
                                    </TableCell>
                                    <TableCell align="right">
                                        {stat.avgLatencyMs ? stat.avgLatencyMs.toFixed(2) : '0.00'}
                                    </TableCell>
                                </TableRow>
                            ))}
                            {stats.length === 0 && (
                                <TableRow>
                                    <TableCell colSpan={6} align="center">
                                        No statistics available
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}
        </Container>
    );
};

export default StatisticsPage;
