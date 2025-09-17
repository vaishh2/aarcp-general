// src/pages/AIRecipeAssistant.jsx
import React, { useState } from 'react';
import axios from 'axios';

const AIAssistantPage = () => {
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);

  const handleAsk = async () => {
    if (!question) return; // ✅ FIXED: changed 'query' to 'question'
    setLoading(true);
    setAnswer('');
    try {
      const response = await axios.get(`http://localhost:8081/api/ask?query=${encodeURIComponent(question)}`);
      setAnswer(response.data);
    } catch (error) {
      setAnswer("Sorry, I couldn't fetch an answer. Please try again.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ai-assistant-container">
      <h2>AI Recipe Assistant</h2>
      <p>Ask any question about cooking, ingredients, or nutrition!</p>
      <input
        type="text"
        value={question}
        onChange={(e) => setQuestion(e.target.value)}
        placeholder="Ask a health or recipe question"
      />
      <button onClick={handleAsk} disabled={loading}>
        {loading ? 'Thinking...' : 'Ask'}
      </button>
      {answer && <div className="ai-answer">{answer}</div>}
    </div>
  );
};

export default AIAssistantPage;
