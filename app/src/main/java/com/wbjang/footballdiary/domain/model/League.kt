package com.wbjang.footballdiary.domain.model

data class League(
    val code: String,       // "PL", "PD", "BL1", "SA", "FL1"
    val name: String,       // "프리미어리그", "라리가" 등
    val country: String,    // "잉글랜드", "스페인" 등
    val emblemUrl: String,
    val teams: List<Team> = emptyList(),
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
