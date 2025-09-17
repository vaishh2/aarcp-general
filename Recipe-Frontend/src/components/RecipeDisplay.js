// src/components/RecipeDisplay.js
import React, { useRef, useState, useEffect, useCallback } from 'react';
import '../App.css';

const RecipeDisplay = ({ dish, recipe }) => {
  const audioRef = useRef(null);
  const mediaRecorderRef = useRef(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
 const isFirefox = typeof navigator !== "undefined" && navigator.userAgent.toLowerCase().includes("firefox");
  const playRecipe = useCallback(async (startFrom = 0) => {
    try {
      const res = await fetch("http://localhost:8081/api/tts", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: recipe
      });

      if (!res.ok) throw new Error("TTS request failed");

      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      audioRef.current.src = url;
      audioRef.current.currentTime = startFrom;
      await audioRef.current.play();
      setIsPlaying(true);
    } catch (err) {
      console.error("Play recipe error:", err);
    }
  }, [recipe]);

  const stopRecipe = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.pause();
      setIsPlaying(false);
    }
  }, []);

  const resumeRecipe = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.play();
      setIsPlaying(true);
    }
  }, []);

  const startRecording = useCallback(async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: isFirefox ? "audio/ogg" : "audio/webm"
      });
      mediaRecorderRef.current = mediaRecorder;
      let chunks = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunks.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(chunks, { type: 'audio/webm' });
        chunks = [];

        
        const formData = new FormData();
        formData.append('file', audioBlob, `command.${isFirefox ? "ogg" : "webm"}`);
        try {
          const res = await fetch('http://localhost:8081/api/stt', {
            method: 'POST',
            body: formData
          });

          if (!res.ok) throw new Error("STT request failed");

          const transcript = await res.text();
          console.log("Command heard:", transcript);

          const command = transcript.toLowerCase().trim();
          if (command.includes("read recipe")) {
            playRecipe(0);
          } else if (command.includes("stop")) {
            stopRecipe();
          } else if (command.includes("resume") || command.includes("continue")) {
            resumeRecipe();
          }
        } catch (err) {
          console.error("STT error:", err);
        }
      };

      mediaRecorder.start();
      setIsRecording(true);
    } catch (error) {
      console.error("Microphone access error:", error);
    }
  }, [isFirefox,playRecipe, stopRecipe, resumeRecipe]);

  const stopRecording = useCallback(() => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== "inactive") {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
    }
  }, []);

  useEffect(() => {
    if (dish && recipe) {
      startRecording();
      return () => stopRecording();
    }
  }, [dish, recipe, startRecording, stopRecording]);

  if (!dish || !recipe) {
    return <p>No recipe selected.</p>;
  }

  return (
    <div className="recipe-display">
      <h2>{dish}</h2>
      <p>{recipe}</p>

      {!isPlaying ? (
        <button onClick={() => playRecipe()}>🔊 Play</button>
      ) : (
        <button onClick={stopRecipe}>⏹ Stop</button>
      )}
   

      <audio ref={audioRef}></audio>
    </div>
  );
};

export default RecipeDisplay;