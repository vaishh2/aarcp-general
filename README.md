# 🍴 AARCP General – AI-Powered Recipe Generator

A full-stack project built with **Spring Boot (Backend)** and **React (Frontend)** that helps users generate, remix, and listen to recipes using AI.  
The project integrates with **OpenRouter API** for AI responses and supports **voice input, text-to-speech, and image-based ingredient detection**.  

---

## ✨ Features
-  Dish Suggestions – Enter ingredients and get smart dish suggestions.  
- Recipe Generation– Fetch complete recipes from the backend.  
- Remix Mode – Get creative variations of existing dishes.  
- Voice Input– Record your voice to add ingredients.  
- Text-to-Speech– Listen to the recipes step by step.  
- Image Upload – Upload images of ingredients to detect them automatically.  
- Filters – Choose vegetarian / non-vegetarian options.  

---

## 📂 Project Structure
AARCP-GENERAL/
│── recipebackend/ # Spring Boot backend (controllers, services, models, repositories)
│ ├── src/main/java/com/recipeai/recipebackend/...
│ ├── src/main/resources/application.properties.example
│ └── pom.xml
│
│── recipe-frontend/ # React frontend
│ ├── src/components/...
│ ├── public/
│ └── package.json
│
└── README.md # Project documentation

# Setup Instructions

## 1. Clone the Repository
```bash
git clone https://github.com/<your-username>/AARCP-GENERAL.git
cd AARCP-GENERAL

2. Backend Setup (Spring Boot)

Go to the backend folder:cd recipebackend
Copy the example properties file and add your API key:cp src/main/resources/application.properties.example src/main/resources/application.properties
Open application.properties and set your OpenRouter API key:openrouter.api.key=YOUR_API_KEY_HERE
Run the backend:mvn spring-boot:run
Backend will run on: http://localhost:8081

3. Frontend Setup (React)
Go to the frontend folder:cd recipe-frontend
Install dependencies:npm install
Start the React app:npm start
Frontend will run on: http://localhost:3000

🔑 API Key Setup
This project uses OpenRouter API.

👉 Important: For security reasons, the real API key is not included in this repo.
You must create your own API key from OpenRouter



🤝 Contributing
Contributions, issues, and feature requests are welcome!
Feel free to fork this repo and submit pull requests.
