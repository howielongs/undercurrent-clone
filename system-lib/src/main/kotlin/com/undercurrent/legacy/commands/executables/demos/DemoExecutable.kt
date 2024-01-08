package com.undercurrent.legacy.commands.executables.demos

import com.undercurrent.legacy.commands.executables.abstractcmds.Executable
import com.undercurrent.legacy.commands.registry.DemoCommand
import com.undercurrent.legacy.commands.registry.TopCommand
import com.undercurrent.system.context.SessionContext
import com.undercurrent.legacy.types.string.PressAgent
import com.undercurrent.legacy.dinosaurs.prompting.cmd_prompting.UserInput

abstract class DemoExecutable(
    override val thisCommand: DemoCommand,
    sessionContext: SessionContext,
) : Executable(thisCommand, sessionContext) {

    override suspend fun execute() {
        with(sourceList(thisCommand)) {
            forEach { prompt ->
                if (prompt == this.last()) {
                    "Operation complete. Enter any key to continue."
                        .let { sessionContext.interrupt(it) }
                } else {
                    UserInput.promptUser(
                        sessionContext = sessionContext,
                        promptString = prompt
                    )
                }
            }
        }
        startNewCommand(TopCommand.START)
    }

    fun sourceList(demoCmd: DemoCommand = DemoCommand.DUMMY): List<String> {
        val returnList = when (demoCmd) {
            DemoCommand.DEMO_INTAKE -> {
                listOf(
                    "What is your current situation?",
                )
            }
            DemoCommand.DEMO_SECURITYSYSTEMS -> {
                listOf(
                    "Welcome to our home security interface, Please enter your access code.",
                    "Access Granted. Please enter your name for the entrance logs.",
                    "Thank you, John Smith. Your entrance has been logged. Please send the word /“help/” " +
                            "if you have any trouble and we will connect you to property management.\n",
                )
            }

            DemoCommand.DEMO_TRAVELSCHEDULE -> {
                listOf(
                    "There has been an update to Elon Musk’s travel schedule. Please input the access code to see the update.",
                    "Thank you, here is the schedule update -\n" +
                            "\n" +
                            "\tNew trip to Hong Kong added to schedule for June 21st, 2023\n" +
                            "\tLeaving 13:40 PST and arriving 18:35 CST\n" +
                            "\tAircraft: Gulfstream G650ER\n" +
                            "\tDeparting SFO, arriving to HKG\n" +
                            "\tNote: Please arrive early to prepare for Chinese Border\n" +
                            "\t*Attached PDF with additional details*\n",
                    "Do you foresee any conflicts with this new scheduling?${PressAgent.yesNoOptions()}",
                    "Thank you, if any conflicts arise please send the word “conflict” and we will pass that information on to the scheduling team.",
                )
            }

            DemoCommand.DEMO_DEADMAN -> {
                listOf(
                    "Welcome to Undercurrent’s deadman’s switch, this is a tool for journalists, whistleblowers, " +
                            "and other activists who fear scrutiny for sharing the truth.\n\n" +
                            "Would you like the tutorial?${PressAgent.yesNoOptions()}",
                    "A deadman’s switch will forward information to contacts of your choosing when you " +
                            "fail to check in on that information. For example, if you have an incriminating " +
                            "picture of someone who is rich and powerful, they may try to stop you " +
                            "from sharing this picture. Here you can upload the picture and set a " +
                            "check-in timer. If you set the timer to 24 hours then you must input " +
                            "your password once every 24 hours or the picture will be automatically " +
                            "forwarded to the contacts you have designated. You can choose signal " +
                            "contacts or email to distribute the information. You can set up a test " +
                            "document or picture to forward to yourself after a short timer to test " +
                            "the deadman’s switch, would you like to do this now?${PressAgent.yesNoOptions()}",
                    "Upload your pictures or documents now by attaching and sending to me.",
                    "Thank you, how long of a check-in timer would you like? (in minutes)",
                    "Timer set! Now add an alphanumeric password (minimum 8 characters).",
                    "Now set your receiving contacts (Signal enabled phone numbers or emails only)\n\n" +
                            "Send as many as you like, separated by commas.",
                    "Deadman’s switch engaged! Check back in 1 hour or your documents will be automatically forwarded!",
                )
            }

            DemoCommand.DEMO_CREDMGMT -> {
                listOf(
                    "Welcome to Undercurrent’s credential management system, which division would you like to access? " +
                            "(Select the letter of one of the options below)\n" +
                            "\n" +
                            "A. Quality Assurance and Testing\n" +
                            "B. Development\n" +
                            "C. Internal Tools \n" +
                            "D. Human Resources\n" +
                            "E. Financials\n" +
                            "F. Legal\n" +
                            "G. Update Production Environment\n",
                    "Your company role requires you to request access to the Financials data.\n\n" +
                            "Would you like to request access from your admin?${PressAgent.yesNoOptions()}",
                    "Please wait while this request is reviewed…\n\n",
                    //todo implement sending follow-up msg without user input
                    "Access Granted. Here is your one-time access code: AQQ3499",
                )
            }

            DemoCommand.DEMO_DOCSHARE -> {
                listOf(
                    "Welcome to Signal Docs. You have 4 documents uploaded, do you have your password?${PressAgent.yesNoOptions()}",
                    "Great, welcome back! Please input your password now.",
                    "Access Granted. What would you like to do? (Select the letter of one of the options below)\n" +
                            "\n" +
                            "A. Upload documents\n" +
                            "B. Delete documents\n" +
                            "C. Change permissions\n" +
                            "D. Set self-destruct timers\n" +
                            "E. Tutorial\n" +
                            "F. Settings\n",
                    "Would you like to add contacts with permissions or revoke permissions from a document? (Select the letter of one of the options below)\n" +
                            "\n" +
                            "A. Add contacts with permissions\n" +
                            "B. Revoke permissions from documents\n",
                    "You are only sharing one document, revoke all access?${PressAgent.yesNoOptions()}",
                    "All permissions for document #3 “Classified Stuff” has been revoked.",
                )
            }

            DemoCommand.DEMO_PSYOP -> {
                listOf(
                    "Scans of your employee communications logs show there were 4 potential " +
                            "social engineering attempts today, targeting 3 employees." +
                            "\n\nWould you like to review these events?${PressAgent.yesNoOptions()}",
                    "Two of these attempts were detected through the traditional email system and 1 of them through the Signal " +
                            "communications system. What information would you like to review? " +
                            "(Select the letter of one of the options below)\n" +
                            "\n" +
                            "A. Names and Manager information for each of the targets\n" +
                            "B. Which systems were targeted\n" +
                            "C. Download the relevant communications logs\n" +
                            "D. Exposed information of the attackers\n" +
                            "E. Cancel\n",
                    "Targeted Employee 1 - John Hopkin\n" +
                            "SE Attempts today - 1\n" +
                            "Total - 4\n" +
                            "Division - Human Resources\n" +
                            "Manager - Jill Perkins\n" +
                            "(555) 299-4500\n" +
                            "\n" +
                            "Targeted Employee 2 - Amy Swinelo\n" +
                            "SE Attempts today - 2\n" +
                            "Total - 8\n" +
                            "Division - R&D\n" +
                            "Manager - Jack Ledon\n" +
                            "(555) 498-5603\n" +
                            "\n" +
                            "Targeted Employee 3 - Jerry Grazi\n" +
                            "SE Attempts today: 1\n" +
                            "Total: 1\n" +
                            "Division - Legal\n" +
                            "Manager - Mannie May\n" +
                            "(555) 344-4335\n",
                    //todo impl follow-on msg
                    "What information would you like to review? (Select the letter of one of the options below)\n" +
                            "\n" +
                            "A. Names and Manager information for each of the targets\n" +
                            "B. Which systems were targeted\n" +
                            "C. Download the relevant communications logs\n" +
                            "D. Exposed information of the attackers\n" +
                            "E. Cancel\n",
                )
            }

            DemoCommand.DUMMY -> {
                listOf(
                    "Welcome!",
                    "Of course, right away!",
                    "Sure thing, boss",
                    "Would you like a warm towel while you wait?",
                    "How about a backrub?",
                )
            }
        }
        return returnList
    }
}
