package com.github.jsocle.hibernate

import org.hibernate.cfg.AvailableSettings
import org.junit.Assert
import org.junit.Test

class HibernatePropertiesTest {
    @Test
    fun test() {
        val properties = HibernateProperties(connectionUrl = "url")
        Assert.assertEquals(mapOf(AvailableSettings.URL to "url"), properties.javaProperties)
    }

    @Test
    fun testForward() {
        val properties = HibernateProperties()
        properties[AvailableSettings.URL] = "url"
        Assert.assertEquals("url", properties[AvailableSettings.URL])
        Assert.assertEquals(mapOf(AvailableSettings.URL to "url"), properties.javaProperties)
    }

    @Test
    fun testPropertyDelegate() {
        val properties = HibernateProperties()
        Assert.assertNull(properties.connectionUrl)

        properties.connectionUrl = "url"
        Assert.assertEquals("url", properties.connectionUrl)
        Assert.assertEquals(mapOf(AvailableSettings.URL to "url"), properties.javaProperties)

        properties.connectionUrl = null
        Assert.assertNull(properties.connectionUrl)
    }

    @Test
    fun testEnumPropertyDelegate() {
        val properties = HibernateProperties()
        Assert.assertNull(properties.hbm2ddlAuto)
        properties.hbm2ddlAuto = Hbm2ddlAuto.CreateDrop
        Assert.assertEquals(Hbm2ddlAuto.CreateDrop, properties.hbm2ddlAuto)
        Assert.assertEquals(mapOf(AvailableSettings.HBM2DDL_AUTO to "create-drop"), properties.javaProperties)
    }
}