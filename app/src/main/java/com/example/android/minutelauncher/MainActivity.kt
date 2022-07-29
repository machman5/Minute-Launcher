package com.example.android.minutelauncher

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android.minutelauncher.ui.theme.MinuteLauncherTheme

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinuteLauncherTheme {
                if (!isAccessGranted(packageManager)) {
                    // TODO: open dialog informing user about permission before opening settings
                    startActivity(Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS })
                }
                val mContext = LocalContext.current
                LazyColumn(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    listOfApps(mContext = mContext, goToApp = { startActivity(it) })
                }
            }
        }
    }
}

fun isAccessGranted(packageManager: PackageManager): Boolean {
    return try {
        packageManager.checkPermission(
            android.Manifest.permission.PACKAGE_USAGE_STATS,
            ""
        ) == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }
}

fun LazyListScope.listOfApps(
    mContext: Context,
    goToApp: (Intent?) -> Unit
) {
    val mainIntent = Intent().apply {
        action = ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val pm = mContext.packageManager

    val installedPackages = pm.queryIntentActivities(mainIntent, 0).sortedBy {
        it.loadLabel(pm).toString().lowercase()
    }

    items(installedPackages) { app ->
        Row {
            val appTitle = app.loadLabel(pm).toString()
            AppCard(appTitle) {
                Toast.makeText(mContext, appTitle, Toast.LENGTH_SHORT).show()
                val intent = pm
                    .getLaunchIntentForPackage(app.activityInfo.packageName)
                    ?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                goToApp(intent)
            }

        }
    }
}

@Composable
fun AppCard(appTitle: String, onClick: () -> Unit) {
    Text(
        text = appTitle,
        style = MaterialTheme.typography.displaySmall,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(2.dp).clickable { onClick() }
    )
}