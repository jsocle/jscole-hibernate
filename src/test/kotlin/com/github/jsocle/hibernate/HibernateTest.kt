package com.github.jsocle.hibernate

import com.github.jsocle.JSocle
import com.github.jsocle.hibernate.models.User
import org.hibernate.stat.Statistics
import org.junit.Assert
import org.junit.Test

val Statistics.sessionCount: Long
    get() = sessionOpenCount - sessionCloseCount

class HibernateTest {
    @Test
    fun testBlockSession() {
        val db = Hibernate(
                JSocle(),
                HibernateProperties(connectionUrl = "jdbc:h2:mem:jsocle-hibernate-test-block-session", hbm2ddlAuto = Hbm2ddlAuto.Create),
                listOf(User::class)
        )

        db.sessionFactory.statistics.isStatisticsEnabled = true;

        // test session life cycle
        Assert.assertEquals(0, db.sessionFactory.statistics.sessionCount);
        db.session { session ->
            Assert.assertEquals(listOf<User>(), session.createCriteria(User::class.java).list())
            Assert.assertEquals(1, db.sessionFactory.statistics.sessionCount);
        }
        Assert.assertEquals(0, db.sessionFactory.statistics.sessionCount);

        // test rollback for not completed transaction.
        db.session { session ->
            session.beginTransaction()
            val user = User()
            session.persist(user)
            session.flush()
            Assert.assertEquals(listOf(user), session.createCriteria(User::class.java).list())
        }
        db.session { session ->
            Assert.assertEquals(listOf<User>(), session.createCriteria(User::class.java).list())
        }
    }

    @Test
    fun testJSocleImplement() {
        val app = object : JSocle() {
            val db = Hibernate(
                    this,
                    HibernateProperties(connectionUrl = "jdbc:h2:mem:jsocle-hibernate-test-jsocle-implement", hbm2ddlAuto = Hbm2ddlAuto.Create),
                    listOf(User::class)
            )

            init {
                route("/") { ->
                    db.session.beginTransaction()
                    val user = User()
                    db.session.persist(user)
                    db.session.flush()
                    Assert.assertEquals(listOf(user), db.session.createCriteria(User::class).list())
                }
            }
        }


        app.db.sessionFactory.statistics.isStatisticsEnabled = true;
        Assert.assertEquals(0, app.db.sessionFactory.statistics.sessionCount);
        Assert.assertEquals(0, app.db.sessionFactory.statistics.transactionCount);
        app.client.get("/");
        Assert.assertEquals(0, app.db.sessionFactory.statistics.sessionCount);
        Assert.assertEquals(0, app.db.sessionFactory.statistics.successfulTransactionCount);

        // test rollback for not completed transaction on teardown request.
        app.db.session { session ->
            Assert.assertEquals(listOf<User>(), session.createCriteria(User::class).list())
        }
    }

    @Test
    fun testSuccessfulTransaction() {
        val app = object : JSocle() {
            val db = Hibernate(
                    this,
                    HibernateProperties(connectionUrl = "jdbc:h2:mem:jsocle-hibernate-test-jsocle-implement", hbm2ddlAuto = Hbm2ddlAuto.Create),
                    listOf(User::class)
            )

            init {
                route("/") { ->
                    db.session.beginTransaction()
                    db.session.persist(User())
                    db.session.commit()

                    @Suppress("UNCHECKED_CAST")
                    val users = db.session.createCriteria(User::class).list() as List<User>
                    Assert.assertEquals(1, users.first().id)
                }
            }
        }

        app.client.get("/")
    }
}