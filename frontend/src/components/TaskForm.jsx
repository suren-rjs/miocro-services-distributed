import React, { useState, useEffect } from 'react';
import api from '../api';

const TaskForm = ({ onTaskAdded, editingTask, onCancel }) => {
  const [task, setTask] = useState({ title: '', description: '', status: 'TODO' });

  useEffect(() => {
    if (editingTask) {
      setTask(editingTask);
    } else {
      setTask({ title: '', description: '', status: 'TODO' });
    }
  }, [editingTask]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingTask) {
        await api.put(`/api/tasks/${editingTask.id}`, task);
      } else {
        await api.post('/api/tasks', task);
      }
      onTaskAdded();
      setTask({ title: '', description: '', status: 'TODO' });
    } catch (error) {
      console.error('Error saving task:', error);
    }
  };

  return (
    <div className="card">
      <h3>{editingTask ? 'Edit Task' : 'Add New Task'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Title</label>
          <input
            type="text"
            value={task.title}
            onChange={(e) => setTask({ ...task, title: e.target.value })}
            required
          />
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea
            value={task.description}
            onChange={(e) => setTask({ ...task, description: e.target.value })}
          />
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button type="submit" className="btn btn-primary">
            {editingTask ? 'Update' : 'Create'}
          </button>
          {editingTask && (
            <button type="button" className="btn" onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default TaskForm;
