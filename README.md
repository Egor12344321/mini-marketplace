# Mini-marketplace

## 🌟 О проекте

- Практика написания спецификации OpenApi для dto/handlers. 
- Реализована бизнес-логика по созданию заказов с rate-limiting, использованием транзакций. 
- Основные ошибки перехватываются глобальным обработчиком ошибок и передаются клиенту в единообразном формате. 
- Логирование каждого запроса с маскировкой чувствительных данных. 
- Базовая JWT-авторизация
- Ролевая модель
- Валидация входящих запросов
- Использование индексов

## ![Java](https://readmecodegen.vercel.app/api/social-icon?name=java&bg=ED8B00&size=32) Технологический стек:
| Категория | Технологии                            |
|-----------|---------------------------------------|
| **Язык** | Java 21                               |
| **Фреймворк** | Spring Boot (Data JPA, Security, MVC) |
| **Базы данных** | PostgreSQL                       |
| **Миграции** | Flyway                                |
| **DevOps** | Docker, Docker Compose                |
| **Документация** | OpenAPI                     |
| **Инструменты** | Lombok                                |

## 🌟 Запуск 
```
git clone https://github.com/Egor12344321/mini-marketplace.git
cd mini-marketplace
mvn package
docker-compose up -d
```





