import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

function ConversationList({ userId, selectedId, onSelect }) {
  const [convs, setConvs] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetch = useCallback(async () => {
    try {
      const res = await api.get('/chat/conversations', { params: { userId } });
      setConvs(res.data.data);
    } catch {
      setConvs([]);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    fetch();
    const interval = setInterval(fetch, 15000);
    return () => clearInterval(interval);
  }, [fetch]);

  if (loading) return <div className="p-4 text-gray-500 text-sm">Loading...</div>;

  if (convs.length === 0) {
    return (
      <div className="p-4 text-gray-500 text-sm text-center">
        No conversations yet
      </div>
    );
  }

  return (
    <div className="overflow-y-auto h-full">
      {convs.map((c) => (
        <button
          key={c.id}
          onClick={() => onSelect(c.id)}
          className={`w-full text-left p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors ${
            selectedId === c.id ? 'bg-primary-50' : ''
          }`}
        >
          <div className="flex items-center gap-3">
            {c.otherUserPicture ? (
              <img src={`/api/v1/files/${c.otherUserPicture}`} alt=""
                className="w-10 h-10 rounded-full object-cover" />
            ) : (
              <div className="w-10 h-10 rounded-full bg-primary-50 flex items-center justify-center flex-shrink-0">
                <span className="text-sm font-bold text-primary-500">
                  {c.otherUserName?.charAt(0) || '?'}
                </span>
              </div>
            )}
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <span className="font-medium text-sm truncate">{c.otherUserName}</span>
                {c.lastMessageAt && (
                  <span className="text-xs text-gray-400 flex-shrink-0">
                    {new Date(c.lastMessageAt).toLocaleDateString('en-IN', {
                      day: 'numeric', month: 'short'
                    })}
                  </span>
                )}
              </div>
              {c.lastMessage && (
                <p className="text-xs text-gray-500 truncate mt-0.5">{c.lastMessage}</p>
              )}
            </div>
            {c.unreadCount > 0 && (
              <span className="bg-primary-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center flex-shrink-0">
                {c.unreadCount > 9 ? '9+' : c.unreadCount}
              </span>
            )}
          </div>
        </button>
      ))}
    </div>
  );
}

function ChatWindow({ conversationId, userId }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);
  const bottomRef = useRef(null);

  const fetchMessages = useCallback(async () => {
    try {
      const res = await api.get(`/chat/conversations/${conversationId}/messages`, {
        params: { userId, size: 100 }
      });
      setMessages(res.data.data.content);
      api.put(`/chat/conversations/${conversationId}/read?userId=${userId}`).catch(() => {});
    } catch {
      setMessages([]);
    } finally {
      setLoading(false);
    }
  }, [conversationId, userId]);

  useEffect(() => {
    if (!conversationId) return;
    setLoading(true);
    fetchMessages();
    const interval = setInterval(fetchMessages, 10000);
    return () => clearInterval(interval);
  }, [conversationId, fetchMessages]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  async function handleSend(e) {
    e.preventDefault();
    if (!text.trim() || sending) return;
    setSending(true);
    try {
      await api.post(`/chat/conversations/${conversationId}/messages?userId=${userId}`,
        { text: text.trim() });
      setText('');
      await fetchMessages();
    } catch {
      toast.error('Failed to send message');
    } finally {
      setSending(false);
    }
  }

  if (!conversationId) {
    return (
      <div className="flex items-center justify-center h-full text-gray-400 text-senior-base">
        Select a conversation to start chatting
      </div>
    );
  }

  if (loading) {
    return <div className="flex items-center justify-center h-full text-gray-500">Loading...</div>;
  }

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {messages.length === 0 && (
          <div className="text-center text-gray-400 text-sm py-8">
            No messages yet. Say hello!
          </div>
        )}
        {messages.map((m) => {
          const isMine = m.senderId === userId;
          return (
            <div key={m.id} className={`flex ${isMine ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[75%] px-4 py-2 rounded-xl text-sm ${
                isMine
                  ? 'bg-primary-500 text-white rounded-br-sm'
                  : 'bg-gray-100 text-gray-900 rounded-bl-sm'
              }`}>
                <p>{m.text}</p>
                <p className={`text-[10px] mt-1 ${isMine ? 'text-primary-100' : 'text-gray-400'}`}>
                  {new Date(m.createdAt).toLocaleTimeString('en-IN', {
                    hour: '2-digit', minute: '2-digit'
                  })}
                  {isMine && (m.read ? ' ✓✓' : ' ✓')}
                </p>
              </div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={handleSend} className="p-4 border-t border-gray-200">
        <div className="flex gap-2">
          <input
            type="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Type a message..."
            className="input flex-1 text-senior-base"
            maxLength={2000}
          />
          <button type="submit" disabled={sending || !text.trim()}
            className="btn-primary py-2 px-4">
            Send
          </button>
        </div>
      </form>
    </div>
  );
}

export default function ChatPage() {
  const { conversationId: paramConvId } = useParams();
  const { user } = useAuth();
  const [selectedId, setSelectedId] = useState(paramConvId ? parseInt(paramConvId) : null);

  useEffect(() => {
    if (paramConvId) setSelectedId(parseInt(paramConvId));
  }, [paramConvId]);

  return (
    <div className="max-w-5xl mx-auto px-4 py-4">
      <h1 className="page-title mb-4">Messages</h1>
      <div className="card flex h-[70vh] overflow-hidden p-0">
        <div className="w-80 border-r border-gray-200 flex-shrink-0 overflow-y-auto">
          <ConversationList
            userId={user.id}
            selectedId={selectedId}
            onSelect={setSelectedId}
          />
        </div>
        <div className="flex-1">
          <ChatWindow conversationId={selectedId} userId={user.id} />
        </div>
      </div>
    </div>
  );
}
