package com.github.jsocle.hibernate

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import kotlin.reflect.KClass

class Hibernate(public val properties: HibernateProperties = HibernateProperties(),
                classes: List<KClass<*>> = listOf()) {
    public val classes = arrayListOf<KClass<*>>() apply { this.addAll(classes) }

    private val sessionFactory: SessionFactory by lazy(LazyThreadSafetyMode.NONE) {
        Configuration()
                .addProperties(properties.javaProperties)
                .apply { this@Hibernate.classes.forEach { addAnnotatedClass(it.java) } }
                .buildSessionFactory()
    }

    public val session: Session
        get() = sessionFactory.openSession()

    fun session<T>(intent: (session: Session) -> T): T {
        val session = sessionFactory.openSession()
        try {
            return intent(session)
        } finally {
            session.close()
        }
    }
}
