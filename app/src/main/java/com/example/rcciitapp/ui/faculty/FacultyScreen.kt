package com.example.rcciitapp.ui.faculty

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.rcciitapp.navigation.Destination
import com.example.rcciitapp.ui.components.FacultyAppBar
import com.example.rcciitapp.viewModel.CourseState
import com.example.rcciitapp.viewModel.CoursesViewModel

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FacultyScreen(
    modifier: Modifier = Modifier,
    courseId: String,
    navController: NavHostController,
) {
    val viewModel: CoursesViewModel = hiltViewModel()
    viewModel.selectCourse(courseId)
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    val isAdminLoggedIn = viewModel.isAdminLoggedIn.collectAsStateWithLifecycle().value
    Scaffold(topBar = {
        FacultyAppBar(
            title = "Faculty",
            onCancelled = { },
            onAddClicked = {
                navController.navigate(Destination.AddFaculty.path)
            },
            topAppBarState = topAppBarState, isAdminLoggedIn = isAdminLoggedIn
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(
                    0.dp,
                    innerPadding.calculateTopPadding(),
                    0.dp,
                    innerPadding.calculateBottomPadding()
                )
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentAlignment = Alignment.TopCenter
        ) {
            viewModel.selectCourse(courseId)
            FacultySection(
                uiState = viewModel.courseUiState.collectAsStateWithLifecycle().value,
                isAdminLoggedIn = isAdminLoggedIn,
            )
        }

    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun FacultySection(
    modifier: Modifier = Modifier,
    uiState: CourseState,
    isAdminLoggedIn: Boolean,
) {
    val uiFacultyState = uiState.isSelectedCourse

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn() {

            if (uiFacultyState != null) {
                items(items = uiFacultyState.faculty, key = { item -> item.id }) { faculty ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            when (it) {
                                DismissValue.DismissedToEnd -> {
                                    // Do Something when swipe Start To End
                                    true
                                }

                                DismissValue.DismissedToStart -> {
                                    // Do Something when swipe End To Start
                                    true
                                }

                                else -> {
                                    false
                                }
                            }
                        }
                    )
                    var willDismissDirection: DismissDirection? by remember {
                        mutableStateOf(null)
                    }
                    if (!isAdminLoggedIn) {
                        FacultyCard(faculty = faculty)
                    } else {
                        SwipeToDismiss(
                            directions = setOf(
                                DismissDirection.StartToEnd,
                                DismissDirection.EndToStart
                            ),
                            dismissThresholds = { FractionalThreshold(0.15f) },
                            state = dismissState,
                            background = {
                                SwipeBackground(dismissState = dismissState)
                            }
                        ) {
                            FacultyCard(faculty = faculty)
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return

    val color by animateColorAsState(
        targetValue =
        when (dismissState.targetValue) {
            DismissValue.Default -> Color.LightGray
            DismissValue.DismissedToEnd -> Color.Red
            DismissValue.DismissedToStart -> Color.Green
        },
        animationSpec = tween()
    )
    val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Delete
        DismissDirection.EndToStart -> Icons.Default.Edit
    }
    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = "Localized description",
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
fun FacultyCardRightSwipeBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterStart),
            imageVector = Icons.Default.Delete,
            contentDescription = null
        )
    }
}

@Composable
fun FacultyCardLeftSwipeBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.CenterStart),
            imageVector = Icons.Default.Edit,
            contentDescription = null
        )
    }
}