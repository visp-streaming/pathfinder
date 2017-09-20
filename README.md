# Pathfinder - A fault tolerance framework for elastic stream processing systems

In order to run Pathfinder, a MySQL database must be set up. Please adjust the application.properties accordingly.
A default docker container might look like this:

```
docker run -d --name mysql2 -e MYSQL_ROOT_PASSWORD=pathfinder -e MYSQL_DATABASE=pathfinder -p 10306:3306 mariadb
```

With the corresponding application.properties entry:

```
spring.datasource.url=jdbc:mysql://127.0.0.1:10306/pathfinder?useSSL=false
spring.datasource.username=root
spring.datasource.password=pathfinder
```