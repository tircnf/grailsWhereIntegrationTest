package wheretest

import grails.gorm.DetachedCriteria
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class ThingSpec extends Specification implements  DomainUnitTest<Thing> {

    def setup() {
    }

    def cleanup() {
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

    void "test detachedCriteria -- fails, and the sql doesn't filter by name."() {
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

    void "test where -- fails, and the sql doesn't filter by name"() {

        given: "two things"
        setupData()
        def queryClosure = { -> Thing.where { name == "thing 1" } }

        expect:
        queryClosure.call().list().size() == 1  // works
        Thing.where { name == "thing 1" }.list().size() == 1   // fails
    }

    // these two tests just failed, but when wrapped in the
    // spock 'where' block (or maybe because it is a closure)
    @Unroll
    void "Test two bad ones #name that failed above"() {
        given: "two things"
        setupData()

        expect: "my finder returns 1 thing"
        query.call() == expected

        // the previous two test failed... why do they work using this 'where/unroll' syntax?
        where:
        name                | expected | query
        "Detached Criteria" | 1        | { -> new DetachedCriteria(Thing).build { eq 'name', 'thing 1' }.list().size() }
        "Where"             | 1        | { -> Thing.where { name == "thing 1" }.list().size() }

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
        "Using where -- works now?" | 1        | { -> Thing.where { name == "thing 1" }.list().size() }
        "Using withCriteria "       | 1        | { -> Thing.withCriteria { eq "name", "thing 1" }.size() }
        "Detached Criteria"         | 1        | { ->
            new DetachedCriteria(Thing).build { eq 'name', 'thing 1' }.list().size()
        }

    }
}