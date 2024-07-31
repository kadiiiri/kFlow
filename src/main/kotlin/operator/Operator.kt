package operator

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kubernetes.KubeClient


class Operator(
    private val name: String,
    private val image: String,
    private val script: String) {
    private val log: KLogger = KotlinLogging.logger {}
    private val kubeClient: KubeClient = KubeClient();
    private val children: MutableList<Operator> = mutableListOf()

    fun addChildren(operators: List<Operator>) {
        children.addAll(operators)
    }

    fun addChild(operator: Operator) {
        children.add(operator)
    }

    fun execute() {
        log.info { "Starting execution of operator '$name' with image '$image' and script '$script'" }
        val job = kubeClient.run(name, image, script)
        kubeClient.awaitCompletion(job)
        children.forEach { it.execute() }
    }

    fun logTree(indent: String = "") {
        log.info { "$indent$name" }
        children.forEach { it.logTree("$indent  ") }
    }

    override fun toString(): String {
        return name
    }
}