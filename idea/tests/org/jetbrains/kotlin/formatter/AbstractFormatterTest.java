/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.formatter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.MultiFileTestUtilKt;
import org.jetbrains.kotlin.TestFile;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.test.PluginTestCaseBase;
import org.jetbrains.kotlin.test.InTextDirectivesUtils;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.SettingsConfigurator;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Based on from com.intellij.psi.formatter.java.AbstractJavaFormatterTest
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractFormatterTest extends LightIdeaTestCase {

    protected enum Action {REFORMAT, INDENT}

    private interface TestFormatAction {
        void run(PsiFile psiFile, int startOffset, int endOffset);
    }

    private static final Map<Action, TestFormatAction> ACTIONS = new EnumMap<Action, TestFormatAction>(Action.class);
    static {
        ACTIONS.put(Action.REFORMAT, new TestFormatAction() {
            @Override
            public void run(PsiFile psiFile, int startOffset, int endOffset) {
                CodeStyleManager.getInstance(getProject()).reformatText(psiFile, startOffset, endOffset);
            }
        });
        ACTIONS.put(Action.INDENT, new TestFormatAction() {
            @Override
            public void run(PsiFile psiFile, int startOffset, int endOffset) {
                CodeStyleManager.getInstance(getProject()).adjustLineIndent(psiFile, startOffset);
            }
        });
    }

    private static final String BASE_PATH =
            new File(PluginTestCaseBase.getTestDataPathBase(), "/formatter/").getAbsolutePath();

    public TextRange myTextRange;
    public TextRange myLineRange;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LanguageLevelProjectExtension.getInstance(getProject()).setLanguageLevel(LanguageLevel.HIGHEST);
    }

    protected void doMultiFileTest(String file) throws Exception {
        String multiFileText = FileUtil.loadFile(new File(file), true);

        List<TestFile> files = MultiFileTestUtilKt.createTestFiles(multiFileText);

        final TestFile afterFile = CollectionsKt.firstOrNull(files, new Function1<TestFile, Boolean>() {
            @Override
            public Boolean invoke(TestFile file) {
                return file.getName().contains(".after.kt");
            }
        });
        final TestFile afterInvFile = CollectionsKt.firstOrNull(files, new Function1<TestFile, Boolean>() {
            @Override
            public Boolean invoke(TestFile file) {
                return file.getName().contains(".after.inv.kt");
            }
        });
        final TestFile beforeFile = CollectionsKt.firstOrNull(files, new Function1<TestFile, Boolean>() {
            @Override
            public Boolean invoke(TestFile file) {
                return file.getName().contains(".before");
            }
        });

        assert beforeFile != null;
        assert afterFile != null || afterInvFile != null;
    }

    public void doTextTest(@NonNls String text, File fileAfter, String extension) throws IncorrectOperationException {
        doTextTest(Action.REFORMAT, text, fileAfter, extension);
    }

    public void doTextTest(final Action action, final String text, File fileAfter, String extension) throws IncorrectOperationException {
        final PsiFile file = createFile("A" + extension, text);

        if (myLineRange != null) {
            DocumentImpl document = new DocumentImpl(text);
            myTextRange =
                    new TextRange(document.getLineStartOffset(myLineRange.getStartOffset()), document.getLineEndOffset(myLineRange.getEndOffset()));
        }

        final PsiDocumentManager manager = PsiDocumentManager.getInstance(getProject());
        final Document document = manager.getDocument(file);

        CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        document.replaceString(0, document.getTextLength(), text);
                        manager.commitDocument(document);
                        try {
                            TextRange rangeToUse = myTextRange;
                            if (rangeToUse == null) {
                                rangeToUse = file.getTextRange();
                            }
                            ACTIONS.get(action).run(file, rangeToUse.getStartOffset(), rangeToUse.getEndOffset());
                        }
                        catch (IncorrectOperationException e) {
                            assertTrue(e.getLocalizedMessage(), false);
                        }
                    }
                });
            }
        }, "", "");


        if (document == null) {
            fail("Don't expect the document to be null");
            return;
        }
        KotlinTestUtils.assertEqualsToFile(fileAfter, document.getText());
        manager.commitDocument(document);
        KotlinTestUtils.assertEqualsToFile(fileAfter, file.getText());
    }

    public void doTest(@NotNull String expectedFileNameWithExtension) throws Exception {
        doTest(expectedFileNameWithExtension, false);
    }

    public void doTestInverted(@NotNull String expectedFileNameWithExtension) throws Exception {
        doTest(expectedFileNameWithExtension, true);
    }

    public void doTest(@NotNull String expectedFileNameWithExtension, boolean inverted) throws Exception {
        String testFileName = expectedFileNameWithExtension.substring(0, expectedFileNameWithExtension.indexOf("."));
        String testFileExtension = expectedFileNameWithExtension.substring(expectedFileNameWithExtension.lastIndexOf("."));
        String originalFileText = FileUtil.loadFile(new File(testFileName + testFileExtension), true);
        CodeStyleSettings codeStyleSettings = FormatSettingsUtil.getSettings();

        Integer rightMargin = InTextDirectivesUtils.getPrefixedInt(originalFileText, "// RIGHT_MARGIN: ");
        if (rightMargin != null) {
            codeStyleSettings.setRightMargin(KotlinLanguage.INSTANCE, rightMargin);
        }

        SettingsConfigurator configurator = FormatSettingsUtil.createConfigurator(originalFileText, codeStyleSettings);
        if (!inverted) {
            configurator.configureSettings();
        }
        else {
            configurator.configureInvertedSettings();
        }

        doTextTest(originalFileText, new File(expectedFileNameWithExtension), testFileExtension);

        codeStyleSettings.clearCodeStyleSettings();
    }
}
