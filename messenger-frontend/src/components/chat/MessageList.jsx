import MessageItem from './MessageItem';

export default function MessageList({
                                        messages,
                                        currentUser,
                                        messagesEndRef,
                                        ...props
                                    }) {
    const safeMessages = Array.isArray(messages) ? messages : [];

    if (safeMessages.length === 0) {
        return <div className="p-4 text-gray-500">Нет сообщений</div>;
    }

    return (
        <div className="flex-1 overflow-y-auto p-4 space-y-2">
            {safeMessages.map((msg) => (
                <MessageItem
                    key={msg.id}
                    message={msg}
                    isOwn={msg.sender?.id === currentUser?.id}
                    {...props}
                />
            ))}
            <div ref={messagesEndRef} />
        </div>
    );
}
