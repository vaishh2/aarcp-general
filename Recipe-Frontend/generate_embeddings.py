import os
import json
from sentence_transformers import SentenceTransformer

# Disable TensorFlow to avoid errors
os.environ["TRANSFORMERS_NO_TF"] = "1"

# Load the knowledge data from Knowledge.json
with open("Knowledge.json", "r", encoding="utf-8") as f:
    knowledge_data = json.load(f)

print(f"Loaded {len(knowledge_data)} entries.")

# Load the sentence transformer model
model = SentenceTransformer("all-MiniLM-L6-v2")
print("Model loaded.")

# Generate embeddings for each entry
for item in knowledge_data:
    embedding = model.encode(item["content"])
    item["vector"] = embedding.tolist()

# Save the updated data to a new JSON file
with open("knowledge_with_vectors.json", "w", encoding="utf-8") as f:
    json.dump(knowledge_data, f, indent=2)

print("✅ Embeddings generated and saved to knowledge_with_vectors.json")
