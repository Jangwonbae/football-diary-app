package com.wbjang.footballdiary.data.mapper

import com.wbjang.footballdiary.data.api.dto.MatchDetailResponseDto
import com.wbjang.footballdiary.data.api.dto.MatchDto
import com.wbjang.footballdiary.domain.model.Match
import com.wbjang.footballdiary.domain.model.MatchCompetition
import com.wbjang.footballdiary.domain.model.MatchStatus
import com.wbjang.footballdiary.domain.model.MatchTeam

fun MatchDto.toDomain(): Match = Match(
    id          = id,
    utcDate     = utcDate,
    status      = MatchStatus.from(status),
    matchday    = matchday,
    competition = competition?.let {
        MatchCompetition(id = it.id, name = it.name, emblemUrl = it.emblem)
    },
    homeTeam = MatchTeam(
        id        = homeTeam.id,
        name      = homeTeam.name,
        shortName = homeTeam.shortName,
        crestUrl  = homeTeam.crest
    ),
    awayTeam = MatchTeam(
        id        = awayTeam.id,
        name      = awayTeam.name,
        shortName = awayTeam.shortName,
        crestUrl  = awayTeam.crest
    ),
    homeScore = score.fullTime.home,
    awayScore = score.fullTime.away
)

// MatchDetailResponseDto는 MatchDto와 공통 필드를 그대로 갖고 있으므로
// MatchDto를 경유해 toDomain()을 재사용합니다.
fun MatchDetailResponseDto.toMatchDomain(): Match = MatchDto(
    id          = id,
    utcDate     = utcDate,
    status      = status,
    matchday    = matchday,
    competition = competition,
    homeTeam    = homeTeam,
    awayTeam    = awayTeam,
    score       = score
).toDomain()
