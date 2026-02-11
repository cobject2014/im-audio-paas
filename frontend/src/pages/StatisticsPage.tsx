import React, { useEffect, useState, useMemo } from 'react';
import { 
    Box, Button, Container, Paper, Table, TableBody, TableCell, 
    TableContainer, TableHead, TableRow, Typography, Alert, CircularProgress,
    TableSortLabel
} from '@mui/material';
import { getStatistics, StatisticsDto } from '../api/adminClient';

type Order = 'asc' | 'desc';

const StatisticsPage: React.FC = () => {
    const [stats, setStats] = useState<StatisticsDto[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [order, setOrder] = useState<Order>('desc');
    const [orderBy, setOrderBy] = useState<keyof StatisticsDto>('totalRequests');

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

    const handleRequestSort = (property: keyof StatisticsDto) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const sortedStats = useMemo(() => {
        return [...stats].sort((a, b) => {
            const aValue = a[orderBy];
            const bValue = b[orderBy];

            if (bValue < aValue) {
                return order === 'asc' ? 1 : -1;
            }
            if (bValue > aValue) {
                return order === 'asc' ? -1 : 1;
            }
            return 0;
        });
    }, [stats, order, orderBy]);

    const headCells: { id: keyof StatisticsDto; label: string; numeric: boolean }[] = [
        { id: 'providerName', label: 'Provider Name', numeric: false },
        { id: 'totalRequests', label: 'Total Requests', numeric: true },
        { id: 'successCount', label: 'Success Count', numeric: true },
        { id: 'failureCount', label: 'Failure Count', numeric: true },
        { id: 'successRate', label: 'Success Rate', numeric: true },
        { id: 'avgLatencyMs', label: 'Avg Latency (ms)', numeric: true },
    ];

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
                                {headCells.map((headCell) => (
                                    <TableCell
                                        key={headCell.id}
                                        align={headCell.numeric ? 'right' : 'left'}
                                        sortDirection={orderBy === headCell.id ? order : false}
                                    >
                                        <TableSortLabel
                                            active={orderBy === headCell.id}
                                            direction={orderBy === headCell.id ? order : 'asc'}
                                            onClick={() => handleRequestSort(headCell.id)}
                                        >
                                            {headCell.label}
                                        </TableSortLabel>
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {sortedStats.map((stat) => (
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
