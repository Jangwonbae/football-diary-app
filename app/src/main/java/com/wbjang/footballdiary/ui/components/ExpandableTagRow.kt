package com.wbjang.footballdiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.wbjang.footballdiary.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableTagRow(tags: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    ContextualFlowRow(
        itemCount = if (isExpanded) tags.size + 1 else tags.size,
        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
        overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
            expandIndicator = {
                val remaining = totalItemCount - shownItemCount
                AssistChip(
                    onClick = { isExpanded = true },
                    label = {
                        Text(
                            text = "+$remaining",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            },
            collapseIndicator = {}
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xsmall)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.write_review_tag_vertical_gap))
    ) { index ->
        if (index == tags.size) {
            TextButton(onClick = { isExpanded = false }) {
                Text(
                    text = stringResource(R.string.tag_collapse),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        } else {
            AssistChip(
                onClick = {},
                label = { Text(text = tags[index], style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
