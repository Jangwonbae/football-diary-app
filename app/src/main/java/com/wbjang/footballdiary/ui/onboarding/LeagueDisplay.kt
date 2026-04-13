package com.wbjang.footballdiary.ui.onboarding

import androidx.annotation.StringRes
import com.wbjang.footballdiary.R
import com.wbjang.footballdiary.domain.model.League

@StringRes
fun League.nameRes(): Int = when (code) {
    "PL"  -> R.string.league_premier_league
    "PD"  -> R.string.league_la_liga
    "BL1" -> R.string.league_bundesliga
    "SA"  -> R.string.league_serie_a
    "FL1" -> R.string.league_ligue_1
    else  -> R.string.app_name
}

@StringRes
fun League.countryRes(): Int = when (code) {
    "PL"  -> R.string.country_england
    "PD"  -> R.string.country_spain
    "BL1" -> R.string.country_germany
    "SA"  -> R.string.country_italy
    "FL1" -> R.string.country_france
    else  -> R.string.app_name
}
