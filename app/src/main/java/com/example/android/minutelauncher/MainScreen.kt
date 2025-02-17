package com.example.android.minutelauncher

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.lang.reflect.Method


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
  onNavigate: (String) -> Unit,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val mContext = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val coroutineScope = rememberCoroutineScope()
  val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
    bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed) {
      Log.d("MAIN_SCREEN", it.name)
      // When sheet is dragged into a collapsed state the keyboard should be hidden
      if (it.name != BottomSheetValue.Expanded.name) {
        focusRequester.freeFocus()
        keyboardController?.hide()
        viewModel.onEvent(Event.UpdateSearch(""))
      }
      true
    }
  )

  var currentAppInfoDialog by remember { mutableStateOf<UserApp?>(null) }
  var currentAppConfirmationDialog by remember { mutableStateOf<UserApp?>(null) }

  val dialogSheetScaffoldState =
    rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden) {
      if (it == ModalBottomSheetValue.Hidden) {
        currentAppConfirmationDialog = null
        currentAppInfoDialog = null
      }
      true
    }
  LaunchedEffect(key1 = true) {
    Log.d("MAIN_SCREEN", "launched effect")
    viewModel.uiEvent.collect { event ->
      Log.d("MAIN_SCREEN", "event: $event")
      when (event) {
        is UiEvent.ShowToast -> Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
        is UiEvent.OpenApplication -> {
          currentAppConfirmationDialog = event.app
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        is UiEvent.LaunchActivity -> mContext.startActivity(event.intent)
        is UiEvent.OpenAppDrawer -> {
          launch { bottomSheetScaffoldState.bottomSheetState.expand() }
          focusRequester.requestFocus()
        }
        is UiEvent.ExpandNotifications -> {
          setExpandNotificationDrawer(mContext,true)
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

      }
    }
  }

  LaunchedEffect(key1 = currentAppInfoDialog, key2 = currentAppConfirmationDialog) {
    if (currentAppInfoDialog != null || currentAppConfirmationDialog != null) {
      launch { dialogSheetScaffoldState.show() }
      keyboardController?.hide()
      focusRequester.freeFocus()
    } else {
      launch { dialogSheetScaffoldState.hide() }
    }
  }

  ModalBottomSheetLayout(
    sheetState = dialogSheetScaffoldState,
    sheetBackgroundColor = MaterialTheme.colorScheme.background,
    sheetContent = {
      Spacer(modifier = Modifier.height(4.dp))
      if (currentAppInfoDialog != null) {
        AppInfo(
          app = currentAppInfoDialog!!,
          onEvent = viewModel::onEvent,
          onDismiss = {
            coroutineScope.launch { dialogSheetScaffoldState.hide() }
            currentAppInfoDialog = null
          }
        )
      }
      if (currentAppConfirmationDialog != null) {
        val app = currentAppConfirmationDialog!!
        AppConfirmation(
          app = app,
          onConfirmation = {
            viewModel.onEvent(Event.LaunchActivity(app))
            currentAppConfirmationDialog = null
          },
          onDismiss = {
            coroutineScope.launch { dialogSheetScaffoldState.hide() }
            currentAppConfirmationDialog = null
          }
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  ) {
    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      sheetPeekHeight = 0.dp,
      backgroundColor = Color.Transparent,
      sheetContent = {
        AppList(
          focusRequester = focusRequester,
          onAppPress = { viewModel.onEvent(Event.OpenApplication(it)) },
          onAppLongPress = { currentAppInfoDialog = it },
          onBackPressed = {
            coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
            viewModel.onEvent(Event.UpdateSearch(""))
          }
        )
      },
    ) {
      FavoriteApps(
        onAppPressed = { currentAppInfoDialog = it },
        onNavigate = onNavigate
      )
    }
  }
}

@SuppressLint("WrongConstant")
fun setExpandNotificationDrawer(context: Context, expand: Boolean) {
  try {
    val statusBarService = context.getSystemService("statusbar")
    val methodName = if (expand) "expandNotificationsPanel" else "collapsePanels"
    val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
    val method: Method = statusBarManager.getMethod(methodName)
    method.invoke(statusBarService)
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
