// import React, { useState } from "react";

// let ws;
// let mediaRecorder;

// export default function VoiceRecorder({ recipeAudio }) {
//   const [listening, setListening] = useState(false);

  

//     ws.onopen = () => {
//       navigator.mediaDevices.getUserMedia({ audio: true }).then(stream => {
//         mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });

//         mediaRecorder.ondataavailable = (e) => {
//           if (e.data.size > 0 && ws.readyState === WebSocket.OPEN) {
//             e.data.arrayBuffer().then(buffer => ws.send(buffer));
//           }
//         };

//         mediaRecorder.start(500); // chunk every 500ms
//         setListening(true);
//       });
//     };

//     ws.onmessage = (msg) => {
//       const command = msg.data;
//       console.log("Command received:", command);

//       if (command === "STOP") recipeAudio.pause();
//       if (command === "RESUME") recipeAudio.play();
//       if (command === "RESTART") {
//         recipeAudio.currentTime = 0;
//         recipeAudio.play();
//       }
//     };
//   };

//   const stopVoiceWS = () => {
//     if (mediaRecorder && mediaRecorder.state !== "inactive") mediaRecorder.stop();
//     if (ws && ws.readyState === WebSocket.OPEN) ws.send("STOP_LISTENING");
//     setListening(false);
//   };



