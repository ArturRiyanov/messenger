import api from './api';

export const getUserChats = () => api.get('/chats').then(res => res.data);
export const getChatMessages = (chatId) => api.get(`/chats/${chatId}/messages`).then(res => res.data);
export const sendMessage = (chatId, content) => api.post('/chats/messages', { chatId, content }).then(res => res.data);
export const updateMessage = (messageId, content) => api.put(`/chats/messages/${messageId}`, { content }).then(res => res.data);
export const deleteMessage = (messageId) => api.delete(`/chats/messages/${messageId}`).then(res => res.data);
export const pinMessage = (chatId, messageId) => api.post(`/chats/${chatId}/messages/${messageId}/pin`).then(res => res.data);
export const unpinMessage = (chatId, messageId) => api.delete(`/chats/${chatId}/messages/${messageId}/pin`).then(res => res.data);
export const getPinnedMessages = (chatId) => api.get(`/chats/${chatId}/pinned`).then(res => res.data);
export const replyToMessage = (chatId, messageId, content) =>
    api.post(`/chats/${chatId}/messages/${messageId}/reply`, { content }).then(res => res.data);
export const forwardMessage = (sourceChatId, messageId, targetChatId) =>
    api.post(`/chats/${sourceChatId}/messages/${messageId}/forward`, { targetChatId }).then(res => res.data);