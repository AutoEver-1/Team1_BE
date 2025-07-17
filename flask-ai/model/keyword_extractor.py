# flask-ai/model/keyword_extractor.py

from keybert import KeyBERT
from konlpy.tag import Okt

class KeywordExtractor:
    def __init__(self):
        self.kw_model = KeyBERT(model="distiluse-base-multilingual-cased-v1")
        self.okt = Okt()

    def extract_keywords(self, text, top_n=5):
        nouns = self.okt.nouns(text)
        if not nouns:
            return []

        clean_text = " ".join(nouns)
        dynamic_n = min(top_n, max(1, len(nouns) // 2))

        keywords = self.kw_model.extract_keywords(
            clean_text,
            top_n=dynamic_n,
            stop_words=None
        )

        return [kw[0] for kw in keywords if kw[1] >= 0.15]

