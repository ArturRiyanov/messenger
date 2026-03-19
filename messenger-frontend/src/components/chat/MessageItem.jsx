import { useState } from 'react';

export default function MessageItem({
                                        message,
                                        isOwn,
                                        onEdit,
                                        onDelete,
                                        onPin,
                                        onUnpin,
                                        onReply,
                                    }) {
    const [editing, setEditing] = useState(false);
    const [editContent, setEditContent] = useState(message?.content || '');

    if (!message) return null;
    if (!message.sender) {
        return (
            <div className="text-red-500 p-2">Ошибка: отправитель неизвестен</div>
        );
    }

    const handleEdit = () => {
        onEdit(message.id, editContent);
        setEditing(false);
    };

    return (
        <div className={`flex ${isOwn ? 'justify-end' : 'justify-start'}`}>
            <div
                className={`max-w-xs rounded-lg p-3 ${
                    isOwn ? 'bg-blue-500 text-white' : 'bg-gray-200'
                }`}
            >
                {!isOwn && (
                    <div className="text-xs font-bold mb-1">
                        {message.sender.username || 'Неизвестный'}
                    </div>
                )}
                {editing ? (
                    <div className="flex">
                        <input
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                            autoFocus
                            className="border rounded px-2 text-black"
                        />
                        <button onClick={handleEdit} className="ml-1">
                            ✓
                        </button>
                        <button onClick={() => setEditing(false)} className="ml-1">
                            ✗
                        </button>
                    </div>
                ) : (
                    <div>{message.content}</div>
                )}
                <div className="text-xs text-right mt-1 opacity-75">
                    {message.createdAt
                        ? new Date(message.createdAt).toLocaleTimeString()
                        : ''}
                    {message.pinned && <span className="ml-2">📌</span>}
                </div>
                <div className="flex justify-end mt-2 space-x-2 text-xs">
                    {isOwn && !editing && (
                        <>
                            <button onClick={() => setEditing(true)}>Edit</button>
                            <button onClick={() => onDelete(message.id)}>Delete</button>
                        </>
                    )}
                    <button onClick={() => onReply(message)}>Reply</button>
                    {!message.pinned ? (
                        <button onClick={() => onPin(message.id)}>Pin</button>
                    ) : (
                        <button onClick={() => onUnpin(message.id)}>Unpin</button>
                    )}
                </div>
            </div>
        </div>
    );
}
