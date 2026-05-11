import React, { useEffect, useState } from 'react';
import { Bell, X } from 'lucide-react';

const NotificationToaster = () => {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    // Connect to the SSE stream via the Gateway
    // Note: EventSource doesn't support headers natively, but since we are 
    // going through the Gateway, and we want to learn, we'll use a trick or 
    // just rely on the fact that for THIS demo we can pass the token as a query param 
    // OR we can use a library. 
    // However, to keep it "standard", let's update the Gateway to also accept token 
    // from a query param if it's an SSE request, OR just use the header-supported 
    // 'event-source-polyfill' if available.
    
    // For simplicity in this learning session, I will update the Gateway Filter 
    // to also look for 'token' in query params for /stream.
    
    const eventSource = new EventSource(`/api/notifications/stream?token=${token}`);

    eventSource.addEventListener('notification', (event) => {
      const data = JSON.parse(event.data);
      const newNotif = {
        id: Date.now(),
        message: `Task "${data.title}" was ${data.action}!`,
        type: data.action === 'COMPLETED' ? 'success' : 'info'
      };
      
      setNotifications(prev => [...prev, newNotif]);

      // Auto-remove after 5 seconds
      setTimeout(() => {
        setNotifications(prev => prev.filter(n => n.id !== newNotif.id));
      }, 5000);
    });

    eventSource.addEventListener('init', (event) => {
      console.log('Notification stream initialized:', event.data);
    });

    eventSource.onerror = (err) => {
      console.error('EventSource failed:', err);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div style={{ position: 'fixed', top: '20px', right: '20px', zIndex: 1000, display: 'flex', flexDirection: 'column', gap: '10px' }}>
      {notifications.map(n => (
        <div key={n.id} style={{
          background: n.type === 'success' ? '#4caf50' : '#2196f3',
          color: 'white',
          padding: '12px 20px',
          borderRadius: '8px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          minWidth: '250px',
          animation: 'slideIn 0.3s ease-out'
        }}>
          <Bell size={20} />
          <span style={{ flex: 1 }}>{n.message}</span>
          <X size={16} style={{ cursor: 'pointer' }} onClick={() => setNotifications(prev => prev.filter(notif => notif.id !== n.id))} />
        </div>
      ))}
      <style>{`
        @keyframes slideIn {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default NotificationToaster;
