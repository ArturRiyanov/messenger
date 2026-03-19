import { createContext, useState, useContext } from 'react';
import { login as apiLogin, register as apiRegister } from '../services/auth';

const AuthContext = createContext();
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));

    const login = async (username, password) => {
        const res = await apiLogin(username, password);
        localStorage.setItem('token', res.data.token);
        setToken(res.data.token);
        setUser({ username: res.data.username, id: res.data.id });
    };

    const register = async (username, email, password) => {
        await apiRegister(username, email, password);
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, token, login, register, logout, isAuthenticated: !!token }}>
            {children}
        </AuthContext.Provider>
    );
};