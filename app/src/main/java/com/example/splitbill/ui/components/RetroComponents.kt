package com.example.splitbill.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitbill.ui.theme.RetroTheme

fun verticalGradient(top: Color, bottom: Color): Brush {
    return Brush.verticalGradient(colors = listOf(top, bottom))
}

@Composable
fun RetroTitleBar(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(verticalGradient(RetroTheme.colors.xpBlue, RetroTheme.colors.xpBlueDark))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun RetroPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .shadow(4.dp, RetroTheme.shapes.panel)
            .clip(RetroTheme.shapes.panel)
            .background(RetroTheme.colors.panelGray)
            .border(1.dp, RetroTheme.colors.silverDark, RetroTheme.shapes.panel)
            .padding(12.dp),
        content = content
    )
}

@Composable
fun RetroButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = if (!enabled) Color.Gray else if (isPressed) RetroTheme.colors.xpBlueDark else RetroTheme.colors.xpBlue
    val bottomColor = if (!enabled) Color.DarkGray else if (isPressed) RetroTheme.colors.xpBlue else RetroTheme.colors.xpBlueDark

    Box(
        modifier = modifier
            .clip(RetroTheme.shapes.beveled)
            .background(verticalGradient(topColor, bottomColor))
            .border(1.dp, RetroTheme.colors.silverDark, RetroTheme.shapes.beveled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.offset(y = if (isPressed && enabled) 1.dp else 0.dp)
        )
    }
}

@Composable
fun RetroSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = if (!enabled) Color.LightGray else if (isPressed) RetroTheme.colors.panelGray else RetroTheme.colors.silver
    val bottomColor = if (!enabled) Color.Gray else if (isPressed) RetroTheme.colors.silver else RetroTheme.colors.panelGray

    Box(
        modifier = modifier
            .clip(RetroTheme.shapes.beveled)
            .background(verticalGradient(topColor, bottomColor))
            .border(1.dp, RetroTheme.colors.silverDark, RetroTheme.shapes.beveled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.colors.textDark,
            modifier = Modifier.offset(y = if (isPressed && enabled) 1.dp else 0.dp)
        )
    }
}

@Composable
fun RetroOutlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        Text(text = label, color = RetroTheme.colors.textDark, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(color = RetroTheme.colors.textDark, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RetroTheme.shapes.beveled)
                .background(Color.White)
                .border(1.dp, RetroTheme.colors.silverDark, RetroTheme.shapes.beveled)
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RetroComponentsPreview() {
    RetroTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RetroTitleBar(title = "SplitBill Messenger")
            
            RetroPanel {
                Text("This is a Retro Panel", color = RetroTheme.colors.textDark)
                Spacer(modifier = Modifier.height(8.dp))
                RetroOutlineField(
                    value = "Input text",
                    onValueChange = {},
                    label = "Retro Outline Field"
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RetroButton(text = "Primary", onClick = {})
                RetroSecondaryButton(text = "Secondary", onClick = {})
            }
        }
    }
}
