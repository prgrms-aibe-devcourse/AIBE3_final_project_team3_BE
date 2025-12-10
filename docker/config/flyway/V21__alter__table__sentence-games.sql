DELETE
FROM sentence_games;

ALTER TABLE sentence_games
    ADD COLUMN feedbacks JSON NOT NULL;

INSERT INTO sentence_games (original_content, corrected_content, feedbacks, created_at)
VALUES --
('I go to the park yesterday',
'I went to the park yesterday',
'[{"tag":"Grammar","problem":"go","correction":"went","extra":"과거 시제에서는 went를 사용해야 합니다."}]',
NOW()),
('She dont like apple',
'She does not like apples',
'['
    '{"tag":"Grammar","problem":"dont","correction":"does not","extra":"3인칭 단수 주어는 does not를 사용해야 합니다."},'
    '{"tag":"Vocabulary","problem":"apple","correction":"apples","extra":"일반적인 의미에서는 복수형을 사용해야 자연스럽습니다."}'
    ']',
NOW()),
('I am boring in this class',
'I am bored in this class',
'['
    '{"tag":"Grammar","problem":"boring","correction":"bored","extra":"감정 상태를 나타낼 때는 bored를 사용해야 합니다."}'
    ']',
NOW()),
('I want to eat ramen today',
'I want to eat ramen today',
'['
    '{"tag":"Meaning","problem":"ramen","correction":"ramen","extra":"문법적 오류는 없으며 표현 선택 문제일 수 있습니다."}'
    ']',
NOW()),
('I go to school without pen',
'I went to school without a pen',
'['
    '{"tag":"Grammar","problem":"go","correction":"went","extra":"과거 시제에는 went를 사용해야 합니다."},'
    '{"tag":"Grammar","problem":"pen","correction":"a pen","extra":"단수 가산명사 앞에는 관사가 필요합니다."}'
    ']',
NOW()),
('He eat breakfast every morning',
'He eats breakfast every morning',
'['
    '{"tag":"Grammar","problem":"eat","correction":"eats","extra":"3인칭 단수 주어에는 동사에 -s가 필요합니다."}'
    ']',
NOW()),
('She not understand the problem',
'She does not understand the problem',
'['
    '{"tag":"Grammar","problem":"not understand","correction":"does not understand","extra":"부정문은 do/does not + 동사원형 구조를 사용합니다."}'
    ']',
NOW()),
('I am very happy because I got a gift',
'I am very happy because I received a gift',
'['
    '{"tag":"Vocabulary","problem":"got","correction":"received","extra":"received가 문맥상 더 자연스러운 표현입니다."}'
    ']',
NOW()),
('I study English every day for improve',
'I study English every day to improve',
'['
    '{"tag":"Grammar","problem":"for improve","correction":"to improve","extra":"목적을 나타낼 때는 to + 동사원형을 사용합니다."}'
    ']',
NOW()),
('We go mountain tomorrow',
'We will go to the mountain tomorrow',
'['
    '{"tag":"Grammar","problem":"go","correction":"will go","extra":"미래 계획에는 will 또는 be going to를 사용합니다."},'
    '{"tag":"Grammar","problem":"mountain","correction":"the mountain","extra":"특정 장소를 의미할 때는 the를 사용할 수 있습니다."}'
    ']',
NOW());