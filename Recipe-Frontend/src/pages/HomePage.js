import React, { useState } from 'react';
import IngredientForm from '../components/IngredientForm';
import DishList from '../components/DishList';
import RecipeDisplay from '../components/RecipeDisplay';
import { useNavigate } from 'react-router-dom';
import MicRecorderComponent from "../components/MicRecorder";
import '../App.css';
import { Link } from "react-router-dom";
const HomePage = () => {
  const [dishes, setDishes] = useState([]);
  const [selectedDish, setSelectedDish] = useState('');
  const [recipe, setRecipe] = useState('');
  const [isRemixMode, setIsRemixMode] = useState(false);

  const navigate = useNavigate();

  const handleToggleRemix = () => {
    setIsRemixMode(!isRemixMode);
    setDishes([]);
    setSelectedDish('');
    setRecipe('');
  };

  const handleFeatureClick = (feature) => {
    navigate(`/feature/${feature}`);
  };

  return (
    <div className="app-container"
      style={{
    backgroundImage: "url('/bg1.jpg')",
    backgroundSize: 'cover',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'center',
    minHeight: '100vh',
  }}
>
      <h1 className="app-title">🍽️ AI Recipe Generator</h1>
<Link to="/image-analyze">
        <button style={{ marginTop: "20px" }}>Analyze Food Image</button>
      </Link>
      <IngredientForm 
        onDishesGenerated={setDishes} 
        selectedFilter={isRemixMode ? 'remix' : 'normal'}  // 👈 pass this
      />

      <div className="remix-toggle">
        <button onClick={handleToggleRemix}>
          {isRemixMode ? '🔄 Switch to Normal Mode' : '✨ Remix Ideas Mode'}
        </button>
        <p className="remix-description">
          {isRemixMode 
            ? 'You are in Remix mode: AI will suggest creative remix ideas from your ingredients!'
            : 'Normal mode: Suggest regular dishes you can cook.'}
        </p>
      </div>
<div className="p-6">
    <h1 className="text-3xl font-bold mb-6">Welcome to Invoice Processor</h1>
    <Link to="/upload-invoice">
      <button className="px-6 py-3 bg-green-500 text-white rounded hover:bg-green-600">
        Upload Invoice
      </button>
    </Link>
  </div>
      {/* Feature buttons */}
      <div className="feature-grid">
        
      <div
  className="feature-card"
  style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/nutrition.jpg'})`
  }}
  onClick={() => handleFeatureClick('nutrition')}
>
  Nutrition Based
</div>
        <div className="feature-card"
        style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/quickemeal.jpg'})`
  }}
         onClick={() => handleFeatureClick('quick')}>
          Quick 5 min Meals
        </div>
        <div className="feature-card" 
        style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/7-daysplan.jpg'})`
  }}onClick={() => handleFeatureClick('7days')}>
          7 Days Plan
        </div>
        <div className="feature-card" 
        style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/protien.jpg'})`
  }}onClick={() => handleFeatureClick('protein')}>
          Protein Rich
        </div>
        <div className="feature-card"
        style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/feature-bg.jpg'})`}}
        onClick={() => navigate('ai-recipe-assistant')}>
          Ask any question about cooking or ingredients
        </div>
        <div className="feature-card"
        style={{backgroundImage: `url(${process.env.PUBLIC_URL + '/mood.jpg'})`}}
        onClick={() => navigate('/mood-recipes')}>
          Mood Based Recipes 🎭
        </div>
      </div>

      <DishList 
        dishes={dishes} 
        onDishSelect={setSelectedDish} 
        setRecipe={setRecipe} 
        isRemixMode={isRemixMode}
      />

      <RecipeDisplay dish={selectedDish} recipe={recipe} />
    </div>
  );
};

export default HomePage;









