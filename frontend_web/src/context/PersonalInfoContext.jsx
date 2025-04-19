import React, { createContext, useContext, useState, useEffect } from "react";

const PersonalInfoContext = createContext(null);

export const PersonalInfoProvider = ({ children }) => {
  const [personalInfo, setPersonalInfoState] = useState(null);

  // Load personalInfo from localStorage on mount
  useEffect(() => {
    const storedPersonalInfo = localStorage.getItem("personalInfo");
    if (storedPersonalInfo) {
      setPersonalInfoState(JSON.parse(storedPersonalInfo));
    }
  }, []);

  // Helper to update personalInfo and save to localStorage
  const updatePersonalInfo = (info) => {
    setPersonalInfoState(info);
    localStorage.setItem("personalInfo", JSON.stringify(info));
  };

  // Separate helper to update just the profile picture
  const updateProfilePicture = (profilePicturePath) => {
    if (personalInfo) {
      const updatedInfo = {
        ...personalInfo,
        profilePicture: profilePicturePath,
      };
      updatePersonalInfo(updatedInfo);
    }
  };

  return (
    <PersonalInfoContext.Provider
      value={{
        personalInfo,
        setPersonalInfo: updatePersonalInfo,
        updateProfilePicture,
      }}
    >
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
