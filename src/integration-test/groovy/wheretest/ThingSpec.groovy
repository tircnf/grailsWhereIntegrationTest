package wheretest

import grails.gorm.DetachedCriteria
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import org.hibernate.SessionFactory
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class ThingSpec extends Specification {

    SessionFactory sessionFactory

    def logger

    def setup() {
        logger = sessionFactory.currentSession.jdbcCoordinator.statementPreparer.jdbcService.sqlStatementLogger
        logger.logToStdout = true
    }

    def cleanup() {
        logger.logToStdout = false
    }

    def setupData() {
        new Thing(name: "thing 1").save(flush: true, failOnError: true)
        new Thing(name: "thing 2").save(flush: true, failOnError: true)
    }

    void "test findAll"() {
        given: "two things"
        setupData()
        println "Running findAllByName"

        expect:
        Thing.findAllByName("thing 1").size() == 1

    }

    void "test criteria"() {

        given: "two things"
        setupData()
        println "Running withCriteria"

        expect:
        Thing.withCriteria { eq "name", "thing 1" }.size() == 1
    }

    void "test detachedCriteria"() {
        given: "two things"
        setupData()

        and: "A detachedCriteria"
        def criteria = new DetachedCriteria(Thing)
        criteria.build {
            eq 'name', 'thing 1'
        }
        println "Running withDetachedCriteria"

        expect:
        criteria.list().size() == 1

    }

    void "test where"() {

        given: "two things"
        setupData()
        println "Running 'where'"

        expect:
        Thing.where { name == "thing 1" }.list().size() == 1
    }

    @Unroll
    void "Test two bad ones #name"() {
        given: "two things"
        setupData()

        expect: "my finder returns 1 thing"
        query.call() == expected

        // the previous two test failed... why do they work using this 'where/unroll' syntax?
        where:
        name                        | expected | query
        "Detached Criteria"         | 1        | {-> new DetachedCriteria(Thing).build { eq 'name', 'thing 1' }.list().size() }
        "Using where"               | 1        | { -> Thing.where { name == "thing 1" }.list().size() }

    }

    void "test where with criteria syntax"() {

        given: "two things"
        setupData()
        println "Running 'where'"

        expect:
        Thing.where { eq 'name', 'thing 1' }.list().size() == 1
    }

    @Unroll
    void "Test them using #name"() {
        given: "two things"
        setupData()

        expect: "my finder returns 1 thing"
        query.call() == expected

        where:
        name                        | expected | query
        "Find all"                  | 1        | { -> Thing.findAllByName("thing 1").size() }
        "Find all again"            | 1        | { -> Thing.findAllByName("thing 2").size() }
        "Doesnt find using findall" | 0        | { -> Thing.findAllByName("unknown").size() }
        "Using where"               | 1        | { -> Thing.where { name == "thing 1" }.list().size() }
        "Using withCriteria"        | 1        | { -> Thing.withCriteria { eq "name", "thing 1" }.size() }
        "Detached Criteria"         | 1        | {-> new DetachedCriteria(Thing).build { eq 'name', 'thing 1' }.list().size() }

    }
}
