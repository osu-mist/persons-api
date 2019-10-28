package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.PhoneRecordObject
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindPhoneRecord.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindPhoneRecord {
  public static class EventBinderFactor implements BinderFactory {
    public Binder build(Annotation annotation) {
      new Binder<BindPhoneRecord, PhoneRecordObject>() {
        public void bind(
          SQLStatement q, BindPhoneRecord bind, PhoneRecordObject phoneRecord
        ) {
          q.bind("phoneType", phoneRecord.phoneType)
          q.bind("phoneSeqno", phoneRecord.phoneSeqno)
          q.bind("id", phoneRecord.id)
          q.bind("primaryIndicator", phoneRecord.primaryIndicator ? 'Y' : null)
        }
      }
    }
  }
}
