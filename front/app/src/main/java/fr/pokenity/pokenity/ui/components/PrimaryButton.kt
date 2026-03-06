package fr.pokenity.pokenity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import fr.pokenity.pokenity.ui.theme.PrimaryButtonOrange
import fr.pokenity.pokenity.ui.theme.PrimaryButtonText
import fr.pokenity.pokenity.ui.theme.PrimaryButtonYellow

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = PrimaryButtonYellow,
        contentColor = PrimaryButtonText,
        disabledContainerColor = PrimaryButtonYellow,
        disabledContentColor = PrimaryButtonText
    ),
    border: BorderStroke = BorderStroke(width = 2.dp, color = PrimaryButtonOrange),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        border = border,
        content = content
    )
}
