package com.undercurrent.legacy.commands.executables.demos

import com.undercurrent.legacy.commands.registry.DemoCommand
import com.undercurrent.legacy.commands.registry.DemoCommand.*
import com.undercurrent.system.context.SessionContext

sealed class DemoCmds(
    thisCommand: DemoCommand,
    sessionContext: SessionContext
) : DemoExecutable(
    thisCommand,
    sessionContext
)

data class IntakeDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_INTAKE, sessionContext)


data class SecuritySysDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_SECURITYSYSTEMS, sessionContext)

data class TravelSchedDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_TRAVELSCHEDULE, sessionContext)

data class DeadmanDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_DEADMAN, sessionContext)

data class CredPrivMgmtDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_CREDMGMT, sessionContext)

data class DocShareDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_DOCSHARE, sessionContext)

data class SocEngDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DEMO_PSYOP, sessionContext)

data class DummyDemoCmd(
    override val sessionContext: SessionContext
) : DemoCmds(DUMMY, sessionContext)

