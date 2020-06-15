package us.dragonma.backgrounds.update

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.*
import javax.swing.*

internal class Credentials(
    title: String? = null,
    usernamePrompt: String = "Username:",
    passwordPrompt: String = "Password:"
) : JPanel(GridBagLayout()) {
    private var ok = false
    private val username: String
    private val password: CharArray

    val basicAuth: String
        get() {
            val auth = ByteArray(username.length + 1 + password.size)

            var i = 0
            username.forEach { auth[i++] = it.toByte() }
            auth[i++] = ':'.toByte()
            password.forEach { auth[i++] = it.toByte() }

            return "Basic ${Base64.getEncoder().encodeToString(auth)}"
        }

    init {
        if (GraphicsEnvironmentExt.isReallyHeadless) {
            val console = System.console() ?: throw CredentialsException("Unable to open console for password input")
            var prefix = ""

            if (title != null) {
                prefix = "> "
                console.format("%s\n", title)
            }
            console.format("%s%s ", prefix, usernamePrompt)
            username = console.readLine()
            console.format("%s%s ", prefix, passwordPrompt)
            password = console.readPassword()
        } else {
            val usernameField = JTextField(10)
            val passwordField = JPasswordField(10)

            usernameField.addActionListener {
                passwordField.requestFocusInWindow()
            }
            passwordField.addActionListener {
                ok = true
                SwingUtilities.getWindowAncestor(it.source as JComponent).dispose()
            }

            val c = GridBagConstraints()

            c.gridx = 0
            c.gridy = 0
            add(JLabel("$usernamePrompt "), c)

            c.gridx = 1
            add(usernameField, c)

            c.gridx = 0
            c.gridy = 1
            add(JLabel("$passwordPrompt "), c)

            c.gridx = 1
            add(passwordField, c)

            JOptionPane.showOptionDialog(
                null,
                this,
                title ?: "Enter Password",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                emptyArray(),
                null
            )

            if (ok) {
                username = usernameField.text
                password = passwordField.password
            } else {
                throw CredentialsException("username and password must be supplied")
            }
        }
    }
}
