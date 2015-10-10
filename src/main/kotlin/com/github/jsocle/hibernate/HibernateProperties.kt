package com.github.jsocle.hibernate

import org.hibernate.cfg.AvailableSettings
import java.util.*

class HibernateProperties(connectionUrl: String? = null, hbm2ddlAuto: Hbm2ddlAuto? = null) {
    val javaProperties = Properties()

    var connectionUrl by StringPropertyDelegate(AvailableSettings.URL)
    var hbm2ddlAuto by EnumPropertyDelegate(AvailableSettings.HBM2DDL_AUTO, Hbm2ddlAuto.values())

    init {
        this.connectionUrl = connectionUrl
        this.hbm2ddlAuto = hbm2ddlAuto
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

class EnumPropertyDelegate<T : ValueEnum>(key: String, private val values: Array<T>) : PropertyDelegate<T>(key) {
    override fun fromString(value: String): T = values.find { it.value == value }!!

    override fun toString(value: T): String {
        return value.value
    }
}

interface ValueEnum {
    val value: String
}

enum class Hbm2ddlAuto(override val value: String) : ValueEnum {
    Update("update"), Create("create"), CreateDrop("create-drop"), Validate("validate")
}
