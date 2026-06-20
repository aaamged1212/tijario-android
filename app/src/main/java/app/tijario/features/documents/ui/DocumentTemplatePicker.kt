package app.tijario.features.documents.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.features.documents.template.DocumentTemplateRegistry

@Composable
fun DocumentTemplatePicker(
    selectedTemplateId: String,
    onTemplateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("القالب", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DocumentTemplateRegistry.templates.forEach { template ->
                val selected = template.id == selectedTemplateId
                val colors = if (selected) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
                if (selected) {
                    Button(
                        onClick = { onTemplateSelected(template.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = colors,
                    ) {
                        Text(template.name.removePrefix("Tijario "), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
                    }
                } else {
                    OutlinedButton(
                        onClick = { onTemplateSelected(template.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = colors,
                    ) {
                        Text(template.name.removePrefix("Tijario "), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
    }
}
