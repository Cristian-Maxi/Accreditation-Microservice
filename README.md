# 🧩 Proyecto de Microservicios con Spring Boot

Este proyecto es una implementación de una arquitectura de microservicios basada en Spring Boot. Utiliza tecnologías modernas como Redis, RabbitMQ y PostgreSQL para resolver problemas empresariales comunes de manera escalable, eficiente y modular.

## 🔧 Tecnologías Utilizadas

- Java 21
- Spring Boot 3.4.4
- Maven
- PostgreSQL
- Redis
- RabbitMQ
- Eureka Server (Service Discovery)
- Spring Cloud Gateway
- MapStruct
- Spring Data JPA
- Spring Validation
- Java Mail Sender
- ITextPDF
- Mailtrap
- Docker / Podman
- Postman / DBBeaver
- Open Api (Swagger)
- Junit / Mockito
- Jacoco + informe en HTML 
- SonarQube (Revisión de cobertura)

---

## 📌 Módulo: Acreditaciones (Accreditation)

Este módulo forma parte de una arquitectura de microservicios orientada a eventos. Está diseñado para gestionar solicitudes de acreditación, validando que el Punto de Venta esté activo antes de registrar la acreditación. Hace uso de Redis para cachear resultados y RabbitMQ para emitir eventos cuando se crea una nueva acreditación.

### ⚙️ Tecnologías Utilizadas

- **Spring Boot**: Framework principal.
- **Spring Data JPA**: Persistencia en base de datos relacional.
- **Spring Validation**: Validación de entradas.
- **DTOs + MapStruct**: Separación entre lógica interna y modelo expuesto.
- **Redis**: Cacheo de resultados (acreditaciones consultadas).
- **RabbitMQ**: Comunicación asincrónica a través de eventos (al crear acreditaciones).
- **RestTemplate**: Consulta remota al microservicio de Punto de Venta para validar que esté activo.
- **Custom Exception Handling**: Manejador centralizado de errores mediante `ApplicationException`.
- **Response Wrapper (ApiResponseDTO)**: Formato unificado para las respuestas de la API.

---

### 🧩 Funcionalidades del Controlador `AccreditationController`

#### POST `/api/accreditations/create`
➤ Registra una nueva acreditación:

- Recibe un DTO validado (`AccreditationRequestDTO`).
- Valida que el `pointOfSaleId` sea válido y que esté activo a través del `PointOfSaleRestTemplate`.
- Persiste la acreditación con nombre del punto de venta y fecha de recepción.
- Emite un evento al exchange de RabbitMQ (`AccreditationCreatedEvent`) para su procesamiento posterior.
- A traves de este evento asincrónico se comunica con emailRabbitMQ Microservice con el objetivo de enviar un PDF con las Acreditaciones guardadss al usuario.
- Retorna la acreditación guardada como `AccreditationResponseDTO`.

#### GET `/api/accreditations/getAll`
➤ Lista todas las acreditaciones:

- Intenta recuperar los datos desde Redis usando `AccreditationCache`.
- Si no hay datos en cache, consulta la base de datos y los transforma en DTOs.
- Cachea los resultados para futuras consultas.
- Retorna una lista de `AccreditationResponseDTO`.

---

### 🧠 Detalles Técnicos del Servicio `AccreditationServiceImpl`

Este servicio encapsula toda la lógica de negocio relacionada con las acreditaciones.

- **Validación de punto de venta**: Antes de guardar una acreditación, consulta el microservicio de puntos de venta utilizando un cliente REST. Solo se permite registrar acreditaciones si el punto de venta existe y está activo.
  
- **Eventos con RabbitMQ**: Después de guardar una acreditación válida, se lanza un evento asincrónico con RabbitMQ a emailRabbitMQ Microservice con el objetivo de enviar un PDF con las Acreditaciones guardadas al usuario.

- **Cacheo con Redis**: 
  - `getAllAccreditations()` verifica si hay datos cacheados con `AccreditationCache`.
  - Si no hay, consulta la base de datos y almacena los resultados en Redis para mejorar el rendimiento de futuras consultas.

- **Transformación con MapStruct**:
  - Convierte entre `Accreditation`, `AccreditationRequestDTO`, `AccreditationResponseDTO` y `AccreditationCreatedEvent` de forma limpia y automática.

- **Errores controlados**: Se lanza una `ApplicationException` en caso de validaciones fallidas o errores inesperados.

---

## 📨 Muestra del PDF que se le envia al email del usuario una vez creada la acreditación

![Captura de pantalla 2025-04-14 193700](https://github.com/user-attachments/assets/2471d590-b0c4-4a55-b9ce-8b5439f10db3)

---

## 🔄 Flujo General
```
Cliente HTTP
  ⬇
Controller (DTO Validado)
  ⬇
Service(Verifica POS)
  ⬇
RestTemplate → PointOfSale Service
  ⬇
Validación OK
  ⬇
Mapper: DTO → Entidad
  ⬇
Base de Datos (JPA)
  ⬇
Mapper: Entidad → Event DTO
  ⬇
RabbitTemplate → RabbitMQ Exchange
  ⬇
Respuesta al Cliente
```
---

## 🚀 Levantar el Proyecto con Podman Compose

### 🔸 Pre-requisitos

- Tener instalado `Podman` y `Podman Compose`.
- Crear un archivo `.env` con las siguientes variables:
```.env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=myappdb

SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

SECRET_KEY=B374A26A71421437AA024E4FADD5B478FDFF1A8EA6FF12F6FB65AF2720B59CCF
```

- docker-compose.yml:

```
version: '3.8'

services:
  postgres:
    image: postgres:16
    container_name: springboot_postgres
    restart: always
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - spring-network

  redis:
    image: redis:7.2
    container_name: redis_cache
    restart: always
    ports:
      - "6379:6379"
    networks:
      - spring-network

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    restart: always
    ports:
      - "5672:5672"   # Puerto de conexión para microservicios
      - "15672:15672" # Puerto de panel web de administración
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    networks:
      - spring-network

  eureka:
    build: ./eureka_server
    container_name: eureka_server
    ports:
      - "8761:8761"
    networks:
      - spring-network

  gateway:
    build: ./cloud-gateway
    container_name: gateway_service
    ports:
      - "8080:8080"
    depends_on:
      - eureka
    environment:
      - SECRET_KEY=${SECRET_KEY}
    networks:
      - spring-network

  pointsalecost:
    build: ./Point_of_Sale_Cost-Microservice
    container_name: pointsalecost_service
    depends_on:
      - postgres
      - redis
      - eureka
    environment:
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
    networks:
      - spring-network

  accreditations:
    build: ./Accreditation-Microservice
    container_name: accreditations_service
    depends_on:
      - postgres
      - redis
      - eureka
      - rabbitmq
    environment:
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
    networks:
      - spring-network

  usermicroservice:
    build: ./Users-Microservice
    container_name: usermicroservice_service
    depends_on:
      - postgres
      - redis
      - eureka
      - rabbitmq
    environment:
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - SECRET_KEY=${SECRET_KEY}
    networks:
      - spring-network

  emailrabbitmq:
    build: ./emailRabbitMQ-Microservice
    container_name: email_rabbitmq_service
    depends_on:
      - rabbitmq
    environment:
      - SECRET_KEY=${SECRET_KEY}
    ports:
      - "8084:8084"
    networks:
      - spring-network

volumes:
  postgres_data:

networks:
  spring-network:
    driver: bridge

```

- Comando para construir y levantar con Podman Compose:
`podman compose up --build`

## ✅ Estructura de la Carpeta

![Estructura de carpetas](https://github.com/user-attachments/assets/b6ff7ad2-9a19-40d1-93d3-4d98b37054b8)


# ⚙️ Test unitarios

## Jacoco + informe en HTML

![accreditation JaCoCo test](https://github.com/user-attachments/assets/f90c3f72-b5b8-435f-922c-6b59b4984afc)


##  Sonar Qube

![Sonar Qube accreditation New Code](https://github.com/user-attachments/assets/face789f-167a-4693-9520-212cae6499ea)


![Sonar Qube accreditation Overall Code](https://github.com/user-attachments/assets/9bb9c719-9dde-4676-9591-c00bef0a2a46)


## 🧩 Swagger | Open API


Endpoint swagger: http://localhost:8080/swagger-ui/index.html

![Accreditation swagger](https://github.com/user-attachments/assets/0f5d2949-7a75-40b8-8b6a-38b9d86ba20d)
