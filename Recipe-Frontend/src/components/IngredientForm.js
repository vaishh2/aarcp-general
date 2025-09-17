import React, { useState, useRef } from "react";
import axios from "axios";
import MicRecorder from "./MicRecorder";
import "../App.css";

const IngredientForm = ({ onDishesGenerated, selectedFilter }) => {
  const [ingredients, setIngredients] = useState("");
  const [loading, setLoading] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [busy, setBusy] = useState(false);
  const [uploading, setUploading] = useState(false);
  const recorderRef = useRef(null);

  const handleGenerate = async () => {
    setLoading(true);
    const url = selectedFilter
      ? "http://localhost:8081/api/recipes/suggest-dishes-filtered"
      : "http://localhost:8081/api/recipes/suggest-dishes";

    const payload = selectedFilter
      ? { ingredients, filter: selectedFilter }
      : { ingredients, feature: selectedFilter };

    try {
      const response = await axios.post(url, payload);
      const dishes = response.data
        .split("\n")
        .filter((line) => line.trim() !== "");
      onDishesGenerated(dishes);
    } catch (error) {
      console.error(error);
      alert("Error generating dishes");
    } finally {
      setLoading(false);
    }
  };

  // MicRecorder callback
  const handleTranscript = (transcriptText) => {
    setIngredients((prev) =>
      prev ? prev + " " + transcriptText : transcriptText
    );
    setBusy(false);
  };

  const toggleRecording = async () => {
    if (busy) return;

    if (!isRecording) {
      try {
        if (!recorderRef.current) return alert("Recorder not ready");
        await recorderRef.current.start();
        setIsRecording(true);
      } catch (err) {
        console.error(err);
        alert("Unable to start recording: " + (err.message || err));
      }
    } else {
      setBusy(true);
      try {
        await recorderRef.current.stop(); // MicRecorder will call handleTranscript
      } catch (err) {
        console.error(err);
        alert("Recording failed: " + (err.message || err));
        setBusy(false);
      } finally {
        setIsRecording(false);
      }
    }
  };

  // Image upload → analyze ingredients
  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    setUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axios.post(
        "http://localhost:8081/vision/ingredients",
        formData
      );

      // Ensure we get an array of ingredients
      const detectedIngredients = Array.isArray(response.data)
        ? response.data
        : response.data.ingredients
        ? response.data.ingredients
        : [];
      setIngredients(detectedIngredients.join(", "));
    } catch (error) {
      console.error("Axios error:", error.response || error);
      alert("Something went wrong while analyzing the image.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="ingredient-form">
      <div className="textarea-wrapper" style={{ position: "relative" }}>
        <textarea
          rows="4"
          value={ingredients}
          onChange={(e) => setIngredients(e.target.value)}
          placeholder="Enter ingredients (comma-separated)..."
          className="ingredient-textarea"
          style={{ paddingRight: "100px" }}
        />
        <div
          style={{
            position: "absolute",
            top: "50%",
            right: "400px",
            transform: "translateY(-50%)",
          }}
        >
          <MicRecorder onTranscript={handleTranscript} ref={recorderRef} />
        </div>
      </div>

      <div style={{ marginTop: "10px" }}>
        <input
          type="file"
          name="file"
          accept="image/*"
          onChange={handleFileChange}
          disabled={uploading}
        />
        {uploading && <p>Analyzing image...</p>}
      </div>

      <button
        onClick={handleGenerate}
        disabled={loading}
        className="ingredient-button"
        style={{ marginTop: "10px" }}
      >
        {loading ? "Generating..." : "Suggest Dishes"}
      </button>
    </div>
  );
};

export default IngredientForm;
