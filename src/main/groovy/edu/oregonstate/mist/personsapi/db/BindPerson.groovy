package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.PersonObject
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindPerson.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindPerson {
    public static class EventBinderFactor implements BinderFactory {
        public Binder build(Annotation annotation) {
            new Binder<BindPerson, PersonObject>() {
                public void bind(SQLStatement q, BindPerson bind, PersonObject person) {
                    println('-----Bind------')
                    println(person.birthDate)
                    println('-------------')
                    q.bind("firstName", person.name.firstName)
                    q.bind("lastName", person.name.lastName)
                    q.bind("birthDate", person.birthDate)
                }
            }
        }
    }
}
