from keybert import KeyBERT
from konlpy.tag import Okt
import re

SENTIMENT_KEYWORDS = {
    # 긍정 감정
    "감동", "재밌", "흥미", "즐거움", "감탄", "유쾌", "웃김", "짜릿", "소름", "완성도", "몰입", "긴장", "설렘",
    "여운", "뭉클", "인상", "몰입감", "공감", "쾌감", "감성", "섬세", "소중함", "따뜻함", "유머",

    # 부정 감정
    "지루", "불쾌", "답답", "짜증", "억지", "피곤", "어색", "이질감", "혼란", "실망", "어설픔", "과장", "식상",

    # 평가 요소
    "완성도", "연출", "연기", "연출력", "서사", "전개", "구성", "마무리", "결말", "반전", "전율", "개연성",
    "OST", "음향", "미장센", "비주얼", "촬영", "연기력", "디테일", "분위기", "메시지", "떡밥", "회수", "상징",

    # 기타 키워드
    "이해", "의문", "공백", "불명확", "복선", "여운", "충격", "반전", "전율"
}
GENRE_KEYWORDS = {
    # 기본 장르
    "공포", "스릴러", "로맨스", "멜로", "액션", "범죄", "드라마", "판타지", "코미디", "애니메이션", "음악", "전쟁", "스포츠",

    # 세부 장르 / 분위기
    "잔혹", "심리", "감성", "청춘", "청소년", "추리", "다큐", "휴먼", "사극", "미스터리", "SF", "누아르", "재난", "모험",

    # 시대/배경
    "고전", "현대", "미래", "중세", "근현대", "1980년대", "1990년대", "전통", "외계", "지구", "한국", "헐리우드",

    # 형식/구조
    "옴니버스", "실화", "패러디", "풍자", "다큐", "웹툰", "게임원작", "노래", "영상미", "쿠키"

    # 연출/기술
    "연출", "연출력", "촬영", "미장센", "영상미", "화면", "음향", "OST", "편집", "분위기", "톤앤매너",

    # 서사/스토리
    "스토리", "서사", "전개", "구성", "줄거리", "개연성", "떡밥", "복선", "회수", "반전", "결말", "세계관",

    # 연기/캐릭터
    "연기", "연기력", "캐릭터", "배역", "몰입도", "몰입감", "감정선", "감정연기",

    # 메시지/의미
    "주제", "메시지", "교훈", "의미", "상징", "풍자", "비판", "사회성", "철학", "감성",

    # 완성도/작품성
    "완성도", "작품성", "완성", "퀄리티", "완성력", "수준", "작품", "걸작", "수작", "명작", "수준급",

    # 관객 반응
    "몰입", "감정", "여운", "감상", "충격", "전율", "재미", "지루함", "긴장감", "집중도", "피로감",

    # 비주얼 요소
    "비주얼", "CG", "특수효과", "배경", "미술", "의상", "소품", "세트", "분장"
}


class KeywordExtractor:
    def __init__(self):
        self.kw_model = KeyBERT(model="distiluse-base-multilingual-cased-v1")
        self.okt = Okt()

    def extract_keywords(self, text, top_n=7):
        cleaned = re.sub(r"[^\w\s+가-힣]", " ", text.lower())

        raw_keywords = self.kw_model.extract_keywords(
            cleaned,
            top_n=top_n * 3,
            stop_words=None
        )

        noun_keywords = []
        for word, score in raw_keywords:
            pos = self.okt.pos(word)
            if all(tag == "Noun" for _, tag in pos):
                if not self.is_probable_person_name(word):
                    if not self.is_weird_compound(word):
                        noun_keywords.append(word)

        noun_list = [w for w, t in self.okt.pos(text) if t == "Noun" and len(w) > 1]
        priority_keywords = [
            w for w in noun_list if w in SENTIMENT_KEYWORDS or w in GENRE_KEYWORDS
        ]

        final_keywords = []
        seen = set()
        for kw in priority_keywords + noun_keywords:
            if kw not in seen:
                final_keywords.append(kw)
                seen.add(kw)
            if len(final_keywords) >= top_n:
                break

        return final_keywords

    def is_weird_compound(self, word):
        if len(word) >= 8 and re.search(r"[가-힣]{3,}[가-힣]{3,}", word):
            return True
        return False

    def is_probable_person_name(self, word):
        if re.match(r"^[A-Z][a-z]+$", word): return True
        if re.match(r"^[A-Za-z]+$", word): return True
        if len(word) == 2 and all('\uac00' <= c <= '\ud7a3' for c in word): return True
        if len(word) == 3 and word[0] in "김이박최정강조윤장임한" and all('\uac00' <= c <= '\ud7a3' for c in word): return True
        return False
