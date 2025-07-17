# flask-ai/app.py
from flask import Flask, request, jsonify
from model.keyword_extractor import KeywordExtractor

app = Flask(__name__)
extractor = KeywordExtractor()

@app.route("/")
def index():
    return "run Flask AI"

@app.route("/analyze", methods=["POST"])
def analyze():
    data = request.get_json()
    review = data.get("review", "")

    if not review.strip():
        return jsonify({"keywords": []})

    keywords = extractor.extract_keywords(review)
    return jsonify({"keywords": keywords})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5050)
