package com.github.jsocle.hibernate

import org.hibernate.cfg.AvailableSettings
import java.util.*

class HibernateProperties(connectionUrl: String? = null) {
    val javaProperties = Properties()

    var connectionUrl by StringPropertyDelegate(AvailableSettings.URL)

    init {
        this.connectionUrl = connectionUrl
    }

    operator
    fun set(key: String, value: String) {
        javaProperties[key] = value
    }

    operator fun get(key: String): String = javaProperties[key] as String
}

class StringPropertyDelegate(private val key: String) {
    operator fun get(hibernateProperties: HibernateProperties, propertyMetadata: PropertyMetadata): String?
            = hibernateProperties.javaProperties[key] as String?

    operator fun set(hibernateProperties: HibernateProperties, propertyMetadata: PropertyMetadata, value: String?) {
        if (value == null) {
            hibernateProperties.javaProperties.remove(key)
        } else {
            hibernateProperties.javaProperties[key] = value
        }
    }
}

