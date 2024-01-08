package com.undercurrent.legacy.service.crypto

import com.undercurrent.legacy.routing.RunConfig
import com.undercurrent.shared.utils.Util

class BtcWalletAddresses {
    companion object {
        const val DISABLE_WALLET_ON_MAC = true
        const val ALLOW_LIVE_BTC_WALLET_WITH_TESTS = false

        const val addressV1 = "tb1qc3svlvwjr3y2l3ful6mgvn44nvrdz55xwhdy0k"
        const val addressV2 = "tb1qv2gzvsla9rgzj5gnrpg9qcjdrk3642rc3q6xq6"
        const val addressA1 = "tb1qnadkw5ucewh09vrrzcm3nt4eesv6vhrxgxldkk"
        const val addressA2 = "tb1qdclzg6eyut963vncdcwxg4sga703j957sredfw"

        fun useMockWalletForTests(): Boolean {
            if (Util.isMacOs()) {
                return false
            }

            return (DISABLE_WALLET_ON_MAC && Util.isMacOs()) ||
                    (RunConfig.isTestMode && !ALLOW_LIVE_BTC_WALLET_WITH_TESTS)
        }
    }
}