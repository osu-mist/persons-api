package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.AddressRecordObject
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindAddressRecord.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindAddressRecord {
    public static class EventBinderFactor implements BinderFactory {
        public Binder build(Annotation annotation) {
            new Binder<BindAddressRecord, AddressRecordObject>() {
                public void bind(
                    SQLStatement q, BindAddressRecord bind, AddressRecordObject addressRecord
                ) {
                    q.bind("addressType", addressRecord.addressType)
                    q.bind("seqno", addressRecord.seqno)
                    q.bind("rowID", addressRecord.rowID)
                    q.bind("nullValue", null)
                }
            }
        }
    }
}
