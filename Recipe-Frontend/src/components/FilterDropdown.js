import React from 'react';

const FilterDropdown = ({ selectedFilter, onFilterChange }) => {
  return (
    <div style={{ marginBottom: '20px' }}>
      <label htmlFor="filter">Select Feature: </label>
      <select
        id="filter"
        value={selectedFilter}
        onChange={(e) => onFilterChange(e.target.value)}
        style={{ padding: '8px 12px', borderRadius: '6px', fontSize: '1rem' }}
      >
        <option value="nutrition">Nutrition Based</option>
        <option value="quick">Quick 5 min Meals</option>
        <option value="7days">7 Days Plan</option>
        <option value="protein">Protein Rich</option>
      </select>
    </div>
  );
};

export default FilterDropdown;
