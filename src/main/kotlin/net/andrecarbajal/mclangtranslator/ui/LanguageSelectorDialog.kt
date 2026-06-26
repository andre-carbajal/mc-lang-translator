package net.andrecarbajal.mclangtranslator.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import net.andrecarbajal.mclangtranslator.cache.ProviderLanguageCache
import net.andrecarbajal.mclangtranslator.providers.McLocale
import net.andrecarbajal.mclangtranslator.providers.TranslationProvider
import net.andrecarbajal.mclangtranslator.state.McTranslatorState
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class LanguageSelectorDialog(
    project: Project,
    sourceFile: VirtualFile,
    private val provider: TranslationProvider,
    private val state: McTranslatorState,
) : DialogWrapper(project) {
    private val checkboxPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }
    private val statusLabel = JLabel("Loading languages...")
    private val refreshButton = JButton("Refresh")
    private val selectAllButton = JButton("Select all")
    private val clearButton = JButton("Clear")
    private val providerCombo = ComboBox(arrayOf(provider.displayName)).apply { isEnabled = false }
    private val checkboxRows = mutableListOf<Pair<McLocale, JCheckBox>>()
    private var currentLanguages: List<McLocale> = emptyList()

    init {
        title = "Translate: ${sourceFile.name}"
        refreshButton.addActionListener { loadLanguages(forceRefresh = true) }
        selectAllButton.addActionListener { checkboxRows.forEach { it.second.isSelected = true }; updateStatus() }
        clearButton.addActionListener { checkboxRows.forEach { it.second.isSelected = false }; updateStatus() }
        init()
        loadLanguages(forceRefresh = false)
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout(8, 8)).apply { preferredSize = Dimension(440, 500) }
        val top = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("Provider:"))
            add(providerCombo)
            add(refreshButton)
        }
        val controls = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(selectAllButton)
            add(clearButton)
        }
        val center = JPanel(BorderLayout(4, 4)).apply {
            add(controls, BorderLayout.NORTH)
            add(JScrollPane(checkboxPanel), BorderLayout.CENTER)
        }
        root.add(top, BorderLayout.NORTH)
        root.add(center, BorderLayout.CENTER)
        root.add(statusLabel, BorderLayout.SOUTH)
        return root
    }

    fun getSelectedLocales(): List<McLocale> = checkboxRows
        .filter { it.second.isSelected }
        .map { it.first }

    private fun loadLanguages(forceRefresh: Boolean) {
        statusLabel.text = "Loading languages..."
        checkboxPanel.removeAll()
        checkboxRows.clear()
        val cache = ProviderLanguageCache.getInstance()
        if (forceRefresh) cache.invalidate(provider.key)
        val cached = cache.get(provider.key)
        if (cached != null) {
            populate(cached, "Loaded ${cached.size} cached languages")
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val loaded = try {
                provider.getSupportedLanguages()
            } catch (_: Exception) {
                provider.fallbackLanguages()
            }
            cache.put(provider.key, loaded)
            SwingUtilities.invokeLater { populate(loaded, "Loaded ${loaded.size} languages") }
        }
    }

    private fun populate(languages: List<McLocale>, status: String) {
        currentLanguages = languages.sortedWith(compareBy({ it.displayName }, { it.mcCode }))
        checkboxPanel.removeAll()
        checkboxRows.clear()
        currentLanguages.forEach { locale ->
            val checkbox =
                JCheckBox("${locale.displayName} (${locale.mcCode})", locale.mcCode in state.lastUsedLanguages)
            checkbox.addActionListener { updateStatus() }
            checkboxRows += locale to checkbox
            checkboxPanel.add(checkbox)
        }
        statusLabel.text = status
        updateStatus()
        checkboxPanel.revalidate()
        checkboxPanel.repaint()
    }

    private fun updateStatus() {
        statusLabel.text = "${checkboxRows.count { it.second.isSelected }} selected / ${checkboxRows.size} available"
    }

    override fun getPreferredFocusedComponent(): JComponent = providerCombo
}
