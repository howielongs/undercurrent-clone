package com.undercurrent.legacy.commands.registry


import com.undercurrent.legacy.commands.executables.demos.*
import com.undercurrent.legacy.dinosaurs.prompting.selectables.SelectableEnum
import com.undercurrent.shared.repository.dinosaurs.ExposedTableWithStatus2
import com.undercurrent.shared.types.enums.AppRole
import com.undercurrent.shared.types.enums.ShopRole

enum class DemoCommand(
    override val hint: String = "",
    override val permissions: Set<AppRole> = setOf(ShopRole.ADMIN),
    override val commandGroup: Set<TopCommand> = setOf(),
    override val priority: Int = 50,
    override val prompt: String? = null,
    override val callback: CallbackType = null,
    override val displayAs: String = "",
    override val handlerClass: HandlerClassType = null,
    override val entityClass: ExposedTableWithStatus2? = null,
    override val simpleHelp: String? = null,
    override val runnerFunc: RunnerFuncType = null

) : UserCommand {
    DEMO_INTAKE(
        "Intake system",
        setOf(ShopRole.ADMIN, ShopRole.VENDOR, ShopRole.CUSTOMER),
        handlerClass = IntakeDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),

    DEMO_SECURITYSYSTEMS(
        "Security Systems",
        setOf(ShopRole.ADMIN),
        handlerClass = SecuritySysDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DEMO_TRAVELSCHEDULE(
        "Travel Schedule",
        setOf(ShopRole.ADMIN),
        handlerClass = TravelSchedDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DEMO_DEADMAN(
        "Deadman`s Switch",
        setOf(ShopRole.ADMIN),
        handlerClass = DeadmanDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DEMO_CREDMGMT(
        "Credential and Privilege Management",
        setOf(ShopRole.ADMIN),
        handlerClass = CredPrivMgmtDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DEMO_DOCSHARE(
        "Secure Document Sharing",
        setOf(ShopRole.ADMIN),
        handlerClass = DocShareDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DEMO_PSYOP(
        "Social Engineering Scanning",
        setOf(ShopRole.ADMIN),
        handlerClass = SocEngDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    DUMMY(
        "Canned responses bot",
        setOf(ShopRole.ADMIN),
        handlerClass = DummyDemoCmd::class.java,
        commandGroup = setOf(TopCommand.DEMOS),
    ),
    ;

    override fun selectable(): SelectableEnum {
        return SelectableEnum(
            promptText = this.name.capitalize(),
            enumValue = this
        )
    }

    override fun lower(): String {
        return this.name.lowercase()
    }

    override fun upper(): String {
        return this.name.uppercase()
    }

    override fun withSlash(): String {
        return "/" + this.name.lowercase()
    }

    override fun parseToString(): String {
        return this.name.lowercase()
    }
}