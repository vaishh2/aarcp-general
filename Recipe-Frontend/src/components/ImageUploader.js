import React, { useState, useEffect } from "react";
import axios from "axios";

function ImageUploader() {
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [ingredients, setIngredients] = useState([]);
  const [recipes, setRecipes] = useState([]);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [loading, setLoading] = useState(false);

  // Generate preview URL
  useEffect(() => {
    if (!file) {
      setPreviewUrl(null);
      return;
    }
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url); // cleanup
  }, [file]);

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setIngredients([]);
    setRecipes([]);
    setSelectedRecipe(null);
  };

  const handleUpload = async () => {
    if (!file) {
      alert("Please select an image first.");
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append("file", file); // Must match backend @RequestParam("file")

    try {
      const response = await axios.post(
        "http://localhost:8081/vision/ingredients",
        formData
      );

      const data = response.data;

      // Backend returns an array of ingredients
      if (Array.isArray(data)) {
        setIngredients(data);
      } else if (data.ingredients && Array.isArray(data.ingredients)) {
        setIngredients(data.ingredients);
      } else if (typeof data === "string") {
        setIngredients([data]);
      } else {
        setIngredients([]);
      }

      // Recipes if backend returns
      setRecipes(data.recipes || []);
    } catch (error) {
      console.error("Error:", error.response || error);
      alert("Something went wrong while analyzing the image.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ textAlign: "center", marginTop: "20px" }}>
      <input
        type="file"
        name="file"
        id="file-input"
        style={{ display: "none" }}
        accept="image/*"
        onChange={handleFileChange}
      />
      <label htmlFor="file-input" style={{ cursor: "pointer" }}>
        <div
          style={{
            border: "2px dashed gray",
            padding: "20px",
            width: "300px",
            margin: "auto",
          }}
        >
          {file ? <p>{file.name}</p> : <p>📷 Click to upload food image</p>}
        </div>
      </label>

      {file && (
        <>
          {previewUrl && (
            <div style={{ marginTop: "10px" }}>
              <img
                src={previewUrl}
                alt="preview"
                style={{ maxWidth: "200px", borderRadius: "8px" }}
              />
            </div>
          )}
          <button
            onClick={handleUpload}
            disabled={loading}
            style={{
              marginTop: "10px",
              padding: "10px 20px",
              background: "green",
              color: "white",
              border: "none",
              borderRadius: "5px",
            }}
          >
            {loading ? "Analyzing..." : "Analyze"}
          </button>
        </>
      )}

      {ingredients.length > 0 && (
        <div style={{ marginTop: "20px" }}>
          <h3>🥗 Detected Ingredients:</h3>
          <ul>
            {ingredients.map((ing, i) => (
              <li key={i}>{ing}</li>
            ))}
          </ul>
        </div>
      )}

      {recipes.length > 0 && !selectedRecipe && (
        <div style={{ marginTop: "20px" }}>
          <h3>🍲 Suggested Recipes:</h3>
          <ul>
            {recipes.map((recipe) => (
              <li
                key={recipe.id}
                style={{ cursor: "pointer", color: "blue" }}
                onClick={() => setSelectedRecipe(recipe)}
              >
                {recipe.name} - {recipe.description}
              </li>
            ))}
          </ul>
        </div>
      )}

      {selectedRecipe && (
        <div
          style={{
            marginTop: "20px",
            border: "1px solid gray",
            padding: "10px",
          }}
        >
          <h3>📖 {selectedRecipe.name}</h3>
          <p>{selectedRecipe.description}</p>
          <button onClick={() => setSelectedRecipe(null)}>⬅ Back</button>
        </div>
      )}
    </div>
  );
}

export default ImageUploader;
