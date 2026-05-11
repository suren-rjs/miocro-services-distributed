import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import api from './api';
import TaskList from './components/TaskList';
import TaskForm from './components/TaskForm';
import Login from './components/Login';
import NotificationToaster from './components/NotificationToaster';
import { Layout, LogOut } from 'lucide-react';
import './App.css';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  const [tasks, setTasks] = useState([]);
  const [editingTask, setEditingTask] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/tasks');
      setTasks(response.data);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/login';
  };

  useEffect(() => {
    if (localStorage.getItem('token')) {
      fetchTasks();
    }
  }, []);

  return (
    <Router>
      <div className="container">
        <header className="header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <Layout size={32} color="#646cff" />
            <h1>Distributed Task Manager</h1>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {localStorage.getItem('token') && (
              <button className="btn" onClick={handleLogout} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <LogOut size={18} />
                Logout
              </button>
            )}
          </div>
        </header>

        <main>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <div>
                    <NotificationToaster />
                    <TaskForm
                      onTaskAdded={() => {
                        fetchTasks();
                        setEditingTask(null);
                      }}
                      editingTask={editingTask}
                      onCancel={() => setEditingTask(null)}
                    />

                    <div style={{ marginTop: '2rem' }}>
                      <h2>Your Tasks</h2>
                      {loading ? (
                        <p>Loading tasks...</p>
                      ) : (
                        <TaskList
                          tasks={tasks}
                          onTaskUpdated={fetchTasks}
                          onEdit={(task) => setEditingTask(task)}
                        />
                      )}
                    </div>
                  </div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
