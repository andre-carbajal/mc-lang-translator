package net.andrecarbajal.mclangtranslator.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TranslateLangFileActionTest : BasePlatformTestCase() {
    fun testActionVisibleOnlyForJsonFilesInsideLangDirectory() {
        val action = TranslateLangFileAction()
        val langFile = myFixture.addFileToProject("src/main/resources/assets/demo/lang/en_us.json", """{"item.demo.name":"Demo"}""")
        val outsideLangFile = myFixture.addFileToProject("src/main/resources/assets/demo/en_us.json", """{"item.demo.name":"Demo"}""")

        val visiblePresentation = Presentation()
        action.update(eventFor(langFile.virtualFile, visiblePresentation))
        assertTrue(visiblePresentation.isEnabledAndVisible)

        val hiddenPresentation = Presentation()
        action.update(eventFor(outsideLangFile.virtualFile, hiddenPresentation))
        assertFalse(hiddenPresentation.isEnabledAndVisible)
    }

    private fun eventFor(file: Any, presentation: Presentation): AnActionEvent {
        val context = DataContext { dataId ->
            when {
                CommonDataKeys.VIRTUAL_FILE.`is`(dataId) -> file
                else -> null
            }
        }
        return AnActionEvent.createFromDataContext("test", presentation, context)
    }
}

