@file:Suppress("DEPRECATION")

package com.github.vok.example.crud

import com.github.mvysny.karibudsl.v8.*
import com.github.mvysny.karibudsl.v8.v7compat.*
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.shared.Version
import com.vaadin.ui.VerticalLayout

/**
 * This is the root (or main) view. MyUI initially shows view whose name is "" (an empty string).
 * @author mvy
 */
@AutoView("")
class WelcomeView: VerticalLayout(), View {

    companion object {
        fun navigateTo() = navigateToView<WelcomeView>()
    }

    init {
        setSizeFull(); isMargin = true
        label7 {
            html("""<h3>VaadinOnKotlin</h3>Welcome to the VaadinOnKotlin demo. VaadinOnKotlin provides means to creating rich Vaadin apps:
            <ul><li>Provides Vaadin DSL builder support</li>
            <li>Provides simple database access via db {} function</li></ul>
            And more.""")
        }
        label7("Vaadin version ${Version.getFullVersion()}")
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
    }
}

