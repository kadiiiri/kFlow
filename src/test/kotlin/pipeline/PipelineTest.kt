package pipeline

import operator.Operator
import org.junit.jupiter.api.Test

class PipelineTest {
    val image = "ubuntu:latest"
    @Test
    fun test() {
        val script1 = javaClass.getResource("/scripts/script1.sh")!!.readText()
        val script2 = javaClass.getResource("/scripts/script2.sh")!!.readText()
        val script3 = javaClass.getResource("/scripts/script3.sh")!!.readText()
        val script4 = javaClass.getResource("/scripts/script4.sh")!!.readText()
        val script5 = javaClass.getResource("/scripts/script5.sh")!!.readText()

        val op1 = Operator("first", image, script1)
        val op2 = Operator("second", image, script2)
        val op3 = Operator("third", image, script3)
        val op4 = Operator("fourth", image, script4)
        val op5 = Operator("fifth", image, script5)

        val pipeline = pipeline {
            specify {
                op1 pipe listOf(op2 pipe op3, op4) pipe op5
            }
        }

        pipeline.run()
    }
}