package pipeline

import operator.Operator
class Pipeline {
    private var rootOperator: Operator? = null
    fun addOperator(operator: Operator) {
        if (rootOperator == null) {
            rootOperator = operator
        } else {
            rootOperator!!.addChild(operator)
        }
    }

    fun run() {
        requireNotNull(rootOperator) { "No operators defined for this pipeline." }
        rootOperator!!.execute()
    }
}

// DSL Functions
infix fun Pipeline.specify(description: Pipeline.() -> Operator): Operator {
    val topOperator = this.description()
    addOperator(topOperator)
    return topOperator
}
infix fun Operator.pipe(operators: List<Operator>): Operator {
    this.addChildren(operators)
    return this
}

infix fun Operator.pipe(operator: Operator): Operator {
    this.addChild(operator)
    return this
}

fun pipeline(specify: Pipeline.() -> Unit): Pipeline {
    val pipeline = Pipeline()
    pipeline.specify()
    return pipeline
}