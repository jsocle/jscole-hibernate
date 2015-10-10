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


abstract class PropertyDelegate<T : Any>(private val key: String) {
    operator fun get(hibernateProperties: HibernateProperties, propertyMetadata: PropertyMetadata): T? {
        if (key !in hibernateProperties.javaProperties) {
            return null
        }
        return fromString(hibernateProperties.javaProperties[key] as String)
    }

    operator fun set(hibernateProperties: HibernateProperties, propertyMetadata: PropertyMetadata, value: T?) {
        if (value == null) {
            hibernateProperties.javaProperties.remove(key)
        } else {
            hibernateProperties.javaProperties[key] = toString(value)
        }
    }

    protected abstract fun fromString(value: String): T

    protected abstract fun toString(value: T): String
}

class StringPropertyDelegate(key: String) : PropertyDelegate<String>(key) {
    override fun fromString(value: String): String = value

    override fun toString(value: String): String = value
}

