package eu.vaadinonkotlin.rest

import com.github.mvysny.dynatest.DynaNodeGroup
import eu.vaadinonkotlin.VaadinOnKotlin
import eu.vaadinonkotlin.sql2o.dataSource
import com.github.vokorm.*
import org.flywaydb.core.Flyway
import org.h2.Driver
import java.time.Instant
import java.time.LocalDate
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * A very simple bean representing a database table. The SELECT column -> bean property mapping is done by SQL2O.
 * Notice how Kotlin generates toString, equals, hashcode and all getters/setters automatically (for data classes).
 *
 * See [vok-orm](https://github.com/mvysny/vok-orm) for more details.
 * @property id person ID
 * @property name person name
 * @property age the person age, 15..100
 * @property dateOfBirth date of birth, optional.
 * @property created when the record was created
 * @property maritalStatus the marital status
 * @property alive whether the person is alive (true) or deceased (false).
 */
data class Person(
        override var id: Long? = null,

        @field:NotNull
        @field:Size(min = 1, max = 200)
        @As("name")
        var personName: String? = null,

        @field:NotNull
        @field:Min(15)
        @field:Max(100)
        var age: Int? = null,

        var dateOfBirth: LocalDate? = null,

        @field:NotNull
        var created: Instant? = null,

        @field:NotNull
        var maritalStatus: MaritalStatus? = null,

        @field:NotNull
        var alive: Boolean? = null

) : Entity<Long> {
    // this brings in tons of useful static methods such as findAll(), findById() etc.
    companion object : Dao<Person>

    override fun save(validate: Boolean) {
        if (id == null) {
            if (created == null) created = Instant.now()
        }
        super.save(validate)
    }
}

enum class MaritalStatus {
    Single,
    Married,
    Divorced,
    Widowed
}

fun DynaNodeGroup.usingDb() {
    beforeGroup {
        VokOrm.dataSourceConfig.apply {
            driverClassName = Driver::class.java.name
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            username = "sa"
            password = ""
        }
        VokOrm.init()
        val flyway = Flyway.configure()
                .dataSource(VaadinOnKotlin.dataSource)
                .load()
        flyway.migrate()
    }
    afterGroup { VokOrm.destroy() }

    beforeEach { Person.deleteAll() }
    afterEach { Person.deleteAll() }
}
