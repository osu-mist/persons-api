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
                    q.bind("addressLine1", address.addressLine1)
                    q.bind("addressLine2", address.addressLine2)
                    q.bind("addressLine3", address.addressLine3)
                    q.bind("addressLine4", address.addressLine4)
                    q.bind("houseNumber", address.houseNumber)
                    q.bind("countyCode", address.countyCode)
                    q.bind("nationCode", address.nationCode)
                    q.bind("seqno", null)
                    q.bind("return_value", null)
                }
            }
        }
    }
}
