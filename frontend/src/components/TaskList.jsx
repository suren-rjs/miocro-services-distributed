import React from 'react';
import api from '../api';
import { Trash2, Edit, CheckCircle } from 'lucide-react';

const TaskList = ({ tasks, onTaskUpdated, onEdit }) => {
  const toggleComplete = async (task) => {
    try {
      await api.put(`/api/tasks/${task.id}/complete`);
      onTaskUpdated();
    } catch (error) {
      console.error('Error completing task:', error);
    }
  };

  const deleteTask = async (id) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await api.delete(`/api/tasks/${id}`);
        onTaskUpdated();
      } catch (error) {
        console.error('Error deleting task:', error);
      }
    }
  };

  return (
    <div className="task-list">
      {tasks.length === 0 ? (
        <p>No tasks found. Add one above!</p>
      ) : (
        tasks.map((task) => (
          <div key={task.id} className="card task-item">
            <div className="task-info">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span className={`status-badge ${task.status === 'DONE' ? 'status-done' : 'status-todo'}`}>
                  {task.status}
                </span>
                <h3>{task.title}</h3>
              </div>
              <p>{task.description}</p>
            </div>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {task.status !== 'DONE' && (
                <button
                  className="btn btn-success"
                  onClick={() => toggleComplete(task)}
                  title="Mark as Complete"
                >
                  <CheckCircle size={18} />
                </button>
              )}
              <button
                className="btn btn-primary"
                onClick={() => onEdit(task)}
                title="Edit Task"
              >
                <Edit size={18} />
              </button>
              <button
                className="btn btn-danger"
                onClick={() => deleteTask(task.id)}
                title="Delete Task"
              >
                <Trash2 size={18} />
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

export default TaskList;
