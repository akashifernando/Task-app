//simplifies HTTP requests
// handles promises cleanly
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081',
  headers: {
    "Content-Type": "application/json",
  },
});

//axios feature run before request sent or after response receive
//auto attach token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');

    
    if (token && !config.url.includes('/api/auth/login') && !config.url.includes('/api/auth/register')) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

//catch error (centralized)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
    
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('username');
      
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

//end point
export const userLogin = (credentials) => api.post('/api/auth/login', credentials);
export const userSignup = (userData) => api.post('/api/auth/register', userData);


export const getAllTasks = (params) => api.get('/api/tasks', { params });
export const getTasks = (userId) => api.get(`/api/tasks?userId=${userId}`);
export const createTask = (data) => api.post('/api/tasks', data);
export const updateTask = (data) => api.put('/api/tasks', data);
export const deleteTask = (id) => api.delete(`/api/tasks/${id}`);
export const getTasksByStatus = (userId, completed) =>
  api.get('/api/tasks/status', { params: { userId, completed } });
// export const getUpcomingTasks = () => api.get('/api/tasks/upcoming');

export default api;

