package com.adygyes.app.presentation.ui.screens.privacy

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adygyes.app.R
import com.adygyes.app.presentation.theme.Dimensions
import android.content.Intent
import android.net.Uri

/**
 * Privacy Policy screen showing app privacy information and data handling policies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.privacy_policy_title),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Dimensions.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            // App Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "AdygGis Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = stringResource(R.string.privacy_policy_full_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Content Sections
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PolicySection(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.privacy_introduction_title),
                        content = stringResource(R.string.privacy_introduction_content)
                    )
                    
                    PolicySection(
                        icon = Icons.Default.Storage,
                        title = stringResource(R.string.privacy_data_collection_title),
                        content = stringResource(R.string.privacy_data_collection_content)
                    )
                    
                    PolicySection(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.privacy_data_usage_title),
                        content = stringResource(R.string.privacy_data_usage_content)
                    )
                    
                    PolicySection(
                        icon = Icons.Default.Group,
                        title = stringResource(R.string.privacy_data_sharing_title),
                        content = stringResource(R.string.privacy_data_sharing_content)
                    )
                    
                    PolicySection(
                        icon = Icons.Default.Email,
                        title = stringResource(R.string.privacy_contact_title),
                        content = stringResource(R.string.privacy_contact_content),
                        isClickable = true,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/MaykopTech"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    )
                }
            }
            
            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PolicySection(
    icon: ImageVector,
    title: String,
    content: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable && onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (isClickable) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            modifier = Modifier.padding(start = 44.dp)
        )
        
        // Divider line for visual separation
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}
