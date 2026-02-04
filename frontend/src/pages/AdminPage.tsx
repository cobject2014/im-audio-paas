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

    // Metadata fields
    const [appKey, setAppKey] = useState('');
    const [region, setRegion] = useState('');

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
            // Parse metadata
            if (provider.metadata) {
                try {
                    const meta = JSON.parse(provider.metadata);
                    setAppKey(meta.appKey || '');
                    setRegion(meta.region || '');
                } catch (e) {
                    console.error("Failed to parse metadata", e);
                    setAppKey('');
                    setRegion('');
                }
            } else {
                setAppKey('');
                setRegion('');
            }
        } else {
            setCurrentProvider({ 
                name: '', 
                providerType: 'ALIYUN', 
                isActive: true, 
                baseUrl: '',
                accessKey: '', 
                secretKey: '' 
            });
            setAppKey('');
            setRegion('');
        }
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
        setCurrentProvider({});
        setAppKey('');
        setRegion('');
    };

    const handleSave = async () => {
        try {
            // Pack metadata
            const metadata: any = {};
            if (appKey) metadata.appKey = appKey;
            if (region) metadata.region = region;
            
            const providerToSave = {
                ...currentProvider,
                metadata: JSON.stringify(metadata)
            };

            if (currentProvider.id) {
                await updateProvider(currentProvider.id, providerToSave);
            } else {
                await createProvider(providerToSave as Omit<ProviderConfig, 'id'>);
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

    const getLabels = (type?: string) => {
        switch(type) {
            case 'ALIYUN': return { ak: 'Access Key ID', sk: 'Access Key Secret' };
            case 'TENCENT': return { ak: 'Secret ID', sk: 'Secret Key' };
            case 'VIBEVOICE': 
            case 'QWEN': return { ak: 'API Token', sk: 'Unused' };
            default: return { ak: 'Access Key', sk: 'Secret Key' };
        }
    }

    const labels = getLabels(currentProvider.providerType);
    const isCloud = ["ALIYUN", "AWS", "TENCENT"].includes(currentProvider.providerType || 'ALIYUN');
    const isLocal = ["VIBEVOICE", "QWEN"].includes(currentProvider.providerType || '');

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
                                <TableCell>{provider.accessKey ? '******' : '-'}</TableCell>
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
                                onChange={(e) => {
                                    setCurrentProvider({ ...currentProvider, providerType: e.target.value as any })
                                }}
                            >
                                <MenuItem value="ALIYUN">Aliyun</MenuItem>
                                <MenuItem value="AWS">AWS</MenuItem>
                                <MenuItem value="TENCENT">Tencent</MenuItem>
                                <MenuItem value="VIBEVOICE">VibeVoice</MenuItem>
                                <MenuItem value="QWEN">Qwen</MenuItem>
                            </Select>
                        </FormControl>
                        
                        <TextField
                            label={labels.ak}
                            fullWidth
                            value={currentProvider.accessKey || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, accessKey: e.target.value })}
                        />
                        
                        {isCloud && (
                            <TextField
                                label={labels.sk}
                                fullWidth
                                type="password"
                                value={currentProvider.secretKey || ''}
                                onChange={(e) => setCurrentProvider({ ...currentProvider, secretKey: e.target.value })}
                                helperText={currentProvider.id ? "Leave blank to keep unchanged" : "Required for new providers"}
                            />
                        )}

                        {currentProvider.providerType === 'ALIYUN' && (
                             <TextField
                                label="App Key"
                                fullWidth
                                value={appKey}
                                onChange={(e) => setAppKey(e.target.value)}
                                helperText="Required for Aliyun NLS"
                            />
                        )}

                        {(currentProvider.providerType === 'AWS' || currentProvider.providerType === 'TENCENT') && (
                            <TextField
                                label="Region"
                                fullWidth
                                value={region}
                                onChange={(e) => setRegion(e.target.value)}
                                placeholder={currentProvider.providerType === 'TENCENT' ? 'e.g. ap-shanghai' : 'e.g. us-east-1'}
                                helperText="Service Region"
                            />
                        )}

                        <TextField
                            label="Base URL (Optional)"
                            fullWidth
                            value={currentProvider.baseUrl || ''}
                            onChange={(e) => setCurrentProvider({ ...currentProvider, baseUrl: e.target.value })}
                            helperText={isLocal ? "Required for self-hosted providers" : "Optional for cloud providers"}
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
