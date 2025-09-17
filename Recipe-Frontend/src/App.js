import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import FeaturePage from './pages/FeaturePage';
import AIAssistantPage from './pages/AIAssistantPage'; // you'll create this next
import MoodRecipePage from './pages/MoodRecipePage';
import ImagePage from './pages/ImagePage';
import InvoiceUploadPage from "./pages/InvoiceUploadPage";

const App = () => {
  return (
    <Router>
      <Routes>
                <Route path="/mood-recipes" element={<MoodRecipePage />} />
        <Route path="/ai-recipe-assistant" element={<AIAssistantPage />} />
        <Route path="/" element={<HomePage />} />
       <Route path="/image-analyze" element={<ImagePage />} />
        <Route path="/feature/:feature" element={<FeaturePage />} />
        <Route path="/upload-invoice" element={<InvoiceUploadPage />} />
      </Routes>
    </Router>
  );
};

export default App;











