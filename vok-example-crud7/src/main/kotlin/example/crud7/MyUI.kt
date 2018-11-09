package example.crud7

import example.crud7.personeditor.CrudView
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.annotations.Push
import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.annotations.Widgetset
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.PushStateNavigation
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewDisplay
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui.*
import eu.vaadinonkotlin.scheduleAtFixedRate
import eu.vaadinonkotlin.seconds
import eu.vaadinonkotlin.vaadin8.Session
import eu.vaadinonkotlin.vaadin8.jpa.db
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * The Vaadin UI which demoes all the features. If not familiar with Vaadin, please check out the Vaadin tutorial first.
 * @author mvy
 */
@Theme("valo")
@Title("VaadinOnKotlin Demo")
@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
@Push(transport = Transport.WEBSOCKET_XHR)
@PushStateNavigation
class MyUI : UI() {

    private val content = Content()

    override fun init(request: VaadinRequest?) {
        setContent(content)
        navigator = Navigator(this, content as ViewDisplay)
        navigator.addProvider(autoViewProvider)
    }
}

private class Content: VerticalLayout(), ViewDisplay {
    private val viewPlaceholder: CssLayout
    private lateinit var currentTimeLabel: Label
    init {
        setSizeFull()
        // the Vaadin DSL demo - build your UI, builder-style!
        horizontalLayout {
            w = fillParent
            menuBar {
                expandRatio = 1f
                addItem("Welcome") { WelcomeView.navigateTo() }
                addItem("CRUD Demo") { CrudView.navigateTo() }
            }
            currentTimeLabel = label {
                setWidthUndefined()
            }
        }
        viewPlaceholder = cssLayout {
            setSizeFull(); expandRatio = 1f
        }
    }

    override fun showView(view: View?) {
        viewPlaceholder.removeAllComponents()
        viewPlaceholder.addComponent(view as Component)
    }

    private val timer = AtomicInteger()
    @Transient
    private var timerHandle: ScheduledFuture<*>? = null

    override fun attach() {
        super.attach()
        // async and Push demo - show a label and periodically update its value from the server.
        timerHandle = scheduleAtFixedRate(0.seconds, 1.seconds) {
            timer.incrementAndGet()
            db {
                // you can use DB even in background threads :)
            }
            ui.access {
                currentTimeLabel.value = "Timer: $timer; last added = ${Session.lastAddedPersonCache.lastAdded}"
            }
        }
    }

    override fun detach() {
        timerHandle?.cancel(false)
        super.detach()
    }
}
