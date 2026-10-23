/*
 * Copyright (c) 2025-2026 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.verifier.service

import com.gitb.core.ValueEmbeddingEnumeration
import com.gitb.vs.ValidateRequest
import eu.europa.ec.eudi.gitb.Utils
import eu.europa.ec.eudi.verifier.utils.Json
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VerifierValidationServiceLogTest {
    private val utils = Utils()
    private val service = VerifierValidationService(utils, Json)

    @Test
    @Disabled("Manual validation of logs")
    fun `validate a log and print the calculated result`() {
        val log =
            checkNotNull(javaClass.classLoader.getResource("verifier-log.json")) {
                "Add the verifier log to src/test/resources/verifier-log.json"
            }.readText()

        val request =
            ValidateRequest().apply {
                sessionId = "manual-log-validation"
                input.add(
                    utils.createAnyContentSimple(
                        "text",
                        log,
                        ValueEmbeddingEnumeration.STRING,
                    ),
                )
            }

        val report = service.validate(request).report

        println("RESULT: ${report.result}")
        println("ERRORS: ${report.counters.nrOfErrors}")
        println("WARNINGS: ${report.counters.nrOfWarnings}")

        report.context.item.forEach { item ->
            println("${item.name}: ${item.item.firstOrNull()?.value}")
        }
    }
}
