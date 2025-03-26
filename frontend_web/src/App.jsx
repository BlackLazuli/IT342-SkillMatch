import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './pages/login/login';
import SignUp from "./pages/sign-up/SignUp";
import MarketingPage from './pages/landingpage/MarketingPage';
import Dashboard from './pages/dashboard/Dashboard';
function App() {
  const [count, setCount] = useState(0);

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/MarketingPage" />} />
        <Route path="/MarketingPage" element={<MarketingPage />} />  
        <Route path="/login" element={<Login />} />
        <Route path="/sign-up" element={<SignUp />} />
        <Route path="/dashboard" element={<Dashboard/>}/>
        
      </Routes>
    </Router>
  );
}

export default App;
