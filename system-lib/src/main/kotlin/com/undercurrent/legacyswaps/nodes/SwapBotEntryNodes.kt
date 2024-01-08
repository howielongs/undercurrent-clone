package com.undercurrent.legacyswaps.nodes

import com.undercurrent.legacyshops.nodes.shared_nodes.CancelNode
import com.undercurrent.legacyswaps.SwapRole
import com.undercurrent.shared.view.treenodes.tnode
import com.undercurrent.system.context.SystemContext


class SwapBotEntryNodes(
    context: SystemContext,
) : SwapOperationNode(
    context
) {

    //todo perhaps check for current user permissions
    override suspend fun next(): tnode {
        return chooseSwapRoleForDemo()
    }

    fun chooseSwapRoleForDemo(): tnode {

        val choiceList = listOf(
            SwapRole.SWAP_ADMIN.name,
            SwapRole.BANKER.name,
            SwapRole.SWAPPER.name,
            "Cancel"
        )

        return menuSelectNode(
            options = listOf(
                SwapRole.SWAP_ADMIN.name,
                SwapRole.BANKER.name,
                SwapRole.SWAPPER.name,
                "Cancel"
            ),
            headerText = "Which role to use for this demo?",
            ifSuccess = { it, options ->
                val selectedValue = options[it]

                when (selectedValue) {
                    SwapRole.SWAP_ADMIN.name -> {
                        SwapAdminNodes(context).next()
                    }

                    SwapRole.BANKER.name -> {
                        SwapBankerNodes(context).next()
                    }

                    SwapRole.SWAPPER.name -> {
                        SwapperNodes(context).next()
                    }

                    "Cancel" -> {
                        CancelNode(context).next()
                    }

                    else -> {
                        sendOutput("Invalid choice. Please try again later.")
                        null
                    }
                }
            })

    }

}


//class SwapBotEntryNodes(
//    context: SystemContext,
//) : SwapOperationNode(context) {
//
//    //todo Do permissions check (on all startNodes)
//    override suspend fun next(): tnode {
//        return chooseSwapRoleForDemo()
//    }


//    fun chooseSwapRoleForDemo(): tnode {
//        val choicesMap = mapOf(
//            "a" to SwapRole.SWAP_ADMIN,
//            "b" to SwapRole.BANKER,
//            "c" to SwapRole.SWAPPER,
//            "d" to "Cancel"
//        )
//
//        val choicesList = choicesMap.values.toList()
//
//        val chooseRolePrompt = """
//            |Which role to use for this demo?
//            |
//            |${menuBuilder.buildMenu(choicesList)}
//        """.trimMargin()
//        return textInputNode(chooseRolePrompt,
//            ifSuccess = {
//                when (menuBuilder.map[it.asAbcIndex()]) {
//                    SwapRole.SWAP_ADMIN -> {
//                        "User selected SWAP_ADMIN".let {
//                            logger.info(it)
//                            println(it)
//                        }
//                        SwapAdminNodes(context).next()
//                    }
//
//                    SwapRole.BANKER -> {
//                        "User selected BANKER".let {
//                            logger.info(it)
//                            println(it)
//                        }
//                        SwapBankerNodes(context).next()
//                    }
//
//                    SwapRole.SWAPPER -> {
//                        "User selected SWAPPER".let {
//                            logger.info(it)
//                            println(it)
//                        }
//                        SwapperNodes(context).next()
//                    }
//
//                    "Cancel" -> {
//                        "User selected Cancel".let {
//                            logger.info(it)
//                            println(it)
//                        }
//                        CancelNode(context).next()
//                    }
//
//                    else -> {
//                        "User selected invalid choice".let {
//                            logger.info(it)
//                            println(it)
//                        }
//                        sendOutput("Invalid choice. Please try again later.")
//                        null
//                    }
//                }
//            })
//
//    }

//}
