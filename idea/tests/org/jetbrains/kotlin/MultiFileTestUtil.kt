/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.test.KotlinTestUtils

data class TestFile(val name: String, val content: String)

fun createTestFiles(multiFileText: String) = KotlinTestUtils.createTestFiles(
        "single.kt",
        multiFileText,
        object : KotlinTestUtils.TestFileFactoryNoModules<TestFile>() {
            override fun create(fileName: String, text: String, directives: Map<String, String>): TestFile {
                var fileText = if (text.startsWith("// FILE")) StringUtil.substringAfter(text, "\n")!! else text
                return TestFile(fileName, fileText)
            }
        })
