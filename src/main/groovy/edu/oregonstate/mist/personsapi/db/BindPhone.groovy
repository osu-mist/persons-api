package edu.oregonstate.mist.personsapi.db

import edu.oregonstate.mist.personsapi.core.PhoneObject
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.Binder
import org.skife.jdbi.v2.sqlobject.BinderFactory
import org.skife.jdbi.v2.sqlobject.BindingAnnotation

import java.lang.annotation.Annotation
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@BindingAnnotation(BindPhone.EventBinderFactor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.PARAMETER])
public @interface BindPhone {
  public static class EventBinderFactor implements BinderFactory {
    public Binder build(Annotation annotation) {
      new Binder<BindPhone, PhoneObject>() {
        public void bind(SQLStatement q, BindPhone bind, PhoneObject phone) {
          q.bind("id", phone.id)
          q.bind("areaCode", phone.areaCode)
          q.bind("phoneNumber", phone.phoneNumber)
          q.bind("phoneExtension", phone.phoneExtension)
          q.bind("primaryIndicator", phone.primaryIndicator)
          q.bind("phoneType", phone.phoneType)
          q.bind("addressType", phone.addressType)
        }
      }
    }
  }
}
