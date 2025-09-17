
import React, { useState, useEffect } from "react";
import Mp3Recorder from "mic-recorder-to-mp3";
import "../App.css"; // optional if you want separate mic-specific CSS, but you said all CSS in App.css

const recorder = new Mp3Recorder({ bitRate: 128 });

const MicRecorder = ({ onTranscript }) => {
  const [isRecording, setIsRecording] = useState(false);
  const [isBlocked, setIsBlocked] = useState(false);
  const [isStopped, setIsStopped] = useState(false);

  useEffect(() => {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(() => setIsBlocked(false))
      .catch(() => setIsBlocked(true));
  }, []);

  const startRecording = () => {
    if (isBlocked) {
      alert("Microphone access denied");
    } else {
      recorder.start().then(() => {
        setIsRecording(true);
        setIsStopped(false);
      }).catch(e => console.error("❌ Recorder start failed:", e));
    }
  };

  const stopRecording = () => {
    recorder.stop().getMp3().then(([buffer, blob]) => {
      setIsRecording(false);
      setIsStopped(true);
      const file = new File(buffer, "voice.mp3", {
        type: blob.type,
        lastModified: Date.now()
      });

      // store file so we can send after confirm
      window.voiceBlobFile = file;
    }).catch(e => console.error(e));
  };

  const confirmRecording = () => {
    if (!window.voiceBlobFile) return;

    const formData = new FormData();
    formData.append("file", window.voiceBlobFile);

    fetch("http://localhost:8081/api/speech-to-text", {
      method: "POST",
      body: formData
    })
      .then(res => res.text())
      .then(text => {
        onTranscript(text);
        setIsStopped(false);
      })
      .catch(err => console.error(err));
  };

  return (
    <div className="mic-wrapper">
      {!isRecording && !isStopped && (
        <button type="button" className="mic-btn" onClick={startRecording}>
          🎤
        </button>
      )}
      {isRecording && (
        <button type="button" className="mic-btn recording" onClick={stopRecording}>
          ⏹
        </button>
      )}
      {isStopped && (
        <button type="button" className="mic-btn confirm" onClick={confirmRecording}>
          ✅
        </button>
      )}
    </div>
  );
};

export default MicRecorder;

