package app.tijario.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.DocumentType
import app.tijario.domain.Validation
import app.tijario.ui.state.DocumentFormState
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.net.URL
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PreviewLocale = Locale("ar", "SA")

@Composable
fun ModernDocumentPreview(
    documentType: DocumentType,
    form: DocumentFormState,
    businessSettings: BusinessSettings?,
    customerCity: String?,
    productDescription: String?,
    modifier: Modifier = Modifier,
) {
    val currency = businessSettings?.currency ?: "SAR"
    val quantity = Validation.parsePositiveInt(form.quantity)?.toDouble() ?: 0.0
    val unitPrice = Validation.parseNonNegativeMoney(form.unitPrice) ?: 0.0
    val subtotal = quantity * unitPrice
    val discount = Validation.parseNonNegativeMoney(form.discount) ?: 0.0
    val extraFees = Validation.parseNonNegativeMoney(form.extraFees) ?: 0.0
    val total = subtotal - discount + extraFees
    val hasItem = form.itemName.isNotBlank() || !productDescription.isNullOrBlank()
    val emptyRows = max(0, 5 - if (hasItem) 1 else 0)
    val issueDate = remember {
        DateTimeFormatter.ofPattern("d MMM yyyy", PreviewLocale).format(LocalDate.now())
    }
    val documentTitle = if (documentType == DocumentType.Invoice) "فاتورة" else "عرض سعر"
    val documentNumber = if (documentType == DocumentType.Invoice) "INV-XXXX" else "QT-XXXX"
    val termsText = form.terms.ifBlank { businessSettings?.termsText.orEmpty() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(794f / 1123f),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            PatternBand(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = businessSettings?.businessName?.ifBlank { null } ?: "Company Name",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 19.sp,
                        )
                        Text(
                            text = "شعارك التجاري",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 7.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00796B)),
                        contentAlignment = Alignment.Center,
                    ) {
                        PreviewLogoImage(logoUrl = businessSettings?.logoUrl)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        SmallMutedText(businessSettings?.city ?: "عنوان المتجر")
                        SmallMutedText(businessSettings?.country ?: "المدينة، الدولة")
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color(0xFF9FD4DF)),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        SmallMutedText("واتساب ${businessSettings?.whatsappNumber ?: "0000000000"}")
                        SmallMutedText("العملة $currency")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "بيانات العميل:",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = form.customerName.ifBlank { "غير محدد" },
                            color = Color(0xFF15191B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = form.customerWhatsapp.ifBlank { "-" },
                            color = Color(0xFF15191B),
                            fontSize = 8.sp,
                            textAlign = TextAlign.Start,
                        )
                        if (!customerCity.isNullOrBlank()) {
                            Text(
                                text = customerCity,
                                color = Color(0xFF15191B),
                                fontSize = 8.sp,
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "$documentTitle #$documentNumber",
                            color = Color(0xFF00796B),
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "التاريخ: $issueDate",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (documentType == DocumentType.Invoice) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .border(1.dp, Color(0xFFC7822B), RoundedCornerShape(0.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = "غير مدفوع",
                                    color = Color(0xFF9A5D12),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PreviewTableHeader()
                    if (hasItem) {
                        PreviewItemRow(
                            index = 1,
                            name = form.itemName.ifBlank { "غير محدد" },
                            description = productDescription,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            lineTotal = subtotal,
                            currency = currency,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .background(Color(0xFFFBFBFB)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "لا توجد بنود بعد. أضف بندًا واحدًا على الأقل لعرض المعاينة.",
                                color = Color(0xFF4D5558),
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    repeat(emptyRows) {
                        PreviewEmptyRow()
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.45f)
                            .height(82.dp)
                            .background(Color(0xFFB8DDE0))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = form.notes.ifBlank {
                                "يرجى مراجعة تفاصيل المستند، ويمكن إضافة ملاحظات الدفع أو التسليم هنا."
                            },
                            color = Color(0xFF253235),
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                        )
                        if (termsText.isNotBlank()) {
                            Text(
                                text = termsText,
                                color = Color(0xFF253235),
                                fontSize = 7.sp,
                                lineHeight = 9.sp,
                            )
                        }
                        Text(
                            text = "شكرًا لتعاملكم معنا",
                            color = Color(0xFF253235),
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(0.95f)
                            .height(82.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            TotalLabel("المجموع الفرعي")
                            TotalLabel("الرسوم")
                            TotalLabel("الخصم")
                            TotalLabel("الإجمالي", bold = true)
                        }
                        Column(
                            modifier = Modifier
                                .width(72.dp)
                                .fillMaxHeight(),
                        ) {
                            TotalValue(formatPreviewAmount(subtotal, currency))
                            TotalValue(formatPreviewAmount(extraFees, currency))
                            TotalValue("-${formatPreviewAmount(discount, currency)}")
                            TotalValue(formatPreviewAmount(total, currency), bold = true)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .border(width = 0.dp, color = Color.Transparent)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF7FAEB8)),
                            )
                        }
                        Text(
                            text = "التوقيع",
                            color = Color(0xFF4D5558),
                            fontSize = 7.sp,
                        )
                    }
                }
            }

            PatternBand(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "تم الإنشاء بواسطة Tijario",
                        color = Color(0xFF253235),
                        fontSize = 8.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternBand(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.background(Color(0xFFA8DBE3)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeWidth = 86.dp.toPx()
            val stroke = 26.dp.toPx()
            for (index in -4..12) {
                val x = index * stripeWidth
                drawLine(
                    color = Color.White.copy(alpha = 0.34f),
                    start = Offset(x, size.height),
                    end = Offset(x + stripeWidth, 0f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = Color(0xFF108191).copy(alpha = 0.14f),
                    start = Offset(x + stripeWidth * 0.55f, size.height),
                    end = Offset(x + stripeWidth * 1.55f, 0f),
                    strokeWidth = stroke,
                )
            }
        }
        content()
    }
}

@Composable
private fun SmallMutedText(text: String) {
    Text(
        text = text,
        color = Color(0xFF4B5357),
        fontSize = 7.sp,
        lineHeight = 9.sp,
    )
}

@Composable
private fun PreviewTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(0xFF55B2C8))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableCell("#", weight = 0.35f, color = Color.White, bold = true, textAlign = TextAlign.Center)
        TableCell("وصف العمل", weight = 2.1f, color = Color.White, bold = true)
        TableCell("الكمية", weight = 0.7f, color = Color.White, bold = true, textAlign = TextAlign.Center)
        TableCell("سعر الوحدة", weight = 1.1f, color = Color.White, bold = true, textAlign = TextAlign.Center)
        TableCell("الإجمالي", weight = 1.1f, color = Color.White, bold = true, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PreviewItemRow(
    index: Int,
    name: String,
    description: String?,
    quantity: Double,
    unitPrice: Double,
    lineTotal: Double,
    currency: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(Color(0xFFFBFBFB))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(0.35f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFFC8EDF3)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString().padStart(2, '0'),
                    color = Color(0xFF2B4B53),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(2.1f)
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = name,
                color = Color(0xFF2B2F31),
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = Color(0xFF4D5558),
                    fontSize = 6.sp,
                    lineHeight = 8.sp,
                )
            }
        }
        TableCell(formatPreviewNumber(quantity), weight = 0.7f, textAlign = TextAlign.Center)
        TableCell(formatPreviewAmount(unitPrice, currency), weight = 1.1f, textAlign = TextAlign.Center)
        TableCell(formatPreviewAmount(lineTotal, currency), weight = 1.1f, bold = true, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PreviewEmptyRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFFFBFBFB))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(0.35f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFFD5F2F7)),
            )
        }
        Spacer(modifier = Modifier.weight(2.1f))
        Spacer(modifier = Modifier.weight(0.7f))
        Spacer(modifier = Modifier.weight(1.1f))
        Spacer(modifier = Modifier.weight(1.1f))
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = Color(0xFF2B2F31),
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 2.dp),
        color = color,
        fontSize = 7.sp,
        lineHeight = 9.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        textAlign = textAlign,
    )
}

@Composable
private fun ColumnScope.TotalLabel(text: String, bold: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = Color(0xFF161B1D),
            fontSize = if (bold) 8.sp else 7.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun ColumnScope.TotalValue(text: String, bold: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color(0xFFB8DDE0))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color(0xFF161B1D),
            fontSize = if (bold) 8.sp else 7.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 9.sp,
        )
    }
}

@Composable
private fun PreviewLogoImage(logoUrl: String?) {
    val logoBitmap = produceState<android.graphics.Bitmap?>(initialValue = null, logoUrl) {
        value = null
        if (!logoUrl.isNullOrBlank()) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    URL(logoUrl).openStream().use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        }
    }

    if (logoBitmap.value != null) {
        Image(
            bitmap = logoBitmap.value!!.asImageBitmap(),
            contentDescription = "شعار المتجر",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        Text(
            text = "شعارك",
            color = Color.White,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 9.sp,
        )
    }
}

private fun formatPreviewNumber(value: Double): String {
    return NumberFormat.getNumberInstance(PreviewLocale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(value)
}

private fun formatPreviewAmount(value: Double, currency: String): String {
    return "${NumberFormat.getNumberInstance(PreviewLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(value)} $currency"
}
