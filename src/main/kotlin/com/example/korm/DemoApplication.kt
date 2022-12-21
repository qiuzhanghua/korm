package com.example.korm

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@SpringBootApplication
class DemoApplication {
    @Bean
    fun commandLineRunner(ds: DataSource) = CommandLineRunner {
//        println(ds.connection)
        Database.connect(ds)
        transaction {
            SchemaUtils.create(Users, Roles)
            val uid = Users.insertAndGetId {
                it[name] = "Daniel"
            }
            commit()
            println(uid.value)
            val rid = Roles.insertAndGetId {
                it[name] = "Admin"
            }
            commit()
            println(rid.value)

        }

        transaction {
            println(Users.selectAll().count())
        }
    }
}

object Users : UUIDTable() {
    val name = varchar("name", 50)
}

object Roles: LongIdTable() {
    val name = varchar("name", 100)
}

