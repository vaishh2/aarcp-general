import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import IngredientForm from '../components/IngredientForm';
import DishList from '../components/DishList';
import RecipeDisplay from '../components/RecipeDisplay';
import FilterDropdown from '../components/FilterDropdown';

import '../App.css';

const FeaturePage = () => {
  const { feature } = useParams();
  const [dishes, setDishes] = useState([]);
  const [selectedDish, setSelectedDish] = useState('');
  const [recipe, setRecipe] = useState('');
  const [selectedFilter, setSelectedFilter] = useState(feature);
  

  const handleFilterChange = (newFilter) => {
    setSelectedFilter(newFilter);
    setDishes([]);
    setSelectedDish('');
    setRecipe('');
  };

  return (
    <div className="app-container">
      <h1 className="app-title">Feature: {selectedFilter}</h1>

      <FilterDropdown 
        selectedFilter={selectedFilter} 
        onFilterChange={handleFilterChange} 
      />

      <IngredientForm 
        onDishesGenerated={setDishes} 
        selectedFilter={selectedFilter} 
      />

      <DishList 
        dishes={dishes} 
        onDishSelect={setSelectedDish} 
        setRecipe={setRecipe} 
      />

      <RecipeDisplay 
        dish={selectedDish} 
        recipe={recipe} 
      />
    </div>
  );
};

export default FeaturePage;






