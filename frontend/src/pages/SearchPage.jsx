import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';

const TABS = [
  { key: 'all', label: 'All' },
  { key: 'activities', label: 'Activities' },
  { key: 'communities', label: 'Communities' },
  { key: 'users', label: 'Users' },
];

export default function SearchPage() {
  const { user } = useAuth();
  const [query, setQuery] = useState('');
  const [type, setType] = useState('all');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const doSearch = useCallback(async (searchPage = 0) => {
    const q = query.trim();
    if (!q) return;
    setLoading(true);
    setSearched(true);
    try {
      const params = { q, type: type === 'all' ? undefined : type, page: searchPage, size: 10 };
      if (user?.city) params.city = user.city;
      if (user?.locality && type !== 'users') params.locality = user.locality;
      const res = await api.get('/search', { params });
      setResults(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
      setPage(searchPage);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, [query, type, user?.city, user?.locality]);

  useEffect(() => {
    setPage(0);
  }, [type]);

  function handleSubmit(e) {
    e.preventDefault();
    doSearch(0);
  }

  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-IN', {
      weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
    });
  }

  function formatTime(dateStr) {
    return new Date(dateStr).toLocaleTimeString('en-IN', {
      hour: '2-digit', minute: '2-digit'
    });
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="page-title mb-6">Search</h1>

      <form onSubmit={handleSubmit} className="mb-6">
        <div className="flex gap-3">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search activities, communities, people..."
            className="input flex-1 text-senior-lg py-3"
          />
          <button type="submit" disabled={loading || !query.trim()}
            className="btn-primary text-senior-base py-3 px-6">
            {loading ? '...' : 'Search'}
          </button>
        </div>
      </form>

      <div className="flex gap-2 mb-6 overflow-x-auto">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setType(t.key)}
            className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${
              type === t.key
                ? 'bg-primary-500 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="text-center py-12 text-gray-500">Searching...</div>
      )}

      {!loading && searched && results.length === 0 && (
        <div className="text-center py-12">
          <p className="text-senior-lg text-gray-500">No results found for "{query}"</p>
          <p className="text-sm text-gray-400 mt-2">Try a different search term or filter</p>
        </div>
      )}

      {!loading && results.length > 0 && (
        <>
          <div className="space-y-4">
            {results.map((item) => {
              const linkTo = item.type === 'ACTIVITY' ? `/activities/${item.id}`
                : item.type === 'COMMUNITY' ? `/communities/${item.id}`
                : `/users/${item.id}`;

              const imgSrc = item.imageUrl
                ? (item.imageUrl.startsWith('http') ? item.imageUrl : `/api/v1/files/${item.imageUrl}`)
                : null;

              return (
                <Link key={`${item.type}-${item.id}`} to={linkTo}
                  className="card flex items-start gap-4 hover:shadow-lg transition-shadow duration-200">
                  {imgSrc ? (
                    <img src={imgSrc} alt="" className="w-16 h-16 rounded-lg object-cover flex-shrink-0" />
                  ) : (
                    <div className="w-16 h-16 rounded-lg bg-primary-50 flex items-center justify-center flex-shrink-0">
                      <span className="text-senior-xl text-primary-400">
                        {item.type === 'ACTIVITY' ? '📅' : item.type === 'COMMUNITY' ? '👥' : '👤'}
                      </span>
                    </div>
                  )}

                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 uppercase">
                        {item.type === 'ACTIVITY' ? 'Activity' : item.type === 'COMMUNITY' ? 'Community' : 'User'}
                      </span>
                      {item.isPrivate && (
                        <span className="text-xs px-2 py-0.5 rounded-full bg-yellow-100 text-yellow-700">
                          Private
                        </span>
                      )}
                    </div>
                    <h3 className="text-senior-lg font-semibold text-gray-900 truncate">{item.title}</h3>
                    {item.subtitle && (
                      <p className="text-sm text-gray-600 mt-1 line-clamp-2">{item.subtitle}</p>
                    )}
                    <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                      {item.city && <span>📍 {item.city}{item.locality ? `, ${item.locality}` : ''}</span>}
                      {item.category && <span>🏷 {item.category}</span>}
                      {item.dateTime && <span>📅 {formatDate(item.dateTime)}</span>}
                      {item.participantCount !== undefined && item.participantCount !== null && (
                        <span>👤 {item.participantCount} joined</span>
                      )}
                      {item.memberCount !== undefined && item.memberCount !== null && (
                        <span>👥 {item.memberCount} members</span>
                      )}
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-4 mt-8">
              <button onClick={() => doSearch(Math.max(0, page - 1))} disabled={page === 0}
                className="btn-secondary text-sm py-2 px-4">Previous</button>
              <span className="flex items-center text-senior-base text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <button onClick={() => doSearch(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary text-sm py-2 px-4">Next</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
