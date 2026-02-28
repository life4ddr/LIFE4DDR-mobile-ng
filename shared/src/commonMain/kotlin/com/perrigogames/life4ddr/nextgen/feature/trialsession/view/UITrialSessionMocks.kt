package com.perrigogames.life4ddr.nextgen.feature.trialsession.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionInput
import com.perrigogames.life4ddr.nextgen.view.UISongJacket
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.ImageDescUrl

object UITrialSessionMocks {

    val initial = UITrialSession(
        trialTitle = "Sidequest".desc(),
        trialLevel = "LV 14".desc(),
        backgroundImage = ImageDescUrl("https://raw.githubusercontent.com/life4ddr/Life4DDR/develop/androidApp/src/main/res/drawable-xxhdpi/sidequest.webp"),
        targetRank = UITargetRank(
            rank = TrialRank.COBALT,
            title = "COBALT".desc(),
            titleColor = MR.colors.cobalt,
            rankGoalItems = listOf(
                "20 or fewer Greats, Goods, or Misses".desc(),
                "230 missing EX or less (6532 EX)".desc(),
            ),
            availableRanks = listOf(TrialRank.SILVER, TrialRank.GOLD, TrialRank.PLATINUM, TrialRank.DIAMOND, TrialRank.COBALT),
            state = UITargetRank.State.UNSELECTABLE
        ),
        content = UITrialSessionContent.Summary(
            items = listOf(
                UITrialSessionContent.Summary.Item(
                    jacket = UISongJacket.WithUrl("https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/Be_a_Hero!.webp"),
                    difficultyClassText = "DIFFICULT".desc(),
                    difficultyClassColor = MR.colors.difficultyDifficult,
                    difficultyNumberText = "13".desc(),
                ),
                UITrialSessionContent.Summary.Item(
                    jacket = UISongJacket.WithUrl("https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/Role-playing_game.webp"),
                    difficultyClassText = "EXPERT".desc(),
                    difficultyClassColor = MR.colors.difficultyExpert,
                    difficultyNumberText = "14".desc(),
                ),
                UITrialSessionContent.Summary.Item(
                    jacket = UISongJacket.WithUrl("https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/Kanata_no_Reflesia.webp"),
                    difficultyClassText = "EXPERT".desc(),
                    difficultyClassColor = MR.colors.difficultyExpert,
                    difficultyNumberText = "15".desc(),
                ),
                UITrialSessionContent.Summary.Item(
                    jacket = UISongJacket.WithUrl("https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/Boss+Rush-jacket.webp"),
                    difficultyClassText = "DIFFICULT".desc(),
                    difficultyClassColor = MR.colors.difficultyDifficult,
                    difficultyNumberText = "14".desc(),
                ),
            ),
        ),
        footer = UITrialSession.Footer.Button(
            buttonText = "Start Trial".desc(),
            buttonAction = TrialSessionInput.StartTrial(fromDialog = true),
        ),
    )

    val exScoreBar = UIEXScoreBar(
        labelText = "EX".desc(),
        currentEx = 0,
        maxEx = 6762,
        currentExText = "0".desc(),
        maxExText = "/ 6762".desc(),
        exTextClickAction = TrialSessionInput.ToggleExLost(false),
    )
}