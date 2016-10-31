package org.jetbrains.ktor.request

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.nio.*
import org.jetbrains.ktor.pipeline.*

class ReceivePipeline : Pipeline<ReceivePipeline.ReceiveState>(Before, Transform, After) {

    class ReceiveState(val call: ApplicationCall) {
        var message: Any = call.request.content.get<ReadChannel>()
    }

    companion object {
        val Before = PipelinePhase("Before")
        val Transform = PipelinePhase("Transform")
        val After = PipelinePhase("After")
    }
}