import { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from './AuthContext';

const WebSocketContext = createContext();
export const useWebSocket = () => useContext(WebSocketContext);

export const WebSocketProvider = ({ children }) => {
    const { token, user } = useAuth();
    const [connected, setConnected] = useState(false);
    const clientRef = useRef(null);
    const subscriptions = useRef(new Map());

    useEffect(() => {
        if (!token || !user) return;

        // Создаём SockJS с токеном в параметрах
        const socket = new SockJS(`/ws?token=${token}`);
        const client = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log('STOMP:', str),
            onConnect: () => {
                console.log('WebSocket connected');
                setConnected(true);
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
                setConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error', frame);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [token, user]);

    const subscribe = (destination, callback) => {
        if (!clientRef.current || !connected) {
            console.warn('WebSocket not connected, cannot subscribe to', destination);
            return null;
        }
        try {
            const sub = clientRef.current.subscribe(destination, (message) => {
                const body = JSON.parse(message.body);
                callback(body);
            });
            subscriptions.current.set(destination, sub);
            return sub;
        } catch (err) {
            console.error('Subscribe error', err);
            return null;
        }
    };

    const unsubscribe = (destination) => {
        const sub = subscriptions.current.get(destination);
        if (sub) {
            sub.unsubscribe();
            subscriptions.current.delete(destination);
        }
    };

    const send = (destination, body) => {
        if (!clientRef.current || !connected) {
            console.warn('WebSocket not connected, cannot send to', destination);
            return;
        }
        clientRef.current.publish({
            destination,
            body: JSON.stringify(body),
        });
    };

    return (
        <WebSocketContext.Provider value={{ connected, subscribe, unsubscribe, send }}>
            {children}
        </WebSocketContext.Provider>
    );
};