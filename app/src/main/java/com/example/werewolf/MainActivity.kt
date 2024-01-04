package com.example.werewolf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.werewolf.ui.theme.WerewolfTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    var numberOfPlayer = 1
    var roleList = mutableListOf<String>("Seer")
    var nameList = mutableListOf<String>()
    var isAlive = Array(1) { true }
    var winnerTeam = ""
    var werewolfKillVote = Array(numberOfPlayer) { 0 }
    var voting = Array(numberOfPlayer + 1) { 0 }
    var winCountingIndex = mutableListOf<Int>()
    var winCountingAlive = mutableListOf<Boolean>()
    var numberOfVillager = 0
    var numberOfWerewolf = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WerewolfTheme {
                Navigator()
            }
        }
    }


    @Composable
    fun Navigator() {

        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "MainScreen") {
            composable("MainScreen") {
                MainScreen(navigation = navController)
            }
            composable("RoleReveal") {
                RoleReveal(navigation = navController)
            }
            composable("NightTime") {
                NightTime(navigation = navController)
            }
            composable("DayTime") {
                DayTime(navigation = navController)
            }
            composable("VillagerVote") {
                VillagerVote(navigation = navController)
            }
            composable("NightKillVote") {
                NightKillVote(navigation = navController)
            }
            composable("Winner") {
                Winner(navigation = navController)
            }
        }
    }

    @Composable
    fun Winner(navigation: NavController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(340F, 1F, 0.63F)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$winnerTeam wins.",
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Button(
                modifier = Modifier
                    .padding(vertical = 100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                onClick = {
                    navigation.navigate("MainScreen")
                }
            ) {
                Text(
                    text = "Play again",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.hsl(340F, 1F, 0.63F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }


    @Composable
    fun VillagerVote(navigation: NavController) {
        var maxVillagerVote by remember { mutableStateOf(0) }
        var maxCount by remember { mutableStateOf(0) }
        var villagerVotePassed by remember { mutableStateOf(false) }

        if (!villagerVotePassed) {
            maxVillagerVote = 0
            maxCount = 0

            maxVillagerVote = voting.maxOrNull() ?: 0
            for (i in voting) {
                if (i >= maxVillagerVote) {
                    maxCount += 1
                }
            }

            if (maxCount <= 1 && voting.indexOf(maxVillagerVote) != 0) {
                isAlive[voting.indexOf(maxVillagerVote) - 1] = false
            }
            villagerVotePassed = true
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(340F, 1F, 0.63F)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            if (maxCount <= 1 && voting.indexOf(maxVillagerVote) != 0) {

                Text(
                    modifier = Modifier
                        .padding(top = 100.dp),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(color = Color.White),
                    text = "${nameList[voting.indexOf(maxVillagerVote) - 1]} was voted to be kill."
                )
                Text(
                    modifier = Modifier
                        .padding(bottom = 100.dp),
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    style = TextStyle(color = Color.White),
                    text = "${nameList[voting.indexOf(maxVillagerVote) - 1]} is " +
                            roleList[voting.indexOf(maxVillagerVote) - 1]
                )
            } else if (maxCount > 1 || voting.indexOf(maxVillagerVote) == 0) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 100.dp),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    text = "No one die today",
                    style = TextStyle(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Button(
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier
                    .padding(vertical = 100.dp),
                onClick = {
                    var werewolfAlive = 0
                    var villagerAlive = 0
                    winCountingIndex =
                        roleList.mapIndexedNotNull { index, s -> if (s == "Werewolf") index else null }
                            .toMutableList()
                    winCountingAlive = mutableListOf()
                    for (i in winCountingIndex) {
                        winCountingAlive.add(isAlive[i])
                    }
                    werewolfAlive = winCountingAlive.count { it }

                    winCountingIndex =
                        roleList.mapIndexedNotNull { index, s -> if (s != "Werewolf") index else null }
                            .toMutableList()
                    winCountingAlive = mutableListOf()
                    for (i in winCountingIndex) {
                        winCountingAlive.add(isAlive[i])
                    }
                    villagerAlive = winCountingAlive.count { it }

                    if (villagerAlive <= werewolfAlive) {
                        navigation.navigate("Winner")
                        winnerTeam = "Werewolf"
                    } else if (werewolfAlive <= 0) {
                        navigation.navigate("Winner")
                        winnerTeam = "Villager"
                    } else {
                        navigation.navigate("NightTime")
                    }
                }
            ) {
                Text(
                    text = "Done",
                    fontSize = 24.sp,
                    style = TextStyle(
                        color = Color.hsl(340F, 1F, 0.63F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

    }

    @Composable
    fun NightKillVote(navigation: NavController) {
        var maxwereWolfVote by remember { mutableStateOf(0) }
        var maxCount by remember { mutableStateOf(0) }
        var werewolfVotePassed by remember { mutableStateOf(false) }


        if (!werewolfVotePassed) {
            maxwereWolfVote = 0
            maxCount = 0

            maxwereWolfVote = werewolfKillVote.maxOrNull() ?: 0
            for (i in werewolfKillVote) {
                if (i >= maxwereWolfVote) {
                    maxCount += 1

                }
            }

            if (maxCount <= 1) {
                isAlive[werewolfKillVote.indexOf(maxwereWolfVote)] = false
            }
            werewolfVotePassed = true

        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(340F, 1F, 0.63F)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            if (maxCount <= 1) {
                Text(
                    modifier = Modifier
                        .padding(top = 100.dp),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    text = "${nameList[werewolfKillVote.indexOf(maxwereWolfVote)]} was killed.",
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    modifier = Modifier
                        .padding(bottom = 100.dp),
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    text = "${nameList[werewolfKillVote.indexOf(maxwereWolfVote)]} is " +
                            roleList[werewolfKillVote.indexOf(maxwereWolfVote)] + ".",
                    style = TextStyle(color = Color.White)
                )
            } else {
                Text(
                    modifier = Modifier
                        .padding(vertical = 100.dp),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    text = "No one was killed by werewolf"
                )
            }

            Button(
                colors = ButtonDefaults.buttonColors(Color.White),
                modifier = Modifier
                    .padding(vertical = 100.dp),
                onClick = {
                    var werewolfAlive = 0
                    var villagerAlive = 0
                    winCountingIndex =
                        roleList.mapIndexedNotNull { index, s -> if (s == "Werewolf") index else null }
                            .toMutableList()
                    winCountingAlive = mutableListOf()
                    for (i in winCountingIndex) {
                        winCountingAlive.add(isAlive[i])
                    }
                    werewolfAlive = winCountingAlive.count { it }

                    winCountingIndex =
                        roleList.mapIndexedNotNull { index, s -> if (s != "Werewolf") index else null }
                            .toMutableList()
                    winCountingAlive = mutableListOf()
                    for (i in winCountingIndex) {
                        winCountingAlive.add(isAlive[i])
                    }
                    villagerAlive = winCountingAlive.count { it }

                    if (villagerAlive <= werewolfAlive) {
                        navigation.navigate("Winner")
                        winnerTeam = "Werewolf"
                    } else if (werewolfAlive <= 0) {
                        navigation.navigate("Winner")
                        winnerTeam = "Villager"
                    } else {
                        navigation.navigate("DayTime")
                    }
                }
            ) {
                Text(
                    text = "Done",
                    fontSize = 24.sp,
                    style = TextStyle(
                        color = Color.hsl(340F, 1F, 0.63F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }


    }


    @Composable
    fun DayTime(navigation: NavController) {
        var playerId by remember { mutableStateOf(1) }
        var timeleft = 300
        var secLeft by remember { mutableStateOf(0) }
        var minLeft by remember { mutableStateOf(5) }
        var isVoting by remember { mutableStateOf(false) }
        val textColor = Color.Black
        var gridVisible by remember { mutableStateOf(true) }
        var voteIsReveal by remember { mutableStateOf(false) }
        var dayTimeVotePass by remember { mutableStateOf(false) }

        if (!dayTimeVotePass) {
            voting = Array(numberOfPlayer + 1) { 0 }
            for (i in 0..<voting.size) {
                voting[i] = 0
            }
            dayTimeVotePass = true
        }

        if (!isVoting) {
            LaunchedEffect(key1 = timeleft) {
                while (timeleft > 0) {
                    delay(1000L)
                    timeleft--
                    secLeft = timeleft % 60
                    minLeft = timeleft / 60
                    if (timeleft == 0) isVoting = true
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.hsl(340F, 1F, 0.63F)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    modifier = Modifier
                        .padding(vertical = 100.dp),
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    text = "Time left\n$minLeft:${secLeft.toString().padStart(2, '0')}",
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                )

                Button(
                    modifier = Modifier
                        .padding(vertical = 100.dp),
                    onClick = {
                        timeleft = 0
                        isVoting = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Vote Now",
                        fontSize = 24.sp,
                        style = TextStyle(
                            color = Color.hsl(340F, 1F, 0.63F),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

            }

        } else {

            if (isAlive[playerId - 1]) {
                AnimatedVisibility(
                    visible = !voteIsReveal,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.hsl(340F, 1F, 0.63F)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 100.dp),
                            color = Color.White,
                            fontSize = 36.sp,
                            text = "${nameList[playerId - 1]} turn",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                        )

                        Button(
                            colors = ButtonDefaults.buttonColors(Color.White),
                            onClick = {
                                voteIsReveal = true
                            }
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 24.sp,
                                style = TextStyle(
                                    color = Color.hsl(340F, 1F, 0.63F),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = voteIsReveal,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.hsl(340F, 1F, 0.63F)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(top = 40.dp),
                            text = "${nameList[playerId - 1]} turn to vote",
                            color = Color.White,
                            fontSize = 36.sp,
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        if (gridVisible) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                for (i in 1..numberOfPlayer) {
                                    if (isAlive[i - 1]) {
                                        items(1) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .background(
                                                        color = Color.White,
                                                        shape = RoundedCornerShape(20.dp)
                                                    )
                                                    .clickable {
                                                        voting[i] += 1
                                                        gridVisible = false
                                                    }
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .padding(vertical = 80.dp),
                                                    color = Color.hsl(340F, 1F, 0.63F),
                                                    text = nameList[i - 1],
                                                    fontSize = 24.sp,
                                                    style = TextStyle(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                items(1) {
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .background(
                                                color = Color.hsl(340F, 1F, 0.63F),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                voting[0] = voting[0] + 1
                                                gridVisible = false

                                            }
                                            .border(
                                                border = BorderStroke(
                                                    width = 5.dp,
                                                    Color.White
                                                )
                                            )
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(vertical = 80.dp),
                                            color = Color.White,
                                            text = "Skip",
                                            fontSize = 24.sp,
                                            style = TextStyle(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 100.dp)
                                        .clickable {
                                            if (playerId < numberOfPlayer) {
                                                playerId += 1
                                                voteIsReveal = false
                                                gridVisible = true
                                            } else {
                                                navigation.navigate("VillagerVote")
                                            }

                                        },
                                    text = "This person has been chosen\n\n\nTap to continue",
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            } else {
                if (playerId < numberOfPlayer) {
                    playerId += 1
                    gridVisible = true
                } else {
                    navigation.navigate("VillagerVote")
                }
            }
        }

    }

    @Composable
    fun NightTime(navigation: NavController) {
        var playerId by remember { mutableIntStateOf(1) }
        var playerRole by remember { mutableStateOf("") }
        var roleIsReveal by remember { mutableStateOf(false) }
        var gridVisible by remember { mutableStateOf(true) }
        var seerReveal by remember { mutableStateOf("") }
        val textColor = Color.White
        var nightTimeVotePass by remember { mutableStateOf(false) }

        if (!nightTimeVotePass) {
            werewolfKillVote = Array(numberOfPlayer) { 0 }
            for (i in 0..<werewolfKillVote.size) {

                werewolfKillVote[i] = 0
            }
            nightTimeVotePass = true
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 10.dp),
                color = textColor,
                fontSize = 36.sp,
                text = "Night Time"
            )

            if (isAlive[playerId - 1]) {
                AnimatedVisibility(
                    visible = !roleIsReveal,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 100.dp),
                            color = Color.White,
                            fontSize = 36.sp,
                            text = "${nameList[playerId - 1]} turn",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                        )

                        Button(onClick = {
                            roleIsReveal = true
                            playerRole = roleList[playerId - 1]
                        }
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 24.sp,
                                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = roleIsReveal,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (playerRole) {
                        "Seer" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Select person to view role",
                                    color = textColor,
                                    fontSize = 24.sp
                                )
                                if (gridVisible) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        for (i in 1..nameList.size) {
                                            if (isAlive[i - 1]) {
                                                items(1) { name ->
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(10.dp)
                                                            .background(
                                                                color = Color(0xFFFFFFFF),
                                                                shape = RoundedCornerShape(20.dp)
                                                            )
                                                            .clickable {
                                                                seerReveal = roleList[i - 1]
                                                                gridVisible = false
                                                            }
                                                    ) {
                                                        Text(
                                                            modifier = Modifier
                                                                .align(Alignment.Center)
                                                                .padding(vertical = 80.dp),
                                                            text = nameList[i - 1],
                                                            fontSize = 24.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 100.dp)
                                                .clickable {
                                                    if (playerId < numberOfPlayer) {
                                                        playerId = playerId + 1
                                                        roleIsReveal = false
                                                        gridVisible = true
                                                    } else {
                                                        navigation.navigate("NightKillVote")
                                                    }


                                                },
                                            text = "This player is $seerReveal\n\n\nTap to continue",
                                            color = textColor,
                                            textAlign = TextAlign.Center,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                            }

                        }

                        "Werewolf" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Vote person to kill",
                                    color = textColor,
                                    fontSize = 24.sp
                                )
                                if (gridVisible) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.spacedBy(20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        for (i in 0..<nameList.size) {
                                            if (isAlive[i]) {
                                                items(1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(10.dp)
                                                            .background(
                                                                color = Color(0xFFFFFFFF),
                                                                shape = RoundedCornerShape(20.dp)
                                                            )
                                                            .clickable {
                                                                werewolfKillVote[i] += 1
                                                                gridVisible = false
                                                            }
                                                    ) {
                                                        Text(
                                                            modifier = Modifier
                                                                .align(Alignment.Center)
                                                                .padding(vertical = 80.dp),
                                                            text = nameList[i],
                                                            fontSize = 24.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(vertical = 100.dp)
                                                .clickable {
                                                    if (playerId < numberOfPlayer) {
                                                        playerId = playerId + 1
                                                        roleIsReveal = false
                                                        gridVisible = true
                                                    } else {
                                                        navigation.navigate("NightKillVote")
                                                    }

                                                },
                                            text = "This person has been chosen\n\n\nTap to continue",
                                            color = textColor,
                                            textAlign = TextAlign.Center,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 100.dp)
                                        .clickable {
                                            if (playerId < numberOfPlayer) {
                                                playerId = playerId + 1
                                                roleIsReveal = false
                                                gridVisible = true
                                            } else {
                                                navigation.navigate("NightKillVote")
                                            }

                                        },
                                    text = "You have nothing to do.\nGo back to sleep\n\n\nTap to continue",
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            } else {
                if (playerId < numberOfPlayer) {
                    playerId = playerId + 1
                    gridVisible = true
                } else {
                    navigation.navigate("NightKillVote")
                }
            }
        }
    }


    @Composable
    fun RoleReveal(navigation: NavController) {
        var playerId by remember { mutableStateOf(1) }
        var playerRole by remember { mutableStateOf("") }
        var roleIsReveal by remember { mutableStateOf(false) }
        var inputName by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(340F, 1F, 0.63F)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !roleIsReveal,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 100.dp),
                        text = "Enter Player $playerId name\nand tap done to reveal role.",
                        textAlign = TextAlign.Center,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    TextField(
                        value = inputName,
                        onValueChange = { text -> inputName = text },
                        modifier = Modifier.padding(vertical = 100.dp),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        placeholder = { Text("Player $playerId", textAlign = TextAlign.Center) }
                    )

                    Button(colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        onClick = {
                            roleIsReveal = true
                            playerRole = roleList[playerId - 1]
                            nameList.add(if (inputName != "") inputName else "Player $playerId")
                            inputName = ""
                        }
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 24.sp,
                            style = TextStyle(
                                color = Color.hsl(340F, 1F, 0.63F),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = roleIsReveal,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 100.dp),
                        text = "You are $playerRole",
                        fontSize = 36.sp,
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                    )

                    if (roleIsReveal) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            onClick = {
                                roleIsReveal = false
                                if (playerId < numberOfPlayer) {
                                    playerId = playerId + 1
                                } else {
                                    navigation.navigate("NightTime")
                                }
                            }
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 24.sp,
                                style = TextStyle(
                                    color = Color.hsl(340F, 1F, 0.63F),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MainScreen(navigation: NavController) {
        var inputNumber by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.hsl(340F, 1F, 0.63F)),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                modifier = Modifier.padding(vertical = 100.dp),
                text = "Insert total player",
                fontSize = 36.sp,
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
            )
            TextField(
                value = inputNumber,
                onValueChange = { text -> inputNumber = text },
                modifier = Modifier
                    .padding(vertical = 100.dp)
                    .background(Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.Black
                ),
            )
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                onClick = {
                    if (inputNumber != "") {
                        numberOfPlayer = inputNumber.toInt()
                        roleList = mutableListOf<String>("Seer")
                        nameList = mutableListOf<String>()
                        numberOfWerewolf = (numberOfPlayer / 4)
                        if (numberOfWerewolf == 0) numberOfWerewolf = 1
                        for (i in 1..numberOfWerewolf) {
                            roleList.add("Werewolf")
                        }

                        numberOfVillager = numberOfPlayer - numberOfWerewolf
                        for (i in 1..<numberOfVillager) {
                            roleList.add("Villager")
                        }
                        roleList = roleList.shuffled().toMutableList()
                        isAlive = Array(numberOfPlayer) { true }
                        werewolfKillVote = Array(numberOfPlayer) { 0 }
                        navigation.navigate("RoleReveal")
                    }
                }) {
                Text(
                    text = "Play",
                    fontSize = 24.sp,
                    style = TextStyle(
                        color = Color.hsl(340F, 1F, 0.63F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}