package dev.eaji

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange

class CreateSep : AnAction() {

    private val prefix = "// "
    private val baseWidth = 80

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document

        val lineNumber = editor.caretModel.primaryCaret.logicalPosition.line
        val startOffset = document.getLineStartOffset(lineNumber)
        val endOffset = document.getLineEndOffset(lineNumber)

        val fullLine = document.getText(TextRange(startOffset, endOffset))
        val leadingIndent = fullLine.takeWhile { it.isWhitespace() }

        // Deduct indent for starting offset and ending offset
        val fillLength = baseWidth - (leadingIndent.length * 2) - prefix.length

        if (fillLength < 1) {
            HintManager.getInstance().showInformationHint(editor, "Indentation is too deep for target width.")
            return
        }

        val fill = "─".repeat(fillLength)
        val replacement = buildString {
            append(leadingIndent).append('\n')
            append(leadingIndent).append(prefix).append(fill).append('\n')
            append(leadingIndent)
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(startOffset, endOffset, replacement)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}