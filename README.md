# POC GraphQL and Kotlin

## Stack

- [Ktor](http://ktor.io/)
- [KGraphQL](https://github.com/pgutkowski/KGraphQL)
- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/)

## How To

Run:

```bash
$> ./gradlew run
```

Use:

```
http://localhost:8080/graphql?q={book(id:2){id,title,year}}
http://localhost:8080/graphql?q={book(id:2){title,year,likes,author{firstName,lastName}}}
http://localhost:8080/graphql?q={author(id:1){firstName,lastName,books{title,year}popularity}}
```
```
http://localhost:8080/graphql?q=mutation{addBook(id:9,title:"Martine fait du GraphQL",author_id:1,year:2017){id,likes}}
```
```
http://localhost:8080/graphql?q=mutation{likeBook(title:"Martine fait du GraphQL"){likes}}
```
```
http://localhost:8080/graphql?q=mutation{deleteBook(id:9){author{firstName,lastName,books{title}}}}
```