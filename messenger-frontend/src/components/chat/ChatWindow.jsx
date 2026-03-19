import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useWebSocket } from '../../context/WebSocketContext';
import {
    getChatMessages,
    sendMessage,
    deleteMessage,
    updateMessage,
    pinMessage,
    unpinMessage,
    replyToMessage,
    getPinnedMessages
} from '../../services/chat';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import TypingIndicator from './TypingIndicator';
import PinnedMessage from './PinnedMessage';

export default function ChatWindow({ refreshChats }) {
    const { chatId } = useParams();
    const [messages, setMessages] = useState([]);
    const [pinnedMessages, setPinnedMessages] = useState([]);
    const [typingUsers, setTypingUsers] = useState([]);
    const [replyTo, setReplyTo] = useState(null);
    const { user } = useAuth();
    const { subscribe, unsubscribe, send } = useWebSocket();
    const messagesEndRef = useRef(null);

    useEffect(() => {
        loadMessages();
        loadPinned();

        if (!subscribe) return;

        const msgSub = subscribe(`/topic/chat.${chatId}`, handleWsMessage);
        const typingSub = subscribe(`/topic/chat.${chatId}.typing`, handleTyping);

        return () => {
            unsubscribe(`/topic/chat.${chatId}`);
            unsubscribe(`/topic/chat.${chatId}.typing`);
        };
    }, [chatId, subscribe, unsubscribe]);

    useEffect(() => {
        setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
    }, [messages]);

    const loadMessages = async () => {
        try {
            const data = await getChatMessages(chatId);
            setMessages(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Failed to load messages', err);
        }
    };

    const loadPinned = async () => {
        try {
            const data = await getPinnedMessages(chatId);
            setPinnedMessages(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Failed to load pinned messages', err);
        }
    };

    const handleWsMessage = ({ action, payload }) => {
        if (action === 'CREATE') {
            setMessages(prev => [...prev, payload]);
            // Обновить последнее сообщение в списке чатов
            refreshChats?.();
        } else if (action === 'UPDATE') {
            setMessages(prev => prev.map(m => m.id === payload.id ? payload : m));
        } else if (action === 'DELETE') {
            setMessages(prev => prev.filter(m => m.id !== payload));
        } else if (action === 'PIN' || action === 'UNPIN') {
            loadPinned();
        }
    };

    const handleTyping = (data) => {
        if (data.userId === user?.id) return;
        setTypingUsers(prev =>
            data.typing
                ? [...prev.filter(u => u.userId !== data.userId), data]
                : prev.filter(u => u.userId !== data.userId)
        );
    };

    const handleSend = async (content) => {
        try {
            let newMessage;
            if (replyTo) {
                newMessage = await replyToMessage(chatId, replyTo.id, content);
                setReplyTo(null);
            } else {
                newMessage = await sendMessage(chatId, content);
            }
            setMessages(prev => [...prev, newMessage]);
            refreshChats?.(); // обновить последнее сообщение в списке
        } catch (err) {
            console.error('Failed to send message', err);
        }
    };

    const handleEdit = async (id, content) => {
        try {
            const updated = await updateMessage(id, content);
            setMessages(prev => prev.map(m => m.id === id ? updated : m));
        } catch (err) {
            console.error('Edit failed', err);
        }
    };

    const handleDelete = async (id) => {
        try {
            await deleteMessage(id);
            setMessages(prev => prev.filter(m => m.id !== id));
        } catch (err) {
            console.error('Delete failed', err);
        }
    };

    const handlePin = async (id) => {
        try {
            await pinMessage(chatId, id);
            loadPinned();
        } catch (err) {
            console.error('Pin failed', err);
        }
    };

    const handleUnpin = async (id) => {
        try {
            await unpinMessage(chatId, id);
            loadPinned();
        } catch (err) {
            console.error('Unpin failed', err);
        }
    };

    const sendTyping = (typing) => {
        send?.('/app/chat.typing', { chatId: parseInt(chatId), typing });
    };

    return (
        <div className="flex flex-col h-full">
            <div className="p-4 border-b bg-white">
                <h2 className="text-lg font-semibold">Чат</h2>
                <PinnedMessage messages={pinnedMessages} onUnpin={handleUnpin} />
            </div>
            <MessageList
                messages={messages}
                currentUser={user}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onPin={handlePin}
                onUnpin={handleUnpin}
                onReply={setReplyTo}
                messagesEndRef={messagesEndRef}
            />
            <TypingIndicator users={typingUsers} />
            <MessageInput
                onSend={handleSend}
                onTyping={sendTyping}
                replyTo={replyTo}
                onCancelReply={() => setReplyTo(null)}
            />
        </div>
    );
}