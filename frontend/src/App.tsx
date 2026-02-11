import { BrowserRouter, Routes, Route, Navigate, Link as RouterLink } from 'react-router-dom'
import { Container, AppBar, Toolbar, Typography, Box, Button } from '@mui/material'
import LoginPage from './pages/LoginPage'
import AdminPage from './pages/AdminPage'
import StatisticsPage from './pages/StatisticsPage'
import DemoPage from './pages/DemoPage'

const Layout = ({ children }: { children: React.ReactNode }) => {
  const handleLogout = () => {
    localStorage.removeItem('adminAuth');
    window.location.href = '/login';
  };

  return (
    <>
      <Box sx={{ flexGrow: 1 }}>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              TTS Gateway Console
            </Typography>
            <Button color="inherit" component={RouterLink} to="/admin">Config</Button>
            <Button color="inherit" component={RouterLink} to="/admin/statistics">Statistics</Button>
            <Button color="inherit" component={RouterLink} to="/demo">Demo</Button>
            <Button color="inherit" onClick={handleLogout} sx={{ ml: 2 }}>Logout</Button>
          </Toolbar>
        </AppBar>
      </Box>
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        {children}
      </Container>
    </>
  );
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/admin/statistics" element={<Layout><StatisticsPage /></Layout>} />
        <Route path="/admin" element={<Layout><AdminPage /></Layout>} />
        <Route path="/demo" element={<Layout><DemoPage /></Layout>} />
        <Route path="/" element={<Navigate to="/admin" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

