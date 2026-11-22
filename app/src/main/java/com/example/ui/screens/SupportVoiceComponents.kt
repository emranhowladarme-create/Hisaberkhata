package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.support.SUPPORT_HELPLINE_NUMBER
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.HisabGreenDeep
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.SupportChatExchange

@Composable
fun SupportTextReplyCard(
    exchange: SupportChatExchange,
    onDialHelpline: () -> Unit,
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HisabGreenBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("support_ai_response_card_${exchange.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(HisabGreenSoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "হিসাবের খাতা AI সাপোর্ট",
                            tint = HisabGreenDark,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হিসাবের খাতা AI সাপোর্ট",
                        color = HisabGreenDeep,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (exchange.isStreaming) {
                    Surface(
                        color = HisabGreenSoft,
                        shape = CircleShape,
                    ) {
                        Text(
                            text = "লিখছে...",
                            color = HisabGreenDark,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                } else {
                    Text(
                        text = exchange.timeLabel,
                        color = InkSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            // Detected problem category & verified KB indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = HisabGreenTint,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, HisabGreenBorder),
                ) {
                    Text(
                        text = exchange.detectedCategory.labelBn,
                        color = HisabGreenDeep,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                if (exchange.kbEntryTitles.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFEF7DC),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFF3D478)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF78350F),
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "যাচাইকৃত তথ্য",
                                color = Color(0xFF78350F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

            if (exchange.isStreaming && exchange.replyText.isBlank() && exchange.bulletPoints.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_ai_loading_indicator"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        color = HisabGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "যাচাইকৃত তথ্য থেকে উত্তর প্রস্তুত করা হচ্ছে...",
                        color = InkSecondary,
                        fontSize = 13.sp,
                    )
                }
            } else {
                if (exchange.replyText.isNotBlank()) {
                    Text(
                        text = exchange.replyText,
                        color = InkPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                }

                exchange.bulletPoints.forEach { bullet ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HisabGreen,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bullet,
                            color = InkPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (!exchange.isStreaming && exchange.needsHumanSupport) {
                Surface(
                    color = WarningAmberBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("human_support_notice_${exchange.id}"),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "মানব সাপোর্ট সহায়তা প্রয়োজন হলে আমাদের অফিশিয়াল হেল্পলাইনে যোগাযোগ করুন:",
                            color = Color(0xFF102A1D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "হেল্পলাইন নম্বর: $SUPPORT_HELPLINE_NUMBER",
                            color = HisabGreenDeep,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Button(
                            onClick = onDialHelpline,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HisabGreenDark,
                                contentColor = PureWhite,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("escalation_helpline_button_${exchange.id}"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "হেল্পলাইনে যোগাযোগ করুন",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
