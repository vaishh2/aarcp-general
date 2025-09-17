
import os
os.environ["TRANSFORMERS_NO_TF"] = "1"

from sentence_transformers import SentenceTransformer
import json

# Load model
model = SentenceTransformer('all-MiniLM-L6-v2')

# Load your knowledge.json file
with open('Knowledge.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
print(f"Loaded {len(knowledge_data)} entries.")
model = SentenceTransformer('all-MiniLM-L6-v2')
print("Model loaded.")
# Generate embeddings
for entry in data:
    full_text = entry['title'] + " " + entry['content']
    embedding = model.encode(full_text).tolist()
    entry['embedding'] = embedding
    print("Embeddings generated.")

# Save to a new JSON file with embeddings
with open('knowledge_with_vectors.json', 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2)

print("✅ Embeddings added to knowledge_with_vectors.json")
