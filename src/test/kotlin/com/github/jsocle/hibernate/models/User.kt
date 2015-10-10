package com.github.jsocle.hibernate.models

import javax.persistence.Entity

@Entity
class User(
        @javax.persistence.Id
        val id: Int? = null
)