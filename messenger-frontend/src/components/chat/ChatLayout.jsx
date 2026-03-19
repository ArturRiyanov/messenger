import { useState, useEffect } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../context/WebSocketContext';
import ChatList from './ChatList';
import ChatWindow from './ChatWindow';
import { getUserChats } from '../../services/chat';

export default function ChatLayout() {
    const [chats, setChats] = useState([]);
    const [selectedChat, setSelectedChat] = useState(null);
    const { user } = useAuth();
    const { subscribe, unsubscribe, connected } = useWebSocket();
    const navigate = useNavigate();

    const loadChats = async () => {
        try {
            const data = await getUserChats();
            setChats(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Failed to load chats', err);
        }
    };

    useEffect(() => {
        loadChats();
    }, []);

    useEffect(() => {
        if (!subscribe || !connected) return;
        const destination = '/topic/user.status';

        const sub = subscribe(destination, (status) => {
            setChats((prev) =>
                prev.map((chat) => ({
                    ...chat,
                    participants: chat.participants?.map((p) =>
                        p.id === status.userId
                            ? { ...p, onlineStatus: status.online }
                            : p
                    ),
                }))
            );
        });

        return () => {
            unsubscribe(destination);
        };
    }, [subscribe, unsubscribe, connected]);

    const handleSelectChat = (chat) => {
        setSelectedChat(chat);
        navigate(`/chats/${chat.id}`);
    };

    const refreshChats = () => {
        loadChats();
    };

    return (
        <div className="flex h-screen">
            <div className="w-1/3 border-r">
                <ChatList
                    chats={chats}
                    selectedChat={selectedChat}
                    onSelectChat={handleSelectChat}
                />
            </div>
            <div className="flex-1">
                <Routes>
                    <Route
                        path=":chatId"
                        element={<ChatWindow refreshChats={refreshChats} />}
                    />
                    <Route path="/" element={<div className="p-4">Select a chat</div>} />
                </Routes>
            </div>
        </div>
    );
}
