package edu.oregonstate.mist.personsapi

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class PhoneFormatter {
    static toE164(String phoneStr) {
        if (!phoneStr) {
            return null
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance()
        Phonenumber.PhoneNumber phoneProto = phoneUtil.parse(phoneStr, 'US')
        phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164)
    }
}
