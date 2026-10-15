import csv
import json
import uuid
import re
import unicodedata
import os

def clean_text(text):
    if not text:
        return ''
    norm = unicodedata.normalize('NFC', text)
    norm = norm.replace('\u200B', '').replace('\u064A', '\u06CC').replace('\u0649', '\u06CC').replace('\u0643', '\u06A9')
    norm = re.sub(r'\u200C+', '\u200C', norm)
    norm = norm.strip(' \t\n\r\u200C\u200B')
    return re.sub(r'\s+', ' ', norm)

def canonical_key(text):
    return clean_text(text).lower()

CATEGORIES = [
    {'id': 'bbcda6ac-8936-4304-accf-3340063069d9', 'name': 'اصطلاح', 'sortOrder': 0, 'isDefault': False},
    {'id': 'e250e0a0-17d4-45b3-8098-bc15d8182ec1', 'name': 'افعال پایه', 'sortOrder': 1, 'isDefault': False},
    {'id': '5b212ca4-8e95-49bd-b9b0-9ab422ffdfec', 'name': 'بدن و پزشکی', 'sortOrder': 2, 'isDefault': False},
    {'id': '4680aca0-e916-4c35-b8d4-f7ee20e662cb', 'name': 'حمل و نقل', 'sortOrder': 3, 'isDefault': False},
    {'id': '5c5103c7-3de0-44cb-9b16-c780ff8ca59d', 'name': 'خانواده', 'sortOrder': 4, 'isDefault': False},
    {'id': '9e2eb307-47ea-4ee3-b270-78fbb1482688', 'name': 'سفر', 'sortOrder': 5, 'isDefault': False},
    {'id': '476656a7-5638-4b73-9b7c-f44e17fae6ae', 'name': 'غذا', 'sortOrder': 6, 'isDefault': False},
    {'id': 'ae0e9f97-c1d1-4211-9fe4-3bf185695de8', 'name': 'مکالمه روزمره', 'sortOrder': 7, 'isDefault': False},
    {'id': '883ee2e9-039c-423a-9211-380862f09d99', 'name': 'وسایل خانه', 'sortOrder': 8, 'isDefault': False},
    {'id': '2e946eb5-3705-46d6-83f0-d7a85409cc46', 'name': 'کار و شغل', 'sortOrder': 9, 'isDefault': False},
    {'id': '7f1e405a-8b1e-4c7a-9a0d-3e6f9b1c2d3e', 'name': 'عمومی', 'sortOrder': 10, 'isDefault': False},
    {'id': '8a2d516b-9c2f-4d8b-ab1e-4f7a0c2d3e4f', 'name': 'اعداد و تاریخ', 'sortOrder': 11, 'isDefault': False},
]
cat_map = {c['name']: c['id'] for c in CATEGORIES}

DIFF_MAP = {
    'آسان': 'EASY',
    'متوسط': 'MEDIUM',
    'سخت': 'HARD',
    'خیلی سخت': 'VERY_HARD'
}

concepts = []
contents = []
difficulty_states = []

csv_path = 'docs/vocabulary.csv'
if not os.path.exists(csv_path):
    print(f"File not found: {csv_path}")
    exit(1)

with open(csv_path, mode='r', encoding='utf-8') as f:
    reader = csv.reader(f)
    next(reader) # skip header
    now_ts = 1792000000000
    for idx, row in enumerate(reader):
        spanish = clean_text(row[1])
        persian_raw = row[2]
        cat_name = clean_text(row[3])
        diff_normal_str = clean_text(row[4])
        diff_reverse_str = clean_text(row[5])
        note_str = clean_text(row[6])
        concept_id = row[7].strip()
        
        cat_id = cat_map.get(cat_name, cat_map['عمومی'])
        is_phrase = (' ' in spanish) or ('-' in spanish) or ('/' in spanish)
        entry_type = 'PHRASE' if is_phrase else 'WORD'
        
        concepts.append({
            'id': concept_id,
            'entryType': entry_type,
            'categoryId': cat_id,
            'active': True,
            'createdAt': now_ts,
            'updatedAt': now_ts
        })
        
        # Spanish content
        contents.append({
            'id': str(uuid.uuid4()),
            'conceptId': concept_id,
            'languageCode': 'es',
            'text': spanish,
            'canonicalKey': canonical_key(spanish),
            'note': note_str if note_str else None,
            'translationIndex': 0
        })
        
        # Persian contents
        parts = [clean_text(p) for p in persian_raw.split('،') if clean_text(p)]
        seen = set()
        unique_parts = []
        for p in parts:
            if p not in seen:
                seen.add(p)
                unique_parts.append(p)
        if not unique_parts:
            unique_parts = [clean_text(persian_raw)]
            
        for p_idx, p_text in enumerate(unique_parts):
            contents.append({
                'id': str(uuid.uuid4()),
                'conceptId': concept_id,
                'languageCode': 'fa',
                'text': p_text,
                'canonicalKey': canonical_key(p_text),
                'note': None,
                'translationIndex': p_idx
            })
            
        # Difficulty states
        diff_normal = DIFF_MAP.get(diff_normal_str, 'EASY')
        diff_reverse = DIFF_MAP.get(diff_reverse_str, 'EASY')
        
        difficulty_states.append({
            'id': str(uuid.uuid4()),
            'conceptId': concept_id,
            'direction': 'NORMAL',
            'current': diff_normal
        })
        difficulty_states.append({
            'id': str(uuid.uuid4()),
            'conceptId': concept_id,
            'direction': 'REVERSE',
            'current': diff_reverse
        })

payload = {
    "schemaVersion": 2,
    "exportedAt": "2026-10-15T05:51:20Z",
    "backupType": "FULL",
    "backupTypes": ["VOCABULARY"],
    "payloads": {
        "VOCABULARY": {
            "schemaVersion": 2,
            "exportedAt": "2026-10-15T05:51:20Z",
            "backupType": "VOCABULARY",
            "categories": CATEGORIES,
            "concepts": concepts,
            "contents": contents,
            "difficultyStates": difficulty_states
        }
    }
}

target_json_path = 'app/src/main/assets/seed/vocabulary_seed.json'
os.makedirs(os.path.dirname(target_json_path), exist_ok=True)
with open(target_json_path, 'w', encoding='utf-8') as f:
    json.dump(payload, f, ensure_ascii=False)

target_csv_path = 'app/src/main/assets/seed/vocabulary.csv'
import shutil
shutil.copyfile(csv_path, target_csv_path)

print(f"Successfully generated seed data:")
print(f" - JSON: {target_json_path} ({len(concepts)} concepts, {len(contents)} contents, {len(difficulty_states)} difficulty states)")
print(f" - CSV: {target_csv_path}")
