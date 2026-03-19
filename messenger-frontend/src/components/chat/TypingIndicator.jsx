export default function TypingIndicator({ users }) {
    if (!users?.length) return null;
    return (
        <div className="px-4 py-1 text-sm text-gray-500 italic">
            {users.map((u) => u.username).join(', ')}{' '}
            {users.length === 1 ? 'печатает' : 'печатают'}...
        </div>
    );
}
