package com.example.fallmon.presentation

enum class FallType(val strFall: String){
    DROP_ATTACK("drop attack"),      // 쓰러짐
    NON_FALL("non fall"),            // 비낙상
    SLIPPING("slipping"),            // 미끄러짐
    STAND_PUSH("stand push"),        // 넘어짐
    SUNKEN_FLOOR("sunken floor"),    // 헛디딤
    FALL("fall"),                    // 단순 낙상
}
data class FallHistory (
    val id: String,
    val fallType: FallType,
    val createdAt: java.util.Date
)
