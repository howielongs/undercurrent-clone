package com.undercurrent.shared.experimental

interface FsmEvent
interface FsmState

interface Fsm<T : FsmState, E : FsmEvent> {
    fun handleEvent(event: E)
    fun display(): String
}


//experimental Finite State Machine
abstract class BaseFsm<T : FsmState, E : FsmEvent>(private var currentState: T) : Fsm<T, E>