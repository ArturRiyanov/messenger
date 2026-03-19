import api from './api';

export const register = (username, email, password) =>
    api.post(
        '/test/register',
        new URLSearchParams({ username, email, password }),
        {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        }
    );

export const login = (username, password) =>
    api.post('/test/login', { username, password });
