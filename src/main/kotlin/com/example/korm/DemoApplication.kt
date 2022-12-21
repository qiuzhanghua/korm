package com.example.korm


import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.UUID
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
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Cities, Users, Roles, UsersRoles)

            val bj = Cities.insertAndGetId {
                it[name] = "Beijing"
            }
            commit()

            val uid = Users.insertAndGetId {
                it[name] = "Daniel"
//                it[city] = bj
            }
            commit()
            println(uid.value)
            val rid = Roles.insertAndGetId {
                it[name] = "User"
            }
            commit()
            println(rid.value)

            val rid2 = Roles.insertAndGetId {
                it[name] = "Admin"
            }
            commit()
            println(rid2.value)
            UsersRoles.insert {
                it[user] = uid
                it[role] = rid
            }
            UsersRoles.insert {
                it[user] = uid
                it[role] = rid2
            }
            commit()

            User.findById(uid)?.roles?.forEach {
                println(it.name)
            }
        }

        transaction {
            println("count of user user <=> role = " + UsersRoles.selectAll().count())
            UsersRoles.selectAll().filter { it[UsersRoles.role].value == 1.toLong() }.forEach {
//            UsersRoles.selectAll().forEach {
                val u = it[UsersRoles.user].value.toString()
                val r = it[UsersRoles.role].value.toString()
                println("user = $u, role = $r")
            }
        }

    }
}

object Cities: UUIDTable("cities") {
    val name = varchar("name", 50)
}

object Users : UUIDTable("users") {
    val name = varchar("name", 50)
//    val cityId = (uuid("city_id").references(Cities.id)).nullable()
    var city = reference("city_id", Cities).nullable()
}

object Roles: LongIdTable("roles") {
    val name = varchar("name", 100)
}

object UsersRoles: Table("user_role") {
    val user = reference("user_id", Users)
    val role = reference("role_id", Roles)
    override val primaryKey = PrimaryKey(
        user, role,
        name = "PK_USER_ROLE")
}

class City(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<City>(Cities)
    var name by Cities.name
    val users by User optionalReferencedOn Users.city
}

class User(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<User>(Users)
    var name by Roles.name
    var roles by Role via UsersRoles
}

class Role(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<Role>(Roles)
    var name by Roles.name
    var users by User via UsersRoles
}