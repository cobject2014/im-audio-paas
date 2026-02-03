import React, { useEffect, useState } from 'react';
import { 
    Box, Button, Container, Paper, Table, TableBody, TableCell, 
    TableContainer, TableHead, TableRow, Typography, IconButton, 
    Dialog, DialogTitle, DialogContent, DialogActions, TextField,
    Select, MenuItem, FormControl, InputLabel, Alert
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import { 
    getProviders, createProvider, updateProvider, deleteProvider, 
    ProviderConfig 
} from '../api/adminClient';

const AdminPage = () => {
    const [providers, setProviders] = useState<ProviderConfig[]>([]);
    const [open, setOpen] = useState(false);
    const [currentProvider, setCurrentProvider] = useState<Partial<ProviderConfig>>({});
    const [error, setError] = useState('');

    useEffect(() => {
        loadProviders();
    }, []);

    const loadProviders = async () => {
        try {
            const data = await getProviders();
            setProviders(data);
        } catch (err) {
            console.error(err);
            setError('Failed to load providers. Please check authentication.');
        }
    };

    const handleOpen = (provider?: ProviderConfig) => {
        if (provider) {
            setCurrentProvider(provider);
        } else {
            setCurrentProvider({ 
                name: '', 
                providerType: 'ALIYUN', 
                isActive: true, // Default active?
                baseUrl: '',
                accessKey: '', 
                secretKey: '' 
            });
        }
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
        setCurrentProvider({});
    };

    const handleSave = async () => {
        try {
            if (currentProvider.id) {
                await updateProvider(currentProvider.id, currentProvider);
            } else {
                await createProvider(currentProvider as Omit<ProviderConfig, 'id'>);
            }
            handleClose();
            loadProviders();
        } catch (err) {
            console.error(err);
            setError('Failed to save provider.');
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('Are you sure you want to delete this provider?')) {
            try {
                await deleteProvider(id);
                loadProviders();
            } catch (err) {
                console.error(err);
                setError('Failed to delete provider.');
            }
        }
    };

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h4">Provider Management</Typography>
                <Button variant="contained" onClick={() => handleOpen()}>
                    Add Provider
                </Button>
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Base URL</TableCell>
                            <TableCell>Access Key</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {providers.map((provider) => (
                            <TableRow key={provider.id}>
                                <TableCell>{provider.name}</TableCell>
                                <TableCell>{provider.providerType}</TableCell>
                                <TableCell>{provider.baseUrl || '-'}</TableCell>
                                <TableCell>{provider.accessKey}</TableCell>
                                <TableCell>{provider.isActive ? 'Active' : 'Inactive'}</TableCell>
                                <TableCell align="right">
                                    <IconButton onClick={() => handleOpen(provider)} color="primary">
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton onClick={() => handleDelete(provider.id)} color="error">
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                        {providers.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={6} align="center">No providers found</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            <Dialog open={open} onClose={handleClose}>
                <DialogTitle>{currentProvider.id ? 'Edit Provider' : 'Add Provider'}</DialogTitle>
                <DialogContent>
                    <Box component="form" sx={{ mt: 1, display: 'flex', flexDirection: 'column', gap: 2, minWidth: 400 }}>
                        <TextField
                            label="Name"
                            fullWidth
                            value={currentProvider.name || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, name: e.target.value })}
                        />
                        <FormControl fullWidth>
                            <InputLabel>Type</InputLabel>
                            <Select
                                value={currentProvider.providerType || 'ALIYUN'}
                                label="Type"
                                onChange={(e) => setCurrentProvider({ ...currentProvider, providerType: e.target.value as any })}
                            >
                                <MenuItem value="ALIYUN">Aliyun</MenuItem>
                                <MenuItem value="AWS">AWS</MenuItem>
                                <MenuItem value="TENCENT">Tencent</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField
                            label="Base URL (Optional)"
                            fullWidth
                            value={currentProvider.baseUrl || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, baseUrl: e.target.value })}
                        />
                        <TextField
                            label="Access Key"
                            fullWidth
                            value={currentProvider.accessKey || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, accessKey: e.target.value })}
                        />
                        <TextField
                            label="Secret Key"
                            fullWidth
                            type="password"
                            value={currentProvider.secretKey || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, secretKey: e.target.value })}
                            helperText={currentProvider.id ? "Leave blank to keep unchanged" : "Required for new providers"}
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleSave} variant="contained">Save</Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default AdminPage;
