package dev.eaji

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange

class CreateComment : AnAction() {

    private val commentStart = "// ───"
    private val commentEnd = "────"
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
        val rawText = fullLine.trim()

        if (rawText.isEmpty()) {
            HintManager.getInstance().showInformationHint(
                editor,
                "Comment text is empty. Use Alt+L for a separator."
            )
            return
        }

        // Title case transformation
        val formattedTitle = rawText.split(Regex("\\s+")).joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        // Deduct indent for starting offset and ending offset
        val nonFillLength = (leadingIndent.length * 2) + commentStart.length + 1 + formattedTitle.length + 1 + commentEnd.length
        val fillLength = baseWidth - nonFillLength

        if (fillLength < 1) {
            HintManager.getInstance().showInformationHint(editor, "Comment text is too long for this indentation level.")
            return
        }

        val fill = "─".repeat(fillLength)
        val replacement = buildString {
            append(leadingIndent).append('\n')
            append(leadingIndent).append(commentStart).append(' ')
            append(formattedTitle).append(' ')
            append(fill).append(commentEnd).append('\n')
            append(leadingIndent).append('\n')
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