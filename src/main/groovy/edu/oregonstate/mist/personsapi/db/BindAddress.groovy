package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.AddressObject
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindAddress.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindAddress {
    public static class EventBinderFactor implements BinderFactory {
        public Binder build(Annotation annotation) {
            new Binder<BindAddress, AddressObject>() {
                public void bind(SQLStatement q, BindAddress bind, AddressObject address) {
                    q.bind("addressType", address.addressType)
                    q.bind("city", address.city)
                    q.bind("postalCode", address.postalCode)
                    q.bind("stateCode", address.stateCode)
                    q.bind("seqno", null)
                    q.bind("return_value", null)
                }
            }
        }
    }
}
