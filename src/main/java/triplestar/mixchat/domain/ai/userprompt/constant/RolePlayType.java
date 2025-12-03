package triplestar.mixchat.domain.ai.userprompt.constant;

public enum RolePlayType {

    DAILY_SERVICE("일상 & 서비스 상황"),
    WORK_COMPANY("회사/직장 상황"),
    SCHOOL_ACADEMIC("학교/학습 상황"),
    TRAVEL_IMMIGRATION("여행 & 공항/이민 상황"),
    HOSPITAL_EMERGENCY("병원 & 긴급 상황"),
    ONLINE_DIGITAL("온라인/디지털 상황"),
    RELATION_EMOTION("인간관계 & 감정/갈등 상황"),
    META_LEARNING("영어 학습 앱 특화 “메타 상황”"),
    FREE_TALK("자유 대화 (Free Talk)")
    ;

    private final String description;

    RolePlayType(String description) {
        this.description = description;
    }
}