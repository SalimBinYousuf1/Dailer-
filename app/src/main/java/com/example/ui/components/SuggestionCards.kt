package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AvatarMonsterType
import com.example.ui.ContactSuggestion
import com.example.ui.theme.CardGradientEnd
import com.example.ui.theme.CardGradientStart

@Composable
fun SuggestionCards(
    card1: ContactSuggestion,
    card2: ContactSuggestion,
    onCardClick: (ContactSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // First card - Soft pastel pink/lavender gradient
        GradientContactCard(
            suggestion = card1,
            onClick = { onCardClick(card1) },
            modifier = Modifier.testTag("contact_suggestion_card_1")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Second card - White/light surface with subtle gray shadow
        LightContactCard(
            suggestion = card2,
            onClick = { onCardClick(card2) },
            modifier = Modifier.testTag("contact_suggestion_card_2")
        )
    }
}

@Composable
fun GradientContactCard(
    suggestion: ContactSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x229C7BA8),
                ambientColor = Color(0x18F6DEE8)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CardGradientStart,
                        CardGradientEnd
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue/purple monster circular avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2D6F6)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if (suggestion.avatarType == AvatarMonsterType.BLUE_PURPLE) {
                            R.drawable.avatar_monster_blue
                        } else {
                            R.drawable.avatar_monster_pink
                        }
                    ),
                    contentDescription = "Contact avatar",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = suggestion.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = suggestion.number,
                    color = Color(0xF5FFFFFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LightContactCard(
    suggestion: ContactSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x18000000),
                ambientColor = Color(0x0C000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pink monster circular avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFBE8EE)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if (suggestion.avatarType == AvatarMonsterType.PINK) {
                            R.drawable.avatar_monster_pink
                        } else {
                            R.drawable.avatar_monster_blue
                        }
                    ),
                    contentDescription = "Contact avatar",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = suggestion.name,
                    color = Color(0xFF1E1E24),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = suggestion.number,
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
