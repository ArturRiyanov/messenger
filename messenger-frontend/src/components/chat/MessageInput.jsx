import { useState, useRef } from 'react';

export default function MessageInput({
                                         onSend,
                                         onTyping,
                                         replyTo,
                                         onCancelReply,
                                     }) {
    const [text, setText] = useState('');
    const timeoutRef = useRef();

    const handleChange = (e) => {
        setText(e.target.value);
        onTyping(true);
        if (timeoutRef.current) clearTimeout(timeoutRef.current);
        timeoutRef.current = setTimeout(() => onTyping(false), 2000);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (text.trim()) {
            onSend(text);
            setText('');
            onTyping(false);
            clearTimeout(timeoutRef.current);
        }
    };

    return (
        <div className="p-4 border-t bg-white">
            {replyTo && (
                <div className="mb-2 p-2 bg-gray-100 rounded flex justify-between">
          <span className="text-sm">
            Ответ {replyTo.sender?.username}:{' '}
              {replyTo.content?.substring(0, 30)}...
          </span>
                    <button onClick={onCancelReply} className="text-red-500">
                        ✗
                    </button>
                </div>
            )}
            <form onSubmit={handleSubmit} className="flex">
                <input
                    type="text"
                    value={text}
                    onChange={handleChange}
                    placeholder="Введите сообщение..."
                    className="flex-1 border rounded-l-lg px-4 py-2"
                />
                <button
                    type="submit"
                    className="bg-blue-500 text-white px-4 py-2 rounded-r-lg"
                >
                    Отправить
                </button>
            </form>
        </div>
    );
}
