import React from 'react';
import axios from 'axios';

const DishList = ({ dishes, onDishSelect, setRecipe, isRemixMode }) => {

  const fetchRecipe = async (dish) => {
    onDishSelect(dish);
    try {
      const response = await axios.post('http://localhost:8081/api/recipes/get-recipe', {
        dish: dish
      });
      setRecipe(response.data);
    } catch (error) {
      alert('Error fetching recipe');
    }
  };

  const fetchRemix = async (dish) => {
    onDishSelect(dish + " (Remix)");
    try {
      const response = await axios.post('http://localhost:8081/api/recipes/remix-dishes', {
        dish: dish
      });
      setRecipe(response.data);
    } catch (error) {
      alert('Error fetching remix ideas');
    }
  };

  return (
    <div style={styles.container}>
      <h2>{isRemixMode ? 'Remix Ideas' : 'Suggested Dishes'}</h2>
      <div style={styles.grid}>
        {dishes.map((dish, index) => (
          <button 
            key={index} 
            style={styles.dishButton} 
            onClick={() => isRemixMode ? fetchRemix(dish) : fetchRecipe(dish)}
          >
            {dish}
          </button>
        ))}
      </div>
    </div>
  );
};

const styles = {
  container: {
    marginBottom: '30px',
  },
  grid: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '10px',
    justifyContent: 'center',
  },
  dishButton: {
    padding: '10px 15px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '1rem',
  }
};

export default DishList;

