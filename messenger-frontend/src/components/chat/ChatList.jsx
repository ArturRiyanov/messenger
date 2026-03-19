export default function ChatList({ chats, selectedChat, onSelectChat }) {
    const safeChats = Array.isArray(chats) ? chats : [];

    const getChatName = (chat) => {
        if (chat.name) return chat.name;
        return 'Chat';
    };

    return (
        <div className="overflow-y-auto h-full">
            <div className="p-4 border-b">
                <h2 className="text-xl font-semibold">Chats</h2>
            </div>
            <ul>
                {safeChats.map((chat) => (
                    <li
                        key={chat.id}
                        onClick={() => onSelectChat(chat)}
                        className={`p-4 border-b cursor-pointer hover:bg-gray-100 ${
                            selectedChat?.id === chat.id ? 'bg-blue-100' : ''
                        }`}
                    >
                        <div className="flex justify-between">
                            <span className="font-medium">{getChatName(chat)}</span>
                            {chat.lastMessage && (
                                <span className="text-sm text-gray-500">
                  {new Date(
                      chat.lastMessage.createdAt
                  ).toLocaleTimeString()}
                </span>
                            )}
                        </div>
                        {chat.lastMessage && (
                            <p className="text-sm text-gray-600 truncate">
                                {chat.lastMessage.sender?.username}:{' '}
                                {chat.lastMessage.content}
                            </p>
                        )}
                    </li>
                ))}
            </ul>
        </div>
    );
}
