import React from "react";
import AppBar from "../../component/AppBar"; 

const CustomerDashboard = () => {
  <AppBar />
  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold">Welcome, Customer!</h2>
      <p>This is your customer dashboard.</p>
    </div>
  );
};

export default CustomerDashboard;
