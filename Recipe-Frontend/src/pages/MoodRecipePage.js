import React, { useState } from 'react';
import axios from 'axios';
import './MoodRecipePage.css';

const MoodRecipePage = () => {
  const [mood, setMood] = useState('');
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(false);
const [type, setType] = useState('');

  const handleFetchRecipes = async () => {
    if (!mood) return;
    setLoading(true);
     try {
    const response = await axios.post(
      `http://localhost:8081/api/mood-recipes`,
      { mood: mood,type },
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
      const recipeList = response.data
        .split((/\n(?=\d+\.\s)/g))
        .filter(line => line.trim() !== '')
       

      setRecipes(recipeList);
    } catch (err) {
      console.error('Error fetching recipes:', err);
      setRecipes(["Failed to fetch recipes"]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mood-page" style={{ backgroundImage: "url('/moodbg.jpg')" }}>
        
      <h1 className="mood-title">What's Your Mood? 😄</h1>

      <div className="mood-controls">
        <select
          value={mood}
          onChange={e => setMood(e.target.value)}
          className="mood-select"
        >
          <option value="">Select Your Mood --</option>
          <option value="tired">Tired</option>
          <option value="feeling sick">Feeling Sick</option>
          <option value="want comfort food">Want Comfort Food</option>
          <option value="need energy">Need Energy</option>
          <option value="party mode">Party Mode</option>
        </select>
<select
  value={type}
  onChange={e => setType(e.target.value)}
  className="mood-select"
>
  <option value="">-- Select Type --</option>
  <option value="veg">Veg</option>
  <option value="non-veg">Non-Veg</option>
</select>

        <button
          onClick={handleFetchRecipes}
          disabled={loading}
          className="mood-button"
        >
          {loading ? 'Loading...' : 'Get Recipes'}
        </button>
      </div>

      <div className="mood-recipe-list">
        {recipes.length > 0 && (
          <ul>
            {recipes.map((r, i) => (
              <li key={i} className="mood-recipe-item">
                {r}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default MoodRecipePage;

