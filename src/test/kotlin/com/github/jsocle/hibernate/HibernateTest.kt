package com.github.jsocle.hibernate

import com.github.jsocle.hibernate.models.User
import org.junit.Assert
import org.junit.Test

class HibernateTest {
    @Test
    fun test() {
        val db = Hibernate(
                HibernateProperties(connectionUrl = "jdbc:h2:mem:jsocle-hibernate", hbm2ddlAuto = Hbm2ddlAuto.Create),
                listOf(User::class)
        )

        db.session { session ->
            Assert.assertEquals(listOf<User>(), session.createCriteria(User::class.java).list())
        }
    }
}