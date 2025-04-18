import React, { createContext, useContext, useState, useEffect } from "react";

const PersonalInfoContext = createContext(null);

export const PersonalInfoProvider = ({ children }) => {
  const [personalInfo, setPersonalInfo] = useState(null);

  // Fetch personalInfo from localStorage if available
  useEffect(() => {
    const storedPersonalInfo = localStorage.getItem("personalInfo");
    if (storedPersonalInfo) {
      setPersonalInfo(JSON.parse(storedPersonalInfo)); // Parse and set if found
    }
  }, []);

  // Update personalInfo and save it to localStorage
  const updatePersonalInfo = (info) => {
    setPersonalInfo(info);
    localStorage.setItem("personalInfo", JSON.stringify(info)); // Save to localStorage
  };

  return (
    <PersonalInfoContext.Provider value={{ personalInfo, setPersonalInfo: updatePersonalInfo }}>
      {children}
    </PersonalInfoContext.Provider>
  );
};

export const usePersonalInfo = () => {
  const context = useContext(PersonalInfoContext);
  if (!context) {
    throw new Error("usePersonalInfo must be used within a PersonalInfoProvider");
  }
  return context;
};
