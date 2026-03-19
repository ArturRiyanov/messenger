import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { WebSocketProvider } from './context/WebSocketContext';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import ChatLayout from './components/chat/ChatLayout';

const PrivateRoute = ({ children }) => {
    const { isAuthenticated } = useAuth();
    return isAuthenticated ? children : <Navigate to="/login" />;
};

function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/chats/*" element={<PrivateRoute><ChatLayout /></PrivateRoute>} />
            <Route path="/" element={<Navigate to="/chats" />} />
        </Routes>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <WebSocketProvider>
                    <AppRoutes />
                </WebSocketProvider>
            </AuthProvider>
        </BrowserRouter>
    );
}