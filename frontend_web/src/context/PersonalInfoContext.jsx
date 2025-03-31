import React, { createContext, useContext, useState } from "react";

const PersonalInfoContext = createContext(null);

export const PersonalInfoProvider = ({ children }) => {
    const [personalInfo, setPersonalInfo] = useState(null); // ✅ Start as null

  return (
    <PersonalInfoContext.Provider value={{ personalInfo, setPersonalInfo }}>
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
