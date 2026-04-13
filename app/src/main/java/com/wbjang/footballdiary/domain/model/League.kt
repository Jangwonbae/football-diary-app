package com.wbjang.footballdiary.domain.model

data class League(
    val code: String,       // "PL", "PD", "BL1", "SA", "FL1"
    val emblemUrl: String,
    val teams: List<Team> = emptyList(),
    val isExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
