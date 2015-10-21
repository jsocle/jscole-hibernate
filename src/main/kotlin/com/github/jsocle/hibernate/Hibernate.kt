package com.github.jsocle.hibernate

import com.github.jsocle.JSocle
import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.internal.AbstractSessionImpl
import kotlin.concurrent.getOrSet
import kotlin.reflect.KClass

class Hibernate(private val app: JSocle, public val properties: HibernateProperties = HibernateProperties(),
                classes: List<KClass<*>> = listOf()) {
    public val classes = arrayListOf<KClass<*>>() apply { this.addAll(classes) }

    public var _sessionFactory: SessionFactory? = null

    internal val sessionFactory: SessionFactory
        get() {
            if (_sessionFactory == null) {
                _sessionFactory = Configuration()
                        .addProperties(properties.javaProperties)
                        .apply { this@Hibernate.classes.forEach { addAnnotatedClass(it.java) } }
                        .buildSessionFactory()
            }
            return _sessionFactory!!
        }

    private val requestLocalSession = ThreadLocal<Session>()

    public val session: Session
        get() = requestLocalSession.getOrSet { sessionFactory.openSession() }

    init {
        app.addOnBeforeFirstRequest { initialize() }
        app.addOnTeardownRequest { closeSession() }
    }

    fun session<T>(intent: (session: Session) -> T): T {
        val session = sessionFactory.openSession()
        try {
            return intent(session)
        } finally {
            session.finalize()
        }
    }

    private fun initialize() {
        // initialize and test db connection
        session { session ->
            assert(2 == session.createSQLQuery("SELECT 1 + 1 FROM DUAL").setMaxResults(1).uniqueResult())
        }
    }

    private fun closeSession() {
        val session = requestLocalSession.get()
        if (session != null) {
            requestLocalSession.remove()
            session.finalize()
        }
    }

    fun reload() {
        if (_sessionFactory != null) {
            _sessionFactory!!.close()
        }
        _sessionFactory = null
        initialize()
    }
}

private val Session.isClosed: Boolean
    get() = (this as AbstractSessionImpl).isClosed

private fun Session.finalize() {
    if (isClosed) {
        return
    }
    if (transaction.status.canRollback()) {
        transaction.rollback()
    }
    close()
}

fun Session.createCriteria<T : Any>(klass: KClass<T>): Criteria {
    return createCriteria(klass.java)
}

fun Session.commit() {
    transaction.commit()
}