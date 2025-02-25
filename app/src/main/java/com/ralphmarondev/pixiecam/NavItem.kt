package com.ralphmarondev.pixiecam

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val onClick: () -> Unit,
    val label: String,
    val imageVector: ImageVector
)
