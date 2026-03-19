export default function PinnedMessage({ messages, onUnpin }) {
    const safeMessages = Array.isArray(messages) ? messages : [];

    if (safeMessages.length === 0) return null;

    return (
        <div className="bg-yellow-100 p-2 rounded mt-2 text-sm">
            <span className="font-bold">📌 Закреплено</span>
            {safeMessages.map((msg) => (
                <div key={msg.id} className="flex justify-between mt-1">
          <span>
            {msg.sender?.username}: {msg.content?.substring(0, 30)}...
          </span>
                    <button
                        onClick={() => onUnpin(msg.id)}
                        className="text-red-500 text-xs"
                    >
                        Открепить
                    </button>
                </div>
            ))}
        </div>
    );
}
