package com.example.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppColors
import com.example.app.ui.theme.AppHeader
import com.example.app.ui.theme.EvergreenTheme

data class ServiceReportFormState(
    val title: String = "Create Service Report",
    val subtitle: String = "Standalone or from a task",
    val saveLabel: String = "Save Report",
    val reportNumber: String = "",
    val taskId: String = "",
    val status: String = "Not Started",
    val generatedAt: String = "",
    val companyName: String = "",
    val companyBranch: String = "",
    val location: String = "",
    val companyEmail: String = "",
    val companyLink: Int? = null,
    val raisedByName: String = "",
    val raisedByPhone: String = "",
    val raisedByEmail: String = "",
    val raisedByNote: String = "",
    val technicianName: String = "",
    val technicianPhone: String = "",
    val technicianNote: String = "",
    val equipmentName: String = "",
    val equipmentMake: String = "",
    val equipmentModel: String = "",
    val serialNumber: String = "",
    val egSerial: String = "",
    val equipmentLocation: String = "",
    val callType: String = "",
    val issue: String = "",
    val issueDescription: String = "",
    val serviceDetails: String = "",
    val timeline: List<TimelineEventState> = emptyList(),
    val servicedToSatisfaction: String = "",
    val runningSmoothly: String = "",
    val comments: String = "",
    val rating: String = "",
    val cost: String = "",
    val viewOnly: Boolean = false
)

data class TimelineEventState(
    val date: String = "",
    val time: String = "",
    val title: String = "",
    val description: String = ""
)

@Composable
fun ServiceReportFormScreen(
    state: ServiceReportFormState,
    onStateChange: (ServiceReportFormState) -> Unit,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onPdfClick: () -> Unit = {},
    onCompanyClick: () -> Unit = {},
    onStatusClick: () -> Unit = {},
    onSatisfactionClick: () -> Unit = {},
    onSmoothClick: () -> Unit = {}
) {
    val enabled = !state.viewOnly
    EvergreenTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AppHeader(
                title = state.title,
                subtitle = state.subtitle,
                onLeadingClick = onBackClick
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                SectionCard("Report Details") {
                    if (state.reportNumber.isNotBlank()) {
                        ReadOnlyRow("Service Report No.", state.reportNumber)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    InputRow("Task ID", "Optional. Existing or external ID", state.taskId, enabled) {
                        onStateChange(state.copy(taskId = it))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectRow("Status", state.status.ifBlank { "Not Started" }, enabled, onStatusClick)
                    if (state.generatedAt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ReadOnlyRow("Generated", state.generatedAt)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Company / Customer") {
                    SelectRow("Company / Customer *", state.companyName.ifBlank { "Select company" }, enabled, onCompanyClick)
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Company name", "Required if not selected", state.companyName, enabled) {
                        onStateChange(state.copy(companyName = it, companyLink = if (it != state.companyName) null else state.companyLink))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Location / Address", "Branch or site", state.location, enabled) {
                        onStateChange(state.copy(location = it))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Raised By") {
                    InputRow("Name", "Requester name", state.raisedByName, enabled) { onStateChange(state.copy(raisedByName = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Phone", "Phone number", state.raisedByPhone, enabled) { onStateChange(state.copy(raisedByPhone = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Email", "Email", state.raisedByEmail, enabled) { onStateChange(state.copy(raisedByEmail = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Created / Reported by", "Optional note", state.raisedByNote, enabled) { onStateChange(state.copy(raisedByNote = it)) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Technician") {
                    InputRow("Technician name", "Name", state.technicianName, enabled) { onStateChange(state.copy(technicianName = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Phone / Contact", "Phone", state.technicianPhone, enabled) { onStateChange(state.copy(technicianPhone = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Assignment note", "Assigned technician, etc.", state.technicianNote, enabled) { onStateChange(state.copy(technicianNote = it)) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Equipment") {
                    InputRow("Equipment name", "Name", state.equipmentName, enabled) { onStateChange(state.copy(equipmentName = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Make", "Make", state.equipmentMake, enabled) { onStateChange(state.copy(equipmentMake = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Model", "Model", state.equipmentModel, enabled) { onStateChange(state.copy(equipmentModel = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Serial number", "Serial No.", state.serialNumber, enabled) { onStateChange(state.copy(serialNumber = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("EG serial number", "EG Serial", state.egSerial, enabled) { onStateChange(state.copy(egSerial = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Location", "Equipment location", state.equipmentLocation, enabled) { onStateChange(state.copy(equipmentLocation = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Call type", "AMC / Breakdown / Service", state.callType, enabled) { onStateChange(state.copy(callType = it)) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Issue") {
                    InputRow("Issue / Complaint", "Short title", state.issue, enabled) { onStateChange(state.copy(issue = it)) }
                    Spacer(modifier = Modifier.height(10.dp))
                    MultilineRow("Details", "Describe the service request", state.issueDescription, enabled) {
                        onStateChange(state.copy(issueDescription = it))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Service / Work Details") {
                    MultilineRow("Work performed", "Observations, work completed, parts used", state.serviceDetails, enabled) {
                        onStateChange(state.copy(serviceDetails = it))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Timeline of Events") {
                    state.timeline.forEachIndexed { index, event ->
                        TimelineCard(
                            event = event,
                            enabled = enabled,
                            onChange = { updated ->
                                onStateChange(state.copy(timeline = state.timeline.toMutableList().also { it[index] = updated }))
                            },
                            onRemove = {
                                onStateChange(state.copy(timeline = state.timeline.filterIndexed { i, _ -> i != index }))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (enabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AppColors.blue, RoundedCornerShape(8.dp))
                                .clickable {
                                    onStateChange(state.copy(timeline = state.timeline + TimelineEventState()))
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.blue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add timeline event", color = AppColors.blue, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Customer Confirmation") {
                    SelectRow("Serviced to satisfaction", state.servicedToSatisfaction.ifBlank { "Select" }, enabled, onSatisfactionClick)
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectRow("Running smoothly", state.runningSmoothly.ifBlank { "Select" }, enabled, onSmoothClick)
                    Spacer(modifier = Modifier.height(10.dp))
                    MultilineRow("Comments", "Customer comments", state.comments, enabled) {
                        onStateChange(state.copy(comments = it))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    InputRow("Rating", "e.g. 5 / 5", state.rating, enabled) { onStateChange(state.copy(rating = it)) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SectionCard("Service Cost") {
                    InputRow("Service Cost", "e.g. 2500", state.cost, enabled) {
                        onStateChange(state.copy(cost = it))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    if (enabled) {
                        ActionButton(Modifier.weight(1f), state.saveLabel, AppColors.blue, Color.White, onSaveClick)
                    }
                    if (state.reportNumber.isNotBlank() || state.viewOnly) {
                        ActionButton(
                            modifier = Modifier.weight(1f),
                            label = "Generate PDF",
                            background = AppColors.blueDark,
                            textColor = Color.White,
                            onClick = onPdfClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.blueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = AppColors.blue, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.textPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun InputRow(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean,
    icon: ImageVector? = null,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppColors.border, RoundedCornerShape(8.dp))
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { Text(placeholder, fontSize = 14.sp, color = Color(0xFF969CAE)) },
                leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = AppColors.blue) } },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = AppColors.textPrimary),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MultilineRow(
    label: String,
    placeholder: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppColors.border, RoundedCornerShape(8.dp))
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { Text(placeholder, fontSize = 14.sp, color = Color(0xFF969CAE)) },
                textStyle = TextStyle(fontSize = 14.sp, color = AppColors.textPrimary),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectRow(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppColors.border, RoundedCornerShape(8.dp))
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(value, fontSize = 14.sp, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppColors.textSecondary)
            }
        }
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
    }
}

@Composable
private fun TimelineCard(
    event: TimelineEventState,
    enabled: Boolean,
    onChange: (TimelineEventState) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.background)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Event", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
            if (enabled) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = AppColors.red,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onRemove)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        InputRow("Date", "YYYY-MM-DD", event.date, enabled) { onChange(event.copy(date = it)) }
        Spacer(modifier = Modifier.height(8.dp))
        InputRow("Time", "e.g. 10:00 AM", event.time, enabled) { onChange(event.copy(time = it)) }
        Spacer(modifier = Modifier.height(8.dp))
        InputRow("Title", "e.g. Technician visited", event.title, enabled) { onChange(event.copy(title = it)) }
        Spacer(modifier = Modifier.height(8.dp))
        MultilineRow("Description", "What happened", event.description, enabled) { onChange(event.copy(description = it)) }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier,
    label: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(background, background)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
