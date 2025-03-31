import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import { PersonalInfoProvider } from "./context/PersonalInfoContext"; // Import provider

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <PersonalInfoProvider> {/* Wrap App with PersonalInfoProvider */}
      <App />
    </PersonalInfoProvider>
  </StrictMode>
);
