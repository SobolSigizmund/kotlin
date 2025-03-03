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

package org.jetbrains.kotlin.idea.completion.test.weighers;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/idea-completion/testData/weighers/smart")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class SmartCompletionWeigherTestGenerated extends AbstractSmartCompletionWeigherTest {
    public void testAllFilesPresentInSmart() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/idea-completion/testData/weighers/smart"), Pattern.compile("^([^\\.]+)\\.kt$"), true);
    }

    @TestMetadata("BooleanExpected.kt")
    public void testBooleanExpected() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/BooleanExpected.kt");
        doTest(fileName);
    }

    @TestMetadata("CallableReference_NothingLast.kt")
    public void testCallableReference_NothingLast() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/CallableReference_NothingLast.kt");
        doTest(fileName);
    }

    @TestMetadata("CallableReference_NothingLast2.kt")
    public void testCallableReference_NothingLast2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/CallableReference_NothingLast2.kt");
        doTest(fileName);
    }

    @TestMetadata("FunctionExpected.kt")
    public void testFunctionExpected() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/FunctionExpected.kt");
        doTest(fileName);
    }

    @TestMetadata("It.kt")
    public void testIt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/It.kt");
        doTest(fileName);
    }

    @TestMetadata("MultipleArgsItem.kt")
    public void testMultipleArgsItem() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/MultipleArgsItem.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarity1.kt")
    public void testNameSimilarity1() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarity1.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarity2.kt")
    public void testNameSimilarity2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarity2.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarity3.kt")
    public void testNameSimilarity3() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarity3.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityAndCompiledParameters.kt")
    public void testNameSimilarityAndCompiledParameters() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityAndCompiledParameters.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForAssignment.kt")
    public void testNameSimilarityForAssignment() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForAssignment.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForBlock.kt")
    public void testNameSimilarityForBlock() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForBlock.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForElse.kt")
    public void testNameSimilarityForElse() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForElse.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForElvis.kt")
    public void testNameSimilarityForElvis() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForElvis.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForEq1.kt")
    public void testNameSimilarityForEq1() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForEq1.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForEq2.kt")
    public void testNameSimilarityForEq2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForEq2.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForEq3.kt")
    public void testNameSimilarityForEq3() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForEq3.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForEq4.kt")
    public void testNameSimilarityForEq4() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForEq4.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForExpressionBody.kt")
    public void testNameSimilarityForExpressionBody() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForExpressionBody.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForGetterExpressionBody.kt")
    public void testNameSimilarityForGetterExpressionBody() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForGetterExpressionBody.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForGetterReturn.kt")
    public void testNameSimilarityForGetterReturn() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForGetterReturn.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForInitializer.kt")
    public void testNameSimilarityForInitializer() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForInitializer.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForReturn.kt")
    public void testNameSimilarityForReturn() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForReturn.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForThen.kt")
    public void testNameSimilarityForThen() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForThen.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityForVararg.kt")
    public void testNameSimilarityForVararg() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityForVararg.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilarityInImplicitlyTypedVarInitializer.kt")
    public void testNameSimilarityInImplicitlyTypedVarInitializer() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilarityInImplicitlyTypedVarInitializer.kt");
        doTest(fileName);
    }

    @TestMetadata("NameSimilaritySorterPlacement.kt")
    public void testNameSimilaritySorterPlacement() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NameSimilaritySorterPlacement.kt");
        doTest(fileName);
    }

    @TestMetadata("NoExpectedType.kt")
    public void testNoExpectedType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NoExpectedType.kt");
        doTest(fileName);
    }

    @TestMetadata("NoNameSimilarityForQualifier.kt")
    public void testNoNameSimilarityForQualifier() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NoNameSimilarityForQualifier.kt");
        doTest(fileName);
    }

    @TestMetadata("NullableExpected.kt")
    public void testNullableExpected() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/NullableExpected.kt");
        doTest(fileName);
    }

    @TestMetadata("ReturnValue1.kt")
    public void testReturnValue1() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/ReturnValue1.kt");
        doTest(fileName);
    }

    @TestMetadata("ReturnValue2.kt")
    public void testReturnValue2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/ReturnValue2.kt");
        doTest(fileName);
    }

    @TestMetadata("SmartPriority.kt")
    public void testSmartPriority() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/SmartPriority.kt");
        doTest(fileName);
    }

    @TestMetadata("SmartPriority2.kt")
    public void testSmartPriority2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/SmartPriority2.kt");
        doTest(fileName);
    }

    @TestMetadata("SmartPriority3.kt")
    public void testSmartPriority3() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/SmartPriority3.kt");
        doTest(fileName);
    }

    @TestMetadata("SuperMembers.kt")
    public void testSuperMembers() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/idea-completion/testData/weighers/smart/SuperMembers.kt");
        doTest(fileName);
    }
}
