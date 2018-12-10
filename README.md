# grails where query strange behavior in tests.

While attempting to show example of a bunch of grails/gorm queries I ran into the following oddity.

If you use a where query in an expect block or an assert statement and the where query is both cretated and executed on
the same line, spock (and my other testing frameworks??) will create the criteria improperly and not apply an of the
restrictions defined in the criteria.

For instance, assuming you have two "Things" named "thing 1" and "thing 2", the following assert statement will fail:

`assert (Thing.where { name == "thing 1" }.list()).size() == 1`

The line is supposed to create a detached criteria where query, add the restriction that the name property has to equal
"thing 1", execute the query (which returns a list) and guaranty that the size == 1.

With sql logging turn on, this is the result of the test:


```
Hibernate: select this_.id as id1_0_0_, this_.version as version2_0_0_, this_.name as name3_0_0_ from thing this_

Condition not satisfied:

Thing.where { name == "thing 1" }.list().size()==1  // the only one that fails
      |                           |      |     |
      |                           |      2     false
      |                           [wheretest.Thing : 7, wheretest.Thing : 8]
      grails.gorm.DetachedCriteria@3522367c 
```


You can see that the executed query does not contain the restriction on name.


This only happens when the where query uses the special gorm/grails abaility to use groovy code and comparison in the
closure.  For instance

` Thing.where { eq 'name', 'thing 1' }.list().size() == 1`

Works.  The criteria is created correctly, the query does add the "name = ?" restriction to the query and the test
passes.

This project contains a few example of queries.  The tests that fail are examples of this issue. 

