import json
import re
import unicodedata
import uuid
import os

def clean_persian_text(text):
    if not text:
        return ''
    norm = unicodedata.normalize('NFC', text)
    norm = norm.replace('\u200B', '').replace('\u064A', '\u06CC').replace('\u0649', '\u06CC').replace('\u0643', '\u06A9')
    norm = re.sub(r'\u200C+', '\u200C', norm)
    norm = norm.strip(' \t\n\r\u200C\u200B')
    return re.sub(r'\s+', ' ', norm)

def clean_spanish_text(text):
    if not text:
        return ''
    norm = unicodedata.normalize('NFC', text)
    norm = norm.strip(' \t\n\r\u200B')
    return re.sub(r'\s+', ' ', norm)

def canonical_key(text):
    if not text:
        return ''
    cleaned = clean_persian_text(text) if re.search(r'[\u0600-\u06FF]', text) else clean_spanish_text(text)
    return cleaned.lower()

backup_path = 'docs/yadin-custom-backup.json'
if not os.path.exists(backup_path):
    print(f"File not found: {backup_path}")
    exit(1)

with open(backup_path, 'r', encoding='utf-8') as f:
    backup = json.load(f)

data = backup['data']
categories = data['categories']
concepts = data['concepts']
contents = data['contents']
concept_categories = data.get('conceptCategories', [])
diff_states = data['difficultyStates']

general_cat_id = 'bf004d79-9f09-53db-87c9-830fd35db247'
cat_ids = {c['id']: c for c in categories}
if general_cat_id not in cat_ids:
    categories.append({
        'id': general_cat_id,
        'name': 'عمومی',
        'sortOrder': 11,
        'isDefault': False
    })
    cat_ids[general_cat_id] = categories[-1]

categories.sort(key=lambda x: x.get('sortOrder', 0))

for c in concepts:
    if not c.get('categoryId') or c.get('categoryId') not in cat_ids:
        c['categoryId'] = general_cat_id

contents_by_concept = {}
for c in contents:
    contents_by_concept.setdefault(c['conceptId'], []).append(c)

new_contents = []

for c in concepts:
    cid = c['id']
    c_list = contents_by_concept.get(cid, [])
    
    es_items = [item for item in c_list if item.get('languageCode') == 'es']
    fa_items = [item for item in c_list if item.get('languageCode') == 'fa']
    
    if cid == '5bd6d29c-457d-47ef-830a-86faf52104be':
        es_items = [{
            'id': es_items[0]['id'] if es_items else str(uuid.uuid4()),
            'conceptId': cid,
            'languageCode': 'es',
            'text': 'el té',
            'canonicalKey': 'el té',
            'note': 'توضیح: در اسپانیایی قهوه el café است',
            'translationIndex': 0
        }]
        fa_items = [{
            'id': str(uuid.uuid4()),
            'conceptId': cid,
            'languageCode': 'fa',
            'text': 'چای',
            'canonicalKey': 'چای',
            'note': None,
            'translationIndex': 0
        }]
    elif cid == '5b4f0053-f11c-4b51-975c-33a0ec095e4e':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'abrelatas']
    elif cid == '65c7283b-69a0-4e7e-a41b-0f49c391b14b':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'mi marido dijo']
    elif cid == '3a54613a-a49c-4dc8-aa4e-04c29e5f0980':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'el trabajo de tus sueños']
    elif cid == '9130500c-088b-44ae-af99-e36de0b9caca':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'imagina el trabajo de tus sueños']
    elif cid == 'e2f2d785-d0d1-45f8-b1f5-1a8d27107272':
        clean_fa_texts = ['دیگری', 'یک تای دیگر', 'یک تای دیگر (مذکر، مؤنث)']
        fa_items = [{
            'id': str(uuid.uuid4()),
            'conceptId': cid,
            'languageCode': 'fa',
            'text': t,
            'canonicalKey': canonical_key(t),
            'note': None,
            'translationIndex': idx
        } for idx, t in enumerate(clean_fa_texts)]
    elif cid == '71191cb5-2bbe-47f5-a290-9cf7e76a7189':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'espero que no nos perdamos']
    elif cid == '289f3435-bcf2-42c1-aac6-e3adf286f57d':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'cintenido']
    elif cid == '7b819469-39fe-4c15-ab99-e2c7886cda6d':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'intentda']
    elif cid == 'eb3fca42-3355-438c-bde8-6bf77842f9f9':
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'confio']
    elif cid == '23e21e17-b35b-4bc8-9c9e-79f47f27f90d':
        if es_items:
            es_items[0]['text'] = 'Prioridad para peatones a 20 km/h'
            es_items[0]['canonicalKey'] = 'prioridad para peatones a 20 km/h'
        fa_items = [item for item in fa_items if item.get('text', '').strip().lower() != 'h']
    elif cid == '22e19b64-cbce-4b8d-9902-4d3e0432cc4b':
        if es_items:
            es_items[0]['text'] = 'Dispositivo V-16'
            es_items[0]['canonicalKey'] = 'dispositivo v-16'
            es_items[0]['note'] = 'چراغ هشدار اضطراری V-16'
        fa_items = [{
            'id': str(uuid.uuid4()),
            'conceptId': cid,
            'languageCode': 'fa',
            'text': 'چراغ هشدار V-16',
            'canonicalKey': 'چراغ هشدار v-16',
            'note': None,
            'translationIndex': 0
        }]
    elif cid == 'a25abedf-b936-4fe5-a878-167cc4eaaa8f':
        if es_items:
            es_items[0]['text'] = 'Permisos de conducir (A1, A2, A, B)'
            es_items[0]['canonicalKey'] = 'permisos de conducir (a1, a2, a, b)'
            es_items[0]['note'] = 'انواع گواهینامه رانندگی'
        fa_items = [{
            'id': str(uuid.uuid4()),
            'conceptId': cid,
            'languageCode': 'fa',
            'text': 'انواع گواهینامه رانندگی (A1، A2، A، B)',
            'canonicalKey': 'انواع گواهینامه رانندگی (a1، a2، a، b)',
            'note': None,
            'translationIndex': 0
        }]
    else:
        cleaned_fa = []
        for item in fa_items:
            t = clean_persian_text(item.get('text', ''))
            if t == '؟' or len(t) == 0:
                continue
            item['text'] = t
            item['canonicalKey'] = canonical_key(t)
            cleaned_fa.append(item)
        fa_items = cleaned_fa

    for item in es_items:
        t = clean_spanish_text(item.get('text', ''))
        item['text'] = t
        item['canonicalKey'] = canonical_key(t)
        if item.get('note'):
            item['note'] = clean_persian_text(item['note'])

    for idx, item in enumerate(fa_items):
        item['translationIndex'] = idx
    for idx, item in enumerate(es_items):
        item['translationIndex'] = idx

    new_contents.extend(es_items)
    new_contents.extend(fa_items)

existing_cc_pairs = set((cc['conceptId'], cc['categoryId']) for cc in concept_categories)
all_cc = list(concept_categories)
for c in concepts:
    cat_id = c.get('categoryId')
    if cat_id and (c['id'], cat_id) not in existing_cc_pairs:
        all_cc.append({'conceptId': c['id'], 'categoryId': cat_id})
        existing_cc_pairs.add((c['id'], cat_id))

seed_payload = {
    "schemaVersion": 2,
    "exportedAt": "2026-10-16T07:15:00Z",
    "backupType": "FULL",
    "backupTypes": ["VOCABULARY"],
    "payloads": {
        "VOCABULARY": {
            "schemaVersion": 2,
            "exportedAt": "2026-10-16T07:15:00Z",
            "backupType": "VOCABULARY",
            "categories": categories,
            "concepts": concepts,
            "contents": new_contents,
            "conceptCategories": all_cc,
            "difficultyStates": diff_states
        }
    }
}

target_json_path = 'app/src/main/assets/seed/vocabulary_seed.json'
os.makedirs(os.path.dirname(target_json_path), exist_ok=True)
with open(target_json_path, 'w', encoding='utf-8') as f:
    json.dump(seed_payload, f, ensure_ascii=False)

print(f"Successfully generated seed data:")
print(f" - JSON: {target_json_path} ({len(concepts)} concepts, {len(new_contents)} contents, {len(diff_states)} difficulty states)")
