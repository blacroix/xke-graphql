package fr.blacroix

import com.github.pgutkowski.kgraphql.KGraphQL
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

data class User(val id: Int, val name: String)

data class Author(
        val id: Int,
        val firstName: String,
        val lastName: String,
        val bookIds: MutableList<Int> = mutableListOf(),
        var books: List<Book> = emptyList(),
        val popularity: Int = 0)

data class Book(val id: Int,
                val title: String,
                val year: Int,
                val authorId: Int,
                var likes: Int = 0,
                var author: Author? = null)

val authors = mutableListOf(
        Author(1, "Stephen", "King", mutableListOf(1), popularity = 110),
        Author(2, "Dan", "Brown", mutableListOf(2), popularity = 90))

val books = mutableListOf(
        Book(1, "The Shining", 1977, 1),
        Book(2, "Da Vinci Code", 2003, 2))

@Suppress("RedundantLambdaArrow")
val schema = KGraphQL.schema {
    query("user") {
        resolver { -> User(1, "Toto") } // http://localhost:8080/graphql?q={user{id,name}}
    }
    query("book") {
        resolver { id: Int ->
            // http://localhost:8080/graphql?q={book(id:2){id,title,year}}
            // http://localhost:8080/graphql?q={book(id:2){title,year,likes,author{firstName,lastName}}}
            // http://localhost:8080/graphql?q={author(id:1){firstName,lastName,books{title,year}popularity}}
            val book = books.find { it.id == id }
            if (book != null) {
                book.author = authors.find { it.id == book.authorId }
            }
            book
        }
    }
    query("author") {
        resolver { id: Int ->
            val author = authors.find { it.id == id }
            if (author != null) {
                author.books = books.filter { author.bookIds.contains(it.id) }
            }
            author
        }
    }
    mutation("addBook") {
        // http://localhost:8080/graphql?q=mutation{addBook(id:9,title:"Martine fait du GraphQL",author_id:1,year:2017){id,likes}}
        resolver { id: Int, title: String, author_id: Int, year: Int ->
            val book = Book(id, title, author_id, year)
            books.add(book)
            book
        }
    }
    mutation("likeBook") {
        // http://localhost:8080/graphql?q=mutation{likeBook(title:"Martine fait du GraphQL"){likes}}
        resolver { title: String ->
            val book = books.find { it.title == title }
            if (book != null) {
                book.likes++
            }
            book
        }
    }
    mutation("deleteBook") {
        // http://localhost:8080/graphql?q=mutation{deleteBook(id:9){author{firstName,lastName,books{title}}}}
        resolver { id: Int ->
            val book = books.find { it.id == id }
            books.remove(book)
            val author = authors.find { it.bookIds.contains(id) }
            if (author != null) {
                author.bookIds.remove(id)
                author.books = books.filter { it.authorId == id }
            }
            if (book != null) {
                book.author = author
            }
            book
        }
    }
}

val server = embeddedServer(Netty, 8080) {
    routing {
        get("/graphql") {
            call.respondText(schema.execute(call.request.queryParameters["q"].toString()),
                    ContentType.Application.Json)
        }
    }
}

fun main(args: Array<String>) {
    server.start(wait = true)
}