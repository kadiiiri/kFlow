package kubernetes

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.BatchV1Api.APIcreateNamespacedJobRequest
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import io.kubernetes.client.util.Config
import java.util.concurrent.TimeUnit

class KubeClient(
    private val namespace: String = "default"
) {
    private val client: ApiClient = Config.defaultClient()
    private val batchApi: BatchV1Api = BatchV1Api(client)
    private val coreApi: CoreV1Api = CoreV1Api(client)
    private val log: KLogger = KotlinLogging.logger {}
    private val configMapName: String = "script-config"
    private val volumeName: String = "script-volume"

    fun run(podName: String, image: String, script: String): String {
        createNamespacedConfigMap(podName, script)
        val job = createNamespacedJob(podName, image)
        val result = job.execute()
        return result.metadata.name
    }

    fun awaitCompletion(jobName: String) {
        log.info { "Waiting for completion of job with name '$jobName'" }

        while (true) {
            try {
                val jobResult = batchApi.readNamespacedJob(jobName, namespace).execute()
                val jobStatus = jobResult.status

                if (jobStatus?.succeeded != null && jobStatus.succeeded!! > 0) {
                    log.info { "Job '$jobName' succeeded" }
                    break
                } else if (jobStatus?.failed != null && jobStatus.failed!! > 0) {
                    log.error { "Job '$jobName' failed" }
                    break
                }

                log.info { "Job '$jobName' is still running..." }
                TimeUnit.SECONDS.sleep(3)

            } catch (e: ApiException) {
                log.error { "Exception when reading Job '$jobName' status: ${e.responseBody}" }
                e.printStackTrace()
                break
            }
        }
    }

    private fun createNamespacedJob(podName: String, image: String): APIcreateNamespacedJobRequest {
        val body = V1Job().apply {
            metadata = V1ObjectMeta().apply { generateName = "$podName-" }
            spec = V1JobSpec().apply {
                backoffLimit = 3
                template = V1PodTemplateSpec().apply {
                    metadata = V1ObjectMeta().generateName(podName)
                    spec = V1PodSpec().apply {
                        containers = listOf(V1Container().apply {
                            name = "$podName-pod-runner"
                            command = listOf("/bin/sh")
                            args = listOf("-xc", "/scripts/$podName.sh")
                            this.image = image
                            volumeMounts = listOf(
                                V1VolumeMount().apply {
                                    name = volumeName
                                    mountPath = "/scripts"
                                }
                            )
                        })
                        restartPolicy = "Never"
                        volumes = listOf(
                            V1Volume().apply {
                                name = volumeName
                                configMap = V1ConfigMapVolumeSource().apply {
                                    name = "$podName-$configMapName"
                                    defaultMode = 511
                                }
                            }
                        )
                    }
                }
            }
        }
        return batchApi.createNamespacedJob(namespace, body)
    }

    private fun createNamespacedConfigMap(podName: String, script: String) {
        val configMap = V1ConfigMap()
            .metadata(V1ObjectMeta().name("$podName-$configMapName"))
            .putDataItem("$podName.sh", script)

        try {
            coreApi.createNamespacedConfigMap(
                namespace,
                configMap
            ).execute()
        } catch (e: ApiException) {
            log.info { "ConfigMap already exists: ${e.responseBody}" }
        }
    }
}