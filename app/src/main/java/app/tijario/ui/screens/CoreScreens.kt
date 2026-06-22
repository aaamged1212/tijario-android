package app.tijario.ui.screens

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.border
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.MainActivity
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.domain.DashboardStatsCalculator
import app.tijario.domain.PaymentStatusMapper
import app.tijario.features.documents.export.DocumentExportManager
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.ui.DocumentTemplatePreferences
import app.tijario.data.remote.AiV2CaptionRequest
import app.tijario.data.remote.AiV2ReplyRequest
import app.tijario.data.remote.AiV2ReportRequest
import app.tijario.data.remote.AiV2Variant
import app.tijario.ui.components.TijarioButton
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.tijario.ui.components.TijarioTextField
import io.github.jan.supabase.auth.auth
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

@Composable
fun ConfigurationRequiredScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = t("config_req"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = t("config_req_msg"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIJARIO_SUPABASE_URL=...\nTIJARIO_SUPABASE_ANON_KEY=...\nTIJARIO_API_BASE_URL=...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TijarioLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tealColor = Color(0xFF0D9488)
        val navyColor = Color(0xFF081C36)
        val stroke = size.width * 0.16f

        // Draw teal loop (upper-left)
        drawArc(
            color = tealColor,
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.65f, h * 0.65f)
        )

        // Draw navy loop (lower-right)
        drawArc(
            color = navyColor,
            startAngle = -45f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(w * 0.27f, h * 0.27f),
            size = Size(w * 0.65f, h * 0.65f)
        )
    }
}

@Composable
fun StatsWavyGraph(
    values: List<Double>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path()
        val normalized = normalizeSparkline(values)
        val points = normalized.mapIndexed { index, value ->
            val x = if (normalized.size == 1) w else (w / (normalized.lastIndex.toFloat())) * index
            val y = h * (0.82f - (value * 0.62f))
            Offset(x, y)
        }

        path.moveTo(points.first().x, points.first().y)
        points.drop(1).forEachIndexed { index, point ->
            val previous = points[index]
            val midX = (previous.x + point.x) / 2f
            path.cubicTo(midX, previous.y, midX, point.y, point.x, point.y)
        }

        // Path for fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        // Draw gradient fill under wave
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.25f), Color.Transparent)
            )
        )

        // Draw the wave line itself
        drawPath(
            path = path,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw peak glow point
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 6.dp.toPx(),
            center = points.last()
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = points.last()
        )
    }
}

private fun normalizeSparkline(values: List<Double>): List<Float> {
    var clean = values.takeLast(7).map { it.coerceAtLeast(0.0) }
    if (clean.isEmpty()) return listOf(0.15f, 0.3f, 0.22f, 0.45f, 0.38f, 0.62f, 0.5f)
    if (clean.size == 1) {
        clean = listOf(0.0, clean[0])
    }
    val max = clean.maxOrNull()?.takeIf { it > 0.0 } ?: return List(clean.size.coerceAtLeast(2)) { 0.2f }
    return clean.map { (it / max).toFloat().coerceIn(0.08f, 1f) }
}

private fun dashboardSparklineValues(
    documents: List<app.tijario.data.model.DocumentSummary>,
    currency: String,
): List<Double> {
    val invoices = documents
        .filter { it.type == app.tijario.data.model.DocumentType.Invoice }
        .filter { it.currency.equals(currency, ignoreCase = true) }
    val byDay = invoices.groupBy { it.issueDate.substringBefore("T").takeIf { value -> value.isNotBlank() } ?: it.issueDate }
    return byDay.toSortedMap().values.toList().takeLast(7).map { dayDocs ->
        DashboardStatsCalculator.calculateCollectedInvoiceAmount(dayDocs, currency)
    }
}

private fun formatDashboardDate(issueDate: String, language: AppLanguage): String {
    val rawDate = issueDate.substringBefore("T")
    return runCatching {
        val date = java.time.LocalDate.parse(rawDate)
        val formatter = java.time.format.DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            if (language == AppLanguage.AR) java.util.Locale("ar") else java.util.Locale.US,
        )
        date.format(formatter)
    }.getOrElse { rawDate }
}

@Composable
fun DashboardScreen(
    dataViewModel: TijarioDataViewModel,
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    onAddProduct: () -> Unit,
    onCustomers: () -> Unit,
    onAiTools: () -> Unit,
    onBusinessSettings: () -> Unit,
    onViewAllDocuments: () -> Unit,
    onDocumentClick: (String) -> Unit,
    hideHeader: Boolean = false,
) {
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val businessCurrency = uiState.businessSettings?.currency ?: "SAR"
    val currencyName = when (businessCurrency.uppercase()) {
        "SAR" -> "الريال السعودي"
        "YER" -> "الريال اليمني"
        "USD" -> "الدولار الأمريكي"
        "AED" -> "الدرهم الإماراتي"
        "EGP" -> "الجنيه المصري"
        "KWD" -> "الدينار الكويتي"
        "BHD" -> "الدينار البحريني"
        "OMR" -> "الريال العماني"
        "QAR" -> "الريال القطري"
        else -> businessCurrency
    }
    val totalAmount = remember(uiState.documents, businessCurrency) {
        DashboardStatsCalculator.calculateCollectedInvoiceAmount(uiState.documents, businessCurrency)
    }
    val sparklineValues = remember(uiState.documents, businessCurrency) {
        dashboardSparklineValues(uiState.documents, businessCurrency)
    }

    val planUsage = uiState.planUsage
    val isDocLimitReached = planUsage != null && planUsage.documentsLimit > 0 && planUsage.documentsUsed >= planUsage.documentsLimit
    var showLimitAlert by remember { mutableStateOf(false) }

    if (showLimitAlert) {
        AlertDialog(
            onDismissRequest = { showLimitAlert = false },
            title = { Text(t("limit_reached_title"), fontWeight = FontWeight.Bold) },
            text = { Text(t("limit_reached_msg") + " (${planUsage?.planName})") },
            confirmButton = {
                Button(onClick = { showLimitAlert = false }) {
                    Text(t("btn_ok"))
                }
            }
        )
    }

    val latestDocuments = remember(uiState.documents) {
        uiState.documents.sortedByDescending { it.issueDate }.take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Stats Navy Card Gradient with wavy graph - directly at the top
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF081C36), Color(0xFF0F2D54))
                        )
                    )
                    .padding(24.dp)
            ) {
                // Wavy graph on the end side of the card (left in RTL, right in LTR)
                StatsWavyGraph(
                    values = sparklineValues,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(130.dp)
                        .height(80.dp)
                        .offset(x = 8.dp, y = (-12).dp)
                )

                Column(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(t("financial_summary"), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(t("all_amounts_in").replace("%s", currencyName), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }

                        // Button (هذا الشهر)
                        Button(
                            onClick = { /* Detail Action */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(t("this_month"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = t("total_sales"),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%,.2f", totalAmount),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = businessCurrency,
                                color = Color(0xFF38BDF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unpaid Invoices
                        val unpaidAmount = remember(uiState.documents, businessCurrency) {
                            DashboardStatsCalculator.calculateOutstandingInvoiceAmount(uiState.documents, businessCurrency)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(t("uncollected_amounts"), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%,.2f", unpaidAmount),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = businessCurrency, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.12f))
                        )

                        // Open Quotes
                        val openQuotesAmount = remember(uiState.documents, businessCurrency) {
                            uiState.documents
                                .filter { it.type == app.tijario.data.model.DocumentType.Quote && (it.status?.lowercase() == "draft" || it.status?.lowercase() == "sent") && it.currency.uppercase() == businessCurrency.uppercase() }
                                .sumOf { it.total }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(t("open_quotes"), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%,.2f", openQuotesAmount),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = businessCurrency, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions title
        Text(
            text = t("quick_actions"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Actions grid first row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // New Invoice
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { if (isDocLimitReached) showLimitAlert = true else onNewInvoice() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFE6F4EA),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = null,
                                tint = Color(0xFF137333),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(t("new_invoice"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // New Quote
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { if (isDocLimitReached) showLimitAlert = true else onNewQuote() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFE8F0FE),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = Color(0xFF1A73E8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(t("new_quote"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Add Customer
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCustomers() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFFCE8E6),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFFC5221F),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(t("add_customer"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Actions grid second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Smart Assistant Card (Tijario AI Helper) - first in code so it renders on the right in RTL
            Card(
                modifier = Modifier
                    .weight(1.8f)
                    .clickable { onAiTools() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(t("tab_ai"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(t("ai_assistant_sub"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Add Product / Service - second in code so it renders on the left in RTL
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .clickable { onAddProduct() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFE4F7EB),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.BusinessCenter,
                                contentDescription = null,
                                tint = Color(0xFF0F9D58),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(t("new_product_service"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Latest Invoices / Quotes list Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text ("آخر المستندات") first so it renders on the right in RTL
            Text(
                text = t("latest_documents"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // TextButton ("عرض الكل") second so it renders on the left in RTL
            TextButton(onClick = onViewAllDocuments) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Arrow left icon second in the button Row so it renders on the left of text in RTL
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(t("view_all"), color = Color(0xFF0D9488), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Invoices / Documents Card list
        if (latestDocuments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("no_docs_yet"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                latestDocuments.forEach { doc ->
                    val customerName = uiState.customers.find { it.id == doc.customerId }?.name ?: t("unknown_customer")
                    val statusColor = when (doc.paymentStatus?.lowercase()) {
                        "paid" -> Color(0xFF22C55E)
                        "partial" -> Color(0xFFF97316)
                        "unpaid" -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDocumentClick(doc.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(statusColor)
                            )

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = doc.documentNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = customerName,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    val dateStr = try {
                                        val ldt = java.time.LocalDateTime.parse(doc.issueDate)
                                        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("ar"))
                                        ldt.format(formatter)
                                    } catch (e: Exception) {
                                        doc.issueDate.substringBefore("T")
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = dateStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Event,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (doc.type == app.tijario.data.model.DocumentType.Invoice) {
                                        val payStatusText = app.tijario.domain.PaymentStatusMapper.getStatusText(doc.paymentStatus, language)
                                        val payColors = app.tijario.domain.PaymentStatusMapper.getStatusColors(doc.paymentStatus)
                                        Surface(
                                            color = Color(payColors.first).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color(payColors.second), CircleShape)
                                                )
                                                Text(
                                                    text = payStatusText,
                                                    color = Color(payColors.second),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${doc.currency} ${String.format(java.util.Locale.US, "%,.2f", doc.total)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatsSmall(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = Color.White.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 104.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    dataViewModel: TijarioDataViewModel,
    onCreateCustomer: () -> Unit,
    onCustomerSelected: ((app.tijario.data.model.Customer) -> Unit)? = null,
    onEditCustomer: ((String) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "active", "new", "top"
    
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val customers = uiState.customers
    val documents = uiState.documents
    val isLoading = uiState.isInitialLoading && customers.isEmpty()

    // Calculate customer metrics dynamically
    val customerDocCounts = remember(documents) {
        documents.groupBy { it.customerId }.mapValues { it.value.size }
    }
    
    val activeCustomersCount = remember(customers, customerDocCounts) {
        customers.count { (customerDocCounts[it.id] ?: 0) > 0 }
    }
    val newCustomersCount = remember(customers, customerDocCounts) {
        customers.count { (customerDocCounts[it.id] ?: 0) == 0 }
    }
    val topCustomersThreshold = remember(customerDocCounts) {
        if (customerDocCounts.isEmpty()) 0 else customerDocCounts.values.maxOrNull() ?: 0
    }
    val topCustomersCount = remember(customers, customerDocCounts, topCustomersThreshold) {
        if (topCustomersThreshold == 0) 0 
        else customers.count { (customerDocCounts[it.id] ?: 0) >= (topCustomersThreshold * 0.5).coerceAtLeast(1.0) }
    }

    // Filter logic
    val filteredCustomers = remember(customers, searchQuery, selectedFilter, customerDocCounts, topCustomersThreshold) {
        customers.filter { customer ->
            val matchesSearch = customer.name.contains(searchQuery, ignoreCase = true) || 
                                customer.whatsappNumber.contains(searchQuery)
            val matchesFilter = when (selectedFilter) {
                "active" -> (customerDocCounts[customer.id] ?: 0) > 0
                "new" -> (customerDocCounts[customer.id] ?: 0) == 0
                "top" -> topCustomersThreshold > 0 && (customerDocCounts[customer.id] ?: 0) >= (topCustomersThreshold * 0.5).coerceAtLeast(1.0)
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    var customerToDelete by remember { mutableStateOf<app.tijario.data.model.Customer?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null; deleteErrorMessage = null },
            title = { Text(t("confirm_delete"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("delete_customer_confirm") + " ${customerToDelete?.name}?")
                    deleteErrorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isDeleting = true
                                deleteErrorMessage = null
                                val res = dataViewModel.deleteCustomer(customerToDelete!!.id!!)
                                if (res.isSuccess) {
                                    customerToDelete = null
                                } else {
                                    deleteErrorMessage = res.exceptionOrNull()?.message ?: Localization.getString("delete_customer_error", language)
                                }
                            } catch (e: Exception) {
                                deleteErrorMessage = e.message ?: "حدث خطأ غير متوقع."
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text(t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null; deleteErrorMessage = null }) {
                    Text(t("cancel"))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with custom layout matching image
            if (!hideHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Right block in RTL (Title/Subtitle with Double People icon)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFE4F2F1),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = t("customers_title"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = t("customers_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Left settings gear button in RTL
                    Surface(
                        color = Color.Transparent,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { /* settings click */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            TijarioTextField(
                label = t("search_placeholder"),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            // Horizontal Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // الكل (All)
                TijarioFilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = t("filter_all"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedFilter == "all") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // نشط (Active)
                TijarioFilterChip(
                    selected = selectedFilter == "active",
                    onClick = { selectedFilter = "active" },
                    label = t("customer_active"), // Maps to active filter text
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF22C55E), CircleShape)
                        )
                    }
                )

                // عميل جديد (New Customer)
                TijarioFilterChip(
                    selected = selectedFilter == "new",
                    onClick = { selectedFilter = "new" },
                    label = t("add_new_customer"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedFilter == "new") Color.White else Color(0xFFFBBF24)
                        )
                    }
                )

                // أكثر تعاملاً (Top Customer)
                TijarioFilterChip(
                    selected = selectedFilter == "top",
                    onClick = { selectedFilter = "top" },
                    label = t("most_active_customers"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (selectedFilter == "top") Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            // Stats Card (Unified card divided into 4 columns)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. إجمالي العملاء (Total)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(customers.size.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(t("total_customers"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // 2. عملاء نشطين (Active)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(activeCustomersCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(t("active_customers"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // 3. عملاء جدد (New)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(newCustomersCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(t("new_customers"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // 4. أكثر تعاملاً (Top)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(topCustomersCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(t("most_active_customers"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Loading state or Empty state
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(t("no_search_results"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onCreateCustomer) {
                            Text(t("add_new_customer"))
                        }
                    }
                }
            } else {
                // Customers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCustomers) { customer ->
                        val latestDoc = remember(documents, customer.id) {
                            documents.filter { it.customerId == customer.id }.maxByOrNull { it.issueDate }
                        }
                        val isCustomerActive = (customerDocCounts[customer.id] ?: 0) > 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelected?.invoke(customer)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Right / Main customer info block
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Initials Avatar with dynamic background colors
                                    val colorsList = listOf(
                                        Color(0xFFE0F2F1), Color(0xFFEDE7F6), Color(0xFFE8EAF6),
                                        Color(0xFFE0F7FA), Color(0xFFF3E5F5), Color(0xFFFFF3E0)
                                    )
                                    val hash = customer.name.hashCode().coerceAtLeast(0)
                                    val bgColor = colorsList[hash % colorsList.size]
                                    val textColors = listOf(
                                        Color(0xFF00796B), Color(0xFF512DA8), Color(0xFF303F9F),
                                        Color(0xFF0097A7), Color(0xFF7B1FA2), Color(0xFFE65100)
                                    )
                                    val textColor = textColors[hash % textColors.size]

                                    Surface(
                                        color = bgColor,
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = customer.name.take(1).uppercase(java.util.Locale.ROOT),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = textColor
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                customer.name,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 15.sp
                                            )
                                            
                                            // Active status badge
                                            val badgeBg = if (isCustomerActive) Color(0xFFE6F4EA) else Color(0xFFF1F3F4)
                                            val badgeText = if (isCustomerActive) Color(0xFF137333) else Color(0xFF5F6368)
                                            Surface(
                                                color = badgeBg,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(modifier = Modifier.size(5.dp).background(badgeText, CircleShape))
                                                    Text(
                                                        text = if (isCustomerActive) t("customer_active") else t("theme_light"), // Active / Normal
                                                        color = badgeText,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        if (latestDoc != null) {
                                            Text(
                                                text = "آخر فاتورة: ${latestDoc.documentNumber}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Text(
                                                text = customer.whatsappNumber,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Left inline action buttons block (Delete, Chat, Call, Edit)
                                val context = LocalContext.current
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Edit Button (Pencil)
                                    if (onCustomerSelected == null) {
                                        IconButton(
                                            onClick = { onEditCustomer?.invoke(customer.id!!) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "تعديل",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // 2. Call Button
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.whatsappNumber}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "تعذر تشغيل تطبيق الاتصال", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Phone,
                                            contentDescription = "اتصال",
                                            tint = Color(0xFF0F9D58),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 3. WhatsApp chat Button
                                    IconButton(
                                        onClick = {
                                            try {
                                                val formatted = customer.whatsappNumber.replace("[^0-9]".toRegex(), "")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, Localization.getString("cant_open_whatsapp", language), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Chat,
                                            contentDescription = "واتساب",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // 4. Delete Button directly replacing the three dots options menu
                                    if (onCustomerSelected == null) {
                                        IconButton(
                                            onClick = { customerToDelete = customer },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = t("delete"),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateCustomer,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun ProductsScreen(
    dataViewModel: TijarioDataViewModel,
    onCreateProduct: () -> Unit,
    onProductSelected: ((app.tijario.data.model.Product) -> Unit)? = null,
    onEditProduct: ((String) -> Unit)? = null,
    hideHeader: Boolean = false
) {
    val language = LocalLanguage.current
    var searchQuery by remember { mutableStateOf("") }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val products = uiState.products
    val businessCurrency = uiState.businessSettings?.currency ?: "SAR"
    val isLoading = uiState.isInitialLoading && products.isEmpty()
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || (it.description ?: "").contains(searchQuery, ignoreCase = true)
    }

    var productToDelete by remember { mutableStateOf<app.tijario.data.model.Product?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null; deleteErrorMessage = null },
            title = { Text(t("confirm_delete"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("delete_product_confirm") + " ${productToDelete?.name}?")
                    deleteErrorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isDeleting = true
                                deleteErrorMessage = null
                                val res = dataViewModel.deleteProduct(productToDelete!!.id!!)
                                if (res.isSuccess) {
                                    productToDelete = null
                                } else {
                                    deleteErrorMessage = res.exceptionOrNull()?.message ?: Localization.getString("delete_product_error", language)
                                }
                            } catch (e: Exception) {
                                deleteErrorMessage = e.message ?: "حدث خطأ غير متوقع."
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    else Text(t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null; deleteErrorMessage = null }) {
                    Text(t("cancel"))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hideHeader) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BusinessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = t("products_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (onProductSelected != null) t("choose_product_doc") else t("products_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TijarioTextField(
                label = t("search_products"),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(t("no_products_available"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onCreateProduct) {
                            Text(t("add_new_product"))
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onProductSelected?.invoke(item)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (item.kind == app.tijario.data.model.ProductKind.Service) Icons.Filled.Star else Icons.Filled.BusinessCenter,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            item.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            item.description ?: (if (item.kind == app.tijario.data.model.ProductKind.Service) t("kind_service") else t("kind_product")),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        item.stockQuantity?.let { stock ->
                                            Text(
                                                t("available_stock") + "$stock",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "${item.price} ${businessCurrency.ifBlank { item.currency }}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (onProductSelected == null) {
                                        IconButton(
                                            onClick = {
                                                onEditProduct?.invoke(item.id!!)
                                            }
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                productToDelete = item
                                            }
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = t("delete"), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateProduct,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun TijarioFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(20.dp)),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DocumentsScreen(
    dataViewModel: TijarioDataViewModel,
    onNewQuote: () -> Unit,
    onNewInvoice: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onEditDocument: (String, app.tijario.data.model.DocumentType) -> Unit = { _, _ -> },
    hideHeader: Boolean = false
) {
    val language = LocalLanguage.current
    var selectedSection by remember { mutableStateOf(0) } // 0 = Invoices, 1 = Quotes
    var selectedFilter by remember { mutableStateOf("all") } // "all", "unpaid", "paid", "partial"
    var menuExpanded by remember { mutableStateOf(false) }
    var documentPendingDelete by remember { mutableStateOf<app.tijario.data.model.DocumentSummary?>(null) }
    var busyDocumentId by remember { mutableStateOf<String?>(null) }

    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportManager = remember(context) { DocumentExportManager(context) }
    val templatePreferences = remember(context) { DocumentTemplatePreferences(context) }
    LaunchedEffect(Unit) {
        dataViewModel.refreshAll()
    }

    val documents = uiState.documents
    val customers = uiState.customers
    val isLoading = uiState.isInitialLoading && documents.isEmpty()

    // Filter documents depending on selection and status filter
    val filteredDocs = documents.filter { doc ->
        val matchesTab = if (selectedSection == 0) doc.type == app.tijario.data.model.DocumentType.Invoice else doc.type == app.tijario.data.model.DocumentType.Quote
        val matchesFilter = when (selectedFilter) {
            "unpaid" -> doc.paymentStatus?.lowercase() == "unpaid"
            "paid" -> doc.paymentStatus?.lowercase() == "paid"
            "partial" -> doc.paymentStatus?.lowercase() == "partial"
            else -> true
        }
        matchesTab && matchesFilter
    }

    // Sort documents (latest first by issueDate)
    val sortedDocs = remember(filteredDocs) {
        filteredDocs.sortedByDescending { it.issueDate }
    }

    fun shareDocument(documentId: String) {
        scope.launch {
            try {
                busyDocumentId = documentId
                val completeDocument = dataViewModel.fetchCompleteDocument(documentId).getOrThrow()
                val renderModel = TijarioDocumentMapper.fromSaved(
                    document = completeDocument,
                    businessSettings = uiState.businessSettings,
                    language = AppLanguage.AR,
                    templateId = templatePreferences.getDefaultTemplateId(),
                )
                val intent = exportManager.shareIntent(renderModel)
                context.startActivity(Intent.createChooser(intent, "مشاركة PDF"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "لا يوجد تطبيق مناسب للمشاركة", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(context, "تعذر تجهيز المستند للمشاركة الآن", Toast.LENGTH_LONG).show()
            } finally {
                busyDocumentId = null
            }
        }
    }

    fun deleteDocument(documentId: String) {
        scope.launch {
            try {
                busyDocumentId = documentId
                val result = dataViewModel.deleteDocument(documentId)
                if (!result.ok) {
                    Toast.makeText(context, result.displayMessage, Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, Localization.getString("cant_delete_doc", language), Toast.LENGTH_LONG).show()
            } finally {
                busyDocumentId = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Icon next to Title
            if (!hideHeader) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = t("documents_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = t("documents_subtitle"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Custom Pill Tab Layout under Navbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Invoices tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedSection == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedSection = 0 }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = if (selectedSection == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t("section_invoices"),
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                // Quotes tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedSection == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedSection = 1 }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Label,
                        contentDescription = null,
                        tint = if (selectedSection == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t("section_quotes"),
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            // Filtering Chips under Tabs - only show for Invoices
            if (selectedSection == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All Filter
                    TijarioFilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = t("filter_all"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedFilter == "all") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    // Unpaid Filter (only meaningful for invoices, but can show)
                    TijarioFilterChip(
                        selected = selectedFilter == "unpaid",
                        onClick = { selectedFilter = "unpaid" },
                        label = t("filter_unpaid"),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    )

                    // Paid Filter
                    TijarioFilterChip(
                        selected = selectedFilter == "paid",
                        onClick = { selectedFilter = "paid" },
                        label = t("filter_paid"),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                        }
                    )

                    // Partial Filter
                    TijarioFilterChip(
                        selected = selectedFilter == "partial",
                        onClick = { selectedFilter = "partial" },
                        label = t("filter_partial"),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFF97316), CircleShape)
                            )
                        }
                    )

                    // Latest indicator (just design / sort toggle)
                    TijarioFilterChip(
                        selected = true,
                        onClick = { /* sorting is default active */ },
                        label = t("filter_latest"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    )
                }
            }

            // Loading state or List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Document list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sortedDocs) { doc ->
                        val customerName = customers.find { it.id == doc.customerId }?.name ?: t("unknown_customer")
                        val statusColor = when (doc.paymentStatus?.lowercase()) {
                            "paid" -> Color(0xFF22C55E)
                            "partial" -> Color(0xFFF97316)
                            "unpaid" -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDocumentClick(doc.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                // 1. Vertical status colored line on the right edge in RTL.
                                // In RTL, the first child in Row is placed on the far right.
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .background(statusColor)
                                )

                                // 2. Document Content Area
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right Column: INV code, Customer name, Date (Drawn on the right in RTL, so first child)
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = doc.documentNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Customer Name
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = customerName,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Issue Date
                                        val dateStr = try {
                                            val ldt = java.time.LocalDateTime.parse(doc.issueDate)
                                            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("ar"))
                                            ldt.format(formatter)
                                        } catch (e: Exception) {
                                            doc.issueDate.substringBefore("T")
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = dateStr,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.Event,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Left Column: Three-dot options, payment status, total amount (Drawn on the left in RTL, so second child)
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        var actionsMenuExpanded by remember { mutableStateOf(false) }

                                        Box {
                                            IconButton(
                                                onClick = { actionsMenuExpanded = true },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = actionsMenuExpanded,
                                                onDismissRequest = { actionsMenuExpanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("معاينة", fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        actionsMenuExpanded = false
                                                        onDocumentClick(doc.id)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("تعديل", fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        actionsMenuExpanded = false
                                                        onEditDocument(doc.id, doc.type)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("مشاركة", fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        actionsMenuExpanded = false
                                                        shareDocument(doc.id)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(t("delete"), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error) },
                                                    onClick = {
                                                        actionsMenuExpanded = false
                                                        documentPendingDelete = doc
                                                    }
                                                )
                                            }
                                        }

                                        if (doc.type == app.tijario.data.model.DocumentType.Invoice) {
                                            val payStatusText = app.tijario.domain.PaymentStatusMapper.getStatusText(doc.paymentStatus, language)
                                            val payColors = app.tijario.domain.PaymentStatusMapper.getStatusColors(doc.paymentStatus)
                                            Surface(
                                                color = Color(payColors.first).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(Color(payColors.second), CircleShape)
                                                    )
                                                    Text(
                                                        text = payStatusText,
                                                        color = Color(payColors.second),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        // Total Price
                                        Text(
                                            text = "${doc.currency} ${String.format(java.util.Locale.US, "%,.2f", doc.total)}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button with DropdownMenu - BottomStart mirrors to bottom-right in AR and bottom-left in EN
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { menuExpanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text(t("btn_create_invoice"), fontWeight = FontWeight.Medium) },
                    onClick = {
                        menuExpanded = false
                        onNewInvoice()
                    }
                )
                DropdownMenuItem(
                    text = { Text(t("btn_create_quote"), fontWeight = FontWeight.Medium) },
                    onClick = {
                        menuExpanded = false
                        onNewQuote()
                    }
                )
            }
        }

        documentPendingDelete?.let { doc ->
            AlertDialog(
                onDismissRequest = { documentPendingDelete = null },
                title = { Text(t("delete_doc_title")) },
                text = { Text(t("delete_doc_confirm") + " (${doc.documentNumber})") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            documentPendingDelete = null
                            deleteDocument(doc.id)
                        },
                    ) {
                        Text(t("delete"), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { documentPendingDelete = null }) {
                        Text(t("cancel"))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiToolsScreen(
    dataViewModel: TijarioDataViewModel,
    hideHeader: Boolean = false
) {
    val language = LocalLanguage.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val planUsage = uiState.planUsage
    val isAiLimitReached = planUsage != null && planUsage.aiLimit > 0 && planUsage.aiUsed >= planUsage.aiLimit

    var selectedTab by remember { mutableStateOf(0) }

    // Smart Reply form
    var quickCase by remember { mutableStateOf<String?>(null) }
    var customerName by remember { mutableStateOf("") }
    var customerMessage by remember { mutableStateOf("") }
    var replyDialect by remember { mutableStateOf("fusha_simple") }
    var replyTone by remember { mutableStateOf("friendly") }
    var replyLength by remember { mutableStateOf("short") }
    var replyExtraNote by remember { mutableStateOf("") }
    var replyGenerationId by remember { mutableStateOf<String?>(null) }
    var replyVariants by remember { mutableStateOf<List<AiV2Variant>>(emptyList()) }
    var replyError by remember { mutableStateOf<String?>(null) }
    var isReplyLoading by remember { mutableStateOf(false) }

    // Smart Caption form
    var captionType by remember { mutableStateOf("product_post") }
    var platform by remember { mutableStateOf("instagram") }
    var captionDialect by remember { mutableStateOf("fusha_simple") }
    var captionTone by remember { mutableStateOf("sales") }
    var captionLength by remember { mutableStateOf("short") }
    var productOrService by remember { mutableStateOf("") }
    var offer by remember { mutableStateOf("") }
    var captionImage by remember { mutableStateOf<app.tijario.data.remote.AiImageInput?>(null) }
    var captionImageLabel by remember { mutableStateOf<String?>(null) }
    var captionExtraNote by remember { mutableStateOf("") }
    var captionGenerationId by remember { mutableStateOf<String?>(null) }
    var captionVariants by remember { mutableStateOf<List<AiV2Variant>>(emptyList()) }
    var captionError by remember { mutableStateOf<String?>(null) }
    var isCaptionLoading by remember { mutableStateOf(false) }

    val captionImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val imageInput = buildAiImageInput(context, uri, language)
                captionImage = imageInput
                captionImageLabel = "تم اختيار صورة المنتج"
                captionError = null
            } catch (e: Exception) {
                captionError = e.message ?: "تعذر قراءة صورة المنتج."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hideHeader) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = t("ai_title"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = t("ai_subtitle"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Limit Banner
        if (isAiLimitReached) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "لقد استهلكت كامل الحد المتاح لك من استعلامات الذكاء الاصطناعي لشهرك الحالي. يرجى الترقية للاستمرار.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(t("tab_ai_reply"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(t("tab_ai_caption"), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        if (selectedTab == 0) {
            // Smart Reply Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t("ai_reply_settings"), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Quick case
                    Text(t("ai_case_type"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf(
                            "price_request" to "طلب سعر",
                            "price_objection" to "السعر غالي",
                            "discount_request" to "يريد خصم",
                            "delivery_question" to "سؤال عن التوصيل",
                            "availability_question" to "سؤال عن التوفر",
                            "customer_hesitant" to "عميل متردد",
                            "customer_interested" to "عميل مهتم",
                            "follow_up" to "متابعة عميل",
                            "payment_reminder" to "تذكير بالدفع",
                            "delayed_response_apology" to "اعتذار عن التأخر",
                            "review_request" to "طلب تقييم",
                            "general_inquiry" to "استفسار عام",
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = quickCase == code,
                                onClick = { quickCase = if (quickCase == code) null else code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Dialect
                    Text(t("ai_dialect"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("fusha_simple" to "فصحى بسيطة", "gulf" to t("ai_gulf"), "yemeni" to "يمني").forEach { (code, label) ->
                            FilterChip(
                                selected = replyDialect == code,
                                onClick = { replyDialect = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Tone
                    Text(t("ai_tone"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("friendly" to t("ai_tone_friendly"), "formal" to t("ai_tone_formal"), "sales" to t("ai_tone_sales")).forEach { (code, label) ->
                            FilterChip(
                                selected = replyTone == code,
                                onClick = { replyTone = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    TijarioTextField(
                        label = t("ai_cust_name_opt"),
                        value = customerName,
                        onValueChange = { customerName = it }
                    )

                    TijarioTextField(
                        label = t("ai_cust_msg_opt"),
                        value = customerMessage,
                        onValueChange = { customerMessage = it },
                        singleLine = false
                    )

                    TijarioTextField(
                        label = t("ai_notes_label"),
                        value = replyExtraNote,
                        onValueChange = { replyExtraNote = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = t("btn_generate_reply"),
                        onClick = {
                            scope.launch {
                                try {
                                    isReplyLoading = true
                                    replyError = null
                                    replyVariants = emptyList()
                                    val req = AiV2ReplyRequest(
                                        clientRequestId = UUID.randomUUID().toString(),
                                        customerMessage = customerMessage.ifBlank { null },
                                        quickCase = quickCase,
                                        customerName = customerName.ifBlank { null },
                                        goal = "auto",
                                        dialect = replyDialect,
                                        tone = replyTone,
                                        length = replyLength,
                                        extraContext = replyExtraNote.ifBlank { null },
                                        language = if (language == AppLanguage.AR) "ar" else "ar",
                                    )
                                    val res = dataViewModel.generateAiReplyV2(req)
                                    if (res.ok) {
                                        replyGenerationId = res.data?.generationId
                                        replyVariants = res.data?.variants ?: emptyList()
                                    } else {
                                        replyError = res.message ?: Localization.getString("unexpected_error", language)
                                    }
                                } catch (e: Exception) {
                                    replyError = e.message ?: Localization.getString("unexpected_error", language)
                                } finally {
                                    isReplyLoading = false
                                }
                            }
                        },
                        enabled = !isAiLimitReached && !isReplyLoading && (quickCase != null || customerMessage.isNotBlank()),
                        isLoading = isReplyLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (replyError != null) {
                        Text(replyError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            // Suggestions List
            if (replyVariants.isNotEmpty()) {
                Text(t("generated_suggestions"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                replyVariants.forEach { variant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(variant.label, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 12.sp)
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Reply", variant.text)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, Localization.getString("reply_copied", language), android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Share, contentDescription = Localization.getString("copy", language), tint = Color(0xFF15803D))
                                }
                            }
                            Text(variant.text, color = Color(0xFF1E293B), fontSize = 14.sp)
                            if (!replyGenerationId.isNullOrBlank()) {
                                TextButton(onClick = {
                                    scope.launch {
                                        val result = dataViewModel.reportAiGenerationV2(
                                            AiV2ReportRequest(
                                                clientRequestId = UUID.randomUUID().toString(),
                                                generationId = replyGenerationId!!,
                                                issueType = "wrong_answer",
                                                note = null,
                                            )
                                        )
                                        if (!result.ok) {
                                            replyError = result.message ?: Localization.getString("unexpected_error", language)
                                        }
                                    }
                                }) {
                                    Text("بلاغ")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Smart Caption Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t("ai_caption_settings"), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Platform
                    Text(t("ai_platform"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("instagram" to t("ai_instagram"), "twitter" to t("ai_twitter"), "facebook" to t("ai_facebook")).forEach { (code, label) ->
                            FilterChip(
                                selected = platform == code,
                                onClick = { platform = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Dialect
                    Text(t("ai_dialect"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("fusha_simple" to "فصحى بسيطة", "gulf" to t("ai_gulf"), "yemeni" to "يمني").forEach { (code, label) ->
                            FilterChip(
                                selected = captionDialect == code,
                                onClick = { captionDialect = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Tone
                    Text(t("ai_tone"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("sales" to t("ai_tone_sales"), "friendly" to t("ai_tone_friendly"), "premium" to "فاخر").forEach { (code, label) ->
                            FilterChip(
                                selected = captionTone == code,
                                onClick = { captionTone = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    TijarioTextField(
                        label = t("ai_prod_srv_label"),
                        value = productOrService,
                        onValueChange = { productOrService = it }
                    )

                    TijarioTextField(
                        label = t("ai_offer_label"),
                        value = offer,
                        onValueChange = { offer = it }
                    )

                    OutlinedButton(
                        onClick = { captionImagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(captionImageLabel ?: t("ai_add_image_opt"))
                    }

                    if (captionImage != null) {
                        TextButton(onClick = {
                            captionImage = null
                            captionImageLabel = null
                        }) {
                            Text(t("ai_remove_image"))
                        }
                    }

                    TijarioTextField(
                        label = t("ai_notes_simple"),
                        value = captionExtraNote,
                        onValueChange = { captionExtraNote = it },
                        singleLine = false
                    )

                    TijarioButton(
                        text = t("ai_btn_gen_caption"),
                        onClick = {
                            scope.launch {
                                try {
                                    isCaptionLoading = true
                                    captionError = null
                                    captionVariants = emptyList()
                                    val req = AiV2CaptionRequest(
                                        clientRequestId = UUID.randomUUID().toString(),
                                        productOrService = productOrService,
                                        captionType = captionType,
                                        platform = platform,
                                        dialect = captionDialect,
                                        tone = captionTone,
                                        length = captionLength,
                                        offer = offer.ifBlank { null },
                                        productImage = captionImage,
                                        extraContext = captionExtraNote.ifBlank { null },
                                        language = if (language == AppLanguage.AR) "ar" else "ar",
                                    )
                                    val res = dataViewModel.generateAiCaptionV2(req)
                                    if (res.ok) {
                                        captionGenerationId = res.data?.generationId
                                        captionVariants = res.data?.variants ?: emptyList()
                                    } else {
                                        captionError = res.message ?: Localization.getString("unexpected_error", language)
                                    }
                                } catch (e: Exception) {
                                    captionError = e.message ?: Localization.getString("unexpected_error", language)
                                } finally {
                                    isCaptionLoading = false
                                }
                            }
                        },
                        enabled = !isAiLimitReached && !isCaptionLoading && productOrService.isNotBlank(),
                        isLoading = isCaptionLoading,
                        icon = Icons.AutoMirrored.Filled.Send
                    )

                    if (captionError != null) {
                        Text(captionError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            // Captions List
            if (captionVariants.isNotEmpty()) {
                Text(t("generated_suggestions"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                captionVariants.forEach { variant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(variant.label, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 12.sp)
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val textToCopy = variant.text
                                    val clip = android.content.ClipData.newPlainText("Caption", textToCopy)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, Localization.getString("caption_copied", language), android.widget.Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Filled.Share, contentDescription = Localization.getString("copy", language), tint = Color(0xFF15803D))
                                }
                            }
                            Text(variant.text, color = Color(0xFF1E293B), fontSize = 14.sp)
                            if (!captionGenerationId.isNullOrBlank()) {
                                TextButton(onClick = {
                                    scope.launch {
                                        val result = dataViewModel.reportAiGenerationV2(
                                            AiV2ReportRequest(
                                                clientRequestId = UUID.randomUUID().toString(),
                                                generationId = captionGenerationId!!,
                                                issueType = "wrong_answer",
                                                note = null,
                                            )
                                        )
                                        if (!result.ok) {
                                            captionError = result.message ?: Localization.getString("unexpected_error", language)
                                        }
                                    }
                                }) {
                                    Text("بلاغ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    dataViewModel: TijarioDataViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Store, 1 = Personal
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()

    var currentUserEmail by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var isLogoUploading by remember { mutableStateOf(false) }
    val businessSettings = uiState.businessSettings

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                dataViewModel.refreshAll()
                val user = app.tijario.config.Supabase.client.auth.currentUserOrNull()
                if (user != null) {
                    currentUserEmail = user.email ?: ""
                    currentUserName = user.userMetadata?.get("full_name")?.toString() ?: ""
                }
            } catch (e: Exception) {
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text(t("tab_account"), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(t("tab_store_account"), fontWeight = FontWeight.Bold) } // Store Account
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(t("tab_personal_account"), fontWeight = FontWeight.Bold) } // Personal Account
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (selectedTab == 0) {
                // Store Account Tab
                StoreAccountContent(
                    settings = businessSettings,
                    isLogoUploading = isLogoUploading,
                    onLogoSelected = { logoUri ->
                        scope.launch {
                            val settings = businessSettings
                            if (settings == null) {
                                snackbarHostState.showSnackbar(Localization.getString("complete_settings_logo", language))
                                return@launch
                            }

                            try {
                                isLogoUploading = true
                                val uploadRequest = buildLogoUploadRequest(context, logoUri, language)
                                val result = app.tijario.config.Supabase.apiClient.uploadBusinessLogo(uploadRequest)
                                val logoUrl = result.data?.logoUrl
                                if (result.ok && !logoUrl.isNullOrBlank()) {
                                    dataViewModel.saveBusinessSettings(settings.copy(logoUrl = logoUrl))
                                    snackbarHostState.showSnackbar(Localization.getString("logo_saved_success", language))
                                } else {
                                    snackbarHostState.showSnackbar(result.displayMessage)
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(e.message ?: Localization.getString("logo_upload_error", language))
                            } finally {
                                isLogoUploading = false
                            }
                        }
                    },
                    onUpdate = { updated ->
                        scope.launch {
                            try {
                                val result = dataViewModel.saveBusinessSettings(updated)
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(Localization.getString("settings_saved_success", language))
                                } else {
                                    snackbarHostState.showSnackbar(Localization.getString("save_settings_error", language))
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(Localization.getString("save_settings_error", language))
                            }
                        }
                    }
                )
            } else {
                // Personal Account Tab
                PersonalAccountContent(
                    email = currentUserEmail,
                    name = currentUserName,
                    onLogout = onLogout,
                    onChangePassword = {
                        scope.launch {
                            if (currentUserEmail.isBlank()) {
                                snackbarHostState.showSnackbar("لا يوجد بريد إلكتروني مرتبط بالحساب")
                                return@launch
                            }

                            try {
                                val result = app.tijario.config.Supabase.apiClient.requestPasswordReset(
                                    app.tijario.data.remote.ResetPasswordRequest(email = currentUserEmail)
                                )
                                if (result.ok) {
                                    snackbarHostState.showSnackbar("تم إرسال رابط تغيير كلمة المرور إلى بريدك")
                                } else {
                                    snackbarHostState.showSnackbar(result.displayMessage)
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("تعذر إرسال رابط تغيير كلمة المرور")
                            }
                        }
                    },
                )
            }

            // App Settings (Global)
            Text(
                text = "إعدادات التطبيق",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val context = LocalContext.current
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Language
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(t("settings_lang"), fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                MainActivity.currentLanguage = AppLanguage.AR
                                AppPreferences.setLanguage(context, AppLanguage.AR)
                            }) {
                                Text("العربية", color = if (MainActivity.currentLanguage == AppLanguage.AR) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                            TextButton(onClick = {
                                MainActivity.currentLanguage = AppLanguage.EN
                                AppPreferences.setLanguage(context, AppLanguage.EN)
                            }) {
                                Text("English", color = if (MainActivity.currentLanguage == AppLanguage.EN) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }
                    HorizontalDivider()
                    // Theme
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(t("settings_theme"), fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = MainActivity.isDarkMode,
                            onCheckedChange = {
                                MainActivity.isDarkMode = it
                                AppPreferences.setDarkMode(context, it)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StoreAccountContent(
    settings: app.tijario.data.model.BusinessSettings?,
    isLogoUploading: Boolean,
    onLogoSelected: (Uri) -> Unit,
    onUpdate: (app.tijario.data.model.BusinessSettings) -> Unit
) {
    var businessName by remember(settings) { mutableStateOf(settings?.businessName ?: "") }
    var whatsapp by remember(settings) { mutableStateOf(settings?.whatsappNumber ?: "") }
    var country by remember(settings) { mutableStateOf(settings?.country ?: "السعودية") }
    var city by remember(settings) { mutableStateOf(settings?.city ?: "") }
    var currency by remember(settings) { mutableStateOf(settings?.currency ?: "SAR") }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            onLogoSelected(uri)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StoreLogoPicker(
                logoUrl = settings?.logoUrl,
                isUploading = isLogoUploading,
                onClick = { logoPicker.launch("image/*") },
            )
        }

        TijarioTextField(
            label = t("shop_name"),
            value = businessName,
            onValueChange = { businessName = it },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) }
        )
        TijarioTextField(
            label = t("whatsapp_phone"),
            value = whatsapp,
            onValueChange = { whatsapp = it },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }
        )
        TijarioTextField(
            label = t("country"),
            value = country,
            onValueChange = { country = it },
            leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null) }
        )
        TijarioTextField(
            label = t("city"),
            value = city,
            onValueChange = { city = it },
            leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) }
        )
        TijarioTextField(
            label = t("currency"),
            value = currency,
            onValueChange = { currency = it },
            leadingIcon = { Icon(Icons.Filled.MonetizationOn, contentDescription = null) }
        )

        TijarioButton(
            text = t("btn_save_store_changes"),
            onClick = {
                if (settings != null) {
                    onUpdate(settings.copy(
                        businessName = businessName,
                        whatsappNumber = whatsapp,
                        country = country,
                        city = city.ifBlank { null },
                        currency = currency
                    ))
                }
            }
        )
    }
}
@Composable
private fun StoreLogoPicker(
    logoUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val logoBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, logoUrl) {
        value = null
        if (!logoUrl.isNullOrBlank()) {
            value = loadCachedLogoBitmap(context, logoUrl)
        }
    }

    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(enabled = !isUploading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
            logoBitmap != null -> Image(
                bitmap = logoBitmap!!.asImageBitmap(),
                contentDescription = t("store_logo"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(t("store_logo"), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private suspend fun loadCachedLogoBitmap(
    context: Context,
    logoUrl: String,
): android.graphics.Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val cacheDir = File(context.filesDir, "business-logo-cache").apply { mkdirs() }
            val cacheFile = File(cacheDir, "${logoUrl.sha256()}.img")

            if (cacheFile.exists() && cacheFile.length() > 0) {
                BitmapFactory.decodeFile(cacheFile.absolutePath)?.let { return@withContext it }
            }

            val bytes = URL(logoUrl).openStream().use { stream ->
                stream.readBytes()
            }

            if (bytes.isNotEmpty()) {
                cacheFile.writeBytes(bytes)
            }

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }

private suspend fun buildLogoUploadRequest(
    context: Context,
    uri: Uri,
    language: AppLanguage,
): app.tijario.data.remote.UploadLogoRequest =
    withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

        require(mimeType in allowedMimeTypes) {
            Localization.getString("logo_format_error", language)
        }

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("تعذر قراءة صورة الشعار.")

        require(bytes.isNotEmpty()) {
            Localization.getString("logo_empty_error", language)
        }
        require(bytes.size <= 2 * 1024 * 1024) {
            val errStr = if (language == AppLanguage.EN) "Logo size must not exceed 2MB." else "حجم الشعار يجب ألا يتجاوز 2MB."
            errStr
        }

        app.tijario.data.remote.UploadLogoRequest(
            fileName = "logo",
            mimeType = mimeType,
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
        )
    }

private suspend fun buildAiImageInput(
    context: Context,
    uri: Uri,
    language: AppLanguage,
): app.tijario.data.remote.AiImageInput =
    withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

        require(mimeType in allowedMimeTypes) {
            Localization.getString("product_image_format_error", language)
        }

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("تعذر قراءة صورة المنتج.")

        require(bytes.isNotEmpty()) {
            Localization.getString("image_empty_error", language)
        }
        require(bytes.size <= 1024 * 1024) {
            Localization.getString("image_size_error", language)
        }

        app.tijario.data.remote.AiImageInput(
            mimeType = mimeType,
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
        )
    }

@Composable
fun PersonalAccountContent(
    email: String,
    name: String,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TijarioTextField(
                    label = t("fullname"),
                    value = name,
                    onValueChange = {},
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                )
                TijarioTextField(
                    label = t("email"),
                    value = email,
                    onValueChange = {},
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
                )
            }
        }

        Button(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(t("change_password"), fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(t("logout"), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UsageIndicator(title: String, used: Int, total: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$used ${t("of")} $total", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
        }
        LinearProgressIndicator(
            progress = { used.toFloat() / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
